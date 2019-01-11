package com.instamaven.app.utils;

import android.content.Context;

public class SettingsHelper {

    public static String getString(Context context, String name) {
        int resource = context.getResources().getIdentifier(name, "string", context.getPackageName());
        return context.getString(resource);
    }

    public static String getUrl(Context context, String name) {
        return getString(context, "api_url") + getString(context, name);
    }

    public static String getUrl(Context context, String name, int id) {
        String formatString = getString(context, "api_url") + getString(context, name);
        return String.format(formatString, id);
    }

    public static String getUrl(Context context, String name, String s1) {
        String formatString = getString(context, "api_url") + getString(context, name);
        return String.format(formatString, s1);
    }

//    public static String getStorageUrl(Context context) {
//        return getString(context, "api_url") + "/storage/";
//    }
}
