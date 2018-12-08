package com.example.tmdm9.healthfitbasic;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getDateInstance;
import static java.text.DateFormat.getTimeInstance;

public class HistoryActivity extends AppCompatActivity {
    TextView tv;

    private static final String TAG = "HistoryAPI";
    private static final int REQUEST_OAUTH_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                        .build();
        
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);
        }

        tv = findViewById(R.id.show_text);
        findViewById(R.id.btn_view_week).setOnClickListener(v -> readHistoryData());
        findViewById(R.id.btn_view_today).setOnClickListener(v -> Fitness.getHistoryClient(
                this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(this::printDataSet)
                .addOnFailureListener(e -> Log.e(TAG, "There was a problem reading the data.", e)));
    }

    private DataReadRequest queryFitnessData() {
        // Setting a start and end date using a range of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        java.text.DateFormat dateFormat = getDateInstance();
        Log.i(TAG, "Range Start: " + dateFormat.format(startTime));
        Log.i(TAG, "Range End: " + dateFormat.format(endTime));

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
        Fitness.getHistoryClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                .readData(readRequest)
                .addOnSuccessListener(dataReadResult -> {
                    // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
                    // as buckets containing DataSets, instead of just DataSets.
                    if (dataReadResult.getBuckets().size() > 0) {
                        for (Bucket bucket : dataReadResult.getBuckets()) {
                            List<DataSet> dataSets = bucket.getDataSets();
                            for (DataSet dataSet : dataSets) {
                                printDataSet(dataSet);
                            }
                        }
                    }
                    else if (dataReadResult.getDataSets().size() > 0) {
                        Log.i(TAG, "Number of returned DataSets is: " + dataReadResult.getDataSets().size());
                        for (DataSet dataSet : dataReadResult.getDataSets()) {
                            printDataSet(dataSet);
                        }
                    }
                })
                .addOnFailureListener(
                        e -> Log.e(TAG, "There was a problem reading the data.", e));
    }

    private void printDataSet(DataSet dataSet) {
        tv.setText(String.format("%s\n%s %s\n", tv.getText(), "Data returned for Data type:", dataSet.getDataType().getName()));
        DateFormat dateFormat = getTimeInstance();

        for (DataPoint dp : dataSet.getDataPoints()) {
            tv.setText(String.format("%s\n%s\n%s %s\n%s %s\n%s %s\n", tv.getText(), "Data point:", "Type:",
                    dp.getDataType().getName(), "Start:", dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)),
                    "End:", dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS))));
            for (Field field : dp.getDataType().getFields()) {
                tv.setText(String.format("%s\n%s %s %s %s\n", tv.getText(), "Field:", field.getName(), "Value:",
                        dp.getValue(field)));
            }
        }
    }
}
