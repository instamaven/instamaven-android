package com.instamaven.app.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.instamaven.app.R;
import com.instamaven.app.adapters.NotificationsListAdapter;
import com.instamaven.app.models.Entity;
import com.instamaven.app.providers.NotificationsProvider;

import java.util.ArrayList;

public class NotificationsActivity extends AppCompatActivity {

    protected ListView notificationsListView;
    protected NotificationsProvider np;
    private NotificationsListAdapter adapter;
    private ArrayList<Entity> notifications;
    public View spinnerView;
    private TextView emptyTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        emptyTV = findViewById(R.id.notificationNotFound);

        notificationsListView = findViewById(R.id.notificationsListView);
        notificationsListView.setEmptyView(emptyTV);
        // get footer view
        spinnerView = getLayoutInflater().inflate(R.layout.progress_spinner, null);

        np = new NotificationsProvider(this, notificationsListView);

        if ((notifications = np.getData()) != null) {
            adapter = new NotificationsListAdapter(this, notifications);
            notificationsListView.setAdapter(adapter);
            np.setAdapter(adapter);

            notificationsListView.setOnScrollListener(new AbsListView.OnScrollListener() {
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
        
        // Action Button Delete All Notifications
        FloatingActionButton fab = findViewById(R.id.notifFloatingActionBtn);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    AlertDialog.Builder builder = new AlertDialog.Builder(NotificationsActivity.this);
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
                                    Toast.makeText(NotificationsActivity.this, getString(R.string.notifications_clear), Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(NotificationsActivity.this, NotificationsActivity.class));
                                    finish();
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
    }
}