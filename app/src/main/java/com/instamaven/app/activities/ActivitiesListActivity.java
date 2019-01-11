package com.instamaven.app.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.instamaven.app.R;
import com.instamaven.app.adapters.ActivitiesListAdapter;
import com.instamaven.app.models.Entity;
import com.instamaven.app.providers.ActivitiesProvider;

import java.util.ArrayList;

public class ActivitiesListActivity extends AppCompatActivity {

    ActivitiesProvider ap;
    ListView listView;
    private ActivitiesListAdapter adapter;
    private ArrayList<Entity> activities;
    public View spinnerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities_list);

        listView = findViewById(R.id.listView);
        // get footer view
        spinnerView = getLayoutInflater().inflate(R.layout.progress_spinner, null);

        ap = new ActivitiesProvider(this, listView);
        ap.setSpinnerView(spinnerView);

        if ((activities = ap.getData()) != null) {
            adapter = new ActivitiesListAdapter(this, activities);
            listView.setAdapter(adapter);
            ap.setAdapter(adapter);

            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (scrollState == 0) {
                        ap.checkDataLoading();
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    ap.checkPosition(view, totalItemCount);
                }
            });
        }
    }
}

