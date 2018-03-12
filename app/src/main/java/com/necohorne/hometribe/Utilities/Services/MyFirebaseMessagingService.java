package com.necohorne.hometribe.Utilities.Services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.necohorne.hometribe.Activities.AppActivities.HomeActivity;
import com.necohorne.hometribe.Activities.AppActivities.LoginActivity;
import com.necohorne.hometribe.Activities.AppActivities.MainActivity;
import com.necohorne.hometribe.Activities.AppActivities.OtherUserActivity;
import com.necohorne.hometribe.Activities.AppActivities.SettingsActivity;
import com.necohorne.hometribe.Activities.AppActivities.UserProfileActivity;
import com.necohorne.hometribe.Constants.Constants;
import com.necohorne.hometribe.Models.Home;
import com.necohorne.hometribe.R;
import com.necohorne.hometribe.Utilities.NewIncidentNotification;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.google.maps.android.SphericalUtil.computeDistanceBetween;

/**
 * Created by necoh on 2018/03/05.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessagingServ";
    private static LatLng sHomeLocation;
    private static LatLng sIncidentLocation;

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived( remoteMessage );
        String identifyDataType = remoteMessage.getData().get(getString( R.string.data_type));
            if (identifyDataType.equals(getString(R.string.data_type_incident))){
                String title = remoteMessage.getData().get(getString(R.string.data_title));
                String description = remoteMessage.getData().get(getString(R.string.data_description));
                String reference = remoteMessage.getData().get(getString(R.string.data_reference));
                getIncidentDistance( getApplicationContext(), reference, title, description);
            }
    }

    public void getIncidentDistance(final Context context, String reference, final String title, final String description) {
        SharedPreferences homePrefs = context.getSharedPreferences( Constants.PREFS_HOME, 0 );
        if (homePrefs.contains( Constants.HOME )) {
            Gson gson = new Gson();
            String json = homePrefs.getString( Constants.HOME, "" );
            Home home = gson.fromJson( json, Home.class );
            sHomeLocation = home.getLocation();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference();
            Query query = userRef.child( context.getString( R.string.dbnode_incidents ) ).child( reference );
            query.addListenerForSingleValueEvent( new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Map<String, Object> objectMap = (HashMap<String, Object>) dataSnapshot.getValue();
                        String sLocation = (objectMap.get(context.getString(R.string.field_incident_location )).toString());
                        sIncidentLocation = getLocation(sLocation);
                        String address = getStreetAddress( sIncidentLocation);
                        double distance = distanceFormatting(computeDistanceBetween(sHomeLocation, sIncidentLocation) / 1000);
                        NewIncidentNotification.notify( getApplicationContext(), title, description, distance, address, sIncidentLocation, 1 );
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            } );

        }
    }

    private LatLng getLocation(String location){
        String regex = "\\blongitude=\\b";
        String str1 = location.replaceAll( "[{]", "" );
        String str2 = str1.substring(9);
        String str3 = str2.replaceAll( "[}]", "" );
        String str4 = str3.replaceAll( regex, "" );
        String[] latlong =  str4.split(",");
        double latitude = Double.parseDouble(latlong[0]);
        double longitude = Double.parseDouble(latlong[1]);
        return new LatLng(latitude, longitude);
    }

    private double distanceFormatting(double distanceFromHome) {
        DecimalFormat df = new DecimalFormat("#.##");
        String dx = df.format(distanceFromHome);
        NumberFormat nf = NumberFormat.getInstance();
        double dfH = 0;
        try {
            dfH = nf.parse(dx).doubleValue();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dfH;
    }

    private String getStreetAddress(LatLng location){
        String address = null;
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1);
            if (addressList.size() > 0) {

                if (addressList.get( 0 ).getThoroughfare()!= null) {
                    address = addressList.get( 0 ).getThoroughfare();
                }
            }
        } catch (IOException e) {
                e.printStackTrace();
            }
            return address;
    }

}
