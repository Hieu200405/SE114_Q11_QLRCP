package com.example.myapplication.models;


import com.google.gson.annotations.SerializedName;

/*
dữ liệu trả về

{
        "BroadcastID": 6,
        "DateOrder": "2025-06-16",
        "ID": 11,
        "IsDeleted": false,
        "Price": 100000.0,
        "SeatID": 34,
        "SeatName": "seat_4",
        "UserID": 4
    }

 */
public class Ticket {
    @SerializedName("BroadcastID")
    private int broadcastId;
    @SerializedName("DateOrder")
    private String dateOrder;
    @SerializedName("ID")
    private int id;
    @SerializedName("IsDeleted")
    private boolean isDeleted;
    @SerializedName("Price")
    private double price;
    @SerializedName("SeatID")
    private int seatId;
    @SerializedName("SeatName")
    private String seatName;
    @SerializedName("UserID")
    private int userId;

//    Getters
    public int getBroadcastId() {
        return broadcastId;
    }

    public String getDateOrder() {
        return dateOrder;
    }

    public int getId() {
        return id;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public double getPrice() {
        return price;
    }

    public int getSeatId() {
        return seatId;
    }

    public String getSeatName() {
        return seatName;
    }

    public int getUserId() {
        return userId;
    }

    // Setters
    public void setBroadcastId(int broadcastId) {
        this.broadcastId = broadcastId;
    }
    public void setDateOrder(String dateOrder) {
        this.dateOrder = dateOrder;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }
    public void setSeatName(String seatName) {
        this.seatName = seatName;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }

}
