package com.example.myapplication.models;

public class LoginRequest {
    private String username;
    private String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters (cần thiết cho Retrofit)
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    // Setters (nếu bạn cần sửa đổi sau khi khởi tạo)
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}