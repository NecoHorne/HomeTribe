package com.necohorne.hometribe.Activities.AppActivities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.necohorne.hometribe.Constants.Constants;
import com.necohorne.hometribe.R;

import java.io.IOException;
import java.io.InputStream;

public class TandCActivity extends AppCompatActivity {

    private CheckBox tcCheckBox;
    private Button tcButton;
    private SharedPreferences tcPrefs;
    private boolean tcAccepted = false;
    private Intent privacyIntent;
    private WebView mWebView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_tand_c );

        privacyIntent = new Intent( TandCActivity.this, PrivacyActivity.class);

        tcCheckBox = (CheckBox) findViewById( R.id.t_and_c_check_box);
        tcButton = (Button) findViewById( R.id.t_and_c_accept_button);
        tcPrefs = getSharedPreferences(Constants.PREFS_PRIVACY, 0);
        tcAccepted = tcPrefs.contains( Constants.PREFS_TANDC);
        mWebView = (WebView) findViewById(R.id.t_and_c_webview);
        String mData = getStringFromAssets( "tc.html", TandCActivity.this );
        mWebView.loadDataWithBaseURL("file///android_asset/",mData, "text/html", "utf-8", null );

        tcButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tcCheckBox.isChecked()){
                    tcPrefs = getSharedPreferences( Constants.PREFS_TANDC, 0);
                    SharedPreferences.Editor editor = tcPrefs.edit();
                    editor.putBoolean(Constants.TANDC, true);
                    editor.commit();
                    startActivity(privacyIntent);
                    finish();
                } else {
                    Toast.makeText( TandCActivity.this, "Please Accept the Terms and Conditions", Toast.LENGTH_SHORT ).show();
                }
            }
        } );

    }

    @Override
    protected void onStart() {
        if (tcAccepted){
            startActivity(privacyIntent);
            finish();
        }
        super.onStart();
    }

    @Override
    protected void onRestart() {
        if (tcAccepted){
            startActivity(privacyIntent);
            finish();
        }
        super.onRestart();
    }

    @Override
    protected void onResume() {
        if (tcAccepted){
            startActivity(privacyIntent);
            finish();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if(mWebView != null){
            mWebView.destroy();
        }
        super.onDestroy();
    }

    public static String getStringFromAssets(String name, Context p_context)
    {
        try
        {
            InputStream is = p_context.getAssets().open(name);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String bufferString = new String(buffer);
            return bufferString;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;

    }
}
