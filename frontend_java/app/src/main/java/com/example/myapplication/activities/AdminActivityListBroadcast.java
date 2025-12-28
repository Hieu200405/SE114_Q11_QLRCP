package com.example.myapplication.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
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

import com.example.myapplication.R;
import com.example.myapplication.adapters.BroadCastFilmAdapter;
import com.example.myapplication.helper.BroadcastCinemaEnricher;
import com.example.myapplication.helper.LocationHelper;
import com.example.myapplication.models.BroadcastFilm;
import com.example.myapplication.models.StatusMessage;
import com.example.myapplication.network.ApiBroadcastService;
import com.example.myapplication.network.ApiClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminActivityListBroadcast extends AppCompatActivity {
    private static final String TAG = "AdminListBroadcast";

    String accessToken, role;
    private TextView textSelectedDate;
    private BroadCastFilmAdapter broadCastFilmAdapter;
    private List<BroadcastFilm> cacheBroadcastFilmList;
    private List<BroadcastFilm> broadcastFilmList;
    private RecyclerView broadcastFilmRecyclerView;
    private ImageButton btnSearchCalendar;
    private FloatingActionButton buttonAddBroadcast;
    private ImageView imageBack;
    ActivityResultLauncher<Intent> launchCreateBroadcast;

    // Location helper for distance calculation
    private LocationHelper locationHelper;


    @SuppressLint("MissingInflatedId")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_list_broadcast);
        accessToken = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("access_token", null);
        role = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("role", "user");
        Log.d("AdminActivityListBroadcast", "Access Token in broadcast  list: " + accessToken);
        if (accessToken == null) {
            Toast.makeText(this, "Access token không tìm thấy", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize your views and set up any necessary listeners here
        int filmId = getIntent().getIntExtra("filmId", -1);
        Log.e("UserDetailFilm", "Received film ID: " + filmId);
        textSelectedDate = findViewById(R.id.textSelectedDate);
        btnSearchCalendar = findViewById(R.id.btnSearch);
        imageBack = findViewById(R.id.imageBack);
        buttonAddBroadcast = findViewById(R.id.buttonAddBroadcast);

        // Initialize location helper for distance calculation
        locationHelper = new LocationHelper(this);

        broadcastFilmRecyclerView = findViewById(R.id.broadcastRecyclerView);
        broadcastFilmRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load the list of broadcasts for the film
        broadcastFilmList = new ArrayList<>();
        broadCastFilmAdapter = new BroadCastFilmAdapter(broadcastFilmList, role);


        broadcastFilmRecyclerView.setAdapter(broadCastFilmAdapter);

        // Get user location and then load broadcasts
        getUserLocationAndLoadBroadcasts(filmId);

        // Thiết lập adapter click listener

        setBroadCastAdapterClickListener();


        //        Listeners
        setLaucherCreateBroadcast();
        setCalendarListener();
        setSearchCalandarListener();
        setBackButtonListener();
        setAddBroadcastListener();
    }

//    Listener adapter click listener for the broadcast list
    void setBroadCastAdapterClickListener() {
        broadCastFilmAdapter.setOnItemClickListener(
            new BroadCastFilmAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BroadcastFilm broadcastFilm) {
                    Intent intent = new Intent(AdminActivityListBroadcast.this, AdminShowSeatsActivity.class);
                    intent.putExtra("broadcastId", broadcastFilm.getID());
                    startActivity(intent);
                }

                @Override
                public void onDeleteClick(BroadcastFilm broadcastFilm) {
                    AlertDeleteBroadcast(broadcastFilm);
                }
            }
        );
    }



    //    Listener for the calendar text view
    private void  setCalendarListener() {
        textSelectedDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this, // hoặc requireContext() nếu trong Fragment
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        @SuppressLint("DefaultLocale") String selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        textSelectedDate.setText(selectedDate);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });
    }


    /**
     * Get user location and then load broadcasts
     */
    private void getUserLocationAndLoadBroadcasts(int filmId) {
        locationHelper.getCurrentLocation(new LocationHelper.LocationResultListener() {
            @Override
            public void onLocationReceived(double latitude, double longitude) {
                Log.d(TAG, "User location: " + latitude + ", " + longitude);
                // Set user location to adapter for distance calculation
                broadCastFilmAdapter.setUserLocation(latitude, longitude);
                // Now load broadcasts
                loadListBroadcast(filmId);
            }

            @Override
            public void onLocationError(String error) {
                Log.w(TAG, "Location error: " + error + ". Loading broadcasts without distance.");
                // Still load broadcasts even without location
                loadListBroadcast(filmId);
            }
        });
    }

    private void loadListBroadcast(int filmID) {
        // Implement the logic to load film details here
        // This could involve making a network request to fetch film data
        // and then updating the UI with that data.
        ApiBroadcastService apiBroadcastService = ApiClient.getRetrofit().create(ApiBroadcastService.class);
        Call<List<BroadcastFilm>> call = apiBroadcastService.getBroadcastsByFilmId(filmID); // Replace 1 with the actual film ID you want to fetch

        call.enqueue(new Callback<List<BroadcastFilm>>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<List<BroadcastFilm>> call, @NonNull Response<List<BroadcastFilm>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BroadcastFilm> broadcasts = response.body();

                    // Enrich broadcasts with cinema information
                    BroadcastCinemaEnricher.enrichBroadcastsWithCinemaInfo(broadcasts,
                        new BroadcastCinemaEnricher.EnrichmentCompleteListener() {
                            @Override
                            public void onEnrichmentComplete(List<BroadcastFilm> enrichedBroadcasts) {
                                cacheBroadcastFilmList = enrichedBroadcasts;
                                broadcastFilmList.clear();
                                broadcastFilmList.addAll(enrichedBroadcasts);
                                Log.e("API_RESPONSE", "Response body: " + response.code());
                                broadCastFilmAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onEnrichmentError(String error) {
                                // Still show broadcasts even if cinema enrichment fails
                                cacheBroadcastFilmList = broadcasts;
                                broadcastFilmList.clear();
                                broadcastFilmList.addAll(broadcasts);
                                Log.e("API_RESPONSE", "Cinema enrichment error: " + error);
                                broadCastFilmAdapter.notifyDataSetChanged();
                            }
                        });

                } else {
                    Toast.makeText(AdminActivityListBroadcast.this, "Không lấy được dữ liệu", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<List<BroadcastFilm>> call, Throwable t) {

            }

        });
    }




    void setSearchCalandarListener() {
        btnSearchCalendar.setOnClickListener(v -> {
            String selectedDate = textSelectedDate.getText().toString();
            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ngày", Toast.LENGTH_SHORT).show();
                return;
            }
            // Call the method to filter broadcasts by the selected date
            filterBroadcastsByDate(selectedDate);
        });
    }


    private void filterBroadcastsByDate(String selectedDate) {
        Log.e("FilterBroadcasts", "Filtering broadcasts for date: " + selectedDate);
        List<BroadcastFilm> filteredList = new ArrayList<>();
        for (BroadcastFilm broadcast : cacheBroadcastFilmList) {
            if (broadcast.getDateBroadcast().equals(selectedDate)) {
                filteredList.add(broadcast);

            }
            Log.e("FilterBroadcasts", "Broadcast found: " + broadcast.getTimeBroadcast() + " on " + broadcast.getDateBroadcast());
        }

        broadcastFilmList.clear();
        broadcastFilmList.addAll(filteredList);
        broadCastFilmAdapter.notifyDataSetChanged();

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "Không có lịch chiếu cho ngày đã chọn", Toast.LENGTH_SHORT).show();
        }

    }

    //    listener for the back button
    void setBackButtonListener() {
        imageBack.setOnClickListener(v -> {
            finish(); // Close the current activity
        });
    }



