package com.ottawa.treasurehunt.treasurehunt.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ottawa.treasurehunt.treasurehunt.utils.game.Game;

/**
 * Created by Edvin Havic on 2017-04-28.
 */

public class Parser {
    private static Gson gson = new GsonBuilder().create();

    protected Parser() {
        // Implemented to prevent instantiation
    }

    public static Game generateGame(String json) {
        return gson.fromJson(json, Game.class);
    }
}
