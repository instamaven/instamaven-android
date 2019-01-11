package com.instamaven.app.fragments;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.instamaven.app.R;
import com.instamaven.app.activities.EditBadgeActivity;
import com.instamaven.app.activities.IMApp;
import com.instamaven.app.activities.MainActivity;
import com.instamaven.app.adapters.CategoriesListAdapter;
import com.instamaven.app.models.CategoryEntity;
import com.instamaven.app.models.Entity;
import com.instamaven.app.providers.CategoriesProvider;

import java.util.ArrayList;
import java.util.Locale;

public class CategoriesGrid extends Fragment {

    private ArrayList<Entity> categories;
    private MainActivity menuActivity;
    private CategoriesListAdapter adapter;
    protected GridView gridView;
    CategoryEntity cat;
    ConstraintLayout statusConstraintLayout, balanceLayout;

    public static CategoriesGrid newInstance(int currentLevel) {
        CategoriesGrid f = new CategoriesGrid();
        IMApp.currentLevel = currentLevel;
        return f;
    }

    public CategoriesGrid() { }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        gridView = getActivity().findViewById(R.id.gridView);
        CategoriesProvider cp = new CategoriesProvider(getActivity(), gridView, IMApp.currentLevel);

        if (IMApp.currentLevel == 0) {
            menuActivity.setActionBarTitle(R.string.title_root_category);
        }
        if ((categories = cp.getData()) != null) {
            adapter = new CategoriesListAdapter(getActivity(), categories);
            gridView.setAdapter(adapter);
            cp.setAdapter(adapter);
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                cat = (CategoryEntity) adapterView.getItemAtPosition(position);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                if (cat.getInt("children") > 0) {
                    ft.replace(R.id.flMenu, CategoriesGrid.newInstance(cat.getInt("id")))
                            .addToBackStack(String.format(Locale.getDefault(), "category_%d", cat.getInt("id")))
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                            .commit();

                } else /*if (cat.getInt("aggregated_badges_count") > 0)*/ {
                    BadgesList badgesList = BadgesList.newInstance(cat.getInt("id"));
                    ft.replace(R.id.flMenu, badgesList)
                            .addToBackStack(null)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                            .commit();
                    menuActivity.setActionBarTitle(cat.getString("title"));
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories_grid, null);

        balanceLayout = view.findViewById(R.id.balanceConstrainLayout);
        IMApp.showBalance(getActivity(), balanceLayout);

        // Status Layout
        statusConstraintLayout = view.findViewById(R.id.statusConstraintLayout);
        statusConstraintLayout.setVisibility(IMApp.visibility==1 ? View.GONE : View.VISIBLE);

        TextView badgeTypeTV = view.findViewById(R.id.badgeTypeTV);
        badgeTypeTV.setText(getString(IMApp.current_mode.equals("online") ? R.string.online_badges : R.string.ondemand_badges));

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

                Intent i = new Intent(getActivity(), EditBadgeActivity.class);
                startActivity(i);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }
}
