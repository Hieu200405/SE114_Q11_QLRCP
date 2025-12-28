package com.example.myapplication.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiFilmService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserDetailFilm extends  AppCompatActivity {
    // This class is currently empty, but you can add methods and properties as needed
    // to handle user details related to films.
    String accessToken;
    private DetailFilm detailFilm;
    private ImageFilmAdapter imageAdapter;
    private ViewPager2 viewPager;
    ImageView imageBack; // Assuming you have a back button in your layout
    ImageView imageFilmShow;
    TextView nameFilmTextView;
    TextView descriptionFilmTextView;
    TextView startedFilmTextView;
    TextView textRating;
    TextView textReadMore;
    TextView textRuntime;
    Button btnBookTicket;
    private ApiFilmService apiFilmService;
    private RecyclerView rvReviews;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList = new ArrayList<>();

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_detail_film); // Ensure you have a layout file named user_detail_film.xml
        accessToken = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("access_token", null);
        if (accessToken == null) {
            Toast.makeText(this, "Access token not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewPager = findViewById(R.id.sliderViewPager); // Initialize your ViewPager2 here
        imageBack = findViewById(R.id.imageBack); // Initialize your back button here
        imageFilmShow = findViewById(R.id.imageFilmShow); // Initialize your film image view here
        nameFilmTextView = findViewById(R.id.textName); // Initialize your film name TextView here
        descriptionFilmTextView = findViewById(R.id.textDescription); // Initialize your film description TextView here
        startedFilmTextView = findViewById(R.id.textStarted); // Initialize your film start date TextView here
        textRating = findViewById(R.id.textRating);
        textReadMore = findViewById(R.id.textReadMore); // Initialize your read more TextView here
        textRuntime = findViewById(R.id.textRuntime); // Initialize your runtime TextView here
        btnBookTicket = findViewById(R.id.btnBookTicket); // Initialize your book ticket button here

        // get the film ID from the intent
        int filmId = getIntent().getIntExtra("film_id", -1);


        // Set up listeners for the back button and read more description
        ListenerSetupBackButton();
        ListenerReadMoreDescription();
        ListenerBookTicket(filmId); // Set up listener for booking ticket

         // Log the received film ID for debugging



        // Initialize your views and set up any necessary data binding or listeners here
         // Get the film ID from the intent
        if (filmId != -1) {
            loadFilmDetail(String.valueOf(filmId)); // Load film details using the ID
        } else {
            Toast.makeText(this, "Lỗi Mã Phim", Toast.LENGTH_SHORT).show();
            Log.e("UserDetailFilm", "Invalid film ID received");
            finish();
        }

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
                    startedFilmTextView.setText("Date on: "+ detailFilm.getStartDate());
                    textRating.setText("Rating: " + detailFilm.getRating());
                    textRuntime.setText(detailFilm.getRuntime() + " min");
                    Log.e("API_RESPONSE", "Response code: " + response.code());
                } else {
                    Toast.makeText(UserDetailFilm.this, "Không lấy được dữ liệu", Toast.LENGTH_SHORT).show();
                }

            }

            public void onFailure(Call<DetailFilm> call, Throwable t) {
                Toast.makeText(UserDetailFilm.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API_ERROR", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    private void loadReviews(int filmId) {
        apiFilmService.getFilmReviews(filmId).enqueue(new Callback<ReviewResponse>() {
            @Override
            public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ReviewResponse reviewResponse = response.body();

                    if ("00".equals(reviewResponse.getCode())) {
                        List<Review> data = reviewResponse.getData();
                        if (data != null) {
                            reviewList.clear();
                            reviewList.addAll(data);
                            reviewAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("REVIEW_ERROR", "Lỗi từ server: " + reviewResponse.getDesc());
                    }
                }
            }

            @Override
            public void onFailure(Call<ReviewResponse> call, Throwable t) {
                Log.e("API_ERROR", "Không thể kết nối API reviews: " + t.getMessage());
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


    private void ListenerBookTicket(Integer filmId) {
        // Implement the logic to handle booking tickets for the film
        // This could involve navigating to a booking screen or showing a dialog
        btnBookTicket.setOnClickListener(v -> {
            Toast.makeText(UserDetailFilm.this, "Đặt vé cho phim " + detailFilm.getName(), Toast.LENGTH_SHORT).show();
            // Add your booking logic here
            Intent intent = new Intent(UserDetailFilm.this, UserShowListBroadcast.class); // Replace with your actual Booking Activity
            intent.putExtra("filmId", filmId);
            startActivity(intent);
        });
    }

}
