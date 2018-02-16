package com.necohorne.hometribe.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.necohorne.hometribe.Constants.Constants;
import com.necohorne.hometribe.R;

import java.util.Arrays;

import static android.text.TextUtils.isEmpty;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    //UI Widgets
    private EditText email;
    private EditText password;
    private Button loginButton;
    private TextView registerNewAccount;
    private TextView forgotPassword;
    private TextView resendVerification;

    //Firebase Email Login
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //Facebook Login
    private CallbackManager mCallbackManager;

    //Google Login
    private GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private Intent mLoggedInIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_login );
        mLoggedInIntent = new Intent( LoginActivity.this, MainActivity.class );

        setupFirebaseAuth();

        emailLoginSetup();

        FacebookLogin();

        googleLogin();

    }

    @Override
    protected void onStart() {
        if (AccessToken.getCurrentAccessToken() != null){
            startActivity(mLoggedInIntent);
            finish();
        }

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null){
            //signed in
            startActivity(mLoggedInIntent);
            finish();
        }

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

    private void setupFirebaseAuth(){
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null){
                    //user is signed in
                    if (user.isEmailVerified()){
                        Log.d( TAG, "onAuthStateChanged: signed in" + user.getUid());
                        startActivity(mLoggedInIntent);
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

    private void FacebookLogin(){
        final com.facebook.login.LoginManager fbLoginManager;

        fbLoginManager = com.facebook.login.LoginManager.getInstance();
         mCallbackManager = CallbackManager.Factory.create();
        fbLoginManager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                startActivity( mLoggedInIntent );
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

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
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


