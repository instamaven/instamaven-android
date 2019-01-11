package com.instamaven.app.models;

import org.json.JSONException;
import org.json.JSONObject;

public class AvatarThemeEntity extends Entity {

    public AvatarThemeEntity(JSONObject object, Integer page, Integer position) throws JSONException {
        put("theme", object.getString("theme"));
        put("title", object.getString("title"));
        put("tag", object.getString("tag"));
        put("image", object.getString("image"));
        // save the position of this comment in adapter
        setPage(page);
        setPosition(position);
    }
}
