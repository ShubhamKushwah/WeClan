package com.syberkeep.weclan;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private Context thisContext = this;
    private ImageView mProfileImage;
    private TextView mFullName;
    private TextView mFriendsCount;
    private TextView mFollowersCount;
    private TextView mStatusText;
    private Button mFriendReqBtn;
    private Button mFollowBtn;
    private Button mBlockBtn;
    private ProgressDialog mProgressDialog;
    private String mCurrentState;
    String userId = null;

    //Firebase
    private DatabaseReference mDatabaseRef;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mNotifDatabase;
    private DatabaseReference mRootRef;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth mFirebaseAuth;

    //Friends states;
    private String state_received = "received";
    private String state_sent = "sent";
    private String state_friends = "friends";
    private String state_not_friends = "not_friends";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        init();
    }

    private void init() {

        mProfileImage = (ImageView) findViewById(R.id.image_avatar_profile);
        mFullName = (TextView) findViewById(R.id.text_user_full_name_profile);
        mFriendsCount = (TextView) findViewById(R.id.friends_count_profile);
        mFollowersCount = (TextView) findViewById(R.id.followers_count_profile);
        mStatusText = (TextView) findViewById(R.id.text_user_status_profile);
        mFriendReqBtn = (Button) findViewById(R.id.btn_friend_request_profile);
        mFollowBtn = (Button) findViewById(R.id.btn_follow_profile);
        mBlockBtn = (Button) findViewById(R.id.btn_block_profile);

        mCurrentState = state_not_friends;

        userId = getIntent().getStringExtra("user_id");

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mDatabaseRef = mRootRef.child("Users").child(userId);
        mFriendReqDatabase = mRootRef.child("friend_requests");
        mFriendsDatabase = mRootRef.child("friends");
        mNotifDatabase = mRootRef.child("notifications");

        mFirebaseAuth = FirebaseAuth.getInstance();

        mCurrentUser = mFirebaseAuth.getCurrentUser();

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String full_name = dataSnapshot.child("full_name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("profile_avatar").getValue().toString();

                mFullName.setText(full_name);
                mStatusText.setText(status);

                mFriendsDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        mFriendsCount.setText(String.valueOf(dataSnapshot.child(userId).getChildrenCount()));

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                loadImage(image);

                // - - - - - - Friends state and requests.  - - - - - -

                mFriendReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        // - - - THERE IS A PENDING REQUEST - - -

                        if (dataSnapshot.hasChild(userId)) {

                            String reqType = dataSnapshot.child(userId).child("request_state").getValue().toString();

                            if (reqType.equals(state_received)) {

                                mCurrentState = state_received;
                                mFriendReqBtn.setText("Accept Request");

                                Toast.makeText(thisContext, "Also add decline option!", Toast.LENGTH_SHORT).show();

                            } else if (reqType.equals(state_sent)) {

                                mCurrentState = state_sent;
                                mFriendReqBtn.setText("Cancel Request");

                            }

                        } else {

                            // - - - FRIENDS ALREADY - - -

                            mFriendsDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(userId)) {

                                        mCurrentState = state_friends;
                                        mFriendReqBtn.setText("Unfriend");

                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

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

        mFriendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setButtonState(mFriendReqBtn, false);

                // - - - - - NOT FRIENDS CURRENTLY - - - - -

                if (mCurrentState.equals(state_not_friends)) {

                    DatabaseReference newNotifDataRef = mRootRef.child("notifications").child(userId).push();
                    String notif_id = newNotifDataRef.getKey();

                    HashMap<String, String> notifMap = new HashMap<>();
                    notifMap.put("from", mCurrentUser.getUid());
                    notifMap.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put("friend_requests/" + mCurrentUser.getUid() + "/" + userId + "/request_state", state_sent);
                    requestMap.put("friend_requests/" + userId + "/" + mCurrentUser.getUid() + "/request_state", state_received);
                    requestMap.put("notifications/" + userId + "/" + notif_id, notifMap);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){
                                Toast.makeText(thisContext, "Can't sent request some error occured!", Toast.LENGTH_SHORT).show();
                            }
                            mCurrentState = state_sent;
                            mFriendReqBtn.setText("Cancel Request");
                        }
                    });

                    setButtonState(mFriendReqBtn, true);

                }

                // - - - - - CANCEL FRIEND REQUEST - - - - -

                if (mCurrentState.equals(state_sent)) {

                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendReqDatabase.child(userId).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    Toast.makeText(thisContext, "Request Cancelled!", Toast.LENGTH_SHORT).show();

                                    setButtonState(mFriendReqBtn, true);
                                    mCurrentState = state_not_friends;
                                    mFriendReqBtn.setText("Send Friend Request");

                                    Toast.makeText(thisContext, "Handle Decline Button if required!", Toast.LENGTH_SHORT).show();

                                }
                            });

                        }
                    });

                }

                // - - - - - FRIEND REQUEST RECEIVED - - - - -

                if (mCurrentState.equals(state_received)) {

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("friends/" + mCurrentUser.getUid() + "/" + userId + "/date", currentDate);
                    friendsMap.put("friends/" + userId + "/" + mCurrentUser.getUid() + "/date", currentDate);

                    friendsMap.put("friend_requests/" + mCurrentUser.getUid() + "/" + userId, null);
                    friendsMap.put("friend_requests/" + userId + "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){
                                String error = databaseError.getMessage();
                                Toast.makeText(thisContext, "Error: " + error, Toast.LENGTH_SHORT).show();
                            }
                            else{
                                mCurrentState = state_friends;
                                mFriendReqBtn.setText("Unfriend");
                                Toast.makeText(thisContext, "You are now friends!", Toast.LENGTH_SHORT).show();
                            }
                            setButtonState(mFriendReqBtn, true);

                        }
                    });

                }

                // - - - - - FRIENDS ALREADY - - - - -

                if (mCurrentState.equals(state_friends)) {

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("friends/" + mCurrentUser.getUid() + "/" + userId, null);
                    unfriendMap.put("friends/" + userId + "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){
                                String error = databaseError.getMessage();
                                Toast.makeText(thisContext, "Error: " + error, Toast.LENGTH_SHORT).show();
                            }
                            else{
                                mCurrentState = state_not_friends;
                                mFriendReqBtn.setText("Send Friend Request");
                                Toast.makeText(thisContext, "You have unfriended that person!", Toast.LENGTH_SHORT).show();
                            }
                            setButtonState(mFriendReqBtn, true);

                        }
                    });

                }

            }
        });

    }

    public void loadImage(String image) {

        if (image.equals("default"))
            return;

        Picasso.with(thisContext).load(image).placeholder(R.mipmap.ic_launcher_round).into(mProfileImage);

    }

    public void setButtonState(Button btn, boolean enabled) {

        // + + + enter false t disable and true to enable + + +

        if (enabled) {

            btn.setEnabled(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btn.setBackgroundTintList(thisContext.getResources().getColorStateList(R.color.colorAccent));
            } else {
                btn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            }

        } else {

            btn.setEnabled(false);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btn.setBackgroundTintList(thisContext.getResources().getColorStateList(R.color.grey_500));
            } else {
                btn.setBackgroundColor(getResources().getColor(R.color.grey_500));
            }

        }

    }

}