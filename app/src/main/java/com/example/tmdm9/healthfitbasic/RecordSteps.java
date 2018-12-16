//Author: Griffin Flaxman
package com.example.tmdm9.healthfitbasic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;

import java.util.Objects;

public class RecordSteps extends AppCompatActivity {
    public static final String TAG = "RecordingSteps";

    private static final int REQUEST_OAUTH_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_steps);

        FitnessOptions fitnessOptions =
                FitnessOptions.builder().addDataType(DataType.TYPE_ACTIVITY_SAMPLES).build();

        // Check if the user has permissions to talk to Fitness APIs, otherwise authenticate the
        // user and request required permissions.
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);
        }

        Button recordingBtn = findViewById(R.id.recording_btn);
        recordingBtn.setOnClickListener(view -> {
            if(recordingBtn.getText().equals(getString(R.string.start_recording_steps_btn_text))){
                startRecording();
                recordingBtn.setText(getString(R.string.stop_recording_steps_btn_text));
            }
            else{
                stopRecording();
                recordingBtn.setText(getString(R.string.start_recording_steps_btn_text));
            }
        });

        //Implement going to the previous screen from a right swipe of the activity
        findViewById(R.id.recording_activity_layout).setOnTouchListener(new OnSwipeTouchListener(this){
            public void onSwipeRight(){
                startActivity(new Intent(RecordSteps.this, MainScreenActivity.class));
            }
        });
    }

    //if the permissions are accepted, log that
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
                Log.d("SUCCESS", "Account authorization successful.");
            }
        }
    }

    /* subscriptions can exist across application instances (so data is recorded even after the application closes
       down).  When creating a new subscription, it may already exist from a previous invocation of this app. If
       the subscription already exists, the method is a no-op.  However, you can check this with a special success
       code.
    */
    public void startRecording() {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        Fitness.getRecordingClient(RecordSteps.this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(RecordSteps.this)))
                .subscribe(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(aVoid -> Log.i(TAG, "Successfully subscribed!"))
                .addOnFailureListener(e -> Log.i(TAG, "There was a problem subscribing."));
        Toast.makeText(RecordSteps.this, "Recording has started.", Toast.LENGTH_SHORT).show();
    }

    // Stops recording by cancelling the subscription to step counting
    private void stopRecording() {
        final String dataTypeStr = DataType.TYPE_STEP_COUNT_DELTA.toString();
        Log.i(TAG, "Unsubscribing from data type: " + dataTypeStr);

        // Invoke the Recording API to unsubscribe from the data type and specify a callback that
        // will check the result.
        Fitness.getRecordingClient(RecordSteps.this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(RecordSteps.this)))
                .unsubscribe(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(aVoid -> Log.i(TAG, "Successfully unsubscribed for data type: " + dataTypeStr))
                .addOnFailureListener(e -> {
                    // Subscription not removed
                    Log.i(TAG, "Failed to unsubscribe for data type: " + dataTypeStr);
                });

        Toast.makeText(RecordSteps.this, "Recording has stopped.", Toast.LENGTH_SHORT).show();
    }
}