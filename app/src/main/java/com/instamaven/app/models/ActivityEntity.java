package com.instamaven.app.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ActivityEntity extends Entity {

    final private static String ACTIVITY_DATE_FORMAT = "EEE, d MMM yyyy HH:mm";

    private long timestamp;

    public ActivityEntity(JSONObject object, Integer page, Integer position) throws JSONException {
        put("id", object.getInt("id"));
        put("description", object.getString("description"));
        put("type", object.getInt("type"));
        timestamp = object.getLong("timestamp");
        DateFormat commentDateFormat = new SimpleDateFormat(ACTIVITY_DATE_FORMAT, Locale.getDefault());
        put("created_at", commentDateFormat.format(new Date(this.timestamp * 1000)));
        // save the position of this comment in adapter
        setPage(page);
        setPosition(position);
    }
}
