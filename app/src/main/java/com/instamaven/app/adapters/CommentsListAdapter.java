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

import org.jsoup.Jsoup;

import java.util.ArrayList;

public class CommentsListAdapter extends ListAdapter {

    public CommentsListAdapter(Context context, ArrayList<Entity> comments) {
        super(context, R.layout.comments_list_item, comments);
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rowView = inflater.inflate(R.layout.comments_list_item, null, true);

        SimpleDraweeView draweeView = rowView.findViewById(R.id.userAvatar);
        draweeView.setImageResource(R.drawable.ic_user_no_image);
        String imgSrc = items.get(position).getString("avatar");
        if (!imgSrc.equals("")) {
            Uri uri = Uri.parse(imgSrc);
            draweeView.setImageURI(uri);
        }

        TextView txtDate = rowView.findViewById(R.id.created_at);
        txtDate.setText(items.get(position).getString("created_at"));

        TextView txtName = rowView.findViewById(R.id.user_name);
        txtName.setText(items.get(position).getString("user_name"));

        TextView txtComment = rowView.findViewById(R.id.comment);
        txtComment.setText(Jsoup.parse(items.get(position).getString("comment")).text());

        return rowView;
    }
}