package com.lujustin.hammrd.models;

import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class PlaceAutoSuggestionList {
    @SerializedName("predictions")
    private ArrayList<PlaceAutoSuggestion> suggestionList;

    public ArrayList<PlaceAutoSuggestion> getSuggestionList() {
        return suggestionList;
    }

    public ArrayList<String> getDescriptionList() {
        ArrayList<String> descriptionList = new ArrayList<>();
        for (PlaceAutoSuggestion s : suggestionList) {
            descriptionList.add(s.getDescription());
        }
        return descriptionList;
    }

    public PlaceAutoSuggestionList(ArrayList<PlaceAutoSuggestion> suggestionList) {
        this.suggestionList = suggestionList;
    }

}
