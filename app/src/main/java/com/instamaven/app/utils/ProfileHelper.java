package com.instamaven.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.instamaven.app.activities.IMApp;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

public class ProfileHelper {

    public static String getToken(Context context) {
        JSONObject userObj = getUser(context);
        if (userObj!=null) {
            try {
                JSONObject user = (JSONObject) userObj.opt("user");
                return user != null ? user.getString("api_token") : "";
            } catch (JSONException e) {
                return "";
            }
        }

        return "";
    }

    public static void resetToken(FragmentActivity context) {
        SharedPreferences userDetails = context.getSharedPreferences("profile", MODE_PRIVATE);
        SharedPreferences.Editor edit = userDetails.edit();
        edit.clear();
        edit.putString("token", "").commit();
    }

    public static void saveUser(Context context, JSONObject userObj) {
        SharedPreferences userDetails = context.getSharedPreferences("profile", MODE_PRIVATE);
        SharedPreferences.Editor edit = userDetails.edit();
        edit.putString("user", userObj.toString()).commit();
    }

    public static JSONObject getUser(Context context) {
        SharedPreferences userDetails = context.getSharedPreferences("profile", MODE_PRIVATE);
        String user = userDetails.getString("user", "");
        try {
            return new JSONObject(user);
        } catch (JSONException e) {
            return null;
        }
    }

    public static String getToken(FragmentActivity context) {
        return getToken((Context)context);
    }

    public static void saveUser(FragmentActivity context, JSONObject userObj) {
        saveUser((Context)context, userObj);
    }

    public static JSONObject getUser(FragmentActivity context) {
        return getUser((Context) context);
    }

    public static void login(Context context, String email, String password) throws Exception {
        String response = new RequestClient.Builder(context)
                .addHeader("Accept", "application/json")
                .setAuth(false)
                .setUrl(SettingsHelper.getUrl(context, "post_user_login"))
                .setMethod("POST")
                .addField("email", email)
                .addField("password", password)
                .addField("connection", IMApp.myFCMtoken)
                .send();
        saveProfile(context, response);
    }

    // Two steps auth providers
    public static void loginUser(Context context, String token, String provider) throws Exception {
        String authEndPoint = "";
        switch (provider) {
            case "facebook":
                authEndPoint = SettingsHelper.getUrl(context, "post_login_fb");
                break;
            case "google":
                authEndPoint = SettingsHelper.getUrl(context, "post_login_google");
                break;
        }
        if (!authEndPoint.isEmpty()) {
            String response = new RequestClient.Builder(context)
                    .addHeader("Accept", "application/json")
                    .setAuth(false)
                    .setUrl(authEndPoint)
                    .setMethod("POST")
                    .addField("token", token)
                    .addField("connection", IMApp.myFCMtoken)

                    .send();
            saveProfile(context, response);
        }
    }

    // One step auth providers
    public static void loginUser(Context context, String token, String secret, String provider) throws Exception {
        String authEndPoint = "";
        switch (provider) {
            case "twitter":
                authEndPoint = SettingsHelper.getUrl(context, "post_login_tw");
                break;
        }
        if (!authEndPoint.isEmpty()) {
            String response = new RequestClient.Builder(context)
                    .addHeader("Accept", "application/json")
                    .setAuth(false)
                    .setUrl(authEndPoint)
                    .setMethod("POST")
                    .addField("token", token)
                    .addField("secret", secret)
                    .addField("connection", IMApp.myFCMtoken)
                    .send();
            saveProfile(context, response);
        }
    }

    // Save Profile
    public static void saveProfile(Context context, String response) throws Exception {
        JSONObject obj = new JSONObject(response);
        if (obj.opt("error") != null) {
            String message = obj.optString("message");
            if (message.isEmpty()) {
                message = "Error: " + obj.opt("status");
            }
            throw new Exception(message);
        } else {
            saveUser(context, (JSONObject) obj.get("data"));
        }
    }

    // Get Profile
    public static boolean getProfile(Context context) {
        JSONObject obj = null;
        try {
            String response = new RequestClient.Builder(context)
                    .addHeader("Accept", "application/json")
                    .setUrl(SettingsHelper.getUrl(context, "get_profile"))
                    .setMethod("GET")
                    .send();
            obj = new JSONObject(response);
            if (obj.opt("data") != null) {
                saveUser(context, (JSONObject) obj.get("data"));
                return true;
            }
        } catch (Exception e) {
            String message = obj.optString("message");
            if (message.isEmpty()) {
                message = "Error: " + obj.opt("status");
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
        return false;
    }

    public static void setVisibility(Context context, Integer visibility) {
        JSONObject obj = null;
        try {
            String response = new RequestClient.Builder(context)
                    .addHeader("Accept", "application/json")
                    .setUrl(SettingsHelper.getUrl(context, "patch_user_status"))
                    .setMethod("PATCH")
                    .addField("visibility", visibility.toString())
                    .send();
            obj = new JSONObject(response);
        } catch (Exception e) {
            String message = obj.optString("message");
            if (message.isEmpty()) {
                message = "Error: " + obj.opt("status");
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }
}
