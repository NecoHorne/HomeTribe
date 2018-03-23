package com.necohorne.hometribe.Activities.AppActivities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.appinvite.FirebaseAppInvite;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.necohorne.hometribe.Activities.Chat.ChatFragment;
import com.necohorne.hometribe.Activities.Dialog.AddIncidentDialog;
import com.necohorne.hometribe.Activities.Dialog.CustomInfoWindow;
import com.necohorne.hometribe.Activities.Dialog.DeleteIncidentDialog;
import com.necohorne.hometribe.Activities.Dialog.HomePromptSetup;
import com.necohorne.hometribe.Activities.Dialog.MapClickAddIncident;
import com.necohorne.hometribe.Constants.Constants;
import com.necohorne.hometribe.Models.Home;
import com.necohorne.hometribe.Models.IncidentCrime;
import com.necohorne.hometribe.Models.UserProfile;
import com.necohorne.hometribe.R;
import com.squareup.picasso.Picasso;


import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener {

    private static final String TAG = "MainActivity";
    private static final float DEFAULT_OUTLINE_WIDTH_DP = 30;
    private float mDefaultSize;
    private Intent mLogOutIntent;
    public static boolean isActivityRunning;
    private static final int REQUEST_INVITE = 0;
    private AdView mAdView;

    //------------SHARED PREFS------------//
    private SharedPreferences mHomePrefs;
    boolean prefBool;

    //------------LOCATION OBJECTS------------//
    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng mCurrentLocation;

    //------------LOGIN / AUTH OBJECTS------------//
    private FirebaseAuth mAuth;
    private String mName;
    private Uri mPhotoUrl;
    private LatLng mHomeLatLng;
    private UserProfile mUserProfile;
    private Home mHome;

    //------------DATABASE OBJECTS------------//
    private DatabaseReference mDatabaseReference;
    private Date mToday;
    private AddIncidentDialog mIncidentDialog;

    //------------BOTTOM MENU BAR------------//
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.bottom_nav_home:
                    goHome();
                    return true;
                case R.id.bottom_nav_chat:
                    openChatWindow();
                    return true;
                case R.id.bottom_nav_incident:
                    addIncidentDialog();
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

        //login
        mAuth = FirebaseAuth.getInstance();
        updateUserProfile();
        dynamicLinkCreate();

        //options Menu Setup
        settingsPrefsSetUp();
        settingsSetup();

        //Shared Preferences
        getPrefs();
        homePromptDialog();
        today();

        //Maps
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapView);
        mapFragment.getMapAsync( MainActivity.this );

        setupDatabase();
        mLogOutIntent = new Intent(MainActivity.this, LoginActivity.class);
        displayMetrics();

        addMobSetup();
    }

    @Override
    protected void onStart() {
        isActivityRunning = true;
        super.onStart();
    }

    @Override
    protected void onResume() {
        updateUserProfile();
        checkAuthenticationState();
        super.onResume();
    }

    @Override
    protected void onRestart() {
        getPrefs();
        homePromptDialog();
        redrawMap();
        uploadUser();
        super.onRestart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        isActivityRunning = false;
        super.onStop();
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

    private void redrawMap(){
        settingsPrefsSetUp();
        mMap.clear();
        boolean success = mMap.setMapStyle( MapStyleOptions.loadRawResourceStyle( getApplicationContext(), getMapStyle()));
        if (!success) {
            Log.e(TAG, "Style parsing failed.");
        }
        setUpLocation();
        homeLocation();
        getIncidentLocations();
    }

    public void addIncidentDialog(){
        if (mIncidentDialog != null){
            mIncidentDialog.setInitialSavedState(null);
            mIncidentDialog = null;
        }
        mIncidentDialog = new AddIncidentDialog();
        mIncidentDialog.show(getFragmentManager(), "activity_add_incident_dialog");
    }

    public void openChatWindow(){
        if (prefBool){
            Bundle args = new Bundle();
            args.putString(getString(R.string.bundle_home), mHome.getLocation().toString());
            ChatFragment bottomSheetFragment = new ChatFragment();
            bottomSheetFragment.setArguments(args);
            bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
        } else {
            Toast.makeText(MainActivity.this, "Please set Home Location to enable chat", Toast.LENGTH_SHORT).show();
        }
    }

    public void goHome(){
        if (prefBool){
            homeLocation();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom( mHomeLatLng, 15));
            Toast.makeText( MainActivity.this, "Home", Toast.LENGTH_SHORT ).show();
        } else {
            Toast.makeText( MainActivity.this, "Please set Home Location to enable this function", Toast.LENGTH_SHORT ).show();
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
        }
        return super.onOptionsItemSelected( item );
    }

    private void settingsPrefsSetUp(){
        PreferenceManager.setDefaultValues(this,R.xml.pref_general,false);
        PreferenceManager.setDefaultValues(this,R.xml.pref_notification,false);
        PreferenceManager.setDefaultValues(this,R.xml.pref_data_sync,false);
        PreferenceManager.setDefaultValues(this,R.xml.pref_incidents,false);
    }

    private void settingsSetup(){
        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

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
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_profile:
                startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
                break;
            case R.id.nav_home:
                Intent homeIntent = new Intent( MainActivity.this, HomeActivity.class);
                startActivity(homeIntent);
                break;
            case R.id.nav_stats:
                Intent statsIntent = new Intent( MainActivity.this, HomeStatsActivity.class );
                startActivity(statsIntent);
                break;
            case R.id.nav_friends:
                Intent neighbourIntent = new Intent( MainActivity.this, NeighboursActivity.class);
                startActivity(neighbourIntent);
                break;
            case R.id.nav_share:
                shareInvite();
                break;
            case R.id.nav_about:
                Intent aboutIntent = new Intent( MainActivity.this, AboutActivity.class);
                startActivity(aboutIntent);
                break;
            case R.id.nav_log_out:
                logOut();
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
        mHomePrefs.registerOnSharedPreferenceChangeListener( new SharedPreferences.OnSharedPreferenceChangeListener() {
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
        final ImageView profilePicture = (ImageView) headerView.findViewById( R.id.nav_profile_image );
        final TextView userName = (TextView) headerView.findViewById( R.id.nav_profile_name );
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String provider = user.getProviders().toString();

        if (user != null){
            if (provider.equals("[password]")) {
                mName = user.getDisplayName();
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference();
                Query query = userRef.child(getString(R.string.dbnode_user)).child(user.getUid());
                Log.d( TAG, "Marker Query: "+ query);
                query.addListenerForSingleValueEvent( new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            Log.d( TAG, "Marker Datasnapshot" );
                            Map<String, Object> objectMap = (HashMap<String, Object>) dataSnapshot.getValue();
                            try {
                                String url = (String) objectMap.get( "profile_image" );
                                if (!url.equals("content://com.android.providers.media.documents/document/image%3A45")
                                        && !url.equals("null")){
                                    mPhotoUrl = Uri.parse(url);
                                    Picasso.with(MainActivity.this)
                                            .load(mPhotoUrl)
                                            .into( profilePicture );
                                    userName.setText(mName);
                                }else {
                                    Bitmap bitmap = getBitmap( R.mipmap.ic_launcher_foreground_icon);
                                    profilePicture.setImageBitmap(bitmap);
                                    userName.setText(mName);
                                }
                            }catch (NullPointerException e){
                                Log.d( TAG, "no profile picture set " + e.toString());
                                Bitmap bitmap = getBitmap( R.mipmap.ic_launcher_foreground_icon);
                                profilePicture.setImageBitmap(bitmap);
                                userName.setText(mName);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                } );
            }else if (provider.equals( "[facebook.com]" )) {
                for (UserInfo profile : user.getProviderData()) {
                    String facebookUserId = "";
                    mName = profile.getDisplayName();
                    mPhotoUrl = profile.getPhotoUrl();
                    if(FacebookAuthProvider.PROVIDER_ID.equals(profile.getProviderId())) {
                        facebookUserId = profile.getUid();
                        String photoUrl = "https://graph.facebook.com/" + facebookUserId + "/picture?height=500";
                        mPhotoUrl = Uri.parse(photoUrl);
                    }
                }
            }else if (provider.equals( "[google.com]" )){
                for (UserInfo profile : user.getProviderData()) {
                    mName = profile.getDisplayName();
                    mPhotoUrl = profile.getPhotoUrl();
                }
        }
            Picasso.with(MainActivity.this)
                    .load(mPhotoUrl)
                    .into( profilePicture );
            userName.setText(mName);
        }
    }

    private UserProfile createUser(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String provider = user.getProviders().toString();
        final String token = FirebaseInstanceId.getInstance().getToken();
        String firebase = "[password]";
        String facebook = "[facebook.com]";
        String google = "[google.com]";
        UserProfile newUser = new UserProfile();

        if (user != null){
            if (provider.equals(firebase)) {
                newUser.setUser_name(user.getDisplayName());
                newUser.setUser_email(user.getEmail());
                newUser.setProfile_image( String.valueOf( user.getPhotoUrl() ) );
                newUser.setUser_id(user.getUid());
                if (user.getPhoneNumber() != null){
                    newUser.setPhone_number(user.getPhoneNumber());
                }
                if (prefBool){
                    newUser.setHome_location(mHome.getLocation());
                }
                newUser.setFcm_token(token);
            }else if (provider.equals(facebook)) {
                for (UserInfo profile : user.getProviderData()) {
                    String facebookUserId = "";
                    newUser.setUser_id(user.getUid());
                    newUser.setUser_name(profile.getDisplayName());
                    newUser.setUser_email(profile.getEmail());
                    newUser.setUser_id(user.getUid());
                    newUser.setFcm_token(token);
                    if(FacebookAuthProvider.PROVIDER_ID.equals(profile.getProviderId())) {
                        facebookUserId = profile.getUid();
                        String photoUrl = "https://graph.facebook.com/" + facebookUserId + "/picture?height=500";
                        newUser.setProfile_image( String.valueOf( Uri.parse(photoUrl) ) );
                        if (profile.getPhoneNumber() != null){
                            newUser.setPhone_number(profile.getPhoneNumber());
                        }
                        if (prefBool){
                            newUser.setHome_location(mHome.getLocation());
                        }
                    }
                }
            }else if (provider.equals(google)){
                for (UserInfo profile : user.getProviderData()) {
                    newUser.setUser_id(profile.getUid());
                    newUser.setUser_name(profile.getDisplayName());
                    newUser.setUser_email(profile.getEmail());
                    newUser.setProfile_image( String.valueOf( profile.getPhotoUrl() ) );
                    newUser.setUser_id(user.getUid());
                    newUser.setFcm_token(token);
                    if (profile.getPhoneNumber() != null){
                        newUser.setPhone_number(profile.getPhoneNumber());
                    }
                    if (prefBool){
                        newUser.setHome_location( mHome.getLocation());
                    }
                }
            }else {
                Log.d( TAG, "Unable to create user." );
            }
        }
        return newUser;
    }

    private void setupDatabase(){
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

    }

    private void uploadUser(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String userId = user.getUid();
        mUserProfile = createUser();
        mDatabaseReference = FirebaseDatabase.getInstance()
                .getReference()
                .child(getString(R.string.dbnode_user))
                .child(mUserProfile.getUser_id());
        Query query = mDatabaseReference.orderByChild(mUserProfile.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    mDatabaseReference.setValue(mUserProfile);
                }else {
                    Log.d( TAG, "uploadUser, Key Exists, User not uploaded" );
                    Map<String, Object> objectMap = (HashMap<String, Object>) dataSnapshot.getValue();
                    try {
                        String uid = objectMap.get("user_id").toString();
                    } catch (NullPointerException e){
                        mDatabaseReference.setValue( mUserProfile);
                    }
                    checkAndLogFCMToken();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        } );
    }

    private void checkAuthenticationState(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null){
            mLogOutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity( mLogOutIntent);
            finish();
        }
    }

    private void checkAndLogFCMToken() {
        //Firebase cloud messaging tokens change from time, method checks if token exists on the DB, if not it will add it
        //if it exists but is not the same as the current one the method will over write with the latest token.

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String token = FirebaseInstanceId.getInstance().getToken();
        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference();
        Query query = userRef.child(getString(R.string.dbnode_user)).child(user.getUid()).child("fcm_token");
        query.addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    // check if current key matches online key. if not write the new one
                    if (!dataSnapshot.equals(token)){
                        userRef.child(getString(R.string.dbnode_user))
                                .child(user.getUid())
                                .child("fcm_token")
                                .setValue(token);
                    }
                }else {
                    //token does not exist, write new one to user.
                    userRef.child(getString(R.string.dbnode_user))
                            .child(user.getUid())
                            .child("fcm_token")
                            .setValue(token);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        } );
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
        mMap.setMapType( GoogleMap.MAP_TYPE_NORMAL);
        boolean success = mMap.setMapStyle( MapStyleOptions.loadRawResourceStyle( getApplicationContext(), getMapStyle()));
        if (!success) {
            Log.e(TAG, "Style parsing failed.");
        }
        setUpLocation();
        getIncidentLocations();
        mMap.setInfoWindowAdapter(new CustomInfoWindow(getApplicationContext()));
        mMap.setOnInfoWindowClickListener(MainActivity.this);
        mMap.setOnMarkerClickListener(MainActivity.this);

        mHomePrefs.registerOnSharedPreferenceChangeListener( new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                homeLocation();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom( mHomeLatLng, 15));
                getIncidentLocations();
            }
        } );

        mapClickAddIncident();

        if (prefBool){
            homeLocation();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mHomeLatLng, 15));
            getIncidentLocations();
        }
        notificationIntent();
    }

    private int getMapStyle(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String mapsPref = pref.getString("prefs_maps", "normal");
        int resource;
        switch (mapsPref){
            case "aub_style":
                resource = R.raw.aub_style;
                return resource;
            case "dark_style":
                resource = R.raw.dark_style;
                return resource;
            case "night_style":
                resource = R.raw.night_style;
                return resource;
            case "normal":
                resource = R.raw.normal;
                return resource;
            case "retro_style":
                resource = R.raw.retro_style;
                return resource;
            case "silver":
                resource = R.raw.silver;
                return resource;
        }
        return Integer.parseInt( null );
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
            mHome = gson.fromJson(json, Home.class);
            if (mHome.getLocation() != null){
                mHomeLatLng = mHome.getLocation();
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                try {
                    List<Address> addressList = geocoder.getFromLocation(mHomeLatLng.latitude, mHomeLatLng.longitude, 1);
                    Bitmap homeMarker = getBitmap(R.drawable.ic_loc_icon_home);
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(homeMarker, (int) mDefaultSize, (int) mDefaultSize, false);
                    MarkerOptions homeMarkerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap));
                    homeMarkerOptions.position(mHomeLatLng);
                    homeMarkerOptions.title("My Home");
                    Marker marker = mMap.addMarker(homeMarkerOptions);
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
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

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (marker.getTitle().equals("My Home")){
            marker.hideInfoWindow();
        }else {
            setUpInfoWindowDialog(marker);
        }
    }

    private void setUpInfoWindowDialog(final Marker marker) {
        //setup the marker with an AlertDialog Builder
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( MainActivity.this );
        View view = getLayoutInflater().inflate(R.layout.marker_popup, null);
        dialogBuilder.setView(view);
        final AlertDialog alertDialog = dialogBuilder.create();

        //get incident data from the marker Tag.
        final IncidentCrime incident = (IncidentCrime) marker.getTag();

        //setup Marker UI
        TextView incidentType = view.findViewById( R.id.pop_incident_type );
        TextView distance = view.findViewById( R.id.pop_distance);
        TextView date = view.findViewById(R.id.popList);
        TextView details = view.findViewById( R.id.popList2);
        TextView policeCAS = view.findViewById(R.id.police_cas);
        TextView streetAddress = view.findViewById(R.id.pop_street_name);
        Button dissmissPop = view.findViewById(R.id.dismissPop);
        ImageButton deleteButton = view.findViewById( R.id.pop_up_delete);
        final TextView reportedBy = view.findViewById(R.id.pop_reported_by);

        //get and init marker data from incident data.
        if (incident != null){
            if (incident.getIncident_type() != null){
                incidentType.setText(incident.getIncident_type());
            }
            //get and set street address
            String address = "";
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            try {
                List<Address> addressList = geocoder.getFromLocation(incident.getIncident_location().latitude, incident.getIncident_location().longitude, 1);
                if (addressList.size() > 0) {
                    if (addressList.get(0).getThoroughfare()!= null) {
                        address = addressList.get(0).getThoroughfare();
                        streetAddress.setText(address);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                streetAddress.setVisibility(View.INVISIBLE);
            }
            //get and calculate distance from users home
            if (incident.getIncident_location() != null){
                double distanceFromHome = checkDistance(incident);
                double dfromHFormat = distanceFormatting(distanceFromHome);
                distance.setText(dfromHFormat + getString(R.string.km_from_home));
            }
            //get and set date
            if (incident.getIncident_date() != null){
                date.setText(getString(R.string.date) + incident.getIncident_date());
            }
            //get and set description
            if (incident.getIncident_description() != null){
                details.setText(getString(R.string.description) + incident.getIncident_description());
            }
            //get and set police CAS number
            if (incident.getPolice_cas_number() != null){
                policeCAS.setText(getString(R.string.police_cas) + incident.getPolice_cas_number());
            }
            //get reported by uid, if it is the same as user idea, reported by you else check database and get reported by user details.
            if (incident.getReported_by() != null){
                final String uid = incident.getReported_by();
                if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals( uid )){
                    reportedBy.setText("Reported By: "+ "\n" + "you");
                    reportedBy.setVisibility( View.VISIBLE);
                    reportedBy.setOnClickListener( new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity( new Intent( MainActivity.this, UserProfileActivity.class));
                        }
                    } );
                    // if the user was the reporter, delete button becomes visible and user will be able to delete incident
                    if (uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        deleteButton.setVisibility(View.VISIBLE);
                        deleteButton.setOnClickListener( new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deleteIncidentDialog(incident);
                                alertDialog.dismiss();
                            }
                        } );
                    }
                }else {
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference();
                    Query query = userRef.child(getString(R.string.dbnode_user)).child(uid);
                    Log.d( TAG, "Marker Query: "+ query);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                Log.d( TAG, "Marker Datasnapshot" );
                                Map<String, Object> objectMap = (HashMap<String, Object>) dataSnapshot.getValue();
                                String userName = (String) objectMap.get( "user_name" );
                                if (userName.equals( "Place Holder" )){
                                }else {
                                    reportedBy.setText("Reported By: "+ "\n" + userName);
                                    reportedBy.setVisibility(View.VISIBLE);
                                    reportedBy.setOnClickListener( new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent otherUser = new Intent(MainActivity.this, OtherUserActivity.class);
                                            otherUser.putExtra(getString(R.string.field_other_uid), uid);
                                            startActivity(otherUser);
                                        }
                                    } );
                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    } );
                }
            }
        }
        alertDialog.show();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dissmissPop.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        } );
    }

    private void deleteIncidentDialog(IncidentCrime incident) {
        String keyRef = incident.getReference();
        DeleteIncidentDialog deleteIncidentDialog = new DeleteIncidentDialog();
        Bundle args = new Bundle();
        args.putString(getString(R.string.field_key_ref), keyRef);
        deleteIncidentDialog.setArguments(args);
        deleteIncidentDialog.show(getFragmentManager(), "dialog_delete_incident" );
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.getTitle().equals("My Home") || marker.getTitle().equals("add_incident_from_Map")){
            return true;
        }
        return false;
    }

    private void mapClickAddIncident(){

        //add an incident by long clicking on the map.

        mMap.setOnMapLongClickListener( new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {

                Bitmap addMarker = Bitmap.createScaledBitmap(getBitmap(R.drawable.ic_add_icon), (int) mDefaultSize, (int) mDefaultSize, false);
                final MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(addMarker));
                markerOptions.title("add_incident_from_Map");
                markerOptions.position(latLng);
                final Marker marker =  mMap.addMarker( markerOptions );


                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                View view = inflater.inflate(R.layout.dialog_map_click_add_incident, null);
                dialogBuilder.setView(view);
                final AlertDialog alertDialog = dialogBuilder.create();

                Button addButton = (Button) view.findViewById( R.id.map_click_yes);
                Button cancelButton = (Button) view.findViewById( R.id.map_click_no);

                alertDialog.show();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable( Color.TRANSPARENT));

                addButton.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle args = new Bundle();
                        args.putString("map_click_location", latLng.toString());
                        MapClickAddIncident mapClickAddIncident = new MapClickAddIncident();
                        mapClickAddIncident.setArguments(args);
                        mapClickAddIncident.show( getFragmentManager(), "map_click_add_incident");
                        alertDialog.dismiss();
                    }
                } );

                cancelButton.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                } );

                alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        marker.remove();
                    }
                } );

            }
        } );
    }

    //------------INCIDENTS TO FIREBASE DATABASE------------//
    public void getIncidentLocations(){
        ArrayList<IncidentCrime> crimeArrayList = new ArrayList<>();
        if (crimeArrayList.size() > 0){
            crimeArrayList.clear();
        }
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = mDatabaseReference.child(getString(R.string.dbnode_incidents));

        mHomePrefs = getSharedPreferences(Constants.PREFS_HOME, 0);
        prefBool = mHomePrefs.contains(Constants.HOME);

        if (prefBool){
            mMap.clear();
            query.addListenerForSingleValueEvent( new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
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

                        incident.setReference(singleSnapshot.getKey());

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

    private void setUpIncidentMarkers(IncidentCrime incident) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        int distanceFromHomePref = Integer.parseInt( pref.getString("prefs_distance_from_home", "5") );
        int incidentTimePref = Integer.parseInt( pref.getString("prefs_time_home", "31") );
        double distanceFromHome = checkDistance(incident);
        double dfromHFormat = distanceFormatting(distanceFromHome);
        today();
        if (incident.getIncident_location() != null){
            if (incident.getIncident_date() != null){
                long days = getDateDiff(convertDate(incident).getTime(), mToday, TimeUnit.DAYS);
                if (days <= incidentTimePref){
                    if (distanceFromHome <= distanceFromHomePref){
                        LatLng location = incident.getIncident_location();
                        Bitmap catMarker = markerType(incident);
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(catMarker));
                        markerOptions.title(incident.getIncident_type());
                        markerOptions.position(location);
                        markerOptions.snippet(dfromHFormat + "Km Away" + "\n" +
                        "Incident Date: " + incident.getIncident_date());
                        Marker marker = mMap.addMarker(markerOptions);
                        marker.setTag(incident);
                    }
                }
            }
        }

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

    private double checkDistance(IncidentCrime incident) {
        double distance = 0;
        if (incident.getIncident_location() != null & mHomeLatLng != null){
            distance = computeDistanceBetween(incident.getIncident_location(), mHomeLatLng);
        }
        return distance / 1000;
    }

    private Calendar convertDate(IncidentCrime incident) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
        try {
            cal.setTime(sdf.parse(incident.getIncident_date()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return cal;
    }

    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }

    public Bitmap markerType(IncidentCrime incident){

        Bitmap redMarker = getBitmap(R.drawable.ic_loc_icon_red);
        Bitmap resizeRed = Bitmap.createScaledBitmap(redMarker, (int) mDefaultSize, (int) mDefaultSize, false);
        Bitmap orangeMarker = getBitmap(R.drawable.ic_loc_icon_orange);
        Bitmap resizeOrange = Bitmap.createScaledBitmap(orangeMarker, (int) mDefaultSize, (int) mDefaultSize, false);
        Bitmap yellowMarker = getBitmap(R.drawable.ic_loc_icon_yellow);
        Bitmap resizeYellow = Bitmap.createScaledBitmap(yellowMarker, (int) mDefaultSize, (int) mDefaultSize, false);
        Bitmap dog = getBitmap( R.drawable.ic_siberian_husky );
        Bitmap resizeDog = Bitmap.createScaledBitmap(dog, (int) mDefaultSize, (int) mDefaultSize, false);


        String type = incident.getIncident_type();

        switch (type){
            case "House Burglary":
                return resizeOrange;
            case "House Robbery":
                return resizeOrange;
            case "Theft of a Motor Vehicle":
                return resizeOrange;
            case "Theft from a Motor Vehicle":
                return resizeYellow;
            case "Hi-jacking":
                return resizeOrange;
            case "Damage to property / Arson":
                return resizeYellow;
            case "Pick-pocketing or bag-snatching":
                return resizeYellow;
            case "Armed Robbery":
                return resizeOrange;
            case "Robbery with aggravating circumstances":
                return resizeRed;
            case "Commercial Burglary":
                return resizeOrange;
            case "Shoplifting":
                return resizeYellow;
            case "Commercial Robbery":
                return resizeOrange;
            case "Dog Poisoning":
                return resizeDog;
            case "Stock-theft":
                return resizeYellow;
            case "Farm Murder":
                return resizeRed;
            case "Farm attack":
                return resizeRed;
            case "Murder":
                return resizeRed;
            case "Attempted Murder":
                return resizeRed;
            case "Rape":
                return resizeRed;
            case "Kidnapping":
                return resizeOrange;
            case "Assault":
                return resizeYellow;
            case "Sexual Assault":
                return resizeRed;
            case "other…":
                return resizeYellow;
        }
        return  null;
    }

    //------------OTHER------------//
    private void notificationIntent(){
        Intent noticeIntent = getIntent();
        if (noticeIntent.hasExtra(getString(R.string.notification_location ))){
            String location = noticeIntent.getStringExtra( getString( R.string.notification_location));
            String format1 = location.replaceAll( "[(]", "" );
            String format2 = format1.replaceAll( "[)]", "" );
            String format3 = format2.substring(9);
            String[] latlong =  format3.split(",");
            double latitude = Double.parseDouble(latlong[0]);
            double longitude = Double.parseDouble(latlong[1]);
            LatLng inLoc = new LatLng( latitude, longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(inLoc, 18));
            noticeIntent.removeExtra(getString( R.string.notification_location));
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

    public void today(){
        Calendar calendar = Calendar.getInstance();
        mToday = calendar.getTime();
    }

    public void displayMetrics(){
        DisplayMetrics dm = MainActivity.this.getResources().getDisplayMetrics();
        float displayDensity = dm.density;
        mDefaultSize = displayDensity * DEFAULT_OUTLINE_WIDTH_DP;
    }

    public void dynamicLinkCreate(){
        FirebaseDynamicLinks.getInstance().getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData data) {
                        if (data == null) {
                            Log.d(TAG, "getInvitation: no data");
                            return;
                        }

                        // Get the deep link
                        Uri deepLink = data.getLink();

                        // Extract invite
                        FirebaseAppInvite invite = FirebaseAppInvite.getInvitation(data);
                        if (invite != null) {
                            String invitationId = invite.getInvitationId();
                        }

                        // Handle the deep link
                        Log.d(TAG, "deepLink:" + deepLink);
                        if (deepLink != null) {
                            //
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "getDynamicLink:onFailure", e);
                    }
                });
    }

    private void shareInvite(){
        String mainDynamicLink = "https://d6q57.app.goo.gl/hometribe";
//        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
//                .setMessage(getString(R.string.invitation_message))
//                .setDeepLink(Uri.parse(mainDynamicLink))
////                .setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
////                .setCallToActionText(getString(R.string.invitation_cta))
//                .build();
//        startActivityForResult(intent, REQUEST_INVITE);
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                "Hey check out the home tribe app at: " + mainDynamicLink);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Send to:"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.d(TAG, "onActivityResult: sent invitation " + id);
                }
            } else {
                Toast.makeText( MainActivity.this, "Share failed, please try again", Toast.LENGTH_SHORT ).show();
            }
        }
    }

    private void addMobSetup(){
        String addMobAppId = "ca-app-pub-8837476093017718~3132418627";
        String mainBannerAddId = "ca-app-pub-8837476093017718/4640359223";
        MobileAds.initialize( MainActivity.this, addMobAppId);
        mAdView = (AdView) findViewById( R.id.adView );
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice( AdRequest.DEVICE_ID_EMULATOR )
                .build();
        mAdView.loadAd(adRequest);
    }
}
