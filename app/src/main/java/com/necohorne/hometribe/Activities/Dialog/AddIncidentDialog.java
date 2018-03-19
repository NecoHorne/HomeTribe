package com.necohorne.hometribe.Activities.Dialog;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.necohorne.hometribe.Models.IncidentCrime;
import com.necohorne.hometribe.R;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.text.TextUtils.isEmpty;
import static com.facebook.FacebookSdk.getApplicationContext;

public class AddIncidentDialog extends DialogFragment{

    private static final String TAG = "Incident Dialog ";
    private Context mContext;
    private View mView;
    private DatabaseReference mDatabase;
    private Spinner mIncidentSpinner;
    private Spinner mProvinceSpinner;
    private EditText mDescription;
    private EditText mPoliceNumber;
    private String mChosenProvince;
    private String mSpecifiedIncident;
    private FirebaseAuth mAuth;
    private Place incidentPlace;
    private PlaceAutocompleteFragment mAutocompleteFragment;
    private TextView date;
    private TextView time;

    private int mMinute;
    private int mHour;
    private int mDay;
    private int mMonth;
    private int mYear;
    private Date mToday;
    private Date mDate;

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
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setupUi();
        getAddress();
        setupDataBase();
        Calendar calendar = Calendar.getInstance();
        mToday = calendar.getTime();
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

        mIncidentSpinner = (Spinner) mView.findViewById(R.id.report_incident_spinner );
        incidentSpinnerUI();
        mProvinceSpinner = (Spinner) mView.findViewById(R.id.incident_province_spinner);
        provinceSpinnerUI();

        date = (TextView) mView.findViewById( R.id.alert_date );
        date.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDateFromPicker();

            }
        } );
        time = (TextView) mView.findViewById( R.id.alert_time );
        time.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTimeFromPicker();

            }
        } );

        mDescription = (EditText) mView.findViewById(R.id.report_incident_description);
        mPoliceNumber = (EditText) mView.findViewById(R.id.report_incident_cas_number);
        Button submitBotton = (Button) mView.findViewById(R.id.report_incident_submit_button );

        submitBotton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupIncidentObject();

                }
            }
         );
    }

    private void getAddress(){

        GeoDataClient geoDataClient = Places.getGeoDataClient( mContext, null );
        PlaceDetectionClient placeDetectionClient = Places.getPlaceDetectionClient( mContext, null );

        mAutocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById( R.id.incident_place_autocomplete_fragment );
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setCountry("ZA")
                .build();

        mAutocompleteFragment.setFilter(typeFilter);
        mAutocompleteFragment.setHint("Incident Location");

        mAutocompleteFragment.setOnPlaceSelectedListener( new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, "Place: " + place.getAddress().toString());
                incidentPlace = place;
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    private void setupDataBase(){
        mDatabase =  FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbnode_incidents));
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

    private void setupIncidentObject() {

        if (!isEmpty(mDescription.getText().toString())
                && (incidentPlace != null )
                && (mYear != 0)
                && (mMonth != 0 )
                && (mDay != 0)) {

            String date = getDate();

            if (mDate.after(mToday)){
                Toast.makeText(mContext, "You can't report future crimes", Toast.LENGTH_SHORT).show();
            } else {
                //IncidentCrime Type
                IncidentCrime incident = new IncidentCrime( mSpecifiedIncident );

                //IncidentCrime Date and time

                incident.setIncident_date(date);

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

                getDialog().dismiss();
                Toast.makeText(mContext, "Incident Reported", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText( mContext, "All fields except Police CAS number are Mandatory", Toast.LENGTH_SHORT ).show();
        }
    }

    private String getDate(){
        int year = mYear;
        int month = mMonth;
        int day = mDay;
        int hour = mHour;
        int minute = mMinute;
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute);
        mDate = calendar.getTime();

        return mDate.toString();
    }

    private void getDateFromPicker(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( mContext);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View view = inflater.inflate(R.layout.date_picker_dialog, null);
        dialogBuilder.setView(view);
        final AlertDialog alertDialog = dialogBuilder.create();
        final DatePicker mDatePicker = (DatePicker) view.findViewById(R.id.datePicker);
        Button setButton = (Button) view.findViewById(R.id.set_date_button);

        setButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDay = mDatePicker.getDayOfMonth();
                mMonth = mDatePicker.getMonth();
                mYear = mDatePicker.getYear();
                StringBuilder stringBuilder = new StringBuilder(  );
                if (mDay <= 9){
                    stringBuilder.append("0" + mDay);
                }else {
                    stringBuilder.append(mDay);
                }
                stringBuilder.append(" / ");
                if ((mMonth + 1) <= 9){
                    stringBuilder.append("0" + (mMonth + 1));
                }else {
                    stringBuilder.append(mMonth + 1);
                }
                stringBuilder.append(" / ");
                stringBuilder.append(mYear);
                date.setText(stringBuilder.toString());
                alertDialog.dismiss();
            }
        } );
        alertDialog.show();
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void getTimeFromPicker(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( mContext);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View view = inflater.inflate(R.layout.time_picker_dialog, null);
        dialogBuilder.setView(view);
        final AlertDialog alertDialog = dialogBuilder.create();

        final TimePicker timePicker = (TimePicker) view.findViewById(R.id.timePicker);
        Button setTimeButton = (Button) view.findViewById( R.id.set_time_button );

        setTimeButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               mHour = timePicker.getCurrentHour();
               mMinute = timePicker.getCurrentMinute();
                StringBuilder stringBuilder = new StringBuilder(  );
                if (mHour <= 9){
                    stringBuilder.append("0" + mHour);
                }else {
                    stringBuilder.append(mHour);
                }
                stringBuilder.append(":");
                if (mMinute <= 9){
                    stringBuilder.append("0" + mMinute);
                } else {
                    stringBuilder.append(mMinute);
                }
                time.setText(stringBuilder.toString());
               alertDialog.dismiss();
            }
        } );
        alertDialog.show();
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
}
