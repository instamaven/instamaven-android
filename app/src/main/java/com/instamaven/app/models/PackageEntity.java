package com.instamaven.app.models;

import org.json.JSONException;
import org.json.JSONObject;

public class PackageEntity extends Entity {

    public PackageEntity(JSONObject object, Integer page, Integer position) throws JSONException {
        put("id", object.getInt("id"));
        put("title", object.getString("title"));
        put("value", object.getInt("value"));
        put("price", object.getDouble("price"));
        // save the position of this comment in adapter
        setPage(page);
        setPosition(position);
    }
}
