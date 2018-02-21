package com.necohorne.hometribe.Activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.necohorne.hometribe.Activities.Dialog.ResendVerificationDialog;
import com.necohorne.hometribe.Activities.Dialog.ResetPasswordDialog;
import com.necohorne.hometribe.R;

import io.fabric.sdk.android.Fabric;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import static android.text.TextUtils.isEmpty;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private Intent mLoggedInIntent;

    //------------UI WIDGETS------------//
    private EditText email;
    private EditText password;
    private Button loginButton;
    private TextView registerNewAccount;
    private TextView forgotPassword;
    private TextView resendVerification;

    //------------FIREBASE LOGIN------------//
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //------------FACEBOOK LOGIN------------//
    private CallbackManager mCallbackManager;

    //------------GOOGLE LOGIN------------//

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseUser mUser;
    private GoogleSignInOptions mGso;


    //------------ACTIVITY LIFECYCLE------------//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        Fabric.with(this, new Crashlytics());
        setContentView( R.layout.activity_login );

        mGso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString( R.string.google_auth_token_android))
                .requestEmail()
                .build();

        mLoggedInIntent = new Intent( LoginActivity.this, MainActivity.class );

        mAuth = FirebaseAuth.getInstance();

        setupFirebaseAuth();

        emailLoginSetup();

        FacebookLogin();

        googleLogin();

    }

    @Override
    protected void onStart() {

        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);

        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null){
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        }
    }

    //------------Authentication------------//
    private void setupFirebaseAuth(){
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                mUser = firebaseAuth.getCurrentUser();

                if (mUser != null) {
                    //user is signed in
                    String provider = mUser.getProviders().toString();

                    if (mUser.isEmailVerified()) {
                        Log.d( TAG, "onAuthStateChanged: signed in" + mUser.getUid() );
                        startActivity( mLoggedInIntent );
                        finish();
                    } else if (provider.equals( "[facebook.com]" )) {
                        Log.d( TAG, "onAuthStateChanged: Facebook signed in" + mUser.getUid() );
                        startActivity( mLoggedInIntent );
                        finish();
                    }else if (provider.equals( "[google.com]" )){
                        Log.d( TAG, "onAuthStateChanged: Google signed in" + mUser.getUid() );
                        startActivity( mLoggedInIntent );
                        finish();
                    }else {
                        Toast.makeText( LoginActivity.this, "Please Verify your email address", Toast.LENGTH_SHORT ).show();
                        FirebaseAuth.getInstance().signOut();
                    }
                }else {
                    Log.d( TAG, "onAuthStateChanged: not signed in");
                }
            }
        };
    }

    //------------LOGIN SETUP------------//
    private void emailLoginSetup(){

        email = (EditText) findViewById(R.id.login_activity_email_edit_text);
        password = (EditText) findViewById( R.id.login_activity_password_edit_text);
        loginButton = (Button) findViewById( R.id.login_activity_login_button);
        loginButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailLogin();
            }
        } );

        registerNewAccount = (TextView) findViewById( R.id.login_activity_register_new );
        registerNewAccount.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        } );

        forgotPassword = (TextView) findViewById( R.id.login_forgot_password );
        forgotPassword.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPass();
            }
        } );

        resendVerification = (TextView) findViewById( R.id.login_resend_verification);
        resendVerification.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendVerEmail();
            }
        } );

        mAuth = FirebaseAuth.getInstance();

    }

    private void forgotPass() {
        ResetPasswordDialog dialog = new ResetPasswordDialog();
        dialog.show( getFragmentManager(),"activity_reset_password");
    }

    private void resendVerEmail() {
        ResendVerificationDialog dialog = new ResendVerificationDialog();
        dialog.show( getFragmentManager(), "activity_resend_verification_dialog" );
    }

    private void createNewAccount() {
        Intent newAccountIntent = new Intent(LoginActivity.this, RegisterNewAccount.class);
        startActivity(newAccountIntent);
    }

    private void emailLogin() {
        //showDialog

        if (!isEmpty(email.getText().toString())
                && !isEmpty( password.getText().toString())){
            Log.d( TAG, "Attempting to Authenticate" );

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    //hideDialog
                }
            }).addOnFailureListener( new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText( LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                    //hideDialog
                }
            } );

        } else {
            Toast.makeText( LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
        }

    }

    private void FacebookLogin(){
        final com.facebook.login.LoginManager fbLoginManager;
        FacebookSdk.getApplicationContext();

        fbLoginManager = com.facebook.login.LoginManager.getInstance();
        mCallbackManager = CallbackManager.Factory.create();

        fbLoginManager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
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
        Button facebook_login_button = findViewById( R.id.facebook_login_button );
        facebook_login_button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fbLoginManager.logInWithReadPermissions(LoginActivity.this, Arrays.asList("email", "public_profile"));
            }
        } );
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void googleLogin(){

        mGoogleSignInClient = GoogleSignIn.getClient(this, mGso);
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
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

}