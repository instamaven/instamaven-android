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

public class BadgeListActivity extends AppCompatActivity {

    private BadgesProvider bp;
    protected ListView badgesListView;
    private BadgesListAdapter adapter;
    private ArrayList<Entity> badges;
    private TextView emptyTV;
    private FloatingActionButton mfab;
    private ConstraintLayout balanceLayout, statusConstraintLayout;
    public String query = "";
    private String lat, lng, selected, distance, status;
    public int categoryId = 0;
    public View spinnerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badge_list);

//        Toolbar mActionBarToolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(mActionBarToolbar);
//        getSupportActionBar().setTitle("My title");

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            query = bundle.getString("q", "");
            lat = bundle.getString("lat", "");
            lng = bundle.getString("lng", "");
            selected = bundle.getString("categories", "");
            distance = bundle.getString("distance", "");
            status = bundle.getString("status", "");
        }
        // Floating Action Button GMAPS
        mfab = findViewById(R.id.mfab);
        mfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation anim = AnimationUtils.loadAnimation(BadgeListActivity.this, R.anim.scale);
                mfab.startAnimation(anim);

                Intent i = new Intent(BadgeListActivity.this, BadgeListMapsActivity.class);
                i.putExtra("query", query);
                i.putExtra("lat", lat);
                i.putExtra("lng", lng);
                i.putExtra("selected", selected);
                i.putExtra("distance", distance);
                i.putExtra("status", status);
                startActivity(i);
            }
        });

        emptyTV = findViewById(R.id.badgeNotFound);
        badgesListView = findViewById(R.id.badgesListView);
        badgesListView.setEmptyView(emptyTV);
        // get footer view
        spinnerView = getLayoutInflater().inflate(R.layout.progress_spinner, null);

        bp = new BadgesProvider(this, badgesListView, BadgesListAdapter.SEARCH_MODE);
        bp.setCurrentCategory(categoryId);
        bp.setQuery(query);
        bp.setCategories(selected);
        bp.setLatitude(lat);
        bp.setLongitude(lng);
        bp.setDistance(distance);
        bp.setSpinnerView(spinnerView);
        bp.setStatus(status);
        IMApp.currentBadgeId = 0;

        if ((badges = bp.getData()) != null) {
            adapter = new BadgesListAdapter(this, badges, BadgesListAdapter.SEARCH_MODE);
            badgesListView.setAdapter(adapter);
            bp.setAdapter(adapter);

            badgesListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (scrollState == 0) {
                        bp.checkDataLoading();
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    boolean last = bp.checkPosition(view, totalItemCount);
                    mfab.setVisibility(last ? View.INVISIBLE : View.VISIBLE);
                }
            });
        }

        badgesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                BadgeEntity badgeEntity = (BadgeEntity) adapterView.getItemAtPosition(position);
                Intent intent = new Intent(BadgeListActivity.this, BadgeDetailsActivity.class);
                IMApp.currentBadgeId = (int) badgeEntity.get("id");
                startActivity(intent);
            }
        });

        balanceLayout = findViewById(R.id.balanceConstrainLayout);
        IMApp.showBalance(BadgeListActivity.this, balanceLayout);

        TextView badgeTypeTV = findViewById(R.id.badgeTypeTV);
        badgeTypeTV.setText(getString(IMApp.current_mode.equals("online") ? R.string.online_badges : R.string.ondemand_badges));

        // Status Layout
        statusConstraintLayout = findViewById(R.id.statusConstraintLayout);
        statusConstraintLayout.setVisibility(IMApp.visibility > 0 ? View.GONE : View.VISIBLE);
    }
}
