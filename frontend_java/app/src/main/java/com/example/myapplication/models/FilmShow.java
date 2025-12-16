package com.example.myapplication.models;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

    public class FirmShow implements Serializable {

        @SerializedName("ID")
        private int id;

        @SerializedName("Name")
        private String name;

        @SerializedName("StartDate")
        private String startDate;

        @SerializedName("EndDate")
        private String endDate;

        @SerializedName("Rating")
        private Double rating;

        @SerializedName("RatingCount")
        private Integer ratingCount;

        @SerializedName("ThumbnailPath")
        private String thumbnailPath;



    // Getters

        public int getId() {
            return id;
        }


        public String getName() {
            return name;
        }


        public String getStartDate() {
            return startDate;
        }


        public String getEndDate() {
            return endDate;
        }


        public Double getRating() {
            return rating;
        }


        public Integer getRatingCount() {
            return ratingCount;
        }

        public String getThumbnailPath() {
            return thumbnailPath;
        }


    //    Setters

        public void setId(int id) {
            this.id = id;
        }
        public void setName(String name) {
            this.name = name;
        }
        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public void setRating(Double rating) {
            this.rating = rating;
        }
        public void setRatingCount(Integer ratingCount) {
            this.ratingCount = ratingCount;
        }
        public void setThumbnailPath(String thumbnailPath) {
            this.thumbnailPath = thumbnailPath;
        }



    }
