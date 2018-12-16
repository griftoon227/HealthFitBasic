//Author: Griffin Flaxman
package com.example.tmdm9.healthfitbasic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class HistoryActivity extends AppCompatActivity {
    TextView tv;
    ListView lv;

    //request code for requesting google account permissions & a tag for logging
    private static final String TAG = "HistoryAPI";
    private static final int REQUEST_OAUTH_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        //requests and then loads banner ads
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //instantiate the fitness options needed for the activity
        FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                        .build();

        //check if  the account doesn't have permissions, and if so, add them
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    HistoryActivity.this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);
        }

        tv = findViewById(R.id.show_text);
        lv = findViewById(R.id.show_list);

        //instantiate listeners for the daily and week buttons to display the views
        findViewById(R.id.btn_view_week).setOnClickListener(v -> readHistoryData());
        findViewById(R.id.btn_view_today).setOnClickListener(v -> Fitness.getHistoryClient(
                HistoryActivity.this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(HistoryActivity.this)))
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(this::createDailyText)
                .addOnFailureListener(e -> Log.e(TAG, "There was a problem reading the data.", e)));

        //Implement going to the previous screen from a right swipe of the activity
        findViewById(R.id.history_activity_layout).setOnTouchListener(new OnSwipeTouchListener(this){
            public void onSwipeRight(){
                startActivity(new Intent(HistoryActivity.this, MainScreenActivity.class));
            }
        });
    }

    private DataReadRequest queryFitnessData() {
        //Setting a start and end date using a range of one week before this moment
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        //The data request can specify multiple data types to return, effectively combining multiple data queries
        //into one call. bucketByTime allows for grouping by a time span.
        return new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
    }

    private void readHistoryData() {
        //Begin by querying the data
        DataReadRequest readRequest = queryFitnessData();

        //Invoke the History API to fetch the data with the query
        Fitness.getHistoryClient(HistoryActivity.this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                .readData(readRequest)
                .addOnSuccessListener(dataReadResult -> {
                    //dataReadResult will be returned as buckets containing DataSets
                    if (dataReadResult.getBuckets().size() > 0) {
                        String[] dates = new String[7];
                        ArrayList<String> steps = new ArrayList<>();

                        Calendar calendar = Calendar.getInstance();

                        //displays the 7 days of the week starting from 7 days ago up to the current day
                        for(int i = 0; i < dates.length; i++){
                            if(i == 0)
                                calendar.add(Calendar.DAY_OF_WEEK, -6);
                            else
                                calendar.add(Calendar.DAY_OF_WEEK, 1);
                            dates[i] = new SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.getTime());
                        }

                        //read through the buckets
                        for (Bucket bucket : dataReadResult.getBuckets()) {
                            //read through the bucket's data sets
                            for (DataSet dataSet : bucket.getDataSets()) {
                                //for each dataSet, retrieve the steps values and add them to an array to be
                                //displayed
                                steps.add(String.format("%s", getStepsFromDataSet(dataSet)));
                            }
                        }

                        //get rid of the textView
                        tv.setVisibility(View.GONE);
                        //set the list view's adapter with the custom adapter I made with the steps and dates info
                        lv.setAdapter(new HistoryListAdapter(HistoryActivity.this, R.layout.steps_item, steps, Arrays.asList(dates)));
                        //show the list view with data
                        lv.setVisibility(View.VISIBLE);
                    }
                })
                //if reading the data fails, log it
                .addOnFailureListener(
                        e -> Log.e(TAG, "There was a problem reading the data.", e));
    }

    //each dataSet has a dataPoint, and every dataPoint has a field, and from that we retrieve the steps data for
    //the day
    private int getStepsFromDataSet(DataSet dataSet) {
        int steps = 0;

        for (DataPoint dp : dataSet.getDataPoints()) {
            for (Field field : dp.getDataType().getFields()) {
                steps += (dp.getValue(field).asInt());
            }
        }

        return steps;
    }

    //a similar situation here for creating the daily text, the only difference is there is no need to bucket data,
    //instead the dataSet is directly received and read from
    private void createDailyText(DataSet dataSet) {
        StringBuilder text = new StringBuilder();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_WEEK, 0);
        int steps = 0;

        text.append(new SimpleDateFormat("EEEE", Locale.US).format(calendar.getTime())).append("\n");

        if(!dataSet.isEmpty()) {
            for(DataPoint dp : dataSet.getDataPoints()){
                for(Field field : dp.getDataType().getFields()){
                    steps += dp.getValue(field).asInt();
                }
            }
        }

        text.append("Steps: ").append(steps);

        lv.setVisibility(View.GONE);
        tv.setText(text.toString());
        tv.setVisibility(View.VISIBLE);
    }
}
