package com.instamaven.app.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.instamaven.app.R;
import com.instamaven.app.models.BadgeEntity;
import com.instamaven.app.models.PlaceEntity;
import com.instamaven.app.utils.Plural;
import com.instamaven.app.utils.ProfileHelper;
import com.instamaven.app.utils.RequestClient;
import com.instamaven.app.utils.SettingsHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class IMApp extends Application {

    public static final String REQUEST_SESSION = "request_session";
    public static final String REQUEST_VIDEO = "request_video";
    public static final String REJECT_VIDEO = "reject_video";
    public static final String CHECK_SESSION = "check_session";
    public static final String FINISH_VIDEO = "finish_video";

    public static final int RC_VIDEO_APP_PERM = 124;

    public static JSONObject myProfile;
    public static int myUserId = 0;
    public static BadgeEntity currentBadge;
    public static PlaceEntity placeEntity;
    public static String myFCMtoken = "";
    public static String paymentToken = "";
    public static Integer currentLevel = 0;
    public static Integer currentBadgeId = 0;
    public static final String FCM_BADGES_CHANNEL = "fcm_badges_channel";
    public static final String FCM_MESSAGES_CHANNEL = "fcm_messages_channel";
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static Switch switchStatus;
    public static JSONObject result;
    public static boolean high_density = false;

    // request to call_anim
    public static Boolean isConnection = false;

    public static Integer user_id, visibility = 1, location = 1;
    public static String current_mode, lat = "0.0", lng = "0.0";
    public static String distance = "km";

    /**
     * @param view         View to animate
     * @param toVisibility Visibility at the end of animation
     * @param toAlpha      Alpha at the end of animation
     * @param duration     Animation duration in ms
     */
    public static void animateView(final View view, final int toVisibility, float toAlpha, int duration) {
        boolean show = toVisibility == View.VISIBLE;
        if (show) {
            view.setAlpha(0);
        }
        view.setVisibility(View.VISIBLE);
        view.animate()
                .setDuration(duration)
                .alpha(show ? toAlpha : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(toVisibility);
                    }
                });
    }

    /**
     * Persist token to third-party servers.
     * <p>
     * Modify this method to associate the user's FCM InstanceID token with any server-side account_bg
     * maintained by your application.
     *
     * @param refreshedToken The new token.
     */
    public static void sendRegistrationToServer(Context context, String refreshedToken) throws Exception {
        JSONObject obj;
        String token = ProfileHelper.getToken(context);
        if (!token.isEmpty()) {
            myFCMtoken = refreshedToken;
            // save to server only if we are already authorized
            String response = new RequestClient.Builder(context)
                    .setUrl(SettingsHelper.getUrl(context, "post_fcm"))
                    .setMethod("POST")
                    .addField("connection", refreshedToken)
                    .addField("client", "android")
                    .send();
            obj = new JSONObject(response);
            if (obj.opt("data") != null) {
                // save updated profile
                ProfileHelper.saveUser(context, (JSONObject) obj.get("data"));
            }
        }
    }

    public static Integer getBalanceValue(Context context, @NonNull JSONObject profile) throws JSONException {
        JSONObject customer = (JSONObject) profile.get("customer");
        if (customer != null) {
            if (customer.get("balance") != JSONObject.NULL) {
                return customer.getInt("balance");
            }
        }
        return 0;
    }

    public static String getBalance(Context context) {
        try {
            JSONObject profile = ProfileHelper.getUser(context);
            Integer balance = getBalanceValue(context, profile);
            return String.format(Locale.getDefault(), "%s %d %s", context.getString(R.string.balance), balance, Plural.word(balance, context.getResources().getStringArray(R.array.coins)));
        } catch (JSONException e) {
            return "";
        }
    }

    public static void showBalance(Context context, ConstraintLayout layout) {
        try {
            JSONObject profile = ProfileHelper.getUser(context);
            Integer balance = getBalanceValue(context, profile);
            layout.setBackgroundColor(Color.parseColor(balance > 0 ? "#86c351" : "#ffc700"));
            TextView balanceTV = layout.findViewById(R.id.userBalance);
            balanceTV.setText(getBalance(context));
        } catch (JSONException e) {
        }
    }

    public static Integer getVisibilityValue(Context context, @NonNull JSONObject profile) throws JSONException {
        JSONObject user = (JSONObject) profile.get("user");
        if (user != null) {
            if (user.get("visibility") != JSONObject.NULL) {
                return user.getInt("visibility");
            }
        }
        return 0;
    }

    public static String getVisibility(Context context) {
        return String.format(Locale.getDefault(), "%s", context.getString(IMApp.visibility == 1 ? R.string.status_visible : R.string.status_invisible)
        );
    }
}