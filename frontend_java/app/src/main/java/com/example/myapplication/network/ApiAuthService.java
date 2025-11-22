package com.example.myapplication.network;

import com.example.myapplication.models.LoginRequest;
import com.example.myapplication.models.LoginResponse;
import com.example.myapplication.models.RegisterReponse;
import com.example.myapplication.models.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiAuthService {

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);


    @POST("auth/register")
    Call<RegisterReponse> register(@Body RegisterRequest registerRequest);
}
