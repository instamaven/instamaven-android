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
import com.instamaven.app.adapters.ChatLogsIncomeListAdapter;
import com.instamaven.app.models.Entity;
import com.instamaven.app.providers.ChatLogsIncomeProvider;

import java.util.ArrayList;

public class ChatlogsIncomeList extends ListFragment {

    ChatLogsIncomeProvider clp;
    ListView listView;
    private ChatLogsIncomeListAdapter adapter;
    private ArrayList<Entity> chatlogs;
    public View spinnerView;
    public View ftView;
    ConstraintLayout balanceLinearLayaout;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView = getListView();
        clp = new ChatLogsIncomeProvider(getActivity(), listView);

        if ((chatlogs = clp.getData()) != null) {
            adapter = new ChatLogsIncomeListAdapter(getActivity(), chatlogs);
            setListAdapter(adapter);
            clp.setAdapter(adapter);

            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (scrollState == 0) {
                        clp.checkDataLoading();
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    clp.checkPosition(view, totalItemCount);
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String val = "";

        // get footer view
        ftView = inflater.inflate(R.layout.progress_spinner, null);

        View view = inflater.inflate(R.layout.fragment_chatlogs, container, false);
        balanceLinearLayaout = view.findViewById(R.id.balanceConstrainLayout);
        IMApp.showBalance(getActivity(), balanceLinearLayaout);

//        final FloatingActionButton fab = view.findViewById(R.id.floatingActionBtn);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.scale);
//                fab.startAnimation(anim);
//
//                Intent intent = new Intent(getActivity(), ProfileActivity.class);
//                intent.putExtra("autoClick", "rechargeBtn");
//                startActivity(intent);
//            }
//        });

        // Inflate the layout for this fragment
        return view;
    }
}
