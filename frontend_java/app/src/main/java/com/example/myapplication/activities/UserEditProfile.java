package com.example.myapplication.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
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

public class UserEditProfile extends AppCompatActivity {
    TextView tvUID;
    TextView tvUsername;
    EditText editName;
    EditText editEmail;
    EditText editPhone;
    Button btnSave;
    Button btnCancel;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_edit_profile);
        tvUID = findViewById(R.id.tvUID);
        tvUsername = findViewById(R.id.tvUsername);
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        // Assuming you have a User object with the necessary data
        Intent intent = getIntent();
        String accessToken = intent.getStringExtra("access_token");
        int userId = intent.getIntExtra("userID", -1);
        String username = intent.getStringExtra("username");
        String name = intent.getStringExtra("name");
        String email = intent.getStringExtra("email");
        String phone = intent.getStringExtra("phone");
        tvUID.setText("UID: "+ String.valueOf(userId));
        tvUsername.setText("User: " + username);

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);


        btnCancel.setOnClickListener(
                v -> {
                    // Handle cancel button click
                    // You can finish the activity or navigate back to the previous screen
                    finish();
                }
        );

        btnSave.setOnClickListener(
                v -> {
                    // Handle save button click
                    String newName = editName.getText().toString();
                    String newEmail = editEmail.getText().toString();
                    String newPhone = editPhone.getText().toString();
                    UserUpdateRequest userUpdateRequest = new UserUpdateRequest(newName, newPhone, newEmail);

                    if (isValidPhoneNumber(newPhone) && isValidEmail(newEmail)) {
                        // Call the updateProfile method to save changes
                        updateProfile(accessToken , userId, userUpdateRequest);
                    } else {
                        // Show an error message if validation fails
                        if (!isValidPhoneNumber(newPhone)) {
                            editPhone.setError("Số điện thoại không hợp lệ");
                        }
                        if (!isValidEmail(newEmail)) {
                            editEmail.setError("Định dạng email không hợp lệ");
                        }
                    }
                }
        );






    }

    //    check phone number has 10 chars and only contains digits
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



    void  updateProfile(String accessToken, int userId, UserUpdateRequest userUpdateRequest) {
        ApiUserService apiUserService = ApiClient.getRetrofit().create(ApiUserService.class);
        Call<UserInfo> call = apiUserService.updateUser("Bearer " + accessToken, String.valueOf(userId), userUpdateRequest);
        call.enqueue(new retrofit2.Callback<UserInfo>() {
            @Override
            public void onResponse(Call<UserInfo> call, retrofit2.Response<UserInfo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Handle successful update
                    UserInfo updatedUser = response.body();
                    // You can update the UI or show a success message here
                    Toast.makeText(UserEditProfile.this, "Hồ sơ cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("updatedUserInfo", updatedUser);
                    setResult(RESULT_OK, intent);
                    finish(); // Close the activity after saving

                } else {
                    // Handle the case where the response is not successful
                    if (response.errorBody() != null) {
                        try {
                            String errorMessage = response.errorBody().string();
                            Toast.makeText(UserEditProfile.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(UserEditProfile.this, "Hồ sơ cập nhật thất bại.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserInfo> call, Throwable t) {
                // Handle the failure case
                Toast.makeText(UserEditProfile.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UserEditProfile", "Failed to update profile: " + t.getMessage());
            }
        });
    }

}
