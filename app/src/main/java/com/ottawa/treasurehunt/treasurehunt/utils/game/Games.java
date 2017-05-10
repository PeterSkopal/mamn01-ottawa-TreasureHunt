package com.ottawa.treasurehunt.treasurehunt.utils.game;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Skopal on 10/05/17.
 */

public class Games {
    @SerializedName("games")
    private ArrayList<Game> games;

    public Games(ArrayList<Game> games) {
        this.games = games;
    }

    public ArrayList<Game> getGames() {
        return games;
    }
}
