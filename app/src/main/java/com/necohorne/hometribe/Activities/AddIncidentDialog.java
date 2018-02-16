package com.necohorne.hometribe.Activities;

import android.app.DialogFragment;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.necohorne.hometribe.R;

public class AddIncidentDialog extends DialogFragment{

    private Context mContext;
    private View mView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.activity_add_incident_dialog , container, false);

        return mView;
    }
}
