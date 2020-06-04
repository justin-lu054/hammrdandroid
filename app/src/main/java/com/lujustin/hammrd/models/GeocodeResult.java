package com.lujustin.hammrd.models;

import com.google.gson.annotations.SerializedName;

public class GeocodeResult {
    @SerializedName("geometry")
    private Geometry geometry;

    public GeocodeResult(Geometry geometry) {
        this.geometry = geometry;
    }

    public double getLatitude() {
        return this.geometry.location.latitude;
    }

    public double getLongitude() {
        return this.geometry.location.longitude;
    }
}
