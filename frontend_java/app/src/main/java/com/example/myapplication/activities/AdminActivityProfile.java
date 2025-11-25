package com.example.myapplication.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.models.UserInfo;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiUserService;

import java.io.IOException;

import retrofit2.Call;

public class AdminActivityProfile extends AppCompatActivity {

    private String accessToken;
    private int userId;

    TextView tvName;
    TextView tvEmail;
    TextView tvPhone;
    TextView tvUsername;
    TextView role;
    TextView tvUserID;

    ImageView imageHome;
    ImageView imageManageFirm;
    ImageView imageManageUser;
    ImageView imageManageRoom;
    ImageView imageUser;
    Button btnEdit;
    Button btnLogout;
    ActivityResultLauncher launcherEditProfile;
    UserInfo userInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_profile);
//        find elements by ID
        setElementsByID();

        // Get the access token and user ID from SharedPreferences
        accessToken = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("access_token", null);
        userId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getInt("user_id", -1);
        Log.d("TOKEN", "Token: " + accessToken);
        Log.d("USER_ID", "User ID: " + userId);
        // Check if accessToken and userId are valid
        if (accessToken == null || userId == -1) {
            Log.e("UserActivityProfile", "Invalid access token or user ID");
            return; // Exit if the token or user ID is not valid
        }
        // Initialize UI components and set up event listeners here

        setUpLauncherEditProfile();


        // Load user profile data using the API
        loadApiUserProfile(accessToken, userId);



        //Listeners for menu buttons
        listenerSetupMenu();
        // Listener for logout button
        listenerLogout();
        // Listener for edit profile button
        listenerEditProfile();

    }

    private void setElementsByID(){
        tvName = findViewById(R.id.textName);
        tvEmail = findViewById(R.id.textEmail);
        tvPhone = findViewById(R.id.textPhone);
        tvUsername = findViewById(R.id.textUsername);
        role = findViewById(R.id.textRole);
        tvUserID = findViewById(R.id.textUserID);
        imageHome = findViewById(R.id.imageHome);
        imageManageFirm = findViewById(R.id.imageManageFirm);
        imageManageRoom = findViewById(R.id.imageManageRoom);
        imageManageUser = findViewById(R.id.imageManageUser);
        imageUser = findViewById(R.id.imageProfile);
        btnEdit = findViewById(R.id.btnEdit);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void loadApiUserProfile(String accessToken, int userId) {
        String token = "Bearer " + accessToken;
        // Implement the API call to load user profile data
        // Use apiUserService to fetch user details and update the TextViews
        ApiUserService apiUserService = ApiClient.getRetrofit().create(ApiUserService.class);
        Call<UserInfo> call = apiUserService.getUserById(token, String.valueOf(userId));
        call.enqueue(new retrofit2.Callback<UserInfo>() {

            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call<UserInfo> call, retrofit2.Response<UserInfo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userInfo = response.body();
                    tvName.setText("Tên: "+ userInfo.getName());
                    tvEmail.setText("Email: " + userInfo.getEmail());
                    tvPhone.setText("Số điện thoại: " + userInfo.getPhone());
                    tvUsername.setText("Tên đăng nhập: " + userInfo.getUsername());
                    role.setText("Vai trò: " + userInfo.getRole());
                    tvUserID.setText("UID: " + String.valueOf(userInfo.getId()));
                } else {
                    // Handle the case where the response is not successful
                    if (response.errorBody() != null) {
                        try {
                            String errorMessage = response.errorBody().string();
                            // Log or display the error message
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<UserInfo> call, Throwable t) {
                // Handle the failure case
                t.printStackTrace();
                Log.e("UserActivityProfile", "Failed to load user profile: " + t.getMessage());
            }
        });

    }


    @SuppressLint("SetTextI18n")
    void setUpLauncherEditProfile() {
        launcherEditProfile = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            UserInfo updatedUserInfo = data.getParcelableExtra("updatedUserInfo");
                            if (updatedUserInfo != null) {
                                tvName.setText("Tên: " + updatedUserInfo.getName());
                                tvEmail.setText("Email: " + updatedUserInfo.getEmail());
                                tvPhone.setText("Số điện thoại: " + updatedUserInfo.getPhone());
                                tvUsername.setText("Tên người dùng: " + updatedUserInfo.getUsername());
                                role.setText("Vai Trò: " + updatedUserInfo.getRole());
                                tvUserID.setText("UID: " + updatedUserInfo.getId());
                            }
                        }
                    }
                }
        );
    }
    void listenerLogout() {
        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(AdminActivityProfile.this)
                    .setTitle("Xác nhận đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        // Xử lý logout
                        deleteDataSaved();

                        Intent intent = new Intent(AdminActivityProfile.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> {
                        dialog.dismiss(); // Đóng dialog nếu chọn Cancel
                    })
                    .show();
        });
    }

    void deleteDataSaved(){
        SharedPreferences preferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear(); // hoặc editor.remove("access_token"); nếu chỉ muốn xóa 1 giá trị
        editor.apply();
    }


    void listenerEditProfile() {
        btnEdit.setOnClickListener(v -> {
            // Handle edit profile button click
            Intent intent = new Intent(AdminActivityProfile.this, UserEditProfile.class);
            intent.putExtra("access_token", accessToken);
            intent.putExtra("userID", userId);
            intent.putExtra("name", userInfo.getName());
            intent.putExtra("username", userInfo.getUsername());
            intent.putExtra("email", userInfo.getEmail());
            intent.putExtra("phone", userInfo.getPhone());
            launcherEditProfile.launch(intent);
        });
    }


    void listenerSetupMenu(){
//        Listener for home button
        imageHome.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivityProfile.this, AdminMainActivity.class);
            startActivity(intent);
        });

        imageManageRoom.setOnClickListener(
                v -> {
                    Intent intent = new Intent(AdminActivityProfile.this, AdminActivityManageRoom.class);
                    startActivity(intent);
                }
        );

        imageManageFirm.setOnClickListener(
                v -> {
                    Intent intent = new Intent(AdminActivityProfile.this, AdminActivityManageFirm.class);
                    startActivity(intent);
                }
        );
        imageManageUser.setOnClickListener(
                v -> {
                    Intent intent = new Intent(AdminActivityProfile.this, AdminActivityManageUser.class);
                    startActivity(intent);
                }
        );
    }
}
