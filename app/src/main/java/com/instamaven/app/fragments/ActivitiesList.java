package com.instamaven.app.fragments;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.instamaven.app.R;
import com.instamaven.app.activities.IMApp;
import com.instamaven.app.adapters.ActivitiesListAdapter;
import com.instamaven.app.models.Entity;
import com.instamaven.app.providers.ActivitiesProvider;

import java.util.ArrayList;

public class ActivitiesList extends ListFragment {

    private ActivitiesProvider ap;
    private ListView listView;
    private ActivitiesListAdapter adapter;
    private ArrayList<Entity> activities;
    public View ftView;
    ConstraintLayout balanceLinearLayaout;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView = getListView();
        ap = new ActivitiesProvider(getActivity(), listView);

        if ((activities = ap.getData()) != null) {
            adapter = new ActivitiesListAdapter(getActivity(), activities);
            setListAdapter(adapter);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String val = "";

        // get footer view
        ftView = inflater.inflate(R.layout.progress_spinner, null);

        View view = inflater.inflate(R.layout.fragment_activities_list, null);
        balanceLinearLayaout = view.findViewById(R.id.balanceConstrainLayout);
        IMApp.showBalance(getActivity(), balanceLinearLayaout);

        // Inflate the layout for this fragment
        return view;
    }
}