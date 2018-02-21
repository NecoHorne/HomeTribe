package com.necohorne.hometribe.Activities.Dialog;

import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;
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

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.necohorne.hometribe.Models.IncidentCrime;
import com.necohorne.hometribe.R;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.text.TextUtils.isEmpty;

public class AddIncidentDialog extends DialogFragment{

    private static final String TAG = "Incident Dialog ";

    private Context mContext;
    private View mView;

    private Spinner mIncidentSpinner;
    private Spinner mProvinceSpinner;
    private Spinner mHourSpinner;
    private Spinner mMinuteSpinner;
    private EditText mDay;
    private EditText mMonth;
    private EditText mYear;
    private EditText mTime;
    private AutoCompleteTextView mCity;
    private EditText mStreetNum;
    private EditText mStreetName;
    private EditText mDescription;
    private EditText mPoliceNumber;
    private Button mSubmitBotton;

    private String[] mTowns;
    private ArrayAdapter<String> mCityAdapter;
    private String mChosenProvince;
    private ArrayAdapter<CharSequence> mProvinceAdapter;
    private ArrayAdapter<CharSequence> mIncidentsAdapter;
    private String mSpecifiedIncident;
    private LatLng mLocation;
    private IncidentCrime mIncident;
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private String mHours;
    private String mMinutes;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.activity_add_incident_dialog , container, false);
        mContext = getActivity();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Log.d( TAG, "incident User " + user.getUid() );

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable( Color.TRANSPARENT));

        setupUi();

        return mView;
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

        mStreetNum = (EditText) mView.findViewById(R.id.report_incident_street_num);
        mStreetName = (EditText) mView.findViewById(R.id.incident_report_street_name);

        mDescription = (EditText) mView.findViewById(R.id.report_incident_description);
        mPoliceNumber = (EditText) mView.findViewById(R.id.report_incident_cas_number);
        mSubmitBotton = (Button) mView.findViewById(R.id.report_incident_submit_button );

        mSubmitBotton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setupIncidentObject();

                Toast.makeText( mContext, "Incident Reported", Toast.LENGTH_SHORT ).show();
                getDialog().dismiss();
                }
            }
         );
    }

    private void incidentSpinnerUI(){
        mIncidentsAdapter = ArrayAdapter.createFromResource( mContext, R.array.incidents_array, R.layout.spinner_item);
        mIncidentsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mIncidentSpinner.setAdapter( mIncidentsAdapter );
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
        mProvinceAdapter = ArrayAdapter.createFromResource( mContext, R.array.provinces_array, R.layout.spinner_item );
        mProvinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mProvinceSpinner.setAdapter( mProvinceAdapter );
        mProvinceSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                townByProvinceSetup();
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

    private void townByProvinceSetup(){
        mChosenProvince = mProvinceSpinner.getSelectedItem().toString();
        switch (mChosenProvince){
            case "Eastern Cape":
                Log.d(TAG, "Switch: Eastern cape chosen");
                mCity = (AutoCompleteTextView) mView.findViewById(R.id.report_incident_town);
                mTowns = getResources().getStringArray(R.array.eastern_cape_towns);
                mCityAdapter = new ArrayAdapter<>( mContext, android.R.layout.simple_list_item_1, mTowns);
                mCity.setAdapter(mCityAdapter);
                break;
            case "Free State":
                Log.d(TAG, "switch: Free State chosen");
                mCity = (AutoCompleteTextView) mView.findViewById(R.id.report_incident_town);
                mTowns = getResources().getStringArray(R.array.free_state_towns);
                mCityAdapter = new ArrayAdapter<>( mContext, android.R.layout.simple_list_item_1, mTowns );
                mCity.setAdapter(mCityAdapter);
                break;
            case "Gauteng":
                Log.d(TAG, "Switch: Gauteng chosen");
                mCity = (AutoCompleteTextView) mView.findViewById(R.id.report_incident_town);
                mTowns = getResources().getStringArray(R.array.gauteng_towns);
                mCityAdapter = new ArrayAdapter<>( mContext, android.R.layout.simple_list_item_1, mTowns );
                mCity.setAdapter(mCityAdapter);
                break;
            case "KwaZulu Natal":
                Log.d(TAG, "Switch: KwaZulu Natal chosen");
                mCity = (AutoCompleteTextView) mView.findViewById(R.id.report_incident_town);
                mTowns = getResources().getStringArray(R.array.kwazulu_natal_towns);
                mCityAdapter = new ArrayAdapter<>( mContext, android.R.layout.simple_list_item_1, mTowns );
                mCity.setAdapter(mCityAdapter);
                break;
            case "Limpopo":
                Log.d(TAG, "Switch: Limpopo chosen");
                mCity = (AutoCompleteTextView) mView.findViewById(R.id.report_incident_town);
                mTowns = getResources().getStringArray(R.array.limpopo_towns);
                mCityAdapter = new ArrayAdapter<>( mContext, android.R.layout.simple_list_item_1, mTowns );
                mCity.setAdapter(mCityAdapter);
                break;
            case "Mpumalanga":
                Log.d(TAG, "Switch: Mpumalanga chosen");
                mCity = (AutoCompleteTextView) mView.findViewById(R.id.report_incident_town);
                mTowns = getResources().getStringArray(R.array.mpumalanga_towns);
                mCityAdapter = new ArrayAdapter<>( mContext, android.R.layout.simple_list_item_1, mTowns );
                mCity.setAdapter(mCityAdapter);
                break;
            case "North West":
                Log.d(TAG, "Switch: North West chosen");
                mCity = (AutoCompleteTextView) mView.findViewById(R.id.report_incident_town);
                mTowns = getResources().getStringArray(R.array.north_west_towns);
                mCityAdapter = new ArrayAdapter<>( mContext, android.R.layout.simple_list_item_1, mTowns );
                mCity.setAdapter(mCityAdapter);
                break;
            case "Northern Cape":
                Log.d(TAG, "Switch: Northern Cape chosen");
                mCity = (AutoCompleteTextView) mView.findViewById(R.id.report_incident_town);
                mTowns = getResources().getStringArray(R.array.northern_cape_towns);
                mCityAdapter = new ArrayAdapter<>( mContext, android.R.layout.simple_list_item_1, mTowns );
                mCity.setAdapter(mCityAdapter);
                break;
            case "Western Cape":
                Log.d(TAG, "Switch: Western Cape chosen");
                mCity = (AutoCompleteTextView) mView.findViewById(R.id.report_incident_town);
                mTowns = getResources().getStringArray(R.array.western_cape_towns);
                mCityAdapter = new ArrayAdapter<>( mContext, android.R.layout.simple_list_item_1, mTowns );
                mCity.setAdapter(mCityAdapter);
                break;
        }
    }

    private void setupIncidentObject() {

        if (!isEmpty(mDay.getText().toString()
        )       || !isEmpty(mMonth.getText().toString())
                || !isEmpty(mYear.getText().toString())
                || !mHours.equals("Hour")
                || !mMinutes.equals("Minutes")
                || !isEmpty(mStreetNum.getText().toString())
                || !isEmpty(mStreetName.getText().toString())
                || !isEmpty(mCity.getText().toString())
                || !isEmpty(mDescription.getText().toString())) {

            //IncidentCrime Type
            mIncident = new IncidentCrime( mSpecifiedIncident );

            //IncidentCrime Date and time
            int year = Integer.parseInt(mYear.getText().toString());
            int month = Integer.parseInt(mMonth.getText().toString());
            int day = Integer.parseInt(mDay.getText().toString());
            int hour = Integer.parseInt(mHours);
            int minute = Integer.parseInt(mMinutes);
            Date date = getDate( year, month, day, hour, minute);
            mIncident.setIncident_date( date);

            //IncidentCrime Country
            String locale = mContext.getResources().getConfiguration().locale.getDisplayCountry();
            mIncident.setCountry(locale);

            //IncidentCrime Town
            mIncident.setTown( mCity.getText().toString());

            //IncidentCrime Street Address
            StringBuilder sbStreetAddress = new StringBuilder();
            sbStreetAddress.append(mStreetNum.getText().toString());
            sbStreetAddress.append(" ");
            sbStreetAddress.append(mStreetName.getText().toString());
            String streetAddress = sbStreetAddress.toString();
            mIncident.setStreet_address(streetAddress);

            //IncidentCrime Location
            try {
                List<Address> result = new Geocoder( mContext, Locale.getDefault() ).getFromLocationName( sbStreetAddress.toString(), 1 );
                if (result.get(0) != null){
                    Address address = result.get(0);
                    mLocation = new LatLng(address.getLatitude(), address.getLongitude());
                    Log.d( TAG, "address: " + mLocation.toString() );
                    mIncident.setIncident_location(mLocation);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            //IncidentCrime Description
            mIncident.setIncident_description(mDescription.getText().toString());

            //IncidentCrime Police CAS Number
            if (isEmpty(mPoliceNumber.getText().toString())){
                mIncident.setPolice_cas_number("No CAS Number at this time");
            }else{
                mIncident.setPolice_cas_number( mPoliceNumber.getText().toString());
            }

            //IncidentCrime Reported By
            mUser = mAuth.getCurrentUser();
            Log.d(TAG, "incident reported by: " + mUser.getUid().toString() );
            mIncident.setReported_by(mUser.getUid());

            uploadIncidentToFirebaseDB(mIncident);

        } else {
            Toast.makeText( mContext, "All fields except Police CAS number are Mandatory", Toast.LENGTH_SHORT ).show();
        }
    }

    private Date getDate(int year, int month, int day, int hour, int minutes){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minutes);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private void uploadIncidentToFirebaseDB(IncidentCrime incident) {
        FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbnode_incidents))
                .setValue(incident);
    }
}
