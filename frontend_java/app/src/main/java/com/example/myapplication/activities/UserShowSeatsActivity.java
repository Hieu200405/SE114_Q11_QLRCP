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
import com.example.myapplication.adapters.SeatAdapter;
import com.example.myapplication.models.BookingTicketRequest;
import com.example.myapplication.models.BookingTicketResponse;
import com.example.myapplication.models.BroadcastFilm;
import com.example.myapplication.models.CreatePaymentRequest;
import com.example.myapplication.models.CreatePaymentResponse;
import com.example.myapplication.models.Seat;
import com.example.myapplication.network.ApiBroadcastService;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiPaymentService;
import com.example.myapplication.network.ApiTicketService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserShowSeatsActivity extends AppCompatActivity {

    private static final String TAG = "UserShowSeatsActivity";
    
    private String accessToken;
    private RecyclerView recyclerViewSeats;
    private SeatAdapter seatAdapter;
    private List<Seat> seatList;
    private BookingTicketRequest bookingTicketRequest;
    
    // Thêm biến để lưu giá vé (có thể lấy từ API hoặc Intent)
    private int ticketPrice = 75000; // Giá vé mặc định 75,000 VND

    Button continueButton;
    Button CancelButton;
    
    // ActivityResultLauncher để nhận kết quả từ PaymentActivity
    private ActivityResultLauncher<Intent> paymentLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_show_seats);
