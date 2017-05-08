package com.ottawa.treasurehunt.treasurehunt.utils.game;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Edvin Havic on 2017-04-28.
 */
public class Position {
    @SerializedName("long")
    private long lon;
    @SerializedName("lat")
    private long lat;

    public Position(long lon, long lat) {
        this.lon = lon;
        this.lat = lat;
    }

    public long getLatitude() {
        return lat;
    }

    public long getLongitude() {
        return lon;
    }
}
