package com.ottawa.treasurehunt.treasurehunt.utils.game;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Skopal on 10/05/17.
 */

public class Minigame {
    @SerializedName("id")
    private int id;

    public Minigame(int id) {
        Log.i("Parser", "Minigame Id " + id);
        this.id = id;
    }

    public int getId() { return id; }
}
