package com.ottawa.treasurehunt.treasurehunt.checkpoint;

import android.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.*;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ottawa.treasurehunt.treasurehunt.R;

// Based on http://androidexample.com/Detect_Noise_Or_Blow_Sound_-_Set_Sound_Frequency_Thersold/index.php?view=article_discription&aid=108&aaid=130

public class WindGameFragment extends Fragment implements SensorEventListener{
    /* constants */
    private static final int POLL_INTERVAL = 300;

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
    private SensorManager sensorManager;

    /****************** Define runnable thread again and again detect noise *********/

    /**
    private Runnable mSleepTask = new Runnable() {
        public void run() {
            //Log.i("Noise", "runnable mSleepTask");

            start();
        }
    };
    */

    // Create runnable thread to Monitor Voice
    private Runnable mPollTask = new Runnable() {
        public void run() {

            double amp = soundSensor.getAmplitude();
            //Log.i("Noise", "runnable mPollTask");
            updateDisplay("Monitoring Voice... " + String.valueOf(amp), amp);

            if ((amp > mThreshold)) {
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
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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
        sensorManager.unregisterListener(this);
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
    }

    private class MyAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationEnd(Animation animation) {
            featherView.clearAnimation();
            ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(featherView.getWidth(), featherView.getHeight());
            lp.setMargins(50, 100, 0, 0);
            featherView.setLayoutParams(lp);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

    }
}
