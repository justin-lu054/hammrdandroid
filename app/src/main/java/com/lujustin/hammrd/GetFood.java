package com.lujustin.hammrd;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.tasks.Tasks;

import com.lujustin.hammrd.models.NearestOpenRestaurant;
import com.lujustin.hammrd.models.NearestOpenRestaurantList;
import com.lujustin.hammrd.models.NearestOpenRestaurantService;

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

    private static final int DEFAULT_ZOOM = 12;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION_DENIED = 0;

    private LoadingActivity loadingActivity;
    private NearestOpenRestaurant restaurant;

    //CURRENT IDEA!
    //have each required location related or api call related service return a task....
    //and when all tasks are done (using Task.all) then we getMapAsync


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadingActivity = new LoadingActivity(this);
        loadingActivity.startLoadingDialog();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        setContentView(R.layout.activity_get_food);
        getLocationPermission();
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
            Retrofit retrofit = new Retrofit.Builder().baseUrl("https://maps.googleapis.com/maps/api/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            NearestOpenRestaurantService nearestOpenRestaurantService =
                    retrofit.create(NearestOpenRestaurantService.class);

            Log.d(TAG, latlngString);
            try {
                Response<NearestOpenRestaurantList> response = nearestOpenRestaurantService.getNearestOpenRestaurant(apiKey, latlngString)
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
                subscriber.onError(e);
            }
        });
    }

    private void getLocationPermission() {
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
        switch(requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION : {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                    initMap();
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

    private void initMap() {
        Observable<Location> locationTask = getDeviceLocation();
        Log.d(TAG, "initMap called");
        locationTask.subscribeOn(Schedulers.io())
                    .flatMap(new Function<Location, Observable<NearestOpenRestaurantList>>() {
            @Override
            public Observable<NearestOpenRestaurantList> apply(Location userLocation) throws Throwable {
                Log.d(TAG, "apply called");
                lastKnownLocation = userLocation;
                String apiKey = getString(R.string.GOOGLE_API_KEY);
                String latlngString = Double.toString(userLocation.getLatitude()) + ","
                        + Double.toString(userLocation.getLongitude());
                return getNearestRestaurant(apiKey, latlngString);
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
        .subscribe(nearestOpenRestaurantList -> {
            Log.d(TAG, "restaurantquery completed");
            restaurant = nearestOpenRestaurantList.getList().get(0);
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.getFoodMap);

            if (mapFragment != null) {
                mapFragment.getMapAsync(GetFood.this);
            }
            loadingActivity.dismissDialog();
        }, e -> e.printStackTrace());

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

    }
}
