package com.example.shivamgupta.chatapp.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shivamgupta.chatapp.R;
import com.example.shivamgupta.chatapp.models.Friends;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView mFriendsList;

    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;

    private String mCurrentUserId;

    private View mMainView;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_friends , container , false);

        mFriendsList = mMainView.findViewById(R.id.friends_list);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUserId);
        mFriendsDatabase.keepSynced(true);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase.keepSynced(true);

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inflate the layout for this fragment
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                        Friends.class,
                        R.layout.users_single_layout,
                        FriendsViewHolder.class,
                        mFriendsDatabase

                ) {
                    @Override
                    protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, int position) {
                        viewHolder.setDate(model.getDate());

                        String user_list_id = getRef(position).getKey();
                        mUserDatabase.child(user_list_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String user_name = dataSnapshot.child("Name").getValue().toString();
                                String user_image = dataSnapshot.child("Thumb_image").getValue().toString();

                                if(dataSnapshot.hasChild("online")){
                                    Boolean user_online = (Boolean) dataSnapshot.child("online").getValue();
                                    viewHolder.setUserOnline(user_online);
                                }
                                
                                viewHolder.setName(user_name);
                                viewHolder.setImage(user_image , getContext());
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                };

        mFriendsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        public View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDate(String date){
            TextView mDateView = mView.findViewById(R.id.tvSingleStatus);
            mDateView.setText(date);
        }

        public void setName(String name){
            TextView mname = mView.findViewById(R.id.tvSingleUserName);
            mname.setText(name);
        }
        public void setImage(String image , Context ctx){
            CircleImageView imageView = mView.findViewById(R.id.ivSingleUserImage);
            Picasso.with(ctx).load(image).placeholder(R.drawable.dp_androidnew).into(imageView);
        }
        public void setUserOnline(Boolean userOnline){

            ImageView user_online_image = mView.findViewById(R.id.ivUserOnline);

            if(userOnline){
                user_online_image.setVisibility(View.VISIBLE);
            }else{
                user_online_image.setVisibility(View.INVISIBLE);
            }
        }
    }
}
