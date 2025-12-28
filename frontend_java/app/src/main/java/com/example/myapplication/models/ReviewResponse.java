package com.example.myapplication.models;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ReviewResponse {
    @SerializedName("code")
    private String code;

    @SerializedName("desc")
    private String desc;

    @SerializedName("data")
    private List<Review> data; // Danh sách các Review

    public String getCode() { return code; }
    public String getDesc() { return desc; }
    public List<Review> getData() { return data; }
}