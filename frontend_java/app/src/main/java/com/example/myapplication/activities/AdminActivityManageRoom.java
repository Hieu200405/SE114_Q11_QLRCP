package com.example.myapplication.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.RoomAdapter;
import com.example.myapplication.cacheModels.CinemaCache;
import com.example.myapplication.helper.BroadcastCinemaEnricher;
import com.example.myapplication.helper.LocationHelper;
import com.example.myapplication.models.RoomResponse;
import com.example.myapplication.models.StatusMessage;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiRoomService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class AdminActivityManageRoom extends AppCompatActivity {
    private static final String TAG = "AdminManageRoom";
    private static final int REQUEST_CODE_EDIT_ROOM = 3;
    private static final int REQUEST_CODE_CANCEL = 2;
    private static final int REQUEST_CODE_ADD_ROOM = 4;
    String accessToken;

    // Cinema filter (optional - passed from CinemaDetailActivity)
    private int cinemaId = -1;
    private String cinemaName = null;

    // Location helper for distance calculation
    private LocationHelper locationHelper;

    List<RoomResponse> roomList;
    RoomAdapter roomAdapter;
    RecyclerView recyclerViewRooms;
    ActivityResultLauncher<Intent> launcherEditRoom;
    ActivityResultLauncher<Intent> launcherAddRoom;
    ImageView imageHome;
    ImageView imageManageFilm;
    ImageView imageManageUser;
    ImageView imageManageRoom;
    ImageView imageManageCinema;
    ImageView imageUser;
    private FloatingActionButton fabAddRoom;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_manage_room);
        // get accesstoken
        accessToken = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("access_token", null);

        // Check if filtering by cinema
        cinemaId = getIntent().getIntExtra("cinema_id", -1);
        cinemaName = getIntent().getStringExtra("cinema_name");

        // Initialize location helper
        locationHelper = new LocationHelper(this);

        setElementsByID();

        // Initialize RecyclerView
        recyclerViewRooms = findViewById(R.id.roomShowsRecyclerView);
        // Assuming roomList is already populated with RoomResponse objects
        roomList = new ArrayList<>();
        recyclerViewRooms.setLayoutManager(new LinearLayoutManager(this));
        roomAdapter = new RoomAdapter(roomList);
        recyclerViewRooms.setAdapter(roomAdapter);

        // Get user location and then load rooms
        getUserLocationAndLoadRooms();

        setOnclickRoomAdapter();
        setLauncherEditRoom();
        setLauncherAddRoom();



    }


    void  setElementsByID() {
        imageHome = findViewById(R.id.imageHome);
        imageManageFilm = findViewById(R.id.imageManageFilm);
        imageManageUser = findViewById(R.id.imageManageUser);
        imageManageRoom = findViewById(R.id.imageManageRoom);
        imageManageCinema = findViewById(R.id.imageManageCinema);
        imageUser = findViewById(R.id.imageProfile);
        fabAddRoom = findViewById(R.id.buttonAddRoom);

        // Set click listeners for the images
        imageHome.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivityManageRoom.this, AdminMainActivity.class);
            startActivity(intent);
        });

        imageManageFilm.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivityManageRoom.this, AdminActivityManageFilm.class);
            startActivity(intent);
        });

        imageManageUser.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivityManageRoom.this, AdminActivityManageUser.class);
            startActivity(intent);
        });

        imageManageCinema.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivityManageRoom.this, CinemaListActivity.class);
            intent.putExtra("isAdminMode", true);
            intent.putExtra("token", accessToken);
            startActivity(intent);
        });

        imageUser.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivityManageRoom.this, AdminActivityProfile.class);
            startActivity(intent);
        });

        fabAddRoom.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivityManageRoom.this, AdminActivityAddRoom.class);
            launcherAddRoom.launch(intent);
        });
    }

    /**
     * Get user location and then load rooms
     */
    private void getUserLocationAndLoadRooms() {
        locationHelper.getCurrentLocation(new LocationHelper.LocationResultListener() {
            @Override
            public void onLocationReceived(double latitude, double longitude) {
                Log.d(TAG, "User location: " + latitude + ", " + longitude);
                // Set user location to adapter for distance calculation
                roomAdapter.setUserLocation(latitude, longitude);
                // Now load rooms
                LoadRooms();
            }

            @Override
            public void onLocationError(String error) {
                Log.w(TAG, "Location error: " + error + ". Loading rooms without distance.");
                // Still load rooms even without location
                LoadRooms();
            }
        });
    }

    void LoadRooms() {
        // Load cinema cache first to ensure cinema info is available for display
        CinemaCache.loadCinemas(new CinemaCache.CinemaLoadListener() {
            @Override
            public void onCinemasLoaded(java.util.List<com.example.myapplication.models.Cinema> cinemas) {
                // Cinema cache loaded, now load rooms
                loadRoomsFromApi();
            }

            @Override
            public void onLoadError(String error) {
                // Still try to load rooms even if cinema cache fails
                Log.w(TAG, "Cinema cache load error: " + error);
                loadRoomsFromApi();
            }
        });
    }

    private void loadRoomsFromApi() {
        // This method should be implemented to load rooms from the server or database
        String token = "Bearer " + accessToken;

        ApiRoomService apiRoomService = ApiClient.getRetrofit().create(ApiRoomService.class);

        // Check if filtering by cinema
        Call<List<RoomResponse>> call;
        if (cinemaId > 0) {
            call = apiRoomService.getRoomsByCinema(cinemaId);
        } else {
            call = apiRoomService.getAllRooms(token);
        }

        call.enqueue(new retrofit2.Callback<List<RoomResponse>>() {
            @Override
            public void onResponse(Call<List<RoomResponse>> call, retrofit2.Response<List<RoomResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    roomList.clear();
                    roomList.addAll(response.body());
                    roomAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(AdminActivityManageRoom.this, "Tải phòng thất bại", Toast.LENGTH_SHORT).show();
                    Log.e("AdminActivityManageRoom", "Error loading rooms: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<RoomResponse>> call, Throwable t) {
                Toast.makeText(AdminActivityManageRoom.this, "Tải phòng thất bại", Toast.LENGTH_SHORT).show();
                Log.e("AdminActivityManageRoom", "Error loading rooms: " + t.getMessage());
            }
        });
    }



    void setOnclickRoomAdapter(){
        roomAdapter.setOnRoomClickListener(
            new RoomAdapter.OnRoomClickListener() {

                @Override
                public void onEditClick(RoomResponse room) {
                    setOnclickEditRoom(room);
                }

                @Override
                public void onDeleteClick(RoomResponse room) {
                    alertDialogDeleteRoom(room);
                }
            }
        );
    }
    void alertDialogDeleteRoom(RoomResponse room) {
        new AlertDialog.Builder(AdminActivityManageRoom.this)
            .setTitle("Xác nhận Xóa Phòng")
            .setMessage("Bạn có chắc chắn muốn xóa không?")
            .setPositiveButton("Chắc chắn", (dialog, which) -> {
                // Xử lý xóa phòng
                DeleteRoom(room);
            })
            .setNegativeButton("Hủy", (dialog, which) -> {
                dialog.dismiss(); // Đóng dialog nếu chọn Cancel
            })
            .show();
    }


    void DeleteRoom(RoomResponse room) {
        // Implement the logic to delete a room
        String token = "Bearer " + accessToken;
        ApiRoomService apiRoomService = ApiClient.getRetrofit().create(ApiRoomService.class);
        Call<StatusMessage> call = apiRoomService.deleteRoom(token, room.getId());
        call.enqueue(new retrofit2.Callback<StatusMessage>() {
            @Override
            public void onResponse(Call<StatusMessage> call, retrofit2.Response<StatusMessage> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StatusMessage statusMessage = response.body();

                    // Clear room cache to force reload updated data
                    BroadcastCinemaEnricher.clearRoomCache();

                    Toast.makeText(AdminActivityManageRoom.this, statusMessage.getMessage(), Toast.LENGTH_SHORT).show();
                    LoadRooms(); // Reload the room list after deletion
                } else {
                    Toast.makeText(AdminActivityManageRoom.this, "Xóa phòng thất bại", Toast.LENGTH_SHORT).show();
                    Log.e("AdminActivityManageRoom", "Lỗi xóa phòng: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<StatusMessage> call, Throwable t) {
                Toast.makeText(AdminActivityManageRoom.this, "Xóa phòng thất bại", Toast.LENGTH_SHORT).show();
                Log.e("AdminActivityManageRoom", "Lỗi xóa phòng: " + t.getMessage());
            }
        });

    }


    void setOnclickEditRoom(RoomResponse room){
        Intent intent = new Intent(AdminActivityManageRoom.this, AdminActivityEditRoom.class);
        intent.putExtra("room", room);
        launcherEditRoom.launch(intent);
    }

    void setLauncherEditRoom(){
        launcherEditRoom = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() ==  REQUEST_CODE_EDIT_ROOM) {
                    Intent data = result.getData();
                    if (data != null) {
                        RoomResponse updatedRoom = data.getParcelableExtra("updatedRoom");
                        if (updatedRoom != null) {
                            // Update the room list with the updated room
                            for (int i = 0; i < roomList.size(); i++) {
                                if (roomList.get(i).getId() == updatedRoom.getId()) {
                                    roomList.set(i, updatedRoom);
                                    break;
                                }
                            }
                            roomAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        );
    }


    void setOnClickAddRoom() {
        Intent intent = new Intent(AdminActivityManageRoom.this, AdminActivityAddRoom.class);
        launcherAddRoom.launch(intent);
    }
    void setLauncherAddRoom() {
        launcherAddRoom = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == REQUEST_CODE_ADD_ROOM) {
                    Intent data = result.getData();
                    if (data != null) {
                        RoomResponse newRoom = data.getParcelableExtra("room");
                        if (newRoom != null) {
                            roomList.add(newRoom);
                            roomAdapter.notifyItemInserted(roomList.size() - 1);
                        }
                    }
                }
            }
        );
    }
}
