package com.ottawa.treasurehunt.treasurehunt.utils.game;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Edvin Havic on 2017-04-28.
 */

public class Game {
    @SerializedName("name")
    private String name;
    @SerializedName("description")
    private String desc;
    @SerializedName("minigame")
    private int minigameId;
    @SerializedName("checkpoints")
    private ArrayList<Checkpoint> checkpoints;
    @SerializedName("position")
    private Position pos;

    public Game(String name, String desc, ArrayList<Checkpoint> checkpoints, Position pos, int minigameId) {
        this.name = name;
        this.desc = desc;
        this.checkpoints = checkpoints;
        this.pos = pos;
        this.minigameId = minigameId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return desc;
    }

    public int getMinigameId() {
        return minigameId;
    }

    public ArrayList<Checkpoint> getCheckpoints() {
        return checkpoints;
    }

    public Position getPosition() {
        return pos;
    }
}

