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

public class NotificationsListAdapter extends ListAdapter {

    public static final int SYSTEM_NOTIFICATION = 1;
    public static final int PURCHASE_NOTIFICATION = 2;
    public static final int REWARD_NOTIFICATION = 3;
    public static final int CHARGE_NOTIFICATION = 4;
    public static final int WITHDRAW_NOTIFICATION = 5;

    public NotificationsListAdapter(Context context, ArrayList<Entity> comments) {
        super(context, R.layout.notifications_list_item, comments);
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rowView = inflater.inflate(R.layout.notifications_list_item, null, true);

        ImageView imageView = rowView.findViewById(R.id.image_view);
        switch (items.get(position).getInt("type")) {
            case PURCHASE_NOTIFICATION:
                imageView.setImageDrawable(context.getDrawable(R.drawable.ic_coins_in));
                break;
            case REWARD_NOTIFICATION:
                imageView.setImageDrawable(context.getDrawable(R.drawable.ic_coins_in));
                break;
            case CHARGE_NOTIFICATION:
                imageView.setImageDrawable(context.getDrawable(R.drawable.ic_coins_out));
                break;
            case WITHDRAW_NOTIFICATION:
                imageView.setImageDrawable(context.getDrawable(R.drawable.ic_coins_out));
                break;
            case SYSTEM_NOTIFICATION:
            default:
                imageView.setImageDrawable(context.getDrawable(R.drawable.ic_notifications_system));
                break;
        }

        TextView txtDate = rowView.findViewById(R.id.created_at);
        txtDate.setText(items.get(position).getString("created_at"));

        TextView txtDescription = rowView.findViewById(R.id.description);
        txtDescription.setText(Jsoup.parse(items.get(position).getString("description")).text());

        return rowView;
    }
}