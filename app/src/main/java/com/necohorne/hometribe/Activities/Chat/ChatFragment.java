package com.necohorne.hometribe.Activities.Chat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.necohorne.hometribe.Models.ChatMessage;
import com.necohorne.hometribe.R;
import com.necohorne.hometribe.Utilities.RecyclerAdapters.ChatRecyclerAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static android.text.TextUtils.isEmpty;
import static com.google.maps.android.SphericalUtil.computeDistanceBetween;

public class ChatFragment extends BottomSheetDialogFragment{

    private static final String TAG = "ChatFragment";

    private ChatRecyclerAdapter mChatRecyclerAdapter;
    private Context mContext;
    private View mView;
    private EditText message;
    private ImageView sendButton;
    private ArrayList<ChatMessage> mMessagesList;
    private DatabaseReference mMessagesReference;
    private RecyclerView mRecyclerView;

    public ChatFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_chat, container, false);
        mContext = getActivity();
        initContent();
        enableMessageListener();

        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMessagesReference.removeEventListener(mValueEventListener);
    }

    private void initContent(){
        mRecyclerView = (RecyclerView) mView.findViewById( R.id.list_message);
        LinearLayoutManager messageLayoutManager = new LinearLayoutManager(mContext);
        messageLayoutManager.setStackFromEnd( true );
        messageLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(messageLayoutManager);
        mRecyclerView.setBackground(new ColorDrawable(Color.TRANSPARENT));
        getMessages();
        message = (EditText) mView.findViewById(R.id.input_message);
        sendButton = (ImageView) mView.findViewById(R.id.sendButton);
        sendButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        } );
    }

    private void sendMessage() {
        if (isEmpty(message.getText().toString())){
            Toast.makeText( mContext, "Message Blank", Toast.LENGTH_LONG ).show();
        }else {
            String messageText = message.getText().toString();
            Log.d(TAG, "Attempting to send message: " + messageText);

            final ChatMessage chat = new ChatMessage();
            chat.setMessage(messageText);
            chat.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
            chat.setTimestamp(getTimeStamp());
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference();
            Query query = userRef.child(getString(R.string.dbnode_user)).child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Map<String, Object> objectMap = (HashMap<String, Object>) dataSnapshot.getValue();
                        String username = (String) objectMap.get("user_name");
                        String sLocation = (objectMap.get("home_location").toString());
                        LatLng msgLoc = getLocation(sLocation);
                        chat.setName(username);
                        chat.setMessage_location(msgLoc);
                        uploadMessage(chat);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void getMessages(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbnode_messages));
        query.addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mMessagesList = new ArrayList<>();
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()) {
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setMessage_location(getLocation(objectMap.get(getString(R.string.field_message_location)).toString()));
                    double distance = checkDistance(chatMessage);
                    if (distance <= 5){
                        chatMessage.setMessage( objectMap.get( getString( R.string.field_message_message)).toString());
                        chatMessage.setKey_ref(objectMap.get(getString(R.string.field_message_key_ref)).toString());
                        chatMessage.setName(objectMap.get(getString(R.string.field_message_name)).toString());
                        chatMessage.setTimestamp(objectMap.get(getString(R.string.field_message_timestamp)).toString());
                        chatMessage.setUser_id( objectMap.get( getString( R.string.field_message_user_id)).toString());
                        mMessagesList.add(chatMessage);
                        Log.d( TAG, "Get Message: " + chatMessage.toString());
                    }
                }
                mChatRecyclerAdapter = new ChatRecyclerAdapter(mContext, mMessagesList);
                mRecyclerView.setAdapter(mChatRecyclerAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        } );

    }

    private String getTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE HH:mm MMM dd yyyy", Locale.getDefault());
        sdf.setTimeZone( TimeZone.getDefault());
        return sdf.format(new Date());
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

    private void uploadMessage(ChatMessage chat){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbnode_messages));
        String keyRef = reference.push().getKey();
        chat.setKey_ref(keyRef);
        reference.child(keyRef)
                .setValue(chat);
        message.setText("");
    }

    private double checkDistance(ChatMessage chat) {
        double distance = 0;
        if (chat.getMessage_location() != null & getHomeLatLng() != null){
            distance = computeDistanceBetween(chat.getMessage_location(), getHomeLatLng());
        }
        return distance / 1000;
    }

    private LatLng getHomeLatLng(){
        String location = getArguments().getString(getString(R.string.bundle_home));

        String str1 = location.substring(10);
        String str2 = str1.replaceAll( "[)]", "" );

        String[] latlng = str2.split( "," );
        double latitude = Double.parseDouble(latlng[0]);
        double longitude = Double.parseDouble(latlng[1]);
        return new LatLng(latitude, longitude);
    }

    ValueEventListener mValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            getMessages();
            mChatRecyclerAdapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void enableMessageListener(){
        mMessagesReference = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbnode_messages));
        mMessagesReference.addValueEventListener(mValueEventListener);
    }

}

