package com.example.myapplication.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.List;

/*

{
    "name": "New Firm",
    "description": "This is a new firm.",
    "thumbnail": "http://example.com/thumbnail.jpg",
    "start_date": "2023-01-01",
    "end_date": "2023-12-31",
    "rating": 4.5,
    "rating_count": 100,
    "runtime": 100,
    "images": [
        "http://example.com/image1.jpg",
        "http://example.com/image2.jpg"
    ]
}

 */

public class FirmRequest{
    private String name;
    private String description;
    private String thumbnail;
    private String start_date;
    private String end_date;
    private double rating;
    private int rating_count;
    private int runtime;
    private List<String> images;

    public FirmRequest(String name, String description, String thumbnail, String start_date, String end_date, double rating, int rating_count, int runtime, List<String> images) {
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
        this.start_date = start_date;
        this.end_date = end_date;
        this.rating = rating;
        this.rating_count = rating_count;
        this.runtime = runtime;
        this.images = images;
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

    public String getStartDate() {
        return start_date;
    }

    public void setStartDate(String start_date) {
        this.start_date = start_date;
    }

    public String getEndDate() {
        return end_date;
    }

    public void setEndDate(String end_date) {
        this.end_date = end_date;
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

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }


}
