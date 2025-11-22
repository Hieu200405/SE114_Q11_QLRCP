package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.models.UserInfo;
import com.example.myapplication.models.UserUpdateRequest;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiUserService;

import java.io.IOException;

import retrofit2.Call;

public class AdminActivityEditProfileUser extends AppCompatActivity {
    String accessToken;

    TextView tvUID, tvUsername, editName, editEmail, editPhone;
    Button btnSave, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_edit_profile_of_user);
//        Get the access token from SharedPreferences
        accessToken = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("access_token", null);

        UserInfo userInfo = (UserInfo) getIntent().getParcelableExtra("userInfo");
        if (userInfo == null) {
            finish();
            return;
        }
        ListenerButtonEvents(userInfo);




    }
    private void setElementsByID() {
        tvUID = findViewById(R.id.tvUID);
        tvUsername = findViewById(R.id.tvUsername);
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    void ListenerButtonEvents(UserInfo userInfo) {
        setElementsByID();
        tvUID.setText("UID: " + userInfo.getId());
        tvUsername.setText("User: " + userInfo.getUsername());
        editName.setText(userInfo.getName());
        editEmail.setText(userInfo.getEmail());
        editPhone.setText(userInfo.getPhone());

        btnSave.setOnClickListener(v -> {
            String Name = editName.getText().toString();
            String Email = editEmail.getText().toString();
            String Phone = editPhone.getText().toString();
            if( Name.isEmpty() || Email.isEmpty() || Phone.isEmpty()) {
                Toast.makeText(AdminActivityEditProfileUser.this, "Vui lòng điền vào tất cả các trường", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isValidEmail(Email)) {
                Toast.makeText(AdminActivityEditProfileUser.this, "Định dạng email không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isValidPhoneNumber(Phone)) {
                Toast.makeText(AdminActivityEditProfileUser.this, "Định dạng số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
            UserUpdateRequest userUpdateRequest = new UserUpdateRequest(Name, Phone, Email);
            updateProfile(userInfo.getId(), userUpdateRequest);


        });

        btnCancel.setOnClickListener(v -> {
            // Handle cancel button click
            finish();
        });
    }

    private boolean isValidPhoneNumber(String phone) {
        // Kiểm tra số điện thoại có 10 kí tự và chỉ chứa chữ số
        if('0' != phone.charAt(0)) {
            return false; // Số điện thoại phải bắt đầu bằng 0
        }
        if (phone.length() != 10) {
            return false;
        }
        for (char c : phone.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    //    check email format
    private boolean isValidEmail(String email) {
        // Kiểm tra định dạng email cơ bản
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }

    void  updateProfile(int userId, UserUpdateRequest userUpdateRequest) {
        ApiUserService apiUserService = ApiClient.getRetrofit().create(ApiUserService.class);
        Call<UserInfo> call = apiUserService.updateUser("Bearer " + accessToken, String.valueOf(userId), userUpdateRequest);
        call.enqueue(new retrofit2.Callback<UserInfo>() {
            @Override
            public void onResponse(Call<UserInfo> call, retrofit2.Response<UserInfo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Handle successful update
                    UserInfo updatedUser = response.body();
                    // You can update the UI or show a success message here
                    Toast.makeText(AdminActivityEditProfileUser.this, "Hồ sơ cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("updatedUserInfo", updatedUser);
                    setResult(1, intent);
                    finish(); // Close the activity after saving

                } else {
                    // Handle the case where the response is not successful
                    if (response.errorBody() != null) {
                        try {
                            String errorMessage = response.errorBody().string();
                            Toast.makeText(AdminActivityEditProfileUser.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                            Log.e("UserEditProfile", "Cập nhật lỗi hồ sơ: " + errorMessage);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(AdminActivityEditProfileUser.this, "Cập nhật lỗi hồ sơ", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserInfo> call, Throwable t) {
                // Handle the failure case
                Toast.makeText(AdminActivityEditProfileUser.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UserEditProfile", "Cập nhật lỗi hồ sơ: " + t.getMessage());
            }
        });
    }
}
