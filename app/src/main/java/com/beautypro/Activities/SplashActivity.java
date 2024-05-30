package com.beautypro.Activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.beautypro.R;

public class SplashActivity extends AppCompatActivity {

    FirebaseFirestore RTDB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        RTDB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initializeFirebase();

        if (USER == null || USER.isEmailVerified()) {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }
        else {
            startActivity(new Intent(SplashActivity.this, EmailVerificationActivity.class));
            finish();
        }
    }
}