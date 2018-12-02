package com.syberkeep.weclan;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class SignInActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Context thisContext = this;
    private Button loginBtn;
    private TextInputLayout emailInput;
    private TextInputLayout passwordInput;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        init();
    }

    private void init() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_sign_in);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Sign In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

        emailInput = (TextInputLayout) findViewById(R.id.input_layout_email_in);
        passwordInput = (TextInputLayout) findViewById(R.id.input_layout_password_in);
        loginBtn = (Button) findViewById(R.id.btn_login);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logIn(emailInput.getEditText().getText().toString(), passwordInput.getEditText().getText().toString());
            }
        });

    }

    public void logIn(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            mDatabaseRef.child(mAuth.getCurrentUser().getUid()).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    Toast.makeText(SignInActivity.this, "You are logged in!", Toast.LENGTH_SHORT).show();
                                    Intent mainIntent = new Intent(thisContext, MainActivity.class);
                                    startActivity(mainIntent);
                                    finish();

                                }
                            });
                        } else {
                            Toast.makeText(SignInActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
