package com.necohorne.hometribe.Activities.AppActivities;

import android.Manifest;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.necohorne.hometribe.Activities.Dialog.ChangePhotoDialog;
import com.necohorne.hometribe.Constants.Constants;
import com.necohorne.hometribe.Models.Home;
import com.necohorne.hometribe.Models.UserProfile;
import com.necohorne.hometribe.R;
import com.necohorne.hometribe.Utilities.FilePaths;
import com.necohorne.hometribe.Utilities.UniversalImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.text.TextUtils.isEmpty;

public class UserProfileActivity extends AppCompatActivity implements
        ChangePhotoDialog.OnPhotoReceivedListener{

    private static final String TAG = "UserProfileActivity";
    private static final int REQUEST_CODE = 1234;
    private static final double MB_THRESHHOLD = 5.0;
    private static final double MB = 1000000.0;
    public static boolean isActivityRunning;

    private ImageView mProfilePicture;
    private TextView mUserName;
    private TextView mHomeTown;
    private EditText mEditName;
    private Button mSaveButton;
    private ImageButton editImage;
    private Context mContext;

    private FirebaseUser mUser;
    private String mUid;
    private String mName;
    private String mEmail;
    private Uri mPhotoUrl;
    private SharedPreferences mHomePrefs;
    private boolean mPrefBool;
    private Boolean mIsEditing;
    private String mProvider;
    private boolean mStoragePermissions;

    private Uri mSelectedImageUri;
    private Bitmap mSelectedImageBitmap;
    private byte[] mBytes;
    private double progress;
    private MenuItem mEditButton;
    private LatLng sHomeLocation;
    private LatLng myLocation;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState );
        setContentView(R.layout.activity_user_profile );
        setupUi();
        mHomePrefs = getSharedPreferences(Constants.PREFS_HOME, 0);
        mPrefBool = mHomePrefs.contains(Constants.HOME );
        initImageLoader();
        updateUserProfile();
        verifyStoragePermissions();
    }

    @Override
    protected void onStart() {
        isActivityRunning = true;
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_menu, menu);
        mEditButton = (MenuItem) menu.findItem( R.id.profile_editButton);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.profile_editButton:
                editMode();
                break;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public void getImagePath(Uri imagePath) {
        if( !imagePath.toString().equals("")){
            mSelectedImageBitmap = null;
            mSelectedImageUri = imagePath;
            Log.d(TAG, "getImagePath: got the image uri: " + mSelectedImageUri);

//            ImageLoader.getInstance().displayImage(imagePath.toString(), mProfilePicture);
        }

    }

    @Override
    public void getImageBitmap(Bitmap bitmap) {
        if(bitmap != null){
            mSelectedImageUri = null;
            mSelectedImageBitmap = bitmap;
            Log.d(TAG, "getImageBitmap: got the image bitmap: " + mSelectedImageBitmap);

            mProfilePicture.setImageBitmap(bitmap);
        }
    }

    @Override
    protected void onStop() {
        isActivityRunning = false;
        super.onStop();
    }

    private void initImageLoader(){
        UniversalImageLoader imageLoader = new UniversalImageLoader(UserProfileActivity.this);
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }

    private void setupUi(){
        addMobSetup();
        mIsEditing = false;
        mProfilePicture = findViewById( R.id.profile_circle_image);
        mUserName = findViewById(R.id.profile_display_name);
        mHomeTown = findViewById(R.id.profile_display_town);
        mEditName = findViewById(R.id.user_profile_name_edit );
        mSaveButton = findViewById(R.id.profile_save_button);
        editImage = findViewById(R.id.profile_edit_picture);
        mContext = getApplicationContext();
    }

    private void editMode() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mEditButton.setVisible(false);
        mIsEditing = true;
        mUserName.setVisibility(View.INVISIBLE);
        mEditName.setVisibility(View.VISIBLE);
        mEditName.setText(user.getDisplayName());
        if (mProvider.equals("[password]")){
            editImage.setVisibility(View.VISIBLE);
            editImage.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mStoragePermissions){
                        ChangePhotoDialog dialog = new ChangePhotoDialog();
                        dialog.show(getFragmentManager(), "dialog_changephoto");
                    }else{
                        verifyStoragePermissions();
                    }
                }
            } );
        }
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
        mEditButton.setVisible(true);
        mUserName.setVisibility(View.VISIBLE);
        mEditName.setVisibility(View.INVISIBLE);
        mSaveButton.setVisibility(View.INVISIBLE);
        editImage.setVisibility( View.INVISIBLE );
    }

    private void updateUserProfile(){
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mProvider = mUser.getProviders().toString();

        if (mProvider.equals("[password]")){
            mUid = mUser.getUid();
            mName = mUser.getDisplayName();
            mEmail = mUser.getEmail();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference();
            Query query = userRef.child(getString(R.string.dbnode_user)).child(mUser.getUid());
            Log.d( TAG, "Marker Query: "+ query);
            query.addListenerForSingleValueEvent( new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        Log.d( TAG, "Marker Datasnapshot" );
                        Map<String, Object> objectMap = (HashMap<String, Object>) dataSnapshot.getValue();
                        try {
                            String url = (String) objectMap.get( "profile_image" );
                            if (!url.equals("content://com.android.providers.media.documents/document/image%3A45")
                                    && !url.equals("null")){
                                mPhotoUrl = Uri.parse(url);
                                Picasso.with(UserProfileActivity.this)
                                        .load(mPhotoUrl)
                                        .into(mProfilePicture );
                                mUserName.setText(mName);
                            }else {
                                Bitmap bitmap = getBitmap(R.mipmap.ic_launcher_foreground_icon);
                                mProfilePicture.setImageBitmap(bitmap);
                                mUserName.setText(mName);
                            }
                        }catch (NullPointerException e){
                            Log.d( TAG, "no profile picture set " + e.toString());
                            Bitmap bitmap = getBitmap(R.mipmap.ic_launcher_foreground_icon);
                            mProfilePicture.setImageBitmap(bitmap);
                            mUserName.setText(mName);
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            } );
        }else if (mProvider.equals("[facebook.com]")){
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
        }else if (mProvider.equals("[google.com]")){
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
                            .setPhotoUri(mSelectedImageUri)
                            .build();
                    user.updateProfile(profileUpdate).addOnCompleteListener( new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Log.d( TAG, "onComplete: User Profile Updated." );
                                Toast.makeText(UserProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbnode_user)).child(user.getUid()).child("user_name");
                                reference.setValue(newDisplayName);
                                if(mSelectedImageUri != null){
                                    uploadNewPhoto(mSelectedImageUri);
                                }else if(mSelectedImageBitmap  != null){
                                    uploadNewPhoto(mSelectedImageBitmap);
                                }
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

    public void verifyStoragePermissions(){
        Log.d(TAG, "verifyPermissions: asking user for permissions.");
        String[] permissions = {android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};
        if (ContextCompat.checkSelfPermission(UserProfileActivity.this,
                permissions[0] ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(UserProfileActivity.this,
                permissions[1] ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(UserProfileActivity.this,
                permissions[2] ) == PackageManager.PERMISSION_GRANTED) {
            mStoragePermissions = true;
        } else {
            ActivityCompat.requestPermissions(
                    UserProfileActivity.this,
                    permissions,
                    REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.d(TAG, "onRequestPermissionsResult: requestCode: " + requestCode);
        switch(requestCode){
            case REQUEST_CODE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG, "onRequestPermissionsResult: User has allowed permission to access: " + permissions[0]);
                }
                break;
        }
    }

    public void uploadNewPhoto(Uri imageUri){

        Log.d(TAG, "uploadNewPhoto: uploading new profile photo to firebase storage.");
        BackgroundImageResize resize = new BackgroundImageResize(null);
        resize.execute(imageUri);
    }

    public void uploadNewPhoto(Bitmap imageBitmap){

        Log.d(TAG, "uploadNewPhoto: uploading new profile photo to firebase storage.");
        BackgroundImageResize resize = new BackgroundImageResize(imageBitmap);
        Uri uri = null;
        resize.execute(uri);
    }

    public class BackgroundImageResize extends AsyncTask<Uri, Integer, byte[]> {

        Bitmap mBitmap;
        public BackgroundImageResize(Bitmap bm) {
            if(bm != null){
                mBitmap = bm;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(UserProfileActivity.this, "compressing image", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected byte[] doInBackground(Uri... params ) {
            Log.d(TAG, "doInBackground: started.");

            if(mBitmap == null){

                try {
                    mBitmap = MediaStore.Images.Media.getBitmap( UserProfileActivity.this.getContentResolver(), params[0]);
                    Log.d(TAG, "doInBackground: bitmap size: megabytes: " + mBitmap.getByteCount()/MB + " MB");
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: IOException: ", e.getCause());
                }
            }

            byte[] bytes = null;
            for (int i = 1; i < 11; i++){
                if(i == 10){
                    Toast.makeText(UserProfileActivity.this, "That image is too large.", Toast.LENGTH_SHORT).show();
                    break;
                }
                bytes = getBytesFromBitmap(mBitmap,100/i);
                Log.d(TAG, "doInBackground: megabytes: (" + (11-i) + "0%) "  + bytes.length/MB + " MB");
                if(bytes.length/MB  < MB_THRESHHOLD){
                    return bytes;
                }
            }
            return bytes;
        }


        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            mBytes = bytes;
            //execute the upload
            executeUploadTask();
        }
    }

    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }

    private void executeUploadTask(){
        FilePaths filePaths = new FilePaths();
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + FirebaseAuth.getInstance().getCurrentUser().getUid()
                        + "/profile_image");

        if(mBytes.length/MB < MB_THRESHHOLD) {
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpg")
                    .setContentLanguage("en")
                    .build();
            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(mBytes, metadata);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri firebaseURL = taskSnapshot.getDownloadUrl();
                    Toast.makeText(UserProfileActivity.this, "Upload Success", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onSuccess: firebase download url : " + firebaseURL.toString());
                    FirebaseDatabase.getInstance().getReference()
                            .child(getString(R.string.dbnode_user))
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(getString(R.string.field_profile_image))
                            .setValue(firebaseURL.toString());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(UserProfileActivity.this, "could not upload photo", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double currentProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    if(currentProgress > (progress + 15)){
                        progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        Log.d(TAG, "onProgress: Upload is " + progress + "% done");
                        Toast.makeText(UserProfileActivity.this, progress + "%", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            ;
        }else{
            Toast.makeText(this, "Image is too Large", Toast.LENGTH_SHORT).show();
        }

    }

    private Bitmap getBitmap(int drawableRes) {
        Drawable drawable = getResources().getDrawable(drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public void setupBio(FirebaseUser user) {
        SharedPreferences homePrefs = mContext.getSharedPreferences( Constants.PREFS_HOME, 0 );
        if (homePrefs.contains( Constants.HOME )) {
            Gson gson = new Gson();
            String json = homePrefs.getString( Constants.HOME, "" );
            Home home = gson.fromJson( json, Home.class );
            sHomeLocation = home.getLocation();

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child( mContext.getString( R.string.dbnode_user))
                    .child(user.getUid());
            Query query = userRef;

            query.addListenerForSingleValueEvent( new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Map<String, Object> objectMap = (HashMap<String, Object>) dataSnapshot.getValue();
                        String sLocation = (objectMap.get(mContext.getString(R.string.field_user_home)).toString());

                        String bio;
                        try {
                            bio = (objectMap.get("bio").toString());
                        }catch (NullPointerException e){
                            Log.d( TAG, "bio Nullpointer " + e );
                            bio = "";
                        }

                        myLocation = getLocation(sLocation);
                        String address = getStreetAddress(myLocation);

                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Bio: "+ bio);
                        stringBuilder.append( "\n\n" );
                        stringBuilder.append("Street Address: "+ address);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            } );

        }
    }

    private LatLng getLocation(String location){
        String regex = "\\blongitude=\\b";
        String str1 = location.replaceAll( "[{]", "" );
        String str2 = str1.substring(9);
        String str3 = str2.replaceAll( "[}]", "" );
        String str4 = str3.replaceAll( regex, "" );
        String[] latlong =  str4.split(",");
        double latitude = Double.parseDouble(latlong[0]);
        double longitude = Double.parseDouble(latlong[1]);
        return new LatLng(latitude, longitude);
    }

    private String getStreetAddress(LatLng location){
        String address = null;
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1);
            if (addressList.size() > 0) {
                if (addressList.get( 0 ).getAddressLine( 0 )!= null) {
                    address = addressList.get( 0 ).getAddressLine(0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }

    private void addMobSetup(){
        String addMobAppId = "ca-app-pub-8837476093017718~3132418627";
        String otherBannerAddId = "ca-app-pub-8837476093017718/6461541501";
        MobileAds.initialize( UserProfileActivity.this, addMobAppId);
        mAdView = (AdView) findViewById( R.id.ad_view_user);
        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
//                .addTestDevice("4926DE19847C7207CFD6EE240D57FD37")
                .build();
        mAdView.loadAd(adRequest);
    }
}
