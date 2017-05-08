package com.ottawa.treasurehunt.treasurehunt.utils.game;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Edvin Havic on 2017-04-28.
 */
public class Quiz {
    @SerializedName("questions")
    private ArrayList<Question> questions;

    public Quiz(ArrayList<Question> questions) {
        this.questions = questions;
    }

    public ArrayList<Question> getQuestions() {
        return questions;
    }
}
