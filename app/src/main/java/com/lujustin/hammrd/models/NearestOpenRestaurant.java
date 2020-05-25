package com.lujustin.hammrd.models;

import com.google.gson.annotations.SerializedName;

public class NearestOpenRestaurant {
    private String name;

    @SerializedName("geometry")
    private Geometry geometry;

    class Geometry {
        @SerializedName("location")
        private RestaurantLocation restaurantLocation;

        public RestaurantLocation getRestaurantLocation() {
            return restaurantLocation;
        }
    }

    class RestaurantLocation {
        @SerializedName("lat")
        private double latitude;
        @SerializedName("lng")
        private double longitude;

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }

    public NearestOpenRestaurant(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return this.geometry.restaurantLocation.getLatitude();
    }

    public double getLongitude() {
        return this.geometry.restaurantLocation.getLongitude();
    }

    public String getName() {
        return name;
    }
}
