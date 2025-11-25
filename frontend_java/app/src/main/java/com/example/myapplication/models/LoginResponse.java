package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("ID")
    private int id;

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("role")
    private String role;

    @SerializedName("username")
    private String username;

    // Getters
    public int getId() { return id; }
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getRole() { return role; }
    public String getUsername() { return username; }
}