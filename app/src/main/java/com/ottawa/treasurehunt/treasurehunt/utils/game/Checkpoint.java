package com.ottawa.treasurehunt.treasurehunt.utils.game;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Edvin Havic on 2017-04-28.
 */

public class Checkpoint {
    @SerializedName("position")
    private Position pos;
    @SerializedName("quiz")
    private ArrayList<Question> quiz;

    public Checkpoint (Position pos, ArrayList<Question> quiz) {
        this.pos = pos;
        this.quiz = quiz;
    }

    public Position getPos () {
        return pos;
    }

    public ArrayList<Question> getQuiz () {
        return quiz;
    }

}
