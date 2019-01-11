package com.instamaven.app.activities;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.instamaven.app.utils.ProfileHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FavoritesBadgeActivity extends AppCompatActivity {

    protected BadgesProvider bp;
    protected ListView favoritesListView;
    private BadgesListAdapter adapter;
    private ArrayList<Entity> favorites;
    public View spinnerView;
    private TextView emptyTV;
//    private  String val;
    protected FloatingActionButton fab;
    protected ConstraintLayout statusConstraintLayout, balanceLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites_badge);

        // Action Button Add Badge
        fab = findViewById(R.id.floatingActionBtn);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation anim = AnimationUtils.loadAnimation(FavoritesBadgeActivity.this, R.anim.scale);
                fab.startAnimation(anim);

                Intent in = new Intent(FavoritesBadgeActivity.this, EditBadgeActivity.class);
                startActivity(in);
            }
        });

        emptyTV = findViewById(R.id.badgeNotFound);
        favoritesListView = findViewById(R.id.favoritesListView);
        favoritesListView.setEmptyView(emptyTV);

        // get footer view
        spinnerView = getLayoutInflater().inflate(R.layout.progress_spinner, null);

        bp = new BadgesProvider(this, favoritesListView, BadgesListAdapter.FAVORITES_MODE);
        bp.setSpinnerView(spinnerView);

        if ((favorites = bp.getData()) != null) {
            adapter = new BadgesListAdapter(this, favorites, BadgesListAdapter.FAVORITES_MODE);
            favoritesListView.setAdapter(adapter);
            bp.setAdapter(adapter);
            //emptyTV.setVisibility(View.GONE);

            favoritesListView.setOnScrollListener(new AbsListView.OnScrollListener() {
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

        // Listener ListView
        favoritesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                BadgeEntity badgeEntity = (BadgeEntity) adapterView.getItemAtPosition(position);
                Intent intent = new Intent(FavoritesBadgeActivity.this, BadgeDetailsActivity.class);
                IMApp.currentBadgeId = (int) badgeEntity.get("id");
                startActivity(intent);
            }
        });

        balanceLayout = findViewById(R.id.balanceConstrainLayout);
        IMApp.showBalance(FavoritesBadgeActivity.this, balanceLayout);

        // Status Layout
        statusConstraintLayout = findViewById(R.id.statusConstraintLayout);
        statusConstraintLayout.setVisibility(IMApp.visibility > 0 ? View.GONE : View.VISIBLE);
    }
}
