package com.ottawa.treasurehunt.treasurehunt;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.ottawa.treasurehunt.treasurehunt.checkpoint.CheckpointActivity;
import com.ottawa.treasurehunt.treasurehunt.utils.Parser;
import com.ottawa.treasurehunt.treasurehunt.utils.game.Checkpoint;
import com.ottawa.treasurehunt.treasurehunt.utils.game.Game;
import com.ottawa.treasurehunt.treasurehunt.utils.game.Position;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Stack;

public class Play extends AppCompatActivity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private Vibrator vibrator;
    private LocationManager locationManager;
    private final static float LOWPASS_ALPHA = 0.10f;
    long pastTime = 0;
    private int gameID;
    private static Game game = null;
    private static Stack<Checkpoint> checkpointStack;
    private static Checkpoint currentCheckpoint;
    private static boolean gameFinished = false;

    protected double currentLat = 55.705738;
    protected double currentLng = 13.209754;
    private static double destLat = 55.710754;
    private static double destLng = 13.210342;
    public static final String GAMEID = "GameID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
            gameID = bundle.getInt(GAMEID);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new MyLocationListener();
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1337);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 10, locationListener);

        //TODO: get the string value from the localstorage, or server
        String string = "{\n" +
                "    \"id\" : 1,\n" +
                "    \"name\" : \"first game\",\n" +
                "    \"description\" : \"This is our test game\",\n" +
                "\n" +
                "    \"position\": {\n" +
                "      \"lat\" : 55.711410,\n" +
                "\t    \"long\" : 13.208124\n" +
                "    },\n" +
                "    \"checkpoints\" : [ {\n" +
                "        \"position\" : {\n" +
                "            \"lat\" : 55.714294,\n" +
                "            \"long\" : 13.210682\n" +
                "        },\n" +
                "        \"minigame\" : null,\n" +
                "        \"quiz\" : [ {\n" +
                "            \"answers\" : [ {\n" +
                "                  \"answer\" : \"190 m\",\n" +
                "                  \"correct\" : true\n" +
                "                }, {\n" +
                "                  \"answer\" : \"160 m\",\n" +
                "                  \"correct\" : false\n" +
                "                }, {\n" +
                "                  \"answer\" : \"220 m\",\n" +
                "                  \"correct\" : false\n" +
                "                }, {\n" +
                "                  \"answer\" : \"260 m\",\n" +
                "                  \"correct\" : false\n" +
                "                } ],\n" +
                "            \"question\" : \"How tall is the Turning Torso?\"\n" +
                "        } ]\n" +
                "    } ]\n" +
                "}";




        try {
            game = Parser.generateGame(string);
            Log.i("success", "success");
        } catch (Exception e) {
            Log.e("Parser error", e.toString());
        }
        checkpointStack = new Stack<Checkpoint>();
        checkpointStack.addAll(game.getCheckpoints());
        nextCheckpoint();
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile (String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (gameFinished)
            startActivity(new Intent(this, MainActivity.class));

        if (event.sensor == mAccelerometer) {
            mLastAccelerometerSet = true;
            mLastAccelerometer = lowPass(event.values.clone(), mLastAccelerometer);
        }
        if (event.sensor == mMagnetometer) {
            mLastMagnetometerSet = true;
            mLastMagnetometer = lowPass(event.values.clone(), mLastMagnetometer);
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            float azimuthInRadians = mOrientation[0];
            long azimuthInDegrees = (long) (Math.toDegrees(azimuthInRadians) + 360) % 360;


            double distance = distFrom(currentLat, currentLng, destLat, destLng);
            double bearing = bearing(currentLat, currentLng, destLat, destLng);
            long diff = Math.abs((long) (bearing - azimuthInDegrees));

            if (distance < 10) {
                Intent i = new Intent(this, CheckpointActivity.class);
                i.putExtra(CheckpointActivity.GAME_TYPE, CheckpointActivity.QUIZ);

                String[] questions = new String[]{
                        "How tall is the Turning Torso?",
                        "Where's the headquarters of the automotive company Tesla located?"
                };

                i.putExtra(CheckpointActivity.QUIZ_QUESTIONS, questions);

                HashMap<String, Boolean> firstQAnswers = new HashMap<>();
                firstQAnswers.put("152m", false);
                firstQAnswers.put("212m", false);
                firstQAnswers.put("173m", false);
                firstQAnswers.put("190m", true);

                HashMap<String, Boolean> secondQAnswers = new HashMap<>();
                secondQAnswers.put("Los Angeles, California", false);
                secondQAnswers.put("Palo Alto, California", true);
                secondQAnswers.put("San Fransisco, California", false);
                secondQAnswers.put("Silicon Valley, California", false);

                @SuppressWarnings("unchecked")
                HashMap<String, Boolean>[] answers = new HashMap[]{
                        firstQAnswers,
                        secondQAnswers
                };

                i.putExtra(CheckpointActivity.QUIZ_ANSWERS, answers);

                this.startActivity(i);
            }

            if (diff <= 30) {
                if (distance < 50) { //close distance
                    if ((SystemClock.elapsedRealtime() - 250) > pastTime) {

                        Log.i("distance:", " " + distance);
                        Log.i("bearing:", " " + bearing);
                        Log.i("currentAzimuth:", " " + azimuthInDegrees);
                        Log.i("currentLat:", " " + currentLat);
                        Log.i("currentLng:", " " + currentLng);
                        Log.i("destLat:", " " + destLat);
                        Log.i("destLng:", " " + destLng);

                        long[] pattern = { 210, 40};
                        vibrator.vibrate(pattern, -1);
                        pastTime = SystemClock.elapsedRealtime();
                    }
                } else if (distance < 200) { //medium distance
                    if ((SystemClock.elapsedRealtime() - 500) > pastTime) {

                        Log.i("distance:", " " + distance);
                        Log.i("bearing:", " " + bearing);
                        Log.i("currentAzimuth:", " " + azimuthInDegrees);
                        Log.i("currentLat:", " " + currentLat);
                        Log.i("currentLng:", " " + currentLng);
                        Log.i("destLat:", " " + destLat);
                        Log.i("destLng:", " " + destLng);

                        long[] pattern = { 450, 50 };
                        vibrator.vibrate(pattern, -1);
                        pastTime = SystemClock.elapsedRealtime();
                    }
                } else { //far distance
                    if ((SystemClock.elapsedRealtime() - 1000) > pastTime) {

                        Log.i("distance:", " " + distance);
                        Log.i("bearing:", " " + bearing);
                        Log.i("currentAzimuth:", " " + azimuthInDegrees);
                        Log.i("currentLat:", " " + currentLat);
                        Log.i("currentLng:", " " + currentLng);
                        Log.i("destLat:", " " + destLat);
                        Log.i("destLng:", " " + destLng);

                        long[] pattern = { 900, 100};
                        vibrator.vibrate(pattern, -1);
                        pastTime = SystemClock.elapsedRealtime();
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public static void nextCheckpoint() {
        Checkpoint cp = checkpointStack.pop();
        if (cp != null) {
            currentCheckpoint = cp;
            destLng = cp.getPos().getLongitude();
            destLat = cp.getPos().getLatitude();
        } else {
            Log.i("GAME STATUS", "FINISHED");
            gameFinished = true;
        }
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
    }

    private float[] lowPass(float[] input, float[] output) {
        if (output == null) return input;

        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + LOWPASS_ALPHA * (input[i] - output[i]);
        }

        return output;
    }

    /**
     *
     * @param lat1 current latitude
     * @param lng1 current longitude
     * @param lat2 checkpoint latitude
     * @param lng2 checkpoint longitude
     * @return distance to checkpoint
     */
    private static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (float) (earthRadius * c);
    }

    /**
     *
     * @param lat1 current latitude
     * @param lng1 current longitude
     * @param lat2 checkpoint latitude
     * @param lng2 checkpoint longitude
     * @return bearing to checkpoint
     */
    private static double bearing(double lat1, double lng1, double lat2, double lng2){
        double l1 = Math.toRadians(lat1);
        double l2 = Math.toRadians(lat2);
        double longDiff = Math.toRadians(lng2 - lng1);
        double y = Math.sin(longDiff) * Math.cos(l2);
        double x = Math.cos(l1) * Math.sin(l2) - Math.sin(l1) * Math.cos(l2) * Math.cos(longDiff);
        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            currentLat = location.getLatitude();
            currentLng = location.getLongitude();
            Log.i("MyLocationListener", "Location changed!");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
}

