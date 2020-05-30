package com.lujustin.hammrd;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.View;
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
import com.lujustin.hammrd.models.MapsApiService;
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

public class GetFood extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;

    private boolean locationPermissionGranted;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;

    //store location in activity state
    private static final String TAG = "GetFood";
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
    private double destinationLatitude;
    private double destinationLongitude;
    private final String ACTION_START_TRACKING = "ACTION_START_TRACKING";

    private LoadingActivity loadingActivity;
    private NearestOpenRestaurant restaurant;

    private Retrofit retrofit;
    private MapsApiService mapsApiService;

    private List<LatLng> directionCoordinates;

    //maybe have some sort of parameter to indicate whether you want to gethome, or getfood? that way we reduce repetition
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsPref = getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        apiKey = getString(R.string.GOOGLE_API_KEY);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        retrofit = new Retrofit.Builder().baseUrl("https://maps.googleapis.com/maps/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mapsApiService = retrofit.create(MapsApiService.class);

        setContentView(R.layout.activity_get_food);
        getLocationPermission();
    }

    private void startLocationTracker() {
        Intent serviceIntent = new Intent(this, LocationTrackingService.class);
        serviceIntent.putExtra("destinationLatitude", destinationLatitude);
        serviceIntent.putExtra("destinationLongitude", destinationLongitude);
        serviceIntent.putExtra("userName", userNameText);
        serviceIntent.putExtra("userNumber", userNumberText);
        serviceIntent.putExtra("contactName", contactNameText);
        serviceIntent.putExtra("contactNumber", contactNumberText);
        serviceIntent.setAction(ACTION_START_TRACKING);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            GetFood.this.startForegroundService(serviceIntent);
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
                Response<NearestOpenRestaurantList> response = mapsApiService.getNearestOpenRestaurant(apiKey, latlngString)
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
                Response<NavDirectionsList> response = mapsApiService.getDirections(apiKey, originCoords, destinationCoords)
                        .execute();
                if (!response.isSuccessful()) {
                    Log.e(TAG, "ERROR RESPONSE CODE " + response.code());
                    Throwable t = new Throwable("Error occured with GET request. Response code " + response.code());
                    subscriber.onError(t);
                    return;
                }
                NavDirectionsList navDirectionsList = response.body();
                subscriber.onNext(navDirectionsList.getDirectionsList().get(0).getDirectionLatLngs());
            }
            catch (Exception e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        });
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            initFoodMap();
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
        switch(requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION : {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                    initFoodMap();
                }
                //can't just use request code to check... you need to check the specific permission as well!
                else {
                    this.finish();
                }
            }
            break;
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION_DENIED : {
                this.finish();
            }
            break;
        }
    }

    private void initFoodMap() {
        //validate that settings are filled out, address provided is valid
        boolean settingsValidated = validateSettings();
        if (!settingsValidated) {
            return;
        }
        loadingActivity = new LoadingActivity(this);
        loadingActivity.startLoadingDialog();
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
                mapFragment.getMapAsync(GetFood.this);
            }
            loadingActivity.dismissDialog();
        }, e -> e.printStackTrace());
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
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .create()
                    .show();
            return false;
        }

        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> results = geocoder.getFromLocationName(address, 1);
            if (results.size() > 0) {
                destinationLatitude = results.get(0).getLatitude();
                destinationLongitude = results.get(0).getLongitude();
            }
            else {
                builder.setTitle("Invalid address detected!")
                        .setMessage("Please provide a valid address in settings.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng currentLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        LatLng nearestRestaurantLatLng = new LatLng(restaurant.getLatitude(), restaurant.getLongitude());
        MarkerOptions userMarkerOptions = new MarkerOptions().position(currentLatLng).title("I am here!");
        MarkerOptions restaurauntMarkerOptions = new MarkerOptions().position(nearestRestaurantLatLng).title(restaurant.getName());
        mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM));
        mMap.addMarker(userMarkerOptions);
        mMap.addMarker(restaurauntMarkerOptions);
        PolylineOptions polylineOptions = new PolylineOptions().addAll(directionCoordinates);
        mMap.addPolyline(polylineOptions);

        Button navButton = findViewById(R.id.navigateButton);
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocationTracker();
                String destinationString = restaurant.getLatitude() + "," +
                                            restaurant.getLongitude();
                Uri navUri = Uri.parse(String.format("google.navigation:q=%s&mode=w", destinationString));
                Intent navIntent = new Intent(Intent.ACTION_VIEW, navUri);
                navIntent.setPackage("com.google.android.apps.maps");
                startActivity(navIntent);
            }
        });
    }
}
