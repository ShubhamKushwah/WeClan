package com.syberkeep.weclan;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private Context thisContext = this;
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private CustomPagerAdapter mCustomAdapter;
    private TabLayout mTabLayout;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("We Clan");

        //Firebase
        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser() != null)
            mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        else
            Toast.makeText(thisContext, "Failed to access database!", Toast.LENGTH_SHORT).show();

        mViewPager = (ViewPager) findViewById(R.id.view_pager_main);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout_main);
        mCustomAdapter = new CustomPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mCustomAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        super.onOptionsItemSelected(item);

        switch (item.getItemId()){

            case R.id.menu_log_out:
                logOut();
                break;
            case R.id.menu_account_settings:
                Toast.makeText(thisContext, "Account / Plan options", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_profile:
                Intent accountIntent = new Intent(thisContext, AccountActivity.class);
                startActivity(accountIntent);
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    public void logOut(){
        mAuth.signOut();
        Intent startIntent = new Intent(thisContext, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){
            Intent startIntent = new Intent(thisContext, StartActivity.class);
            startActivity(startIntent);
            finish();
        }
        else {

            mUsersDatabase.child("online").setValue("true");

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        //when app is minimized or stopped.

        mUsersDatabase.child("online").setValue(ServerValue.TIMESTAMP);

    }
}