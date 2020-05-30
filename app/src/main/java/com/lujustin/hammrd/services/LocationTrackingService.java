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
    private double destinationLatitude;
    private double destinationLongitude;

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
                    destinationLatitude = bundle.getDouble("destinationLatitude");
                    destinationLongitude = bundle.getDouble("destinationLongitude");
                    userNameText = bundle.getString("userNameText");
                    userNumberText = bundle.getString("userNumber");
                    contactNameText = bundle.getString("contactName");
                    contactNumberText = bundle.getString("contactNumber");
                    alreadyTracking = true;
                    startLocationTracking();
                }
                if (action.equals(ACTION_STOP_TRACKING)) {
                    stopLocationTracking();
                }
            }
        }
        return START_NOT_STICKY;
    }

    public void startLocationTracking() {
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

    public void getLocation() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permission not granted. Stopping service");
            stopSelf();
            return;
        }
        //added foreground service type to android manifest and location updates now work in background
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                Log.d(TAG, "Called location updates");
                if (location != null) {
                    Log.d(TAG, "latitude: " + location.getLatitude() + " longitude: " + location.getLongitude());
                }
            }

        }, Looper.myLooper());
    }

    public void stopLocationTracking() {
        alreadyTracking = false;
        stopForeground(true);
        stopSelf();
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
    }

}
