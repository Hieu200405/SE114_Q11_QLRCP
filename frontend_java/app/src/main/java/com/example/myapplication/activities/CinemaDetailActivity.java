package com.example.myapplication.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.adapters.RoomAdapter;
import com.example.myapplication.helper.LocationHelper;
import com.example.myapplication.models.Cinema;
import com.example.myapplication.models.RoomResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity hiển thị chi tiết rạp chiếu phim
 * - Thông tin rạp
 * - Danh sách phòng chiếu
 * - Khoảng cách và thời gian di chuyển
 * - Nút điều hướng tới rạp
 */
public class CinemaDetailActivity extends AppCompatActivity {

    // Views
    private ImageView ivCinemaImage, btnBack;
    private TextView tvCinemaName, tvAddress, tvPhone, tvDescription;
    private TextView tvDistance, tvDuration;
    private LinearLayout layoutDistanceInfo;
    private Button btnNavigate, btnViewRooms;
    private RecyclerView rvRooms;

    // Data
    private Cinema cinema;
    private double userLat, userLng;
    private LocationHelper locationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cinema_detail);

        // Get data from intent
        cinema = getIntent().getParcelableExtra("cinema");
        userLat = getIntent().getDoubleExtra("userLat", 0);
        userLng = getIntent().getDoubleExtra("userLng", 0);

        if (cinema == null) {
            finish();
            return;
        }

        initViews();
        setupListeners();
        displayCinemaInfo();
    }

    private void initViews() {
        ivCinemaImage = findViewById(R.id.ivCinemaImage);
        tvCinemaName = findViewById(R.id.tvCinemaName);
        tvAddress = findViewById(R.id.tvAddress);
        tvPhone = findViewById(R.id.tvPhone);
        tvDescription = findViewById(R.id.tvDescription);
        tvDistance = findViewById(R.id.tvDistance);
        tvDuration = findViewById(R.id.tvDuration);
        layoutDistanceInfo = findViewById(R.id.layoutDistanceInfo);
        btnNavigate = findViewById(R.id.btnNavigate);
        btnViewRooms = findViewById(R.id.btnViewRooms);
        rvRooms = findViewById(R.id.rvRooms);

        // Header View
        btnBack = findViewById(R.id.imageBack);

        locationHelper = new LocationHelper(this);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnNavigate.setOnClickListener(v -> {
            locationHelper.openNavigationApp(this, cinema.getLatitude(), cinema.getLongitude(), cinema.getName());
        });

        btnViewRooms.setOnClickListener(v -> {
            // Open rooms list for this cinema
            Intent intent = new Intent(this, AdminActivityManageRoom.class);
            intent.putExtra("cinema_id", cinema.getId());
            intent.putExtra("cinema_name", cinema.getName());
            startActivity(intent);
        });
    }

    private void displayCinemaInfo() {
        tvCinemaName.setText(cinema.getName());
        tvAddress.setText(cinema.getAddress());

        // Phone
        if (cinema.getPhone() != null && !cinema.getPhone().isEmpty()) {
            tvPhone.setText(cinema.getPhone());
            tvPhone.setVisibility(View.VISIBLE);
            tvPhone.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + cinema.getPhone()));
                startActivity(intent);
            });
        } else {
            tvPhone.setVisibility(View.GONE);
        }

        // Description
        if (cinema.getDescription() != null && !cinema.getDescription().isEmpty()) {
            tvDescription.setText(cinema.getDescription());
            tvDescription.setVisibility(View.VISIBLE);
        } else {
            tvDescription.setVisibility(View.GONE);
        }

        // Distance info
        if (cinema.getDistance() != null) {
            layoutDistanceInfo.setVisibility(View.VISIBLE);
            tvDistance.setText(cinema.getDistanceText());
            tvDuration.setText("~" + cinema.getDurationText());
        } else {
            layoutDistanceInfo.setVisibility(View.GONE);
        }

        // Image
        if (cinema.getImageUrl() != null && !cinema.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(cinema.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(ivCinemaImage);
        }

        // Rooms
        if (cinema.getRooms() != null && !cinema.getRooms().isEmpty()) {
            setupRoomsList(cinema.getRooms());
            btnViewRooms.setVisibility(View.VISIBLE);
        } else {
            rvRooms.setVisibility(View.GONE);
            btnViewRooms.setVisibility(View.VISIBLE);
        }
    }

    private void setupRoomsList(List<RoomResponse> rooms) {
        rvRooms.setLayoutManager(new LinearLayoutManager(this));
        RoomAdapter adapter = new RoomAdapter(rooms);
        rvRooms.setAdapter(adapter);
        rvRooms.setVisibility(View.VISIBLE);
    }
}
