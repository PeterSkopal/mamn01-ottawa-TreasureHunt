package com.ottawa.treasurehunt.treasurehunt;

import android.Manifest;
import android.content.Context;
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

import com.ottawa.treasurehunt.treasurehunt.utils.game.Position;

import org.json.JSONException;
import org.json.JSONObject;

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
    protected double currentLng = 0;
    protected double currentLat = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new MyLocationListener();
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1337);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);


        // hardcoded JSON object
        JSONObject obj = new JSONObject();
        JSONObject v1 = new JSONObject();
        JSONObject v2 = new JSONObject();
        JSONObject v3 = new JSONObject();
        try {
            v1.put("long", 55.714810);
            v1.put("lat", 13.211906);
            v2.put("long", 55.713573);
            v2.put("lat", 13.211144);
            v3.put("long", 55.713078);
            v3.put("lat", 13.212636);
            JSONObject[] array = {v1, v2, v3};
            obj.put("checkpoints", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }

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


            long spot = 200; // This is hardcoded TODO: get the real degree value gps coordinate and calculate bearing
            double lat1, lat2, lng1, lng2;
            lat1 = currentLat;
            lng1 = currentLng;
            lat2 = 13.212636;
            lng2 = 55.713078;
            double bearing = bearing(lat1, lng1, lat2, lng2);
            long distance = 40;

            long diff = Math.abs((long) (bearing - azimuthInDegrees));
            if (diff <= 30) {
                if (distance < 50) { //close distance
                    if ((SystemClock.elapsedRealtime() - 250) > pastTime) {
                        long[] pattern = { 210, 40};
                        Log.i("vibration", "Now it vibrates Close.");
                        vibrator.vibrate(pattern, -1);
                        pastTime = SystemClock.elapsedRealtime();
                    }
                } else if (distance < 200) { //medium distance
                    if ((SystemClock.elapsedRealtime() - 500) > pastTime) {
                        long[] pattern = { 450, 50};
                        Log.i("vibration", "Now it vibrates Medium.");
                        vibrator.vibrate(pattern, -1);
                        pastTime = SystemClock.elapsedRealtime();
                    }
                } else { //far distance
                    if ((SystemClock.elapsedRealtime() - 1000) > pastTime) {
                        long[] pattern = { 900, 100};
                        Log.i("vibration", "Now it vibrates Far.");
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

    public static void setLocation(Position position) {

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
    private static float distFrom(float lat1, float lng1, float lat2, float lng2) {
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

        return (Math.toDegrees(Math.atan2(y, x))+360)%360;
    }
    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            currentLat = location.getLatitude();
            currentLng = location.getLongitude();
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

