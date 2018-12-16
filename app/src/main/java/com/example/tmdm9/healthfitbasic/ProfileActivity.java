//Author: Griffin Flaxman
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
    //request code for requesting google account permissions & a tag for logging
    private static final int REQUEST_OAUTH_REQUEST_CODE = 2;
    private static final String TAG = "Profile";

    //declare the views that will be modified in the activity
    Button updateFieldsButton;
    EditText heightETV, weight;
    TextView height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //associate the views with their XML ids
        heightETV = findViewById(R.id.height_etv);
        height = findViewById(R.id.height_text_label);
        weight = findViewById(R.id.weight_etv);
        updateFieldsButton = findViewById(R.id.update_fields_btn);

        //declare some fitness options for reading and writing height and weight data
        FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_HEIGHT, FitnessOptions.ACCESS_READ)
                        .addDataType(DataType.TYPE_HEIGHT, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_READ)
                        .addDataType(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_WRITE)
                        .build();

        //requesting permissions if they have not been allowed yet
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this, // your activity
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    new Scope(Scopes.FITNESS_BODY_READ_WRITE));
        }

        //get the FireBase authorization instance to grab the account's name data
        FirebaseAuth auth = FirebaseAuth.getInstance();

        //display the current logged in user's name data
        ((TextView)findViewById(R.id.users_name_text)).setText(String.format("%s %s",
                getString(R.string.users_name_profile_text),
                (Objects.requireNonNull(auth.getCurrentUser()).getDisplayName())));

        //Implement going to the previous screen from a right swipe of the activity
        findViewById(R.id.profile_activity_layout).setOnTouchListener(new OnSwipeTouchListener(this){
            public void onSwipeRight(){
                startActivity(new Intent(ProfileActivity.this, MainScreenActivity.class));
            }
        });

        //View Profile
        setProfile();

        //Set on click listener for save buttons
        updateFieldsButton.setOnClickListener(view -> {
            insertHeight();
            insertWeight();
        });
    }

    //set the profile if permissions for height and weight data are allowed
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
                Log.d("SUCCESS", "Account authorization successful.");
                setProfile();
            }
        }
    }

    //set the height text and the weight text if they exist, validation done w/in the functions
    private void setProfile(){
        setHeightTextFromAccount();
        setWeightTextFromAccount();
    }

    //read the height dataType from the google account and then set the height text to that value in feet (using
    //the converter)
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
                                    heightETV.setVisibility(View.GONE);
                                    height.setVisibility(View.VISIBLE);
                                    height.setText(String.format(
                                            "%s",meterToFootConverter(dp.getValue(field).asFloat())));
                                }
                            }
                        }
                    }
                })
                //if reading fails, log the error
                .addOnFailureListener(e -> Log.e(TAG, e.getMessage()));
    }

    //same as weight reading as it is for height
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
                //if fails, log the error
                .addOnFailureListener(e -> Log.e(TAG, e.getMessage()));
    }

    private void insertHeight() {
        DataSet dataSet;

        //for height, if the editText is not empty, get the dataSet
        if (!heightETV.getText().toString().isEmpty()) {
            dataSet = createDataForRequest(DataType.TYPE_HEIGHT,
                    Float.parseFloat(heightETV.getText().toString()));
        }
        //if the height text is visible, don't insert a new height, as it should not be set more than once
        else if(height.getVisibility() == View.VISIBLE)
            return;
        //if the height editText is empty, set the dataSet w/ a null value
        else {
            dataSet = null;
        }

        //as long as there is a dataSet with data, insert the data into the google account's history using the api
        if (dataSet != null) {
            Fitness.getHistoryClient(ProfileActivity.this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(ProfileActivity.this)))
                    .insertData(dataSet)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Height Updated"));
        }
        //otherwise tell the user the data was not entered properly
        else {
            Toast.makeText(ProfileActivity.this, "Height must be entered in feet (1-9)", Toast.LENGTH_SHORT).show();
        }
    }

    private void insertWeight(){
        DataSet dataSet;

        //for weight, if the editText is not empty, get the dataSet
        if (!weight.getText().toString().isEmpty()) {
            dataSet = createDataForRequest(DataType.TYPE_WEIGHT,
                    Float.parseFloat(weight.getText().toString()));
        }
        //if the weight editText is empty, set the dataSet w/ a null value
        else {
            dataSet = null;
        }

        //as long as there is a dataSet with data, insert the data into the google account's history using the api
        if (dataSet != null) {
            Fitness.getHistoryClient(ProfileActivity.this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(ProfileActivity.this)))
                    .insertData(dataSet)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Weight Updated"));
        }
        //otherwise tell the user the data was not entered properly
        else {
            Toast.makeText(ProfileActivity.this, "Weight must be entered in pounds (20-600)", Toast.LENGTH_SHORT).show();
        }
    }

    //create the dataSet using a dataSource and entering the height and weight data into a data point
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
        else if((dataPoint.getDataType().equals(DataType.TYPE_WEIGHT)) && (values >= 20 && values <= 600))
            dataPoint.getValue(Field.FIELD_WEIGHT).setFloat(poundToKilogramConverter(values));
        else
            return null;

        dataSet.add(dataPoint);

        return dataSet;
    }
}
