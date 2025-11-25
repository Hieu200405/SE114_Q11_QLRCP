package com.example.myapplication.activities;

import static androidx.core.content.ContextCompat.startActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.TicketAdapter;
import com.example.myapplication.models.BookingTicketResponse;
import com.example.myapplication.models.Ticket;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiTicketService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserActivityHistoryBookingTicket extends AppCompatActivity {
    int RequestDeleteTicket = 1;

    String accessToken;
    int userId;
    ApiTicketService apiTicketService;
    List<Ticket> ticketList;
    RecyclerView recyclerViewTickets;
    TicketAdapter ticketAdapter;
    ImageView imageUser;
    ImageView imageHome;
    ImageView imageHistory;

    ActivityResultLauncher <Intent> launcherDetailTicket;



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity_history_book);
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        accessToken =  prefs.getString("access_token", null);
        Log.d("TOKEN", "Token: " + accessToken);
        userId = prefs.getInt("user_id", -1);
        Log.d("USER_ID", "User ID: " + userId);


        imageUser = findViewById(R.id.imageUser);
        imageHome = findViewById(R.id.imageHome);
        imageHistory = findViewById(R.id.imageHistory);


        recyclerViewTickets = findViewById(R.id.historyTicketBookedRecyclerView);
        recyclerViewTickets.setLayoutManager(new LinearLayoutManager(this));
        ticketList = new ArrayList<>();
        ticketAdapter = new TicketAdapter(ticketList, accessToken);
        recyclerViewTickets.setAdapter(ticketAdapter);
        // Load the booking history
        loadHistoryBookingTicket(accessToken, userId);

        setLauncherDetailTicket();

//         ListenerSetupBackButton();
        ListenerSetupHomeButton();

//        thiết lập adapter onclick
        ticketAdapter.setOnItemClickListener(
        ticket -> {
            loadDetailTicketByApi(ticket.getId());
        });

    }

    private  void  loadHistoryBookingTicket(String accessToken, int userId) {
        apiTicketService = ApiClient.getRetrofit().create(ApiTicketService.class);
        // Call the API to get the booking history
        accessToken = "Bearer " + accessToken; // Ensure the token is prefixed with "Bearer "
        String id = String.valueOf(userId);
        Call <List<Ticket>> call = apiTicketService.getTicketsBookedByUser(accessToken, id);

        call.enqueue(new retrofit2.Callback<List<Ticket>>() {
            @Override
            public void onResponse(Call<List<Ticket>> call, retrofit2.Response<List<Ticket>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("API_RESPONSE", "Booking history loaded successfully: "+ response.code());
                    ticketList.clear();
                    ticketList.addAll(response.body());
                    ticketAdapter.notifyDataSetChanged();
                } else {
                    Log.e("API_ERROR", "Response error: " + response.code());
                    // Handle the case where the response is not successful
                    Log.e("API_ERROR", "Failed to load booking history: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Ticket>> call, Throwable t) {
                // Handle the failure case
                Log.e("API_ERROR", "Failed to load booking history: " + t.getMessage());
            }
        });

    }

    void  ListenerSetupHomeButton() {
        imageHome.setOnClickListener(v->{
            Intent intent = new Intent(UserActivityHistoryBookingTicket.this, UserMainActivity.class);
            startActivity(intent);
        });

        imageUser.setOnClickListener(v->{
            Intent intent = new Intent(UserActivityHistoryBookingTicket.this, UserActivityProfile.class);
            startActivity(intent);
        });
    }


    private void loadDetailTicketByApi(int ticketId) {
        String token = "Bearer " + accessToken;
        ApiTicketService apiTicketService = ApiClient.getRetrofit().create(ApiTicketService.class);
        Call<BookingTicketResponse> call = apiTicketService.getTicketDetail(token, String.valueOf(ticketId));

        call.enqueue(new Callback<BookingTicketResponse>() {
            @Override
            public void onResponse(Call<BookingTicketResponse> call, Response<BookingTicketResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BookingTicketResponse bookingTicketResponse = response.body();

                    Intent intent = new Intent(UserActivityHistoryBookingTicket.this, UserAdminShowDetailTicket.class);
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

    void setLauncherDetailTicket(){
        launcherDetailTicket = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RequestDeleteTicket) {
                        // vé hiện tai đã bị xóa
                        Intent data = result.getData();
                        if (data != null) {
                            int ticketId = data.getIntExtra("ticketId", -1);
                            if (ticketId != -1) {
                                // Xóa vé khỏi danh sách
                                for (int i = 0; i < ticketList.size(); i++) {
                                    if (ticketList.get(i).getId() == ticketId) {
                                        ticketList.remove(i);
                                        ticketAdapter.notifyItemRemoved(i);
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
