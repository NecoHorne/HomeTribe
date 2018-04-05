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
import com.necohorne.hometribe.Activities.AppActivities.MainActivity;
import com.necohorne.hometribe.Activities.AppActivities.OtherUserActivity;
import com.necohorne.hometribe.Activities.AppActivities.SearchableActivity;
import com.necohorne.hometribe.Activities.AppActivities.UserProfileActivity;
import com.necohorne.hometribe.Constants.Constants;
import com.necohorne.hometribe.Models.Home;
import com.necohorne.hometribe.Models.UserProfile;
import com.necohorne.hometribe.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.google.maps.android.SphericalUtil.computeDistanceBetween;

/**
 * Created by necoh on 2018/03/13.
 */

public class SearchRecyclerAdapter extends RecyclerView.Adapter<SearchRecyclerAdapter.ViewHolder>{

    private static final String TAG = "SearchRecyclerAdapter";
    private View mView;
    private Context mContext;
    private Context popContext;
    private ArrayList<UserProfile> mUserProfiles;
    private boolean prefBool;
    private SharedPreferences mHomePrefs;

    public SearchRecyclerAdapter(Context context, ArrayList<UserProfile> userProfiles) {
        mContext = context;
        mUserProfiles = userProfiles;
    }

    @Override
    public SearchRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_list_item, parent,false);

        //to call an alert dialog from recycler, the context from the parent needs to be used.
        popContext = parent.getContext();

        mHomePrefs = mContext.getSharedPreferences(Constants.PREFS_HOME, 0);
        prefBool = mHomePrefs.contains(Constants.HOME);

        return new ViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(SearchRecyclerAdapter.ViewHolder holder, int position) {
        final UserProfile userProfile = mUserProfiles.get(position);
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        try {
            String url = userProfile.getProfile_image();
            if (!url.equals("content://com.android.providers.media.documents/document/image%3A45") && !url.equals("null")){
                Uri photoUrl = Uri.parse(url);
                Picasso.with(mContext)
                        .load(photoUrl)
                        .into(holder.searchProfilePicture);
            }else {
                Bitmap bitmap = getBitmap( R.mipmap.ic_launcher_foreground_round);
                holder.searchProfilePicture.setImageBitmap(bitmap);
            }
        }catch (NullPointerException e){
            Log.d( TAG, "no profile picture set " + e.toString());
            Bitmap bitmap = getBitmap( R.mipmap.ic_launcher_foreground_round);
            holder.searchProfilePicture.setImageBitmap(bitmap);
        }

        holder.searchProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userProfile.getUser_id().equals(user.getUid())){
                    mContext.startActivity(new Intent( mContext, UserProfileActivity.class));
                }else {
                    Intent profileIntent = new Intent( mContext, OtherUserActivity.class);
                    profileIntent.putExtra(mContext.getString(R.string.field_other_uid), userProfile.getUser_id());
                    mContext.startActivity(profileIntent);
                }
            }
        } );

        holder.searchUserName.setText(userProfile.getUser_name());

        checkForNeighbourRequest(userProfile, holder);

    }

    private void checkForNeighbourRequest(final UserProfile userProfile, final ViewHolder holder){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (userProfile.getUser_id().equals( user.getUid() )){
            holder.searchAddButton.setVisibility(View.INVISIBLE);
        } else {
            DatabaseReference requestReference = FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child(mContext.getString(R.string.dbnode_user))
                    .child(user.getUid())
                    .child(mContext.getString(R.string.dbnode_sent_neighbour_requests))
                    .child("user_id")
                    .child(userProfile.getUser_id());

            final DatabaseReference neighboursRef = FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child(mContext.getString(R.string.dbnode_user))
                    .child(user.getUid())
                    .child(mContext.getString(R.string.dbnode_neighbours))
                    .child("user_id")
                    .child(userProfile.getUser_id());

            Query requestQuery = requestReference;
            requestQuery.addListenerForSingleValueEvent( new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        if (dataSnapshot.getValue().equals(userProfile.getUser_id())) {
                            holder.searchAddButton.setVisibility( View.INVISIBLE );
                        }else {
                            setUpAddButton(userProfile, holder);
                        }
                    }else {
                        Query neighbourQuery = neighboursRef;
                        neighbourQuery.addListenerForSingleValueEvent( new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    holder.searchAddButton.setVisibility( View.INVISIBLE );
                                } else {
                                    setUpAddButton( userProfile, holder );
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        } );
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            } );
        }
    }

    private void neighbourRequest(UserProfile userProfile) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference sendReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(mContext.getString(R.string.dbnode_user))
                .child(user.getUid())
                .child(mContext.getString( R.string.dbnode_sent_neighbour_requests))
                .child("user_id")
                .child(userProfile.getUser_id());
        sendReference.setValue(userProfile.getUser_id());

        DatabaseReference requestReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(mContext.getString(R.string.dbnode_user))
                .child(userProfile.getUser_id())
                .child(mContext.getString(R.string.dbnode_neighbour_requests))
                .child("user_id")
                .child(user.getUid());
        requestReference.setValue(user.getUid());

        Toast.makeText(mContext, "Neighbour Request Sent!", Toast.LENGTH_SHORT).show();
    }

    private void setUpAddButton(final UserProfile userProfile, final ViewHolder holder){
        holder.searchAddButton.setImageDrawable(mContext.getDrawable(R.drawable.ic_person_add_black_24dp));
        holder.searchAddButton.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
        holder.searchAddButton.setVisibility(View.VISIBLE);
        holder.searchAddButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkDistance(userProfile) < 4){
                    requestAlertDialog(userProfile, holder);
                } else {
                    Toast.makeText( mContext, "Sorry this user lives too far to be added as a neighbour.", Toast.LENGTH_SHORT).show();
                }
            }
        } );
    }

    private void requestAlertDialog(final UserProfile userProfile, final ViewHolder holder) {
        //setup the dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( popContext);
        LayoutInflater inflater = (LayoutInflater) popContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View view = inflater.inflate(R.layout.neighbour_dialog_popup, null);
        dialogBuilder.setView(view);
        final AlertDialog alertDialog = dialogBuilder.create();

        //ui elements
        Button yesButton = view.findViewById( R.id.neighbour_alert_yes);
        Button noButton = view.findViewById(R.id.neighbour_alert_no);

        alertDialog.show();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable( Color.TRANSPARENT));

        yesButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                neighbourRequest(userProfile);
                holder.searchAddButton.setVisibility(View.INVISIBLE);
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

    private double checkDistance(UserProfile profile) {
        double distance = 0;
        if (profile.getHome_location() != null & getHomeLatLng() != null){
            distance = computeDistanceBetween(profile.getHome_location(), getHomeLatLng());
        }
        return distance / 1000;
    }

    private LatLng getHomeLatLng() {
        LatLng homeLatLng = null;
        if (prefBool) {
            Gson gson = new Gson();
            String json = mHomePrefs.getString( Constants.HOME, "" );
            Home mHome = gson.fromJson( json, Home.class );
            if (mHome.getLocation() != null) {
                homeLatLng = mHome.getLocation();
            }
        }
        return homeLatLng;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView searchProfilePicture;
        public TextView searchUserName;
        public ImageButton searchAddButton;

        public ViewHolder(View itemView) {
            super( itemView );
            searchProfilePicture = itemView.findViewById(R.id.pending_profile_image );
            searchUserName = itemView.findViewById(R.id.pending_user_name );
            searchAddButton = itemView.findViewById(R.id.search_add_button);
        }
    }
}
