package com.ottawa.treasurehunt.treasurehunt.checkpoint;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ottawa.treasurehunt.treasurehunt.R;

import java.util.HashMap;
import java.util.Map;

public class QuizFragment extends Fragment implements SensorEventListener {
    private static final String QUESTION = "QUESTION";
    private static final String ANSWERS = "ANSWERS";
    private static final String QUIZ_NUM = "QUIZ_NUM";
    private static final String QUIZ_TOTAL = "QUIZ_TOTAL";

    private static final int PROGRESS_TIMER = 600; // = PROGRESS_TIMER * 10ms = 6000ms

    private static final float LOWPASS_ALPHA = 0.1f;
    private static final float TILT_THRESHOLD = 5;
    private static final long ACCEPT_CHOICE_TIME = 1500;
    private static final long ACCEPT_CHOICE_VIBRATION_TIME = 50;

    private static final int BUTTON_COLOR_GRADIENT = Color.rgb(50, 50, 50);

    private SensorManager sensorManager;
    private Sensor sensorAccel;
    private Sensor sensorMagneto;
    private Vibrator vibrator;
    private float[] arrAccel = new float[3];
    private float[] arrMagento = new float[3];
    private float[] matRotation = new float[9];
    private float[] vecOrientation = new float[3];
    private float pitch;
    private float roll;
    private boolean hasAccel;
    private boolean hasMagneto;
    private long startTime;
    private int currentChoiceBtn;
    private short gradientNormalizer = 255;

    private String question;
    private HashMap<String, Boolean> answers;
    private int quizNumber;
    private int quizNumberTotal;

    private IResultCallback resCallback;

    TextView txtQuizProgress;
    ProgressBar progressTimeLeft;
    Handler progressTimeHandler;
    Runnable progressTimeRunnable;
    TextView txtQuestion;
    Button[] btns;
    ProgressBar[] progg;

