package com.instamaven.app.activities;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.instamaven.app.R;
import com.instamaven.app.adapters.ViewPaperAdapter;
import com.instamaven.app.fragments.ChatlogsIncomeList;
import com.instamaven.app.fragments.ChatlogsList;
import com.instamaven.app.fragments.ChatlogsOutcomeList;

public class ChatLogsTabsActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_logs_tabs);

        viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
//        setupTabIcons();

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_chatlogs_all);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_chatlogs_income_call);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_chatlogs_outcome_call);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPaperAdapter adapter = new ViewPaperAdapter(getSupportFragmentManager());
        adapter.addFragment(new ChatlogsList(), getString(R.string.all_chatlogs));
        adapter.addFragment(new ChatlogsIncomeList(), getString(R.string.income_chatlogs));
        adapter.addFragment(new ChatlogsOutcomeList(), getString(R.string.outcome_chatlogs));
        viewPager.setAdapter(adapter);
    }
}
