package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;

public class StatusMessage {
    @SerializedName("message")
    private String message;

    public  String getMessage() {
        return message;
    }
}