package com.instamaven.app.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.instamaven.app.R;
import com.instamaven.app.adapters.ChatLogsListAdapter;
import com.instamaven.app.models.Entity;
import com.instamaven.app.providers.ChatLogsProvider;

import java.util.ArrayList;

public class ChatLogsListActivity extends AppCompatActivity {

    protected ChatLogsProvider clp;
    protected ListView listView;
    private ChatLogsListAdapter adapter;
    private ArrayList<Entity> chatlogs;
    public View spinnerView;
    private TextView emptyTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatlogs_list);

        emptyTV = findViewById(R.id.chatLogsNotFound);
        listView = findViewById(R.id.chatListView);
        listView.setEmptyView(emptyTV);
        // get footer view
        spinnerView = getLayoutInflater().inflate(R.layout.progress_spinner, null);

        clp = new ChatLogsProvider(this, listView);
        clp.setSpinnerView(spinnerView);

        if ((chatlogs = clp.getData()) != null) {
            adapter = new ChatLogsListAdapter(this, chatlogs);
            listView.setAdapter(adapter);
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
}
