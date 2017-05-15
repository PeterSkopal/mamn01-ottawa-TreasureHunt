package com.ottawa.treasurehunt.treasurehunt;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ottawa.treasurehunt.treasurehunt.checkpoint.CheckpointActivity;
import com.ottawa.treasurehunt.treasurehunt.utils.Parser;
import com.ottawa.treasurehunt.treasurehunt.utils.game.Checkpoint;
import com.ottawa.treasurehunt.treasurehunt.utils.game.Game;
import com.ottawa.treasurehunt.treasurehunt.utils.PlayFragment;

import java.util.ArrayList;
import java.util.HashMap;

public class Play extends FragmentActivity implements SensorEventListener {
    public static final String GAMEID = "GameID";
    public static final String CHECKPOINT_CURRENT = "checkpointCurrent";
    public static final String PREFS = "com.ottawa.treasurehunt.Play";

    private int currentCheckpoint = 0;
    private ArrayList<Checkpoint> checkpointList;
    private boolean isLaunchingCheckpoint;
    private SharedPreferences prefs;

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
    private Game game = null;

    protected double currentLat = 55.705738;
    protected double currentLng = 13.209754;
    private double destLat = 55.710754;
    private double destLng = 13.210342;

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

        RequestQueue queue = Volley.newRequestQueue(this);
        String gamesUrl = "https://treasurehunt-mamn01.herokuapp.com/api/games/" + gameID;

        StringRequest strRequest = new StringRequest(Request.Method.GET, gamesUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("Play", response);

                        game = Parser.generateGame(response);

                        checkpointList = game.getCheckpoints();

                        Log.i("Play", "onCreate PLAY");

                        prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);

                        currentCheckpoint = prefs.getInt(CHECKPOINT_CURRENT, 0);

                        Log.i("Play", "onCreate > currentCheckpoint: " + currentCheckpoint);
                        setCheckpoint(currentCheckpoint);

                        isLaunchingCheckpoint = false;
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Maps", error.toString());
                    }
                });

        queue.add(strRequest);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
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

            /* Only use when debugging device routing
            Log.i("DATA_READINGS", "distance \t\t" + distance);
            Log.i("DATA_READINGS", "bearing \t\t" + bearing);
            Log.i("DATA_READINGS", "azimuth \t\t" + azimuthInDegrees);
            Log.i("DATA_READINGS", "lat \t\t" + currentLat);
            Log.i("DATA_READINGS", "long \t\t" + currentLng);
            Log.i("DATA_READINGS", "lat dest \t\t" + destLat);
            Log.i("DATA_READINGS", "long dest \t\t" + destLng);
            */

            if (distance < 10 && !isLaunchingCheckpoint) {
                isLaunchingCheckpoint = true;
                vibrator.vibrate(1000);
                mSensorManager.unregisterListener(this);
                if (checkpointList.get(currentCheckpoint).getMinigameId() != 0) { //if minigame
                    setFragment(PlayFragment.newInstance("minigame"));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent i = new Intent(getApplicationContext(), CheckpointActivity.class);
                            int gameId = checkpointList.get(currentCheckpoint).getMinigameId();

                            i.putExtra(CheckpointActivity.GAME_TYPE, CheckpointActivity.MINIGAME);
                            i.putExtra(CheckpointActivity.MINIGAME_ID, gameId);

                            prefs.edit().putInt(CHECKPOINT_CURRENT, ++currentCheckpoint).apply();
                            startActivity(i);
                        }
                    }, 5000);


                } else { //if quiz
                    setFragment(PlayFragment.newInstance("quiz"));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent i = new Intent(getApplicationContext(), CheckpointActivity.class);
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

                            prefs.edit().putInt(CHECKPOINT_CURRENT, ++currentCheckpoint).apply();
                            startActivity(i);
                        }
                    }, 5000);
                }
            }

            if (diff <= 30) {
                if (distance < 50) { //close distance
                    if ((SystemClock.elapsedRealtime() - 250) > pastTime) {
                        long[] pattern = { 210, 40};
                        vibrator.vibrate(pattern, -1);
                        pastTime = SystemClock.elapsedRealtime();
                    }
                } else if (distance < 200) { //medium distance
                    if ((SystemClock.elapsedRealtime() - 500) > pastTime) {
                        long[] pattern = { 450, 50 };
                        vibrator.vibrate(pattern, -1);
                        pastTime = SystemClock.elapsedRealtime();
                    }
                } else { //far distance
                    if ((SystemClock.elapsedRealtime() - 1000) > pastTime) {
                        long[] pattern = { 900, 100};
                        vibrator.vibrate(pattern, -1);
                        pastTime = SystemClock.elapsedRealtime();
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}



    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);

        Log.i("Play", "Resuming");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);

        Log.i("Play", "Pausing");
    }

    private void setCheckpoint(int checkpoint) {
        Checkpoint cp = checkpointList.get(checkpoint);

        if (cp != null) {
            destLng = cp.getPos().getLongitude();
            destLat = cp.getPos().getLatitude();
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
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
            Log.i("Play", "Location changed!");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    }

    // http://stackoverflow.com/a/28208203
    protected void setFragment(Fragment fragment) {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit();
    }
}

