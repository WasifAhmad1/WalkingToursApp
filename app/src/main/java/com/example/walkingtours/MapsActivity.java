package com.example.walkingtours;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.walkingtours.volley.GetFencesVolley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.walkingtours.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static GoogleMap mMap;
    private ActivityMapsBinding binding;
    private final String TAG = getClass().getSimpleName();
    private FusedLocationProviderClient mFusedLocationClient;
    private static final int LOCATION_REQUEST = 111;
    private static final int BACKGROUND_LOCATION_REQUEST = 333;
    private static final float INIT_ZOOM = 17.0f;
    private static int count = 0;
    private static TextView address;
    private static LocationListener locationListener;
    private static ArrayList<LatLng> latLonHistory = new ArrayList<>();
    private static ArrayList<LatLng> latLngList = new ArrayList<>();
    private static ArrayList<Circle> circleList = new ArrayList<>();
    private static ArrayList<Circle> circleCopyList = new ArrayList<>();
    private Polyline llHistoryPolyline;
    private Marker carMarker;
    private Geocoder geocoder;
    private CheckBox checkAddress;
    private CheckBox checkGeoFence;
    private CheckBox checkTravelPath;
    private CheckBox checkTourPath;
    private static NotificationManager nm;

    private static ArrayList<Fence> fences = new ArrayList<Fence>();

    private final List<PatternItem> pattern = Collections.singletonList(new Dot());



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        geocoder = new Geocoder(this);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        address = findViewById(R.id.address);
        checkAddress = findViewById(R.id.checkAddresses);
        checkAddress.setChecked(true);
        checkGeoFence = findViewById(R.id.checkFences);
        checkGeoFence.setChecked(true);
        checkTravelPath = findViewById(R.id.checkTravelPath);
        checkTravelPath.setChecked(true);
        checkTourPath = findViewById(R.id.checkTourPath);
        checkTourPath.setChecked(true);
        hideSystemUI();

         nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);



        //use the location object to get the address

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        if (checkAppPermission()) {
        setupMap();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    public void genList(ArrayList <Fence> fenceList) {
        fences.addAll(fenceList);
        FenceManager fenceManager = FenceManager.getInstance(this);
        fenceManager.makeFences(fences);

        checkGeoFence.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(checkGeoFence.isChecked()){
                    circleList.clear();
                    FenceManager fenceManager = FenceManager.getInstance(MapsActivity.this);
                    fenceManager.makeFences(fences);
                } else{
                    removeFence();
                }
            }
        });

    }

    public void genPolyLine (ArrayList <LatLng> latLngs) {
        latLngList.addAll(latLngs);
        FenceManager.makePolyLines(latLngs, mMap);

        checkTourPath.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(checkTourPath.isChecked()) {
                    FenceManager.makePolyLines(latLngs, mMap);
                } else{
                    FenceManager.clearPolyLines(mMap);

                }
            }
            });



    }

    public void drawFence(double lat, double lon, float radius, String color) {

        int line = Color.parseColor(color);
        int fill = ColorUtils.setAlphaComponent(line, 85);

        LatLng latLng = new LatLng(lat, lon);
        Circle c = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(radius)
                .strokePattern(pattern)
                .strokeColor(line)
                .fillColor(fill));

        circleList.add(c);

    }

    public void removeFence() {
        for (Circle c : circleList){
            c.remove();
        }
    }



    private void setupMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        FenceManager.getInstance(this);
        GetFencesVolley.getFences(this);



        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(true);

        mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f));
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.setBuildingsEnabled(true);
        determineLocation();
        setupListeners();
    }

    @SuppressLint("MissingPermission")
    private void determineLocation() {
        FusedLocationProviderClient mFusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location.
                        if (location != null) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                            Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                            List<Address> addresses;

                            if(checkAddress.isChecked()) {
                                try {
                                    address.setVisibility(View.VISIBLE);
                                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    String number = addresses.get(0).getSubThoroughfare();
                                    String street = addresses.get(0).getThoroughfare();
                                    String city = addresses.get(0).getLocality();
                                    String state = addresses.get(0).getAdminArea();
                                    String zip = addresses.get(0).getPostalCode();
                                    String country = addresses.get(0).getCountryCode();
                                    address.setText(number + " " + street + ", " + city + ", " + state + " " + zip + "," +
                                            country);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else{
                                address.setVisibility(View.INVISIBLE);
                            }

                        }
                    }
                });

        mFusedLocationClient.getLastLocation().addOnFailureListener(this,
                e -> Log.d(TAG, "onFailure: "));


    }

    @SuppressLint("MissingPermission")
    private void setupListeners() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new AppLocationListener(this);

        if (locationManager != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,  1, locationListener);
        }

    }

    public void updateLocation(Location location) {

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        //System.out.println("The latitude is " + location.getLatitude() + " the longitude is " + location.getLongitude());
        //Log.d(TAG, "updateLocation: " + latLng);
        latLonHistory.add(latLng); // Add the LL to our location history
        latLonHistory.size();
        LatLng test = new LatLng(41.887, -87.62);
        //latLonHistory.add(test);
        if (llHistoryPolyline != null) {
            llHistoryPolyline.remove(); // Remove old polyline
        }

        if (latLonHistory.size() == 1) { // First update
            mMap.addMarker(new MarkerOptions().alpha(0.5f).position(test).title("My Origin"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, INIT_ZOOM));
            return;
        }

        if (latLonHistory.size() > 1) { // Second (or more) update

            updatePath();
            LatLng prev = latLonHistory.get(count);
            count++;
            updatePerson(prev, latLng, location.getBearing());
            updateAddress(location);


        }

        String addr = "";
       /* try {
            Address address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1).get(0);
            addr = address.getSubThoroughfare() + " " + address.getThoroughfare();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        //binding.textView.setText(addr);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    private void updateAddress (Location location) {

        Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
        List<Address> addresses;
        if(checkAddress.isChecked()) {
            try {
                address.setVisibility(View.VISIBLE);
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                String number = addresses.get(0).getSubThoroughfare();
                String street = addresses.get(0).getThoroughfare();
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String zip = addresses.get(0).getPostalCode();
                String country = addresses.get(0).getCountryCode();

                address.setText(number + " " + street + " " + city + ", " + state + "\n" + " " + zip + "," +
                            country);



            } catch (IOException e) {
                e.printStackTrace();
            }
        } else{
            address.setVisibility(View.INVISIBLE);
        }

    }

    private void updatePath() {
        if(checkTravelPath.isChecked()) {
            PolylineOptions polylineOptions = new PolylineOptions();


            for (LatLng ll : latLonHistory) {
                polylineOptions.add(ll);
            }


            llHistoryPolyline = mMap.addPolyline(polylineOptions);
            llHistoryPolyline.setEndCap(new RoundCap());
            llHistoryPolyline.setWidth(8);
            llHistoryPolyline.setColor(Color.BLUE);
        }
        checkTravelPath.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!checkTravelPath.isChecked()){
                    //latLonHistory.clear();
                    llHistoryPolyline.remove();
                }
                else{
                    PolylineOptions polylineOptions = new PolylineOptions();


                    for (LatLng ll : latLonHistory) {
                        polylineOptions.add(ll);
                    }


                    llHistoryPolyline = mMap.addPolyline(polylineOptions);
                    llHistoryPolyline.setEndCap(new RoundCap());
                    llHistoryPolyline.setWidth(8);
                    llHistoryPolyline.setColor(Color.BLUE);
                }
            }
        });

    }



    private void updatePerson(LatLng prev, LatLng latLng, float bearing) {
        float r = getRadius();
        if (r > 0) {

            double degrees = getDegrees(latLng.longitude, prev.longitude, latLng.latitude, prev.latitude);

            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_left);
            if(degrees>45.0 && degrees<135.0) {
                icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_up);
            }
            else if (degrees>135.0 && degrees<225.0) {
                icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_right);
            }
            else if (degrees>225.0 && degrees<315.0) {
                icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_down);
            }

            Bitmap resized = Bitmap.createScaledBitmap(icon, (int) r, (int) r, false);

            BitmapDescriptor iconBitmap = BitmapDescriptorFactory.fromBitmap(resized);

            MarkerOptions options = new MarkerOptions();
            options.position(latLng);
            options.icon(iconBitmap);
            options.rotation(bearing);

            if (carMarker != null) {
                carMarker.remove();
            }

            carMarker = mMap.addMarker(options);
        }
    }

    private double getDegrees (double currLong, double prevLong, double currLat, double prevLat) {
        double lonDiff = currLong - prevLong;
        double latDiff = currLat - prevLat;
        double angle = Math.atan2(lonDiff, latDiff);
        double degrees = Math.toDegrees(angle) + 90;
        return degrees;
    }



    private float getRadius() {
        float z = mMap.getCameraPosition().zoom;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        float factor = (float) ((35.0 / 2.0 * z) - (355.0 / 2.0));
        float multiplier = ((7.0f / 7200.0f) * screenWidth) - (1.0f / 20.0f);
        float radius = factor * multiplier;
        return radius;
    }


    private boolean checkAppPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    }, LOCATION_REQUEST);
            return false;
        }


        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST) {
            if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getBackgroundLocPerm();
                } else {
                }
            }

        } else if (requestCode == BACKGROUND_LOCATION_REQUEST) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        setupMap();
                    } else {
                    }
                }
            }
        }
    }

    private void getBackgroundLocPerm() {

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)
            return;

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "NEED BASIC PERMS FIRST!", Toast.LENGTH_LONG).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    BACKGROUND_LOCATION_REQUEST);
            return;
        }


    }
    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                        //| View.SYSTEM_UI_FLAG_FULLSCREEN);
    }


    @Override
    protected void onDestroy() {
        nm.cancelAll();
        super.onDestroy();
        latLonHistory.clear();

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(locationListener);
    }


}