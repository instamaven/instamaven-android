package com.instamaven.app.models;

import org.json.JSONException;
import org.json.JSONObject;

public class PlaceEntity extends Entity {

    public PlaceEntity(JSONObject object) throws JSONException {
        put("zip", object.getString("zip"));
        put("lat", object.getDouble("lat"));
        put("lng", object.getDouble("lng"));
        put("city", object.getString("city"));
        put("state", object.getString("state"));
        put("population", object.getInt("population"));
        put("density", object.getDouble("density"));
        put("distance", object.getDouble("distance"));
    }
}
