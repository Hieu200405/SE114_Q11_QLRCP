package com.example.myapplication.network;

import com.example.myapplication.models.DetailFilm;
import com.example.myapplication.models.FilmIdBroadcastToday;
import com.example.myapplication.models.FilmRequest;
import com.example.myapplication.models.FilmShow;
import com.example.myapplication.models.FilmUpdateRequest;
import com.example.myapplication.models.ReviewRequest;
import com.example.myapplication.models.ReviewResponse;
import com.example.myapplication.models.StatusMessage;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiFilmService {

    @GET("films/get_all")
    Call<List<FilmShow>> getAllFilms();

    @GET("films/get/{id}")
    Call<DetailFilm> getFilmById(@Header("Authorization") String token, @Path("id") String id);


    @GET("films/list_filmIds_broadcast_today")
    Call<FilmIdBroadcastToday> getFilmIdsBroadcastToday(@Header("Authorization") String token);


    @POST("films/create")
    Call<FilmShow> createFilm(@Header("Authorization") String token, @Body FilmRequest filmRequest);

    @PUT("films/update/{filmId}")
    Call<FilmShow> updateFilm(@Header("Authorization") String token, @Path("filmId") int filmId, @Body FilmUpdateRequest filmUpdateRequest);


    @DELETE("films/delete/{id}")
    Call<StatusMessage> deleteFilm(@Header("Authorization") String token, @Path("id") int id);


    @GET("reviews/film/{film_id}")
    Call<ReviewResponse> getFilmReviews(@Path("film_id") int filmId);

    @POST("reviews/add")
    Call<ReviewResponse> addReview(
            @Header("Authorization") String token,
            @Body ReviewRequest reviewRequest
    );

    @DELETE("reviews/admin/delete/{review_id}")
    Call<ReviewResponse> deleteReview(
            @Header("Authorization") String token,
            @Path("review_id") int reviewId
    );
}
