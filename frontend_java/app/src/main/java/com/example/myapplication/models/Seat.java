package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;

public class Seat {
    @SerializedName("BroadcastID")
    private int broadcastId;

    @SerializedName("ID")
    private int id;

    @SerializedName("IsBought")
    private boolean isBought;

    @SerializedName("RoomID")
    private int roomId;

    @SerializedName("Name")
    private String name;

    // NEW: Biến trạng thái người dùng có chọn ghế này không
    private boolean isSelected = false;

    public Seat(int broadcastId, int id, boolean isBought, int roomId, String name) {
        this.broadcastId = broadcastId;
        this.id = id;
        this.isBought = isBought;
        this.roomId = roomId;
        this.name = name;
    }

    // Getters
    public int getBroadcastId() {
        return broadcastId;
    }

    public int getId() {
        return id;
    }

    public boolean isBought() {
        return isBought;
    }

    public int getRoomId() {
        return roomId;
    }

    public String getName() {
        return name;
    }

    // NEW: Getter & Setter cho chọn ghế
    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

    // Setter nếu bạn muốn cập nhật trạng thái bought từ response mới
    public void setBought(boolean bought) {
        this.isBought = bought;
    }
}
