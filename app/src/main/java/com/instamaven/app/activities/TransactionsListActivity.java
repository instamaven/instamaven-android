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
import com.instamaven.app.adapters.ActivitiesIncomeListAdapter;
import com.instamaven.app.models.Entity;
import com.instamaven.app.providers.TransactionsProvider;

import java.util.ArrayList;

public class TransactionsListActivity extends AppCompatActivity {

    protected ListView listView;
    protected TransactionsProvider transactionsProvider;
    private ArrayList<Entity> transactions;
    private ActivitiesIncomeListAdapter adapter;
    public View spinnerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions_list);

        listView = findViewById(R.id.transactionsListView);

        spinnerView = getLayoutInflater().inflate(R.layout.progress_spinner, null);

        transactionsProvider = new TransactionsProvider(this, listView);
        transactionsProvider.setSpinnerView(spinnerView);

        if ((transactions = transactionsProvider.getData()) != null) {
            adapter = new ActivitiesIncomeListAdapter(this, transactions);
            listView.setAdapter(adapter);
            transactionsProvider.setAdapter(adapter);

            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (scrollState == 0) {
                        transactionsProvider.checkDataLoading();
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    transactionsProvider.checkPosition(view, totalItemCount);
                }
            });
        }

        final FloatingActionButton myFab = findViewById(R.id.floatingActionBtn);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale);
                myFab.startAnimation(anim);

                Intent intent = new Intent(TransactionsListActivity.this, ProfileActivity.class);
                intent.putExtra("autoClick", "rechargeBtn");
                startActivity(intent);
            }
        });
    }
}