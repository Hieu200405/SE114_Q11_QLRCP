package com.example.myapplication.activities;

import static androidx.core.content.ContextCompat.startActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.TicketAdapter;
import com.example.myapplication.models.BookingTicketResponse;
import com.example.myapplication.models.ReviewRequest;
import com.example.myapplication.models.ReviewResponse;
import com.example.myapplication.models.Ticket;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiFilmService;
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
    ApiFilmService apiFilmService;
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

    private void showRatingDialog(int filmId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_rating, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        EditText etComment = dialogView.findViewById(R.id.etComment);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(v -> {
            int rating = (int) ratingBar.getRating();
            String comment = etComment.getText().toString().trim();

            if (rating == 0) {
                Toast.makeText(this, "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
                return;
            }

            sendReviewRequest(filmId, rating, comment);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void sendReviewRequest(int filmId, int rating, String comment) {
        ReviewRequest reviewRequest = new ReviewRequest(filmId, rating, comment);
        String tokenHeader = "Bearer " + accessToken;

        apiFilmService.addReview(tokenHeader, reviewRequest).enqueue(new Callback<ReviewResponse>() {
            @Override
            public void onResponse(@NonNull Call<ReviewResponse> call, @NonNull Response<ReviewResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ReviewResponse result = response.body();

                    if (result.getCode().equals("00")) {
                        // Success
                        Toast.makeText(UserActivityHistoryBookingTicket.this,
                                "Cảm ơn bạn đã đánh giá phim!", Toast.LENGTH_SHORT).show();
                        ticketAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(UserActivityHistoryBookingTicket.this,
                                "Lỗi: " + result.getDesc(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e("API_ERROR", "Response code: " + response.code());
                    Toast.makeText(UserActivityHistoryBookingTicket.this,
                            "Không thể gửi đánh giá. Vui lòng thử lại sau.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReviewResponse> call, @NonNull Throwable t) {
                Log.e("API_ERROR", "Network error: " + t.getMessage());
                Toast.makeText(UserActivityHistoryBookingTicket.this,
                        "Lỗi kết nối mạng!", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
