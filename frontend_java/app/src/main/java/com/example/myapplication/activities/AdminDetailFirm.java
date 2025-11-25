package com.example.myapplication.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.adapters.ImageFirmAdapter;
import com.example.myapplication.models.DetailFirm;
import com.example.myapplication.models.ImageFirm;
import com.example.myapplication.models.StatusMessage;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiFirmService;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDetailFirm extends AppCompatActivity {
    private final int DELETE_FIRM_REQUEST_CODE = 6;
    String accessToken;
    int position = 0; // Current position in the ViewPager
    ActivityResultLauncher<Intent> updateFirmLauncher;
    private DetailFirm detailFirm;
    private ImageFirmAdapter imageAdapter;
    private ViewPager2 viewPager;
    ImageView imageBack, imageFirmShow;
    TextView nameFirmTextView, descriptionFirmTextView, startedFirmTextView;
    TextView textRating;
    TextView textReadMore;
    TextView textRuntime;
    Button btnBroadcast;
    Button btnUpdate, btnDelete;

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_detail_firm);
        accessToken = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("access_token", null);
        Log.d("AdminDetailFirm", "Access Token in detail firm: " + accessToken);
        if(accessToken == null) {
            Toast.makeText(this, "Access token not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setElementsByID();
        setUpdateFirmLauncher();

        // get the firm ID from the intent
        int firmId = getIntent().getIntExtra("firm_id", -1);
        position = getIntent().getIntExtra("position", -1); // Get the position if needed


        // Set up listeners for the back button and read more description
        ListenerSetupBackButton();
        ListenerReadMoreDescription();
        ListenerDeleteButton();

        // Log the received firm ID for debugging



        // Initialize your views and set up any necessary data binding or listeners here
        // Get the firm ID from the intent
        if (firmId != -1) {
            loadFirmDetail(String.valueOf(firmId)); // Load firm details using the ID
        } else {
            Toast.makeText(this, "Lỗi mã phim", Toast.LENGTH_SHORT).show();
            Log.e("UserDetailFirm", "Invalid film ID received");
            finish();
        }
        ListenerBoadcast(firmId); // Set up listener for booking tickets

    }

    void setElementsByID() {
        // Initialize your views here
        viewPager = findViewById(R.id.sliderViewPager);
        imageBack = findViewById(R.id.imageBack);
        imageFirmShow = findViewById(R.id.imageFirmShow);
        nameFirmTextView = findViewById(R.id.textName);
        descriptionFirmTextView = findViewById(R.id.textDescription);
        startedFirmTextView = findViewById(R.id.textStarted);
        textRating = findViewById(R.id.textRating);
        textReadMore = findViewById(R.id.textReadMore);
        textRuntime = findViewById(R.id.textRuntime);
        btnBroadcast = findViewById(R.id.btnBroadcast);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);

        ListenerUpdateButton();
    }

    private void loadFirmDetail(String id) {
        // Implement the logic to load firm details here
        // This could involve making a network request to fetch firm data
        // and then updating the UI with that data.
        ApiFirmService apiFirmService = ApiClient.getRetrofit().create(ApiFirmService.class);
        Call<DetailFirm> call = apiFirmService.getFirmById("Bearer "+ accessToken, id); // Replace 1 with the actual firm ID you want to fetch

        call.enqueue(new Callback<DetailFirm>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<DetailFirm> call, @NonNull Response<DetailFirm> response) {
                if (response.isSuccessful() && response.body() != null) {
                    detailFirm = response.body();
                    Log.e("API_RESPONSE", "Response body: " + detailFirm.getImages().size());
                    List<ImageFirm> imageArray = detailFirm.getImages();
                    imageAdapter = new ImageFirmAdapter(imageArray);
                    viewPager.setAdapter(imageAdapter);
                    Glide.with(imageFirmShow)
                            .load(detailFirm.getThumbnailPath())
                            .error(R.drawable.default_img) // Replace with your default image resource
                            .into(imageFirmShow);
                    nameFirmTextView.setText(detailFirm.getName());
                    descriptionFirmTextView.setText(detailFirm.getDescription());
                    startedFirmTextView.setText("Started on: "+ detailFirm.getStartDate());
                    textRating.setText("Rating: " + detailFirm.getRating());
                    textRuntime.setText(detailFirm.getRuntime() + " min");
                    Log.e("API_RESPONSE", "Response code: " + response.code());
                } else {
                    Toast.makeText(AdminDetailFirm.this, "Không lấy được dữ liệu", Toast.LENGTH_SHORT).show();
                }

            }

            public void onFailure(Call<DetailFirm> call, Throwable t) {
                Toast.makeText(AdminDetailFirm.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API_ERROR", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    //    xử lý sự kiện click của nút back
    private void ListenerSetupBackButton() {
        imageBack.setOnClickListener(v -> {
            // Hoặc bạn có thể sử dụng finish() để đóng activity hiện tại
            finish();
        });
    }

    private void ListenerReadMoreDescription() {
        // Implement the logic to show more description or details about the firm
        // This could involve expanding a TextView or navigating to another screen
        Toast.makeText(this, "Read more clicked", Toast.LENGTH_SHORT).show();
        textReadMore.setOnClickListener(new View.OnClickListener() {
            boolean isExpanded = false; // Biến theo dõi trạng thái

            @Override
            public void onClick(View v) {
                if (isExpanded) {
                    // Thu gọn lại
                    descriptionFirmTextView.setMaxLines(4);
                    textReadMore.setText(R.string.read_more);
                } else {
                    // Mở rộng
                    descriptionFirmTextView.setMaxLines(Integer.MAX_VALUE);
                    textReadMore.setText(R.string.read_less); // Thêm string này trong strings.xml
                }
                isExpanded = !isExpanded;
            }
        });
    }


    private void ListenerBoadcast(Integer firmId) {
        // Implement the logic to handle booking tickets for the firm
        // This could involve navigating to a booking screen or showing a dialog
        btnBroadcast.setOnClickListener(v -> {
            Toast.makeText(AdminDetailFirm.this, "Booking ticket for " + detailFirm.getName(), Toast.LENGTH_SHORT).show();
            // Add your booking logic here
            Intent intent = new Intent(AdminDetailFirm.this, AdminActivityListBroadcast.class); // Replace with your actual Booking Activity
            intent.putExtra("firmId", firmId);
            startActivity(intent);
        });
    }





//    Listeners for update and delete buttons

    private void setUpdateFirmLauncher(){
        updateFirmLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result ->{
                    if (result.getResultCode() == RESULT_OK) {
                        // Handle the result from the update activity
                        Intent data = result.getData();
                        if (data != null) {
                            int firmId = data.getIntExtra("firm_id", -1);
                            String status = data.getStringExtra("status");
                            if (firmId != -1 && status != null) {
                                // Reload the firm details after update
                                loadFirmDetail(String.valueOf(firmId));
                                Toast.makeText(AdminDetailFirm.this, "Cập nhật thành công: " + status, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AdminDetailFirm.this, "Cập nhật không thành công", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );
    }


    public void ListenerUpdateButton() {
        btnUpdate.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDetailFirm.this, AdminActivityUpdateFirm.class);
            intent.putExtra("firm_id", detailFirm.getId());
            intent.putExtra("thumbnail_url", detailFirm.getThumbnailPath());
            intent.putExtra("name", detailFirm.getName());
            intent.putExtra("description", detailFirm.getDescription());
            intent.putExtra("rating", detailFirm.getRating());
            intent.putExtra("rating_count", detailFirm.getRatingCount());
            intent.putExtra("runtime", detailFirm.getRuntime());


            updateFirmLauncher.launch(intent);
        });
    }

    public void ListenerDeleteButton() {
        btnDelete.setOnClickListener(v -> {
            AlertDeleteFirm();
        });
    }
    void AlertDeleteFirm() {
        new AlertDialog.Builder(AdminDetailFirm.this)
                .setTitle("Xác nhận Xóa phim")
                .setMessage("Bạn có chắc chắn muốn xóa không?")
                .setPositiveButton("Chắc chắn", (dialog, which) -> {
                    // Xử lý xóa phòng
                    DeleteFirmByApi();
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    dialog.dismiss(); // Đóng dialog nếu chọn Cancel
                })
                .show();
    }

    void DeleteFirmByApi(){
            ApiFirmService apiFirmService = ApiClient.getRetrofit().create(ApiFirmService.class);
            Call<StatusMessage> call = apiFirmService.deleteFirm("Bearer " + accessToken, detailFirm.getId());
            call.enqueue(
                    new Callback<StatusMessage>() {
                        @Override
                        public void onResponse(@NonNull Call<StatusMessage> call, @NonNull Response<StatusMessage> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                StatusMessage statusMessage = response.body();
                                Intent intent = new Intent();
                                intent.putExtra("position", position);
                                intent.putExtra("status", statusMessage.getMessage());
                                setResult(DELETE_FIRM_REQUEST_CODE, intent);
                                finish();

                            } else {
                                Toast.makeText(AdminDetailFirm.this, "Phim có lịch chiếu không thể xóa", Toast.LENGTH_SHORT).show();
                                Log.e("API_ERROR", "Response code: " + response.code() + ", message: " + response.message());
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<StatusMessage> call, @NonNull Throwable t) {
                            Toast.makeText(AdminDetailFirm.this, "Phim có lịch chiếu không thể xóa", Toast.LENGTH_SHORT).show();
                            Log.e("API_ERROR", Objects.requireNonNull(t.getMessage()));
                        }
                    }
            );

    }
}
