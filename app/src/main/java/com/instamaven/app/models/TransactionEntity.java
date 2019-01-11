package com.instamaven.app.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TransactionEntity extends Entity {

    final private static String TRANSACTION_DATE_FORMAT = "EEE, d MMM yyyy HH:mm";

    public TransactionEntity(JSONObject object, Integer page, Integer position) throws JSONException {
        put("amount", object.getInt("amount"));
        put("value", object.getInt("value"));
        long timestamp = object.getLong("timestamp");
        DateFormat transactionDateFormat = new SimpleDateFormat(TRANSACTION_DATE_FORMAT, Locale.getDefault());
        put("created_at", transactionDateFormat.format(new Date(timestamp * 1000)));
        // save the position of this comment in adapter
        setPage(page);
        setPosition(position);
    }
}
