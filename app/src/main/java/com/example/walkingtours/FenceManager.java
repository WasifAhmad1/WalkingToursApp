package com.example.walkingtours;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FenceManager {

    private static String TAG = FenceManager.class.getSimpleName();
    private static FenceManager instance;
    private final MapsActivity activity;
    private final GeofencingClient geofencingClient;
    private static Polyline llHistoryPolyline;
    private  PendingIntent geofencePendingIntent;
    private static HashMap<String, String> addressCopy = new HashMap<>();
    private static HashMap<String, String> descCopy = new HashMap<>();
    private static HashMap<String, String> urlCopy = new HashMap<>();


    public static FenceManager getInstance(MapsActivity a) {
        if (instance == null)
            instance = new FenceManager(a);
        return instance;
    }

    private FenceManager(MapsActivity activity) {
        this.activity = activity;
        geofencingClient = LocationServices.getGeofencingClient(activity);


        geofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener(activity,
                        aVoid -> Log.d(TAG, "onSuccess: removeGeofences"))
                .addOnFailureListener(activity,
                        e -> Log.d(TAG, "onFailure: removeGeofences"));



    }

    public static void makePolyLines (ArrayList<LatLng> latLngs, GoogleMap mMap) {
        PolylineOptions polylineOptions = new PolylineOptions();
        for (LatLng ll : latLngs) {
            polylineOptions.add(ll);
        }

        llHistoryPolyline = mMap.addPolyline(polylineOptions);
        llHistoryPolyline.setEndCap(new RoundCap());
        llHistoryPolyline.setWidth(8);
        llHistoryPolyline.setColor(Color.parseColor("#FFA500"));

    }

    public static void clearPolyLines (GoogleMap mMap) {
        llHistoryPolyline.remove();
    }




    @SuppressLint("MissingPermission")
    public void makeFences(ArrayList<Fence> fences) {
        Map<String, String> addressFences = new HashMap<>();
        Map<String, String> descriptionFences = new HashMap<>();
        Map<String, String> urlFences = new HashMap<>();
        for (Fence fence : fences) {
            Geofence geofence = new Geofence.Builder()
                    .setRequestId(fence.getId())
                    .setCircularRegion(fence.getLat(), fence.getLongitude(), fence.getRadius())
                    .setNotificationResponsiveness(1000)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .build();


            GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                    .addGeofence(geofence)
                    .build();
            addressFences.put(fence.getId(), fence.getAddress());
            descriptionFences.put(fence.getId(), fence.getDescription());
            urlFences.put(fence.getId(), fence.getUrl());
            addressCopy.put(fence.getId(), fence.getAddress());
            descCopy.put(fence.getId(), fence.getDescription());
            urlCopy.put(fence.getId(),fence.getUrl());

            geofencePendingIntent = getGeofencePendingIntent();


            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            geofencingClient
                    .addGeofences(geofencingRequest, geofencePendingIntent)
                    .addOnSuccessListener(aVoid ->
                            Log.d(TAG, "****onSuccess: "))
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        Log.d(TAG, "*****onFailure: ");
                    });

            activity.drawFence(fence.getLat(), fence.getLongitude(), fence.getRadius(), fence.getFenceColor());
        }

    }

    public static String getAddressDetails(String id) {
        return addressCopy.get(id);
    }
    public static String getDescDetails(String id) {
        return descCopy.get(id);
    }
    public static String getUrlDetails(String id) {
        return urlCopy.get(id);
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(activity, GeofenceReceiver.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(activity, 0, intent, PendingIntent.FLAG_MUTABLE |
                PendingIntent.FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }
}
