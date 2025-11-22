package com.example.myapplication.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.models.LoginRequest;
import com.example.myapplication.models.LoginResponse;
import com.example.myapplication.models.RegisterReponse;
import com.example.myapplication.models.RegisterRequest;
import com.example.myapplication.network.ApiAuthService;
import com.example.myapplication.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    ApiAuthService apiAuthService;
    RegisterRequest registerRequest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        apiAuthService = ApiClient.getRetrofit().create(ApiAuthService.class);


        findViewById(R.id.btnRegister).setOnClickListener(v -> {
            String username = ((android.widget.EditText) findViewById(R.id.etUsername)).getText().toString();
            String password = ((android.widget.EditText) findViewById(R.id.etPassword)).getText().toString();
            String fullName = ((android.widget.EditText) findViewById(R.id.etFullName)).getText().toString();
            String email = ((android.widget.EditText) findViewById(R.id.etEmail)).getText().toString();
            String phone = ((android.widget.EditText) findViewById(R.id.etPhone)).getText().toString();
            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isValidPhoneNumber(phone)) {
                Toast.makeText(this, "Số điện thoại không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isValidEmail(email)) {
                Toast.makeText(this, "Email không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isValidPassword(password)) {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 6 kí tự!", Toast.LENGTH_SHORT).show();
                return;
            }

            registerRequest = new RegisterRequest(username, password, fullName, phone, email);
            RegisterApi(registerRequest);



        });
        TextView loginLink = findViewById(R.id.tvLoginLink);
        loginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // tùy chọn: đóng activity hiện tại
        });

    }


    private void RegisterApi(RegisterRequest registerRequest) {
        Call<RegisterReponse> call = apiAuthService.register(registerRequest);
        call.enqueue(new Callback<RegisterReponse>() {


            @Override
            public void onResponse(Call<RegisterReponse> call, Response<RegisterReponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RegisterActivity.this,"Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(RegisterActivity.this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<RegisterReponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this,"Trùng thông tin user với tài khoản khác, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            }

        });
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
