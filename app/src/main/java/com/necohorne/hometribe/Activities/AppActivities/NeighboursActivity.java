package com.necohorne.hometribe.Activities.AppActivities;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.necohorne.hometribe.Utilities.RecyclerAdapters.NeighbourRecyclerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NeighboursActivity extends AppCompatActivity {

    private static final String TAG = "NeighboursActivity";
    private NeighbourRecyclerAdapter mNeighbourRecyclerAdapter;
    private Context mContext;
    private TextView emptyList;
    private ArrayList<UserProfile> mNeighbours;
    private RecyclerView mRecyclerView;
    private ArrayList<UserProfile> mUserProfiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_neighbours );
        mContext = getApplicationContext();
        mNeighbours = new ArrayList<>();
        initRecycler();
        neighbourChangeListener();
    }

    private void initRecycler() {
        emptyList = findViewById( R.id.empty_neighbours );
        mRecyclerView = (RecyclerView) findViewById( R.id.neighbour_recycler);
        LinearLayoutManager neighbourLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(neighbourLayoutManager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.neighbour_search_menu, menu );
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search_neighbours).getActionView();
        searchView.setSearchableInfo( searchManager.getSearchableInfo(new ComponentName( NeighboursActivity.this, SearchableActivity.class)));
        searchView.setQueryHint("Search for Neighbours");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.pending_neighbours:
                startActivity( new Intent( NeighboursActivity.this, PendingNeighbourActivity.class));
                break;
        }
        return super.onOptionsItemSelected( item );
    }

    public void getPendingRequests(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference requestReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(mContext.getString(R.string.dbnode_user))
                .child(user.getUid())
                .child(mContext.getString(R.string.dbnode_neighbours))
                .child("user_id");
        Query query = requestReference;
        query.addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUserProfiles = new ArrayList<>();
                mNeighbours = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        String snap = singleSnapshot.getValue().toString();
                        UserProfile userProfile = new UserProfile();
                        userProfile.setUser_id(snap);
                        mUserProfiles.add(userProfile);
                    }
                    getUsersSetup(mUserProfiles);
                } else {
                    mNeighbourRecyclerAdapter = new NeighbourRecyclerAdapter(mContext, mNeighbours);
                    mRecyclerView.setAdapter(mNeighbourRecyclerAdapter);
                    mNeighbourRecyclerAdapter.notifyDataSetChanged();
                    if (mNeighbours.size() == 0){
                        mRecyclerView.setVisibility(View.INVISIBLE);
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
                        mNeighbours.add(userProfile);
                    }
                    mNeighbourRecyclerAdapter = new NeighbourRecyclerAdapter(mContext, mNeighbours);
                    mRecyclerView.setAdapter(mNeighbourRecyclerAdapter);
                    mNeighbourRecyclerAdapter.notifyDataSetChanged();
                    if (mNeighbours.size() == 0){
                        mRecyclerView.setVisibility(View.INVISIBLE);
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

    private void neighbourChangeListener(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference requestReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(mContext.getString(R.string.dbnode_user))
                .child(user.getUid())
                .child(mContext.getString(R.string.dbnode_neighbours));
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
