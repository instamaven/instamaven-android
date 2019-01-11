package com.instamaven.app.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BadgeEntity extends Entity {

    public BadgeEntity(JSONObject object) throws JSONException {
        this(object, 0, 0);
    }

    public BadgeEntity(JSONObject object, Integer page, Integer position) throws JSONException {
        put("id", object.getInt("id"));
        put("user_id", object.getInt("user_id"));
        put("title", object.getString("title"));
        put("description", object.optString("description", ""));
        put("image", object.optString("image", ""));
        put("url", object.optString("url", ""));
        put("address", object.optString("address", ""));
        put("latitude", object.optDouble("latitude", 0.0));
        put("longitude", object.optDouble("longitude", 0.0));
        put("rate", object.optDouble("rate", 0.0));
        put("rates", object.optInt("rates", 0));
        put("active", object.optInt("active", 0));
        put("favorite", object.optInt("favorite", 0));
        put("amount", object.optInt("amount", 0));
        put("price", object.optInt("price"));
        put("price_type", object.optString("price_type"));
        put("tags",object.optString("tags"));
        // save the position of this badge in adapter
        setPage(page);
        setPosition(position);
    }
}
