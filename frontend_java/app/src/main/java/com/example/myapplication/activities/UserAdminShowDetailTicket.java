package com.example.myapplication.activities;

import static retrofit2.Response.error;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.models.BookingTicketResponse;
import com.example.myapplication.models.Broadcast;
import com.example.myapplication.models.ReviewRequest;
import com.example.myapplication.models.ReviewResponse;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiFilmService;
import com.example.myapplication.network.ApiTicketService;

import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserAdminShowDetailTicket extends AppCompatActivity {

    private ImageView imageThumbnail;
    private TextView tvTicketID;
    private TextView tvFilmName;
    private TextView tvDate;
    private TextView tvTime;
    private TextView tvRuntime;
    private TextView tvSeatName;
    private TextView tvRoomID;
    private TextView tvDateOrder;
    private TextView tvTimeOrder;
    private TextView tvPrice;
    private Button btnBack;
    private Button btnDelete, btnDeleteUser;
    private TextView tvUserID;
    String accessToken;
    String ticketId;
    private ApiFilmService apiFilmService;
    private BookingTicketResponse currentTicket;

    @SuppressLint({ "MissingInflatedId", "SetTextI18n" })
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_ticket);
        // find elements by ID
        setElementsByID();

        // set access token from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        accessToken = prefs.getString("access_token", null);
        // set role from SharedPreferences
        String role = prefs.getString("role", null);

        BookingTicketResponse bookingTicketResponse = getIntent().getParcelableExtra("bookingTicketResponse");
        this.currentTicket = bookingTicketResponse;
        Broadcast broadcast = bookingTicketResponse.getBroadcast();
        Log.d("thumbnail", "Thumbnail URL: " + broadcast.getThumbnail());
        if (bookingTicketResponse != null && broadcast != null) {
            if (broadcast.getThumbnail() == null || broadcast.getThumbnail().isEmpty()) {
                // Set a default image if the thumbnail is null or empty
                Glide.with(this)
                        .load(R.drawable.default_img) // Replace with your default image resource
                        .into(imageThumbnail);
            } else {
                // Load the thumbnail from the URL
                Glide.with(this)
                        .load(broadcast.getThumbnail())
                        .error(R.drawable.default_img) // Fallback image in case of error
                        .into(imageThumbnail);
            }
            Log.d("userID", "User ID: " + bookingTicketResponse.getUserID());
            // set ticketId from BookingTicketResponse
            ticketId = String.valueOf(bookingTicketResponse.getID());

            tvFilmName.setText(broadcast.getFilmName());
            tvDate.setText("Ngày chiếu: " + broadcast.getDateBroadcast());
            tvTime.setText("Thời gian: " + broadcast.getTimeBroadcast());
            tvRuntime.setText("Runtime: " + broadcast.getRuntime() + " phút");
            tvRoomID.setText(String.valueOf(bookingTicketResponse.getRoomID()));
            tvSeatName.setText("Ghế " + String.valueOf(bookingTicketResponse.getSeatID()));
            tvDateOrder.setText(bookingTicketResponse.getDateOrder());
            tvPrice.setText(String.valueOf(bookingTicketResponse.getPrice()) + " VND");
            tvTimeOrder.setText(bookingTicketResponse.getTimeOrder());
            tvTicketID.setText(String.valueOf(bookingTicketResponse.getID()));
            tvUserID.setText(String.valueOf(bookingTicketResponse.getUserID()));
        } else {
            // Handle the case where bookingTicketResponse is null

            tvFilmName.setText("No booking details available");
        }

        btnBack.setOnClickListener(v -> {
            finish(); // Close the activity and return to the previous one
        });

        if (role != null && role.equals("admin")) {
            btnDelete.setVisibility(Button.VISIBLE);
            listenerDeleteTicket(bookingTicketResponse);
        } else {
            btnDelete.setVisibility(Button.GONE);
        }
        if (role != null && role.equals("user")) {
            btnDeleteUser.setVisibility(Button.VISIBLE);
            listenerDeleteUser(bookingTicketResponse);
        } else {
            btnDeleteUser.setVisibility(Button.GONE);
        }

        apiFilmService = ApiClient.getRetrofit().create(ApiFilmService.class);

        Button btnRating = findViewById(R.id.btnRatingTicketDetail);
        if (btnRating != null) {
            btnRating.setOnClickListener(v -> {
                if (currentTicket != null && currentTicket.getBroadcast() != null) {
                    showRatingDialog(currentTicket.getBroadcast().getFilmID());
                } else {
                    Toast.makeText(this, "Thông tin vé không khả dụng", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    void DeleteTicketByApi() {
        String token = "Bearer " + accessToken;
        ApiTicketService apiTicketService = ApiClient.getRetrofit().create(ApiTicketService.class);
        Call<String> call = apiTicketService.deleteTicket(token, String.valueOf(ticketId));
        call.enqueue(new Callback<String>() {

            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Log.d("API_RESPONSE", "Ticket deleted successfully: " + response.code());
                    // Handle successful deletion, e.g., show a message or update UI
                    Toast.makeText(UserAdminShowDetailTicket.this, "Xóa vé thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("API_ERROR", "Xóa vé thất bại: " + response.code());
                    // Handle the case where the response is not successful
                    if (response.errorBody() != null) {
                        try {
                            String errorMessage = response.errorBody().string();
                            Log.e("API_ERROR", "Error message: " + errorMessage);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("API_ERROR", "Failed to delete ticket: " + t.getMessage());
                // Handle the failure case
            }
        });
    }

    void setElementsByID() {
        imageThumbnail = findViewById(R.id.imgThumbnail);
        tvFilmName = findViewById(R.id.tvFilmName);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        tvRuntime = findViewById(R.id.tvRuntime);
        tvSeatName = findViewById(R.id.tvSeatName);
        tvRoomID = findViewById(R.id.tvRoomID);
        tvDateOrder = findViewById(R.id.tvDateOrder);
        tvTimeOrder = findViewById(R.id.tvTimeOrder);
        tvTicketID = findViewById(R.id.tvTicketID);
        tvPrice = findViewById(R.id.tvPrice);
        tvUserID = findViewById(R.id.tvUserID);
        btnBack = findViewById(R.id.btnBack);
        btnDelete = findViewById(R.id.btnDelete);
        btnDeleteUser = findViewById(R.id.btnDeleteUser);
    }

    void listenerDeleteTicket(BookingTicketResponse bookingTicketResponse) {
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(UserAdminShowDetailTicket.this)
                    .setTitle("Xác nhận xóa vé")
                    .setMessage("Bạn có chắc chắn muốn xóa không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        DeleteTicketByApi();
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("ticketId", bookingTicketResponse.getID());
                        resultIntent.putExtra("seatId", bookingTicketResponse.getSeatID());
                        setResult(4, resultIntent); // Trả về kết quả OK với ticketId
                        finish(); // Đóng activity sau khi xóa vé
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> {
                        dialog.dismiss(); // Đóng dialog nếu chọn Cancel
                    })
                    .show();
        });

    }

    void listenerDeleteUser(BookingTicketResponse bookingTicketResponse) {
        // ticket just delete on the order day

        btnDeleteUser.setOnClickListener(v -> {
            Date currentDate = new Date();
            String currentDateStr = android.text.format.DateFormat.format("yyyy-MM-dd", currentDate).toString();
            String orderDate = bookingTicketResponse.getDateOrder();
            if (!currentDateStr.equals(orderDate)) {
                Toast.makeText(UserAdminShowDetailTicket.this, "Đã quá thời gian hủy vé", Toast.LENGTH_SHORT).show();
                return; // Không hiển thị nút xóa nếu ngày hiện tại không trùng với ngày đặt vé
            }
            new AlertDialog.Builder(UserAdminShowDetailTicket.this)
                    .setTitle("Xác nhận xóa vé")
                    .setMessage("Bạn có chắc chắn muốn xóa vé này không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        DeleteTicketByApi();
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("ticketId", bookingTicketResponse.getID());
                        setResult(1, resultIntent); // Trả về kết quả OK với ticketId
                        finish(); // Đóng activity sau khi xóa vé

                    })
                    .setNegativeButton("Hủy", (dialog, which) -> {
                        dialog.dismiss(); // Đóng dialog nếu chọn Cancel
                    })
                    .show();
        });
    }

    public void showRatingDialog(int broadcastId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_rating, null);
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
            sendReviewRequest(broadcastId, rating, comment);
            dialog.dismiss();
        });
        dialog.show();
    }

    private void sendReviewRequest(int broadcastId, int rating, String comment) {
        ReviewRequest request = new ReviewRequest(broadcastId, rating, comment);
        String authHeader = "Bearer " + accessToken;
        Log.d("REVIEW_DEBUG", "Gửi đánh giá cho FilmID: " + broadcastId);
        apiFilmService.addReview(authHeader, request).enqueue(new Callback<ReviewResponse>() {
            @Override
            public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if ("00".equals(response.body().getCode())) {
                        Toast.makeText(UserAdminShowDetailTicket.this, "Đánh giá thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(UserAdminShowDetailTicket.this, "Lỗi: " + response.body().getDesc(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(UserAdminShowDetailTicket.this, "Gửi đánh giá thất bại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ReviewResponse> call, Throwable t) {
                Toast.makeText(UserAdminShowDetailTicket.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
