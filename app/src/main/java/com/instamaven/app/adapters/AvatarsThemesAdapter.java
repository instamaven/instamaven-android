package com.instamaven.app.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.instamaven.app.R;
import com.instamaven.app.models.Entity;

import java.util.ArrayList;

public class AvatarsThemesAdapter extends ListAdapter {

    public AvatarsThemesAdapter(Context context, ArrayList<Entity> themes) {
        super(context, R.layout.avatars_list_item, themes);
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rowView = inflater.inflate(R.layout.avatars_themes_list_item, null, true);

        SimpleDraweeView draweeView = rowView.findViewById(R.id.themeImage);
        draweeView.setImageResource(R.drawable.ic_user_no_image);
        String imgSrc = items.get(position).getString("image");
        if (!imgSrc.equals("")) {
            Uri uri = Uri.parse(imgSrc);
            draweeView.setImageURI(uri);
        }

        TextView txtTitle = rowView.findViewById(R.id.themeTitle);
        txtTitle.setText(items.get(position).getString("title"));

        TextView txtTag = rowView.findViewById(R.id.themeTag);
        txtTag.setText(items.get(position).getString("tag"));

        return rowView;
    }
}
