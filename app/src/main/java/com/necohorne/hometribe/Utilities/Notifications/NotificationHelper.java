package com.necohorne.hometribe.Utilities.Notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;

import com.google.android.gms.maps.model.LatLng;
import com.necohorne.hometribe.Activities.AppActivities.MainActivity;
import com.necohorne.hometribe.Activities.AppActivities.PendingNeighbourActivity;
import com.necohorne.hometribe.R;

/**
 * Created by necoh on 2018/03/08.
 */

public class NotificationHelper extends ContextWrapper{
    private NotificationManager mManager;
    public static final String INCIDENT_CHANNEL_ID = "com.necohorne.hometribe.STANDARD";
    public static final String NEIGHBOUR_CHANNEL_ID = "com.necohorne.hometribe.NEIGHBOUR";
    public static final String INCIDENT_CHANNEL_NAME = "INCIDENT CHANNEL";
    public static final String NEIGHBOUR_CHANNEL_NAME = "NEIGHBOUR CHANNEL";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public NotificationHelper(Context base) {
        super(base);
        createChannels();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createChannels() {

        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String strRingtonePreference = preference.getString("notifications_new_message_ringtone", "content://settings/system/notification_sound");
        Uri sound = Uri.parse(strRingtonePreference);

        AudioAttributes attributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build();

        // create android channel
        NotificationChannel androidChannel = new NotificationChannel( INCIDENT_CHANNEL_ID,
                INCIDENT_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        androidChannel.enableLights(true);
        androidChannel.enableVibration(true);
        androidChannel.setLightColor( Color.GREEN);
        androidChannel.setLockscreenVisibility( Notification.VISIBILITY_PRIVATE);
        androidChannel.setSound(sound, attributes);
        getManager().createNotificationChannel(androidChannel);

        // create neighbour channel
        NotificationChannel neighbourChannel = new NotificationChannel(NEIGHBOUR_CHANNEL_ID,
                NEIGHBOUR_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        neighbourChannel.enableLights(true);
        neighbourChannel.enableVibration(true);
        neighbourChannel.setLightColor( Color.GREEN);
        neighbourChannel.setSound(sound, attributes);
        neighbourChannel.setLockscreenVisibility( Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(neighbourChannel);

    }

    public NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getIncidentChannelNotification(String title, String description, double distance, String streetName, LatLng location) {

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra(getString(R.string.notification_location), location.toString() );
        return new Notification.Builder(getApplicationContext(), INCIDENT_CHANNEL_ID )
                .setContentTitle(title)
                .setContentText(description)
                .setLargeIcon(BitmapFactory.decodeResource( getApplicationContext().getResources(), R.mipmap.ic_launcher_round))
                .setContentIntent(
                        PendingIntent.getActivity(
                                getApplicationContext(),
                                0,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT ) )
                .setSmallIcon(R.drawable.ic_report_problem_black_24dp)
                .setStyle( new Notification.BigTextStyle()
                        .bigText("New Incident " + distance +" Km away from Home." + "\n\n" + description)
                        .setBigContentTitle(title)
                        .setSummaryText(streetName))
                .setAutoCancel(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNeighbourChannelNotification(String username, String uid) {

        Intent intent = new Intent(getApplicationContext(), PendingNeighbourActivity.class);
        intent.putExtra(getString(R.string.neighbour_intent_extra), uid );
        return new Notification.Builder(getApplicationContext(), NEIGHBOUR_CHANNEL_ID)
                .setContentTitle("New Neighbour Request")
                .setContentText("you have a new Neighbour Request from " + username)
                .setLargeIcon(BitmapFactory.decodeResource( getApplicationContext().getResources(), R.mipmap.ic_launcher_round))
                .setContentIntent(
                        PendingIntent.getActivity(
                                getApplicationContext(),
                                0,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT ) )
                .setSmallIcon(R.drawable.ic_stat_neighbour_request)
                .setStyle( new Notification.BigTextStyle()
                        .bigText("you have a new Neighbour Request from " + username)
                        .setBigContentTitle("New Neighbour Request"))
                .setAutoCancel(true);
    }
}
