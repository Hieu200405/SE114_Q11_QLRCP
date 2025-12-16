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
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.adapters.ImageFilmAdapter;
import com.example.myapplication.models.DetailFilm;
import com.example.myapplication.models.ImageFilm;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiFilmService;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserDetailFirm extends  AppCompatActivity {
    // This class is currently empty, but you can add methods and properties as needed
    // to handle user details related to firms.
    String accessToken;
    private DetailFilm detailFilm;
    private ImageFilmAdapter imageAdapter;
    private ViewPager2 viewPager;
    ImageView imageBack; // Assuming you have a back button in your layout
    ImageView imageFirmShow;
    TextView nameFirmTextView;
    TextView descriptionFirmTextView;
    TextView startedFirmTextView;
    TextView textRating;
    TextView textReadMore;
    TextView textRuntime;
    Button btnBookTicket;

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
        imageFirmShow = findViewById(R.id.imageFilmShow); // Initialize your firm image view here
        nameFirmTextView = findViewById(R.id.textName); // Initialize your firm name TextView here
        descriptionFirmTextView = findViewById(R.id.textDescription); // Initialize your firm description TextView here
        startedFirmTextView = findViewById(R.id.textStarted); // Initialize your firm start date TextView here
        textRating = findViewById(R.id.textRating);
        textReadMore = findViewById(R.id.textReadMore); // Initialize your read more TextView here
        textRuntime = findViewById(R.id.textRuntime); // Initialize your runtime TextView here
        btnBookTicket = findViewById(R.id.btnBookTicket); // Initialize your book ticket button here

        // get the firm ID from the intent
        int firmId = getIntent().getIntExtra("firm_id", -1);


        // Set up listeners for the back button and read more description
        ListenerSetupBackButton();
        ListenerReadMoreDescription();
        ListenerBookTicket(firmId); // Set up listener for booking ticket

         // Log the received firm ID for debugging



        // Initialize your views and set up any necessary data binding or listeners here
         // Get the firm ID from the intent
        if (firmId != -1) {
            loadFirmDetail(String.valueOf(firmId)); // Load firm details using the ID
        } else {
            Toast.makeText(this, "Lỗi Mã Phim", Toast.LENGTH_SHORT).show();
            Log.e("UserDetailFirm", "Invalid firm ID received");
            finish();
        }

    }

    private void loadFirmDetail(String id) {
        // Implement the logic to load firm details here
        // This could involve making a network request to fetch firm data
        // and then updating the UI with that data.
        ApiFilmService apiFilmService = ApiClient.getRetrofit().create(ApiFilmService.class);
        Call<DetailFilm> call = apiFilmService.getFilmById("Bearer "+ accessToken, id); // Replace 1 with the actual firm ID you want to fetch

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
                    Glide.with(imageFirmShow)
                            .load(detailFilm.getThumbnailPath())
                            .error(R.drawable.default_img) // Replace with your default image resource
                            .into(imageFirmShow);
                    nameFirmTextView.setText(detailFilm.getName());
                    descriptionFirmTextView.setText(detailFilm.getDescription());
                    startedFirmTextView.setText("Date on: "+ detailFilm.getStartDate());
                    textRating.setText("Rating: " + detailFilm.getRating());
                    textRuntime.setText(detailFilm.getRuntime() + " min");
                    Log.e("API_RESPONSE", "Response code: " + response.code());
                } else {
                    Toast.makeText(UserDetailFirm.this, "Không lấy được dữ liệu", Toast.LENGTH_SHORT).show();
                }

            }

            public void onFailure(Call<DetailFilm> call, Throwable t) {
                Toast.makeText(UserDetailFirm.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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


    private void ListenerBookTicket(Integer firmId) {
        // Implement the logic to handle booking tickets for the firm
        // This could involve navigating to a booking screen or showing a dialog
        btnBookTicket.setOnClickListener(v -> {
            Toast.makeText(UserDetailFirm.this, "Đặt vé cho phim " + detailFilm.getName(), Toast.LENGTH_SHORT).show();
            // Add your booking logic here
            Intent intent = new Intent(UserDetailFirm.this, UserShowListBroadcast.class); // Replace with your actual Booking Activity
            intent.putExtra("firmId", firmId);
            startActivity(intent);
        });
    }

}
