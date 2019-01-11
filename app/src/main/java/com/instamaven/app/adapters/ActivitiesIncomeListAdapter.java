package com.instamaven.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.instamaven.app.R;
import com.instamaven.app.models.Entity;

import org.jsoup.Jsoup;

import java.util.ArrayList;

public class ActivitiesIncomeListAdapter extends ListAdapter {

//    public static final int PURCHASE_ACTIVITY = 1;
//    public static final int REWARD_ACTIVITY = 2;

    public ActivitiesIncomeListAdapter(Context context, ArrayList<Entity> transactions) {
        super(context, R.layout.activities_income_list_item, transactions);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rowView = inflater.inflate(R.layout.activities_income_list_item, null, true);

//        ImageView imageView = rowView.findViewById(R.id.image_view);
//        imageView.setImageDrawable(context.getDrawable(R.drawable.ic_coins_in));
//        switch (items.get(position).getInt("type")) {
//            case PURCHASE_ACTIVITY:
//                imageView.setImageDrawable(context.getDrawable(R.drawable.ic_coins_in));
//                break;
//            case REWARD_ACTIVITY:
//                imageView.setImageDrawable(context.getDrawable(R.drawable.ic_coins_in));
//                break;
//        }

        TextView txtDate = rowView.findViewById(R.id.dateTV);
        txtDate.setText(items.get(position).getString("created_at"));

        TextView txtDescription = rowView.findViewById(R.id.description);
        txtDescription.setText(Jsoup.parse(items.get(position).getString("description")).text());

        return rowView;
    }
}
