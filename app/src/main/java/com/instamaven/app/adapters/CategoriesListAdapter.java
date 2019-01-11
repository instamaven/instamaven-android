package com.instamaven.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.instamaven.app.R;
import com.instamaven.app.models.Entity;

import org.jsoup.Jsoup;

import java.util.ArrayList;

public class CategoriesListAdapter extends ListAdapter {

    protected Integer[] imageIds = {
            R.drawable.ic_cat_noimg,
            R.drawable.ic_cat_baby,
            R.drawable.ic_cat_healthand_beauty,
            R.drawable.ic_cat_cloth,
            R.drawable.ic_cat_compserv,
            R.drawable.ic_cat_education,
            R.drawable.ic_cat_household_rep,
            R.drawable.ic_cat_entertain,
            R.drawable.ic_cat_food,
            R.drawable.ic_cat_gift,
            R.drawable.ic_cat_house,
            R.drawable.ic_cat_pet,
            R.drawable.ic_cat_other,
            R.drawable.ic_cat_travel,
            R.drawable.ic_cat_vehide,
    };

    public CategoriesListAdapter(Context context, ArrayList<Entity> categories) {
        super(context, R.layout.categories_grid_item, categories);
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rowView = inflater.inflate(R.layout.categories_grid_item, null, true);

        ImageView imgIcon = rowView.findViewById(R.id.icon);
        Integer id = items.get(position).getInt("id");

        if (id > 0 && id < imageIds.length) {
            imgIcon.setImageResource(imageIds[id]);
        } else {
            imgIcon.setImageResource(imageIds[0]);
        }

        TextView txtTitle = rowView.findViewById(R.id.title);
        txtTitle.setText(Jsoup.parse(items.get(position).getString("title")).text());

        TextView txtCount = rowView.findViewById(R.id.aggregated_badges_count);
        Integer cnt = items.get(position).getInt("aggregated_badges_count");
        txtCount.setText(cnt.toString());

        return rowView;
    }
}