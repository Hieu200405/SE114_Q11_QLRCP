package com.example.myapplication.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.SeatAdapter;
import com.example.myapplication.models.BookingTicketRequest;
import com.example.myapplication.models.BookingTicketResponse;
import com.example.myapplication.models.BroadcastFirm;
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

public class UserShowSeatsActivity extends AppCompatActivity {

    private String accessToken;
    private RecyclerView recyclerViewSeats;
    private SeatAdapter seatAdapter;
    private List<Seat> seatList;
    private BookingTicketRequest bookingTicketRequest; // Assuming you have a BookingTicketRequest model

    Button continueButton; // Assuming you have a continue button in your layout
    Button CancelButton; // Assuming you have a cancel button in your layout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_show_seats);
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
        seatAdapter = new SeatAdapter(seatList, selectedSeat);
        recyclerViewSeats.setAdapter(seatAdapter);

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
        setContinueButton(BroadcastId);
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
                        Toast.makeText(UserShowSeatsActivity.this, "Không có chỗ ngồi cho lịch chiếu này.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Optionally, you can handle the case where seats are available
                        Log.d("UserShowSeatsActivity", "Seats loaded successfully.");
                    }

                } else {
                    Log.e("UserShowSeatsActivity", "Response error: " + response.message());
                    Toast.makeText(UserShowSeatsActivity.this, "Lỗi tải chỗ ngồi: " + response.message(), Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onFailure(Call<List<Seat>> call, Throwable t) {

            }

        });
    }


    void setCancelButton(){
        CancelButton.setOnClickListener(v -> {
            // Handle cancel button click
            Toast.makeText(this, "Đã huỷ", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity or perform any other action
        });
    }

    void setContinueButton(int broadcastId) {
        continueButton = findViewById(R.id.btnContinue);
        continueButton.setOnClickListener(v -> {
            // Handle continue button click
            if (seatAdapter.getSelectedSeat() == null) {
                Toast.makeText(this, "Vui lòng chọn một chỗ ngồi.", Toast.LENGTH_SHORT).show();
                return;
            }
            Seat seatSelected = seatAdapter.getSelectedSeat();

            // Assuming you have a method to handle booking logic
            bookingTicketRequest = new BookingTicketRequest(broadcastId, seatSelected.getId());
            bookingTicketRequest.setSeatId(seatAdapter.getSelectedSeat().getId());
            // Add other necessary fields to bookingTicketRequest

            // Call your booking API here
            bookTicketByApi(bookingTicketRequest);

        });
    }


    private void bookTicketByApi(BookingTicketRequest bookingTicketRequest) {

        ApiTicketService apiTicketService = ApiClient.getRetrofit().create(ApiTicketService.class);
        Call<BookingTicketResponse> call = apiTicketService.createTicket("Bearer "+ accessToken, bookingTicketRequest);

        call.enqueue(new Callback<BookingTicketResponse>() {
            @Override
            public void onResponse(Call<BookingTicketResponse> call, Response<BookingTicketResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(UserShowSeatsActivity.this, "Vé đặt thành công.", Toast.LENGTH_SHORT).show();
                    BookingTicketResponse bookingResponse = response.body();

//                    show ticket details
                    Intent intent = new Intent(UserShowSeatsActivity.this, UserAdminShowDetailTicket.class);
                    intent.putExtra("bookingTicketResponse", bookingResponse);
                    startActivity(intent);
                    loadSeatsFromApi(bookingTicketRequest.getBroadcastId());
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("UserShowSeatsActivity", "Booking failed: " + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.e("UserShowSeatsActivity", "Booking failed: " + response.code());
                    Log.e("UserShowSeatsActivity", "Booking failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<BookingTicketResponse> call, Throwable t) {
                Log.e("UserShowSeatsActivity", "Booking failed: " + t.getMessage());
                Toast.makeText(UserShowSeatsActivity.this, "Error: ", Toast.LENGTH_SHORT).show();
            }
        });
    }


}