package com.instamaven.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.instamaven.app.R;
import com.instamaven.app.adapters.BadgesListAdapter;
import com.instamaven.app.models.BadgeEntity;
import com.instamaven.app.models.Entity;
import com.instamaven.app.providers.BadgesProvider;

import java.util.ArrayList;

public class MyBadgeListActivity extends AppCompatActivity {

    protected BadgesProvider bp;
    protected ListView listView;
    private BadgesListAdapter adapter;
    private ArrayList<Entity> chatlogs;
    public View spinnerView;
    private TextView emptyTV;
    private  String val;
    private FloatingActionButton fab;
    private ConstraintLayout balanceLayout, statusConstraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_badges_list);

        // Floating Action Button Add Badge
        fab = findViewById(R.id.floatingActionBtn);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation anim = AnimationUtils.loadAnimation(MyBadgeListActivity.this, R.anim.scale);
                fab.startAnimation(anim);

                Intent intent = new Intent(MyBadgeListActivity.this, EditBadgeActivity.class);
                startActivity(intent);
            }
        });

        emptyTV = findViewById(R.id.badgeNotFound);
        listView = findViewById(R.id.badgesListView);
        listView.setEmptyView(emptyTV);
        // get footer view
        spinnerView = getLayoutInflater().inflate(R.layout.progress_spinner, null);

        bp = new BadgesProvider(this, listView, BadgesListAdapter.MY_BADGES_MODE);
        bp.setSpinnerView(spinnerView);

        if ((chatlogs = bp.getData()) != null) {
            adapter = new BadgesListAdapter(this, chatlogs, BadgesListAdapter.MY_BADGES_MODE);
            listView.setAdapter(adapter);
            bp.setAdapter(adapter);

            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (scrollState == 0) {
                        bp.checkDataLoading();
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    boolean last = bp.checkPosition(view, totalItemCount);
                    fab.setVisibility(last ? View.INVISIBLE : View.VISIBLE);
                }
            });
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                BadgeEntity badgeEntity = (BadgeEntity) adapterView.getItemAtPosition(position);
                Intent intent = new Intent(MyBadgeListActivity.this, BadgeDetailsActivity.class);
                IMApp.currentBadgeId = (int) badgeEntity.get("id");
                startActivity(intent);
            }
        });

        balanceLayout = findViewById(R.id.balanceConstrainLayout);
        IMApp.showBalance(MyBadgeListActivity.this, balanceLayout);

        // Status Layout
        statusConstraintLayout = findViewById(R.id.statusConstraintLayout);
        statusConstraintLayout.setVisibility(IMApp.visibility > 0 ? View.GONE : View.VISIBLE);
    }
}
