package com.necohorne.hometribe.Utilities.Services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.necohorne.hometribe.R;

/**
 * Created by necoh on 2018/03/07.
 */

public class MyFirebaseIdService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIdService";
    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        userRef.child(getString( R.string.dbnode_user))
                .child(user.getUid())
                .child("fcm_token")
                .setValue(refreshedToken);
    }
}
