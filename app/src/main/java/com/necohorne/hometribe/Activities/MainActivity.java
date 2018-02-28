package com.necohorne.hometribe.Activities;

import android.Manifest;
import android.app.AlertDialog;
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
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
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
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FacebookAuthProvider;
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
import com.necohorne.hometribe.Activities.Dialog.CustomInfoWindow;
import com.necohorne.hometribe.Activities.Dialog.HomePromptSetup;
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
                    if (mIncidentDialog != null){
                        mIncidentDialog.setInitialSavedState(null);
                        mIncidentDialog = null;
                    }
                    mIncidentDialog = new AddIncidentDialog();
                    mIncidentDialog.show(getFragmentManager(), "activity_add_incident_dialog");
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
        homeLocation();
        uploadUser();
        super.onRestart();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
            case R.id.nav_friends:
                //
                break;
            case R.id.nav_share:
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
        ImageView profilePicture = (ImageView) headerView.findViewById( R.id.nav_profile_image );
        TextView userName = (TextView) headerView.findViewById( R.id.nav_profile_name );
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String provider = user.getProviders().toString();

        if (user != null){
            String uid;
            String email;
            if (provider.equals("[password]")) {
                uid = user.getUid();
                mName = user.getDisplayName();
                email = user.getEmail();
                mPhotoUrl = user.getPhotoUrl();
            }else if (provider.equals( "[facebook.com]" )) {
                for (UserInfo profile : user.getProviderData()) {
                    String facebookUserId = "";
                    uid = profile.getUid();
                    mName = profile.getDisplayName();
                    email = profile.getEmail();
                    mPhotoUrl = profile.getPhotoUrl();
                    if(FacebookAuthProvider.PROVIDER_ID.equals(profile.getProviderId())) {
                        facebookUserId = profile.getUid();
                        String photoUrl = "https://graph.facebook.com/" + facebookUserId + "/picture?height=500";
                        mPhotoUrl = Uri.parse(photoUrl);
                    }
                }
            }else if (provider.equals( "[google.com]" )){
                for (UserInfo profile : user.getProviderData()) {
                    String providerId = profile.getProviderId();
                    uid = profile.getUid();
                    mName = profile.getDisplayName();
                    email = profile.getEmail();
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
            }else if (provider.equals(facebook)) {
                for (UserInfo profile : user.getProviderData()) {
                    String facebookUserId = "";
                    newUser.setUser_id(user.getUid());
                    newUser.setUser_name(profile.getDisplayName());
                    newUser.setUser_email(profile.getEmail());
                    newUser.setUser_id(user.getUid());
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
        boolean success = mMap.setMapStyle( MapStyleOptions.loadRawResourceStyle( getApplicationContext(), R.raw.silver));
        if (!success) {
            Log.e(TAG, "Style parsing failed.");
        }
        setUpLocation();
        getIncidentLocations();
        mMap.setInfoWindowAdapter(new CustomInfoWindow(getApplicationContext()));
        mMap.setOnInfoWindowClickListener(MainActivity.this);
        mMap.setOnMarkerClickListener(MainActivity.this);
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
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mHomeLatLng, 14));
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

    private void setUpInfoWindowDialog(Marker marker) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( MainActivity.this );
        View view = getLayoutInflater().inflate(R.layout.marker_popup, null);

        IncidentCrime incident = (IncidentCrime) marker.getTag();

        TextView incidentType = view.findViewById( R.id.pop_incident_type );
        TextView distance = view.findViewById( R.id.pop_distance);
        TextView date = view.findViewById(R.id.popList);
        TextView details = view.findViewById( R.id.popList2);
        TextView policeCAS = view.findViewById( R.id.police_cas);
        Button dissmissPop = view.findViewById(R.id.dismissPop);
        final TextView reportedBy = view.findViewById(R.id.pop_reported_by);

        if (incident != null){
            if (incident.getIncident_type() != null){
                incidentType.setText(incident.getIncident_type());
            }
            if (incident.getIncident_location() != null){
                double distanceFromHome = checkDistance(incident);
                double dfromHFormat = distanceFormatting(distanceFromHome);
                distance.setText(dfromHFormat + getString(R.string.km_from_home));
            }
            if (incident.getIncident_date() != null){
                date.setText(getString(R.string.date) + incident.getIncident_date());
            }
            if (incident.getIncident_description() != null){
                details.setText(getString(R.string.description) + incident.getIncident_description());
            }
            if (incident.getPolice_cas_number() != null){
                policeCAS.setText(getString(R.string.police_cas) + incident.getPolice_cas_number());
            }
            if (incident.getReported_by() != null){
                String uid = incident.getReported_by();
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference();
                Query query = userRef.child( getString( R.string.dbnode_user)).child(uid);
                Log.d( TAG, "Marker Query: "+ query);
                query.addListenerForSingleValueEvent( new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            Log.d( TAG, "Marker Datasnapshot" );
                            Map<String, Object> objectMap = (HashMap<String, Object>) dataSnapshot.getValue();
                            String userName = (String) objectMap.get( "user_name" );
                            if (userName.equals( "Place Holder" )){
                            }else {
                                reportedBy.setText("Reported By: "+ "\n" + userName);
                                reportedBy.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                } );
            }
        }
        dialogBuilder.setView(view);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dissmissPop.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        } );
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.getTitle().equals("My Home")){
            return true;
        }
        return false;
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
                            Bitmap homeMarker = getBitmap(R.drawable.ic_loc_icon_red);
                            Bitmap resizedBitmap = Bitmap.createScaledBitmap(homeMarker, (int) mDefaultSize, (int) mDefaultSize, false);
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap));
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

    //------------OTHER------------//
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

}
