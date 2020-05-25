package com.lujustin.hammrd;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.textfield.TextInputEditText;
import com.lujustin.hammrd.adapters.PlaceAutoSuggestAdapter;

import java.util.Arrays;

public class Settings extends AppCompatActivity {

    SharedPreferences settingsPref;
    TextInputEditText userNameField;
    TextInputEditText userNumberField;
    TextInputEditText contactNameField;
    TextInputEditText contactNumberField;
    AutoCompleteTextView addressField;
    Button saveButton;

    public static final String preferenceName = "HammrdPreferences";
    public static final String userName = "userName";
    public static final String userNumber = "userNumber";
    public static final String contactName = "contactName";
    public static final String contactNumber = "contactNumber";
    public static final String address = "address";

    private static final String TAG = "SettingsActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settingsPref = getSharedPreferences(preferenceName, Context.MODE_PRIVATE);

        userNameField = findViewById(R.id.userName);
        userNumberField = findViewById(R.id.userNumber);
        contactNameField = findViewById(R.id.contactName);
        contactNumberField = findViewById(R.id.contactNumber);

        addressField = findViewById(R.id.address);
        Log.d(TAG, Integer.toString(android.R.layout.simple_list_item_1));

        addressField.setAdapter(new PlaceAutoSuggestAdapter(this, android.R.layout.simple_list_item_1));

        saveButton = findViewById(R.id.saveSettingsButton);

        loadSettings();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeSettings();
            }
        });
    }

    private void writeSettings() {
        SharedPreferences.Editor settingsEditor = settingsPref.edit();

        String userNameText = userNameField.getText().toString();
        String userNumberText = userNumberField.getText().toString();
        String contactNameText = contactNameField.getText().toString();
        String contactNumberText = contactNumberField.getText().toString();
        String addressText = addressField.getText().toString();

        settingsEditor.putString(userName, userNameText);
        settingsEditor.putString(userNumber, userNumberText);
        settingsEditor.putString(contactName, contactNameText);
        settingsEditor.putString(contactNumber, contactNumberText);
        settingsEditor.putString(address, addressText);

        settingsEditor.commit();
        Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();
        finish();
    }


    private void loadSettings() {
        String userNameText = settingsPref.getString(userName, "");
        String userNumberText = settingsPref.getString(userNumber, "");
        String contactNameText = settingsPref.getString(contactName, "");
        String contactNumberText = settingsPref.getString(contactName, "");
        String addressText = settingsPref.getString(address, "");

        userNameField.setText(userNameText);
        userNumberField.setText(userNumberText);
        contactNameField.setText(contactNameText);
        contactNumberField.setText(contactNumberText);
        addressField.setText(addressText);
    }
}
