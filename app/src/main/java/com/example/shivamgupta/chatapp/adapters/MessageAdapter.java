package com.example.shivamgupta.chatapp.adapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.shivamgupta.chatapp.R;
import com.example.shivamgupta.chatapp.models.Messages;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

/**
 * Created by Shivam Gupta on 23-04-2018.
 */

public class MessageAdapter extends RecyclerView.Adapter< MessageAdapter.MessageViewHolder> {


    private List<Messages> mMessageList;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public MessageAdapter(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_layout , parent , false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {

        String current_user_id = mAuth.getCurrentUser().getUid();

        Messages c = mMessageList.get(position);

        String from_user = c.getFrom();

        if(from_user.equals(current_user_id)){

            holder.messageText.setBackgroundColor(Color.WHITE);
            holder.messageText.setTextColor(Color.BLACK);
        }else {

            holder.messageText.setBackgroundResource(R.drawable.message_text_background);
            holder.messageText.setTextColor(Color.WHITE);
        }

        holder.messageText.setText(c.getMessage());
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public TextView timeText;


        public MessageViewHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.tvMessage);

        }
    }
}