    View.OnClickListener onAnswerClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            progressTimeHandler.removeCallbacks(progressTimeRunnable);
            if (answers.get(((Button) v).getText()))
                resCallback.callback(true); // answer was correct
            else
                resCallback.callback(false);
        }
    };


    public QuizFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param question Question to be answered
     * @param answers  4 answers with one or multiple true answers
     * @return A new instance of fragment QuizFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static QuizFragment newInstance(String question,
                                           HashMap<String, Boolean> answers,
                                           int quizNumber,
                                           int quizNumberTotal) {
        QuizFragment fragment = new QuizFragment();
        Bundle args = new Bundle();
        args.putString(QUESTION, question);
        args.putSerializable(ANSWERS, answers);
        args.putInt(QUIZ_NUM, quizNumber);
        args.putInt(QUIZ_TOTAL, quizNumberTotal);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            question = getArguments().getString(QUESTION);
            answers = (HashMap<String, Boolean>) getArguments().getSerializable(ANSWERS);
            quizNumber = getArguments().getInt(QUIZ_NUM);
            quizNumberTotal = getArguments().getInt(QUIZ_TOTAL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);

        //txtQuizProgress = (TextView) view.findViewById(R.id.txtQuizProgress);
        progressTimeLeft = (ProgressBar) view.findViewById(R.id.progressTimeLeft);
        txtQuestion = (TextView) view.findViewById(R.id.txtQuestion);
        btns = new Button[]{
                (Button) view.findViewById(R.id.btnAns1),
                (Button) view.findViewById(R.id.btnAns2),
                (Button) view.findViewById(R.id.btnAns3),
                (Button) view.findViewById(R.id.btnAns4)
        };

        progg = new ProgressBar[]{
                (ProgressBar) view.findViewById(R.id.proggAns1),
                (ProgressBar) view.findViewById(R.id.proggAns2),
                (ProgressBar) view.findViewById(R.id.proggAns3),
                (ProgressBar) view.findViewById(R.id.proggAns4)
        };

        for(ProgressBar progress : progg){
            progress.setMax(100);
        }

        // Makes left progressbars grow towards screen edge
        progg[0].setRotation(180);
        progg[2].setRotation(180);



        //txtQuizProgress.setText(String.format("%d / %d", quizNumber, quizNumberTotal));
        txtQuestion.setText(question);

        int i = 0;
        for (Map.Entry<String, Boolean> entry : answers.entrySet()) {
            btns[i++].setText(entry.getKey());
        }

        progressTimeLeft.setMax(PROGRESS_TIMER);
        progressTimeLeft.setScaleY(3f);

        progressTimeHandler = new Handler();
        progressTimeRunnable = new Runnable() { // Progress timer, if progress = 100 then start next quiz
            int count = 0;

            @Override
            public void run() {
                if (count <= PROGRESS_TIMER) {
                    progressTimeLeft.setProgress(count++);
                    short normalizedProgress = (short) (gradientNormalizer*((float)count/PROGRESS_TIMER));
                    progressTimeLeft.setProgressTintList(ColorStateList.valueOf(
                            Color.argb(normalizedProgress, normalizedProgress, (255-normalizedProgress), 0)));
                    progressTimeHandler.postDelayed(this, 10);
                } else {
                    progressTimeHandler.removeCallbacks(this);

                    if (currentChoiceBtn > -1)
                        chooseButton(currentChoiceBtn);
                    else
                        resCallback.callback(false);
                }
            }
        };

        progressTimeRunnable.run();

        sensorManager = (SensorManager) getActivity().getSystemService(
                getActivity().SENSOR_SERVICE);

        sensorAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagneto = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        currentChoiceBtn = -1; // default

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof IResultCallback) {
            resCallback = (IResultCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement IResultCallback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        resCallback = null;
        progressTimeHandler.removeCallbacks(progressTimeRunnable);
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorAccel, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensorMagneto, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == sensorAccel) {
            arrAccel = lowPass(event.values.clone(), arrAccel);
            hasAccel = true;
        }

        if (event.sensor == sensorMagneto) {
            arrMagento = lowPass(event.values.clone(), arrMagento);
            hasMagneto = true;
        }

        if (hasAccel && hasMagneto) {
            SensorManager.getRotationMatrix(matRotation, null, arrAccel, arrMagento);
            SensorManager.getOrientation(matRotation, vecOrientation);

            pitch = vecOrientation[1] * 180f / (float) Math.PI;
            roll = vecOrientation[2] * 180f / (float) Math.PI;

            for (Button btn : btns) {
                btn.setBackgroundColor(BUTTON_COLOR_GRADIENT);
            }

            //for (ProgressBar progress : progg){
            //    progress.setProgress(50);
            //}

            if (pitch > TILT_THRESHOLD && roll < -TILT_THRESHOLD) { // TOP LEFT
                if (currentChoiceBtn != 0) {
                    vibrator.vibrate(ACCEPT_CHOICE_VIBRATION_TIME);
                    startTime = System.currentTimeMillis();
                    resetProgress();
                }

                currentChoiceBtn = 0;
                double progress = ((System.currentTimeMillis()-startTime)/(double)ACCEPT_CHOICE_TIME)*100;
                progg[0].setProgress((int)progress);
                btns[0].setBackgroundColor(Color.rgb(91,172,223));
            } else if (pitch > TILT_THRESHOLD && roll > TILT_THRESHOLD) { // TOP RIGHT
                if (currentChoiceBtn != 1) {
                    vibrator.vibrate(ACCEPT_CHOICE_VIBRATION_TIME);
                    startTime = System.currentTimeMillis();
                    resetProgress();
                }

                currentChoiceBtn = 1;
                double progress = ((System.currentTimeMillis()-startTime)/(double)ACCEPT_CHOICE_TIME)*100;
                progg[1].setProgress((int)progress);
                btns[1].setBackgroundColor(Color.rgb(91,172,223));
            } else if (pitch < -TILT_THRESHOLD && roll < -TILT_THRESHOLD) { // BOTTOM LEFT
                if (currentChoiceBtn != 2) {
                    vibrator.vibrate(ACCEPT_CHOICE_VIBRATION_TIME);
                    startTime = System.currentTimeMillis();
                    resetProgress();
                }

                currentChoiceBtn = 2;
                double progress = ((System.currentTimeMillis()-startTime)/(double)ACCEPT_CHOICE_TIME)*100;
                progg[2].setProgress((int)progress);
                btns[2].setBackgroundColor(Color.rgb(91,172,223));
            } else if (pitch < -TILT_THRESHOLD && roll > TILT_THRESHOLD) { // BOTTOM RIGHT
                if (currentChoiceBtn != 3) {
                    vibrator.vibrate(ACCEPT_CHOICE_VIBRATION_TIME);
                    startTime = System.currentTimeMillis();
                    resetProgress();
                }

                currentChoiceBtn = 3;
                double progress = ((System.currentTimeMillis()-startTime)/(double)ACCEPT_CHOICE_TIME)*100;
                progg[3].setProgress((int)progress);
                btns[3].setBackgroundColor(Color.rgb(91,172,223));
            } else {
                currentChoiceBtn = -1;
                resetProgress();
            }

            if (currentChoiceBtn > -1 &&
                    System.currentTimeMillis() - startTime > ACCEPT_CHOICE_TIME) {

                sensorManager.unregisterListener(this);
                progressTimeHandler.removeCallbacks(progressTimeRunnable);

                chooseButton(currentChoiceBtn);
            }
        }
    }

    private void chooseButton(int num) {
        if (answers.get(btns[currentChoiceBtn].getText()))
            resCallback.callback(true); // answer was correct
        else
            resCallback.callback(false);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    private float[] lowPass(float[] input, float[] output) {
        if (output == null) return input;

        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + LOWPASS_ALPHA * (input[i] - output[i]);
        }

        return output;
    }

    private void resetProgress (){
        for(ProgressBar progress : progg){
            progress.setProgress(0);
        }
    }
}
