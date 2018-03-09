package com.necohorne.hometribe.Utilities;

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
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.necohorne.hometribe.Activities.AppActivities.MainActivity;
import com.necohorne.hometribe.Constants.Constants;
import com.necohorne.hometribe.Models.Home;
import com.necohorne.hometribe.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static com.google.maps.android.SphericalUtil.computeDistanceBetween;

/**
 * Created by necoh on 2018/03/08.
 */

public class NotificationHelper extends ContextWrapper{
    private NotificationManager mManager;
    public static final String ANDROID_CHANNEL_ID = "com.necohorne.hometribe.STANDARD";
    public static final String ANDROID_CHANNEL_NAME = "STANDARD CHANNEL";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public NotificationHelper(Context base) {
        super(base);
        createChannels();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createChannels() {

        // create android channel
        NotificationChannel androidChannel = new NotificationChannel(ANDROID_CHANNEL_ID,
                ANDROID_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        androidChannel.enableLights(true);
        androidChannel.enableVibration(true);
        androidChannel.setLightColor( Color.GREEN);
        androidChannel.setLockscreenVisibility( Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(androidChannel);

    }

    public NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getStandardChannelNotification(String title, String description, double distance, String streetName) {
        return new Notification.Builder(getApplicationContext(), ANDROID_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(description)
                .setLargeIcon(BitmapFactory.decodeResource( getApplicationContext().getResources(), R.drawable.ic_android_black_24dp))
                .setContentIntent(
                        PendingIntent.getActivity(
                                getApplicationContext(),
                                0,
                                new Intent( getApplicationContext(),MainActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT ) )
                .setSmallIcon(R.drawable.ic_report_problem_black_24dp)
                .setStyle( new Notification.BigTextStyle()
                        .bigText("New Incident " + distance +" Km away from Home." + "\n\n" + description)
                        .setBigContentTitle(title)
                        .setSummaryText(streetName))
                .setAutoCancel(true);
    }
}
