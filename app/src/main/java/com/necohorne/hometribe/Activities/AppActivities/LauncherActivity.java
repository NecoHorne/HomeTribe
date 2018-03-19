package com.necohorne.hometribe.Activities.AppActivities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.necohorne.hometribe.Constants.Constants;
import com.necohorne.hometribe.R;

public class LauncherActivity extends AppCompatActivity {

    private SharedPreferences tcPrefs;
    private boolean tcAccepted = false;
    private boolean policyAccepted = false;
    private SharedPreferences mPrivacyPolicy;
    private Intent mLoginIntent;
    private Intent policyIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_launcher );
        mLoginIntent = new Intent(LauncherActivity.this, LoginActivity.class);
        policyIntent = new Intent(LauncherActivity.this, TandCActivity.class);
        timedRun();
    }

    private void timedRun(){
        tcPrefs = getSharedPreferences(Constants.PREFS_TANDC, 0);
        mPrivacyPolicy = getSharedPreferences(Constants.PREFS_PRIVACY, 0);
        tcAccepted = tcPrefs.contains(Constants.TANDC);
        policyAccepted = mPrivacyPolicy.contains(Constants.PRIVACY);
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        if (tcAccepted && policyAccepted){
                            startActivity(mLoginIntent);
                            finish();

                        } else {
                            startActivity(policyIntent);
                            finish();
                        }

                        cancel();
                    }
                },
                5000
        );
    }
}
