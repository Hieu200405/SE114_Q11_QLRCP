package com.example.myapplication.models;



/*
{
    "room_id": 1,
    "firm_id": 1,
    "time_broadcast": "10:00:00",
    "date_broadcast": "2025-10-10",
    "price": 100000,
    "seats": 15
}

 */
public class BroadcastFirmRequest {
    private int room_id;
    private int firm_id;
    private String time_broadcast;
    private String date_broadcast;
    private double price;
    private int seats;

    public BroadcastFirmRequest(int room_id, int firm_id, String time_broadcast, String date_broadcast, double price, int seats) {
        this.room_id = room_id;
        this.firm_id = firm_id;
        this.time_broadcast = time_broadcast;
        this.date_broadcast = date_broadcast;
        this.price = price;
        this.seats = seats;
    }

    public int getRoomId() {
        return room_id;
    }

    public int getFirmId() {
        return firm_id;
    }

    public String getTimeBroadcast() {
        return time_broadcast;
    }

    public String getDateBroadcast() {
        return date_broadcast;
    }

    public double getPrice() {
        return price;
    }

    public int getSeats() {
        return seats;
    }

}
