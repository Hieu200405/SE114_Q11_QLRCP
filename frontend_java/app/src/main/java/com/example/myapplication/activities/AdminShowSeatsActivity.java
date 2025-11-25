package com.example.myapplication.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.AdminSeatAdapter;
import com.example.myapplication.adapters.SeatAdapter;
import com.example.myapplication.models.BookingTicketRequest;
import com.example.myapplication.models.BookingTicketResponse;
import com.example.myapplication.models.Seat;
import com.example.myapplication.network.ApiBroadcastService;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiTicketService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminShowSeatsActivity extends AppCompatActivity {
    int RequestDeleteTicket = 4;
    private String accessToken;
    private RecyclerView recyclerViewSeats;
    private AdminSeatAdapter seatAdapter;
    private List<Seat> seatList;
    private BookingTicketRequest bookingTicketRequest; // Assuming you have a BookingTicketRequest model

    Button CancelButton; // Assuming you have a cancel button in your layout
    ActivityResultLauncher<Intent> launcherDetailTicket;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_activity_show_seats);
//        set token
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        accessToken =  prefs.getString("access_token", null);
        Log.d("TOKEN", "Token: " + accessToken);



        CancelButton = findViewById(R.id.btnCancel);
        Seat selectedSeat = null; // Initialize the selected seat object


        recyclerViewSeats = findViewById(R.id.recyclerSeats);

// 1. Set up the RecyclerView
        recyclerViewSeats.setHasFixedSize(true);
        recyclerViewSeats.setLayoutManager(new GridLayoutManager(this, 5));

        seatList = new ArrayList<>();
        seatAdapter = new AdminSeatAdapter(seatList, selectedSeat);
        recyclerViewSeats.setAdapter(seatAdapter);
        setLauncherDetailTicket(); // Set up the launcher for detail ticket activity

//  2. Load seats from api
        int BroadcastId = getIntent().getIntExtra("broadcastId", -1);
        Log.e("UserShowSeatsActivity", "Received broadcast ID: " + BroadcastId);
        if (BroadcastId == -1) {
            Toast.makeText(this, "Lỗi mã lịch chiếu", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if the ID is invalid
            return;
        }
        loadSeatsFromApi(BroadcastId);

// 3. Set up the continue button and cancel button

        setCancelButton(); // Set up the cancel button

//        4.Seat adapter click listener
        seatAdapter.setOnItemClickListener(
            seat -> {
                loadDetailTicketBySeatByApi(seat.getId());
            }
        );
    }

    private void loadSeatsFromApi(int broadcastId) {
        ApiBroadcastService apiBroadcastService = ApiClient.getRetrofit().create(ApiBroadcastService.class);
        Call<List<Seat>> call = apiBroadcastService.getSeatsByBroadcastId(broadcastId); // Replace 1 with the actual firm ID you want to fetch

        call.enqueue(new Callback<List<Seat>>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<List<Seat>> call, @NonNull Response<List<Seat>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    seatList.clear();
                    seatList.addAll(response.body());
                    seatAdapter.notifyDataSetChanged();
                    Log.e("UserShowSeatsActivity", "Received seats: " + seatList.size());
                    if (seatList.isEmpty()) {
                        Toast.makeText(AdminShowSeatsActivity.this, "Không có chỗ ngồi cho lịch chiếu này.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Optionally, you can handle the case where seats are available
                        Log.d("UserShowSeatsActivity", "Seats loaded successfully.");
                    }

                } else {
                    Log.e("UserShowSeatsActivity", "Response error: " + response.message());
                    Toast.makeText(AdminShowSeatsActivity.this, "Lỗi tải chỗ ngồi: " + response.message(), Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onFailure(Call<List<Seat>> call, Throwable t) {

            }

        });
    }


    void setCancelButton(){
        CancelButton.setOnClickListener(v -> {
            finish(); // Close the activity or perform any other action
        });
    }


    private void loadDetailTicketBySeatByApi(int seatId) {
        String token = "Bearer " + accessToken;
        ApiTicketService apiTicketService = ApiClient.getRetrofit().create(ApiTicketService.class);
        Call<BookingTicketResponse> call = apiTicketService.getTicketBySeat(token, String.valueOf(seatId));

        call.enqueue(new Callback<BookingTicketResponse>() {
            @Override
            public void onResponse(Call<BookingTicketResponse> call, Response<BookingTicketResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BookingTicketResponse bookingTicketResponse = response.body();

                    Intent intent = new Intent(AdminShowSeatsActivity.this, UserAdminShowDetailTicket.class);
                    intent.putExtra("bookingTicketResponse", bookingTicketResponse);
                    launcherDetailTicket.launch(intent);


                } else {
                    Log.e("API_ERROR", "Failed to load ticket details: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BookingTicketResponse> call, Throwable t) {
                Log.e("API_ERROR", "Failed to load ticket details: " + t.getMessage());
            }
        });
    }


    private void setLauncherDetailTicket(){
        launcherDetailTicket = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RequestDeleteTicket) {
                        Log.d("AdminShowSeatsActivity", "RequestDeleteTicket received");
                        Intent data = result.getData();
                        if (data != null) {
                            int seatId = data.getIntExtra("seatId", -1);
                            if (seatId != -1) {
                                for (int i = 0; i < seatList.size(); i++) {
                                    if (seatList.get(i).getId() == seatId) {
                                        seatList.get(i).setBought(false);
                                        seatAdapter.notifyItemChanged(i);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
        );
    }

}
