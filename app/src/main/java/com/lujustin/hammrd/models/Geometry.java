package com.lujustin.hammrd.models;

import com.google.gson.annotations.SerializedName;

public class Geometry {
    @SerializedName("location")
    public LocationResult location;

    class LocationResult {
        @SerializedName("lat")
        public double latitude;
        @SerializedName("lng")
        public double longitude;

    }
}
