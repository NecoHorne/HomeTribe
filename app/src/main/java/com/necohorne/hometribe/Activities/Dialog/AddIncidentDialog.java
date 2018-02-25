package com.necohorne.hometribe.Activities.Dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.necohorne.hometribe.Activities.MainActivity;
import com.necohorne.hometribe.Models.IncidentCrime;
import com.necohorne.hometribe.R;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.Inflater;

import static android.text.TextUtils.isEmpty;
import static com.facebook.FacebookSdk.getApplicationContext;

public class AddIncidentDialog extends DialogFragment{

    private static final String TAG = "Incident Dialog ";

    private Context mContext;
    private View mView;

    private DatabaseReference mDatabase;
    private StorageReference mStorageRef;

    private Spinner mIncidentSpinner;
    private Spinner mProvinceSpinner;
    private Spinner mHourSpinner;
    private Spinner mMinuteSpinner;
    private EditText mDay;
    private EditText mMonth;
    private EditText mYear;
    private String mHours;
    private String mMinutes;
    private EditText mDescription;
    private EditText mPoliceNumber;

    private String mChosenProvince;
    private String mSpecifiedIncident;
    private FirebaseAuth mAuth;
    private Place incidentPlace;
    private Object mIncidentdialog;
    private PlaceAutocompleteFragment mAutocompleteFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            try {
                mView = inflater.inflate(R.layout.activity_add_incident_dialog , container, false);
            } catch (InflateException e) {
                return mView;
            }

