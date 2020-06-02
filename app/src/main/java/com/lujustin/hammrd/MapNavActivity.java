package com.lujustin.hammrd;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Tasks;

import com.lujustin.hammrd.models.NavDirectionsList;
import com.lujustin.hammrd.models.NearestOpenRestaurant;
import com.lujustin.hammrd.models.NearestOpenRestaurantList;
import com.lujustin.hammrd.models.MapsApiInterface;
import com.lujustin.hammrd.services.LocationTrackingService;

import java.io.IOException;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapNavActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;

    private boolean locationPermissionGranted;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;

    //store location in activity state
    private static final String TAG = "MapNavActivity";
    private static final int DEFAULT_ZOOM = 14;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION_DENIED = 0;

    private String apiKey;
    private SharedPreferences settingsPref;

    private static final String preferenceName = "HammrdPreferences";
    private String userNameText;
    private String userNumberText;
    private String contactNameText;
    private String contactNumberText;
    private String address;
    private double homeLatitude;
    private double homeLongitude;
    private final String ACTION_START_TRACKING = "ACTION_START_TRACKING";
    private String NAV_MODE;

    private LoadingActivity loadingActivity;
    private NearestOpenRestaurant restaurant;

    private Retrofit retrofit;
    private MapsApiInterface mapsApiInterface;

    private List<LatLng> directionCoordinates;
    private Button navButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiKey = getString(R.string.GOOGLE_API_KEY);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        retrofit = new Retrofit.Builder().baseUrl("https://maps.googleapis.com/maps/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mapsApiInterface = retrofit.create(MapsApiInterface.class);
        settingsPref = getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        NAV_MODE = getIntent().getStringExtra("NAV_MODE");
        setContentView(R.layout.activity_get_food);
        checkLocationPermission();
    }

    private void startLocationTracker() {
        Intent serviceIntent = new Intent(this, LocationTrackingService.class);
        serviceIntent.putExtra("destinationLatitude", homeLatitude);
        serviceIntent.putExtra("destinationLongitude", homeLongitude);
        serviceIntent.putExtra("userName", userNameText);
        serviceIntent.putExtra("userNumber", userNumberText);
        serviceIntent.putExtra("contactName", contactNameText);
        serviceIntent.putExtra("contactNumber", contactNumberText);

        long maxInactivityTime = settingsPref.getLong("maxInactivityTime", (15 * 60 * 1000));
        serviceIntent.putExtra("maxInactivityTime", maxInactivityTime);

        serviceIntent.setAction(ACTION_START_TRACKING);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            MapNavActivity.this.startForegroundService(serviceIntent);
        }
        else {
            startService(serviceIntent);
        }
    }

    private Observable<Location> getDeviceLocation() {
        return Observable.create(subscriber -> {
            try {
                Location locationResult = null;
                if (locationPermissionGranted) {
                    locationResult = Tasks.await(fusedLocationProviderClient.getLastLocation());
                }
                subscriber.onNext(locationResult);
            }
            catch (SecurityException e) {
                subscriber.onError(e);
            }
        });
    }

    private Observable<NearestOpenRestaurantList> getNearestRestaurant(String apiKey, String latlngString) {
        return Observable.create(subscriber -> {
            try {
                Response<NearestOpenRestaurantList> response = mapsApiInterface.getNearestOpenRestaurant(apiKey, latlngString)
                        .execute();
                if (!response.isSuccessful()) {
                    Log.e(TAG, "ERROR RESPONSE CODE " + response.code());
                    Throwable t = new Throwable("Error occurred with GET request. Response code " + response.code());
                    subscriber.onError(t);
                    return;
                }
                NearestOpenRestaurantList nearestOpenRestaurants = response.body();
                subscriber.onNext(nearestOpenRestaurants);

            } catch (Exception e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        });
    }

    private Observable<List<LatLng>> getWalkingDirections(String apiKey, String originCoords, String destinationCoords) {
        return Observable.create(subscriber -> {
            try {
                Response<NavDirectionsList> response = mapsApiInterface.getDirections(apiKey, originCoords, destinationCoords)
                        .execute();
                if (!response.isSuccessful()) {
                    Log.e(TAG, "ERROR RESPONSE CODE " + response.code());
                    Throwable t = new Throwable("Error occurred while fetching directions. Response code " + response.code());
                    subscriber.onError(t);
                    return;
                }
                NavDirectionsList navDirectionsList = response.body();

                if (navDirectionsList.getDirectionsList().size() == 0) {
                    Throwable t = new Throwable("No walking directions were found to your address.");
                    subscriber.onError(t);
                    return;
                }

                subscriber.onNext(navDirectionsList.getDirectionsList().get(0).getDirectionLatLngs());
            }
            catch (Exception e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            initMap();
        }

        else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch(requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION : {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                    initMap();
                }
                //can't just use request code to check... you need to check the specific permission as well!
                else {
                    builder.setTitle("Location permissions required")
                            .setMessage("Location permissions are required for this feature to work.")
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialog, which) -> stopLoadingAndFinish())
                            .create()
                            .show();
                }
            }
            break;
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION_DENIED : {
                builder.setTitle("Location permissions required")
                        .setMessage("Location permissions are required for this feature to work.")
                        .setCancelable(false)
                        .setPositiveButton("OK", (dialog, which) -> stopLoadingAndFinish())
                        .create()
                        .show();
            }
            break;
        }
    }

    public void initMap() {
        navButton = findViewById(R.id.navigateButton);
        loadingActivity = new LoadingActivity(this);
        loadingActivity.startLoadingDialog();
        switch(NAV_MODE) {
            case "MapNavActivity":
                navButton.setText("Take me to food!");
                initFoodMap();
                break;
            case "GetHome":
                navButton.setText("Take me home!");
                initHomeMap();
                break;
        }
    }

    private void initFoodMap() {
        Observable<Location> locationTask = getDeviceLocation();
        locationTask.subscribeOn(Schedulers.io())
                    .flatMap(new Function<Location, Observable<NearestOpenRestaurantList>>() {
                        @Override
                        public Observable<NearestOpenRestaurantList> apply(Location userLocation) throws Throwable {
                            lastKnownLocation = userLocation;
                            String latlngString = userLocation.getLatitude() + ","
                                    + userLocation.getLongitude();
                            return getNearestRestaurant(apiKey, latlngString);
                        }
                    })
                    .flatMap(new Function<NearestOpenRestaurantList, Observable<List<LatLng>>>() {
                        @Override
                        public Observable<List<LatLng>> apply(NearestOpenRestaurantList nearestOpenRestaurantList) throws Throwable {
                            restaurant = nearestOpenRestaurantList.getList().get(0);

                            String userLocationString = lastKnownLocation.getLatitude() + ","
                                                        + lastKnownLocation.getLongitude();

                            String restaurantLocationString = restaurant.getLatitude() + ","
                                                            + restaurant.getLongitude();

                            return getWalkingDirections(apiKey, userLocationString, restaurantLocationString);
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                        directionCoordinates = result;
                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.getFoodMap);
                        if (mapFragment != null) {
                            mapFragment.getMapAsync(MapNavActivity.this);
                        }
                        loadingActivity.dismissDialog();
                    }, e -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("No walking directions found!")
                                .setMessage(e.getMessage())
                                .setCancelable(false)
                                .setPositiveButton("OK", (dialog, which) -> stopLoadingAndFinish())
                                .create()
                                .show();
                        e.printStackTrace();
                        return;
                    });
    }

    private void initHomeMap() {
        //validate that settings are filled out, address provided is valid
        boolean settingsValidated = validateSettings();
        if (!settingsValidated) {
            return;
        }
        Observable<Location> locationTask = getDeviceLocation();
        locationTask.subscribeOn(Schedulers.io())
                    .flatMap(new Function<Location, Observable<List<LatLng>>>() {
                        @Override
                        public Observable<List<LatLng>> apply(Location location) throws Throwable {
                            lastKnownLocation = location;
                            String userLocationString = lastKnownLocation.getLatitude() + ","
                                                        + lastKnownLocation.getLongitude();
                            String homeLocationString = homeLatitude + "," + homeLongitude;
                            return getWalkingDirections(apiKey, userLocationString, homeLocationString);
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                        directionCoordinates = result;
                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.getFoodMap);
                        if (mapFragment != null) {
                            mapFragment.getMapAsync(MapNavActivity.this);
                        }
                        loadingActivity.dismissDialog();
                    }, e ->  {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("No walking directions found!")
                                .setMessage(e.getMessage())
                                .setCancelable(false)
                                .setPositiveButton("OK", (dialog, which) -> stopLoadingAndFinish())
                                .create()
                                .show();
                        e.printStackTrace();
                        return;
                    });
    }

    private boolean validateSettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        userNameText = settingsPref.getString("userName", "");
        userNumberText = settingsPref.getString("userNumber", "");
        contactNameText = settingsPref.getString("contactName", "");
        contactNumberText = settingsPref.getString("contactNumber", "");
        address = settingsPref.getString("address", "");

        if (userNameText.length() == 0
                || userNumberText.length() == 0
                || contactNameText.length() == 0
                || contactNumberText.length() == 0
                || address.length() == 0) {
            builder.setTitle("Looks like you're missing something!")
                    .setMessage("Please fill out all the settings first.")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, which) -> stopLoadingAndFinish())
                    .create()
                    .show();
            return false;
        }

        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> results = geocoder.getFromLocationName(address, 1);
            if (results.size() > 0) {
                homeLatitude = results.get(0).getLatitude();
                homeLongitude = results.get(0).getLongitude();
            }
            else {
                builder.setTitle("Invalid address detected!")
                        .setMessage("Please provide a valid address in settings.")
                        .setCancelable(false)
                        .setPositiveButton("OK", (dialog, which) -> stopLoadingAndFinish())
                        .create()
                        .show();
                return false;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Function that closes the loading dialog before finishing activity to stop memory leaks
     */
    private void stopLoadingAndFinish() {
        loadingActivity.dismissDialog();
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng currentLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        MarkerOptions userMarkerOptions = new MarkerOptions().position(currentLatLng).title("I am here!");
        mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM));
        mMap.addMarker(userMarkerOptions);
        PolylineOptions polylineOptions = new PolylineOptions().addAll(directionCoordinates);
        mMap.addPolyline(polylineOptions);
        String destinationString;
        switch (NAV_MODE) {
            case "GetHome":
                LatLng homeLatLng = new LatLng(homeLatitude, homeLongitude);
                MarkerOptions homeMarkerOptions = new MarkerOptions()
                                                    .position(homeLatLng)
                                                    .title(address);
                mMap.addMarker(homeMarkerOptions);
                destinationString = homeLatitude + "," + homeLongitude;
                navButton.setOnClickListener(v -> {
                    startLocationTracker();
                    Uri navUri = Uri.parse(String.format("google.navigation:q=%s&mode=w", destinationString));
                    Intent navIntent = new Intent(Intent.ACTION_VIEW, navUri);
                    navIntent.setPackage("com.google.android.apps.maps");
                    startActivity(navIntent);
                });
                break;
            case "MapNavActivity":
                LatLng nearestRestaurantLatLng = new LatLng(restaurant.getLatitude(), restaurant.getLongitude());
                MarkerOptions restaurauntMarkerOptions = new MarkerOptions()
                                                        .position(nearestRestaurantLatLng)
                                                        .title(restaurant.getName());
                mMap.addMarker(restaurauntMarkerOptions);
                destinationString = restaurant.getLatitude() + "," + restaurant.getLongitude();
                navButton.setOnClickListener(v -> {
                    Uri navUri = Uri.parse(String.format("google.navigation:q=%s&mode=w", destinationString));
                    Intent navIntent = new Intent(Intent.ACTION_VIEW, navUri);
                    navIntent.setPackage("com.google.android.apps.maps");
                    startActivity(navIntent);
                });
                break;
        }
    }
}
