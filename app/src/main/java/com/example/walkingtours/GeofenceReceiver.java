package com.example.walkingtours;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.squareup.picasso.BuildConfig;

import java.util.List;
import java.util.Map;

public class GeofenceReceiver extends BroadcastReceiver {
    private final String TAG = getClass().getSimpleName();
    private static final String NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel";
    @Override
    public void onReceive(Context context, Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Error: " + geofencingEvent.getErrorCode());
            return;
        }
        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();



        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            for (Geofence g : triggeringGeofences) {
                Log.d(TAG, "*********onReceive: " + g.getRequestId() + ", " + geofenceTransition);
                String address = FenceManager.getAddressDetails(g.getRequestId());
                String desc = FenceManager.getDescDetails(g.getRequestId());
                String url = FenceManager.getUrlDetails(g.getRequestId());
                sendNotification(context, g.getRequestId(), geofenceTransition,
                        address, desc, url);

            }
        } else {
            // Log the error.
            Log.d(TAG, "onReceive: NOT ENTER OR EXIT");
        }
    }

    //////


    public void sendNotification(Context context, String id, int transitionType,
                                 String address, String description, String url) {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        //Uri sound2 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        if (notificationManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();

            String name = context.getString(R.string.app_name);

            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setSound(Uri.parse("android.resource://"+context.getPackageName()+"/"+R.raw.notif_sound), audioAttributes);

            notificationManager.createNotificationChannel(channel);
        }

        String transitionString;
        if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) transitionString = "Welcome!";
        else transitionString = "Goodbye!";


        Intent resultIntent = new Intent(context.getApplicationContext(), FenceInfoActivity.class);
        resultIntent.putExtra("Address", address);
        resultIntent.putExtra("Desc", description);
        resultIntent.putExtra("Url", url);
        resultIntent.putExtra("ID", id);

        PendingIntent pi = PendingIntent.getActivity(
                context.getApplicationContext(), 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);


        Notification notification = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentIntent(pi)
                .setSmallIcon(R.drawable.fence_notif)
                .setContentTitle(id) // Bold title
                .setSubText(id) // small text at top left
                .setContentText(id + " (Tap to See Details")
                .setContentText(address)
                .setVibrate(new long[] {1, 1, 1})
                .setAutoCancel(true)
                .setLights(0xff0000ff, 300, 1000) // blue color
                .setSound(Uri.parse("android.resource://"+context.getPackageName()+"/"+R.raw.notif_sound))
                .build();

        if (notificationManager != null) {
            notificationManager.notify(getUniqueId(), notification);
        }
    }

    private static int getUniqueId() {
        return(int) (System.currentTimeMillis() % 10000);
    }

    }

