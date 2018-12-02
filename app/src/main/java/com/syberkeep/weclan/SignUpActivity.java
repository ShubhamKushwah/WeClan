package com.syberkeep.weclan;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "ERROR";
    private Context thisContext = this;
    private Toolbar mToolbar;
    private Button createAccountBtn;
    private TextInputLayout fullNameField;
    private TextInputLayout emailField;
    private TextInputLayout passwordField, passwordFieldVerify;
    private ProgressDialog mProgressDialog;
    private String passwordErrorEditText = "";

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        init();
    }

    private void init() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_sign_up);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create new account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        mUserDatabase = FirebaseDatabase.getInstance().getReference();

        emailField = (TextInputLayout) findViewById(R.id.input_layout_email_up);
        passwordField = (TextInputLayout) findViewById(R.id.input_layout_password_up);
        passwordFieldVerify = (TextInputLayout) findViewById(R.id.input_layout_password_2_up);
        fullNameField = (TextInputLayout) findViewById(R.id.input_layout_full_name_up);

        mProgressDialog = new ProgressDialog(thisContext);

        createAccountBtn = (Button) findViewById(R.id.btn_create_up);
        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String full_name = fullNameField.getEditText().getText().toString();
                String email = emailField.getEditText().getText().toString();
                String password = passwordField.getEditText().getText().toString();
                String password2 = passwordFieldVerify.getEditText().getText().toString();

                if(TextUtils.isEmpty(full_name)) {
                    fullNameField.setError("Please enter your name here");
                    return;
                }
                else if(TextUtils.isEmpty(email)) {
                    emailField.setError("Please enter your email here");
                    return;
                }
                else if(TextUtils.isEmpty(password)) {
                    passwordField.setError("Please enter a password");
                    return;
                }
                else if(TextUtils.isEmpty(password2)){
                    passwordFieldVerify.setError("Please re-enter the above password");
                    return;
                }
                else if(!password.equals(password2)){
                    passwordFieldVerify.setError("Passwords do not match");
                    return;
                }
                else if(!passwordIsFine()){
                    passwordField.setError(passwordErrorEditText);
                    return;
                }

                mProgressDialog.setTitle("Please wait");
                mProgressDialog.setMessage("Creating a brand new account...");
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();

                createAccount(full_name, email, password);
            }
        });
    }
    
    private boolean passwordIsFine() {
        String password = passwordField.getEditText().getText().toString();
        if(passwordField.getEditText().length() < 8 ||passwordField.getEditText().length() > 12){
            passwordErrorEditText = "password should be 8-12 characters";
            return false;
        }
        else if(!password.matches("[a-zA-Z0-9._ ]*")) {
            passwordErrorEditText = "Special characters such as '!', '@' etc. are not allowed";
            return false;
        }
        else
            return true;
    }

    private void createAccount(final String full_name, final String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            String uid = mAuth.getCurrentUser().getUid();
                            String token = FirebaseInstanceId.getInstance().getToken();

                            mUserDatabase = mUserDatabase.child("Users").child(uid);

                            HashMap<String, String> mapUsers = new HashMap<>();
                            mapUsers.put("device_token", token);
                            mapUsers.put("full_name", full_name);
                            mapUsers.put("email", email);
                            mapUsers.put("status", "Hi! I am new here!");
                            mapUsers.put("avatar", "default");
                            mapUsers.put("thumbnail", "default");
                            mapUsers.put("profile_avatar", "default");

                            mUserDatabase.setValue(mapUsers).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){

                                        Toast.makeText(thisContext, "Account successfully created!", Toast.LENGTH_SHORT).show();

                                        Intent mainIntent = new Intent(thisContext, MainActivity.class);
                                        startActivity(mainIntent);
                                        finish();

                                    }
                                    else {
                                        mProgressDialog.dismiss();
                                        Log.w(TAG, "storingData:failure", task.getException());
                                        Toast.makeText(SignUpActivity.this, "Some error occured!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else {
                            mProgressDialog.dismiss();
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(thisContext, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}