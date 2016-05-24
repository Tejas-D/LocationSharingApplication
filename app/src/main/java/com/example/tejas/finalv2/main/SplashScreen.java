/**
 * File Name:               SplashScreen.java
 * File Description:        Creating the class that will initate a splash screen
 *
 * Author:                  Tejas Dwarkaram
 * Credit:                  Nico Kiewiet, Brandon Talbot
 */

package com.example.tejas.finalv2.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.example.tejas.finalv2.R;

public class SplashScreen extends Activity {

    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(SplashScreen.this, MapsActivity.class);
                startActivity(i);

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
