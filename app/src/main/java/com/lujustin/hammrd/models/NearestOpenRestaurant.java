package com.lujustin.hammrd.models;

import com.google.gson.annotations.SerializedName;

public class NearestOpenRestaurant {
    private String name;

    @SerializedName("geometry")
    private Geometry geometry;

    public NearestOpenRestaurant(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return this.geometry.location.latitude;
    }

    public double getLongitude() {
        return this.geometry.location.longitude;
    }

    public String getName() {
        return name;
    }
}
