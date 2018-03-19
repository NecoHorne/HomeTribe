package com.necohorne.hometribe.Activities.AppActivities;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.necohorne.hometribe.Models.UserProfile;
import com.necohorne.hometribe.R;
import com.necohorne.hometribe.Utilities.RecyclerAdapters.PendingNeighRecyclerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PendingNeighbourActivity extends AppCompatActivity {

    private Context mContext;
    private ArrayList<UserProfile> mUserProfiles;
    private ArrayList<UserProfile> mNeighbourProfiles;
    private RecyclerView mRecyclerView;
    private PendingNeighRecyclerAdapter mPendingNeighRecyclerAdapter;
    private TextView emptyList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState );
        setContentView(R.layout.activity_pending_neighbour );
        mContext = getApplicationContext();
        initRecycler();
        setUpChangeListener();
    }

    public void initRecycler(){
        mRecyclerView = (RecyclerView) findViewById( R.id.pending_neigh_recycler);
        LinearLayoutManager pendingLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(pendingLayoutManager);
        emptyList = findViewById(R.id.pending_empty_list);
    }

    public void getPendingRequests(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference requestReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(mContext.getString(R.string.dbnode_user))
                .child(user.getUid())
                .child(mContext.getString(R.string.dbnode_neighbour_requests))
                .child("user_id");
        Query query = requestReference;
        query.addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUserProfiles = new ArrayList<>();
                mNeighbourProfiles = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        String snap = singleSnapshot.getValue().toString();
                        UserProfile userProfile = new UserProfile();
                        userProfile.setUser_id(snap);
                        mUserProfiles.add(userProfile);
                    }
                    getUsersSetup(mUserProfiles);
                } else {
                    mPendingNeighRecyclerAdapter = new PendingNeighRecyclerAdapter(mContext, mNeighbourProfiles);
                    mRecyclerView.setAdapter(mPendingNeighRecyclerAdapter);
                    mPendingNeighRecyclerAdapter.notifyDataSetChanged();
                    if (mUserProfiles.size() == 0){
                        mRecyclerView.setVisibility( View.INVISIBLE);
                        emptyList.setVisibility(View.VISIBLE);
                    } else {
                        mRecyclerView.setVisibility(View.VISIBLE);
                        emptyList.setVisibility(View.INVISIBLE);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        } );
    }

    private void getUsersSetup(ArrayList<UserProfile> userProfiles) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbnode_user));

        for (int i = 0; i < userProfiles.size(); i++ ){
            UserProfile userProfile = userProfiles.get(i);
            Query query = reference.child(userProfile.getUser_id());
            query.addListenerForSingleValueEvent( new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()){
                        Map<String, Object> objectMap = (HashMap<String, Object>) dataSnapshot.getValue();
                            UserProfile userProfile = new UserProfile();
                            userProfile.setUser_name( objectMap.get( "user_name" ).toString() );
                            userProfile.setProfile_image( objectMap.get( "profile_image" ).toString() );
                            userProfile.setUser_id( objectMap.get( "user_id" ).toString() );
                            mNeighbourProfiles.add(userProfile);
                    }
                    mPendingNeighRecyclerAdapter = new PendingNeighRecyclerAdapter(mContext, mNeighbourProfiles);
                    mRecyclerView.setAdapter(mPendingNeighRecyclerAdapter);
                    mPendingNeighRecyclerAdapter.notifyDataSetChanged();
                    if (mUserProfiles.size() == 0){
                        mRecyclerView.setVisibility( View.INVISIBLE);
                        emptyList.setVisibility(View.VISIBLE);
                    } else {
                        mRecyclerView.setVisibility(View.VISIBLE);
                        emptyList.setVisibility(View.INVISIBLE);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            } );
        }
    }

    private void setUpChangeListener(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference requestReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(mContext.getString(R.string.dbnode_user))
                .child(user.getUid())
                .child(mContext.getString(R.string.dbnode_neighbour_requests));
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getPendingRequests();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        requestReference.addValueEventListener(postListener);
    }

}
