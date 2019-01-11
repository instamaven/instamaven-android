package com.instamaven.app.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CommentEntity extends Entity {

    final private static String COMMENT_DATE_FORMAT = "EEE, d MMM yyyy HH:mm";

    private long timestamp;

    public CommentEntity(JSONObject object, Integer page, Integer position) throws JSONException {
        put("id", object.getInt("id"));
        put("comment", object.getString("comment"));
        put("user_name", object.getString("user_name"));
        put("avatar", object.getString("avatar"));
        timestamp = object.getLong("timestamp");
        DateFormat commentDateFormat = new SimpleDateFormat(COMMENT_DATE_FORMAT, Locale.getDefault());
        put("created_at", commentDateFormat.format(new Date(this.timestamp * 1000)));
        // save the position of this comment in adapter
        setPage(page);
        setPosition(position);
    }
}
