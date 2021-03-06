package com.lujustin.hammrd;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    private Button getHomeButton;
    private Button getFoodButton;
    private Button settingsButton;
    private static final String FIRST_TIME_INDICATOR = "FIRST_TIME_INDICATOR";
    private static final String NAV_MODE = "NAV_MODE";
    private static final String NAV_MODE_GETHOME = "GetHome";
    private static final String NAV_MODE_GETFOOD = "GetFood";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getHomeButton = findViewById(R.id.homeButton);
        getHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGetHome();
            }
        });

        getFoodButton = findViewById(R.id.foodButton);
        getFoodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGetFood();
            }
        });

        settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettings();
            }
        });

    }

    @Override
    protected void onResume() {
        Log.d("MainActivity", "onResume called");
        checkFirstTimeActivity();
        super.onResume();
    }

    public void checkFirstTimeActivity() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPreferences.getBoolean(FIRST_TIME_INDICATOR, false)) {
            Log.d("MainActivity", "launching first time activity");
            openFirstTimeActivity();
        }
    }

    public void openFirstTimeActivity() {
        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
    }


    public void openGetHome() {
        Intent intent = new Intent(this, MapNavActivity.class);
        intent.putExtra(NAV_MODE, NAV_MODE_GETHOME);
        startActivity(intent);
    }

    public void openGetFood() {
        Intent intent = new Intent(this, MapNavActivity.class);
        intent.putExtra(NAV_MODE, NAV_MODE_GETFOOD);
        startActivity(intent);
    }

    public void openSettings() {
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }
}
