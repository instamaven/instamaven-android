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

import com.instamaven.app.activities.BadgeDetailsActivity;
import com.instamaven.app.activities.EditBadgeActivity;
import com.instamaven.app.activities.MainActivity;
import com.instamaven.app.activities.IMApp;
import com.instamaven.app.R;
import com.instamaven.app.adapters.BadgesListAdapter;
import com.instamaven.app.models.BadgeEntity;
import com.instamaven.app.models.Entity;
import com.instamaven.app.providers.BadgesProvider;

import java.util.ArrayList;

public class BadgesList extends ListFragment {

    public int categoryId = 0;
    public String query = "";
    protected ListView listView;
    private BadgesListAdapter adapter;
    private ArrayList<Entity> badges;
    private BadgesProvider bp;
    FloatingActionButton fab;
    ConstraintLayout balanceLayout, statusConstraintLayout;

    private int preLast;

    public View spinnerView;

    MainActivity menuActivity;

    private static int badgesListMode = BadgesListAdapter.BADGES_MODE;

    public static BadgesList newInstance(int categoryId) {
        badgesListMode = BadgesListAdapter.BADGES_MODE;
        BadgesList f = new BadgesList();
        f.categoryId = categoryId;

        return f;
    }

    public static BadgesList newInstance(String query) {
        badgesListMode = BadgesListAdapter.SEARCH_MODE;
        BadgesList f = new BadgesList();
        f.query = query;
        //menuActivity.setActionBarTitle(query);
        return f;
    }

    public static BadgesList newInstance() {
        badgesListMode = BadgesListAdapter.FAVORITES_MODE;

        return new BadgesList();
    }

    public static BadgesList newInstance(boolean myOwnMode) {
        badgesListMode = BadgesListAdapter.MY_BADGES_MODE;

        return new BadgesList();
    }

    public BadgesList() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView = getListView();
        bp = new BadgesProvider(getActivity(), listView, badgesListMode);
        bp.setCurrentCategory(categoryId);
        bp.setQuery(query);
        bp.setSpinnerView(spinnerView);
        // reset global badge ID
        IMApp.currentBadgeId = 0;
        if ((badges = bp.getData()) != null) {
            adapter = new BadgesListAdapter(getActivity(), badges, badgesListMode);
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

        // Set Bar Title/Set Action Icons
        menuActivity = (MainActivity) getActivity();
        if (badgesListMode == BadgesListAdapter.MY_BADGES_MODE) {
            menuActivity.addBadgeMenuVisibility = true;
            menuActivity.searchMenuVisibility = false;
            menuActivity.setActionBarTitle(R.string.my_badges);
        } else if (badgesListMode == BadgesListAdapter.FAVORITES_MODE) {
            menuActivity.addBadgeMenuVisibility = true;
            menuActivity.searchMenuVisibility = false;
            menuActivity.setActionBarTitle(R.string.favorites);
        } else if (badgesListMode == BadgesListAdapter.SEARCH_MODE) {
            menuActivity.setActionBarTitle("Search:" + " " + query);
        }
        menuActivity.searchMenuVisibility = true;
        menuActivity.videoCallMenuVisibility = false;
        menuActivity.invalidateOptionsMenu();

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
