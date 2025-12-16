package com.example.myapplication.models;



/*
{
    "name": "Updated Firm",
    "description": "This is an updated firm.",
    "thumbnail": "http://example.com/updated_thumbnail.jpg",
    "rating": 4.8,
    "rating_count": 150,
    "runtime": 110
}
 */
public class FirmUpdateRequest {
    private String name;
    private String description;
    private String thumbnail;
    private double rating;
    private int rating_count;
    private int runtime;

    public FirmUpdateRequest(String name, String description, String thumbnail, double rating, int rating_count, int runtime) {
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
        this.rating = rating;
        this.rating_count = rating_count;
        this.runtime = runtime;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getRatingCount() {
        return rating_count;
    }

    public void setRatingCount(int rating_count) {
        this.rating_count = rating_count;
    }

    public int getRuntime() {
        return runtime;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }
}
