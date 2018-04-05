package com.necohorne.hometribe.Activities.AppActivities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.necohorne.hometribe.Constants.Constants;
import com.necohorne.hometribe.R;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class LauncherActivity extends AppCompatActivity {

    private static final String TAG = "LauncherActivity";

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
                            if (hasInternetAccess()){
                                startActivity(mLoginIntent);
                                finish();
                            }else {
                                startActivity( new Intent( LauncherActivity.this, NoInternetActivity.class));
                                finish();
                            }

                        } else {
                            startActivity(policyIntent);
                            finish();
                        }

                        cancel();
                    }
                },
                4000
        );
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService( Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    public boolean hasInternetAccess() {
        if (isNetworkAvailable()) {
            try {
                HttpURLConnection urlc = (HttpURLConnection)
                        (new URL("http://clients3.google.com/generate_204")
                                .openConnection());
                urlc.setRequestProperty("User-Agent", "Android");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 204 &&
                        urlc.getContentLength() == 0);
            } catch (IOException e) {
                Log.e(TAG, "Error checking internet connection", e);
            }
        } else {
            Log.d(TAG, "No network available!");
        }
        return false;
    }
}
