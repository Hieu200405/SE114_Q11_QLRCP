package com.example.myapplication.network;

import com.example.myapplication.models.RoomRequest;
import com.example.myapplication.models.RoomResponse;
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

public interface ApiRoomService {
    @GET("rooms/get_all")
    Call<List<RoomResponse>> getAllRooms(@Header("Authorization") String token);

    @GET("rooms/get/{room_id}")
    Call<RoomResponse> getRoomById(@Path("room_id") int roomId);

    @POST("rooms/create")
    Call<RoomResponse> createRoom(@Header("Authorization") String token, @Body RoomRequest roomRequest);

    @PUT("rooms/update/{room_id}")
    Call<RoomResponse> updateRoom(@Header("Authorization") String token, @Body RoomRequest roomRequest, @Path("room_id") int roomId);

    @DELETE("rooms/delete/{room_id}")
    Call<StatusMessage> deleteRoom(@Header("Authorization") String token, @Path("room_id") int roomId);

    // Cinema related endpoints
    @GET("rooms/by-cinema/{cinema_id}")
    Call<List<RoomResponse>> getRoomsByCinema(@Path("cinema_id") int cinemaId);

    @PUT("rooms/{room_id}/assign-cinema")
    Call<RoomResponse> assignRoomToCinema(
            @Header("Authorization") String token,
            @Path("room_id") int roomId,
            @Body AssignCinemaRequest request
    );

    @GET("rooms/get-with-cinema/{room_id}")
    Call<RoomResponse> getRoomWithCinema(@Path("room_id") int roomId);

    /**
     * Request body for assigning room to cinema
     */
    class AssignCinemaRequest {
        private int cinema_id;

        public AssignCinemaRequest(int cinemaId) {
            this.cinema_id = cinemaId;
        }

        public int getCinema_id() {
            return cinema_id;
        }
    }
}
