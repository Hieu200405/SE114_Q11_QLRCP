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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.models.Cinema;
import com.example.myapplication.models.RoomRequest;
import com.example.myapplication.models.RoomResponse;
import com.example.myapplication.network.ApiCinemaService;
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
    }

    private void loadCinemas() {
        ApiCinemaService apiCinemaService = ApiClient.getRetrofit().create(ApiCinemaService.class);
        apiCinemaService.getAllCinemas().enqueue(new Callback<List<Cinema>>() {
            @Override
            public void onResponse(@NonNull Call<List<Cinema>> call, @NonNull Response<List<Cinema>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cinemaList.clear();
                    cinemaList.addAll(response.body());
                    setupCinemaSpinner();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Cinema>> call, @NonNull Throwable t) {
                Log.e("AdminActivityAddRoom", "Lỗi load cinema: " + t.getMessage());
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
                ((TextView) view).setTextColor(getResources().getColor(android.R.color.white));
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView) view).setTextColor(getResources().getColor(android.R.color.white));
                view.setBackgroundColor(getResources().getColor(R.color.primary_bg));
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
                } else {
                    selectedCinemaId = -1;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCinemaId = -1;
            }
        });
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
