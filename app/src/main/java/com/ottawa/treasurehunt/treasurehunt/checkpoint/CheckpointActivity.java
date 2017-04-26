package com.ottawa.treasurehunt.treasurehunt.checkpoint;

import android.app.Fragment;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.ottawa.treasurehunt.treasurehunt.R;

import java.util.HashMap;

public class CheckpointActivity extends FragmentActivity implements QuizFragment.OnFragmentInteractionListener {
    public static final String GAME_TYPE = "GAME_TYPE";

    public static final String MINIGAME = "GAME_MINIGAME";
    public static final String MINIGAME_ID = "GAME_MINIGAME_ID";

    public static final String QUIZ = "GAME_QUIZ";
    public static final String QUIZ_QUESTIONS = "GAME_QUIZ_QUESTION";
    public static final String QUIZ_ANSWERS = "GAME_QUIZ_ANSWERS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkpoint);

        Bundle bundle = getIntent().getExtras();

        Log.i("Checkpoint", "onCreate running");

        if (bundle == null || bundle.getString(GAME_TYPE) == null)
            throw new RuntimeException("CheckpointActivity: Invalid extras provided.");

        if (bundle.getString(GAME_TYPE).equals(MINIGAME)) {
            int id = bundle.getInt(MINIGAME_ID); // launch minigame with this id

            // example: setFragment(minigameFragments[id].newInstance());
        } else if (bundle.getString(GAME_TYPE).equals(QUIZ)) {
            String[] question = bundle.getStringArray(QUIZ_QUESTIONS);

            @SuppressWarnings("unchecked")
            HashMap<String, Boolean>[] answers =
                    (HashMap<String, Boolean>[]) bundle.getSerializable(QUIZ_ANSWERS);

            Log.i("Checkpoint", "Launching QuizFragment");

            setFragment(QuizFragment.newInstance(question[0], answers[0]));
        }
    }

    // http://stackoverflow.com/a/28208203
    protected void setFragment(Fragment fragment) {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.i("Checkpoint", "onFragmentInteraction URI: " + uri);
    }
}
