package com.ottawa.treasurehunt.treasurehunt.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ottawa.treasurehunt.treasurehunt.utils.game.Game;

import java.util.ArrayList;

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

    public static ArrayList<Game> generateGames(String json) { return gson.fromJson(json, new TypeToken<ArrayList<Game>>(){}.getType()); }
}
