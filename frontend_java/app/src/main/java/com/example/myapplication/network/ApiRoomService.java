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

    @POST("rooms/create")
    Call<RoomResponse> createRoom(@Header("Authorization") String token, @Body RoomRequest roomRequest);

    @PUT("rooms/update/{room_id}")
    Call<RoomResponse> updateRoom(@Header("Authorization") String token, @Body RoomRequest roomRequest, @Path("room_id") int roomId);

    @DELETE("rooms/delete/{room_id}")
    Call<StatusMessage> deleteRoom(@Header("Authorization") String token, @Path("room_id") int roomId);
}
