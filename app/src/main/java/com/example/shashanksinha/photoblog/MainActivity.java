package com.example.shashanksinha.photoblog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private Toolbar mainToolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String currentUserUid;

    private FloatingActionButton addPostBtn;

    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;

    private BottomNavigationView bottomNavigationView;

    public static Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        activity = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();


        //Importing UI
        mainToolbar = findViewById(R.id.main_toolbar);
        addPostBtn = findViewById(R.id.add_post_btn);

        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Photo Blog");




        bottomNavigationView = findViewById(R.id.mainBottomNav);


        // Fragments...
        homeFragment = new HomeFragment();
        notificationFragment = new NotificationFragment();
        accountFragment = new AccountFragment();


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId())
                {
                    case R.id.bottom_action_home:
                        replaceFragment(homeFragment);
                        return  true;

                    /*case R.id.bottom_action_notification:
                        replaceFragment(notificationFragment);
                        return true;*/

                    case R.id.bottom_action_account:
                        //replaceFragment(accountFragment);
                        sendToSetup();
                        return true;

                    default:
                        return false;
                }


            }
        });




        addPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent addNewPostIntent = new Intent(MainActivity.this,NewPostActivity.class);
                startActivity(addNewPostIntent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);


            }
        });


    }


    //Toolbar Inflation...
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }


    //Toolbar onItemSelected...
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.action_logout_btn:
                logout();
                return true;

            case R.id.action_settings_btn:
                sendToSetup();
                return true;

             default:
                 return false;

        }
        
    }

    private void sendToSetup() {

        Intent setupIntent = new Intent(MainActivity.this,SetupActivity.class);
        startActivity(setupIntent);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);

    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(currentUser == null)
        {
            sendToLogin();
        } else {
            currentUserUid = mAuth.getCurrentUser().getUid();

            firebaseFirestore.collection("Users").document(currentUserUid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if(task.isSuccessful())
                    {
                        if(!task.getResult().exists())
                        {
                            sendToSetup();
                        } else {
                            replaceFragment(homeFragment);
                        }
                    } else {

                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(MainActivity.this,"Error : "+errorMessage,Toast.LENGTH_LONG).show();
                    }
                }
            });

        }
    }

    private void sendToLogin() {

        //sending to login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void logout() {

        mAuth.signOut();
        sendToLogin();
    }


    private void replaceFragment(Fragment fragment)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();
    }

}
