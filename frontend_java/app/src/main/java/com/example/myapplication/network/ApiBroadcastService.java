package com.example.myapplication.network;

import com.example.myapplication.models.BroadcastFilm;
import com.example.myapplication.models.BroadcastFilmRequest;
import com.example.myapplication.models.Seat;
import com.example.myapplication.models.StatusMessage;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiBroadcastService {

    @GET("broadcasts/film/{id}")
    Call<List<BroadcastFilm>> getBroadcastsByFilmId(@Path("id") int filmId);

    @GET("broadcasts/seats/{id}")
    Call<List<Seat>> getSeatsByBroadcastId(@Path("id") int broadcastId);

    @POST("broadcasts/create")
    Call<BroadcastFilm> createBroadcast(@Header("Authorization") String accessToken, @Body BroadcastFilmRequest broadcastFilmRequest);


    @DELETE("broadcasts/delete/{id}")
    Call<StatusMessage> deleteBroadcast(@Header("Authorization") String Token, @Path("id") int broadcastId);
}
