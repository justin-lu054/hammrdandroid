package com.lujustin.hammrd;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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

    String userNameText;
    String userNumberText;
    String contactNameText;
    String contactNumberText;
    String addressText;
    String maxInactivityTimeText;

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
                verifySettings();
            }
        });

        loadSettings();
    }

    private void verifySettings() {
        userNameText = userNameField.getText().toString();
        userNumberText = userNumberField.getText().toString();
        contactNameText = contactNameField.getText().toString();
        contactNumberText = contactNumberField.getText().toString();
        addressText = addressField.getText().toString();
        maxInactivityTimeText = maxInactivityTimeField.getText().toString();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (userNameText.trim().length() == 0 || contactNameText.trim().length() == 0 ||
            addressText.trim().length() == 0 || maxInactivityTimeText.length() == 0) {

            builder.setTitle("Looks like you're missing something!")
                    .setMessage("Please fill out all the settings first.")
                    .setCancelable(true)
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
            return;
        }

        if (userNumberText.length() != 10 || contactNumberText.length() != 10) {
            builder.setTitle("Looks like you're missing something!")
                    .setMessage("Please provide a valid phone number.")
                    .setCancelable(true)
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
            return;
        }
        writeSettings();
    }

    private void writeSettings() {
        SharedPreferences.Editor settingsEditor = settingsPref.edit();

        //convert the specified maximum inactivity time to miliseconds
        long maxInactivityTimeValue = Integer.parseInt(maxInactivityTimeText) * 60 * 1000;
        settingsEditor.putString(userName, userNameText);
        settingsEditor.putString(userNumber, "+1" + userNumberText);
        settingsEditor.putString(contactName, contactNameText);
        settingsEditor.putString(contactNumber, "+1" + contactNumberText);
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
        long maxInactivityTimeValue = settingsPref.getLong(maxInactivityTime, 30 * 60 * 1000);

        userNameField.setText(userNameText);
        userNumberField.setText(userNumberText.replace("+1", ""));
        contactNameField.setText(contactNameText);
        contactNumberField.setText(contactNumberText.replace("+1", ""));
        addressField.setText(addressText);
        maxInactivityTimeField.setText(Long.toString(maxInactivityTimeValue / 60 / 1000));
    }
}
