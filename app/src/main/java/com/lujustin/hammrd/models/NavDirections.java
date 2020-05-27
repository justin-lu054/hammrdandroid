package com.lujustin.hammrd.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;
import com.google.maps.android.PolyUtil;

import java.util.List;

public class NavDirections {
    @SerializedName("overview_polyline")
    private OverviewPolyline overviewPolyine;

    class OverviewPolyline {
        @SerializedName("points")
        private String polylineString;

        public OverviewPolyline(String polylineString) {
            this.polylineString = polylineString;
        }
        public String getPolylineString() {
            return polylineString;
        }
    }

    public NavDirections(OverviewPolyline overviewPolyine) {
        this.overviewPolyine = overviewPolyine;
    }

    public List<LatLng> getDirectionLatLngs() {
        return PolyUtil.decode(overviewPolyine.getPolylineString());
    }

}
