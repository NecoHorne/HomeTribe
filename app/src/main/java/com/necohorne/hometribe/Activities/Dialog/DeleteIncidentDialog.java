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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.necohorne.hometribe.R;

/**
 * Created by necoh on 2018/03/01.
 */

public class DeleteIncidentDialog extends DialogFragment {

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

        mView = inflater.inflate( R.layout.dialog_delete_incident, container, false);
        mContext = getActivity();

        TextView cancel = (TextView) mView.findViewById(R.id.cancel);
        cancel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        } );
        TextView delete = (TextView) mView.findViewById(R.id.confirm_delete);
        delete.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                reference.child(getString(R.string.dbnode_incidents))
                        .child(keyRef)
                        .removeValue();
                Toast.makeText( mContext, "Incident Successfully Deleted", Toast.LENGTH_SHORT);
                getDialog().dismiss();
            }
        } );

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable( Color.TRANSPARENT));

        return mView;
    }
}
