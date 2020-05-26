package com.lujustin.hammrd.models;

import com.google.gson.annotations.SerializedName;

public class PlaceAutoSuggestion {

    @SerializedName("description")
    private String description;

    @SerializedName("place_id")
    private String placeId;

    public PlaceAutoSuggestion(String description, String placeId) {
        this.description = description;
        this.placeId = placeId;
    }

    public String getDescription() {
        return description;
    }

    public String getPlaceId() {
        return placeId;
    }
}
