package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;

public class ImageFirm {

    public ImageFirm(int id, String imageUrl) {
        this.id = id;
        this.imageUrl = imageUrl;
    }

    @SerializedName("ImageID")
    private int id;

    @SerializedName("ImageURL")
    private String imageUrl;

    public int getId() {
        return id;
    }
    public String getImageUrl() {
        return imageUrl;
    }


}
