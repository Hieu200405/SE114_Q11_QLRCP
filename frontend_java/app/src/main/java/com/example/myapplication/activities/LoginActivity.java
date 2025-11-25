package com.example.myapplication.activities;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;


import com.example.myapplication.R;
import com.example.myapplication.models.LoginRequest;
import com.example.myapplication.models.LoginResponse;
import com.example.myapplication.network.ApiAuthService;
import com.example.myapplication.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    ApiAuthService apiAuthService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        apiAuthService = ApiClient.getRetrofit().create(ApiAuthService.class);

        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            String username = ((android.widget.EditText) findViewById(R.id.etUsername)).getText().toString();
            String password = ((android.widget.EditText) findViewById(R.id.etPassword)).getText().toString();

            if(username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tài khoản và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            LoginRequest loginRequest = new LoginRequest(username, password);
            LoginApi(loginRequest);

        });

        TextView registerLink = findViewById(R.id.tvRegisterLink);
        registerLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void LoginApi(LoginRequest loginRequest){
        Call<LoginResponse> call = apiAuthService.login(loginRequest);
        call.enqueue(new Callback<LoginResponse>() {


            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    // Lưu vào SharedPreferences
                    SharedPreferences sharedPref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("access_token", loginResponse.getAccessToken());
                    editor.putString("refresh_token", loginResponse.getRefreshToken());
                    editor.putString("username", loginResponse.getUsername());
                    editor.putString("role", loginResponse.getRole());
                    editor.putInt("user_id", loginResponse.getId());
                    editor.apply();

                    Log.e("username", loginResponse.getUsername());
                    Log.e("role", loginResponse.getRole());

                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    if(loginResponse.getRole().equals("admin")) {
                        Toast.makeText(LoginActivity.this ,"Đăng nhập với quyền admin", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, AdminMainActivity.class));
                    }
                    else {
                        startActivity(new Intent(LoginActivity.this, UserMainActivity.class));
                    }

                } else {
                    Toast.makeText(LoginActivity.this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {

            }

        });
    }


}
