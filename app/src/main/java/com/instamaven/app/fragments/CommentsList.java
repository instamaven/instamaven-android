package com.instamaven.app.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.instamaven.app.R;
import com.instamaven.app.adapters.CommentsListAdapter;
import com.instamaven.app.models.Entity;
import com.instamaven.app.providers.CommentsProvider;

import java.util.ArrayList;

public class CommentsList extends ListFragment {

    private CommentsProvider cp;
    private int badgeId = 0;
    private ListView listView;
    private CommentsListAdapter adapter;
    private ArrayList<Entity> comments;
    public View ftView;

    public static CommentsList newInstance(int badgeId) {
        CommentsList f = new CommentsList();
        f.badgeId = badgeId;

        return f;
    }

    public CommentsList() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView = getListView();
        cp = new CommentsProvider(getActivity(), listView, badgeId);

        if ((comments = cp.getData()) != null) {
            adapter = new CommentsListAdapter(getActivity(), comments);
            setListAdapter(adapter);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // get footer view
        ftView = inflater.inflate(R.layout.progress_spinner, null);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_comments_list, null);
    }
}