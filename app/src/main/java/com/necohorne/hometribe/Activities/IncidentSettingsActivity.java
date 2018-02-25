package com.necohorne.hometribe.Activities;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.necohorne.hometribe.Constants.Constants;
import com.necohorne.hometribe.R;

import static com.necohorne.hometribe.Constants.Constants.*;
import static com.necohorne.hometribe.Constants.Constants.TIME;

public class IncidentSettingsActivity extends AppCompatActivity {

    private RadioButton mRadioButton;
    private RadioButton mTimeRadioButton;
    private RadioGroup distanceGroup;
    private RadioGroup timegroup;
    private SharedPreferences distanceSharedPrefs;
    private SharedPreferences timeSharedPrefs;
    private SharedPreferences.Editor mEditor;
    private SharedPreferences.Editor mEditorTime;
    private TextView mDistanceTitle;
    private TextView mTimeTitle;
    private RadioButton mRadioButtonToggle;
    private RadioButton mRadioButtonToggle2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_incident_settings );
        distanceSharedPrefs = getSharedPreferences( PREFS_DISTANCE, 0);
        timeSharedPrefs = getSharedPreferences( PREFS_TIME, 0 );
        setupUI();
        getPrefs();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    public void setupUI(){

        mDistanceTitle = (TextView) findViewById( R.id.incident_settings_group_title);
        mTimeTitle = (TextView) findViewById(R.id.incident_settings_group2_title);

        distanceGroup = (RadioGroup) findViewById( R.id.incident_settings_radioGroup_distance);
        distanceGroup.setOnCheckedChangeListener( new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mRadioButton = (RadioButton) findViewById(checkedId);
                mEditor = distanceSharedPrefs.edit();

                switch (mRadioButton.getId()){
                    case R.id.incident_settings_5k_radio:
                        distanceSharedPrefs = getSharedPreferences(PREFS_DISTANCE, 0);
                        mEditor.putString(DISTANCE, FIVE_KILOMETERS);
                        mEditor.commit();
                        mDistanceTitle.setText(R.string.incidents_5k_title);
                        break;
                    case R.id.incident_settings_10k_radio:
                        distanceSharedPrefs = getSharedPreferences( PREFS_DISTANCE, 0);
                        mEditor.putString( DISTANCE, TEN_KILOMETERS);
                        mEditor.commit();
                        mDistanceTitle.setText( R.string.incidents_10k_title);
                        break;
                    case R.id.incident_settings_15k_radio:
                        distanceSharedPrefs = getSharedPreferences( PREFS_DISTANCE, 0);
                        mEditor.putString( DISTANCE, FIFTEEN_KILOMETERS);
                        mEditor.commit();
                        mDistanceTitle.setText( R.string.incidents_15k_title);
                        break;
                }
            }
        } );

        timegroup = (RadioGroup) findViewById( R.id.incident_settings_radioGroup_time);
        timegroup.setOnCheckedChangeListener( new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mTimeRadioButton = (RadioButton) findViewById(checkedId);
                mEditorTime = timeSharedPrefs.edit();
                switch (mTimeRadioButton.getId()){
                    case R.id.incident_settings_week_radio:
                        timeSharedPrefs = getSharedPreferences(PREFS_TIME, 0 );
                        mEditorTime.putString(TIME, ONE_WEEK);
                        mEditorTime.commit();
                        mTimeTitle.setText( R.string.incidents_week_title);
                        break;
                    case R.id.incident_settings_month_radio:
                        timeSharedPrefs = getSharedPreferences(PREFS_TIME, 0 );
                        mEditorTime.putString(TIME, ONE_MONTH);
                        mEditorTime.commit();
                        mTimeTitle.setText(R.string.incidents_month_title);
                        break;
                    case R.id.incident_settings_3month_radio:
                        timeSharedPrefs = getSharedPreferences(PREFS_TIME, 0 );
                        mEditorTime.putString(TIME, THREE_MONTHS);
                        mEditorTime.commit();
                        mTimeTitle.setText( R.string.incidents_3month_title);
                        break;
                }
            }
        } );

    }

    public void getPrefs(){
        String distance = distanceSharedPrefs.getString(DISTANCE, FIVE_KILOMETERS);
        String time = timeSharedPrefs.getString(TIME, ONE_MONTH);

        switch (distance){
            case FIVE_KILOMETERS:
                mRadioButtonToggle = (RadioButton) findViewById( R.id.incident_settings_5k_radio);
                mRadioButtonToggle.toggle();
                mDistanceTitle.setText( R.string.incidents_5k_title);
                break;
            case TEN_KILOMETERS:
                mRadioButtonToggle = (RadioButton) findViewById( R.id.incident_settings_10k_radio);
                mRadioButtonToggle.toggle();
                mDistanceTitle.setText( R.string.incidents_10k_title);
                break;
            case  FIFTEEN_KILOMETERS:
                mRadioButtonToggle = (RadioButton) findViewById( R.id.incident_settings_15k_radio);
                mRadioButtonToggle.toggle();
                mDistanceTitle.setText( R.string.incidents_15k_title);
                break;
        }

        switch (time){
            case ONE_WEEK:
                mRadioButtonToggle2 = (RadioButton) findViewById( R.id.incident_settings_week_radio);
                mRadioButtonToggle2.toggle();
                mTimeTitle.setText( R.string.incidents_week_title);
                break;
            case ONE_MONTH:
                mRadioButtonToggle2 = (RadioButton) findViewById( R.id.incident_settings_month_radio);
                mRadioButtonToggle2.toggle();
                mTimeTitle.setText( R.string.incidents_month_title);
                break;
            case THREE_MONTHS:
                mRadioButtonToggle2 = (RadioButton) findViewById( R.id.incident_settings_3month_radio);
                mRadioButtonToggle2.toggle();
                mTimeTitle.setText( R.string.incidents_3month_title);
                break;
        }
    }
}
