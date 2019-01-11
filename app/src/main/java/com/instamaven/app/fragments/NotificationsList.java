package com.instamaven.app.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.instamaven.app.R;
import com.instamaven.app.activities.NotificationsActivity;
import com.instamaven.app.adapters.NotificationsListAdapter;
import com.instamaven.app.models.Entity;
import com.instamaven.app.providers.NotificationsProvider;
import com.instamaven.app.utils.RequestClient;

import java.util.ArrayList;

public class NotificationsList extends ListFragment {

    private NotificationsProvider np;
    private ListView listView;
    private NotificationsListAdapter adapter;
    private ArrayList<Entity> notifications;
    public View ftView;

    protected RequestClient.Builder client;

    public static NotificationsList newInstance() {
        NotificationsList f = new NotificationsList();

        return f;
    }

    public NotificationsList() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView = getListView();
        np = new NotificationsProvider(getActivity(), listView);

        if ((notifications = np.getData()) != null) {
            adapter = new NotificationsListAdapter(getActivity(), notifications);
            setListAdapter(adapter);
            np.setAdapter(adapter);

            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (scrollState == 0) {
                        np.checkDataLoading();
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    np.checkPosition(view, totalItemCount);
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // get footer view
        ftView = inflater.inflate(R.layout.progress_spinner, null);

        View view = inflater.inflate(R.layout.fragment_notifications_list, null);

        // Action Button Delete All Notifications
        FloatingActionButton fab = view.findViewById(R.id.notifFloatingActionBtn);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setCancelable(false);
                    builder.setMessage(getString(R.string.notification_all_delete));
                    builder.setPositiveButton(R.string.ok_btn,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                    try {
                                        np.clearAll();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                    ft.replace(R.id.flMenu, new NotificationsList());
                                    ft.commit();
                                    Toast.makeText(getActivity(), getString(R.string.notifications_clear), Toast.LENGTH_LONG).show();
                                }

                            }).setNegativeButton(R.string.cancel_btn,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                }
                            }).show();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Inflate the layout for this fragment
        return  view;
    }
}