package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.models.RoomRequest;
import com.example.myapplication.models.RoomResponse;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiRoomService;

import retrofit2.Call;

public class AdminActivityAddRoom extends AppCompatActivity {
    String accessToken;
    EditText editRoomName;
    EditText editSeats;
    Button buttonCancel;
    Button buttonCreate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_create_room);
        setElementsByID();
        // Get the access token from SharedPreferences
        accessToken = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("access_token", null);


        setListeners();
    }
    private void setElementsByID() {
        editRoomName = findViewById(R.id.editRoomName);
        editSeats = findViewById(R.id.editSeats);
        buttonCancel = findViewById(R.id.buttonCancel);
        buttonCreate = findViewById(R.id.buttonCreate);
    }

    private void setListeners() {
        buttonCancel.setOnClickListener(v -> finish());
        buttonCreate.setOnClickListener(v -> {
            // Handle create room logic here
            String roomName = editRoomName.getText().toString();
            String seatsStr = editSeats.getText().toString();

            // Validate inputs
            if (roomName.isEmpty() || seatsStr.isEmpty()) {
                // Show error message
                return;
            }
            int seats = Integer.parseInt(seatsStr);

            CreateRoomAPI(roomName, seats);

        });
    }

    private void CreateRoomAPI(String roomName, int seats) {
        String token = "Bearer " + accessToken;
        // Call the API to create a room
        ApiRoomService apiRoomService = ApiClient.getRetrofit().create(ApiRoomService.class);
        Call<RoomResponse> call = apiRoomService.createRoom(token, new RoomRequest(roomName, seats));
        call.enqueue(new retrofit2.Callback<RoomResponse>() {
            @Override
            public void onResponse(Call<RoomResponse> call, retrofit2.Response<RoomResponse> response) {
                if (response.isSuccessful()) {
                    RoomResponse roomResponse = response.body();
                    // Handle successful room creation
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("room", roomResponse);
                    setResult(4, resultIntent);
                    finish();
                } else {
                    // Handle error
                    Toast.makeText(AdminActivityAddRoom.this, "Tạo phòng thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RoomResponse> call, Throwable t) {
                // Handle failure
                Toast.makeText(AdminActivityAddRoom.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("AdminActivityAddRoom", "Tạo Phòng thất bại: " + t.getMessage());
            }
        });


    }

}
