package com.necohorne.hometribe.Activities.AppActivities;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.necohorne.hometribe.Constants.Constants;
import com.necohorne.hometribe.Models.ChatMessage;
import com.necohorne.hometribe.Models.Home;
import com.necohorne.hometribe.Models.UserProfile;
import com.necohorne.hometribe.R;
import com.necohorne.hometribe.Utilities.RecyclerAdapters.NeighbourRecyclerAdapter;
import com.necohorne.hometribe.Utilities.RecyclerAdapters.SearchRecyclerAdapter;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.google.maps.android.SphericalUtil.computeDistanceBetween;

public class SearchableActivity extends Activity {

    private static final String TAG = "SearchableActivity";
    private SearchRecyclerAdapter mSearchRecyclerAdapter;
    private Context mContext;
    private ArrayList<UserProfile> mUserProfiles;
    private RecyclerView mRecyclerView;
    private TextView emptyList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_searchable );
        Log.d(TAG, "onCreate: Activity started");
        mContext = this;
        initRecycler();
        getSearchIntent();
    }

    private void initRecycler(){
        emptyList = findViewById( R.id.empty_list);
        mRecyclerView = (RecyclerView) findViewById( R.id.search_recycler_view);
        LinearLayoutManager searchLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(searchLayoutManager);

    }

    private void getSearchIntent(){
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d( TAG, "search query : " + query );
            neighbourSearch(query);
        }
    }

    private void neighbourSearch(final String query) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query dbQuery = reference.child(getString(R.string.dbnode_user));
        dbQuery.addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUserProfiles = new ArrayList<>();
                if (dataSnapshot.exists()){
                    for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()) {
                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                        try {
                            String username = objectMap.get("user_name").toString();
                            String userEmail = objectMap.get("user_email").toString();
                            if (userEmail.equalsIgnoreCase( query )) {
                                UserProfile userProfile = new UserProfile();
                                userProfile.setHome_location(getLocation(objectMap.get("home_location").toString()));
                                userProfile.setUser_name( objectMap.get( "user_name" ).toString() );
                                userProfile.setProfile_image( objectMap.get( "profile_image" ).toString() );
                                userProfile.setUser_id( objectMap.get( "user_id" ).toString() );
                                mUserProfiles.add( userProfile );
                            }
                            if (username.equalsIgnoreCase( query )) {
                                UserProfile userProfile = new UserProfile();
                                userProfile.setUser_name( objectMap.get("user_name").toString());
                                userProfile.setHome_location(getLocation(objectMap.get("home_location").toString()));
                                userProfile.setProfile_image( objectMap.get("profile_image").toString() );
                                userProfile.setUser_id( objectMap.get("user_id").toString() );
                                mUserProfiles.add( userProfile );
                            }
                        } catch (NullPointerException e) {
                            Log.d( TAG, "Null Pointer Exception: " + e );
                        }
                    }
                }
                mSearchRecyclerAdapter = new SearchRecyclerAdapter(mContext, mUserProfiles);
                mRecyclerView.setAdapter(mSearchRecyclerAdapter);
                if (mUserProfiles.size() == 0){
                    mRecyclerView.setVisibility( View.INVISIBLE );
                    emptyList.setVisibility(View.VISIBLE);
                }else {
                    mRecyclerView.setVisibility( View.VISIBLE );
                    emptyList.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        } );

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
}
