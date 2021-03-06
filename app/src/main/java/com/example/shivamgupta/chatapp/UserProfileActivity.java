package com.example.shivamgupta.chatapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName , mProfileStatus , mProfileFriendsCount;
    private Button mSendFriendRequestBtn , mDeclineFriReqbtn;

    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mDatabaseRef;
    private DatabaseReference mFriendsDataBase;
    private DatabaseReference mNotificationDataBase;
    private DatabaseReference mRootRef;

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
        mNotificationDataBase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mRootRef = FirebaseDatabase.getInstance().getReference();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mProfileImage = findViewById(R.id.ivUserProfileImage);
        mProfileName = findViewById(R.id.tvUserDisplayName);
        mProfileStatus = findViewById(R.id.tvUserStatus);
        mProfileFriendsCount = findViewById(R.id.tvTotalFriends);
        mSendFriendRequestBtn = findViewById(R.id.btnSendFriRequest);
        mDeclineFriReqbtn = findViewById(R.id.btnDeclineFriReq);

        mDeclineFriReqbtn.setVisibility(View.INVISIBLE);
        mDeclineFriReqbtn.setEnabled(false);

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
                        if(dataSnapshot.hasChild(user_id)) {
                            String req_type = dataSnapshot.child(user_id).child("request_type")
                                    .getValue().toString();

                            if (req_type.equals("received")) {
                                mCurrentState = "req_received";
                                mSendFriendRequestBtn.setText("Accept Friend Request");

                                mDeclineFriReqbtn.setVisibility(View.VISIBLE);
                                mDeclineFriReqbtn.setEnabled(true);

                            } else if (req_type.equals("sent")) {
                                mCurrentState = "req_sent";
                                mSendFriendRequestBtn.setText("Cancel Friend Request");

                                mDeclineFriReqbtn.setVisibility(View.INVISIBLE);
                                mDeclineFriReqbtn.setEnabled(false);

                            }

                            progressDialog.dismiss();
                        }
                            else {
                                mFriendsDataBase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild(user_id)){
                                            mCurrentState = "friends";
                                            mSendFriendRequestBtn.setText("Un friend This Person");

                                            mDeclineFriReqbtn.setVisibility(View.INVISIBLE);
                                            mDeclineFriReqbtn.setEnabled(false);

                                        }

                                        progressDialog.dismiss();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        progressDialog.dismiss();
                                    }
                                });
                            }

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

                    DatabaseReference newNotificationref = mRootRef.child("notifications").child(user_id).push();
                    String newNotificationId = newNotificationref.getKey();

                    HashMap<String , String> notificationData = new HashMap<>();
                    notificationData.put("from" , mCurrentUser.getUid());
                    notificationData.put("type" , "request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + user_id + "/request_type" , "sent");
                    requestMap.put("Friend_req/" + user_id + "/" + mCurrentUser.getUid() + "/request_type" , "received");
                    requestMap.put("notifications/" + user_id + "/" + newNotificationId , notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){

                                Toast.makeText(UserProfileActivity.this, "there is some error", Toast.LENGTH_SHORT).show();
                            }
                            mSendFriendRequestBtn.setEnabled(true);

                            mCurrentState = "req_sent";
                            mSendFriendRequestBtn.setText("Cancel Friend Request");
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

                                    mDeclineFriReqbtn.setVisibility(View.INVISIBLE);
                                    mDeclineFriReqbtn.setEnabled(false);

                                }
                            });

                        }
                    });
                }

                //----------------Req Received-----------------

                if(mCurrentState.equals("req_received")){
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id + "/date" , currentDate);
                    friendsMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid() + "/date" , currentDate);

                    friendsMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + user_id , null);
                    friendsMap.put("Friend_req/" + user_id + "/" + mCurrentUser.getUid() , null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null){

                                mSendFriendRequestBtn.setEnabled(true);
                                mCurrentState = "friends";
                                mSendFriendRequestBtn.setText("Unfriend this Person");

                                mDeclineFriReqbtn.setVisibility(View.INVISIBLE);
                                mDeclineFriReqbtn.setEnabled(false);
                            }else{

                                String error = databaseError.getMessage();

                                Toast.makeText(UserProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                //-----------------UnFriend--------------

                if(mCurrentState.equals("friends")){
                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id , null);
                    unfriendMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid() , null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null){
                                mCurrentState = "not_friends";
                                mSendFriendRequestBtn.setText("Send Friend Request");

                                mDeclineFriReqbtn.setVisibility(View.INVISIBLE);
                                mDeclineFriReqbtn.setEnabled(false);
                            }else{
                                String error = databaseError.getMessage();

                                Toast.makeText(UserProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }

                            mSendFriendRequestBtn.setEnabled(true);
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mCurrentUser != null) {
            mDatabaseRef.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(mCurrentUser != null) {
            mDatabaseRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }
}
