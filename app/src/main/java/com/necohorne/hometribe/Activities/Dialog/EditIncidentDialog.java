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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.necohorne.hometribe.Models.IncidentCrime;
import com.necohorne.hometribe.R;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.text.TextUtils.isEmpty;
import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by necoh on 2018/04/25.
 */

public class EditIncidentDialog extends DialogFragment {

    private static final String TAG = "DeleteIncidentDialog";

    private Context mContext;
    private View mView;
    private String keyRef;

    private FirebaseAuth mAuth;
    private Spinner mIncidentSpinner;
    private EditText mDescription;
    private EditText mPoliceNumber;
    private String mSpecifiedIncident;
    private DatabaseReference mDatabase;
    private TextView date;
    private TextView time;

    private int mMinute;
    private int mHour;
    private int mDay;
    private int mMonth;
    private int mYear;
    private Date mToday;
    private Date mDate;
    private LatLng mLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        keyRef = getArguments().getString(getString(R.string.field_key_ref));
        if (keyRef != null){
            Log.d( TAG, "Key = " + keyRef );
            getIncidentLocation(keyRef);
        }
    }

    private void getIncidentLocation(String keyRef) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbnode_incidents))
                .child(keyRef);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Map<String, Object> objectMap = (HashMap<String, Object>) dataSnapshot.getValue();
                    mLocation = getLocation(objectMap.get(getString(R.string.field_incident_location)).toString());
                    Log.d("Edit Location: ", mLocation.toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mView = inflater.inflate( R.layout.edit_incident_layout, container, false);
        mContext = getActivity();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase =  FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbnode_incidents));
        setupUi();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 1);
        mToday = calendar.getTime();

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable( Color.TRANSPARENT));
        return mView;
    }

    private void setupUi() {

        mIncidentSpinner = (Spinner) mView.findViewById(R.id.edit_add_incident_spinner );
        incidentSpinnerUI();

        date = (TextView) mView.findViewById( R.id.edit_add_alert_date );
        date.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDateFromPicker();

            }
        } );
        time = (TextView) mView.findViewById( R.id.edit_add_alert_time);
        time.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTimeFromPicker();

            }
        } );

        mDescription = (EditText) mView.findViewById(R.id.edit_add_report_incident_description);
        mPoliceNumber = (EditText) mView.findViewById(R.id.edit_add_report_incident_cas_number);


        Button submitButton = (Button) mView.findViewById(R.id.edit_add_report_incident_submit_button );

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupIncidentObject();
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
                && (mLocation != null )
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
                //todo

                //IncidentCrime Town
                Geocoder geocoder = new Geocoder( getApplicationContext(), Locale.getDefault() );
                List<Address> addresses;
                try {
                    addresses = geocoder.getFromLocation(
                            mLocation.latitude,
                            mLocation.longitude,
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
                incident.setIncident_location(mLocation);

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


                DatabaseReference newIncident = mDatabase.child(keyRef);
                newIncident.setValue(incident);

                    getDialog().dismiss();
                    Toast.makeText(mContext, "Incident Reported", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText( mContext, "All fields except Police CAS number are Mandatory", Toast.LENGTH_SHORT ).show();
        }
    }
}
