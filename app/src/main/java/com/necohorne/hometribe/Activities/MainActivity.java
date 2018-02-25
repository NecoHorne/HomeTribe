package com.necohorne.hometribe.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.necohorne.hometribe.Activities.Dialog.AddIncidentDialog;
import com.necohorne.hometribe.Activities.Dialog.HomePromptSetup;
import com.necohorne.hometribe.Constants.Constants;
import com.necohorne.hometribe.Models.Home;
import com.necohorne.hometribe.Models.IncidentCrime;
import com.necohorne.hometribe.R;


import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.google.maps.android.SphericalUtil.computeDistanceBetween;
import static com.necohorne.hometribe.Constants.Constants.DISTANCE;
import static com.necohorne.hometribe.Constants.Constants.FIVE_KILOMETERS;
import static com.necohorne.hometribe.Constants.Constants.ONE_MONTH;
import static com.necohorne.hometribe.Constants.Constants.PREFS_DISTANCE;
import static com.necohorne.hometribe.Constants.Constants.PREFS_TIME;
import static com.necohorne.hometribe.Constants.Constants.TIME;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private static final String TAG = "MainActivity";
    private Intent mLogOutIntent;

    //------------SHARED PREFS------------//
    private SharedPreferences mHomePrefs;
    private SharedPreferences distanceSharedPrefs;
    private SharedPreferences timeSharedPrefs;
    boolean prefBool;
    private String mDistancePref;
    private String mTimePref;

    //------------LOCATION OBJECTS------------//
    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng mCurrentLocation;

    //------------LOGIN / AUTH OBJECTS------------//
    private FirebaseAuth mAuth;

    //------------USER PROFILE------------//
    private ImageView profilePicture;
    private TextView userName;
    private FirebaseUser mUser;
    private String mUid;
    private String mName;
    private String mEmail;
    private Uri mPhotoUrl;
    private LatLng mHomeLatLng;
    private ArrayList<IncidentCrime> mCrimeArrayList;

    //------------DATABASE OBJECTS------------//
    private DatabaseReference mDatabaseReference;

    private Date mToday;
    private AddIncidentDialog mIncidentdialog;

    //------------BOTTOM MENU BAR------------//
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.bottom_nav_home:
                    homeLocation();
                    Toast.makeText( MainActivity.this, "Home", Toast.LENGTH_LONG ).show();
                    return true;
                case R.id.bottom_nav_chat:
