package com.example.myapplication.network;

import com.example.myapplication.models.RegisterRequest;
import com.example.myapplication.models.StatusMessage;
import com.example.myapplication.models.UserInfo;
import com.example.myapplication.models.UserUpdateRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiUserService {
    @GET("users/get/{userId}")
    Call<UserInfo> getUserById(@Header("Authorization") String token, @Path("userId") String userId);

    @GET("users/get_all")
    Call<List<UserInfo>> getAllUsers(@Header("Authorization") String token);

    @PUT("users/update/{userId}")
    Call<UserInfo> updateUser(@Header("Authorization") String token, @Path("userId") String userId, @Body UserUpdateRequest userUpdateRequest);

    @DELETE("users/delete/{userId}")
    Call<StatusMessage> deleteUser(@Header("Authorization") String token, @Path("userId") String userId);


    @POST("users/create")
    Call<UserInfo> createUser(@Header("Authorization") String token,
                              @Body RegisterRequest registerRequest);

}
