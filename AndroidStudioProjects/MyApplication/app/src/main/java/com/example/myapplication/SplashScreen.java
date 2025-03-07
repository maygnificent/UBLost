package com.example.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;

public class SplashScreen extends AppCompatActivity { // THIS IS THE LOGO PAGE
    int SPLASH_TIME_OUT = 4000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

         new Handler().postDelayed(new Runnable() {
              @Override
              public void run() {
                  Intent homeIntent = new Intent(SplashScreen.this, LoginActivity.class);
                  startActivity(homeIntent);
                  finish();
              }
          },SPLASH_TIME_OUT);
    }

}
