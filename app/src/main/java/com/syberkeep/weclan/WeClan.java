package com.syberkeep.weclan;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

public class WeClan extends Application {

    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);     //Enabling firebase offline capabilities.

        /*  In Manifest file write in the application tag android:name=".WeClan"   */

        /**
         * Now write <DatabaseReference>.keepSynced(true);
         * e.g. mDatabaseRef.keepSynced(true);
         *
         * e.g. in activity - AccountActivity - write it above the database value event listener.
         *
         * But this will only keep the string values synced so we need to keep track of Picasso images too.
         */

        //Offline capabilities of Picasso: to sync images directly without using the string value.

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

        //Now add the network policy method to the element like this:
        // Picasso.with(thisContext).load(image).networkPolicy(NetworkPolicy.OFFLINE);

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null) {
            mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

            mUsersDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot != null) {
                        mUsersDatabase.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);  //this line runs when user disconnects & saves the current time!
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }
}