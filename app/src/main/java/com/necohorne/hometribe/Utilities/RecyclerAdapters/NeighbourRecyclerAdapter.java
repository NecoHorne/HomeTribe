package com.necohorne.hometribe.Utilities.RecyclerAdapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.necohorne.hometribe.R;

/**
 * Created by necoh on 2018/03/12.
 */

public class NeighbourRecyclerAdapter extends RecyclerView.Adapter<NeighbourRecyclerAdapter.ViewHolder>{

    private static final String TAG = "NeighbourRecyclerAdapte";
    private View mView;
    private Context mContext;

    public NeighbourRecyclerAdapter (Context context){
        mContext = context;
    }

    @Override
    public NeighbourRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.neighbour_list_item, parent,false);
        return new ViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(NeighbourRecyclerAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
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
