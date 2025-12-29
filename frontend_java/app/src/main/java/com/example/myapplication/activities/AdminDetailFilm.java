package com.example.myapplication.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.adapters.ImageFilmAdapter;
import com.example.myapplication.adapters.ReviewAdapter;
import com.example.myapplication.models.DetailFilm;
import com.example.myapplication.models.ImageFilm;
import com.example.myapplication.models.Review;
import com.example.myapplication.models.ReviewResponse;
import com.example.myapplication.models.StatusMessage;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiFilmService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDetailFilm extends AppCompatActivity {
    private final int DELETE_FILM_REQUEST_CODE = 6;
    String accessToken;
    int position = 0; // Current position in the ViewPager
    ActivityResultLauncher<Intent> updateFiLmLauncher;
    private DetailFilm detailFilm;
    private ImageFilmAdapter imageAdapter;
    private ViewPager2 viewPager;
    ImageView imageBack, imageFilmShow;
    TextView nameFilmTextView, descriptionFilmTextView, startedFilmTextView;
    TextView textRating;
    TextView textReadMore;
    TextView textRuntime;
    Button btnBroadcast;
    Button btnUpdate, btnDelete;
    private RecyclerView rvReviews;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;
    private ApiFilmService apiFilmService;

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_detail_film);
        accessToken = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("access_token", null);
        Log.d("AdminDetailFilm", "Access Token in detail film: " + accessToken);
        if(accessToken == null) {
            Toast.makeText(this, "Access token not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiFilmService = ApiClient.getRetrofit().create(ApiFilmService.class);
        rvReviews = findViewById(R.id.rvReviews);
        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(reviewList);

        if (rvReviews != null) {
            rvReviews.setLayoutManager(new LinearLayoutManager(this));
            rvReviews.setNestedScrollingEnabled(false);
            rvReviews.setAdapter(reviewAdapter);
        } else {
            Log.e("DEBUG", "Không tìm thấy rvReviews trong layout!");
        }

        setElementsByID();
        setUpdateFilmLauncher();

        // get the film ID from the intent
        int filmId = getIntent().getIntExtra("film_id", -1);
        position = getIntent().getIntExtra("position", -1); // Get the position if needed


        // Set up listeners for the back button and read more description
        ListenerSetupBackButton();
        ListenerReadMoreDescription();
        ListenerDeleteButton();

        // Log the received film ID for debugging



        // Initialize your views and set up any necessary data binding or listeners here
        // Get the film ID from the intent
        if (filmId != -1) {
            loadFilmDetail(String.valueOf(filmId)); // Load film details using the ID
        } else {
            Toast.makeText(this, "Lỗi mã phim", Toast.LENGTH_SHORT).show();
            Log.e("UserDetailFilm", "Invalid film ID received");
            finish();
        }
        ListenerBoadcast(filmId); // Set up listener for booking tickets

        if (filmId != -1) {
            loadReviews(filmId);
        }

    }

    void setElementsByID() {
        // Initialize your views here
        viewPager = findViewById(R.id.sliderViewPager);
        imageBack = findViewById(R.id.imageBack);
        imageFilmShow = findViewById(R.id.imageFilmShow);
        nameFilmTextView = findViewById(R.id.textName);
        descriptionFilmTextView = findViewById(R.id.textDescription);
        startedFilmTextView = findViewById(R.id.textStarted);
        textRating = findViewById(R.id.textRating);
        textReadMore = findViewById(R.id.textReadMore);
        textRuntime = findViewById(R.id.textRuntime);
        btnBroadcast = findViewById(R.id.btnBroadcast);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);

        ListenerUpdateButton();
    }

    private void loadFilmDetail(String id) {
        // Implement the logic to load film details here
        // This could involve making a network request to fetch film data
        // and then updating the UI with that data.
        ApiFilmService apiFilmService = ApiClient.getRetrofit().create(ApiFilmService.class);
        Call<DetailFilm> call = apiFilmService.getFilmById("Bearer "+ accessToken, id); // Replace 1 with the actual film ID you want to fetch

        call.enqueue(new Callback<DetailFilm>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<DetailFilm> call, @NonNull Response<DetailFilm> response) {
                if (response.isSuccessful() && response.body() != null) {
                    detailFilm = response.body();
                    Log.e("API_RESPONSE", "Response body: " + detailFilm.getImages().size());
                    List<ImageFilm> imageArray = detailFilm.getImages();
                    imageAdapter = new ImageFilmAdapter(imageArray);
                    viewPager.setAdapter(imageAdapter);
                    Glide.with(imageFilmShow)
                            .load(detailFilm.getThumbnailPath())
                            .error(R.drawable.default_img) // Replace with your default image resource
                            .into(imageFilmShow);
                    nameFilmTextView.setText(detailFilm.getName());
                    descriptionFilmTextView.setText(detailFilm.getDescription());
                    startedFilmTextView.setText("Started on: "+ detailFilm.getStartDate());
                    textRating.setText("Rating: " + detailFilm.getRating());
                    textRuntime.setText(detailFilm.getRuntime() + " min");
                    Log.e("API_RESPONSE", "Response code: " + response.code());
                } else {
                    Toast.makeText(AdminDetailFilm.this, "Không lấy được dữ liệu", Toast.LENGTH_SHORT).show();
                }

            }

            public void onFailure(Call<DetailFilm> call, Throwable t) {
                Toast.makeText(AdminDetailFilm.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API_ERROR", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    private void loadReviews(int filmId) {
        apiFilmService.getFilmReviews(filmId).enqueue(new Callback<ReviewResponse>() {
            @Override
            public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ReviewResponse res = response.body();
                    if ("00".equals(res.getCode()) && res.getData() != null) {
                        reviewList.clear();
                        reviewList.addAll(res.getData());
                        reviewAdapter.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onFailure(Call<ReviewResponse> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
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
        // Implement the logic to show more description or details about the film
        // This could involve expanding a TextView or navigating to another screen
        Toast.makeText(this, "Read more clicked", Toast.LENGTH_SHORT).show();
        textReadMore.setOnClickListener(new View.OnClickListener() {
            boolean isExpanded = false; // Biến theo dõi trạng thái

            @Override
            public void onClick(View v) {
                if (isExpanded) {
                    // Thu gọn lại
                    descriptionFilmTextView.setMaxLines(4);
                    textReadMore.setText(R.string.read_more);
                } else {
                    // Mở rộng
                    descriptionFilmTextView.setMaxLines(Integer.MAX_VALUE);
                    textReadMore.setText(R.string.read_less); // Thêm string này trong strings.xml
                }
                isExpanded = !isExpanded;
            }
        });
    }


    private void ListenerBoadcast(Integer filmId) {
        // Implement the logic to handle booking tickets for the film
        // This could involve navigating to a booking screen or showing a dialog
        btnBroadcast.setOnClickListener(v -> {
            Toast.makeText(AdminDetailFilm.this, "Booking ticket for " + detailFilm.getName(), Toast.LENGTH_SHORT).show();
            // Add your booking logic here
            Intent intent = new Intent(AdminDetailFilm.this, AdminActivityListBroadcast.class); // Replace with your actual Booking Activity
            intent.putExtra("filmId", filmId);
            startActivity(intent);
        });
    }





//    Listeners for update and delete buttons

    private void setUpdateFilmLauncher(){
        updateFiLmLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result ->{
                    if (result.getResultCode() == RESULT_OK) {
                        // Handle the result from the update activity
                        Intent data = result.getData();
                        if (data != null) {
                            int filmId = data.getIntExtra("film_id", -1);
                            String status = data.getStringExtra("status");
                            if (filmId != -1 && status != null) {
                                // Reload the film details after update
                                loadFilmDetail(String.valueOf(filmId));
                                Toast.makeText(AdminDetailFilm.this, "Cập nhật thành công: " + status, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AdminDetailFilm.this, "Cập nhật không thành công", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );
    }


    public void ListenerUpdateButton() {
        btnUpdate.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDetailFilm.this, AdminActivityUpdateFilm.class);
            intent.putExtra("film_id", detailFilm.getId());
            intent.putExtra("thumbnail_url", detailFilm.getThumbnailPath());
            intent.putExtra("name", detailFilm.getName());
            intent.putExtra("description", detailFilm.getDescription());
            intent.putExtra("rating", detailFilm.getRating());
            intent.putExtra("rating_count", detailFilm.getRatingCount());
            intent.putExtra("runtime", detailFilm.getRuntime());


            updateFiLmLauncher.launch(intent);
        });
    }

    public void ListenerDeleteButton() {
        btnDelete.setOnClickListener(v -> {
            AlertDeleteFilm();
        });
    }
    void AlertDeleteFilm() {
        new AlertDialog.Builder(AdminDetailFilm.this)
                .setTitle("Xác nhận Xóa phim")
                .setMessage("Bạn có chắc chắn muốn xóa không?")
                .setPositiveButton("Chắc chắn", (dialog, which) -> {
                    // Xử lý xóa phòng
                    DeleteFilmByApi();
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    dialog.dismiss(); // Đóng dialog nếu chọn Cancel
                })
                .show();
    }

    void DeleteFilmByApi(){
            ApiFilmService apiFilmService = ApiClient.getRetrofit().create(ApiFilmService.class);
            Call<StatusMessage> call = apiFilmService.deleteFilm("Bearer " + accessToken, detailFilm.getId());
            call.enqueue(
                    new Callback<StatusMessage>() {
                        @Override
                        public void onResponse(@NonNull Call<StatusMessage> call, @NonNull Response<StatusMessage> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                StatusMessage statusMessage = response.body();
                                Intent intent = new Intent();
                                intent.putExtra("position", position);
                                intent.putExtra("status", statusMessage.getMessage());
                                setResult(DELETE_FILM_REQUEST_CODE, intent);
                                finish();

                            } else {
                                Toast.makeText(AdminDetailFilm.this, "Phim có lịch chiếu không thể xóa", Toast.LENGTH_SHORT).show();
                                Log.e("API_ERROR", "Response code: " + response.code() + ", message: " + response.message());
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<StatusMessage> call, @NonNull Throwable t) {
                            Toast.makeText(AdminDetailFilm.this, "Phim có lịch chiếu không thể xóa", Toast.LENGTH_SHORT).show();
                            Log.e("API_ERROR", Objects.requireNonNull(t.getMessage()));
                        }
                    }
            );

    }
}
