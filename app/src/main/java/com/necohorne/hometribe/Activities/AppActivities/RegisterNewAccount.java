package com.necohorne.hometribe.Activities.AppActivities;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.necohorne.hometribe.R;

import static android.text.TextUtils.isEmpty;

public class RegisterNewAccount extends AppCompatActivity {

    private static final String TAG = "RegisterNewAccount";

    private EditText mDisplayName;
    private EditText mEmail;
    private EditText mPassword;
    private EditText mConfirmPassword;
    private Button createNewAccountButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_register_new_account );

        mAuth = FirebaseAuth.getInstance();

        setUpUi();

        createNewAccountButton = (Button) findViewById( R.id.register_account_create_button);
        createNewAccountButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        } );
    }

    @Override
    public void onBackPressed() {
        Toast.makeText( RegisterNewAccount.this, "Account Creation Canceled", Toast.LENGTH_LONG ).show();
        super.onBackPressed();
    }

    private void sendVerificationEmail(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            user.sendEmailVerification().addOnCompleteListener( new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(RegisterNewAccount.this, "Verification Email sent" , Toast.LENGTH_SHORT ).show();
                    }else {
                        Toast.makeText(RegisterNewAccount.this, "Verification Email could not be sent" , Toast.LENGTH_SHORT ).show();
                    }
                }
            } );
        }
    }

    private void setUpUi() {
        mDisplayName = (EditText) findViewById( R.id.register_account_display_name);
        mEmail = (EditText) findViewById( R.id.register_account_email);
        mPassword = (EditText) findViewById( R.id.register_account_password);
        mConfirmPassword = (EditText) findViewById( R.id.register_account_password_confirm);
    }

    private void createAccount() {
        Log.d(TAG, "createAccount has been called");

        //check for empty fields to ensure all fields are filled in.
        if (!isEmpty(mEmail.getText().toString())
                && !isEmpty( mDisplayName.getText().toString())
                && !isEmpty( mPassword.getText().toString())
                && !isEmpty( mConfirmPassword.getText().toString())){
            //check if passwords match
            if ((mPassword.getText().toString()).equals(mConfirmPassword.getText().toString())){

                mAuth.createUserWithEmailAndPassword( mEmail.getText().toString(), mPassword.getText().toString()).addOnCompleteListener(
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d(TAG, "registerEmail onComplete; " + task.isSuccessful());
                                if (task.isSuccessful()){
                                    Log.d(TAG, "registerEmail onComplete; " + FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    updateDisplayName(mDisplayName.getText().toString());
                                    sendVerificationEmail();
                                    FirebaseAuth.getInstance().signOut();
                                    startActivity( new Intent( RegisterNewAccount.this, LoginActivity.class));
                                    finish();
                                }else {
                                    Toast.makeText( RegisterNewAccount.this, "Account Creation Unsuccessful", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }else {
                Toast.makeText( RegisterNewAccount.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText( RegisterNewAccount.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDisplayName(final String displayName){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build();
        user.updateProfile(profileUpdate).addOnCompleteListener( new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "updateDisplayName onComplete; " + displayName);
            }
        } );
    }
}
