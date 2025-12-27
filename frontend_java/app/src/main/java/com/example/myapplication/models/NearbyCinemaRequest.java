package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;

/**
 * Request model for getting nearby cinemas
 */
public class NearbyCinemaRequest {

    @SerializedName("lat")
    private double lat;

    @SerializedName("lng")
    private double lng;

    @SerializedName("max_distance")
    private Integer maxDistance; // in km, optional

    @SerializedName("use_actual_distance")
    private Boolean useActualDistance; // optional, default true

    public NearbyCinemaRequest(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public NearbyCinemaRequest(double lat, double lng, int maxDistance) {
        this.lat = lat;
        this.lng = lng;
        this.maxDistance = maxDistance;
    }

    public NearbyCinemaRequest(double lat, double lng, int maxDistance, boolean useActualDistance) {
        this.lat = lat;
        this.lng = lng;
        this.maxDistance = maxDistance;
        this.useActualDistance = useActualDistance;
    }

    // Getters and Setters
    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }

    public Integer getMaxDistance() { return maxDistance; }
    public void setMaxDistance(Integer maxDistance) { this.maxDistance = maxDistance; }

    public Boolean getUseActualDistance() { return useActualDistance; }
    public void setUseActualDistance(Boolean useActualDistance) { this.useActualDistance = useActualDistance; }
}

