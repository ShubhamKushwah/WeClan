package com.syberkeep.weclan;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextView mTitleName;
    private TextView mTitleLastSeenText;
    private CircleImageView mProfileChatUser;

    //Firebase
    private DatabaseReference mRootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        init();
    }

    private void init() {

        mRootRef = FirebaseDatabase.getInstance().getReference();

        String chatUserId = getIntent().getStringExtra("user_id");
        String chatUserName = getIntent().getStringExtra("user_name");

        mToolbar = (Toolbar) findViewById(R.id.toolbar_chat);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_action_bar, null);

        actionBar.setCustomView(action_bar_view);

        mTitleName = (TextView) findViewById(R.id.custom_bar_username);
        mTitleLastSeenText = (TextView) findViewById(R.id.custom_bar_last_seen);
        mProfileChatUser = (CircleImageView) findViewById(R.id.custom_bar_image);

        mTitleName.setText(chatUserName);

        mRootRef.child("Users").child(chatUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online_state = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("thumbnail").getValue().toString();

                if (online_state.equals("true")) {

                    mTitleLastSeenText.setText("online");

                } else {

                    mTitleLastSeenText.setText("maybe online");

                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(online_state);

                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());

                    Toast.makeText(ChatActivity.this, lastSeenTime, Toast.LENGTH_SHORT).show();

                    mTitleLastSeenText.setText(lastSeenTime);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}