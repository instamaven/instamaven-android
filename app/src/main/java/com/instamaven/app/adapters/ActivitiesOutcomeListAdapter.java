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

public class ActivitiesOutcomeListAdapter extends ListAdapter {

//    public static final int CHARGE_ACTIVITY = 1;
//    public static final int WITHDRAW_ACTIVITY = 2;

    public ActivitiesOutcomeListAdapter(Context context, ArrayList<Entity> withdrawings) {
        super(context, R.layout.activities_outcome_list_item, withdrawings);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rowView = inflater.inflate(R.layout.activities_outcome_list_item, null, true);

//        ImageView imageView = rowView.findViewById(R.id.image_view);
//        switch (items.get(position).getInt("type")) {
//            case CHARGE_ACTIVITY:
//                imageView.setImageDrawable(context.getDrawable(R.drawable.ic_coins_out));
//                break;
//            case WITHDRAW_ACTIVITY:
//                imageView.setImageDrawable(context.getDrawable(R.drawable.ic_coins_out));
//                break;
//        }

        TextView dateTV = rowView.findViewById(R.id.withdrawDateTV);
        dateTV.setText(items.get(position).getString("created_at"));

        TextView txtDescription = rowView.findViewById(R.id.description);
        txtDescription.setText(Jsoup.parse(items.get(position).getString("description")).text());

        return rowView;
    }
}
