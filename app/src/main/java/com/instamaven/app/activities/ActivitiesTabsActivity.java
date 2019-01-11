package com.instamaven.app.activities;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.instamaven.app.R;
import com.instamaven.app.adapters.ViewPaperAdapter;
import com.instamaven.app.fragments.ActivitiesList;
import com.instamaven.app.fragments.ActivitiesIncomeList;
import com.instamaven.app.fragments.ActivitiesOutcomeList;

public class ActivitiesTabsActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities_tabs);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
//        setupTabIcons();

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_all_activities);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_coins_in);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_coins_out);

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPaperAdapter adapter = new ViewPaperAdapter(getSupportFragmentManager());
        adapter.addFragment(new ActivitiesList(), getString(R.string.all_activities));
        adapter.addFragment(new ActivitiesIncomeList(), getString(R.string.list_transactions));
        adapter.addFragment(new ActivitiesOutcomeList(), getString(R.string.list_withdrawing));
        viewPager.setAdapter(adapter);
    }
}
