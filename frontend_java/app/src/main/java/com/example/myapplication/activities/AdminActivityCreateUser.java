package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.models.RegisterRequest;
import com.example.myapplication.models.UserInfo;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiUserService;

import retrofit2.Call;

public class AdminActivityCreateUser extends AppCompatActivity {

    String accessToken;
    EditText editName, editUsername, editPassword, editPhone, editEmail;
    Button btnCancel, btnCreate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_create_user);

//        get access token from shared preferences
        accessToken = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("access_token", null);
        Log.d("accessToken", "Access Token: " + accessToken);

        setElementById();
        btnCancel.setOnClickListener(
                v -> {
                    finish();
                }
        );
        btnCreate.setOnClickListener(
            v -> {
                String name = editName.getText().toString().trim();
                String username = editUsername.getText().toString().trim();
                String password = editPassword.getText().toString().trim();
                String phone = editPhone.getText().toString().trim();
                String email = editEmail.getText().toString().trim();

                if (name.isEmpty() || username.isEmpty() || password.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                    Toast.makeText(AdminActivityCreateUser.this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isValidPhoneNumber(phone)) {
                    Toast.makeText(AdminActivityCreateUser.this, "Số điện thoại không hợp lệ!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isValidEmail(email)) {
                    Toast.makeText(AdminActivityCreateUser.this, "Email không hợp lệ!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isValidPassword(password)) {
                    Toast.makeText(AdminActivityCreateUser.this, "Mật khẩu phải có ít nhất 6 kí tự!", Toast.LENGTH_SHORT).show();
                    return;
                }
                RegisterRequest registerRequest = new RegisterRequest(username, password, name, email, phone);
                createUserByApi(registerRequest);
            }
        );
        
    }
    void setElementById() {
        editName = findViewById(R.id.editName);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        editPhone = findViewById(R.id.editPhone);
        editEmail = findViewById(R.id.editEmail);
        btnCancel = findViewById(R.id.btnCancel);
        btnCreate = findViewById(R.id.btnCreate);
    }

    void createUserByApi(RegisterRequest registerRequest) {
        ApiUserService apiUserService = ApiClient.getRetrofit().create(ApiUserService.class);
        Call<UserInfo> call = apiUserService.createUser("Bearer" + accessToken, registerRequest);
        call.enqueue(
                new retrofit2.Callback<UserInfo>() {
                    @Override
                    public void onResponse(Call<UserInfo> call, retrofit2.Response<UserInfo> response) {
                        if (response.isSuccessful()) {
                            UserInfo userInfo = response.body();
                            if (userInfo != null) {
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("newUserInfo", userInfo);
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            }
                        } else {
                            // Handle error response
                            Toast.makeText(AdminActivityCreateUser.this, "Lỗi tạo user: " + response.message(), Toast.LENGTH_SHORT).show();
                            Log.e("AdminActivityCreateUser", "Lỗi tạo user: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<UserInfo> call, Throwable t) {
                        // Handle failure
                        Toast.makeText(AdminActivityCreateUser.this,"Trùng thông tin user với tài khoản khác, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                        Log.e("AdminActivityCreateUser", "Tạo user thất bại: " + t.getMessage());
                    }
                }
        );

    }

    //    check password has at least 6 chars
    private boolean isValidPassword(String password) {
        // Kiểm tra mật khẩu có ít nhất 6 kí tự
        return password.length() >= 6;
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
}
