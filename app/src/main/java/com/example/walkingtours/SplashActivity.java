package com.example.walkingtours;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;

import java.util.Objects;


public class SplashActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private static final int SPLASH_TIME_OUT = 3000;
    private static final int LOCATION_REQUEST = 111;
    private static final int BACKGROUND_LOCATION_REQUEST = 333;
    private static final int ACCURACY_REQUEST = 222;
    private static String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};
    final static int PERMISSIONS_ALL = 1;
    private static AlertDialog.Builder builder;
    private static AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setTitle("Walking Tours");
        genActivity();



    }

    /*@Override
    protected void onResume() {
        super.onResume();
        if (checkAppPermission()) {
            genActivity();
        }
    } */


    public void getAccuracy () {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        LocationSettingsRequest lsr = builder.build();

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(lsr);

        task.addOnSuccessListener(this, locationSettingsResponse -> {
            showServicesDialog(locationSettingsResponse);

        });

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // The next line will cause a pop-up to display asking you if
                    // it is ok to turn on google's location services if they are
                    // not already running)
                    //
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(SplashActivity.this, ACCURACY_REQUEST);
                } catch (IntentSender.SendIntentException sendEx) {
                    sendEx.printStackTrace();
                }
            }
        });
    }

    public void genActivity() {
        //requestPermissions(PERMISSIONS,PERMISSIONS_ALL);


        if (!checkPermission()) {
            builder = new AlertDialog.Builder(this);

            builder.setTitle("The app is opening now");

            builder.setTitle("App Required Permissions!");
            builder.setMessage("This app cannot be used without fine location access and background Location access. \n\n" +
                    "Please enable these permissions so that you can use this app. \n\n" +
                    "Click ok to exit the app");

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });

            dialog = builder.create();
            dialog.show();
        } else {
            init();
            progressBar.setVisibility(View.VISIBLE);
            getAccuracy();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }, 10000);

            new Handler().postDelayed(() -> {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i =
                        new Intent(SplashActivity.this, MapsActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out); // new act, old act
                // close this activity
                finish();
            }, SPLASH_TIME_OUT);
        }
    }


    private void init() {
        this.progressBar = findViewById(R.id.progressBar);
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
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
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dialog.dismiss();
                getBackgroundLocPerm();
            } else {
                //finish();

            }

        } else if (requestCode == BACKGROUND_LOCATION_REQUEST) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        init();
                        progressBar.setVisibility(View.VISIBLE);
                        getAccuracy();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.VISIBLE);
                            }
                        }, 10000);

                        new Handler().postDelayed(() -> {
                            // This method will be executed once the timer is over
                            // Start your app main activity
                            Intent a =
                                    new Intent(SplashActivity.this, MapsActivity.class);
                            startActivity(a);
                            overridePendingTransition(R.anim.slide_in, R.anim.slide_out); // new act, old act
                            // close this activity
                            finish();
                        }, SPLASH_TIME_OUT);
                    } else{
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);

                        builder2.setTitle("App Required Permissions!");
                        builder2.setMessage("This app cannot be used without fine location access and background Location access. \n\n" +
                                "Please enable these permissions so that you can use this app. \n\n" +
                                "Click ok to exit the app");

                        builder2.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });

                        AlertDialog dialog2 = builder2.create();
                        dialog2.show();

                    }
                }
            }
        }







    }
    private void askPermission () {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
    }

    private void getBackgroundLocPerm() {

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)
            return;

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);


        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    BACKGROUND_LOCATION_REQUEST);
        }

        //ContextCompat.checkSelfPermission(this, Manifest.permission


    }

    private void showServicesDialog(LocationSettingsResponse locationSettingsResponse) {
        String s = "Services:\n" +
                "\tBluetooth Low Energy (BLE):  " + Objects.requireNonNull(locationSettingsResponse.getLocationSettingsStates()).isBleUsable() + "\n" +
                "\tGlobal Positioning System (GPS):  " + locationSettingsResponse.getLocationSettingsStates().isGpsUsable() + "\n" +
                "\tNetwork Location:  " + locationSettingsResponse.getLocationSettingsStates().isNetworkLocationUsable() + "\n" +
                "\tLocation Usable:  " + locationSettingsResponse.getLocationSettingsStates().isLocationUsable() + "\n";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Accuracy Check is a Success!");
        builder.setMessage(s)
                .setPositiveButton("OK", (dialog, id) -> {
                });
        builder.create().show();
    }


    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
}