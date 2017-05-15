package com.ottawa.treasurehunt.treasurehunt.utils.game;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Edvin Havic on 2017-04-28.
 */

public class Checkpoint {
    @SerializedName("position")
    private Position pos;
    @SerializedName("quizzes")
    private ArrayList<Question> quiz;
    @SerializedName("minigame")
    private int minigame;

    public Checkpoint (Position pos, ArrayList<Question> quiz , int minigame) {
        this.pos = pos;
        this.quiz = quiz;
        this.minigame = minigame;
    }

    public Position getPos () {
        return pos;
    }

    public ArrayList<Question> getQuiz () { return quiz; }

    public int getMinigameId () { return minigame; }
}
