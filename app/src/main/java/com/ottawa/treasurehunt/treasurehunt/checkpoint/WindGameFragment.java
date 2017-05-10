package com.ottawa.treasurehunt.treasurehunt.checkpoint;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.*;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ottawa.treasurehunt.treasurehunt.R;

// Based on http://androidexample.com/Detect_Noise_Or_Blow_Sound_-_Set_Sound_Frequency_Thersold/index.php?view=article_discription&aid=108&aaid=130

public class WindGameFragment extends Fragment{
    /* constants */
    private static final int POLL_INTERVAL = 150;
    private static float ALPHA = 0.35f; // if ALPHA = 1 OR 0, no filter applies.

    /** running state **/
    private boolean mRunning = false;

    /** config state **/
    private int mThreshold;

    private PowerManager.WakeLock mWakeLock;

    private Handler mHandler = new Handler();

    /* References to view elements */
    private TextView mStatusView;
    private ImageView featherView;

    /* data source */
    private SoundMeter soundSensor;

    private IResultCallback resCallback;

    private float output[] = new float[1];
    private float posY = 0;

    /****************** Define runnable thread again and again detect noise *********/


    private Runnable mSleepTask = new Runnable() {
        public void run() {
            //Log.i("Noise", "runnable mSleepTask");

            start();
        }
    };


    // Create runnable thread to Monitor Voice
    private Runnable mPollTask = new Runnable() {
        public void run() {

            double amp = soundSensor.getAmplitude();

            float input[] = {(float)amp};
            lowPass(input, output);
            //Log.i("Noise", "runnable mPollTask");
            updateDisplay("Monitoring Voice... " + String.valueOf(output[0]), output[0]);

            if ((output[0] > mThreshold)) {
                callForHelp();
                //Log.i("Noise", "==== onCreate ===");

            }

            // Runnable(mPollTask) will again execute after POLL_INTERVAL
            mHandler.postDelayed(mPollTask, POLL_INTERVAL);

        }
    };

    /*********************************************************/

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // Defined SoundLevelView in main.xml file
        //setContentView(R.layout.main);

        // Used to record voice
        soundSensor = new SoundMeter();

        PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "NoiseAlert");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myFragmentView = inflater.inflate(R.layout.fragment_wind_game,container,false);

        featherView = (ImageView) myFragmentView.findViewById(R.id.imageFeather);
        mStatusView = (TextView) myFragmentView.findViewById(R.id.status);

        return myFragmentView;
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
        onStop();
    }

    private void initializeApplicationConstants() {
        // Set Noise Threshold
        mThreshold = 8;

    }

    private void updateDisplay(String status, double signalEMA) {
        mStatusView.setText(status);
        //
        //mDisplay.setLevel((int)signalEMA, mThreshold);
    }


    private void callForHelp() {

        //stop();

        // Show alert when noise thersold crossed
        Toast.makeText(getContext().getApplicationContext(), "Noise Thersold Crossed, do here your stuff.",
                Toast.LENGTH_LONG).show();

        posY = featherView.getY()-40;
        featherView.setY(posY);

        if(featherView.getY() <= 10){
            stop();
            resCallback.callback(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.i("Noise", "==== onResume ===");

        initializeApplicationConstants();
        //mDisplay.setLevel(0, mThreshold);

        if (!mRunning) {
            mRunning = true;
            start();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Log.i("Noise", "==== onStop ===");

        //Stop noise monitoring
        stop();

    }

    private void start() {
        //Log.i("Noise", "==== start ===");

        soundSensor.start();
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }

        //Noise monitoring start
        // Runnable(mPollTask) will execute after POLL_INTERVAL
        mHandler.postDelayed(mPollTask, POLL_INTERVAL);
    }

    private void stop() {
        //Log.i("Noise", "==== Stop Noise Monitoring===");
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        mHandler.removeCallbacks(mSleepTask);
        mHandler.removeCallbacks(mPollTask);
        soundSensor.stop();
        //mDisplay.setLevel(0,0);
        updateDisplay("stopped...", 0.0);
        mRunning = false;

    }

    private  float[] lowPass(float[] input, float[] output){
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }
}
