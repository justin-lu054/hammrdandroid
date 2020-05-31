package com.lujustin.hammrd;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.lujustin.hammrd.adapters.PlaceAutoSuggestAdapter;

public class Settings extends AppCompatActivity {

    private SharedPreferences settingsPref;
    private TextInputEditText userNameField;
    private TextInputEditText userNumberField;
    private TextInputEditText contactNameField;
    private TextInputEditText contactNumberField;
    private TextInputEditText maxInactivityTimeField;
    private AutoCompleteTextView addressField;
    private Button saveButton;

    public static final String preferenceName = "HammrdPreferences";
    public static final String userName = "userName";
    public static final String userNumber = "userNumber";
    public static final String contactName = "contactName";
    public static final String contactNumber = "contactNumber";
    public static final String address = "address";
    public static final String maxInactivityTime = "maxInactivityTime";

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
        maxInactivityTimeField = findViewById(R.id.maxInactivityTime);

        addressField = findViewById(R.id.address);
        addressField.setAdapter(new PlaceAutoSuggestAdapter(this, android.R.layout.simple_list_item_1));

        saveButton = findViewById(R.id.saveSettingsButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeSettings();
            }
        });

        loadSettings();
    }

    private void writeSettings() {
        SharedPreferences.Editor settingsEditor = settingsPref.edit();
        String userNameText = userNameField.getText().toString();
        String userNumberText = userNumberField.getText().toString();
        String contactNameText = contactNameField.getText().toString();
        String contactNumberText = contactNumberField.getText().toString();
        String addressText = addressField.getText().toString();
        String maxInactivityTimeText = maxInactivityTimeField.getText().toString();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String validPhonePattern = "^\\+1[2-9]\\d{9}$";

        if (userNameText.trim().length() == 0 || userNumberText.trim().length() == 0 ||
            contactNameText.trim().length() == 0 || contactNumberText.trim().length() == 0 ||
            addressText.trim().length() == 0 || maxInactivityTimeText.length() == 0) {

            builder.setTitle("Looks like you're missing something!")
                    .setMessage("Please fill out all the settings first.")
                    .setCancelable(true)
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
            return;
        }

        if (!(userNumberText.matches(validPhonePattern) && contactNumberText.matches(validPhonePattern))) {
            builder.setTitle("Please provide a valid phone number.")
                    .setMessage("\"Please enter your phone number in the format +1xxxxxxxxxx." +
                            "Only North American numbers are supported")
                    .setCancelable(true)
                    .setPositiveButton("Dismiss", ((dialog, which) -> dialog.dismiss()))
                    .create()
                    .show();
            return;
        }
        //convert the specified maximum inactivity time to miliseconds
        long maxInactivityTimeValue = Integer.parseInt(maxInactivityTimeText) * 60 * 1000;
        settingsEditor.putString(userName, userNameText);
        settingsEditor.putString(userNumber, userNumberText);
        settingsEditor.putString(contactName, contactNameText);
        settingsEditor.putString(contactNumber, contactNumberText);
        settingsEditor.putString(address, addressText);
        settingsEditor.putLong(maxInactivityTime, maxInactivityTimeValue);

        settingsEditor.commit();
        Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();
        finish();
    }


    private void loadSettings() {
        String userNameText = settingsPref.getString(userName, "");
        String userNumberText = settingsPref.getString(userNumber, "");
        String contactNameText = settingsPref.getString(contactName, "");
        String contactNumberText = settingsPref.getString(contactNumber, "");
        String addressText = settingsPref.getString(address, "");
        long maxInactivityTimeValue = settingsPref.getLong(maxInactivityTime, 30);

        userNameField.setText(userNameText);
        userNumberField.setText(userNumberText);
        contactNameField.setText(contactNameText);
        contactNumberField.setText(contactNumberText);
        addressField.setText(addressText);
        maxInactivityTimeField.setText(Long.toString(maxInactivityTimeValue / 60 / 1000));
    }
}
