package com.necohorne.hometribe.Utilities;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.maps.model.LatLng;
import com.necohorne.hometribe.Activities.AppActivities.MainActivity;
import com.necohorne.hometribe.R;

public class NewIncidentNotification {

    private static final String NOTIFICATION_TAG = "NewIncident";

    public static void notify(final Context context,
                              final String title,final String description, double distance, String streetName, LatLng location, final int number) {
        final Resources res = context.getResources();
        final Bitmap picture = BitmapFactory.decodeResource( context.getResources(), R.drawable.ic_android_black_24dp);

        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.putExtra(context.getString(R.string.notification_location), location.toString());
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            final NotificationCompat.Builder builder = new NotificationCompat.Builder( context )

                    .setDefaults( Notification.DEFAULT_ALL )
                    .setSmallIcon( R.drawable.ic_report_problem_black_24dp)
                    .setContentTitle( title )
                    .setContentText( description )
                    .setPriority( NotificationCompat.PRIORITY_DEFAULT )
                    .setLargeIcon( picture )
                    //.setTicker( ticker )
                    .setNumber( number )
                    .setContentIntent(
                            PendingIntent.getActivity(
                                    context,
                                    0,
                                    mainIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT ) )
                    .setStyle( new NotificationCompat.BigTextStyle()
                            .bigText("New Incident " + distance +" Km away from Home." + "\n\n" + description)
                            .setBigContentTitle(title)
                            .setSummaryText(streetName))
                    .addAction(
                            R.drawable.ic_action_stat_share,
                            res.getString( R.string.action_share ),
                            PendingIntent.getActivity(
                                    context,
                                    0,
                                    Intent.createChooser( new Intent( Intent.ACTION_SEND )
                                            .setType( "text/plain" )
                                            .putExtra( Intent.EXTRA_TEXT, "Dummy text" ), "Dummy title" ),
                                    PendingIntent.FLAG_UPDATE_CURRENT ) )
                    .setAutoCancel( true );
            notify( context, builder.build() );

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationHelper notificationHelper = new NotificationHelper( context );
            Notification.Builder builder = notificationHelper.getStandardChannelNotification(title, description, distance, streetName, location);
            notificationHelper.getManager().notify(101, builder.build());
        }
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private static void notify(final Context context, final Notification notification) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService( Context.NOTIFICATION_SERVICE );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.notify( NOTIFICATION_TAG, 0, notification );
        } else {
            nm.notify( NOTIFICATION_TAG.hashCode(), notification );
        }
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public static void cancel(final Context context) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService( Context.NOTIFICATION_SERVICE );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.cancel( NOTIFICATION_TAG, 0 );
        } else {
            nm.cancel( NOTIFICATION_TAG.hashCode() );
        }
    }

}
