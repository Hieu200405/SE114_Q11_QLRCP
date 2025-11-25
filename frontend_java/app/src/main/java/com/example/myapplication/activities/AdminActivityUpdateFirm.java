package com.example.myapplication.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.models.FirmShow;
import com.example.myapplication.models.FirmUpdateRequest;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiFirmService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AdminActivityUpdateFirm extends AppCompatActivity {
    String accessToken;
    ImageView imageThumbnail, imageBack;
    private ActivityResultLauncher<String> pickImageLauncher;
    int firmId;
    Button buttonPickThumbnail, buttonUpdateFirm;
    EditText editFirmName, editDescription, editRunningTime, editRating, editRatingCount;
    Uri thumbnailImageUri;
    String thumbnailImageUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_update_firm);
        accessToken = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("access_token", null);
        setElementById();
        setPickImageLauncher();
        firmId = getIntent().getIntExtra("firm_id", -1);
        String thumbnailUrl = getIntent().getStringExtra("thumbnail_url");

        /*
        intent.putExtra("name", detailFirm.getName());
            intent.putExtra("description", detailFirm.getDescription());
            intent.putExtra("rating", detailFirm.getRating());
            intent.putExtra("rating_count", detailFirm.getRatingCount());
            intent.putExtra("runtime", detailFirm.getRuntime());
         */
        String name = getIntent().getStringExtra("name");
        String description = getIntent().getStringExtra("description");
        double rating = getIntent().getDoubleExtra("rating", 0.0);
        int ratingCount = getIntent().getIntExtra("rating_count", 0);
        int runningTime = getIntent().getIntExtra("runtime", 0);



        Glide.with(this)
                        .load(thumbnailUrl)
                        .error(R.drawable.default_img)
                        .into(imageThumbnail);
        editFirmName.setText(name);
        editDescription.setText(description);
        editRunningTime.setText(String.valueOf(runningTime));
        editRating.setText(String.valueOf(rating));
        editRatingCount.setText(String.valueOf(ratingCount));


        imageThumbnail.setOnClickListener(v -> {
            pickImageLauncher.launch("image/*");
        });
        buttonPickThumbnail.setOnClickListener(v -> {
            pickImageLauncher.launch("image/*");
        });

        ListenUpdateFirmButton();
    }


    private void setElementById(){
        imageThumbnail = findViewById(R.id.imageThumbnail);
        imageBack = findViewById(R.id.imageBack);
        buttonPickThumbnail = findViewById(R.id.buttonPickThumbnail);
        buttonUpdateFirm = findViewById(R.id.buttonUpdateFirm);
        editFirmName = findViewById(R.id.editFirmName);
        editDescription = findViewById(R.id.editDescription);
        editRunningTime = findViewById(R.id.editRunningTime);
        editRating = findViewById(R.id.editRating);
        editRatingCount = findViewById(R.id.editRatingCount);

        imageBack.setOnClickListener(v -> {
            finish();
        });



    }

    private void setPickImageLauncher(){
        pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    Glide.with(this)
                            .load(uri)
                            .into(imageThumbnail);
                    thumbnailImageUri = uri; // Save the thumbnail image URI
                }
            }
        );
    }

    interface UploadCallback {
        void onSuccess(String imageUrl);
        void onFailure();
    }


    private void uploadToCloudinary(Uri imageUri, UploadCallback callback) {
        String cloudName = "dfu6ly3og";
        String uploadPreset = "android_upload";
        String url = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload";

        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }

            byte[] imageBytes = byteBuffer.toByteArray();
            inputStream.close();

            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "image.jpg",
                            RequestBody.create(imageBytes, MediaType.parse("image/*")))
                    .addFormDataPart("upload_preset", uploadPreset)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    callback.onFailure();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseData = response.body().string();
                        Log.d("UPLOAD_RESULT","UPLOAD_RESULT: " + responseData);
                        try {
                            JSONObject jsonObject = new JSONObject(responseData);
                            String url = jsonObject.getString("url");
                            Log.d("UPLOADED_URL", "Image URL: " + url);
                            callback.onSuccess(url);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callback.onFailure();
                        }

//                        Log.d("UPLOAD_RESULT", responseData);
                    } else {
                        Log.e("UPLOAD_RESULT", "Upload failed: " + response.code());
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            callback.onFailure();
        }
    }

    Boolean checkValidInput(String name, String description, String runningTime, String rating, String ratingCount) {
        if (name.isEmpty() || description.isEmpty() || runningTime.isEmpty() ||
                rating.isEmpty() || ratingCount.isEmpty() ) {
            Toast.makeText(this, "Vui lòng điền vào tất cả các trường", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            Integer.parseInt(runningTime);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Thời lượng chiếu không hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            Double.parseDouble(rating);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Rating phải là số", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            Integer.parseInt(ratingCount);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số lượng đánh giá phải là số", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (thumbnailImageUri == null) {
            Toast.makeText(this, "Vui lòng chọn một Thumbnail", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(Double.parseDouble(rating) < 0 || Double.parseDouble(rating) > 10){
            Toast.makeText(this, "Rating phải ở giữa 0 và 10", Toast.LENGTH_SHORT).show();
            return false;
        }

        return  true;
    }

    private void updateFirmByApi(FirmUpdateRequest firmUpdateRequest) {
        ApiFirmService apiFirmService = ApiClient.getRetrofit().create(ApiFirmService.class);
        retrofit2.Call<FirmShow> call = apiFirmService.updateFirm(accessToken, firmId, firmUpdateRequest);
        call.enqueue(
                new retrofit2.Callback<FirmShow>() {
                    @Override
                    public void onResponse(retrofit2.Call<FirmShow> call, retrofit2.Response<FirmShow> response) {
                        if (response.isSuccessful()) {
                            FirmShow updatedFirm = response.body();
                            Intent intent = new Intent();
                            intent.putExtra("firmId", firmId);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            Toast.makeText(AdminActivityUpdateFirm.this, "Cập nhật phim thất bại", Toast.LENGTH_SHORT).show();
                            Log.e("UPDATE_FIRM", "Error: " + response.errorBody());
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<FirmShow> call, Throwable t) {
                        Toast.makeText(AdminActivityUpdateFirm.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("UPDATE_FIRM", "Failure: ", t);
                    }
                }
        );
    }

    private void ListenUpdateFirmButton(){
        buttonUpdateFirm.setOnClickListener(v -> {
            String name = editFirmName.getText().toString().trim();
            String description = editDescription.getText().toString().trim();
            String runningTime = editRunningTime.getText().toString().trim();
            String rating = editRating.getText().toString().trim();
            String ratingCount = editRatingCount.getText().toString().trim();

            if (!checkValidInput(name, description, runningTime, rating, ratingCount)) {
                return;
            }

            uploadToCloudinary(thumbnailImageUri, new UploadCallback() {

                @Override
                public void onSuccess(String imageUrl) {
                    FirmUpdateRequest firmUpdateRequest = new FirmUpdateRequest(
                            name,
                            description,
                            imageUrl,
                            Double.parseDouble(rating),
                            Integer.parseInt(ratingCount),
                            Integer.parseInt(runningTime)
                    );
                    runOnUiThread(() -> updateFirmByApi(firmUpdateRequest));
                }

                @Override
                public void onFailure() {
                    runOnUiThread(() -> Toast.makeText(AdminActivityUpdateFirm.this, "Tải lên thất bại", Toast.LENGTH_SHORT).show());
                }
            });

        });
    }

}
