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

public class ChatLogsListAdapter extends ListAdapter {

    public static final int OUTCOME_CALL = 1;
    public static final int INCOME_CALL = 2;

    protected Context context;

    public ChatLogsListAdapter(Context context, ArrayList<Entity> comments) {
        super(context, R.layout.chatlogs_item, comments);
        this.context = context;
    }

    public View getView(int position, View view, ViewGroup parent) {
        String text = "";
        LayoutInflater inflater = LayoutInflater.from(context);
        View rowView = inflater.inflate(R.layout.chatlogs_item, null, true);

        TextView txtLabel = rowView.findViewById(R.id.labelCoins);
        TextView txtValue = rowView.findViewById(R.id.value);
        ImageView imageView = rowView.findViewById(R.id.image_view);
        switch (items.get(position).getInt("type")) {
            case OUTCOME_CALL:
                imageView.setImageDrawable(context.getDrawable(R.drawable.ic_chatlogs_outcome_call));
                txtLabel.setText(context.getString(R.string.charge));
                text = String.format("%d %s", items.get(position).getInt("value"), context.getString(R.string.coins));
                txtValue.setTextColor(context.getResources().getColor(R.color.picker_color_red));
                break;
            case INCOME_CALL:
                imageView.setImageDrawable(context.getDrawable(R.drawable.ic_chatlogs_income_call));
                txtLabel.setText(context.getString(R.string.reward));
                text = String.format("%d %s", items.get(position).getInt("value"), context.getString(R.string.coins));
                txtValue.setTextColor(context.getResources().getColor(R.color.btn_color_green));
                break;
            default:
//                imageView.setImageDrawable(context.getDrawable(R.drawable.ic_activities_reward));
                break;
        }

        txtValue.setText(text);

        TextView txtTitle = rowView.findViewById(R.id.title);
        txtTitle.setText(Jsoup.parse(items.get(position).getString("title")).text());

        TextView txtDate = rowView.findViewById(R.id.created_at);
        txtDate.setText(items.get(position).getString("created_at"));

        TextView txtSeconds = rowView.findViewById(R.id.seconds);
        txtSeconds.setText(String.format("%d %s", items.get(position).getInt("seconds"), context.getString(R.string.sec)));

//        TextView txtTotal = rowView.findViewById(R.id.totalCall);
//        txtTotal.setText(items.get(position).getInt("total"));

        return rowView;
    }
}