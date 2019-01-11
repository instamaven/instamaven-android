package com.instamaven.app.activities;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.instamaven.app.R;
import com.instamaven.app.adapters.BadgesListAdapter;
import com.instamaven.app.adapters.ViewPaperAdapter;
import com.instamaven.app.fragments.BadgesList;
import com.instamaven.app.fragments.ChatlogsIncomeList;
import com.instamaven.app.fragments.ChatlogsList;
import com.instamaven.app.fragments.ChatlogsOutcomeList;
import com.instamaven.app.fragments.FavoritesBadges;
import com.instamaven.app.fragments.MyBadgesList;

public class BadgesTabsActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badges_tabs);

        viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_account_badges);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_account_favorites);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPaperAdapter adapter = new ViewPaperAdapter(getSupportFragmentManager());
        adapter.addFragment(new MyBadgesList(), getString(R.string.my_badges));
        adapter.addFragment(new FavoritesBadges(), getString(R.string.favorites));
        viewPager.setAdapter(adapter);
    }
}
