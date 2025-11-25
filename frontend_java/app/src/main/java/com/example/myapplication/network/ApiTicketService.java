package com.example.myapplication.network;

import com.example.myapplication.models.BookingTicketRequest;
import com.example.myapplication.models.BookingTicketResponse;
import com.example.myapplication.models.Ticket;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiTicketService {
    @POST("tickets/create")
    Call<BookingTicketResponse> createTicket(@Header("Authorization") String token,
                                             @Body BookingTicketRequest ticketRequest);

    @GET("tickets/get_by_user/{userId}")
    Call<List<Ticket>> getTicketsBookedByUser(@Header("Authorization") String token,
                                              @Path("userId") String userId);


    @GET("tickets/get_detail/{ticketId}")
    Call<BookingTicketResponse> getTicketDetail(@Header("Authorization") String token,
                                              @Path("ticketId") String ticketId);



    @GET("tickets/get_all")
    Call<List<Ticket>> getAllTickets(@Header("Authorization") String token);


    @GET("tickets/get_by_seat/{seatId}")
    Call<BookingTicketResponse> getTicketBySeat(@Header("Authorization") String token,
                                              @Path("seatId") String seatId);

    @DELETE("tickets/delete/{ticketId}")
    Call<String> deleteTicket(@Header("Authorization") String token,
                            @Path("ticketId") String ticketId);




}
