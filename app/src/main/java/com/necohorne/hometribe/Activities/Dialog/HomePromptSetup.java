package com.necohorne.hometribe.Activities.Dialog;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.necohorne.hometribe.Activities.AppActivities.HomeActivity;
import com.necohorne.hometribe.R;

/**
 * Created by necoh on 2018/02/22.
 */

public class HomePromptSetup extends DialogFragment {

    private View mView;
    private Context mContext;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate( R.layout.fragment_home_prompt , container, false);
        mContext = getActivity();
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable( Color.TRANSPARENT));
        getDialog().setCancelable(false);

        TextView setLocation = (TextView) mView.findViewById( R.id.home_prompt_set );
        setLocation.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent setHome = new Intent(mContext, HomeActivity.class);
                startActivity(setHome);
                getDialog().dismiss();
            }
        } );

        return mView;
    }
}
