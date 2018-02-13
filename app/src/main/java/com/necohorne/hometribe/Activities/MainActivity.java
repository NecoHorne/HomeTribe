package com.necohorne.hometribe.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.support.design.widget.Snackbar;
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

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.necohorne.hometribe.R;
import com.squareup.picasso.Picasso;


import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    //location objects
    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;

    //login objects
    private static AccessToken mAccessToken;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();

    //user profile
    private ProfilePictureView profilePicture;
    private TextView userName;
    private static Profile fbProfile;

    //Bottom Nav object
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
//                    alert dialog logging an incident
                    return true;
            }
            return false;
        }
    };

    static {
        mAccessToken.getCurrentAccessToken();
        fbProfile = Profile.getCurrentProfile();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState );
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        //login
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        updateProfilePicture();

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
                setUpLocation();
                Toast.makeText( MainActivity.this, "Current Location", Toast.LENGTH_LONG).show();
            }
        } );

        //Maps
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapView);
        mapFragment.getMapAsync( MainActivity.this );


    }

    @Override
    protected void onResume() {

        super.onResume();
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

    //Menus
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
                //
                break;
            case R.id.nav_chat:
                //
                break;
            //add more nav menu items
        }
        DrawerLayout drawer = (DrawerLayout) findViewById( R.id.drawer_layout );
        drawer.closeDrawer( GravityCompat.START );
        return true;
    }

    // Social Media
    private void logOut() {
        facebookLogOut();
        googleLogOut();
    }

    private void googleLogOut(){

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if (account != null) {
            mGoogleSignInClient.signOut().addOnCompleteListener( this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    startActivity( new Intent( MainActivity.this, LoginActivity.class ) );
                    finish();
                }
            } );
        }else {
            //already signed out.
        }
    }

    private void facebookLogOut(){
            if (mAccessToken != null){
                return;
                //already Logged out
            }else {
                LoginManager.getInstance().logOut();
                Toast.makeText( MainActivity.this, "Logged Out", Toast.LENGTH_LONG ).show();
            }
            startActivity( new Intent( MainActivity.this, LoginActivity.class ) );
            finish();
        }

    private void updateProfilePicture(){

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        NavigationView navigationView = (NavigationView) findViewById( R.id.nav_view );
        View headerView = navigationView.getHeaderView(0);
        profilePicture = (ProfilePictureView) headerView.findViewById( R.id.nav_profile_image );
        userName = (TextView) headerView.findViewById( R.id.nav_profile_name );

        if (fbProfile != null) {
            try {
                userName.setText( fbProfile.getFirstName() + " " + fbProfile.getLastName());
                profilePicture.setProfileId(fbProfile.getId());

            }catch (Exception e){
                Log.d("Facebook Profile Error", "error getting facebook profile data");

            }
        }
    }

    // Maps
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        setUpLocation();
    }

    private void setUpLocation() {

        locationManager = (LocationManager) this.getSystemService( LOCATION_SERVICE );

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d( "location ", location.toString() );
                mMap.clear();

                LatLng newLocation = new LatLng( location.getLatitude(), location.getLongitude());

                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                try {
                    List<Address> addressList = geocoder.getFromLocation( newLocation.latitude, newLocation.longitude, 1 );

                    mMap.addMarker( new MarkerOptions().position(newLocation ).title( addressList.get(0).getAddressLine(0)));
                    mMap.moveCamera( CameraUpdateFactory.newLatLngZoom(newLocation, 13) );

                } catch (IOException e) {
                    e.printStackTrace();
                }
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
            ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1 );
        } else {
            locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, locationListener );
        }
    }

    public void homeLocation(){
        //TODO
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

}
