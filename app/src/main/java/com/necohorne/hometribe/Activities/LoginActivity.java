package com.necohorne.hometribe.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.necohorne.hometribe.Constants.Constants;
import com.necohorne.hometribe.R;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    //Facebook Login
    private CallbackManager mCallbackManager;

    //Google Login
    private GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_login );

        customFacebookLogin();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        googleLogin();

    }


    private void customFacebookLogin(){
        final com.facebook.login.LoginManager fbLoginManager;

        fbLoginManager = com.facebook.login.LoginManager.getInstance();
         mCallbackManager = CallbackManager.Factory.create();
        fbLoginManager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                startActivity( new Intent( LoginActivity.this, MainActivity.class ) );
                Toast.makeText( LoginActivity.this, "Logged In!", Toast.LENGTH_LONG ).show();
                finish();
            }

            @Override
            public void onCancel() {
                Toast.makeText( LoginActivity.this, "Login Cancelled", Toast.LENGTH_LONG ).show();
            }

            @Override
            public void onError(FacebookException e) {
                Toast.makeText( LoginActivity.this, "Something went wrong", Toast.LENGTH_LONG ).show();
            }
        });

        Button facebook_login_button = (Button) findViewById( R.id.facebook_login_button );
        facebook_login_button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fbLoginManager.logInWithReadPermissions(LoginActivity.this, Arrays.asList("email", "public_profile", "user_birthday"));
            }
        } );


    }

    private void googleLogin(){
        Button signInButton = (Button) findViewById(R.id.google_login_button);

        signInButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        } );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult( requestCode, resultCode, data );

        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult( task );
            super.onActivityResult( requestCode, resultCode, data );
        }
    }

    @Override
    protected void onStart() {
        if (AccessToken.getCurrentAccessToken() != null)
        {
            startActivity( new Intent( LoginActivity.this, MainActivity.class ) );
            finish();
        }

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if (account != null)
        {
            //signed in
            startActivity( new Intent( LoginActivity.this, MainActivity.class ) );
            finish();
        }

        super.onStart();
    }

    private void handleSignInResult(com.google.android.gms.tasks.Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            startActivity( new Intent( LoginActivity.this, MainActivity.class ) );
            Toast.makeText( LoginActivity.this, "Logged In!", Toast.LENGTH_LONG ).show();
            finish();


        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }
}


