package com.example.myapplication.network;

import com.example.myapplication.models.DetailFilm;
import com.example.myapplication.models.FilmIdBroadcastToday;
import com.example.myapplication.models.FilmRequest;
import com.example.myapplication.models.FilmShow;
import com.example.myapplication.models.FilmUpdateRequest;
import com.example.myapplication.models.StatusMessage;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiFilmService {

    @GET("firms/get_all")
    Call<List<FilmShow>> getAllFilms();

    @GET("firms/get/{id}")
    Call<DetailFilm> getFirmById(@Header("Authorization") String token, @Path("id") String id);


    @GET("firms/list_firmIds_broadcast_today")
    Call<FilmIdBroadcastToday> getFirmIdsBroadcastToday(@Header("Authorization") String token);


    @POST("firms/create")
    Call<FilmShow> createFirm(@Header("Authorization") String token, @Body FilmRequest filmRequest);

    @PUT("firms/update/{firmId}")
    Call<FilmShow> updateFirm(@Header("Authorization") String token, @Path("firmId") int firmId, @Body FilmUpdateRequest filmUpdateRequest);


    @DELETE("firms/delete/{id}")
    Call<StatusMessage> deleteFirm(@Header("Authorization") String token, @Path("id") int id);



}
