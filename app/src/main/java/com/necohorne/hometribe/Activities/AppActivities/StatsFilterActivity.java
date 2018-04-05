package com.necohorne.hometribe.Activities.AppActivities;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.necohorne.hometribe.Constants.Constants;
import com.necohorne.hometribe.R;
import com.necohorne.hometribe.Utilities.MyDialogCloseListener;

public class StatsFilterActivity extends DialogFragment {

    private static final String TAG = "StatsFilterActivity";

    private Context mContext;
    private View mView;
    private RadioGroup distanceGroup;
    private RadioButton distanceButton;
    private RadioGroup timeGroup;
    private RadioButton timeButton;
    private SharedPreferences mStatsTime;
    private SharedPreferences mStatsDistance;
    private SharedPreferences.Editor mTimeEditor;
    private SharedPreferences.Editor mDistanceEditor;

    private String mTimePrefs;
    private SharedPreferences time;
    private SharedPreferences distance;
    private String mDistancePrefs;

    private RadioButton filter5k;
    private RadioButton filter10k;
    private RadioButton filterTown;

    private RadioButton filterWeek;
    private RadioButton filterMonth;
    private RadioButton filterAll;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.activity_stats_filter , container, false);
        mContext = getActivity();
        getPrefs();
        distance();
        time();
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable( Color.TRANSPARENT));
        return mView;
    }

    private void getPrefs(){

        time = mContext.getSharedPreferences(Constants.PREFS_TIME, 0 );
        mTimePrefs = time.getString(Constants.TIME, "all");
        filterWeek = mView.findViewById(R.id.filter_last_week);
        filterMonth = mView.findViewById(R.id.filter_last_month);
        filterAll = mView.findViewById(R.id.filter_all_time);
        switch (mTimePrefs){
            case Constants.ONE_WEEK:
                filterWeek.toggle();
                break;
            case Constants.ONE_MONTH:
                filterMonth.toggle();
                break;
            case Constants.ALL_TIME:
                filterAll.toggle();
                break;
        }

        distance = mContext.getSharedPreferences(Constants.PREFS_DISTANCE, 0 );
        mDistancePrefs = distance.getString(Constants.DISTANCE, "my_town");
        filter5k = mView.findViewById(R.id.filter_5km);
        filter10k = mView.findViewById(R.id.filter_10k);
        filterTown = mView.findViewById(R.id.filter_my_town);
        switch (mDistancePrefs){
            case Constants.FIVE_KILOMETERS:
                filter5k.toggle();
                break;
            case Constants.TEN_KILOMETERS:
                filter10k.toggle();
                break;
            case Constants.MY_TOWN:
                filterTown.toggle();
                break;
        }
    }

    private void time() {
        timeGroup = (RadioGroup) mView.findViewById(R.id.filter_radio_group_time);
        timeGroup.setOnCheckedChangeListener( new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                timeButton = (RadioButton) mView.findViewById(checkedId);
                switch (timeButton.getId()) {
                    case R.id.filter_last_week:
                        mStatsTime = mContext.getSharedPreferences( Constants.PREFS_TIME, 0 );
                        mTimeEditor = mStatsTime.edit();
                        mTimeEditor.putString(Constants.TIME, Constants.ONE_WEEK);
                        mTimeEditor.commit();
                        break;
                    case R.id.filter_last_month:
                        mStatsTime = mContext.getSharedPreferences( Constants.PREFS_TIME, 0 );
                        mTimeEditor = mStatsTime.edit();
                        mTimeEditor.putString(Constants.TIME, Constants.ONE_MONTH);
                        mTimeEditor.commit();
                        break;
                    case R.id.filter_all_time:
                        mStatsTime = mContext.getSharedPreferences(Constants.PREFS_TIME, 0 );
                        mTimeEditor = mStatsTime.edit();
                        mTimeEditor.putString(Constants.TIME, Constants.ALL_TIME);
                        mTimeEditor.commit();
                        break;
                }
            }
        } );
    }

    private void distance() {
        distanceGroup = (RadioGroup) mView.findViewById(R.id.filter_radio_group_distance);
        distanceGroup.setOnCheckedChangeListener( new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                distanceButton = (RadioButton) mView.findViewById(checkedId);
                switch (distanceButton.getId()){
                    case R.id.filter_5km:
                        mStatsDistance = mContext.getSharedPreferences( Constants.PREFS_DISTANCE, 0 );
                        mDistanceEditor = mStatsDistance.edit();
                        mDistanceEditor.putString(Constants.DISTANCE, Constants.FIVE_KILOMETERS);
                        mDistanceEditor.commit();
                    break;
                    case R.id.filter_10k:
                        mStatsDistance = mContext.getSharedPreferences( Constants.PREFS_DISTANCE, 0 );
                        mDistanceEditor = mStatsDistance.edit();
                        mDistanceEditor.putString(Constants.DISTANCE, Constants.TEN_KILOMETERS);
                        mDistanceEditor.commit();
                        break;
                    case R.id.filter_my_town:
                        mStatsDistance = mContext.getSharedPreferences( Constants.PREFS_DISTANCE, 0 );
                        mDistanceEditor = mStatsDistance.edit();
                        mDistanceEditor.putString(Constants.DISTANCE, Constants.MY_TOWN);
                        mDistanceEditor.commit();
                        break;
                }
            }
        } );
    }

    public void onDismiss(DialogInterface dialog) {
        Activity activity = getActivity();
        if(activity instanceof MyDialogCloseListener)
            ((MyDialogCloseListener)activity).handleDialogClose(dialog);
    }
}
