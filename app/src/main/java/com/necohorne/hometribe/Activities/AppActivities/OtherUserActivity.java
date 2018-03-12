package com.necohorne.hometribe.Activities.AppActivities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.necohorne.hometribe.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OtherUserActivity extends AppCompatActivity {

    private static final String TAG = "OtherUserActivity";
    public static boolean isActivityRunning;

    private TextView displayName;
    private TextView homeTown;
    private ImageView profilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_other_user );
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
        displayName = (TextView) findViewById(R.id.other_user_name);
        homeTown = (TextView) findViewById(R.id.other_town);
        profilePicture = (ImageView) findViewById(R.id.other_user_picture);

        //get intent extras from either infowindow or chat username clicks.
        Intent intent = getIntent();
        String uid = intent.getStringExtra( getString( R.string.field_other_uid));
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
}
