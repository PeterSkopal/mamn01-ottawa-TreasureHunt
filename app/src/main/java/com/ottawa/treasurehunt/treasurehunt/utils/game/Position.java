package com.ottawa.treasurehunt.treasurehunt.utils.game;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Edvin Havic on 2017-04-28.
 */
public class Position {
    @SerializedName("lat")
    private double lat;
    @SerializedName("long")
    private double lon;

    public Position(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLatitude() {
        return lat;
    }

    public double getLongitude() {
        return lon;
    }
}
