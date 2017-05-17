package com.ottawa.treasurehunt.treasurehunt;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.support.v7.app.ActionBar;
import android.widget.Toast;

import com.ottawa.treasurehunt.treasurehunt.checkpoint.CheckpointActivity;

import java.security.Permission;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION_RECORD_AUDIO = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //****************************************************************
        // ADD NEW PERMISSIONS YOU WANT TO REQUEST HERE
        String permissionsWeNeed[] = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO};
        //****************************************************************

        //Log.i("Permission Check", Boolean.toString(ContextCompat.checkSelfPermission(this,
        //        Manifest.permission.ACCESS_FINE_LOCATION)
        //                != PackageManager.PERMISSION_GRANTED));

        LinkedList<String> permissionsWeRequest = new LinkedList<String>();

        for (int i = 0; i < permissionsWeNeed.length; i++){
            if(ContextCompat.checkSelfPermission(this, permissionsWeNeed[i]) != PackageManager.PERMISSION_GRANTED){
                Log.i("Permission type we need", permissionsWeNeed[i]);
                permissionsWeRequest.add(permissionsWeNeed[i]);
            }
        }

        if(permissionsWeRequest.size() > 0){
            Log.i("Request length",String.valueOf(permissionsWeRequest.size()));
            String[] permissionsToArray = Arrays.copyOf(permissionsWeRequest.toArray(), permissionsWeRequest.toArray().length, String[].class);
            Log.i("Permissions we request", permissionsWeRequest.toString());
            ActivityCompat.requestPermissions(this,
                    permissionsToArray,
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION_RECORD_AUDIO);
        }
    }

    public void onPlayClick (View v) {
        Intent i = new Intent(this, MapsActivity.class);
        this.startActivity(i);
    }

    public void onSettingsClick (View v) {
        Toast.makeText(this, "Out of Scope for this Prototype", Toast.LENGTH_SHORT).show();
    }

    public void onHighscoreClick (View v) {
        Toast.makeText(this, "Out of Scope for this Prototype", Toast.LENGTH_SHORT).show();
    }

    public void onResume(){
        super.onResume();
        //Hide the status bar
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                boolean allGranted = true;
                for(int i = 0; i < grantResults.length; i++){
                    if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        allGranted = false;
                        break;
                    }
                }

                if (allGranted) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    for (int i = 0; i < grantResults.length; i++){
                        Log.i("Permission ID", permissions[i]);
                        Log.i("Permission not granted", Boolean.toString(grantResults[i] == PackageManager.PERMISSION_DENIED));
                        if (grantResults[i] == PackageManager.PERMISSION_DENIED){
                            switch (permissions[i]){
                                case Manifest.permission.ACCESS_FINE_LOCATION:
                                    Toast.makeText(getApplicationContext(), "The application needs access to your location to work properly. Now it may crash.",
                                            Toast.LENGTH_LONG).show();
                                    break;
                                case Manifest.permission.RECORD_AUDIO:
                                    Toast.makeText(getApplicationContext(), "The application needs access to your microphone to work properly. Now it may crash.",
                                            Toast.LENGTH_LONG).show();
                                    break;
                            }
                        }
                    }
                }
                return;
            }


            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
