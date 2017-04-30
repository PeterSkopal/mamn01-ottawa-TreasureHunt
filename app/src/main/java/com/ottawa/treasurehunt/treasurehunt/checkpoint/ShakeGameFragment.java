package com.ottawa.treasurehunt.treasurehunt.checkpoint;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.ottawa.treasurehunt.treasurehunt.MainActivity;
import com.ottawa.treasurehunt.treasurehunt.R;

public class ShakeGameFragment extends Fragment implements SensorEventListener{
    private static float ALPHA = 0.10f; // if ALPHA = 1 OR 0, no filter applies.

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float gravity[] = new float[3];
    private float linear_acceleration[] = new float[3];
    private float correctedAcceleraction[] = new float[3];
    private double hypothenuse;
    private boolean finished = false;

    private int DIFFICULTY = 200;

    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View myFragmentView = inflater.inflate(R.layout.fragment_shakegame,container,false);

        // Setup sensors
        sensorManager = (SensorManager)getActivity().getSystemService(getActivity().SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Setup progressbar
        progressBar = (ProgressBar)myFragmentView.findViewById(R.id.shakeProgress);
        progressBar.setMax(DIFFICULTY);

        return myFragmentView;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Correcting for gravity
        // alpha is calculated as t / (t + dT)
        // with t, the low-pass filter's time-constant
        // and dT, the event delivery rate

        final float alpha = 0.8f;

        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];

        lowPass(linear_acceleration, correctedAcceleraction);

        // Calculating vector in 3D space
        hypothenuse = Math.sqrt(Math.pow(correctedAcceleraction[0], 2) +
                Math.pow(correctedAcceleraction[1], 2) +
                Math.pow(correctedAcceleraction[2], 2));
        progressBar.setProgress(progressBar.getProgress()+(int)Math.round(hypothenuse));

        // Win condition
        if(progressBar.getProgress() >= DIFFICULTY && !finished){
            finished = true;
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setCancelable(false)
                    .setMessage("Mini Game finished!")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which){
                            Intent intent = new Intent(getActivity().getBaseContext(), MainActivity.class);
                            startActivity(intent);
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onResume(){
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    private  float[] lowPass(float[] input, float[] output){
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }
}
