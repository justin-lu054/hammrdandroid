package com.lujustin.hammrd.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NearestOpenRestaurantList {
    @SerializedName("results")
    private List<NearestOpenRestaurant> results;

    public NearestOpenRestaurantList(List<NearestOpenRestaurant> results) {
        this.results = results;
    }
    public List<NearestOpenRestaurant> getList() {
        return results;
    }

}
