package com.example.myapplication.activities;

import android.annotation.SuppressLint;
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

public class AdminActivityEditRoom extends AppCompatActivity {
    String accessToken;
    private EditText editRoomName;
    private EditText editSeats;
    private Spinner spinnerCinema;
    TextView textRoomID;
    private Button buttonCancel;
    private Button buttonSave;

    private List<Cinema> cinemaList = new ArrayList<>();
    private int selectedCinemaId = -1;
    private int currentCinemaId = -1;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_edit_room);
        // Get the access token from SharedPreferences
        accessToken = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("access_token", null);
        if (accessToken == null) {
            finish();
            return;
        }
        // Initialize UI elements
        setElementsByID();

        Intent intent = getIntent();
        RoomResponse roomResponse = (RoomResponse) intent.getParcelableExtra("room");
        if (roomResponse != null) {
            editRoomName.setText(roomResponse.getName());
            editSeats.setText(String.valueOf(roomResponse.getSeats()));
            textRoomID.setText("Mã phòng: " + roomResponse.getId());

            // Get current cinema ID from room
            if (roomResponse.getCinemaId() != null) {
                currentCinemaId = roomResponse.getCinemaId();
                selectedCinemaId = currentCinemaId;
            }
        }

        loadCinemas();
        setListeners(roomResponse.getId());
    }

    private void setElementsByID() {
        editRoomName = findViewById(R.id.editRoomName);
        editSeats = findViewById(R.id.editSeats);
        spinnerCinema = findViewById(R.id.spinnerCinema);
        buttonCancel = findViewById(R.id.buttonCancel);
        buttonSave = findViewById(R.id.buttonSave);
        textRoomID = findViewById(R.id.textRoomId);
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
                Log.e("AdminActivityEditRoom", "Lỗi load cinema: " + t.getMessage());
            }
        });
    }

    private void setupCinemaSpinner() {
        List<String> cinemaNames = new ArrayList<>();
        cinemaNames.add("-- Chọn rạp chiếu phim --");
        int selectedPosition = 0;

        for (int i = 0; i < cinemaList.size(); i++) {
            Cinema cinema = cinemaList.get(i);
            cinemaNames.add(cinema.getName());
            if (cinema.getId() == currentCinemaId) {
                selectedPosition = i + 1; // +1 because of placeholder
            }
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
        spinnerCinema.setSelection(selectedPosition);

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
                selectedCinemaId = currentCinemaId;
            }
        });
    }

    void setListeners(int roomId) {
        buttonCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        buttonSave.setOnClickListener(v -> {
            String roomName = editRoomName.getText().toString().trim();
            String seatsStr = editSeats.getText().toString().trim();

            if (roomName.isEmpty() || seatsStr.isEmpty()) {
                editRoomName.setError("Tên phòng là bắt buộc");
                editSeats.setError("Số ghế là bắt buộc");
                Toast.makeText(this ,"Vui lòng điền vào tất cả các trường", Toast.LENGTH_SHORT).show();
                return;
            }

            int seats = Integer.parseInt(seatsStr);
            // Create RoomRequest object with cinema_id
            RoomRequest roomRequest = new RoomRequest(roomName, seats);
            if (selectedCinemaId > 0) {
                roomRequest.setCinemaId(selectedCinemaId);
            }
            updateRoomApi(roomRequest, roomId);
        });
    }

    void updateRoomApi(RoomRequest roomRequest, int roomId){
        ApiRoomService apiRoomService = ApiClient.getRetrofit().create(ApiRoomService.class);
        apiRoomService.updateRoom("Bearer " + accessToken, roomRequest, roomId)
            .enqueue(new Callback<RoomResponse>() {
                @Override
                public void onResponse(@NonNull Call<RoomResponse> call, @NonNull Response<RoomResponse> response) {
                    if (response.isSuccessful()) {
                        RoomResponse updatedRoom = response.body();
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("updatedRoom", updatedRoom);
                        setResult(3, resultIntent);
                        finish();
                    } else {
                        Toast.makeText(AdminActivityEditRoom.this, "Phòng sắp có lịch chiếu, không cập nhật", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RoomResponse> call, @NonNull Throwable t) {
                    Toast.makeText(AdminActivityEditRoom.this, "Phòng sắp có lịch chiếu, không cập nhật", Toast.LENGTH_SHORT).show();
                }
            });
    }

}
