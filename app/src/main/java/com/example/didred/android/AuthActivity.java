package com.example.didred.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import com.google.firebase.auth.FirebaseAuth;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startMainActivity();
        }
    }

    public void startMainActivity() {
        startActivity(new Intent(this, AboutActivity.class));
    }
}
