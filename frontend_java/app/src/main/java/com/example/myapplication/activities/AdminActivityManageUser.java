package com.example.myapplication.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.UserAdapter;
import com.example.myapplication.models.StatusMessage;
import com.example.myapplication.models.UserInfo;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiUserService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminActivityManageUser extends AppCompatActivity {

    int EDIT_USER_REQUEST_CODE = 1;
    String accessToken;
    ImageView imageHome, imageManageFirm, imageManageUser, imageManageRoom, imageUser;
    FloatingActionButton fabAddUser;
    RecyclerView recyclerViewUser;
    UserAdapter userAdapter;
    List<UserInfo> userInfoList = new ArrayList<>();
    // Cache to store user info list
    List<UserInfo> cachedUserInfoList = new ArrayList<>();

    ActivityResultLauncher<Intent> launchEditUserActivity;
    ActivityResultLauncher<Intent> launchAddUserActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_manage_user);
        setElemntById();
//        Get access token from  shared preferences
        accessToken = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("access_token", null);
        if (accessToken == null) {
            Toast.makeText(this, "Access token not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

//        Set up the RecyclerView and UserAdapter
        userAdapter = new UserAdapter(userInfoList);
        recyclerViewUser.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUser.setAdapter(userAdapter);
        ListenerUserAdapterOnclick();
        LoadUserList();

        setLauchEditUserActivity();

//        Sự kiện tạo user từ admin
        setLauchAddUserActivity();
        setOnclickAddUser();

    }

    private void setElemntById(){
        imageHome = findViewById(R.id.imageHome);
        imageManageFirm = findViewById(R.id.imageManageFirm);
        imageManageUser = findViewById(R.id.imageManageUser);
        imageManageRoom = findViewById(R.id.imageManageRoom);
        imageUser = findViewById(R.id.imageProfile);
        fabAddUser = findViewById(R.id.buttonAddUser);
        recyclerViewUser = findViewById(R.id.UsersRecyclerView);

        imageHome.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivityManageUser.this, AdminMainActivity.class));
        });
        imageManageFirm.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivityManageUser.this, AdminActivityManageFirm.class));
        });
        imageManageRoom.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivityManageUser.this, AdminActivityManageRoom.class));
        });
        imageUser.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivityManageUser.this, AdminActivityProfile.class));
        });
    }
    void ListenerUserAdapterOnclick(){

        userAdapter.setOnItemClickListener(new UserAdapter.OnItemClickListener() {

            @Override
            public void onViewClick(UserInfo userInfo) {
                onViewClickEvent(userInfo);
            }

            @Override
            public void onEditClick(UserInfo userInfo) {
                Intent intent = new Intent(AdminActivityManageUser.this, AdminActivityEditProfileUser.class);
                intent.putExtra("userInfo", userInfo);
                launchEditUserActivity.launch(intent);
            }

            @Override
            public void onDeleteClick(UserInfo userInfo) {
                AlertDialogDeleteUser(userInfo);
            }
        });
    }

    void LoadUserList() {
        String token = "Bearer " + accessToken;
        ApiUserService apiUserService = ApiClient.getRetrofit().create(ApiUserService.class);
        Call<List<UserInfo>> call = apiUserService.getAllUsers(token);
        call.enqueue(
                new Callback<List<UserInfo>>() {
                    @Override
                    public void onResponse(Call<List<UserInfo>> call, Response<List<UserInfo>> response){
                        if (response.isSuccessful() && response.body() != null) {
                            cachedUserInfoList.clear();
                            cachedUserInfoList.addAll(response.body());
                            userInfoList.clear();
                            userInfoList.addAll(response.body());
                            userAdapter.notifyDataSetChanged();
                        } else {
                            // Handle the case where the response is not successful
                            Toast.makeText(AdminActivityManageUser.this, "Tải người dùng thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<UserInfo>> call, Throwable t) {
                        Toast.makeText(AdminActivityManageUser.this, "Tải người dùng thất bại", Toast.LENGTH_SHORT).show();
                        Log.e("API_ERROR", "Failed to load users: " + t.getMessage());
                    }
                }
        );
    }


//    Sự kiện xóa người dùng
    void AlertDialogDeleteUser(UserInfo userInfo) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xóa người dùng")
                .setMessage("Bạn có chắc chắn muốn xóa người dùng này?")
                .setPositiveButton("Có", (dialog, which) -> DeleteUserByApi(userInfo))
                .setNegativeButton("Không", null)
                .show();
    }
    void DeleteUserByApi(UserInfo userInfo) {
        String token = "Bearer " + accessToken;
        ApiUserService apiUserService = ApiClient.getRetrofit().create(ApiUserService.class);
        Call<StatusMessage> call = apiUserService.deleteUser(token, String.valueOf(userInfo.getId()));
        call.enqueue(new Callback<StatusMessage>() {
            @Override
            public void onResponse(Call<StatusMessage> call, Response<StatusMessage> response) {
                if (response.isSuccessful()) {
                    // Remove the user from the list and notify the adapter
                    userInfoList.remove(userInfo);
                    userAdapter.notifyDataSetChanged();
                    Toast.makeText(AdminActivityManageUser.this, "Xóa người dùng thất bại", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminActivityManageUser.this, "Người dùng đã đặt vé không thể xóa", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<StatusMessage> call, Throwable t) {
                Toast.makeText(AdminActivityManageUser.this, "Xóa người dùng thất bại: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


//    Sự kiện xem chi tiết người dùng



    public void onViewClickEvent(UserInfo userInfo) {
        // Inflate layout dialog
        View dialogView = LayoutInflater.from(AdminActivityManageUser.this).inflate(R.layout.admin_dialog_user_detail, null);

        // Ánh xạ các view trong dialog
        TextView textUserId = dialogView.findViewById(R.id.textUserId);
        TextView textName = dialogView.findViewById(R.id.textName);
        TextView textUsername = dialogView.findViewById(R.id.textUsername);
        TextView textEmail = dialogView.findViewById(R.id.textEmail);
        TextView textPhone = dialogView.findViewById(R.id.textPhone);
        TextView textRole = dialogView.findViewById(R.id.textRole);
        Button btnClose = dialogView.findViewById(R.id.btnClose);

        // Set dữ liệu vào các TextView
        textUserId.setText(String.valueOf(userInfo.getId()));
        textName.setText(userInfo.getName());
        textUsername.setText(userInfo.getUsername());
        textEmail.setText(userInfo.getEmail());
        textPhone.setText(userInfo.getPhone());
        textRole.setText(userInfo.getRole());

        // Tạo AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(AdminActivityManageUser.this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Thiết lập viền bo và nền trong suốt cho dialog
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Cài đặt kích thước dialog (tùy ý)
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Bắt sự kiện đóng dialog
        btnClose.setOnClickListener(v -> dialog.dismiss());

        // Hiển thị dialog
        dialog.show();
    }


//      Sự kiện sửa người dùng
    void setLauchEditUserActivity() {
        launchEditUserActivity = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == EDIT_USER_REQUEST_CODE) {
                    Intent data = result.getData();
                    if (data != null) {
                        UserInfo updatedUserInfo = data.getParcelableExtra("updatedUserInfo");
                        if (updatedUserInfo != null) {
                            // Update the user list and notify the adapter
                            for (int i = 0; i < userInfoList.size(); i++) {
                                if (userInfoList.get(i).getId() == updatedUserInfo.getId()) {
                                    userInfoList.set(i, updatedUserInfo);
                                    userAdapter.notifyItemChanged(i);
                                    break;
                                }
                            }
                        }
                    }
                }
            });
    }

//      Sự kiện thêm người dùng
    void setLauchAddUserActivity() {
        launchAddUserActivity = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        UserInfo newUserInfo = data.getParcelableExtra("newUserInfo");
                        if (newUserInfo != null) {
                            // Add the new user to the list and notify the adapter
                            userInfoList.add(newUserInfo);
                            userAdapter.notifyItemInserted(userInfoList.size() - 1);
                        }
                    }
                }
            });
    }

    void setOnclickAddUser() {
        fabAddUser.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivityManageUser.this, AdminActivityCreateUser.class);
            launchAddUserActivity.launch(intent);
        });
    }


}
