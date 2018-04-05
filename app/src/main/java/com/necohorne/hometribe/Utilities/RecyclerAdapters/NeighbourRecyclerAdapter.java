package com.necohorne.hometribe.Utilities.RecyclerAdapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.necohorne.hometribe.Activities.AppActivities.OtherUserActivity;
import com.necohorne.hometribe.Constants.Constants;
import com.necohorne.hometribe.Models.Home;
import com.necohorne.hometribe.Models.UserProfile;
import com.necohorne.hometribe.R;
import com.necohorne.hometribe.Utilities.Notifications.NewIncidentNotification;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.google.maps.android.SphericalUtil.computeDistanceBetween;

/**
 * Created by necoh on 2018/03/12.
 */

public class NeighbourRecyclerAdapter extends RecyclerView.Adapter<NeighbourRecyclerAdapter.ViewHolder>{

    private static final String TAG = "NeighbourRecyclerAdapte";
    private View mView;
    private Context mContext;
    private Context popContext;
    private ArrayList<UserProfile> mUserProfiles;
    private LatLng sHomeLocation;
    private LatLng sUserLocation;

    public NeighbourRecyclerAdapter (Context context, ArrayList<UserProfile> userProfiles){
        mContext = context;
        mUserProfiles = userProfiles;
    }

    @Override
    public NeighbourRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.neighbour_list_item, parent,false);
        popContext = parent.getContext();
        return new ViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(NeighbourRecyclerAdapter.ViewHolder holder, int position) {
        final UserProfile userProfile = mUserProfiles.get(position);
        try {
            String url = userProfile.getProfile_image();
            if (!url.equals("content://com.android.providers.media.documents/document/image%3A45") && !url.equals("null")){
                Uri photoUrl = Uri.parse(url);
                Picasso.with(mContext)
                        .load(photoUrl)
                        .into(holder.profilePicture);
            }else {
                Bitmap bitmap = getBitmap( R.mipmap.ic_launcher_foreground_round);
                holder.profilePicture.setImageBitmap(bitmap);
            }
        }catch (NullPointerException e){
            Log.d( TAG, "no profile picture set " + e.toString());
            Bitmap bitmap = getBitmap( R.mipmap.ic_launcher_foreground_round);
            holder.profilePicture.setImageBitmap(bitmap);
        }

        holder.profilePicture.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent( mContext, OtherUserActivity.class);
                profileIntent.putExtra(mContext.getString(R.string.field_other_uid), userProfile.getUser_id());
                mContext.startActivity(profileIntent);
            }
        } );

        holder.userName.setText(userProfile.getUser_name());
        setupLocations( userProfile, holder );

        holder.deleteNeighbour.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogDelete(userProfile);
            }
        } );
    }

    private void alertDialogDelete(final UserProfile userProfile) {
        //setup the dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( popContext);
        LayoutInflater inflater = (LayoutInflater) popContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View view = inflater.inflate(R.layout.neighbour_dialog_popup, null);
        dialogBuilder.setView(view);
        final AlertDialog alertDialog = dialogBuilder.create();

        //ui elements
        Button yesButton = view.findViewById( R.id.neighbour_alert_yes);
        Button noButton = view.findViewById(R.id.neighbour_alert_no);
        TextView textDialog = view.findViewById(R.id.neighbour_dialog_text);
        textDialog.setText( "Are you sure you want to remove this user as a Neighbour?" );

        alertDialog.show();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable( Color.TRANSPARENT));

        yesButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteNeighbour(userProfile);
                alertDialog.dismiss();
            }
        } );

        noButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        } );
    }

    private void deleteNeighbour(UserProfile userProfile) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String receiverId = user.getUid();
        String senderId = userProfile.getUser_id();

        DatabaseReference receiveReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(mContext.getString(R.string.dbnode_user))
                .child(receiverId)
                .child(mContext.getString( R.string.dbnode_neighbours))
                .child(mContext.getString(R.string.dbnode_user_id))
                .child(senderId);
        receiveReference.removeValue();

        DatabaseReference senderReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(mContext.getString(R.string.dbnode_user))
                .child(senderId)
                .child(mContext.getString(R.string.dbnode_neighbours))
                .child(mContext.getString(R.string.dbnode_user_id))
                .child(receiverId);
        senderReference.removeValue();

        Toast.makeText( mContext, "Neighbour Removed.", Toast.LENGTH_SHORT ).show();
    }

    @Override
    public int getItemCount() {
        return mUserProfiles.size();
    }

    private Bitmap getBitmap(int drawableRes) {
        Drawable drawable = mContext.getDrawable(drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public void setupLocations(UserProfile userProfile, final ViewHolder holder) {
        SharedPreferences homePrefs = mContext.getSharedPreferences( Constants.PREFS_HOME, 0 );
        if (homePrefs.contains( Constants.HOME )) {
            Gson gson = new Gson();
            String json = homePrefs.getString( Constants.HOME, "" );
            Home home = gson.fromJson( json, Home.class );
            sHomeLocation = home.getLocation();

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child( mContext.getString( R.string.dbnode_user))
                    .child(userProfile.getUser_id());
            Query query = userRef;

            query.addListenerForSingleValueEvent( new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Map<String, Object> objectMap = (HashMap<String, Object>) dataSnapshot.getValue();
                        String sLocation = (objectMap.get(mContext.getString(R.string.field_user_home)).toString());
                        sUserLocation = getLocation(sLocation);
                        String address = getStreetAddress(sUserLocation);
                        double distance = distanceFormatting(computeDistanceBetween(sHomeLocation, sUserLocation) / 1000);

                        holder.streetAddress.setText(address);
                        holder.distanceFromHome.setText(distance + " Km from Home");
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

    private double distanceFormatting(double distanceFromHome) {
        DecimalFormat df = new DecimalFormat("#.##");
        String dx = df.format(distanceFromHome);
        NumberFormat nf = NumberFormat.getInstance();
        double dfH = 0;
        try {
            dfH = nf.parse(dx).doubleValue();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dfH;
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

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView userName;
        public TextView streetAddress;
        public TextView distanceFromHome;
        public ImageView profilePicture;
        public ImageButton deleteNeighbour;

        public ViewHolder(View itemView) {
            super( itemView );
            userName = itemView.findViewById(R.id.neighbour_user_name);
            streetAddress = itemView.findViewById(R.id.neighbour_address);
            distanceFromHome = itemView.findViewById( R.id.neighbour_distance);
            profilePicture = itemView.findViewById( R.id.pending_profile_image );
            deleteNeighbour = itemView.findViewById( R.id.neighbour_delete_button);
        }
    }
}
