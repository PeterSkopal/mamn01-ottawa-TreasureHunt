package com.ottawa.treasurehunt.treasurehunt.utils.game;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Edvin Havic on 2017-04-28.
 */
public class Answer {
    @SerializedName("answer")
    private String answer;
    @SerializedName("correct")
    private boolean isCorrect;

    public Answer(String answer, boolean isCorrect) {
        this.answer = answer;
        this.isCorrect = isCorrect;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public String getAnswer() {
        return answer;
    }
}
