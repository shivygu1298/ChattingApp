package com.example.shivamgupta.chatapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar mToolBar;

    private DatabaseReference mRootRef;

    private String mUserId;

    private ImageView mSend;
    private TextView mChatArea;
    private ImageView mAdd;

    private TextView mDisplayName;
    private TextView mLastSeen;
    private CircleImageView mrofilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mToolBar = findViewById(R.id.app_bar_chat);
        setSupportActionBar(mToolBar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUserId = getIntent().getStringExtra("user_id");

        String userName = getIntent().getStringExtra("user_name");

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar , null);

        actionBar.setCustomView(action_bar_view);

        mSend = findViewById(R.id.ivSend);
        mAdd = findViewById(R.id.ivAdd);
        mChatArea = findViewById(R.id.tvChatArea);


        mDisplayName = findViewById(R.id.tvChatUserName);
        mLastSeen = findViewById(R.id.tvLastSeen);
        mrofilePic = findViewById(R.id.ivUserDp);


        mDisplayName.setText(userName);

        mRootRef.child("Users").child(mUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("Image").getValue().toString();

                if(online.equals("true")){
                    mLastSeen.setText("online");

                }else{
                    mLastSeen.setText(online);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }
}
