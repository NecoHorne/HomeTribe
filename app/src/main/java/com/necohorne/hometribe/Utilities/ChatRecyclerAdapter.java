package com.necohorne.hometribe.Utilities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.necohorne.hometribe.Activities.AppActivities.MainActivity;
import com.necohorne.hometribe.Activities.AppActivities.OtherUserActivity;
import com.necohorne.hometribe.Activities.AppActivities.UserProfileActivity;
import com.necohorne.hometribe.Models.ChatMessage;
import com.necohorne.hometribe.Models.UserProfile;
import com.necohorne.hometribe.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by necoh on 2018/03/01.
 */

public class ChatRecyclerAdapter extends RecyclerView.Adapter<ChatRecyclerAdapter.ViewHolder> {

    private static final String TAG = "ChatRecyclerAdapter";
    private Context mContext;
    private ArrayList<ChatMessage> mChatMessages;

    public ChatRecyclerAdapter(Context context, ArrayList<ChatMessage> chatMessages) {
        mContext = context;
        mChatMessages = chatMessages;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_list_item, parent,false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ChatRecyclerAdapter.ViewHolder holder, int position) {
        final ChatMessage chat = mChatMessages.get(position);

        //make username more interactive with color changes and clicks to user profile.
        holder.name.setText(chat.getName());
        if (chat.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            holder.name.setTextColor(mContext.getResources().getColor(R.color.color_f));
            holder.name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startActivity(new Intent(mContext, UserProfileActivity.class));
                }
            });
        } else {
            holder.name.setTextColor(mContext.getResources().getColor(R.color.Black));
            holder.name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent otherUser = new Intent(mContext, OtherUserActivity.class);
                    otherUser.putExtra(mContext.getResources().getString(R.string.field_other_uid), chat.getUser_id());
                    mContext.startActivity(otherUser);
                }
            } );
        }
        holder.message.setText(chat.getMessage());
        Linkify.addLinks(holder.message, Linkify.ALL);
        holder.time_stamp.setText(chat.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return mChatMessages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView message;
        public TextView name;
        public TextView time_stamp;
        public RelativeLayout mRelativeLayout;


        public ViewHolder(View itemView) {
            super( itemView );
            mRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.chat_rel_layout);
            message = (TextView) itemView.findViewById(R.id.message);
            name = (TextView) itemView.findViewById(R.id.name);
            time_stamp = (TextView) itemView.findViewById(R.id.time_stamp);
        }
    }
}