//                     open chat window drawer
                    return true;
                case R.id.bottom_nav_incident:

                    if (mIncidentdialog != null){
                        mIncidentdialog = null;
                    }
                    mIncidentdialog = new AddIncidentDialog();
                    mIncidentdialog.show(getFragmentManager(), "activity_add_incident_dialog");
                    return true;
            }
            return false;
        }
    };


    //------------ACTIVITY LIFECYCLE------------//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState );
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        //login
        mAuth = FirebaseAuth.getInstance();
        updateUserProfile();

        //options Menu Setup
        settingsPrefsSetUp();

        DrawerLayout drawer = (DrawerLayout) findViewById( R.id.drawer_layout );
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close );
        drawer.addDrawerListener( toggle );
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById( R.id.nav_view );
        navigationView.setNavigationItemSelectedListener( this );


        BottomNavigationView navigation = (BottomNavigationView) findViewById( R.id.bottom_nav_bar );
        navigation.setOnNavigationItemSelectedListener( mOnNavigationItemSelectedListener );

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById( R.id.gps_location_fab);
        floatingActionButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentLocation(mCurrentLocation);
                Toast.makeText( MainActivity.this, "Current Location", Toast.LENGTH_LONG).show();
            }
        } );

        //Shared Preferences
        getPrefs();
        homePromptDialog();
        today();


        //Maps
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapView);
        mapFragment.getMapAsync( MainActivity.this );

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getIncidentLocations();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mDatabaseReference.addValueEventListener(postListener);

        mLogOutIntent = new Intent(MainActivity.this, LoginActivity.class);

    }

    @Override
    protected void onResume() {
        updateUserProfile();
        checkAuthenticationState();
        super.onResume();
    }

    @Override
    protected void onRestart() {
        homePromptDialog();
        getPrefs();
        homeLocation();
        super.onRestart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById( R.id.drawer_layout );
        if (drawer.isDrawerOpen( GravityCompat.START )) {
            drawer.closeDrawer( GravityCompat.START );
        } else {
            super.onBackPressed();
        }
    }

    //------------MENUS------------//
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.main, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent( this, SettingsActivity.class ));
                break;
            case  R.id.incident_settings:
                startActivity( new Intent( this, IncidentSettingsActivity.class));
                break;
            case R.id.menu_log_out:
                logOut();
                break;
            case  R.id.account_settings:
                //TODO
                break;
        }
        return super.onOptionsItemSelected( item );
    }

    private void settingsPrefsSetUp(){
        PreferenceManager.setDefaultValues(this,R.xml.pref_general,false);
        PreferenceManager.setDefaultValues(this,R.xml.pref_notification,false);
        PreferenceManager.setDefaultValues(this,R.xml.pref_data_sync,false);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_home:
                Intent homeIntent = new Intent( MainActivity.this, HomeActivity.class);
                startActivity(homeIntent);
                break;
            case R.id.nav_friends:
                //
                break;
            case R.id.nav_share:
                //
                break;
            case R.id.nav_send:
                //
                break;
            case R.id.test_features:
                //
                break;
            //add more nav menu items
        }
        DrawerLayout drawer = (DrawerLayout) findViewById( R.id.drawer_layout );
        drawer.closeDrawer( GravityCompat.START );
        return true;
    }

    public void getPrefs(){
        mHomePrefs = getSharedPreferences(Constants.PREFS_HOME, 0);
        prefBool = mHomePrefs.contains(Constants.HOME);
        distanceSharedPrefs = getSharedPreferences(PREFS_DISTANCE, 0);
        timeSharedPrefs = getSharedPreferences(PREFS_TIME, 0);
        mDistancePref = distanceSharedPrefs.getString(DISTANCE, FIVE_KILOMETERS);
        mTimePref = timeSharedPrefs.getString(TIME, ONE_MONTH);

        mHomePrefs.registerOnSharedPreferenceChangeListener( new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                getIncidentLocations();
            }
        } );
        distanceSharedPrefs.registerOnSharedPreferenceChangeListener( new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                getIncidentLocations();
            }
        } );
        timeSharedPrefs.registerOnSharedPreferenceChangeListener( new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                getIncidentLocations();
            }
        } );
    }

    //------------PROFILE / LOGIN------------//
    private void logOut() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            FirebaseAuth.getInstance().signOut();
            Toast.makeText( MainActivity.this, "Successfully logged out", Toast.LENGTH_LONG).show();
            startActivity(mLogOutIntent);
            finish();
        }
    }

    private void updateUserProfile(){

        NavigationView navigationView = (NavigationView) findViewById( R.id.nav_view );
        View headerView = navigationView.getHeaderView(0);
        profilePicture = (ImageView) headerView.findViewById( R.id.nav_profile_image );
        userName = (TextView) headerView.findViewById( R.id.nav_profile_name );
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        String provider = mUser.getProviders().toString();

        if (mUser != null){
            if (provider.equals("[firebase.com]")) {
                mUid = mUser.getUid();
                mName = mUser.getDisplayName();
                mEmail = mUser.getEmail();
                mPhotoUrl = mUser.getPhotoUrl();
            }else if (provider.equals( "[facebook.com]" )) {
                for (UserInfo profile : mUser.getProviderData()) {
                    String providerId = profile.getProviderId();
                    mUid = profile.getUid();
                    mName = profile.getDisplayName();
                    mEmail = profile.getEmail();
                    mPhotoUrl = profile.getPhotoUrl();
                }
            }else if (provider.equals( "[google.com]" )){
                for (UserInfo profile : mUser.getProviderData()) {
                    String providerId = profile.getProviderId();
                    mUid = profile.getUid();
                    mName = profile.getDisplayName();
                    mEmail = profile.getEmail();
                    mPhotoUrl = profile.getPhotoUrl();
                }
        }
        profilePicture.setImageURI(mPhotoUrl);
        userName.setText(mName);
        }
    }

    private void checkAuthenticationState(){
        Log.d(TAG, "checkAuthenticationState: checking authentication state.");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null){
            Log.d(TAG, "checkAuthenticationState: user is not Authenticated, Navigating back to Login Activity");
            mLogOutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity( mLogOutIntent);
            finish();
        }else {
            Log.d(TAG, "checkAuthenticationState: user is Authenticated");
        }
    }

    private void setFirebaseUserDetails(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null){
            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                    .setDisplayName("Place Holder") //TODO
                    .setPhotoUri(Uri.parse("http://www.mstrafo.de/fileadmin/_processed_/b/1/csm_person-placeholder-male_5602d73d5e.png"))
                    .build();
            user.updateProfile( profileUpdate).addOnCompleteListener( new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Log.d( TAG, "onComplete: User Profile Updated." );
                        //TODO
                    }else {
                        Log.d( TAG, "onComplete: User Profile Update Unsuccessful." );
                    }
                }
            } );
        }
    }

    private void homePromptDialog(){
        if (!prefBool){
            HomePromptSetup dialog = new HomePromptSetup();
            dialog.show(getFragmentManager(),"fragment_home_prompt");
        }
    }

    //------------MAPS------------//
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        setUpLocation();
        getIncidentLocations();
    }

    private void setUpLocation() {

        locationManager = (LocationManager) this.getSystemService( LOCATION_SERVICE );

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mCurrentLocation = new LatLng( location.getLatitude(), location.getLongitude());
                homeLocation();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        if (ContextCompat.checkSelfPermission( this,
                Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            //Ask User for permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            //locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
        }
    }

    public void currentLocation(LatLng currentLocation){

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(currentLocation.latitude, currentLocation.longitude, 1);

            Bitmap homeMarker = getBitmap(R.drawable.ic_loc_user_3);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(homeMarker, 115, 115, false );
            MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap));

            mMap.addMarker(markerOptions.position(currentLocation).title(addressList.get(0).getAddressLine(0)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void homeLocation(){
        if (prefBool){
            Gson gson = new Gson();
            String json = mHomePrefs.getString(Constants.HOME, "" );
            Home home = gson.fromJson(json, Home.class);
            if (home.getLocation() != null){
                mHomeLatLng = home.getLocation();
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                try {
                    List<Address> addressList = geocoder.getFromLocation(mHomeLatLng.latitude, mHomeLatLng.longitude, 1);
                    Bitmap homeMarker = getBitmap(R.drawable.ic_loc_icon_home);
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(homeMarker, 115, 115, false);
                    MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap));
//                    mMap.clear();
                    mMap.addMarker(markerOptions.position(mHomeLatLng).title("My Home"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mHomeLatLng, 14));
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults );

        if (grantResults.length > 0 && grantResults[0]
                == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }

    //------------FIREBASE DATABASE------------//
    public void getIncidentLocations(){
        mCrimeArrayList = new ArrayList<>();
        if (mCrimeArrayList.size() > 0){
            mCrimeArrayList.clear();
        }
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = mDatabaseReference.child(getString(R.string.dbnode_incidents));
        mMap.clear();
        query.addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d( TAG, "onDataChange: found Incidents" + singleSnapshot.getValue());
                    IncidentCrime incident = new IncidentCrime();
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                    incident.setIncident_type(objectMap.get(getString(R.string.field_incident_type)).toString());

                    String date = objectMap.get(getString( R.string.field_incident_date)).toString();
                    incident.setIncident_date(date);

                    incident.setCountry(objectMap.get(getString(R.string.field_incident_type)).toString());
                    incident.setState_province(objectMap.get(getString(R.string.field_state_province)).toString());
                    incident.setTown(objectMap.get(getString(R.string.field_town)).toString());
                    incident.setStreet_address(objectMap.get(getString(R.string.field_street_address)).toString());

                    String sLocation = (objectMap.get(getString(R.string.field_incident_location)).toString());
                    incident.setIncident_location(getLocation(sLocation));

                    incident.setIncident_description(objectMap.get(getString(R.string.field_incident_description)).toString());
                    incident.setPolice_cas_number(objectMap.get(getString(R.string.field_police_cas_number)).toString());
                    incident.setReported_by(objectMap.get(getString(R.string.field_reported_by)).toString());

                    setUpIncidentMarkers(incident);
                }
                homeLocation();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText( MainActivity.this, "Database Error, please try again later", Toast.LENGTH_SHORT ).show();
            }
        } );
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
        LatLng latLng = new LatLng(latitude, longitude);

        return latLng;
    }

    private void setUpIncidentMarkers(IncidentCrime incident) {

        double distanceFromHome = checkDistance(incident) * 1000;
        today();
        if (incident.getIncident_location() != null){
            if (incident.getIncident_date() != null){
                long days = getDateDiff(convertDate(incident).getTime(), mToday, TimeUnit.DAYS);
                Log.d( TAG, "Time Prefs: " + mTimePref );
                if (days <= Integer.parseInt(mTimePref)){
                    LatLng location = incident.getIncident_location();
                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    try {
                        List<Address> addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1);
                        Bitmap homeMarker = getBitmap(R.drawable.ic_loc_icon_red);
                        Bitmap resizedBitmap = Bitmap.createScaledBitmap(homeMarker, 115, 115, false);
                        MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap));
                        mMap.addMarker(markerOptions.position(location).title(incident.getIncident_type()));
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private double checkDistance(IncidentCrime incident) {
        double distance = 0;
        if (incident.getIncident_location() != null & mHomeLatLng != null){
            distance = computeDistanceBetween(incident.getIncident_location(), mHomeLatLng);
        }
        return distance;
    }

    private Calendar convertDate(IncidentCrime incident) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        try {
            cal.setTime(sdf.parse(incident.getIncident_date()));
            Log.d( TAG, "set date to calander success");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.d( TAG, "onDataChange: Date" + incident.getIncident_date() );

        return cal;
    }

    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }

    public void today(){
        Calendar calendar = Calendar.getInstance();
            mToday = calendar.getTime();
    }
}
