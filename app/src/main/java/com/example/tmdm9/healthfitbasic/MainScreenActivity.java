package com.example.tmdm9.healthfitbasic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;

public class MainScreenActivity extends AppCompatActivity{
    Button recordStepsBtn, profileBtn, historyBtn, goalsBtn, logoutBtn;
    TextView descriptionText;

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

        recordStepsBtn = findViewById(R.id.btn_record_data);
        profileBtn = findViewById(R.id.btn_profile);
        historyBtn = findViewById(R.id.btn_history);
        goalsBtn = findViewById(R.id.btn_goals);
        logoutBtn = findViewById(R.id.btn_logout);

        descriptionText = findViewById(R.id.nav_description_text);

        recordStepsBtn.setOnClickListener(view -> descriptionText.setText(getString(R.string.recording_steps_desc_text)));

        profileBtn.setOnClickListener(view -> descriptionText.setText(getString(R.string.profile_desc_text)));

        historyBtn.setOnClickListener(view -> descriptionText.setText(getString(R.string.history_desc_text)));

        goalsBtn.setOnClickListener(view -> descriptionText.setText(getString(R.string.goals_desc_text)));

        logoutBtn.setOnClickListener(view -> descriptionText.setText(getString(R.string.logout_desc_text)));

        recordStepsBtn.setOnLongClickListener(view -> {
            startActivity(new Intent(MainScreenActivity.this, RecordSteps.class));
            return false;
        });

        profileBtn.setOnLongClickListener(view -> {
            startActivity(new Intent(MainScreenActivity.this, ProfileActivity.class));
            return false;
        });

        historyBtn.setOnLongClickListener(view -> {
            startActivity(new Intent(MainScreenActivity.this, HistoryActivity.class));
            return false;
        });

        goalsBtn.setOnLongClickListener(view -> {
            startActivity(new Intent(MainScreenActivity.this, GoalsActivity.class));
            return false;
        });

        logoutBtn.setOnLongClickListener(view -> {
            signOut();
            return false;
        });


    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(status ->
                startActivity(new Intent(MainScreenActivity.this, SplashScreenActivity.class)));
    }
}
