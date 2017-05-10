package com.ottawa.treasurehunt.treasurehunt.utils.game;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Edvin Havic on 2017-04-28.
 */
public class Question {
    @SerializedName("question")
    private String question;
    @SerializedName("answers")
    private ArrayList<Answer> answers;

    public Question(String question, ArrayList<Answer> answers) {
        this.answers = answers;
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }

    public ArrayList<Answer> getAnswers() {
        return answers;
    }

    public Answer getCorrectAnswer() {
        for (Answer a : answers) {
            if (a.isCorrect()) {
                return a;
            }
        }

        return null;
    }
}
