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

public class ChatLogsIncomeListAdapter extends ListAdapter {

    protected Context context;

    public ChatLogsIncomeListAdapter(Context context, ArrayList<Entity> comments) {
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
        imageView.setImageDrawable(context.getDrawable(R.drawable.ic_chatlogs_income_call));

        txtLabel.setText(context.getString(R.string.reward));
        text = String.format("%d %s", items.get(position).getInt("value"), context.getString(R.string.coins));
        txtValue.setTextColor(context.getResources().getColor(R.color.btn_color_green));
        txtValue.setText(text);

        TextView txtTitle = rowView.findViewById(R.id.title);
        txtTitle.setText(Jsoup.parse(items.get(position).getString("title")).text());

        TextView txtDate = rowView.findViewById(R.id.created_at);
        txtDate.setText(items.get(position).getString("created_at"));

        TextView txtSeconds = rowView.findViewById(R.id.seconds);
        txtSeconds.setText(String.format("%d %s", items.get(position).getInt("seconds"), context.getString(R.string.sec)));

        return rowView;
    }
}

