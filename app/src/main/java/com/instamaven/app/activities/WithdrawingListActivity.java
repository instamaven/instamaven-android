package com.instamaven.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.ListView;

import com.instamaven.app.R;
import com.instamaven.app.adapters.ActivitiesOutcomeListAdapter;
import com.instamaven.app.models.Entity;
import com.instamaven.app.providers.WithdrawProvider;

import java.util.ArrayList;

public class WithdrawingListActivity extends AppCompatActivity {

    protected ListView listView;
    protected WithdrawProvider withdrawProvider;
    private ArrayList<Entity> withdrawings;
    private ActivitiesOutcomeListAdapter adapter;
    public View spinnerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdrawings_list);

        listView = findViewById(R.id.withdrawingsListView);

        spinnerView = getLayoutInflater().inflate(R.layout.progress_spinner, null);

        withdrawProvider = new WithdrawProvider(this, listView);
        withdrawProvider.setSpinnerView(spinnerView);

        if ((withdrawings = withdrawProvider.getData()) != null) {
            adapter = new ActivitiesOutcomeListAdapter(this, withdrawings);
            listView.setAdapter(adapter);
            withdrawProvider.setAdapter(adapter);

            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (scrollState == 0) {
                        withdrawProvider.checkDataLoading();
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    withdrawProvider.checkPosition(view, totalItemCount);
                }
            });
        }

        final FloatingActionButton myFab = findViewById(R.id.floatingActionBtn);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale);
                myFab.startAnimation(anim);

                Intent intent = new Intent(WithdrawingListActivity.this, ProfileActivity.class);
                intent.putExtra("autoClick", "withdrawBtn");
                startActivity(intent);
            }
        });
    }
}
