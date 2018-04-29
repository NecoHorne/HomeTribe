package com.necohorne.hometribe.Activities.AppActivities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.necohorne.hometribe.Constants.Constants;
import com.necohorne.hometribe.R;

import java.net.URL;

public class PrivacyActivity extends AppCompatActivity {

    private WebView mWebView;
    private CheckBox mCheckBox;
    private Button mButton;
    private boolean policyAccepted = false;
    private SharedPreferences mPrivacyPolicy;
    private Intent mLoginIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_privacy );
        mWebView = (WebView) findViewById(R.id.privacy_policy_webview);
        mCheckBox = (CheckBox) findViewById(R.id.privacy_policy_check);
        mButton = (Button) findViewById(R.id.privacy_policy_button);

        String privacyPolicy = "https://www.iubenda.com/privacy-policy/75710776/full-legal";
        mLoginIntent = new Intent( PrivacyActivity.this, LoginActivity.class);
        mWebView.loadUrl(privacyPolicy);
        mPrivacyPolicy = getSharedPreferences( Constants.PREFS_PRIVACY, 0);
        policyAccepted = mPrivacyPolicy.contains(Constants.PREFS_PRIVACY);

        mButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCheckBox.isChecked()){
                    mPrivacyPolicy = getSharedPreferences(Constants.PREFS_PRIVACY, 0);
                    SharedPreferences.Editor editor = mPrivacyPolicy.edit();
                    editor.putBoolean(Constants.PRIVACY, true);
                    editor.commit();
                    startActivity(mLoginIntent);
                    finish();
                }else {
                    Toast.makeText( PrivacyActivity.this, "Please Accept the Privacy Policy", Toast.LENGTH_SHORT ).show();
                }
            }
        } );

    }

    @Override
    protected void onStart() {
        if (policyAccepted){
            startActivity(mLoginIntent);
            finish();
        }
        super.onStart();
    }

    @Override
    protected void onRestart() {
        if (policyAccepted){
            startActivity(mLoginIntent);
            finish();
        }
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        if(mWebView != null){
            mWebView.destroy();
        }
        super.onDestroy();
    }
}
