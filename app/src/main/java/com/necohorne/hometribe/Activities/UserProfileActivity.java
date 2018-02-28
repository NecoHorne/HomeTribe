package com.necohorne.hometribe.Activities;

import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.necohorne.hometribe.Constants.Constants;
import com.necohorne.hometribe.Models.Home;
import com.necohorne.hometribe.Models.UserProfile;
import com.necohorne.hometribe.R;
import com.squareup.picasso.Picasso;

import static android.text.TextUtils.isEmpty;

public class UserProfileActivity extends AppCompatActivity {

    private static final String TAG = "UserProfileActivity";

    private ImageView mProfilePicture;
    private TextView mUserName;
    private TextView mHomeTown;
    private ImageButton editButton;
    private EditText mEditName;
    private Button mSaveButton;

    private FirebaseUser mUser;
    private String mUid;
    private String mName;
    private String mEmail;
    private Uri mPhotoUrl;
    private SharedPreferences mHomePrefs;
    private boolean mPrefBool;
    private Boolean mIsEditing;
    private String mProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_user_profile );
        setupUi();
        mHomePrefs = getSharedPreferences(Constants.PREFS_HOME, 0);
        mPrefBool = mHomePrefs.contains(Constants.HOME );
        updateUserProfile();
    }

    @Override
    public void onBackPressed() {
        if (mIsEditing){
            exitEditMode();
            Toast.makeText( UserProfileActivity.this, "Editing Canceled", Toast.LENGTH_SHORT).show();
        }else {
            super.onBackPressed();
        }
    }

    private void setupUi(){
        mIsEditing = false;
        mProfilePicture = findViewById( R.id.profile_image);
        mUserName = findViewById(R.id.profile_display_name);
        mHomeTown = findViewById(R.id.profile_display_town);
        editButton = findViewById(R.id.imageButton );
        if (mUser != null){
            if (mProvider.equals("[password]")) {
                editButton.setVisibility(View.VISIBLE);
            }else {
                editButton.setVisibility(View.INVISIBLE);
            }
        }




        editButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editMode();
            }
        } );

        mEditName = findViewById(R.id.user_profile_name_edit );
        mSaveButton = findViewById(R.id.profile_save_button);
    }

    private void editMode() {
        mIsEditing = true;
        editButton.setVisibility(View.INVISIBLE);
        mUserName.setVisibility(View.INVISIBLE);
        mEditName.setVisibility(View.VISIBLE);
        mSaveButton.setVisibility(View.VISIBLE);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateFirebaseUserDetails();
            }
        } );
    }

    private void exitEditMode(){
        mIsEditing = false;
        if (mUser != null){
            if (mProvider.equals("[password]")) {
                editButton.setVisibility(View.VISIBLE);
            }else {
                editButton.setVisibility(View.INVISIBLE);
            }
        }
        mUserName.setVisibility(View.VISIBLE);
        mEditName.setVisibility(View.INVISIBLE);
        mSaveButton.setVisibility(View.INVISIBLE);

    }

    private void updateUserProfile(){
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mProvider = mUser.getProviders().toString();

        if (mUser != null){
            if (mProvider.equals("[password]")) {
                mUid = mUser.getUid();
                mName = mUser.getDisplayName();
                mEmail = mUser.getEmail();
                mPhotoUrl = mUser.getPhotoUrl();
            }else if (mProvider.equals( "[facebook.com]" )) {
                for (UserInfo profile : mUser.getProviderData()) {
                    String providerId = profile.getProviderId();
                    String facebookUserId = "";
                    mUid = profile.getUid();
                    mName = profile.getDisplayName();
                    mEmail = profile.getEmail();
                    mPhotoUrl = profile.getPhotoUrl();
                    if(FacebookAuthProvider.PROVIDER_ID.equals(profile.getProviderId())) {
                        facebookUserId = profile.getUid();
                        String photoUrl = "https://graph.facebook.com/" + facebookUserId + "/picture?height=500";
                        mPhotoUrl = Uri.parse(photoUrl);
                    }
                }
            }else if (mProvider.equals( "[google.com]" )){
                for (UserInfo profile : mUser.getProviderData()) {
                    String providerId = profile.getProviderId();
                    mUid = profile.getUid();
                    mName = profile.getDisplayName();
                    mEmail = profile.getEmail();
                    mPhotoUrl = profile.getPhotoUrl();
                }
            }
            Picasso.with(UserProfileActivity.this)
                    .load(mPhotoUrl)
                    .into(mProfilePicture);
            mUserName.setText(mName);
            if (mPrefBool){
                Gson gson = new Gson();
                String json = mHomePrefs.getString(Constants.HOME, "" );
                Home home = gson.fromJson(json, Home.class);
                mHomeTown.setText(home.getTown_city());
            }
        }
    }

    private void updateFirebaseUserDetails(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (!isEmpty(mEditName.getText().toString())){
            if (mEditName.getText().toString().length() < 4){
                Toast.makeText( UserProfileActivity.this, "Name Has to be at least 4 characters", Toast.LENGTH_SHORT).show();
            }else {
                final String newDisplayName = mEditName.getText().toString();

                if (user != null){
                    UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                            .setDisplayName(newDisplayName)
                            .setPhotoUri(Uri.parse("http://www.mstrafo.de/fileadmin/_processed_/b/1/csm_person-placeholder-male_5602d73d5e.png"))
                            .build();
                    user.updateProfile(profileUpdate).addOnCompleteListener( new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Log.d( TAG, "onComplete: User Profile Updated." );
                                Toast.makeText(UserProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbnode_user)).child(user.getUid()).child("user_name");
                                reference.setValue(newDisplayName);
                                exitEditMode();
                                recreate();
                            }else {
                                Log.d( TAG, "onComplete: User Profile Update Unsuccessful." );
                            }
                        }
                    } );
                }
            }
        }

    }
}
