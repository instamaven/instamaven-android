package com.instamaven.app.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.instamaven.app.R;
import com.instamaven.app.adapters.AvatarsThemesAdapter;
import com.instamaven.app.models.Entity;
import com.instamaven.app.providers.AvatarsThemesProvider;

import java.util.ArrayList;

public class AvatarsCategoriesListActivity extends AppCompatActivity {

    protected ListView listView;
    protected ArrayList<Entity> themes;
    private AvatarsThemesProvider atp;
    private TextView emptyTV;
    public View spinnerView;
    private AvatarsThemesAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instamaven_list);

        listView = findViewById(R.id.listView);
        emptyTV = findViewById(R.id.themesNotFound);

        listView = findViewById(R.id.listView);
        listView.setEmptyView(emptyTV);
        // get footer view
        spinnerView = getLayoutInflater().inflate(R.layout.progress_spinner, null);

        atp = new AvatarsThemesProvider(this, listView);
        atp.setSpinnerView(spinnerView);

        if ((themes = atp.getData()) != null) {
            adapter = new AvatarsThemesAdapter(this, themes);
            listView.setAdapter(adapter);
            atp.setAdapter(adapter);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(AvatarsCategoriesListActivity.this, AvatarsGridActivity.class);
                i.putExtra("theme", themes.get(position).getString("theme"));
                startActivity(i);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { }
}