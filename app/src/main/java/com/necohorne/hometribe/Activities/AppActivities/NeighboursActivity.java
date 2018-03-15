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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.necohorne.hometribe.Models.UserProfile;
import com.necohorne.hometribe.R;
import com.necohorne.hometribe.Utilities.RecyclerAdapters.NeighbourRecyclerAdapter;

import java.util.ArrayList;

public class NeighboursActivity extends AppCompatActivity {

    private static final String TAG = "NeighboursActivity";
    private NeighbourRecyclerAdapter mNeighbourRecyclerAdapter;
    private Context mContext;
    private TextView emptyList;
    private ArrayList<UserProfile> mNeighbours;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_neighbours );
        mContext = getApplicationContext();
        mNeighbours = new ArrayList<>();
        initUI();
    }

    private void initUI() {
        emptyList = findViewById( R.id.empty_neighbours );
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.neighbour_recycler);
        LinearLayoutManager neighbourLayoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(neighbourLayoutManager);
        mNeighbourRecyclerAdapter = new NeighbourRecyclerAdapter(mContext);
        recyclerView.setAdapter(mNeighbourRecyclerAdapter);

        if (mNeighbours.size() == 0){
            recyclerView.setVisibility(View.INVISIBLE);
            emptyList.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyList.setVisibility(View.INVISIBLE);
        }
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
}
