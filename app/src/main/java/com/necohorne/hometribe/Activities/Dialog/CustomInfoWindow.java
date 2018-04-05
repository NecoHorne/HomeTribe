package com.necohorne.hometribe.Activities.Dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.necohorne.hometribe.R;

public class CustomInfoWindow implements GoogleMap.InfoWindowAdapter {

    private View view;
    private LayoutInflater layoutInflater;
    private Context context;

    public CustomInfoWindow(Context context) {
        CustomInfoWindow.this.context = context;
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        view = layoutInflater.inflate(R.layout.activity_custom_info_window, null);
    }
    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {

        TextView mTitle = (TextView) view.findViewById(R.id.winTitle);
        mTitle.setText(marker.getTitle());

        TextView distanceFromHome = (TextView) view.findViewById( R.id.distanceFromHomeId );
        distanceFromHome.setText(marker.getSnippet());

        TextView moreBtn = (TextView) view.findViewById(R.id.more_info );

        return view;
    }
}
