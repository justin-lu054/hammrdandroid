package com.lujustin.hammrd.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NavDirectionsList {
    @SerializedName("routes")
    private List<NavDirections> directionsList;

    public NavDirectionsList(List<NavDirections> directionsList) {
        this.directionsList = directionsList;
    }

    public List<NavDirections> getDirectionsList() {
        return directionsList;
    }

}
