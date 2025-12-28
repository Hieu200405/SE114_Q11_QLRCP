package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Request model for calculating distance from user location to multiple destinations
 */
public class DistanceRequest {

    @SerializedName("origin_lat")
    private double originLat;

    @SerializedName("origin_lng")
    private double originLng;

    @SerializedName("destinations")
    private List<Destination> destinations;

    public DistanceRequest(double originLat, double originLng, List<Destination> destinations) {
        this.originLat = originLat;
        this.originLng = originLng;
        this.destinations = destinations;
    }

    // Getters and Setters
    public double getOriginLat() { return originLat; }
    public void setOriginLat(double originLat) { this.originLat = originLat; }

    public double getOriginLng() { return originLng; }
    public void setOriginLng(double originLng) { this.originLng = originLng; }

    public List<Destination> getDestinations() { return destinations; }
    public void setDestinations(List<Destination> destinations) { this.destinations = destinations; }

    /**
     * Inner class for destination coordinate
     */
    public static class Destination {
        @SerializedName("lat")
        private double lat;

        @SerializedName("lng")
        private double lng;

        public Destination(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }

        public double getLat() { return lat; }
        public void setLat(double lat) { this.lat = lat; }

        public double getLng() { return lng; }
        public void setLng(double lng) { this.lng = lng; }
    }
}

