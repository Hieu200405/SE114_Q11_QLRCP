package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;

/**
 * Model for Map configuration response
 */
public class MapConfig {

    @SerializedName("mapKey")
    private String mapKey;

    @SerializedName("mapStyle")
    private String mapStyle;

    public String getMapKey() { return mapKey; }
    public String getMapStyle() { return mapStyle; }

    public void setMapKey(String mapKey) { this.mapKey = mapKey; }
    public void setMapStyle(String mapStyle) { this.mapStyle = mapStyle; }
}

