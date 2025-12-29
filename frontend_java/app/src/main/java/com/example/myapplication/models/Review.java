package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Review implements Serializable {
    @SerializedName("ID")
    private int id;

    @SerializedName("UserID")
    private int userId;

    @SerializedName("FilmID")
    private int filmId;

    @SerializedName("UserName")
    private String userName; // Tên người dùng để hiển thị trên UI

    @SerializedName("Rating")
    private int rating;

    @SerializedName("Comment")
    private String comment;

    @SerializedName("CreatedAt")
    private String createdAt;

    @SerializedName("Status")
    private int status;

    // Constructor mặc định (cần thiết cho GSON)
    public Review() {
    }

    // Getter và Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getFilmId() {
        return filmId;
    }

    public void setFilmId(int filmId) {
        this.filmId = filmId;
    }

    public String getUserName() {
        return userName != null ? userName : "Người dùng ẩn danh";
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
