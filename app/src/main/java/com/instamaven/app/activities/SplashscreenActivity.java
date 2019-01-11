package com.instamaven.app.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.instamaven.app.R;

import java.util.Timer;
import java.util.TimerTask;

public class SplashscreenActivity extends AppCompatActivity {

    long Delay = 2500;
    protected ImageView imageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale);
        imageView = findViewById(R.id.splashImageView);
        imageView.startAnimation(anim);
        // Create a Timer
        Timer RunSplash = new Timer();

        // Task to do when the timer ends
        TimerTask ShowSplash = new TimerTask() {
            @Override
            public void run() {
                // Close SplashScreenActivity.class
                finish();

                // Start Activity.class
                Intent myIntent = new Intent(SplashscreenActivity.this, SignInActivity.class);
                startActivity(myIntent);
            }
        };

        // Start the timer
        RunSplash.schedule(ShowSplash, Delay);
    }
}
