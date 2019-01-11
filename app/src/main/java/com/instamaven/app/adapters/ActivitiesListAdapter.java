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

public class ActivitiesListAdapter extends ListAdapter {

    public static final int PURCHASE_ACTIVITY = 1;
    public static final int REWARD_ACTIVITY = 2;
    public static final int CHARGE_ACTIVITY = 3;
    public static final int WITHDRAW_ACTIVITY = 4;

    public ActivitiesListAdapter(Context context, ArrayList<Entity> comments) {
        super(context, R.layout.activities_list_item, comments);
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rowView = inflater.inflate(R.layout.activities_list_item, null, true);

        ImageView imageView = rowView.findViewById(R.id.image_view);
        switch (items.get(position).getInt("type")) {
            case PURCHASE_ACTIVITY:
                imageView.setImageDrawable(context.getDrawable(R.drawable.ic_coins_in));
                break;
            case REWARD_ACTIVITY:
                imageView.setImageDrawable(context.getDrawable(R.drawable.ic_coins_in));
                break;
            case CHARGE_ACTIVITY:
                imageView.setImageDrawable(context.getDrawable(R.drawable.ic_coins_out));
                break;
            case WITHDRAW_ACTIVITY:
                imageView.setImageDrawable(context.getDrawable(R.drawable.ic_coins_out));
                break;
        }

        TextView txtDate = rowView.findViewById(R.id.created_at);
        txtDate.setText(items.get(position).getString("created_at"));

        TextView txtDescription = rowView.findViewById(R.id.amount);
        txtDescription.setText(Jsoup.parse(items.get(position).getString("description")).text());

        return rowView;
    }
}