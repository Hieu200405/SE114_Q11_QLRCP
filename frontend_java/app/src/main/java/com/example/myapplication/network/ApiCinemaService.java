package com.example.myapplication.network;

import com.example.myapplication.models.Cinema;
import com.example.myapplication.models.CinemaRequest;
import com.example.myapplication.models.DistanceRequest;
import com.example.myapplication.models.DistanceResponse;
import com.example.myapplication.models.MapConfig;
import com.example.myapplication.models.NearbyCinemaRequest;
import com.example.myapplication.models.PlaceAutocomplete;
import com.example.myapplication.models.PlaceDetail;
import com.example.myapplication.models.StatusMessage;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * API Service for Cinema operations
 * Base URL: /api/cinemas/
 */
public interface ApiCinemaService {

    // ==================== CRUD OPERATIONS ====================

    /**
     * Get all cinemas
     * URL: GET /api/cinemas/get_all
     */
    @GET("cinemas/get_all")
    Call<List<Cinema>> getAllCinemas();

    /**
     * Get cinema by ID with rooms
     * URL: GET /api/cinemas/get/{cinema_id}
     */
    @GET("cinemas/get/{cinema_id}")
    Call<Cinema> getCinemaById(@Path("cinema_id") int cinemaId);

    /**
     * Create new cinema
     * URL: POST /api/cinemas/create
     */
    @POST("cinemas/create")
    Call<Cinema> createCinema(
            @Header("Authorization") String token,
            @Body CinemaRequest cinemaRequest
    );

    /**
     * Update existing cinema
     * URL: PUT /api/cinemas/update/{cinema_id}
     */
    @PUT("cinemas/update/{cinema_id}")
    Call<Cinema> updateCinema(
            @Header("Authorization") String token,
            @Path("cinema_id") int cinemaId,
            @Body CinemaRequest cinemaRequest
    );

    /**
     * Delete cinema (soft delete)
     * URL: DELETE /api/cinemas/delete/{cinema_id}
     */
    @DELETE("cinemas/delete/{cinema_id}")
    Call<StatusMessage> deleteCinema(
            @Header("Authorization") String token,
            @Path("cinema_id") int cinemaId
    );

    // ==================== LOCATION & DISTANCE OPERATIONS ====================

    /**
     * Get nearby cinemas based on user location
     * URL: POST /api/cinemas/nearby
     * Returns cinemas sorted by distance with actual driving distance from Goong
     */
    @POST("cinemas/nearby")
    Call<List<Cinema>> getNearbyCinemas(@Body NearbyCinemaRequest request);

    /**
     * Get cinemas showing a specific film
     * URL: POST /api/cinemas/for-film/{film_id}
     * Body (optional): { "lat": 10.7769, "lng": 106.7009 }
     */
    @POST("cinemas/for-film/{film_id}")
    Call<List<Cinema>> getCinemasForFilm(
            @Path("film_id") int filmId,
            @Body NearbyCinemaRequest locationRequest
    );

    /**
     * Calculate distance from user location to multiple cinema locations
     * URL: POST /api/cinemas/distance
     */
    @POST("cinemas/distance")
    Call<DistanceResponse> calculateDistance(@Body DistanceRequest request);

    // ==================== GOONG MAP UTILITIES ====================

    /**
     * Search places with autocomplete (using backend proxy)
     * URL: GET /api/cinemas/search-places?keyword=CGV&lat=10.7769&lng=106.7009
     */
    @GET("cinemas/search-places")
    Call<List<PlaceAutocomplete.Prediction>> searchPlaces(
            @Query("keyword") String keyword,
            @Query("lat") Double lat,
            @Query("lng") Double lng
    );

    /**
     * Get place detail by place_id (using backend proxy)
     * URL: GET /api/cinemas/place-detail?place_id=xxx
     */
    @GET("cinemas/place-detail")
    Call<Map<String, Object>> getPlaceDetail(@Query("place_id") String placeId);

    /**
     * Forward geocoding - convert address to coordinates (using backend proxy)
     * URL: GET /api/cinemas/geocode?address=72 Lê Thánh Tôn, Quận 1, TP.HCM
     */
    @GET("cinemas/geocode")
    Call<Map<String, Object>> geocodeAddress(@Query("address") String address);

    /**
     * Reverse geocoding - convert coordinates to address (using backend proxy)
     * URL: GET /api/cinemas/reverse-geocode?lat=10.7769&lng=106.7009
     */
    @GET("cinemas/reverse-geocode")
    Call<Map<String, Object>> reverseGeocode(
            @Query("lat") double lat,
            @Query("lng") double lng
    );

    /**
     * Get map configuration (map key for frontend)
     * URL: GET /api/cinemas/map-config
     */
    @GET("cinemas/map-config")
    Call<MapConfig> getMapConfig();
}

