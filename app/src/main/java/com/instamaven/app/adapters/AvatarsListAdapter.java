package com.instamaven.app.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.instamaven.app.R;
import com.instamaven.app.models.Entity;

import org.jsoup.Jsoup;

import java.util.ArrayList;

public class AvatarsListAdapter extends ArrayAdapter {

    protected Context context;
    protected ArrayList<String> categories;

    public AvatarsListAdapter(Context context, ArrayList<String> categories) {
        super(context, R.layout.avatars_list_item, categories);
        this.context = context;
        this.categories = categories;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rowView = inflater.inflate(R.layout.avatars_list_item, null, true);

        SimpleDraweeView img = rowView.findViewById(R.id.avatarIV);
        String url = categories.get(position);
        if (!url.isEmpty()) {
            Uri uri = Uri.parse(categories.get(position));
            img.setImageURI(uri);
        }
        return rowView;
    }
}
