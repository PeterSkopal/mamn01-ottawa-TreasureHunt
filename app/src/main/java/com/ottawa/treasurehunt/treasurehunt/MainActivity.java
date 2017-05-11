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

    public void onMapsClick (View v) {
        Intent i = new Intent(this, MapsActivity.class);
        this.startActivity(i);
    }

    public void onPlayClick (View v) {
        Intent i = new Intent(this, Play.class);
        this.startActivity(i);
    }

    public void onCheckpointClick(View v) {
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

    public void onMiniClick (View v) {
      //  Intent i = new Intent(this, ShakeGameActivity.class);
        Intent i = new Intent(this, CheckpointActivity.class);
        i.putExtra(CheckpointActivity.GAME_TYPE, CheckpointActivity.MINIGAME);
        i.putExtra(CheckpointActivity.MINIGAME_ID, 1);


        this.startActivity(i);
    }

    public void onMini2Click (View v) {
        //  Intent i = new Intent(this, ShakeGameActivity.class);
        Intent i = new Intent(this, CheckpointActivity.class);
        i.putExtra(CheckpointActivity.GAME_TYPE, CheckpointActivity.MINIGAME);
        i.putExtra(CheckpointActivity.MINIGAME_ID, 2);


        this.startActivity(i);
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
