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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.BroadCastFilmAdapter;
import com.example.myapplication.models.BroadcastFilm;
import com.example.myapplication.network.ApiBroadcastService;
import com.example.myapplication.network.ApiClient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserShowListBroadcast extends AppCompatActivity {


    private TextView textSelectedDate;
    private BroadCastFilmAdapter broadCastFilmAdapter;
    private List<BroadcastFilm> cacheBroadcastFilmList;
    private List<BroadcastFilm> broadcastFilmList;
    private RecyclerView broadcastFirmRecyclerView;
    private ImageButton btnSearchCalendar;
    private ImageView imageBack;


    @SuppressLint("MissingInflatedId")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_list_broadcast);

        // Initialize your views and set up any necessary listeners here
        int firmId = getIntent().getIntExtra("firmId", -1);
        Log.e("UserDetailFirm", "Received firm ID: " + firmId);
        textSelectedDate = findViewById(R.id.textSelectedDate);
        btnSearchCalendar = findViewById(R.id.btnSearch);
        imageBack = findViewById(R.id.imageBack);



        broadcastFirmRecyclerView = findViewById(R.id.broadcastRecyclerView);
        broadcastFirmRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load the list of broadcasts for the firm
        broadcastFilmList = new ArrayList<>();
        broadCastFilmAdapter = new BroadCastFilmAdapter(broadcastFilmList);

        broadcastFirmRecyclerView.setAdapter(broadCastFilmAdapter);
        loadListBroadcast(firmId);

        // Thiết lập adapter click listener

        broadCastFilmAdapter.setOnItemClickListener(
                new BroadCastFilmAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(BroadcastFilm broadcastFilm) {
                        // Handle item click, e.g., navigate to a detailed view
                        Intent intent = new Intent(UserShowListBroadcast.this, UserShowSeatsActivity.class);
                        intent.putExtra("broadcastId", broadcastFilm.getID());
                        intent.putExtra("firmId", firmId);
                        startActivity(intent);
                    }

                    @Override
                    public void onDeleteClick(BroadcastFilm broadcastFilm) {
                        // Handle delete click if needed
                        Toast.makeText(UserShowListBroadcast.this, "Xóa lịch chiếu không được phép", Toast.LENGTH_SHORT).show();
                    }
                }
        );




        //        Listeners
        setCalendarListener();
        setSearchCalandarListener();
        setBackButtonListener();
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


    private void loadListBroadcast(int firmID) {
        // Implement the logic to load firm details here
        // This could involve making a network request to fetch firm data
        // and then updating the UI with that data.
        ApiBroadcastService apiBroadcastService = ApiClient.getRetrofit().create(ApiBroadcastService.class);
        Call<List<BroadcastFilm>> call = apiBroadcastService.getBroadcastsByFirmId(firmID); // Replace 1 with the actual firm ID you want to fetch

        call.enqueue(new Callback<List<BroadcastFilm>>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<List<BroadcastFilm>> call, @NonNull Response<List<BroadcastFilm>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cacheBroadcastFilmList = response.body();
                    broadcastFilmList.clear();
                    broadcastFilmList.addAll(response.body());
                    Log.e("API_RESPONSE", "Response body: " + response.code());
                    broadCastFilmAdapter.notifyDataSetChanged();

                } else {
                    Toast.makeText(UserShowListBroadcast.this, "Không lấy được dữ liệu", Toast.LENGTH_SHORT).show();
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

}
