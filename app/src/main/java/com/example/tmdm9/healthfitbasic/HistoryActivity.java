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

import java.text.DateFormat;
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

    private static final String TAG = "HistoryAPI";
    private static final int REQUEST_OAUTH_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        //for ads
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                        .build();
        
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    HistoryActivity.this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);
        }

        tv = findViewById(R.id.show_text);
        lv = findViewById(R.id.show_list);

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
        // Setting a start and end date using a range of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        // The data request can specify multiple data types to return, effectively
        // combining multiple data queries into one call.
        // In this example, it's very unlikely that the request is for several hundred
        // datapoints each consisting of a few steps and a timestamp.  The more likely
        // scenario is wanting to see how many steps were walked per day, for 7 days.
        // Analogous to a "Group By" in SQL, defines how data should be aggregated.
        // bucketByTime allows for a time span, whereas bucketBySession would allow
        // bucketing by "sessions", which would need to be defined in code.

        return new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
    }

    private void readHistoryData() {
        //Begin by querying the data
        DataReadRequest readRequest = queryFitnessData();

        // Invoke the History API to fetch the data with the query
        Fitness.getHistoryClient(HistoryActivity.this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                .readData(readRequest)
                .addOnSuccessListener(dataReadResult -> {
                    // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
                    // as buckets containing DataSets, instead of just DataSets.
                    if (dataReadResult.getBuckets().size() > 0) {
                        String[] dates = new String[7];
                        ArrayList<String> steps = new ArrayList<>();

                        Calendar calendar = Calendar.getInstance();

                        for(int i = 0; i < dates.length; i++){
                            if(i == 0)
                                calendar.add(Calendar.DAY_OF_WEEK, -6);
                            else
                                calendar.add(Calendar.DAY_OF_WEEK, 1);
                            dates[i] = new SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.getTime());
                        }

                        for (Bucket bucket : dataReadResult.getBuckets()) {
                            for (DataSet dataSet : bucket.getDataSets()) {
                                steps.add(String.format("%s", createWeekList(dataSet)));
                            }
                        }

                        /*for(String step: steps)
                            System.out.println("Steps: " + step);
                        for(String date: dates)
                            System.out.println("Date: " + date);*/
                        tv.setVisibility(View.GONE);
                        lv.setAdapter(new HistoryListAdapter(HistoryActivity.this, R.layout.steps_item, steps, Arrays.asList(dates)));
                        lv.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(
                        e -> Log.e(TAG, "There was a problem reading the data.", e));
    }

    private int createWeekList(DataSet dataSet) {
        int steps = 0;

        for (DataPoint dp : dataSet.getDataPoints()) {
            for (Field field : dp.getDataType().getFields()) {
                System.out.println("Date: "+DateFormat.getDateInstance().format(dp.getStartTime(TimeUnit.MILLISECONDS)));
                steps += (dp.getValue(field).asInt());
            }
        }

        return steps;
    }

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
