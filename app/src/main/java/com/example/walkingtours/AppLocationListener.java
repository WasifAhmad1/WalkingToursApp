package com.example.walkingtours;

import android.location.Location;
import android.location.LocationListener;

import androidx.annotation.NonNull;

import java.util.List;

public class AppLocationListener implements LocationListener {

    private final MapsActivity mapsActivity;

    public AppLocationListener(MapsActivity mapsActivity) {
        this.mapsActivity = mapsActivity;
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        mapsActivity.updateLocation(location);

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
    }
}
