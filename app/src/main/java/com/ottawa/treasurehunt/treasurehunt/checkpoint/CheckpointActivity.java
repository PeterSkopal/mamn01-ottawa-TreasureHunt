package com.ottawa.treasurehunt.treasurehunt.checkpoint;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.ottawa.treasurehunt.treasurehunt.Play;
import com.ottawa.treasurehunt.treasurehunt.R;
import com.ottawa.treasurehunt.treasurehunt.utils.game.Game;
import com.ottawa.treasurehunt.treasurehunt.utils.game.Position;

import java.security.InvalidParameterException;
import java.util.HashMap;

public class CheckpointActivity extends FragmentActivity implements IResultCallback {
    public static final String GAME_TYPE = "GAME_TYPE";

    public static final String MINIGAME = "GAME_MINIGAME";
    public static final String MINIGAME_ID = "GAME_MINIGAME_ID";
    public static final int MINIGAME_SHAKE = 0;
    public static final int MINIGAME_FEATHER = 1;

    public static final String QUIZ = "GAME_QUIZ";
    public static final String QUIZ_QUESTIONS = "GAME_QUIZ_QUESTION";
    public static final String QUIZ_ANSWERS = "GAME_QUIZ_ANSWERS";

    private String gameType;
    private Play play;

    Fragment[] quizFragments;
    int currentQuiz;
    int currentPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkpoint);

        Bundle bundle = getIntent().getExtras();

        Log.i("Checkpoint", "onCreate running");

        if (bundle == null || bundle.getString(GAME_TYPE) == null)
            throw new RuntimeException("CheckpointActivity: Invalid extras provided.");

        gameType = bundle.getString(GAME_TYPE);

        if (bundle.getString(GAME_TYPE).equals(MINIGAME)) {
            int id = bundle.getInt(MINIGAME_ID); // launch minigame with this id
            Fragment miniGameFragment;

            switch (id){
                case MINIGAME_SHAKE:
                    miniGameFragment = new ShakeGameFragment();
                    break;
                case MINIGAME_FEATHER:
                    miniGameFragment = new WindGameFragment();
                    break;
                default:
                    throw new InvalidParameterException("");
            }


            setFragment(miniGameFragment);

            // example: setFragment(minigameFragments[id].newInstance());
        } else if (bundle.getString(GAME_TYPE).equals(QUIZ)) {
            String[] question = bundle.getStringArray(QUIZ_QUESTIONS);

            @SuppressWarnings("unchecked")
            HashMap<String, Boolean>[] answers =
                    (HashMap<String, Boolean>[]) bundle.getSerializable(QUIZ_ANSWERS);

            Log.i("Checkpoint", "Launching QuizFragment");
            Log.i("Checkpoint", "Question length: " + question.length);
            Log.i("Checkpoint", "Answers length: " + answers.length);

            if (question.length != answers.length)
                throw new RuntimeException("CheckpointActivity: Must provide answers for all questions.");

            quizFragments = new Fragment[question.length];

            for (int i = 0; i < question.length; i++) {
                quizFragments[i] = QuizFragment.newInstance(
                        question[i], answers[i], i + 1, question.length);
            }

            setFragment(quizFragments[0]);
            currentQuiz = 0;
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
    public void callback(boolean answeredCorrect) {
        currentQuiz++;

        if (answeredCorrect)
            currentPoints++; // placeholder

        Log.i("Checkpoint", "call running CheckpointActivity::IResultCallback");

        if (gameType.equals(QUIZ) && currentQuiz < quizFragments.length) {
            setFragment(ResultSplash.newInstance(answeredCorrect));

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setFragment(quizFragments[currentQuiz]);
                }
            }, 2000);
        } else {
            // no more quiz fragments or we're a mini-game
            // collect and store the points
            // return to Play activity

            setFragment(ResultSplash.newInstance(answeredCorrect));

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Play.nextCheckpoint();
                    startActivity(
                            new Intent(getBaseContext(), Play.class));
                    finish();
                }
            }, 2000);
        }
    }
}
