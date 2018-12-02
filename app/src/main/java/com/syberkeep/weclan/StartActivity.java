package com.syberkeep.weclan;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class StartActivity extends AppCompatActivity {

    private Context thisContext = this;
    private Button signInBtn;
    private Button signUpBtn;

    //Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        init();
    }

    private void init() {
        signInBtn = (Button) findViewById(R.id.btn_start_sign_in);
        signUpBtn = (Button) findViewById(R.id.btn_start_sign_up);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            Intent mainIntent = new Intent(thisContext, MainActivity.class);
            startActivity(mainIntent);
            finish();
            Toast.makeText(thisContext, "You are already logged in!", Toast.LENGTH_SHORT).show();
        }

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(thisContext, SignInActivity.class);
                startActivity(loginIntent);
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signUpIntent = new Intent(thisContext, SignUpActivity.class);
                startActivity(signUpIntent);
            }
        });
    }
}