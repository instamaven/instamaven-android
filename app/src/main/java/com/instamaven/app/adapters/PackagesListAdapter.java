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

public class PackagesListAdapter extends ListAdapter {

    public PackagesListAdapter(Context context, ArrayList<Entity> packages) {
        super(context, R.layout.package_list_item, packages);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rowView = inflater.inflate(R.layout.package_list_item, null, true);

        TextView txtTitle = rowView.findViewById(R.id.titleTV);
        txtTitle.setText(Jsoup.parse(items.get(position).getString("title")).text());

        TextView txtValue = rowView.findViewById(R.id.coincePriceTV);
        txtValue.setText(items.get(position).getInt("value").toString());

        TextView txtPrice = rowView.findViewById(R.id.priceTV);
        txtPrice.setText(items.get(position).getDouble("price").toString());

        return rowView;
    }
}
