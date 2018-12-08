package com.example.tmdm9.healthfitbasic;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);

        ((TextView)findViewById(R.id.users_name_text)).setText(String.format("%s %s",
                getString(R.string.users_name_profile_text),
                Objects.requireNonNull(googleSignInAccount).getDisplayName()));
    }
}
