package com.ottawa.treasurehunt.treasurehunt;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;

public class ShakeGameActivity extends AppCompatActivity implements SensorEventListener{
    private static float ALPHA = 0.10f; // if ALPHA = 1 OR 0, no filter applies.

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float gravity[] = new float[3];
    private float linear_acceleration[] = new float[3];
    private float correctedAcceleraction[] = new float[3];
    private double hypothenuse;

    private int DIFFICULTY = 1000;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shake_game);
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        progressBar = (ProgressBar)findViewById(R.id.shakeProgress);
        progressBar.setMax(DIFFICULTY);
    }


    protected void onResume(){
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
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

        hypothenuse = Math.sqrt(Math.pow(correctedAcceleraction[0], 2) +
                                Math.pow(correctedAcceleraction[1], 2) +
                                Math.pow(correctedAcceleraction[2], 2));
        progressBar.setProgress(progressBar.getProgress()+(int)Math.round(hypothenuse));
        if(progressBar.getProgress() >= DIFFICULTY){
            progressBar.setBackgroundColor(0xFF00FF00);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private  float[] lowPass(float[] input, float[] output){
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }
}
