package com.necohorne.hometribe.Activities.Dialog;

import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.necohorne.hometribe.Activities.AppActivities.MainActivity;
import com.necohorne.hometribe.Models.Report;
import com.necohorne.hometribe.R;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by necoh on 2018/04/25.
 */

public class ReportIncidentDialog extends DialogFragment {

    private static final String TAG = "DeleteIncidentDialog";

    private Context mContext;
    private View mView;
    private String keyRef;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        keyRef = getArguments().getString(getString(R.string.field_key_ref));
        if (keyRef != null){
            Log.d( TAG, "Key = " + keyRef );
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mView = inflater.inflate( R.layout.dialog_report_incident, container, false);
        mContext = getActivity();

        final EditText report_edit = (EditText) mView.findViewById(R.id.report_text);

        TextView cancel = (TextView) mView.findViewById(R.id.report_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        TextView report = (TextView) mView.findViewById(R.id.report_confirm);

        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!report_edit.getText().toString().equals("")){
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Calendar calendar = Calendar.getInstance();
                    Date today = calendar.getTime();
                    String date = today.toString();
                    Report newReport = new Report(keyRef, uid, report_edit.getText().toString(), date);
                    addReport(newReport);
                    getDialog().dismiss();
                    Toast.makeText(mContext, "Thank you! \nIncident Flagged", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(mContext, "Field cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable( Color.TRANSPARENT));
        return mView;
    }

    private void addReport(Report newReport) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            reference.child(getString(R.string.dbnode_report))
                    .push()
                    .setValue(newReport);
        }
    }
}
