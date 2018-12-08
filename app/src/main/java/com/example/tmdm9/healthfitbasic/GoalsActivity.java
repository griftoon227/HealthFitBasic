package com.example.tmdm9.healthfitbasic;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Goal;
import com.google.android.gms.fitness.request.GoalsReadRequest;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GoalsActivity extends AppCompatActivity {
    ListView goalsListView;
    ArrayAdapter<String> arrayAdapter;

    int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);

        goalsListView = findViewById(R.id.goals_list_view);

        FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                        .build();

        Scope scopeLocation = new Scope(Scopes.FITNESS_LOCATION_READ);

        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this, // your activity
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    scopeLocation);
        }

        Task<List<Goal>> goalsTask = Fitness.getGoalsClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                .readCurrentGoals(new GoalsReadRequest.Builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .addDataType(DataType.TYPE_DISTANCE_DELTA)
                        .build());

        goalsTask.addOnSuccessListener(this::populateListView);
    }

    private void populateListView(List<Goal> goals){
        ArrayList<String> goalsStrings = new ArrayList<>();

        if (!(goals == null || goals.isEmpty())) {
            for(Goal goal : goals) {
                goalsStrings.add(goal.getActivityName());
            }
        }
        else {
            findViewById(R.id.goals_list_view).setVisibility(View.GONE);
            findViewById(R.id.no_goals_text).setVisibility(View.VISIBLE);
        }

        arrayAdapter = new ArrayAdapter<>(this, R.layout.goal_item, goalsStrings);
        goalsListView.setAdapter(arrayAdapter);
    }
}

