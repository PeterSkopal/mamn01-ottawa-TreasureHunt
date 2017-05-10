package com.ottawa.treasurehunt.treasurehunt.utils.game;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Edvin Havic on 2017-04-28.
 */

public class Game {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("description")
    private String desc;
    @SerializedName("checkpoints")
    private ArrayList<Checkpoint> checkpoints;
    @SerializedName("position")
    private static Position pos;

    public Game(int id, String name, String desc, ArrayList<Checkpoint> checkpoints, Position pos) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.checkpoints = checkpoints;
        this.pos = pos;
    }

    public int getId() { return id; }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return desc;
    }

    public ArrayList<Checkpoint> getCheckpoints() {
        return checkpoints;
    }

    public static Position getPosition() {
        return pos;
    }
}

