package com.syberkeep.weclan;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainFragment extends Fragment {

    private static Context thisContext = null;
    private View mView;
    private RecyclerView mRecyclerView;

    //Firebase
    private DatabaseReference mDatabaseRef;
    private DatabaseReference mFollowersDatabase;
    private FirebaseUser mCurrentUser;

    public MainFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_main, container, false);
        init();
        return mView;

    }

    private void init() {
        thisContext = getActivity();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mFollowersDatabase = FirebaseDatabase.getInstance().getReference().child("followers");

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mRecyclerView = (RecyclerView) mView.findViewById(R.id.recycler_view_main);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(thisContext));
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<UsersModel, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<UsersModel, UsersViewHolder>(
                UsersModel.class,
                R.layout.single_user_model,
                UsersViewHolder.class,
                mDatabaseRef
        ) {
            @Override
            protected void populateViewHolder(final UsersViewHolder usersViewHolder, UsersModel model, final int position) {
                usersViewHolder.setName(model.getFull_name());
                usersViewHolder.setStatus(model.getStatus());
                usersViewHolder.setImage(model.getThumbnail(), getActivity().getApplicationContext());

                final Button btnFollow = usersViewHolder.view.findViewById(R.id.user_follow_btn_model);

                final String user_id = getRef(position).getKey();


                // - - - FOLLOW USER - - -

                mFollowersDatabase.child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(mCurrentUser.getUid())) {

                            // - - setting the button color and text - -
                            btnFollow.setText("Unfollow");

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                btnFollow.setBackgroundTintList(thisContext.getResources().getColorStateList(R.color.grey_500));
                            } else {
                                btnFollow.setBackgroundColor(getResources().getColor(R.color.grey_500));
                            }

                            // - - button color and text done - -

                            //Following already
                            btnFollow.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    mFollowersDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            Toast.makeText(thisContext, "Unfollowed!", Toast.LENGTH_SHORT).show();

                                            btnFollow.setText("Follow");

                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                btnFollow.setBackgroundTintList(thisContext.getResources().getColorStateList(R.color.colorAccent));
                                            } else {
                                                btnFollow.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                            }

                                        }
                                    });

                                }
                            });

                        } else {

                            btnFollow.setText("Follow");

                            btnFollow.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    mFollowersDatabase.child(user_id).child(mCurrentUser.getUid()).setValue("true").addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            Toast.makeText(thisContext, "Now following... so clutch!", Toast.LENGTH_SHORT).show();

                                            btnFollow.setText("Unfollow");

                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                btnFollow.setBackgroundTintList(thisContext.getResources().getColorStateList(R.color.grey_500));
                                            } else {
                                                btnFollow.setBackgroundColor(getResources().getColor(R.color.grey_500));
                                            }

                                        }
                                    });

                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                usersViewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String userId = getRef(position).getKey();

                        Intent profileIntent = new Intent(thisContext, ProfileActivity.class);
                        profileIntent.putExtra("user_id", userId);
                        startActivity(profileIntent);

                    }
                });

            }
        };

        mRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View view;

        public UsersViewHolder(View itemView) {
            super(itemView);

            view = itemView;
        }

        public void setName(String name) {
            TextView mUserItemName = view.findViewById(R.id.user_full_name_model);
            mUserItemName.setText(name);
        }

        public void setStatus(String status) {
            TextView mUserItemStatus = view.findViewById(R.id.user_status_model);
            mUserItemStatus.setText(status);
        }

        public void setImage(String thumb_image, Context context) {
            CircleImageView usersImageView = (CircleImageView) view.findViewById(R.id.user_profile_avatar_model);
            Picasso.with(context).load(thumb_image).placeholder(R.mipmap.ic_launcher_round).into(usersImageView);
        }

    }

}