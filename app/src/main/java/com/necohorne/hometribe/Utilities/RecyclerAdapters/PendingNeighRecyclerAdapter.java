package com.necohorne.hometribe.Utilities.RecyclerAdapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.necohorne.hometribe.Models.UserProfile;
import com.necohorne.hometribe.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by necoh on 2018/03/15.
 */

public class PendingNeighRecyclerAdapter extends RecyclerView.Adapter<PendingNeighRecyclerAdapter.ViewHolder> {

    private static final String TAG = "PendingNeighRecyclerAda";
    private View mView;
    private Context mContext;
    private ArrayList<UserProfile> mUserProfiles;

    public PendingNeighRecyclerAdapter(Context context, ArrayList<UserProfile> userProfiles) {
        mContext = context;
        mUserProfiles = userProfiles;
    }

    @Override
    public PendingNeighRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mView = LayoutInflater.from(parent.getContext()).inflate( R.layout.pending_neighbour_list_item, parent,false);

        return new ViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(PendingNeighRecyclerAdapter.ViewHolder holder, int position) {
        final UserProfile userProfile = mUserProfiles.get(position);

        try {
            String url = userProfile.getProfile_image();
            if (!url.equals("content://com.android.providers.media.documents/document/image%3A45") && !url.equals("null")){
                Uri photoUrl = Uri.parse(url);
                Picasso.with(mContext)
                        .load(photoUrl)
                        .into(holder.pendingProfileImage);
            }else {
                Bitmap bitmap = getBitmap( R.mipmap.ic_launcher_foreground_round);
                holder.pendingProfileImage.setImageBitmap(bitmap);
            }
        }catch (NullPointerException e){
            Log.d( TAG, "no profile picture set " + e.toString());
            Bitmap bitmap = getBitmap( R.mipmap.ic_launcher_foreground_round);
            holder.pendingProfileImage.setImageBitmap(bitmap);
        }
        holder.pendingUserName.setText( userProfile.getUser_name());
        holder.pendingDeclineButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                declineRequest(userProfile);
            }
        } );

    }

    @Override
    public int getItemCount() {
        return mUserProfiles.size();
    }

    private Bitmap getBitmap(int drawableRes) {
        Drawable drawable = mContext.getResources().getDrawable(drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private void declineRequest(UserProfile userProfile){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String receiverId = user.getUid();
        String senderId = userProfile.getUser_id();

        DatabaseReference receiveReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(mContext.getString(R.string.dbnode_user))
                .child(receiverId)
                .child(mContext.getString( R.string.dbnode_neighbour_requests))
                .child("user_id")
                .child(senderId);
        receiveReference.removeValue();

        DatabaseReference senderReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(mContext.getString(R.string.dbnode_user))
                .child(senderId)
                .child(mContext.getString(R.string.dbnode_sent_neighbour_requests))
                .child("user_id")
                .child(receiverId);
        senderReference.removeValue();

        Toast.makeText(mContext, "Neighbour Request removed!", Toast.LENGTH_SHORT).show();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView pendingProfileImage;
        private TextView pendingUserName;
        private Button pendingAcceptButton;
        private Button pendingDeclineButton;

        public ViewHolder(View itemView) {
            super( itemView );
            pendingProfileImage = itemView.findViewById(R.id.pending_profile_image);
            pendingUserName = itemView.findViewById(R.id.pending_user_name);
            pendingAcceptButton = itemView.findViewById( R.id.pending_accept);
            pendingDeclineButton = itemView.findViewById( R.id.pending_decline);
        }
    }
}
