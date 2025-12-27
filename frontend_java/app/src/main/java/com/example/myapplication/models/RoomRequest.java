package com.example.myapplication.models;


/*
    {
    "name": "Room 22",
    "seats": 20,
    "cinema_id": 1
}

 */
public class RoomRequest {
    private String name;
    private int seats;
    private Integer cinema_id;

    public RoomRequest(String name, int seats) {
        this.name = name;
        this.seats = seats;
    }

    public RoomRequest(String name, int seats, Integer cinemaId) {
        this.name = name;
        this.seats = seats;
        this.cinema_id = cinemaId;
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

    public Integer getCinema_id() {
        return cinema_id;
    }

    public void setCinema_id(Integer cinema_id) {
        this.cinema_id = cinema_id;
    }

    // Alias method
    public void setCinemaId(int cinemaId) {
        this.cinema_id = cinemaId;
    }
}
