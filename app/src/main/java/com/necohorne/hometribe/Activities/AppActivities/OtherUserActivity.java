package com.necohorne.hometribe.Activities.AppActivities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.media.Image;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.necohorne.hometribe.Constants.Constants;
import com.necohorne.hometribe.Models.Home;
import com.necohorne.hometribe.Models.UserProfile;
import com.necohorne.hometribe.R;
import com.necohorne.hometribe.Utilities.RecyclerAdapters.SearchRecyclerAdapter;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.google.maps.android.SphericalUtil.computeDistanceBetween;

public class OtherUserActivity extends AppCompatActivity {

    private static final String TAG = "OtherUserActivity";
    public static boolean isActivityRunning;

    private TextView displayName;
    private TextView homeTown;
    private ImageView profilePicture;
    private ImageButton addButton;
    private boolean prefBool;
    private SharedPreferences mHomePrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_other_user );
        mHomePrefs = getSharedPreferences(Constants.PREFS_HOME, 0);
        prefBool = mHomePrefs.contains(Constants.HOME);
        setUpUi();
    }

    @Override
    protected void onStart() {
        isActivityRunning = true;
        super.onStart();
    }

    @Override
    protected void onStop() {
        isActivityRunning = false;
        super.onStop();
    }

    private void setUpUi() {
        //get intent extras from either infowindow or chat username clicks.
        Intent intent = getIntent();
        final String uid = intent.getStringExtra( getString( R.string.field_other_uid));

        displayName = (TextView) findViewById(R.id.other_user_name);
        homeTown = (TextView) findViewById(R.id.other_town);
        profilePicture = (ImageView) findViewById(R.id.other_user_picture);
        addButton = (ImageButton) findViewById( R.id.other_user_add );

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference();
        Query query = userRef.child(getString(R.string.dbnode_user)).child(uid);
        Log.d( TAG, "Marker Query: "+ query);
        query.addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Log.d( TAG, "Marker Datasnapshot" );
                    Map<String, Object> objectMap = (HashMap<String, Object>) dataSnapshot.getValue();
                    String userName = (String) objectMap.get("user_name");
                    displayName.setText(userName);
                    String town = objectMap.get("home_location").toString();
                    LatLng location = getLocation( town );
                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    checkForNeighbourRequest(uid, location);
                    try {
                        List<Address> addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1 );
                        if (addressList.size() > 0){
                            Address addr = addressList.get(0);
                            String geoTown = addr.getSubLocality();
                            homeTown.setText(geoTown);
                        }
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                    try {
                        String url = (String) objectMap.get( "profile_image" );
                        if (!url.equals("content://com.android.providers.media.documents/document/image%3A45")
                                && !url.equals("null")){
                            Uri photo = Uri.parse(url);
                            Picasso.with(OtherUserActivity.this)
                                    .load(photo)
                                    .into( profilePicture );
                        }else {
                            Bitmap bitmap = getBitmap( R.mipmap.ic_launcher_foreground_icon);
                            profilePicture.setImageBitmap(bitmap);
                        }
                    } catch (NullPointerException e){
                        Log.d( TAG, "no profile picture set " + e.toString());
                        Bitmap bitmap = getBitmap( R.mipmap.ic_launcher_foreground_icon);
                        profilePicture.setImageBitmap(bitmap);
                    }
                        }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
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

    private Bitmap getBitmap(int drawableRes) {
        Drawable drawable = getResources().getDrawable(drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private void requestAlertDialog(final String uid) {
        //setup the dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( OtherUserActivity.this);
        LayoutInflater inflater = (LayoutInflater) OtherUserActivity.this.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View view = inflater.inflate(R.layout.neighbour_dialog_popup, null);
        dialogBuilder.setView(view);
        final AlertDialog alertDialog = dialogBuilder.create();

        //ui elements
        Button yesButton = view.findViewById( R.id.neighbour_alert_yes);
        Button noButton = view.findViewById(R.id.neighbour_alert_no);

        alertDialog.show();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable( Color.TRANSPARENT));

        yesButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                neighbourRequest(uid);
                addButton.setVisibility(View.INVISIBLE);
                alertDialog.dismiss();
            }
        } );

        noButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        } );

    }

    private void neighbourRequest(String uid) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference sendReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(getString(R.string.dbnode_user))
                .child(user.getUid())
                .child(getString( R.string.dbnode_sent_neighbour_requests))
                .child("user_id")
                .child(uid);
        sendReference.setValue(uid);

        DatabaseReference requestReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(getString(R.string.dbnode_user))
                .child(uid)
                .child(getString(R.string.dbnode_neighbour_requests))
                .child("user_id")
                .child(user.getUid());
        requestReference.setValue(user.getUid());

        Toast.makeText(OtherUserActivity.this, "Neighbour Request Sent!", Toast.LENGTH_SHORT).show();
    }

    private void checkForNeighbourRequest(final String uid, final LatLng location){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (uid.equals( user.getUid() )){
            addButton.setVisibility(View.INVISIBLE);
        } else {
            DatabaseReference requestReference = FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child(getString(R.string.dbnode_user))
                    .child(user.getUid())
                    .child(getString(R.string.dbnode_sent_neighbour_requests))
                    .child("user_id")
                    .child(uid);

            final DatabaseReference neighboursRef = FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child(getString(R.string.dbnode_user))
                    .child(user.getUid())
                    .child(getString(R.string.dbnode_neighbours))
                    .child("user_id")
                    .child(uid);

            Query requestQuery = requestReference;
            requestQuery.addListenerForSingleValueEvent( new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        if (dataSnapshot.getValue().equals(uid)) {
                            addButton.setVisibility( View.INVISIBLE );
                        }else {
                            addButton.setOnClickListener( new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (checkDistance(location) < 4){
                                        requestAlertDialog(uid);
                                    } else {
                                        Toast.makeText( OtherUserActivity.this, "Sorry this user lives too far to be added as a neighbour.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } );
                        }
                    }else {
                        Query neighbourQuery = neighboursRef;
                        neighbourQuery.addListenerForSingleValueEvent( new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    addButton.setVisibility( View.INVISIBLE );
                                } else {
                                    addButton.setOnClickListener( new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (checkDistance(location) < 4){
                                                requestAlertDialog(uid);
                                            } else {
                                                Toast.makeText( OtherUserActivity.this, "Sorry this user lives too far to be added as a neighbour.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    } );
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        } );
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            } );
        }
    }

    private double checkDistance(LatLng location) {
        double distance = 0;
        if (location != null & getHomeLatLng() != null){
            distance = computeDistanceBetween(location, getHomeLatLng());
        }
        return distance / 1000;
    }

    private LatLng getHomeLatLng() {
        LatLng homeLatLng = null;
        if (prefBool) {
            Gson gson = new Gson();
            String json = mHomePrefs.getString( Constants.HOME, "" );
            Home mHome = gson.fromJson( json, Home.class );
            if (mHome.getLocation() != null) {
                homeLatLng = mHome.getLocation();
            }
        }
        return homeLatLng;
    }
}
