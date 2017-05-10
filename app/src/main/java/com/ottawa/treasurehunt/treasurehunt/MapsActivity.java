package com.ottawa.treasurehunt.treasurehunt;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {
    private GoogleMap mMap;
    private LocationManager locationManager;
    private Button button;

    private HashMap<Marker, Integer> gameMarkers;
    private boolean centerOnce;
    private int standingOnGameId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        button = (Button) findViewById(R.id.startBtn);
        button.setVisibility(View.GONE);
        mapFragment.getMapAsync(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("To play, simply walk up to a Game Marker and await the Start Game button.")
                .setTitle("Welcome!")
                .setCancelable(false)
                .setPositiveButton("OK", null);
        AlertDialog alert = builder.create();
        alert.show();

        gameMarkers = new HashMap<>();
        centerOnce = false;

        SharedPreferences prefs = getSharedPreferences(Play.PREFS, Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //TODO: put all the markers from all the json game IDs, both on map and hashmap
        //TODO: JSON thing is now needed
        Marker m = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(55.711165, 13.207776)).title("First Game"));
        gameMarkers.put(m, 1);

        m = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(55.721001, 13.210863)).title("Delphi's Game"));
        gameMarkers.put(m, 2);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        enableMyLocation();
    }

    public void onStartPress(View v) {
        Intent i = new Intent(this, Play.class);
        i.putExtra(Play.GAMEID, standingOnGameId);
        this.startActivity(i);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1337);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 10, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        /*
        double distance = distFrom(location.getLatitude(), location.getLongitude(), destLat, destLng);
        if (distance < 25) {
            button.setVisibility(View.VISIBLE);
        } else {
            button.setVisibility(View.GONE);
        }
        */

        /*
         * When out location is updated we go through our markers to see if we're standing on top of
         * a marker. If we're standing on a marker we show the START GAME button and set
         * `standingOnGameId` value to the marker's gameId, when the user presses START GAME, we
         * launch the Play activity with `standingOnGameId` id, see `onStartPress`.
         */
        for (Map.Entry<Marker, Integer> e : gameMarkers.entrySet()) {
            double distance = distFrom(
                    location.getLatitude(),
                    location.getLongitude(),
                    e.getKey().getPosition().latitude,
                    e.getKey().getPosition().longitude);

            if (distance < 25) {
                button.setVisibility(View.VISIBLE);
                standingOnGameId = e.getValue();
                break;
            } else {
                button.setVisibility(View.INVISIBLE);
            }
        }

        if (!centerOnce) { /* This is used ¿instead? of implementing onConnected and onDisconnected (I believe) */
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 17.0f));
            centerOnce = true;
        }
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
}