//    Sự kiện click của nút thêm lịch chiếu
    private void setLaucherCreateBroadcast() {
        launchCreateBroadcast = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Refresh the list of broadcasts after creating a new one
                    BroadcastFilm broadcastFilm = result.getData().getParcelableExtra("newBroadcast");
                    if (broadcastFilm != null) {
                        Log.e("AdminActivityListBroadcast", "New broadcast created: " + broadcastFilm.getID());
                        broadcastFilmList.add(broadcastFilm);
                        broadCastFilmAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("AdminActivityListBroadcast", "No new broadcast data received");
                    }

                }
            }
        );
    }

    private void setAddBroadcastListener() {
        buttonAddBroadcast.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivityListBroadcast.this, AdminActivityCreateBroadcast.class);
            intent.putExtra("filmId", getIntent().getIntExtra("filmId", -1));
            Log.e("AdminActivityListBroadcast", "Launching AdminActivityCreateBroadcast with filmId: " + getIntent().getIntExtra("filmId", -1));

            launchCreateBroadcast.launch(intent);
//            startActivity(intent);
        });
    }


//    Sự kiện xóa broadcast
    private void deleteBroadcast(BroadcastFilm broadcastFilm) {
        ApiBroadcastService apiBroadcastService = ApiClient.getRetrofit().create(ApiBroadcastService.class);
        Call<StatusMessage> call = apiBroadcastService.deleteBroadcast("Bearer " + accessToken, broadcastFilm.getID());
        call.enqueue(
            new Callback<StatusMessage>() {
                @Override
                public void onResponse(@NonNull Call<StatusMessage> call, @NonNull Response<StatusMessage> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        StatusMessage statusMessage = response.body();
                        if (response.code() == 200) {
                            Toast.makeText(AdminActivityListBroadcast.this, "Xóa lịch chiếu thành công", Toast.LENGTH_SHORT).show();
                            // Remove the broadcast from the list and notify the adapter
                            broadcastFilmList.remove(broadcastFilm);
                            broadCastFilmAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(AdminActivityListBroadcast.this, "Lỗi: " + statusMessage.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(AdminActivityListBroadcast.this, "Không thể xóa lịch chiếu", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<StatusMessage> call, @NonNull Throwable t) {
                    Toast.makeText(AdminActivityListBroadcast.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        );

    }

//    Thêm sự kiện thông báo xác nhận xóa lịch chiếu
    private void AlertDeleteBroadcast(BroadcastFilm broadcastFilm) {
        new AlertDialog.Builder(AdminActivityListBroadcast.this)
            .setTitle("Xác nhận Xóa Lịch Chiếu")
            .setMessage("Bạn có chắc chắn muốn xóa không?")
            .setPositiveButton("Chắc chắn", (dialog, which) -> {
                // Xử lý xóa phòng
                deleteBroadcast(broadcastFilm);
                Toast.makeText(AdminActivityListBroadcast.this, "Delete clicked for broadcast ID: " + broadcastFilm.getID(), Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Hủy", (dialog, which) -> {
                dialog.dismiss(); // Đóng dialog nếu chọn Cancel
            })
            .show();

    }

}