        mContext = getActivity();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Log.d( TAG, "incident User " + user.getUid() );
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable( Color.TRANSPARENT));
        setupUi();
        getAddress();
        setupDataBase();
        return mView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Fragment mf = mAutocompleteFragment.getFragmentManager().findFragmentById( R.id.incident_place_autocomplete_fragment  );
        if (mf != null)
            getFragmentManager().beginTransaction().remove(mf).commit();
    }

    private void setupUi() {

        mIncidentSpinner = (Spinner) mView.findViewById( R.id.report_incident_spinner );
        incidentSpinnerUI();
        mProvinceSpinner = (Spinner) mView.findViewById( R.id.incident_province_spinner);
        provinceSpinnerUI();
        mHourSpinner = (Spinner) mView.findViewById(R.id.hour_spinner);
        hourSpinnerUI();
        mMinuteSpinner = (Spinner) mView.findViewById(R.id.minutes_spinner);
        minuteSpinnerUi();

        mDay = (EditText) mView.findViewById(R.id.report_incident_day);
        mMonth = (EditText) mView.findViewById(R.id.report_incident_month);
        mYear = (EditText) mView.findViewById(R.id.report_incident_year);

        mDescription = (EditText) mView.findViewById(R.id.report_incident_description);
        mPoliceNumber = (EditText) mView.findViewById(R.id.report_incident_cas_number);
        Button submitBotton = (Button) mView.findViewById( R.id.report_incident_submit_button );

        submitBotton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupIncidentObject();
                Toast.makeText(mContext, "Incident Reported", Toast.LENGTH_SHORT).show();
                getDialog().dismiss();
                }
            }
         );
    }

    private void getAddress(){

        GeoDataClient geoDataClient = Places.getGeoDataClient( mContext, null );
        PlaceDetectionClient placeDetectionClient = Places.getPlaceDetectionClient( mContext, null );

        mAutocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById( R.id.incident_place_autocomplete_fragment );

        mAutocompleteFragment.setOnPlaceSelectedListener( new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, "Place: " + place.getAddress().toString());
                incidentPlace = place;
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    private void setupDataBase(){
        mDatabase =  FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbnode_incidents));

        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    private void incidentSpinnerUI(){
        ArrayAdapter<CharSequence> incidentsAdapter = ArrayAdapter.createFromResource( mContext, R.array.incidents_array, R.layout.spinner_item );
        incidentsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mIncidentSpinner.setAdapter( incidentsAdapter );
        mIncidentSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSpecifiedIncident = mIncidentSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        } );
    }

    private void provinceSpinnerUI(){
        ArrayAdapter<CharSequence> provinceAdapter = ArrayAdapter.createFromResource( mContext, R.array.provinces_array, R.layout.spinner_item );
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mProvinceSpinner.setAdapter( provinceAdapter );
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

    private void hourSpinnerUI(){
        ArrayAdapter<CharSequence> hourSpinnerAdapter = ArrayAdapter.createFromResource( mContext, R.array.hours, R.layout.spinner_item);
        hourSpinnerAdapter.setDropDownViewResource( android.R.layout.simple_spinner_item);
        mHourSpinner.setAdapter( hourSpinnerAdapter);
        mHourSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mHours = mHourSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        } );

    }

    private void minuteSpinnerUi(){
        ArrayAdapter<CharSequence> minSpinnerAdapter = ArrayAdapter.createFromResource( mContext, R.array.minutes, R.layout.spinner_item);
        minSpinnerAdapter.setDropDownViewResource( android.R.layout.simple_spinner_item);
        mMinuteSpinner.setAdapter( minSpinnerAdapter);
        mMinuteSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mMinutes = mMinuteSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        } );

    }

    private void setupIncidentObject() {

        if (!isEmpty(mDay.getText().toString()
        )       || !isEmpty(mMonth.getText().toString())
                || !isEmpty(mYear.getText().toString())
                || !mHours.equals("Hour")
                || !mMinutes.equals("Minutes")
                || incidentPlace == null
                || !isEmpty(mDescription.getText().toString())) {

            //IncidentCrime Type
            IncidentCrime incident = new IncidentCrime( mSpecifiedIncident );

            //IncidentCrime Date and time

            incident.setIncident_date(getDate());

            //IncidentCrime Country
            String locale = mContext.getResources().getConfiguration().locale.getDisplayCountry();
            incident.setCountry(locale);

            //IncidentCrime state_province
            incident.setState_province(mChosenProvince);

            //IncidentCrime Town
            Geocoder geocoder = new Geocoder( getApplicationContext(), Locale.getDefault() );
            List<Address> addresses;
            try {
                addresses = geocoder.getFromLocation(
                        incidentPlace.getLatLng().latitude,
                        incidentPlace.getLatLng().longitude,
                        1 );
                if (addresses.size() > 0) {

                    if (addresses.get( 0 ).getSubLocality() != null) {
                        incident.setTown(addresses.get( 0 ).getSubLocality());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            //IncidentCrime Street Address
            incident.setStreet_address(incidentPlace.getAddress().toString());

            //IncidentCrime Location
            incident.setIncident_location(incidentPlace.getLatLng());

            //IncidentCrime Description
            incident.setIncident_description(mDescription.getText().toString());

            //IncidentCrime Police CAS Number
            if (isEmpty(mPoliceNumber.getText().toString())){
                incident.setPolice_cas_number("No CAS Number at this time");
            }else{
                incident.setPolice_cas_number( mPoliceNumber.getText().toString());
            }

            //IncidentCrime Reported By
            FirebaseUser user = mAuth.getCurrentUser();
            Log.d(TAG, "incident reported by: " + user.getUid().toString() );
            incident.setReported_by( user.getUid());


            DatabaseReference newIncident = mDatabase.push();
            newIncident.setValue( incident );

        } else {
            Toast.makeText( mContext, "All fields except Police CAS number are Mandatory", Toast.LENGTH_SHORT ).show();
        }
    }

    private String getDate(){
        int year = Integer.parseInt(mYear.getText().toString());
        int month = Integer.parseInt(mMonth.getText().toString()) - 1;
        int day = Integer.parseInt(mDay.getText().toString());
        int hour = Integer.parseInt(mHours);
        int minute = Integer.parseInt(mMinutes);
        Calendar calendar = Calendar.getInstance();
        calendar.set( year, month, day, hour, minute);
        Date date = calendar.getTime();

        return date.toString();
    }
}
