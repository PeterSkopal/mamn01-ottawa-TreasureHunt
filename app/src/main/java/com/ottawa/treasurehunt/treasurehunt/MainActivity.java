package com.ottawa.treasurehunt.treasurehunt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.ottawa.treasurehunt.treasurehunt.checkpoint.CheckpointActivity;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void onMapsClick (View v) {
        Intent i = new Intent(this, ShakeGameActivity.class);
        this.startActivity(i);
    }

    public void onPlayClick (View v) {
        Intent i = new Intent(this, Play.class);
        this.startActivity(i);
    }

    public void onCheckpointClick(View v) {
        Intent i = new Intent(this, CheckpointActivity.class);
        i.putExtra(CheckpointActivity.GAME_TYPE, CheckpointActivity.QUIZ);

        String[] questions = new String[]{ "How tall is the Turning Torso? " };

        i.putExtra(CheckpointActivity.QUIZ_QUESTIONS, questions);

        HashMap<String, Boolean> firstQAnswers = new HashMap<>();
        firstQAnswers.put("152m", false);
        firstQAnswers.put("212m", false);
        firstQAnswers.put("173m", false);
        firstQAnswers.put("190m", true);

        @SuppressWarnings("unchecked")
        HashMap<String, Boolean>[] answers = new HashMap[]{ firstQAnswers };

        i.putExtra(CheckpointActivity.QUIZ_ANSWERS, answers);

        this.startActivity(i);
    }

    public void onMiniClick (View v) {
        Intent i = new Intent(this, ShakeGameActivity.class);
        this.startActivity(i);
    }
}
