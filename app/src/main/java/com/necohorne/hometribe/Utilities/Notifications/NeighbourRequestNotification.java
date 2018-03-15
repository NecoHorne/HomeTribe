package com.necohorne.hometribe.Utilities.Notifications;

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

import com.necohorne.hometribe.Activities.AppActivities.PendingNeighbourActivity;
import com.necohorne.hometribe.R;

public class NeighbourRequestNotification {


    private static final String NOTIFICATION_TAG = "NeighbourRequest";

    public static void notify(final Context context, final String username, final String uid, final int number) {
        final Resources res = context.getResources();

        // This image is used as the notification's large icon (thumbnail).
        // TODO: Remove this if your notification has no relevant thumbnail.
        final Bitmap picture = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round);
        Intent neighbourIntent = new Intent( context, PendingNeighbourActivity.class);
        neighbourIntent.putExtra( context.getString( R.string.neighbour_intent_extra ), uid );

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            final NotificationCompat.Builder builder = new NotificationCompat.Builder( context )
                    .setDefaults( Notification.DEFAULT_ALL )
                    .setSmallIcon( R.drawable.ic_stat_neighbour_request )
                    .setContentTitle( "New Neighbour Request" )
                    .setContentText( "you have a new Neighbour Request from " + username )
                    .setPriority( NotificationCompat.PRIORITY_DEFAULT )
                    .setLargeIcon( picture )
                    .setNumber( number )
                    .setContentIntent(
                            PendingIntent.getActivity(
                                    context,
                                    0,
                                    neighbourIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT ) )
                    .setStyle( new NotificationCompat.BigTextStyle()
                            .bigText("you have a new Neighbour Request from " + username )
                            .setBigContentTitle("New Neighbour Request"))
                    .setAutoCancel( true );
            notify( context, builder.build() );
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationHelper notificationHelper = new NotificationHelper( context );
            Notification.Builder builder = notificationHelper.getNeighbourChannelNotification(username, uid);
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
