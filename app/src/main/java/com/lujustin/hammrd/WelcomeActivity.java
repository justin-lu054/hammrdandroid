package com.lujustin.hammrd;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class WelcomeActivity extends AppCompatActivity {

    private Button getStartedButton;
    private CheckBox tosCheckbox;
    private boolean checkboxChecked = false;
    private static final String FIRST_TIME_INDICATOR = "FIRST_TIME_INDICATOR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firsttime);

        getStartedButton = findViewById(R.id.getStartedButton);
        tosCheckbox = findViewById(R.id.tosCheckbox);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        tosCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkboxChecked = isChecked;
            }
        });

        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkboxChecked) {
                    builder.setTitle("Terms of Service")
                            .setMessage("You must accept the terms of service before proceeding.")
                            .setCancelable(false)
                            .setPositiveButton("OK", ((dialog, which) -> dialog.dismiss()))
                            .create()
                            .show();
                    return;
                }
                SharedPreferences.Editor sharedPreferencesEditor = PreferenceManager
                        .getDefaultSharedPreferences(WelcomeActivity.this).edit();

                sharedPreferencesEditor.putBoolean(FIRST_TIME_INDICATOR, true);
                sharedPreferencesEditor.apply();
                openSettings();
                finish();
            }
        });
    }

    public void openSettings() {
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }
}
