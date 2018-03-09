package com.necohorne.hometribe.Activities.AppActivities;

import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.necohorne.hometribe.Constants.Constants;
import com.necohorne.hometribe.Models.Home;
import com.necohorne.hometribe.R;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    protected GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;

    private Place homePlace;

    private Button saveButton;
    private Spinner mProvinceSpinner;
    private PlaceAutocompleteFragment mAutocompleteFragment;
    private String[] mTowns;
    private int mProvinces_array;
    private ArrayAdapter<CharSequence> mProvinceAdapter;
    private String mChosenProvince;
    private SharedPreferences mHomePrefs;

    public static boolean isActivityRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getAddress();
        uiSetup();
        mHomePrefs = getSharedPreferences(Constants.PREFS_HOME, 0 );
        boolean prefBool = mHomePrefs.contains(Constants.HOME);
        if (prefBool){
            getHomePrefs();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActivityRunning = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActivityRunning = false;
    }

    private void uiSetup() {
        mProvinceSpinner = (Spinner) findViewById(R.id.home_province_spinner2);
        provinceSpinnerSetup();
        saveButton = (Button) findViewById(R.id.home_save_button2);
        saveButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveHome();
            }
        } );
    }

    private void provinceSpinnerSetup() {
        mProvinces_array = R.array.provinces_array;
        mProvinceAdapter = ArrayAdapter.createFromResource( getApplicationContext(), mProvinces_array, R.layout.spinner_item_home );
        mProvinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mProvinceSpinner.setAdapter( mProvinceAdapter );
        mProvinceSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mChosenProvince = mProvinceSpinner.getSelectedItem().toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        } );
    }

    private void getAddress(){

        mGeoDataClient = Places.getGeoDataClient(this, null);
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        mAutocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.home_place_autocomplete_fragment);

        mAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, "Place: " + place.getAddress().toString());
                homePlace = place;
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    private void saveHome() {

        if (homePlace != null){
            Home home = new Home();
            home.setStreet_address( homePlace.getAddress().toString());
            home.setLocation(homePlace.getLatLng());
            home.setState_province(mChosenProvince);

            Geocoder geocoder = new Geocoder( getApplicationContext(), Locale.getDefault() );
            List<Address> addresses;
            try {
                addresses = geocoder.getFromLocation(
                        homePlace.getLatLng().latitude,
                        homePlace.getLatLng().longitude,
                        1 );
                if (addresses.size() > 0) {
                    if (addresses.get( 0 ).getPostalCode() != null) {
                        home.setPostal_code( addresses.get( 0 ).getPostalCode());
                    }
                    if (addresses.get( 0 ).getSubLocality() != null) {
                        home.setTown_city(addresses.get( 0 ).getSubLocality());
                    }
                    if (addresses.get( 0 ).getCountryName() != null) {
                        home.setCountry(addresses.get( 0 ).getCountryName());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference();
            dataRef.child(getString(R.string.dbnode_user))
                    .child( FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("home_location")
                    .setValue(homePlace.getLatLng());

            mHomePrefs = getSharedPreferences( Constants.PREFS_HOME, 0 );
            SharedPreferences.Editor editor = mHomePrefs.edit();
            Gson gson = new Gson();
            String json = gson.toJson(home);
            editor.putString("home", json );
            editor.commit();

            getHomePrefs();
            finish();

        } else {
            Toast.makeText( getApplicationContext(), "Please fill in valid address", Toast.LENGTH_SHORT).show();
        }
    }

    public void getHomePrefs(){
        Gson gson = new Gson();
        String json = mHomePrefs.getString( Constants.HOME, "" );
        Home home = gson.fromJson(json, Home.class);

        List<String> options = Arrays.asList( getResources().getStringArray( mProvinces_array ));
        int provinceSelected = options.indexOf(home.getState_province());
        mProvinceSpinner.setSelection(provinceSelected);
        mAutocompleteFragment.setText(home.getStreet_address());
    }
}
