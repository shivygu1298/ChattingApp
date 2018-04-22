package com.example.shivamgupta.chatapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shivamgupta.chatapp.adapters.MessageAdapter;
import com.example.shivamgupta.chatapp.models.GetTimeAgo;
import com.example.shivamgupta.chatapp.models.Messages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar mToolBar;

    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private String mcurrentUserId;

    private String mChatUser;

    private ImageView mSend;
    private EditText mChatArea;
    private ImageView mAdd;

    private TextView mDisplayName;
    private TextView mLastSeen;
    private CircleImageView mrofilePic;

    private RecyclerView mChatList;
    private SwipeRefreshLayout mRefreshLayout;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;


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
        mChatUser = getIntent().getStringExtra("user_id");

        mAuth = FirebaseAuth.getInstance();
        mcurrentUserId = mAuth.getCurrentUser().getUid();

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

        mAdapter = new MessageAdapter(messagesList);

        mChatList = findViewById(R.id.rvmessageList);
        mRefreshLayout = findViewById(R.id.refresh_layout);
        mLinearLayout = new LinearLayoutManager(this);

        mChatList.setHasFixedSize(true);
        mChatList.setLayoutManager(mLinearLayout);

        mChatList.setAdapter(mAdapter);

        loadMessages();


        mDisplayName.setText(userName);

        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("Thumb_image").getValue().toString();

                if(online.equals("true")){
                    mLastSeen.setText("online");

                }else{
                    GetTimeAgo getTimeAgo = new GetTimeAgo();

                    long time = Long.parseLong(online);
                    String timeNow = getTimeAgo.getTimeAgo(time , getApplicationContext());

                    mLastSeen.setText(timeNow);
                }
                Picasso.with(getApplicationContext()).load(image).placeholder(R.drawable.dp_androidnew).into(mrofilePic);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });

        mRootRef.child("Chat").child(mcurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(mChatUser)){

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen" , false);
                    chatAddMap.put("timestamp" , ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mcurrentUserId + "/" + mChatUser , chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mcurrentUserId , chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){
                                Log.d("ERROR", "onComplete: done" + databaseError.getMessage());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = mChatArea.getText().toString();

                if(!TextUtils.isEmpty(message)){

                    String current_user_ref = "messages/" + mcurrentUserId + "/" + mChatUser;
                    String chat_user_ref = "messages/" + mChatUser + "/" + mcurrentUserId;

                    DatabaseReference user_mesage_push = mRootRef.child("messages").
                            child(mcurrentUserId).child(mChatUser).push();

                    String push_id = user_mesage_push.getKey();

                    Map message_map = new HashMap();
                    message_map.put("message" , message);
                    message_map.put("seen" , false);
                    message_map.put("type" , "text");
                    message_map.put("time" , ServerValue.TIMESTAMP);
                    message_map.put("from" , mcurrentUserId);

                    Map message_user_map = new HashMap();
                    message_user_map.put(current_user_ref + "/" + push_id, message_map);
                    message_user_map.put(chat_user_ref + "/" + push_id , message_map);

                    mChatArea.setText("");

                    mRootRef.updateChildren(message_user_map, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError != null){
                                Log.d("Chat", "onComplete: Error");
                            }
                        }
                    });

                }
            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;

                messagesList.clear();
                loadMessages();
            }
        });
    }

    private void loadMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mcurrentUserId).child(mChatUser);

        Query messageQuery = messageRef.limitToLast(mCurrentPage*TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

                mChatList.scrollToPosition(messagesList.size() -1);
                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            mRootRef.child("Users").child(mcurrentUserId).child("online").setValue("true");

        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            mRootRef.child("Users").child(mcurrentUserId).child("online").setValue(ServerValue.TIMESTAMP);

        }
    }
}
