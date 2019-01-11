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

import com.instamaven.app.R;
import com.instamaven.app.activities.IMApp;
import com.instamaven.app.activities.ProfileActivity;
import com.instamaven.app.adapters.ActivitiesIncomeListAdapter;
import com.instamaven.app.models.Entity;
import com.instamaven.app.providers.ActivitiesIncomeProvider;

import java.util.ArrayList;

public class ActivitiesIncomeList extends ListFragment {

    private ActivitiesIncomeProvider aip;
    private ListView listView;
    private ActivitiesIncomeListAdapter adapter;
    private ArrayList<Entity> transactions;
    public View ftView;
    ConstraintLayout balanceLinearLayaout;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView = getListView();
        aip = new ActivitiesIncomeProvider(getActivity(), listView);

        if ((transactions = aip.getData()) != null) {
            adapter = new ActivitiesIncomeListAdapter(getActivity(), transactions);
            setListAdapter(adapter);
            aip.setAdapter(adapter);

            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (scrollState == 0) {
                        aip.checkDataLoading();
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    aip.checkPosition(view, totalItemCount);
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String val = "";

        // get footer view
        ftView = inflater.inflate(R.layout.progress_spinner, null);

        View view = inflater.inflate(R.layout.fragment_activities_income_list, container, false);
        balanceLinearLayaout = view.findViewById(R.id.balanceConstrainLayout);
        IMApp.showBalance(getActivity(), balanceLinearLayaout);

        final FloatingActionButton fab = view.findViewById(R.id.floatingActionBtn);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.scale);
                fab.startAnimation(anim);

                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra("autoClick", "rechargeBtn");
                startActivity(intent);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }
}
