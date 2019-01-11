package com.instamaven.app.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.instamaven.app.R;
import com.instamaven.app.fragments.ActivitiesList;
import com.instamaven.app.fragments.ActivitiesIncomeList;
import com.instamaven.app.fragments.ActivitiesOutcomeList;

public class ActivitiesBottomNavigationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities_bottom_navigations);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                switch (item.getItemId()) {
                    case R.id.navigation_allActivities:
                        selectedFragment = new ActivitiesList();
                        break;
                    case R.id.navigation_transactions:
                        selectedFragment = new ActivitiesIncomeList();
                        break;
                    case R.id.navigation_withdrawing:
                        selectedFragment = new ActivitiesOutcomeList();
                        break;
                }
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_layout, selectedFragment);
                transaction.commit();
                return true;
            }
        });

        //Manually displaying the first fragment - one time only
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, new ActivitiesList());
        transaction.commit();
    }
}
