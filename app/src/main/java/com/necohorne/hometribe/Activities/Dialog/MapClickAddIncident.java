package com.necohorne.hometribe.Activities.Dialog;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
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

import com.google.android.gms.maps.model.LatLng;
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

/**
 * Created by necoh on 2018/03/23.
 * This Class will get the latlng information from the map long click and open an Add incident dialog,
 * writing incident data to database
 */

public class MapClickAddIncident extends DialogFragment {

    private static final String TAG = "MapClickAddIncident";

    private View mView;
    private Context mContext;
    private FirebaseAuth mAuth;
    private Spinner mIncidentSpinner;
    private Spinner mProvinceSpinner;
    private EditText mDescription;
    private EditText mPoliceNumber;
    private String mChosenProvince;
    private String mSpecifiedIncident;
    private DatabaseReference mDatabase;
    private TextView date;
    private TextView time;
    private TextView location;
    private LatLng incidentPlace;

    private int mMinute;
    private int mHour;
    private int mDay;
    private int mMonth;
    private int mYear;
    private Date mToday;
    private Date mDate;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate( R.layout.map_click_add_incident_layout , container, false);

        mContext = getActivity();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable( Color.TRANSPARENT));
        setupDataBase();
        setupUi();
        Calendar calendar = Calendar.getInstance();
        mToday = calendar.getTime();
        return mView;
    }

    private void setupDataBase(){
        mDatabase =  FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbnode_incidents));
    }

    private void setupUi() {

        mIncidentSpinner = (Spinner) mView.findViewById(R.id.map_add_incident_spinner );
        incidentSpinnerUI();
        mProvinceSpinner = (Spinner) mView.findViewById(R.id.map_add_incident_province_spinner);
        provinceSpinnerUI();

        date = (TextView) mView.findViewById( R.id.map_add_alert_date );
        date.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDateFromPicker();

            }
        } );
        time = (TextView) mView.findViewById( R.id.map_add_alert_time);
        time.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTimeFromPicker();

            }
        } );
        location = (TextView) mView.findViewById(R.id.map_add_incident_place);
        location.setText(getLocation());

        mDescription = (EditText) mView.findViewById(R.id.map_add_report_incident_description);
        mPoliceNumber = (EditText) mView.findViewById(R.id.map_add_report_incident_cas_number);
        Button submitBotton = (Button) mView.findViewById(R.id.map_add_report_incident_submit_button );

        submitBotton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setupIncidentObject();
                }
            }
        );
    }

    private String getLocation(){
        String location = getArguments().getString("map_click_location");
        String sub = location.substring(9);
        String str1 = sub.replaceAll("[(]", "" );
        String str2 = str1.replaceAll("[)]", "" );
        String[] latLong = str2.split(",");
        double latitude = Double.parseDouble(latLong[0]);
        double longitude = Double.parseDouble(latLong[1]);
        incidentPlace = new LatLng(latitude, longitude);
        return new String(latitude + " , " + longitude);
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
                            incidentPlace.latitude,
                            incidentPlace.longitude,
                            1 );
                    if (addresses.size() > 0) {

                        if (addresses.get( 0 ).getSubLocality() != null) {
                            incident.setTown(addresses.get( 0 ).getSubLocality());
                        }

                        if (addresses.get( 0 ).getAddressLine( 0 ) != null) {
                            incident.setStreet_address(addresses.get( 0 ).getAddressLine(0));
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //IncidentCrime Location
                incident.setIncident_location(incidentPlace);

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

}
