package com.lujustin.hammrd.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.lujustin.hammrd.R;
import com.lujustin.hammrd.models.HammrdTwilioData;
import com.lujustin.hammrd.models.TwilioInterface;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LocationTrackingService extends Service {

    private static final String TAG = "LocationTrackingService";

    private final static long UPDATE_INTERVAL = 60 * 1000;
    private final static long FASTEST_INTERVAL = 60 * 1000;
    private final static String ACTION_STOP_TRACKING = "ACTION_STOP_TRACKING";
    private final String ACTION_START_TRACKING = "ACTION_START_TRACKING";

    private FusedLocationProviderClient fusedLocationProviderClient;
    private PowerManager.WakeLock wakeLock;

    private String userNameText;
    private String userNumberText;
    private String contactNameText;
    private String contactNumberText;
    private Location destinationLocation;
    private long maxInactivityTime;

    private Retrofit retrofit;
    private TwilioInterface twilioInterface;

    private LocationCallback locationCallback;

    private ArrayList<LocalDateTime> timeHistory = new ArrayList<>();
    private ArrayList<Location> locationHistory = new ArrayList<>();
    private long elapsedTime = 0;
    private double elapsedDistance = 0;
    private boolean alreadyTracking = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        retrofit = new Retrofit.Builder().baseUrl("https://hammrdandroidtwilioservice.herokuapp.com/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
        twilioInterface = retrofit.create(TwilioInterface.class);

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LocationTrackingService::WakeLock");
        wakeLock.acquire();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy called");
        stopLocationTracking();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(ACTION_START_TRACKING) && !alreadyTracking) {
                    Bundle bundle = intent.getExtras();
                    destinationLocation = new Location("");
                    destinationLocation.setLatitude(bundle.getDouble("destinationLatitude"));
                    destinationLocation.setLongitude(bundle.getDouble("destinationLongitude"));
                    userNameText = bundle.getString("userName");
                    userNumberText = bundle.getString("userNumber");
                    contactNameText = bundle.getString("contactName");
                    contactNumberText = bundle.getString("contactNumber");
                    maxInactivityTime = bundle.getLong("maxInactivityTime");
                    alreadyTracking = true;
                    startLocationTracking();
                }
                if (action.equals(ACTION_STOP_TRACKING)) {
                    /*
                        We need to call this here to prevent duplicate location updates from being
                        called if the service is stopped an restarted within a single lifespan
                        of the app
                     */
                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                    stopLocationTracking();
                }
            }
        }
        return START_NOT_STICKY;
    }

    private void startLocationTracking() {
        Log.d(TAG, "Starting location tracking service...");
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "location_tracking_channel";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "My Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Intent stopIntent = new Intent(this, LocationTrackingService.class);
            stopIntent.setAction(ACTION_STOP_TRACKING);
            PendingIntent pendingStopIntent = PendingIntent.getService(this, 0,
                    stopIntent, 0);
            NotificationCompat.Action stopAction = new NotificationCompat.Action(android.R.drawable.ic_menu_close_clear_cancel,
                    "Stop Tracking", pendingStopIntent);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle("Tracking your location")
                    .setContentText("We're doing this to keep you safe!")
                    .setOngoing(true)
                    .addAction(stopAction)
                    .build();

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(1, notification);
            startForeground(1, notification);
        }
        getLocation();
    }

    private void getLocation() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permission not granted. Stopping service");
            stopLocationTracking();
            return;
        }

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                Log.d(TAG, "Called location updates");
                if (location != null) {
                    handleLocation(location);
                }
            }
        };
        //added foreground service type to android manifest and location updates now work in background
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void handleLocation(Location location) {
        LocalDateTime currentTime = LocalDateTime.now();
        if (locationHistory.size() > 0) {
            elapsedDistance += locationHistory.get(locationHistory.size() - 1).distanceTo(location);
            locationHistory.remove(0);
        }
        if (timeHistory.size() > 0) {
            elapsedTime += (Duration.between(timeHistory.get(timeHistory.size() - 1), currentTime).getSeconds()) * 1000;
            timeHistory.remove(0);
        }
        Log.d(TAG, "latitude: " + location.getLatitude() + " longitude: " + location.getLongitude());
        Log.d(TAG, "elapsed time: " + elapsedTime);
        Log.d(TAG, "elapsed distance: " + elapsedDistance);

        if (location.distanceTo(destinationLocation) < 100) {
            Log.d(TAG, "Reached Destination");
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            stopLocationTracking();
        }

        if (elapsedTime > maxInactivityTime) {
            if (elapsedDistance < 100) {
                Log.d(TAG, "Inactivity detected");

                String locationString = location.getLatitude() + "," + location.getLongitude();
                int elapsedTimeMin = (int) elapsedTime / 60 / 1000;

                HammrdTwilioData hammrdTwilioData = new HammrdTwilioData(userNameText, userNumberText,
                                                        contactNameText, contactNumberText,
                                                        locationString, elapsedTimeMin);
                Call<Void> twilioPostRequest = twilioInterface.sendSMS(hammrdTwilioData);
                twilioPostRequest.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if(!response.isSuccessful()) {
                            Log.e(TAG, "Error with POST request. Code " + response.code());
                            return;
                        }
                        Log.d(TAG, "Success! Text message sent.");
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e(TAG, "Error with POST request.");
                        t.printStackTrace();
                    }
                });
            }
            timeHistory.clear();
            locationHistory.clear();
            elapsedDistance = 0;
            elapsedTime = 0;
        }

        timeHistory.add(currentTime);
        locationHistory.add(location);
    }

    private void stopLocationTracking() {
        alreadyTracking = false;
        timeHistory.clear();
        locationHistory.clear();
        elapsedDistance = 0;
        elapsedTime = 0;
        fusedLocationProviderClient = null;
        stopForeground(true);
        stopSelf();
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
    }
}
