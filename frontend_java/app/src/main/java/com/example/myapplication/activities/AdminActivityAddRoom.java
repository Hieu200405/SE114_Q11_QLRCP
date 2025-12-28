package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.cacheModels.CinemaCache;
import com.example.myapplication.helper.BroadcastCinemaEnricher;
import com.example.myapplication.models.Cinema;
import com.example.myapplication.models.RoomRequest;
import com.example.myapplication.models.RoomResponse;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiRoomService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminActivityAddRoom extends AppCompatActivity {
    String accessToken;
    EditText editRoomName;
    EditText editSeats;
    Spinner spinnerCinema;
    Button buttonCancel;
    Button buttonCreate;

    // Cinema info views
    private LinearLayout layoutCinemaInfo;
    private TextView tvSelectedCinemaAddress;
    private TextView tvSelectedCinemaPhone;

    private List<Cinema> cinemaList = new ArrayList<>();
    private int selectedCinemaId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_create_room);
        setElementsByID();
        // Get the access token from SharedPreferences
        accessToken = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("access_token", null);

        loadCinemas();
        setListeners();
    }

    private void setElementsByID() {
        editRoomName = findViewById(R.id.editRoomName);
        editSeats = findViewById(R.id.editSeats);
        spinnerCinema = findViewById(R.id.spinnerCinema);
        buttonCancel = findViewById(R.id.buttonCancel);
        buttonCreate = findViewById(R.id.buttonCreate);

        // Cinema info views
        layoutCinemaInfo = findViewById(R.id.layoutCinemaInfo);
        tvSelectedCinemaAddress = findViewById(R.id.tvSelectedCinemaAddress);
        tvSelectedCinemaPhone = findViewById(R.id.tvSelectedCinemaPhone);
    }

    private void loadCinemas() {
        CinemaCache.loadCinemas(new CinemaCache.CinemaLoadListener() {
            @Override
            public void onCinemasLoaded(List<Cinema> cinemas) {
                cinemaList.clear();
                cinemaList.addAll(cinemas);
                setupCinemaSpinner();
            }

            @Override
            public void onLoadError(String error) {
                Log.e("AdminActivityAddRoom", "Lỗi load cinema: " + error);
            }
        });
    }

    private void setupCinemaSpinner() {
        List<String> cinemaNames = new ArrayList<>();
        cinemaNames.add("-- Chọn rạp chiếu phim --");
        for (Cinema cinema : cinemaList) {
            cinemaNames.add(cinema.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, cinemaNames) {
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setTextColor(getResources().getColor(R.color.primary_text));
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView) view).setTextColor(getResources().getColor(R.color.primary_text));
                view.setBackgroundColor(getResources().getColor(R.color.white));
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCinema.setAdapter(adapter);

        spinnerCinema.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedCinemaId = cinemaList.get(position - 1).getId();
                    showCinemaInfo(cinemaList.get(position - 1));
                } else {
                    selectedCinemaId = -1;
                    hideCinemaInfo();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCinemaId = -1;
                hideCinemaInfo();
            }
        });
    }

    private void showCinemaInfo(Cinema cinema) {
        if (layoutCinemaInfo != null) {
            layoutCinemaInfo.setVisibility(View.VISIBLE);

            if (tvSelectedCinemaAddress != null) {
                String address = cinema.getAddress() != null ? cinema.getAddress() : "Chưa có địa chỉ";
                tvSelectedCinemaAddress.setText(address);
            }

            if (tvSelectedCinemaPhone != null) {
                String phone = cinema.getPhone() != null ? cinema.getPhone() : "Chưa có số điện thoại";
                tvSelectedCinemaPhone.setText(phone);
            }
        }
    }

    private void hideCinemaInfo() {
        if (layoutCinemaInfo != null) {
            layoutCinemaInfo.setVisibility(View.GONE);
        }
    }

    private void setListeners() {
        buttonCancel.setOnClickListener(v -> finish());
        buttonCreate.setOnClickListener(v -> {
            String roomName = editRoomName.getText().toString();
            String seatsStr = editSeats.getText().toString();

            // Validate inputs
            if (roomName.isEmpty() || seatsStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedCinemaId == -1) {
                Toast.makeText(this, "Vui lòng chọn rạp chiếu phim", Toast.LENGTH_SHORT).show();
                return;
            }

            int seats = Integer.parseInt(seatsStr);
            CreateRoomAPI(roomName, seats, selectedCinemaId);
        });
    }

    private void CreateRoomAPI(String roomName, int seats, int cinemaId) {
        String token = "Bearer " + accessToken;
        ApiRoomService apiRoomService = ApiClient.getRetrofit().create(ApiRoomService.class);

        RoomRequest roomRequest = new RoomRequest(roomName, seats);
        roomRequest.setCinemaId(cinemaId);

        Call<RoomResponse> call = apiRoomService.createRoom(token, roomRequest);
        call.enqueue(new Callback<RoomResponse>() {
            @Override
            public void onResponse(@NonNull Call<RoomResponse> call, @NonNull Response<RoomResponse> response) {
                if (response.isSuccessful()) {
                    RoomResponse roomResponse = response.body();

                    // Clear room cache to force reload new data
                    BroadcastCinemaEnricher.clearRoomCache();

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("room", roomResponse);
                    setResult(4, resultIntent);
                    finish();
                } else {
                    Toast.makeText(AdminActivityAddRoom.this, "Tạo phòng thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<RoomResponse> call, @NonNull Throwable t) {
                Toast.makeText(AdminActivityAddRoom.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("AdminActivityAddRoom", "Tạo Phòng thất bại: " + t.getMessage());
            }
        });
    }

}
