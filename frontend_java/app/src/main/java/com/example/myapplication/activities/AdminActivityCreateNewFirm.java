package com.example.myapplication.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.adapters.CoverImageAdapter;
import com.example.myapplication.models.DetailFirm;
import com.example.myapplication.models.FirmRequest;
import com.example.myapplication.models.FirmShow;

import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiFirmService;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;



import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;


public class AdminActivityCreateNewFirm extends AppCompatActivity {
    String accessToken;
    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<String> pickCoverImageLauncher;
    private Uri thumbnailImageUri;

    private Uri selectedImageUri;
    private String ThumbnailImageUrl;
    private List<String> coverImageUrls = new ArrayList<>();
    ImageView imageThumbnail, imageBack;
    Button buttonPickThumbnail, buttonCreateFirm;
    RecyclerView recyclerCoverImages;
    CoverImageAdapter coverImageAdapter;
    ArrayList<Uri> coverImageUris = new ArrayList<>();
    EditText editFirmName, editDescription, editRunningTime, editRating, editRatingCount, editStartDate, editEndDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_create_firm);
        accessToken = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("access_token", null);
        setElementById();
        // Initialize UI components and set up listeners here
        // For example, you might want to set up a button to create a new firm

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        Glide.with(this)
                                .load(uri)
                                .into(imageThumbnail);
                        thumbnailImageUri = uri; // Save the thumbnail image URI
                    }
                }
        );
        pickCoverImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        coverImageAdapter.addImage(uri);
                    }
                }
        );

        recyclerCoverImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        coverImageAdapter = new CoverImageAdapter(this,
                coverImageUris,
                () -> pickCoverImageLauncher.launch("image/*"),
                position -> {
                    coverImageAdapter.removeImage(position);
                });
        recyclerCoverImages.setAdapter(coverImageAdapter);

        imageThumbnail.setOnClickListener(v -> {
            pickImageLauncher.launch("image/*");
        });
        buttonPickThumbnail.setOnClickListener(v -> {
            pickImageLauncher.launch("image/*");
        });

    }

    void setElementById(){
        imageThumbnail = findViewById(R.id.imageThumbnail);
        buttonPickThumbnail = findViewById(R.id.buttonPickThumbnail);
        recyclerCoverImages = findViewById(R.id.recyclerCoverImages);
        editFirmName = findViewById(R.id.editFirmName);
        editDescription = findViewById(R.id.editDescription);
        editRunningTime = findViewById(R.id.editRunningTime);
        editRating = findViewById(R.id.editRating);
        editRatingCount = findViewById(R.id.editRatingCount);
        editStartDate = findViewById(R.id.editStartDate);
        editEndDate = findViewById(R.id.editEndDate);
        imageBack = findViewById(R.id.imageBack);
        buttonCreateFirm = findViewById(R.id.buttonCreateFirm);

        imageBack.setOnClickListener(v -> {
            finish();
         });

        buttonCreateFirm.setOnClickListener(
                v -> {
                    CreateFirm();
                }
        );

        editStartDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        editStartDate.setText(selectedDate);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });
        editEndDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        editEndDate.setText(selectedDate);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

    }


    private void uploadToCloudinary(Uri imageUri, boolean isThumbnail, CountDownLatch latch) {
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
                    // xử lý khi upload thất bại
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseData = response.body().string();
                        try {
                            JSONObject jsonObject = new JSONObject(responseData);
                            String imageUrl = jsonObject.getString("secure_url");  // hoặc "url" tùy field Cloudinary trả về

                            if (isThumbnail) {
                                Log.d("UPLOAD_RESULT", "Thumbnail URL: " + imageUrl);
                                ThumbnailImageUrl = imageUrl; // Lưu URL ảnh thumbnail
                            } else {
                                Log.d("UPLOAD_RESULT", "Cover Image URL: " + imageUrl);
                                coverImageUrls.add(imageUrl);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

//                        Log.d("UPLOAD_RESULT", responseData);
                    } else {
                        Log.e("UPLOAD_RESULT", "Upload failed: " + response.code());
                    }
                }
            });
            latch.countDown();

        } catch (Exception e) {
            e.printStackTrace();
            latch.countDown(); // giảm latch dù thất bại
        }
    }


    void CreateFirm(){
        try {
            int totalUploads = coverImageUris.size() + 1; // +1 for thumbnail
            CountDownLatch latch = new CountDownLatch(totalUploads);

            // Upload thumbnail
            uploadToCloudinary(thumbnailImageUri, true, latch);

            // Upload cover images
            for (Uri coverImageUri : coverImageUris) {
                uploadToCloudinary(coverImageUri, false, latch);
            }
            new Thread(() -> {
                try {
                    latch.await(); // block thread này tới khi latch về 0

                    // Sau khi tất cả upload xong
                    runOnUiThread(() -> {
                        // Tạo firm request
                        String name = editFirmName.getText().toString();
                        String description = editDescription.getText().toString();
                        String startDate = editStartDate.getText().toString();
                        String endDate = editEndDate.getText().toString();
                        String runningTimeStr = (editRunningTime.getText().toString());
                        String ratingStr = (editRating.getText().toString());
                        String ratingCountStr = (editRatingCount.getText().toString());

                        if (!checkValidInput(name, description, runningTimeStr, ratingStr, ratingCountStr, startDate, endDate)) {
                            return; // nếu không hợp lệ thì không tạo firm
                        }

                        int runningTime = Integer.parseInt(runningTimeStr);
                        double rating = Double.parseDouble(ratingStr);
                        int ratingCount = Integer.parseInt(ratingCountStr);
                        if(ThumbnailImageUrl == null || ThumbnailImageUrl.isEmpty()) {
                            Toast.makeText(this, "Tải lên thumbnail thất bại", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if(coverImageUrls.isEmpty()) {
                            Toast.makeText(this, "Tải lên ảnh bìa thất bại", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Log.d("UPLOAD_RESULT 1", "Thumbnail URL: " + ThumbnailImageUrl);
                        for (String coverImageUrl : coverImageUrls) {
                            Log.d("UPLOAD_RESULT 1", "Ảnh bìa URL: " + coverImageUrl);
                        }

                        FirmRequest firmRequest = new FirmRequest(
                                name,
                                description,
                                ThumbnailImageUrl,
                                startDate,
                                endDate,
                                rating,
                                ratingCount,
                                runningTime,
                                coverImageUrls
                        );
                        callApiCreateFirm(
                                firmRequest
                        );

                    });

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();


        }
        catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi Tạo Phim: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void callApiCreateFirm(FirmRequest firmRequest) {
        ApiFirmService apiFirmService = ApiClient.getRetrofit().create(ApiFirmService.class);
        retrofit2.Call<FirmShow> call = apiFirmService.createFirm("Bearer " + accessToken, firmRequest);
        call.enqueue(new retrofit2.Callback<FirmShow>() {
            @Override
            public void onResponse(retrofit2.Call<FirmShow> call, retrofit2.Response<FirmShow> response) {
                if (response.isSuccessful()) {
                    FirmShow firmShow = response.body();
                    Toast.makeText(AdminActivityCreateNewFirm.this, "Tạo Phim Thành Công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("new_firm", firmShow);
                    setResult(4, intent);
                    finish(); // Close the activity after successful creation
                } else {
                    Toast.makeText(AdminActivityCreateNewFirm.this, "Tạo Phim Thất Bại: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<FirmShow> call, Throwable t) {
                Toast.makeText(AdminActivityCreateNewFirm.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    Boolean checkValidInput(String name, String description, String runningTime, String rating, String ratingCount, String startDate, String endDate) {
        if (name.isEmpty() || description.isEmpty() || runningTime.isEmpty() ||
                rating.isEmpty() || ratingCount.isEmpty() || startDate.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ các trường", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            Integer.parseInt(runningTime);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Thời lượng chiếu phải là một số hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            Double.parseDouble(rating);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Xếp hạng phải là một số hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            Integer.parseInt(ratingCount);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số lượng xếp hạng phải là một số hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (thumbnailImageUri == null) {
            Toast.makeText(this, "Vui lòng chọn một ảnh thumbnail", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (coverImageUrls.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một ảnh bìa", Toast.LENGTH_SHORT).show();
            return false;
        }
//        nếu thời gian bắt đầu lớn hơn thời gian kết thúc
//        time ở định dạng yyyy-MM-dd
        if (startDate.compareTo(endDate) > 0 && !startDate.isEmpty() && !endDate.isEmpty()) {
            Toast.makeText(this, "Ngày bắt đầu phải là trước ngày kết thúc", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(Double.parseDouble(rating) < 0 || Double.parseDouble(rating) > 10){
            Toast.makeText(this, "Xếp hạng phải nằm trong khoảng từ 0 đến 10", Toast.LENGTH_SHORT).show();
            return false;
        }

        return  true;
    }
}
