package com.lujustin.hammrd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    private Button getHomeButton;
    private Button getFoodButton;
    private Button settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

    public void openGetHome() {
        Intent intent = new Intent(this, MapNavActivity.class);
        intent.putExtra("NAV_MODE", "GetHome");
        startActivity(intent);
    }

    public void openGetFood() {
        Intent intent = new Intent(this, MapNavActivity.class);
        intent.putExtra("NAV_MODE", "MapNavActivity");
        startActivity(intent);
    }

    public void openSettings() {
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }
}
