package com.ottawa.treasurehunt.treasurehunt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void onMapsClick (View v) {
        Intent i = new Intent(this, MapsActivity.class);
        this.startActivity(i);
    }

    public void onPlayClick (View v) {
        Intent i = new Intent(this, Play.class);
        this.startActivity(i);
    }
}
