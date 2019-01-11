package com.instamaven.app.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CategoryEntity extends Entity {

    public CategoryEntity(JSONObject object, Integer page, Integer position) throws JSONException {
        put("id", object.getInt("id"));
        put("title", object.getString("title"));
        put("aggregated_badges_count", object.getInt("aggregated_badges_count"));
        put("children", object.getInt("children"));
        put("parent_id", object.getInt("parent_id"));
        // save the position of this comment in adapter
        setPage(page);
        setPosition(position);
    }
}
