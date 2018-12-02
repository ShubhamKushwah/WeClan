package com.syberkeep.weclan;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountActivity extends AppCompatActivity {

    private Context thisContext = this;
    private Toolbar mToolbar;
    private CircleImageView mImageViewAvatar;
    private EditText nameEditText;
    private EditText statusEditText;
    private ProgressDialog mProgressDialog;
    private FloatingActionButton mChangeStatusBtn;
    private FloatingActionButton mChangeAvtBtn;
    private FloatingActionButton mChangeNameBtn;

    //Userinfo
    private String status = "";
    private String full_name = "";
    private String image = "";

    //Firebase
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        init();
    }

    private void init() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_accounts_activity);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);

        mDatabaseRef.keepSynced(true);

        mImageViewAvatar = (CircleImageView) findViewById(R.id.image_avatar_account);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading profile");
        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.setCanceledOnTouchOutside(false);

        nameEditText = (EditText) findViewById(R.id.edittext_fullname_account);
        statusEditText = (EditText) findViewById(R.id.status_edittext_account);

        mChangeStatusBtn = (FloatingActionButton) findViewById(R.id.btn_change_status);

        mChangeNameBtn = (FloatingActionButton) findViewById(R.id.btn_change_name);

        mChangeAvtBtn = (FloatingActionButton) findViewById(R.id.btn_change_avatar_account);
        mChangeAvtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent imageIntent = new Intent(thisContext, ImageActivity.class);
                startActivity(imageIntent);
            }
        });

        mProgressDialog.show();

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                full_name = dataSnapshot.child("full_name").getValue().toString();
                image = dataSnapshot.child("profile_avatar").getValue().toString();
                status = dataSnapshot.child("status").getValue().toString();

                if(full_name.equals(""))
                    full_name = "name";
                if(status.equals(""))
                    status = "Hi! there my status is here!";

                nameEditText.setText(full_name);
                statusEditText.setText(status);

                if(!image.equals("default")){
                    //Network policy used for Firebase Offline capabilities.
                    Picasso.with(thisContext).load(image).placeholder(R.mipmap.ic_launcher).networkPolicy(NetworkPolicy.OFFLINE).into(mImageViewAvatar, new Callback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(thisContext, "Successfully loaded profile offline!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError() {
                            Picasso.with(thisContext).load(image).placeholder(R.mipmap.ic_launcher).into(mImageViewAvatar);
                            Toast.makeText(thisContext, "Offline failed! Trying again!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                mProgressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mProgressDialog.dismiss();
                Toast.makeText(thisContext, "Cancelled!", Toast.LENGTH_SHORT).show();
            }
        });

        statusEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(final Editable editable) {
                if(editable.toString().equals(status)){
                    mChangeStatusBtn.setOnClickListener(null);
                    mChangeStatusBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey_300)));
                }
                else {
                    mChangeStatusBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                    mChangeStatusBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if(editable.toString().equals("")) {
                                statusEditText.setText("Hi! there my status is here!");
                                return;
                            }

                            final ProgressDialog progressDialog = new ProgressDialog(thisContext);
                            progressDialog.setTitle("Updating status");
                            progressDialog.setMessage("Please wait...");
                            progressDialog.setCancelable(false);
                            progressDialog.show();

                            mDatabaseRef.child("status").setValue(editable.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressDialog.dismiss();
                                    if(task.isSuccessful()){
                                        Toast.makeText(thisContext, "Status updated!", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        Toast.makeText(thisContext, "Please check your internet!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                    });
                }
            }
        });

        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(final Editable editable) {
                if(editable.toString().equals(full_name)){
                    mChangeNameBtn.setOnClickListener(null);
                    mChangeNameBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey_300)));
                }
                else {
                    mChangeNameBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                    mChangeNameBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if(editable.toString().equals("")) {
                                nameEditText.setText("your name");
                                return;
                            }

                            final ProgressDialog progressDialog = new ProgressDialog(thisContext);
                            progressDialog.setTitle("Updating status");
                            progressDialog.setMessage("Please wait...");
                            progressDialog.setCancelable(false);
                            progressDialog.show();

                            mDatabaseRef.child("full_name").setValue(editable.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressDialog.dismiss();
                                    if(task.isSuccessful()){
                                        Toast.makeText(thisContext, "Name updated!", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        Toast.makeText(thisContext, "Please check your internet!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                    });
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

}
