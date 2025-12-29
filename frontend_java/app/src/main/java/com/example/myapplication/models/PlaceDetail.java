package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;

/**
 * Model for Goong Place Detail response
 */
public class PlaceDetail {

    @SerializedName("result")
    private Result result;

    @SerializedName("status")
    private String status;

    public Result getResult() { return result; }
    public String getStatus() { return status; }

    public static class Result {
        @SerializedName("place_id")
        private String placeId;

        @SerializedName("name")
        private String name;

        @SerializedName("formatted_address")
        private String formattedAddress;

        @SerializedName("geometry")
        private Geometry geometry;

        public String getPlaceId() { return placeId; }
        public String getName() { return name; }
        public String getFormattedAddress() { return formattedAddress; }
        public Geometry getGeometry() { return geometry; }

        public double getLatitude() {
            return geometry != null && geometry.getLocation() != null
                ? geometry.getLocation().getLat() : 0;
        }

        public double getLongitude() {
            return geometry != null && geometry.getLocation() != null
                ? geometry.getLocation().getLng() : 0;
        }
    }

    public static class Geometry {
        @SerializedName("location")
        private Location location;

        public Location getLocation() { return location; }
    }

    public static class Location {
        @SerializedName("lat")
        private double lat;

        @SerializedName("lng")
        private double lng;

        public double getLat() { return lat; }
        public double getLng() { return lng; }
    }
}

