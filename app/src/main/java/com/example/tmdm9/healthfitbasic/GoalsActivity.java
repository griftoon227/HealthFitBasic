//Author: Griffin Flaxman
package com.example.tmdm9.healthfitbasic;

import android.app.Activity;
import android.content.Intent;
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
    //request code for requesting google account permissions
    private static final int REQUEST_OAUTH_REQUEST_CODE = 2;

    private ListView goalsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);

        //instantiate the goals listView
        goalsListView = findViewById(R.id.goals_list_view);

        //instantiate the fitness options needed for the activity
        FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                        .build();

        //need location scope to read distance goals
        Scope scopeLocation = new Scope(Scopes.FITNESS_LOCATION_READ);

        //check if  the account doesn't have permissions, and if so, add them
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this, // your activity
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    scopeLocation);
        }

        //go to read goals function if the google acct has permissions
        readGoals();

        //Implement going to the previous screen from a right swipe of the activity
        findViewById(R.id.goals_activity_layout).setOnTouchListener(new OnSwipeTouchListener(this){
            @Override
            public void onSwipeRight(){
                startActivity(new Intent(GoalsActivity.this, MainScreenActivity.class));
            }
        });
    }

    //check the result of the permissions, and if they are accepted, go to the read goals function
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
                readGoals();
            }
        }
    }

    private void readGoals(){
        //asynchronously retrieve the goals using the Fitness goals client for the steps and distance data types
        Task<List<Goal>> goalsTask = Fitness.getGoalsClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                .readCurrentGoals(new GoalsReadRequest.Builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .addDataType(DataType.TYPE_DISTANCE_DELTA)
                        .build());

        //when the task finishes, populate the listView
        goalsTask.addOnSuccessListener(this::populateListView);
    }

    private void populateListView(List<Goal> goals){
        ArrayList<String> goalsStrings = new ArrayList<>();

        //if there are goals, add them to the array, and display the list. otherwise, display the no goals textView
        if (!(goals == null || goals.isEmpty())) {
            for(Goal goal : goals) {
                goalsStrings.add(goal.getActivityName());
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.goal_item, goalsStrings);
            goalsListView.setAdapter(arrayAdapter);
        }
        else {
            findViewById(R.id.goals_list_view).setVisibility(View.GONE);
            findViewById(R.id.no_goals_text).setVisibility(View.VISIBLE);
        }
    }
}

