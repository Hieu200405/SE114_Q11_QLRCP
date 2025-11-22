package com.example.myapplication.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.models.Broadcast;
import com.example.myapplication.models.BroadcastFirm;
import com.example.myapplication.models.BroadcastFirmRequest;
import com.example.myapplication.models.RoomResponse;
import com.example.myapplication.network.ApiBroadcastService;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiRoomService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;

public class AdminActivityCreateBroadcast extends AppCompatActivity {
    String accessToken;
    List<RoomResponse> roomList = new ArrayList<>();
    Spinner spinnerRoom;
    EditText editTime, editDate, editPrice, editSeats;
    TextView textRoomInfo;
    Button btnCancel, btnCreate;
    int selectedMaxSeats = 0;
    ArrayAdapter<String> adapter;
    int room_id;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_create_broadcast);
//        Get access token from shared preferences
        accessToken = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("access_token", null);
        if (accessToken == null) {
            Toast.makeText(this, "Access token không tìm thấy", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        int firmId = getIntent().getIntExtra("firmId", -1);
        loadRoomsByApi();
        setElementsByID();
        setOnclickSpinnerRoom();
        setButtonListeners(firmId);
    }

    @SuppressLint("WrongViewCast")
    private void setElementsByID() {
        spinnerRoom = findViewById(R.id.spinnerRoom);
        editTime = findViewById(R.id.editTime);
        editDate = findViewById(R.id.editDate);
        editPrice = findViewById(R.id.editPrice);
        editSeats = findViewById(R.id.editSeats);
        btnCancel = findViewById(R.id.btnCancel);
        btnCreate = findViewById(R.id.btnCreate);
        textRoomInfo = findViewById(R.id.textRoomInfo);

        editDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        editDate.setText(selectedDate);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        editTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view, selectedHour, selectedMinute) -> {
                        String selectedTime = String.format("%02d:%02d:00", selectedHour, selectedMinute);
                        editTime.setText(selectedTime);
                    },
                    hour, minute, true // true = 24h format
            );
            timePickerDialog.show();
        });
    }
    void  loadRoomsByApi() {
        // This method should call the API to load the rooms and set them to the spinner
        // For example, using Retrofit to fetch the rooms and then setting the adapter for the spinner
        ApiRoomService apiRoomService = ApiClient.getRetrofit().create(ApiRoomService.class);
        Call<List<RoomResponse>> call = apiRoomService.getAllRooms("Bearer " + accessToken);
        call.enqueue(new retrofit2.Callback<List<RoomResponse>>() {
            @Override
            public void onResponse(Call<List<RoomResponse>> call, retrofit2.Response<List<RoomResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    roomList.clear();
                    roomList.addAll(response.body());
                    List<String> roomNames = new ArrayList<>();
                    for (RoomResponse room : roomList) {
                        roomNames.add(room.getName());
                    }
                    adapter = new ArrayAdapter<>(AdminActivityCreateBroadcast.this, R.layout.spinner_selected_item, roomNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
                    spinnerRoom.setAdapter(adapter);
                } else {
                    // Handle the error case
                    Toast.makeText(AdminActivityCreateBroadcast.this, "Lỗi khi tải danh sách phòng: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RoomResponse>> call, Throwable t) {
                // Handle the failure case
                Toast.makeText(AdminActivityCreateBroadcast.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    void setOnclickSpinnerRoom() {
        // Xử lý chọn phòng
        spinnerRoom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                RoomResponse selectedRoom = roomList.get(position);
                textRoomInfo.setText("ID: " + selectedRoom.getId() +
                        " | Tên: " + selectedRoom.getName() +
                        " | Ghế tối đa: " + selectedRoom.getSeats());
                selectedMaxSeats = selectedRoom.getSeats();
                room_id = selectedRoom.getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }


    void setButtonListeners(int firmId) {
        btnCancel.setOnClickListener(v -> finish());
        btnCreate.setOnClickListener(
            v -> {
                String time = editTime.getText().toString();
                String date = editDate.getText().toString();
                String priceStr = editPrice.getText().toString();
                String seatsStr = editSeats.getText().toString();

                if (time.isEmpty() || date.isEmpty() || priceStr.isEmpty() || seatsStr.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // ngày chiếu lớn hơn ngày hiện tại
                Calendar calendar = Calendar.getInstance();
                String currentDate = String.format("%04d-%02d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
                if (!date.isEmpty() && date.compareTo(currentDate) < 0) {
                    Toast.makeText(this, "Ngày chiếu phải lớn hơn hoặc bằng ngày hiện tại!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (time.isEmpty() || date.isEmpty() || priceStr.isEmpty() || seatsStr.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                    return;
                }

                int price = Integer.parseInt(priceStr);
                int seats = Integer.parseInt(seatsStr);

                if (price <= 10000) {
                    Toast.makeText(this, "Giá vé phải lớn hơn 10.000!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (seats > selectedMaxSeats) {
                    Toast.makeText(this, "Số ghế vượt quá số ghế tối đa của phòng!", Toast.LENGTH_SHORT).show();
                    return;
                }
                BroadcastFirmRequest broadcastFirmRequest = new BroadcastFirmRequest(room_id, firmId, time, date, price, seats);
                createBroadcastApi(broadcastFirmRequest);
            }
        );

    }


    void  createBroadcastApi(BroadcastFirmRequest broadcastFirmRequest){
        ApiBroadcastService apiBroadcastService = ApiClient.getRetrofit().create(ApiBroadcastService.class);
        Call<BroadcastFirm> call = apiBroadcastService.createBroadcast("Bearer " + accessToken, broadcastFirmRequest);
        call.enqueue(
            new retrofit2.Callback<BroadcastFirm>() {
                @Override
                public void onResponse(Call<BroadcastFirm> call, retrofit2.Response<BroadcastFirm> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Handle successful creation of broadcast
                        BroadcastFirm createdBroadcast = response.body();
                        Toast.makeText(AdminActivityCreateBroadcast.this, "Tạo buổi chiếu thành công!", Toast.LENGTH_SHORT).show();
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("newBroadcast", createdBroadcast);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        // Handle the error case
                        Toast.makeText(AdminActivityCreateBroadcast.this, "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<BroadcastFirm> call, Throwable t) {
                    // Handle the failure case
                    Toast.makeText(AdminActivityCreateBroadcast.this, "Gọi API lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        );


    }

}
