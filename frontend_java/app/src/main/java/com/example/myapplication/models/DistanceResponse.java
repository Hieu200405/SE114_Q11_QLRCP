package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Response model for distance calculation
 */
public class DistanceResponse {

    @SerializedName("origin")
    private Origin origin;

    @SerializedName("distances")
    private List<DistanceResult> distances;

    public Origin getOrigin() { return origin; }
    public List<DistanceResult> getDistances() { return distances; }

    public static class Origin {
        @SerializedName("lat")
        private double lat;

        @SerializedName("lng")
        private double lng;

        public double getLat() { return lat; }
        public double getLng() { return lng; }
    }

    public static class DistanceResult {
        @SerializedName("distance_text")
        private String distanceText;

        @SerializedName("distance_value")
        private int distanceValue; // in meters

        @SerializedName("duration_text")
        private String durationText;

        @SerializedName("duration_value")
        private int durationValue; // in seconds

        public String getDistanceText() { return distanceText; }
        public int getDistanceValue() { return distanceValue; }
        public String getDurationText() { return durationText; }
        public int getDurationValue() { return durationValue; }
    }
}

