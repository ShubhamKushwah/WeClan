package com.syberkeep.weclan;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsFragment extends Fragment {

    private Context thisContext;

    private RecyclerView mFriendList;
    private FirebaseAuth mAuth;
    private String mCurrentUid;
    private View mMainView;

    //Firebase
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendsDatabase;

    public FriendsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);

        mFriendList = (RecyclerView) mMainView.findViewById(R.id.friends_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrentUid = mAuth.getCurrentUser().getUid();

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("friends").child(mCurrentUid);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mFriendList.setHasFixedSize(true);
        mFriendList.setLayoutManager(new LinearLayoutManager(thisContext));

        thisContext = getContext();

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<FriendsModel, FriendsFragment.FriendsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<FriendsModel, FriendsViewHolder>(
                FriendsModel.class,
                R.layout.single_user_model,
                FriendsFragment.FriendsViewHolder.class,
                mFriendsDatabase
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder friendsViewHolder, final FriendsModel friendsModel, int position) {

                friendsViewHolder.setDate(friendsModel.getDate());
                final String uid = getRef(position).getKey();
                mUsersDatabase.child(uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String fullName = dataSnapshot.child("full_name").getValue().toString();
                        String thumbnail = dataSnapshot.child("thumbnail").getValue().toString();

                        if(dataSnapshot.hasChild("online")){
                            String online_state = dataSnapshot.child("online").getValue().toString();
                            friendsViewHolder.setOnline(online_state);
                        }

                        friendsViewHolder.setName(fullName);
                        friendsViewHolder.setImage(thumbnail, thisContext);

                        friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                CharSequence options[] = new CharSequence[]{"Send message", "View profile"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(thisContext);
                                builder.setTitle("Now what");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        if (i == 0) {

                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("user_id", uid);
                                            chatIntent.putExtra("user_name", fullName);
                                            startActivity(chatIntent);

                                        }
                                        else if (i == 1) {

                                            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                            profileIntent.putExtra("user_id", uid);
                                            startActivity(profileIntent);

                                        }
                                        Toast.makeText(getContext(), fullName, Toast.LENGTH_SHORT).show();

                                    }
                                });

                                builder.show();

                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        mFriendList.setAdapter(friendsRecyclerViewAdapter);

    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView= itemView;
        }

        public void setDate(String date){
            TextView textDate = (TextView) mView.findViewById(R.id.user_status_model);
            textDate.setText(date);
        }

        public void setName(String name){
            TextView textName = (TextView) mView.findViewById(R.id.user_full_name_model);
            textName.setText(name);
        }

        public void setImage(String thumb_image, Context context) {
            CircleImageView usersImageView = (CircleImageView) mView.findViewById(R.id.user_profile_avatar_model);
            Picasso.with(context).load(thumb_image).placeholder(R.mipmap.ic_launcher_round).into(usersImageView);
        }

        public void setOnline(String online_state){
            ImageView onlineView = (ImageView) mView.findViewById(R.id.user_online_image_model);

            if(online_state.equals("true"))
                onlineView.setVisibility(View.VISIBLE);
            else
                onlineView.setVisibility(View.INVISIBLE);
        }

    }

}