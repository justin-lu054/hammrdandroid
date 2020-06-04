package com.lujustin.hammrd.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class GeocodeResultList {
    @SerializedName("results")
    private ArrayList<GeocodeResult> results;

    public GeocodeResultList(ArrayList<GeocodeResult> results) {
        this.results = results;
    }

    public ArrayList<GeocodeResult> getResults() {
        return results;
    }


}
