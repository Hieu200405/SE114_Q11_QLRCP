package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;

public class BookingTicketRequest {
    @SerializedName("broadcast_id")
    int BroadcastId;
    @SerializedName("seat_id")
    int SeatId;
    public BookingTicketRequest(int broadcastId, int seatId) {
        this.BroadcastId = broadcastId;
        this.SeatId = seatId;
    }

    // Getters
    public int getBroadcastId() {
        return BroadcastId;
    }
    public int getSeatId() {
        return SeatId;
    }
    // Setters
    public void setBroadcastId(int broadcastId) {
        this.BroadcastId = broadcastId;
    }
    public void setSeatId(int seatId) {
        this.SeatId = seatId;
    }


}
