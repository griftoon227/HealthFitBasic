package com.example.tmdm9.healthfitbasic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;

public class MainScreenActivity extends AppCompatActivity{
    FirebaseAuth mAuth;
    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainscreen);

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, connectionResult -> Toast.makeText(getApplicationContext(), "Connection failed.", Toast.LENGTH_SHORT).show())
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        findViewById(R.id.recording_layout).setOnClickListener(view -> startActivity(new Intent(MainScreenActivity.this, RecordSteps.class)));
        findViewById(R.id.profile_layout).setOnClickListener(view -> startActivity(new Intent(MainScreenActivity.this, ProfileActivity.class)));
        findViewById(R.id.history_layout).setOnClickListener(view -> startActivity(new Intent(MainScreenActivity.this, HistoryActivity.class)));
        findViewById(R.id.goals_layout).setOnClickListener(view -> startActivity(new Intent(MainScreenActivity.this, GoalsActivity.class)));
        findViewById(R.id.logout_layout).setOnClickListener(view -> signOut());
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(status ->
                startActivity(new Intent(MainScreenActivity.this, SplashScreenActivity.class)));
    }
}
