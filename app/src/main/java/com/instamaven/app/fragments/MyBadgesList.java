package com.instamaven.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.instamaven.app.R;
import com.instamaven.app.activities.BadgeDetailsActivity;
import com.instamaven.app.activities.EditBadgeActivity;
import com.instamaven.app.activities.IMApp;
import com.instamaven.app.adapters.BadgesListAdapter;
import com.instamaven.app.models.BadgeEntity;
import com.instamaven.app.models.Entity;
import com.instamaven.app.providers.BadgesProvider;

import java.util.ArrayList;

public class MyBadgesList extends ListFragment {

    public int categoryId = 0;
    protected ListView listView;
    private BadgesListAdapter adapter;
    private ArrayList<Entity> badges;
    private BadgesProvider bp;
    private FloatingActionButton fab;
    private ConstraintLayout balanceLayout, statusConstraintLayout;
    public View spinnerView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView = getListView();
        bp = new BadgesProvider(getActivity(), listView, BadgesListAdapter.MY_BADGES_MODE);
        bp.setCurrentCategory(categoryId);
        bp.setSpinnerView(spinnerView);
        // reset global badge ID
        IMApp.currentBadgeId = 0;
        if ((badges = bp.getData()) != null) {
            adapter = new BadgesListAdapter(getActivity(), badges, BadgesListAdapter.MY_BADGES_MODE);
            setListAdapter(adapter);
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
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        BadgeEntity badgeEntity = (BadgeEntity) l.getItemAtPosition(position);
        Intent intent = new Intent(getActivity(), BadgeDetailsActivity.class);
        IMApp.currentBadgeId = (int) badgeEntity.get("id");
        startActivity(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // get footer view
        spinnerView = inflater.inflate(R.layout.progress_spinner, null);

        View view = inflater.inflate(R.layout.fragment_badges_list, null);
        balanceLayout = view.findViewById(R.id.balanceConstrainLayout);
        IMApp.showBalance(getActivity(), balanceLayout);

        TextView badgeTypeTV = view.findViewById(R.id.badgeTypeTV);
        badgeTypeTV.setText(getString(IMApp.current_mode.equals("online") ? R.string.online_badges : R.string.ondemand_badges));

        // Status Layout
        statusConstraintLayout = view.findViewById(R.id.statusConstraintLayout);
        statusConstraintLayout.setVisibility(IMApp.visibility==1 ? View.GONE : View.VISIBLE);

        // Action Button Add Badge
        fab = view.findViewById(R.id.floatingActionBtn);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.scale);
                fab.startAnimation(anim);

                Intent in = new Intent(getActivity(), EditBadgeActivity.class);
                startActivity(in);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }
}
