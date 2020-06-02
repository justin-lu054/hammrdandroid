package com.lujustin.hammrd.models;

import androidx.core.app.NotificationCompat;

import com.google.gson.annotations.SerializedName;

public class HammrdTwilioData {
    @SerializedName("userName")
    private String userName;

    @SerializedName("userNumber")
    private String userNumber;

    @SerializedName("contactName")
    private String contactName;

    @SerializedName("contactNumber")
    private String contactNumber;

    @SerializedName("location")
    private String location;

    @SerializedName("elapsedTime")
    private int elapsedTime;

    public HammrdTwilioData(String userName, String userNumber,
                            String contactName, String contactNumber, String location, int elapsedTime) {
        this.userName = userName;
        this.userNumber = userNumber;
        this.contactName = contactName;
        this.contactNumber = contactNumber;
        this.location = location;
        this.elapsedTime = elapsedTime;
    }

}
