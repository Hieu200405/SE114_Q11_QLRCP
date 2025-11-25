package com.example.myapplication.models;


/*
    {
    "name": "Room 22",
    "seats": 20
}

 */
public class RoomRequest {
    private String name;
    private int seats;

    public RoomRequest(String name, int seats) {
        this.name = name;
        this.seats = seats;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }
}
