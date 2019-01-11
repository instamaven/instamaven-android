package com.instamaven.app.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatLogEntity extends Entity {

    final private static String CHATLOG_DATE_FORMAT = "EEE, d MMM yyyy HH:mm";

    private long timestamp;

    public ChatLogEntity(JSONObject object, Integer page, Integer position) throws JSONException {
        put("id", object.getInt("id"));
        put("badge_id", object.getInt("badge_id"));
        put("seconds", object.getInt("seconds"));
        put("price", object.getDouble("price"));
        put("price_type", object.getString("price_type"));
        put("value", object.getInt("value"));
        put("coin_price", object.getDouble("coin_price"));
        put("amount", object.getDouble("amount"));
        put("type", object.getInt("type"));
        put("title", object.getString("title"));
        //put("total", object.getInt("total"));
        timestamp = object.getLong("timestamp");
        DateFormat commentDateFormat = new SimpleDateFormat(CHATLOG_DATE_FORMAT, Locale.getDefault());
        put("created_at", commentDateFormat.format(new Date(timestamp * 1000)));
        // save the position of this comment in adapter
        setPage(page);
        setPosition(position);
    }
}