//        set token
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        accessToken =  prefs.getString("access_token", null);
        Log.d("TOKEN", "Token: " + accessToken);

        // Khởi tạo payment launcher
        initPaymentLauncher();

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
        // Try to get firmId (passed from UserShowListBroadcast) so we can fetch broadcast price
        int firmId = getIntent().getIntExtra("firmId", -1);
        if (firmId != -1) {
            loadBroadcastPrice(firmId, BroadcastId);
        }
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

    /**
     * Load broadcasts for a firm and extract the price for the given broadcastId.
     * If found, sets `ticketPrice` to the broadcast price (rounded to int).
     */
    private void loadBroadcastPrice(int firmId, int broadcastId) {
        ApiBroadcastService apiBroadcastService = ApiClient.getRetrofit().create(ApiBroadcastService.class);
        Call<List<BroadcastFilm>> call = apiBroadcastService.getBroadcastsByFirmId(firmId);

        call.enqueue(new Callback<List<BroadcastFilm>>() {
            @Override
            public void onResponse(@NonNull Call<List<BroadcastFilm>> call, @NonNull Response<List<BroadcastFilm>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BroadcastFilm> broadcasts = response.body();
                    for (BroadcastFilm b : broadcasts) {
                        if (b != null && b.getID() == broadcastId) {
                            // Use the Price field from the broadcast
                            double price = b.getPrice();
                            ticketPrice = (int) Math.round(price);
                            Log.d(TAG, "Ticket price set from broadcast: " + ticketPrice);
                            return;
                        }
                    }
                    Log.w(TAG, "Broadcast with id " + broadcastId + " not found in firm list; using default price " + ticketPrice);
                } else {
                    Log.e(TAG, "Failed to load broadcasts to get price: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<BroadcastFilm>> call, Throwable t) {
                Log.e(TAG, "Error loading broadcasts for price: " + t.getMessage());
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

            // Lưu tạm thông tin đặt vé
            bookingTicketRequest = new BookingTicketRequest(broadcastId, seatSelected.getId());
            bookingTicketRequest.setSeatId(seatAdapter.getSelectedSeat().getId());

            // Bắt đầu luồng thanh toán PayOS thay vì đặt vé trực tiếp
            initiatePayment(broadcastId, seatSelected);
        });
    }

    /**
     * Khởi tạo ActivityResultLauncher để nhận kết quả từ PaymentActivity
     */
    private void initPaymentLauncher() {
        paymentLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == PaymentActivity.RESULT_PAYMENT_SUCCESS) {
                    // Thanh toán thành công - vé đã được tạo trong PaymentActivity
                    Log.d(TAG, "Payment successful");
                    loadSeatsFromApi(bookingTicketRequest.getBroadcastId());
                    // Có thể đóng activity hoặc cập nhật UI
                } else if (result.getResultCode() == PaymentActivity.RESULT_PAYMENT_CANCELLED) {
                    Log.d(TAG, "Payment cancelled");
                    Toast.makeText(this, "Thanh toán đã bị hủy", Toast.LENGTH_SHORT).show();
                } else if (result.getResultCode() == PaymentActivity.RESULT_PAYMENT_FAILED) {
                    Log.d(TAG, "Payment failed");
                    String errorMessage = "Thanh toán thất bại";
                    if (result.getData() != null) {
                        errorMessage = result.getData().getStringExtra("error_message");
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    /**
     * Bắt đầu luồng thanh toán PayOS
     * @param broadcastId ID lịch chiếu
     * @param seat Ghế đã chọn
     */
    private void initiatePayment(int broadcastId, Seat seat) {
        // Hiển thị loading
        continueButton.setEnabled(false);
        continueButton.setText("Đang xử lý...");

        // Tạo request thanh toán
        CreatePaymentRequest paymentRequest = new CreatePaymentRequest.Builder()
                .amount(ticketPrice)
                .description("VE" + broadcastId)
                .broadcastId(broadcastId)
                .seatId(seat.getId())
                .items(Collections.singletonList(
                    new CreatePaymentRequest.PaymentItem("Ve xem phim", 1, ticketPrice)
                ))
                .build();

        // Gọi API tạo link thanh toán
        ApiPaymentService apiPaymentService = ApiClient.getRetrofit().create(ApiPaymentService.class);
        Call<CreatePaymentResponse> call = apiPaymentService.createPayment(
                "Bearer " + accessToken, paymentRequest);

        call.enqueue(new Callback<CreatePaymentResponse>() {
            @Override
            public void onResponse(Call<CreatePaymentResponse> call, Response<CreatePaymentResponse> response) {
                continueButton.setEnabled(true);
                continueButton.setText("Tiếp tục");

                if (response.isSuccessful() && response.body() != null) {
                    CreatePaymentResponse paymentResponse = response.body();
                    
                    if (paymentResponse.isSuccess() && paymentResponse.getData() != null) {
                        String checkoutUrl = paymentResponse.getData().getCheckoutUrl();
                        long orderCode = paymentResponse.getData().getOrderCode();
                        int amount = paymentResponse.getData().getAmount();

                        Log.d(TAG, "Payment link created: " + checkoutUrl);
                        Log.d(TAG, "Order code: " + orderCode);

                        // Mở PaymentActivity với WebView
                        Intent paymentIntent = new Intent(UserShowSeatsActivity.this, PaymentActivity.class);
                        paymentIntent.putExtra(PaymentActivity.EXTRA_CHECKOUT_URL, checkoutUrl);
                        paymentIntent.putExtra(PaymentActivity.EXTRA_ORDER_CODE, orderCode);
                        paymentIntent.putExtra(PaymentActivity.EXTRA_AMOUNT, amount);
                        paymentIntent.putExtra(PaymentActivity.EXTRA_BROADCAST_ID, broadcastId);
                        paymentIntent.putExtra(PaymentActivity.EXTRA_SEAT_ID, seat.getId());
                        
                        paymentLauncher.launch(paymentIntent);
                    } else {
                        String error = paymentResponse.getDesc() != null ? 
                                paymentResponse.getDesc() : "Không thể tạo link thanh toán";
                        Toast.makeText(UserShowSeatsActivity.this, error, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Payment creation failed: " + error);
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? 
                                response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Payment API error: " + errorBody);
                        Toast.makeText(UserShowSeatsActivity.this, 
                                "Lỗi tạo thanh toán: " + response.code(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<CreatePaymentResponse> call, Throwable t) {
                continueButton.setEnabled(true);
                continueButton.setText("Tiếp tục");
                Log.e(TAG, "Payment API call failed: " + t.getMessage());
                Toast.makeText(UserShowSeatsActivity.this, 
                        "Lỗi kết nối. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Phương thức đặt vé trực tiếp (giữ lại cho trường hợp không dùng PayOS)
     * @deprecated Sử dụng initiatePayment() thay thế để thanh toán qua PayOS
     */
    @Deprecated
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