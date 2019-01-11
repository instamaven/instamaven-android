package com.instamaven.app.activities;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.instamaven.app.R;
import com.instamaven.app.adapters.CommentsListAdapter;
import com.instamaven.app.models.Entity;
import com.instamaven.app.providers.CommentsProvider;

import java.util.ArrayList;

public class CommentsListActivity extends AppCompatActivity {

    protected CommentsProvider cp;
    protected ListView listView;
    private int badgeId = 0;
    private CommentsListAdapter adapter;
    private ArrayList<Entity> comments;
    public View spinnerView;
    private TextView emptyTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments_list);

        badgeId = getIntent().getExtras().getInt("badgeId", 0);
        emptyTV = findViewById(R.id.commentsNotFound);

        listView = findViewById(R.id.listView);
        listView.setEmptyView(emptyTV);
        // get footer view
        spinnerView = getLayoutInflater().inflate(R.layout.progress_spinner, null);

        cp = new CommentsProvider(this, listView, badgeId);
        cp.setSpinnerView(spinnerView);

        if ((comments = cp.getData()) != null) {
            adapter = new CommentsListAdapter(this, comments);
            listView.setAdapter(adapter);
            cp.setAdapter(adapter);

            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (scrollState == 0) {
                        cp.checkDataLoading();
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    cp.checkPosition(view, totalItemCount);
                }
            });
        }
    }
}

