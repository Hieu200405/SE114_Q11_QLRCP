package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;

public class ReviewRequest {
    @SerializedName("FilmID")
    private int filmId;

    @SerializedName("Rating")
    private int rating;

    @SerializedName("Comment")
    private String comment;

    public ReviewRequest(int filmId, int rating, String comment) {
        this.filmId = filmId;
        this.rating = rating;
        this.comment = comment;
    }
}