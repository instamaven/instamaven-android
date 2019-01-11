package com.instamaven.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ListFragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;

import com.instamaven.app.activities.EditBadgeActivity;
import com.instamaven.app.activities.IMApp;
import com.instamaven.app.activities.MainActivity;
import com.instamaven.app.R;
import com.instamaven.app.adapters.CategoriesListAdapter;
import com.instamaven.app.models.CategoryEntity;
import com.instamaven.app.models.Entity;
import com.instamaven.app.providers.CategoriesProvider;

import java.util.ArrayList;
import java.util.Locale;

public class CategoriesList extends ListFragment {

    private ArrayList<Entity> categories;
    private MainActivity menuActivity;
    private CategoriesListAdapter adapter;
    protected ListView listView;

    public static CategoriesList newInstance(int currentLevel) {
        CategoriesList f = new CategoriesList();
        IMApp.currentLevel = currentLevel;
        return f;
    }

    public CategoriesList() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView = getListView();
        CategoriesProvider cp = new CategoriesProvider(getActivity(), listView, IMApp.currentLevel);

        if (IMApp.currentLevel == 0) {
            menuActivity.setActionBarTitle(R.string.title_root_category);
        }

        if ((categories = cp.getData()) != null) {
            adapter = new CategoriesListAdapter(getActivity(), categories);
            setListAdapter(adapter);
            cp.setAdapter(adapter);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        CategoryEntity cat = (CategoryEntity) l.getItemAtPosition(position);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (cat.getInt("children") > 0) {
            ft.replace(R.id.flMenu, CategoriesList.newInstance(cat.getInt("id")))
                .addToBackStack(String.format(Locale.getDefault(), "category_%d", cat.getInt("id")))
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
        } else if (cat.getInt("aggregated_badges_count") > 0) {
            BadgesList badgesList = BadgesList.newInstance(cat.getInt("id"));
            ft.replace(R.id.flMenu, badgesList)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_categories_list, null);

        // set action icons
        menuActivity = ((MainActivity) getActivity());
        menuActivity.videoCallMenuVisibility = false;
        menuActivity.searchMenuVisibility = true;
        menuActivity.invalidateOptionsMenu();

        // Action Button Add Badge
        final FloatingActionButton fab = view.findViewById(R.id.floatingActionBtn);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.scale);
                fab.startAnimation(anim);

                Intent in = new Intent(getActivity(), EditBadgeActivity.class);
                startActivity(in);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }
}



