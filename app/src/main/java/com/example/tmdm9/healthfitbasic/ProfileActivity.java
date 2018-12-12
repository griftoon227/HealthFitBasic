package com.example.tmdm9.healthfitbasic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.example.tmdm9.healthfitbasic.Converter.*;

public class ProfileActivity extends AppCompatActivity {
    private static final int REQUEST_OAUTH_REQUEST_CODE = 2;

    Button saveHeightButton, saveWeightButton, viewProfileButton;
    EditText height, weight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        height = findViewById(R.id.height_etv);
        weight = findViewById(R.id.weight_etv);
        saveHeightButton = findViewById(R.id.save_height_button);
        saveWeightButton = findViewById(R.id.save_weight_button);
        viewProfileButton = findViewById(R.id.view_prof_btn);


        FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_HEIGHT, FitnessOptions.ACCESS_READ)
                        .addDataType(DataType.TYPE_HEIGHT, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_READ)
                        .addDataType(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_WRITE)
                        .build();

        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this, // your activity
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    new Scope(Scopes.FITNESS_BODY_READ_WRITE));
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        
        ((TextView)findViewById(R.id.users_name_text)).setText(String.format("%s %s",
                getString(R.string.users_name_profile_text),
                (Objects.requireNonNull(auth.getCurrentUser()).getDisplayName())));

        //Implement going to the previous screen from a right swipe of the activity
        findViewById(R.id.profile_activity_layout).setOnTouchListener(new OnSwipeTouchListener(this){
            public void onSwipeRight(){
                startActivity(new Intent(ProfileActivity.this, MainScreenActivity.class));
            }
        });

        //view profile
        viewProfileButton.setOnClickListener(view -> {
            height.setVisibility(View.VISIBLE);
            weight.setVisibility(View.VISIBLE);
            saveHeightButton.setVisibility(View.VISIBLE);
            saveWeightButton.setVisibility(View.VISIBLE);
            setHeightTextFromAccount();
            setWeightTextFromAccount();
        });

        //Set on click listener for save buttons
        saveHeightButton.setOnClickListener(view -> insertHeight());

        saveWeightButton.setOnClickListener(view -> insertWeight());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
                Log.d("SUCCESS", "Account authorization successful.");
            }
        }
    }

    private void setHeightTextFromAccount() {
        Calendar calendar = Calendar.getInstance();
        Fitness.getHistoryClient(ProfileActivity.this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(ProfileActivity.this)))
                .readData(new DataReadRequest.Builder()
                        .read(DataType.TYPE_HEIGHT)
                        .setTimeRange(1, calendar.getTimeInMillis(), TimeUnit.MILLISECONDS)
                        .setLimit(1)
                        .build())
                .addOnSuccessListener(dataReadResponse -> {
                    //Used for non-aggregated data
                    if (dataReadResponse.getDataSets().size() > 0) {
                        for (DataSet dataSet : dataReadResponse.getDataSets()) {
                            for (DataPoint dp : dataSet.getDataPoints()) {
                                for (Field field : dp.getDataType().getFields()) {
                                    height.setText(String.format(
                                            "%s",meterToFootConverter(dp.getValue(field).asFloat())));
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> System.out.println(e.getMessage()));
    }

    private void setWeightTextFromAccount() {
        Calendar calendar = Calendar.getInstance();
        Fitness.getHistoryClient(ProfileActivity.this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(ProfileActivity.this)))
                .readData(new DataReadRequest.Builder()
                        .read(DataType.TYPE_WEIGHT)
                        .setTimeRange(1, calendar.getTimeInMillis(), TimeUnit.MILLISECONDS)
                        .setLimit(1)
                        .build())
                .addOnSuccessListener(dataReadResponse -> {
                    //Used for non-aggregated data
                    if (dataReadResponse.getDataSets().size() > 0) {
                        for (DataSet dataSet : dataReadResponse.getDataSets()) {
                            for (DataPoint dp : dataSet.getDataPoints()) {
                                for (Field field : dp.getDataType().getFields()) {
                                    weight.setText(String.format(
                                            "%s",kilogramToPoundConverter(dp.getValue(field).asFloat())));
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> System.out.println(e.getMessage()));
    }

    private void insertHeight() {
        DataSet dataSet;

        //for height
        if (!height.getText().toString().isEmpty()) {
            dataSet = createDataForRequest(DataType.TYPE_HEIGHT,
                    Float.parseFloat(height.getText().toString()));
        }
        else {
            dataSet = null;
        }

        if (dataSet != null) {
            System.out.println(String.format("%s %s %s", dataSet, dataSet.getDataPoints().isEmpty(), dataSet.toString()));
            Fitness.getHistoryClient(ProfileActivity.this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(ProfileActivity.this)))
                    .insertData(dataSet)
                    .addOnSuccessListener(aVoid -> Toast.makeText(ProfileActivity.this,
                            "Height Updated.", Toast.LENGTH_SHORT).show());
        }

        else {
            Toast.makeText(ProfileActivity.this, "Height must be entered in feet (1-9)", Toast.LENGTH_SHORT).show();
        }
    }

    private void insertWeight(){
        DataSet dataSet;

        //for weight
        if (!weight.getText().toString().isEmpty()) {
            dataSet = createDataForRequest(DataType.TYPE_WEIGHT,
                    Float.parseFloat(weight.getText().toString()));
        }
        else {
            dataSet = null;
        }

        if (dataSet != null) {
            System.out.println(String.format("%s %s %s", dataSet, dataSet.getDataPoints().isEmpty(), dataSet.toString()));
            Fitness.getHistoryClient(ProfileActivity.this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(ProfileActivity.this)))
                    .insertData(dataSet)
                    .addOnSuccessListener(aVoid -> Toast.makeText(ProfileActivity.this,
                            "Weight Updated.", Toast.LENGTH_SHORT).show());
        }

        else {
            Toast.makeText(ProfileActivity.this, "Weight must be entered in feet (*-*)", Toast.LENGTH_SHORT).show();
        }
    }

    private DataSet createDataForRequest(DataType dataType, Float values) {
        Calendar calendar = Calendar.getInstance();
        DataSource dataSource = new DataSource.Builder()
                .setAppPackageName(getPackageName())
                .setDataType(dataType)
                .setType(DataSource.TYPE_RAW)
                .build();

        DataSet dataSet = DataSet.create(dataSource);
        DataPoint dataPoint = dataSet.createDataPoint().setTimeInterval((long) 1, calendar.getTimeInMillis(), TimeUnit.MILLISECONDS);

        if((dataPoint.getDataType().equals(DataType.TYPE_HEIGHT)) && (values >= 1 && values <= 9))
            dataPoint.getValue(Field.FIELD_HEIGHT).setFloat(footToMeterConverter(values));
        else if((dataPoint.getDataType().equals(DataType.TYPE_WEIGHT)) && (values >= 1 && values <= 100))
            dataPoint.getValue(Field.FIELD_WEIGHT).setFloat(poundToKilogramConverter(values));
        else
            return null;

        dataSet.add(dataPoint);

        return dataSet;
    }
}
