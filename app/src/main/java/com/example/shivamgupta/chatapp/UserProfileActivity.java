package com.example.shivamgupta.chatapp;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName , mProfileStatus , mProfileFriendsCount;
    private Button mSendFriendRequestBtn;

    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mDatabaseRef;
    private DatabaseReference mFriendsDataBase;

    private FirebaseUser mCurrentUser;

    private ProgressDialog progressDialog;

    private String mCurrentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendsDataBase = FirebaseDatabase.getInstance().getReference().child("Friends");

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mProfileImage = findViewById(R.id.ivUserProfileImage);
        mProfileName = findViewById(R.id.tvUserDisplayName);
        mProfileStatus = findViewById(R.id.tvUserStatus);
        mProfileFriendsCount = findViewById(R.id.tvTotalFriends);
        mSendFriendRequestBtn = findViewById(R.id.btnSendFriRequest);

        mCurrentState = "not_friends";

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading User Data");
        progressDialog.setMessage("Please wait while we load the data....");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String displayname = dataSnapshot.child("Name").getValue().toString();
                String Status = dataSnapshot.child("Status").getValue().toString();
                String image = dataSnapshot.child("Image").getValue().toString();

                mProfileName.setText(displayname);
                mProfileStatus.setText(Status);

                Picasso.with(UserProfileActivity.this).load(image).placeholder(R.drawable.dp_androidnew).
                        into(mProfileImage);

                //--------------Friends List / Request Feature-------------------

                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(user_id)){
                            String req_type  = dataSnapshot.child(user_id).child("request_type")
                                    .getValue().toString();

                            if(req_type.equals("received")){
                                mCurrentState = "req_received";
                                mSendFriendRequestBtn.setText("Accept Friend Request");
                            }else if(req_type.equals("sent")){
                                mCurrentState = "req_sent";
                                mSendFriendRequestBtn.setText("Cancel Friend Request");
                            }

                        }

                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSendFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mSendFriendRequestBtn.setEnabled(false);

                //------------Not Friends State----------------

                if(mCurrentState.equals("not_friends")){
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id)
                            .child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid())
                                        .child("request_type").setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        mSendFriendRequestBtn.setEnabled(true);
                                        mCurrentState = "req_sent";
                                        mSendFriendRequestBtn.setText("Cancel Friend Request");

                                        Toast.makeText(UserProfileActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }else{

                                Toast.makeText(UserProfileActivity.this, "Error123", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                //----------Cancel Friend Request------------------

                if(mCurrentState.equals("req_sent")){

                    mFriendRequestDatabase.child(mCurrentUser.getUid())
                            .child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendRequestDatabase.child(user_id)
                                    .child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mSendFriendRequestBtn.setEnabled(true);
                                    mCurrentState = "not_friends";
                                    mSendFriendRequestBtn.setText("Send Friend Request");
                                }
                            });

                        }
                    });
                }

                //----------------Req Received-----------------
                if(mCurrentState.equals("req_received")){
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    mFriendsDataBase.child(mCurrentUser.getUid()).child(user_id).setValue(currentDate)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendsDataBase.child(user_id).child(mCurrentUser.getUid()).setValue(currentDate)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue()
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            mSendFriendRequestBtn.setEnabled(true);
                                                                            mCurrentState = "friends";
                                                                            mSendFriendRequestBtn.setText("Un friend This Person");
                                                                        }
                                                                    });

                                                        }
                                                    });
                                        }
                                    });
                        }
                    });

                }

            }
        });


    }
}