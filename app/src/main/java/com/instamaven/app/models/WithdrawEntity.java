package com.instamaven.app.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WithdrawEntity extends Entity {

    final private static String WITHDRAW_DATE_FORMAT = "EEE, d MMM yyyy HH:mm";

    public WithdrawEntity(JSONObject object, Integer page, Integer position) throws JSONException {
        put("coins", object.getInt("coins"));
        put("coin_price", object.getDouble("coin_price"));
        put("amount", object.getDouble("amount"));
        long timestamp = object.getLong("timestamp");
        DateFormat withdrawDateFormat = new SimpleDateFormat(WITHDRAW_DATE_FORMAT, Locale.getDefault());
        put("created_at", withdrawDateFormat.format(new Date(timestamp * 1000)));
        // save the position of this comment in adapter
        setPage(page);
        setPosition(position);
    }
}
