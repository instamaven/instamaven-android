package com.instamaven.app.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.instamaven.app.R;
import com.instamaven.app.models.BadgeEntity;
import com.instamaven.app.utils.Plural;
import com.instamaven.app.utils.RequestClient;
import com.instamaven.app.utils.SettingsHelper;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.view.View.GONE;

public class BadgeDetailsActivity extends AppCompatActivity {

    protected RequestClient.Builder client;
    protected int badgeId = 0;
    protected JSONObject badge;
    protected String latitude, longitude, btnTag, price, imgSrc, response;
    protected TextView dateTV, priceTV, titleText, descText, addressText, ratesText, commentsText, urlText, badgeTypeTV;
    protected ImageButton comments;
    protected Button favoriteBtn;
    protected Intent shareIntent;
    protected LinearLayout addressLinearLayout, urlLinearLayout;
    protected ConstraintLayout balanceLinearLayout, statusConstraintLayout;

    public boolean searchMenuVisibility = false;
    public boolean videoCallMenuVisibility = true;
    public boolean shareMenuVisible = true;

    final private static String BADGE_DATE_FORMAT = "d MMM yyyy";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badge_details);

        balanceLinearLayout = findViewById(R.id.balanceConstrainLayout);
        IMApp.showBalance(this, balanceLinearLayout);

        badgeTypeTV = findViewById(R.id.badgeTypeTV);
        badgeTypeTV.setText(getString(IMApp.current_mode.equals("online") ? R.string.online_badges : R.string.ondemand_badges));

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // badgeId can be set in FirebaseMessaging.sendNotification()
            badgeId = extras.getInt("badgeId", 0);
            if (badgeId == 0) {
                String data = getIntent().getDataString();
                if (data != null && data.length() > 0) {
                    String search = getString(R.string.api_url).concat("/badges/");
                    int index = data.indexOf(search);
                    if (index != -1) {
                        badgeId = Integer.parseInt(data.substring(index + search.length()));
                        // rewrite global badge ID
                        IMApp.currentBadgeId = badgeId;
                    }
                }
            }
        }
        if (badgeId == 0) {
            badgeId = IMApp.currentBadgeId;
        }

        client = new RequestClient.Builder(this)
                .addHeader("Accept", "application/json");

        JSONObject obj = null;
        try {
            response = client.setUrl(SettingsHelper.getUrl(this, "get_badge_details", badgeId))
                    .setMethod("GET").send();

            obj = new JSONObject(response);
            if (obj.opt("error") != null) {
                // error found
            } else {
                badge = (JSONObject) obj.get("data");

                SimpleDraweeView draweeView = findViewById(R.id.imageBadge);
                String imgSrc = badge.optString("image", "");
                if (!imgSrc.equals("")) {
                    //Uri uri = Uri.parse(SettingsHelper.getStorageUrl(this) + imgSrc);
                    Uri uri = Uri.parse(imgSrc);
                    draweeView.setImageURI(uri);
                }

                dateTV = findViewById(R.id.dateBadge);
                long timestamp = badge.getLong("timestamp");
                DateFormat badgeDateFormat = new SimpleDateFormat(BADGE_DATE_FORMAT, Locale.getDefault());
                dateTV.setText(badgeDateFormat.format(new Date(timestamp * 1000)));

                priceTV = findViewById(R.id.priceBadgeTV);
                price = badge.opt("price") + " " + getString(R.string.coins);
                if (badge.opt("price_type").equals("per_minute")) {
                    price += " " + getString(R.string.per_minute);
                }
                priceTV.setText(String.format("%s %s", getString(R.string.edit_price), price));

                titleText = findViewById(R.id.editTitleText);
                titleText.setText(Jsoup.parse(badge.getString("title")).text());

                descText = findViewById(R.id.editDescriptionText);
                if (badge.opt("description") != JSONObject.NULL) {
                    descText.setText(Jsoup.parse(badge.getString("description")).text());
                }

                addressLinearLayout = findViewById(R.id.addressLinearLayout);
                addressText = findViewById(R.id.addressTextView);
                if (badge.opt("address") != JSONObject.NULL) {
                    addressText.setText(Jsoup.parse(badge.getString("address")).text());
                    addressLinearLayout.setVisibility(View.VISIBLE);
                }

                urlLinearLayout = findViewById(R.id.urlLinearLayout);
                urlText = findViewById(R.id.urlTextView);
                if (badge.opt("url") != JSONObject.NULL) {
                    urlText.setText(badge.getString("url"));
                    urlLinearLayout.setVisibility(View.VISIBLE);
                }

                latitude = badge.optString("latitude", "");
                longitude = badge.optString("longitude", "");

                ConstraintLayout llMap = findViewById(R.id.mapBtnLayout);
                if (latitude.equals("null") ||
                        longitude.equals("null") ||
                        Double.parseDouble(latitude) == 0 ||
                        Double.parseDouble(longitude) == 0) {
                    llMap.setVisibility(GONE);
                } else {
                    llMap.setVisibility(View.VISIBLE);
                    Button mapBtn = findViewById(R.id.mapBtn);
                    mapBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(getApplicationContext(), MapsActivity.class);
                            i.putExtra("latitude", latitude);
                            i.putExtra("longitude", longitude);
                            i.putExtra("mode", MapsActivity.VIEW_MODE);
                            startActivity(i);
                        }
                    });
                }

                RatingBar rateBar = findViewById(R.id.ratingBarBadge);
                Double rate = badge.getDouble("rate");
                rateBar.setRating(rate.floatValue());

                ratesText = findViewById(R.id.numOfRates);
                ratesText.setText(badge.get("rates").toString());

                commentsText = findViewById(R.id.numOfComments);
                commentsText.setText(badge.get("comments").toString());

                // Comments Button
                comments = findViewById(R.id.commentsBtn);
                comments.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale);
                        comments.startAnimation(anim);
                        Intent intent = new Intent(BadgeDetailsActivity.this, CommentsListActivity.class);
                        intent.putExtra("badgeId", badgeId);
                        startActivity(intent);
                    }
                });

                // Action Favorite Button
                favoriteBtn = findViewById(R.id.favoriteBtn);
                if (IMApp.myUserId == badge.getInt("user_id")) {
                    favoriteBtn.setVisibility(GONE);
                } else {
                    favoriteBtn.setVisibility(View.VISIBLE);

                    btnTag = String.format(getString(R.string.btn_tag), badge.getInt("id"));
                    favoriteBtn.setTag(btnTag);
                    setFavoriteIcon(getWindow().getDecorView().getRootView(), btnTag, badge.getInt("favorite"));
                    favoriteBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale);
                            favoriteBtn.startAnimation(anim);
                            try {
                                Integer favorite = badge.getInt("favorite") ^ 1;
                                client.setUrl(SettingsHelper.getUrl(getApplicationContext(), "post_favorites", badge.getInt("id")))
                                        .setMethod(favorite == 0 ? "DELETE" : "POST")
                                        .send();
                                badge.put("favorite", favorite);
                                setFavoriteIcon(v, btnTag, favorite);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                IMApp.user_id = badge.getInt("user_id");
                IMApp.currentBadge = new BadgeEntity(badge);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Status Layout
        statusConstraintLayout = findViewById(R.id.statusConstraintLayout);
        statusConstraintLayout.setVisibility(IMApp.visibility > 0 ? View.GONE : View.VISIBLE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setVisible(false);
        MenuItem shareItem = menu.findItem(R.id.action_share);
        shareItem.setVisible(true);
        MenuItem filtersItem = menu.findItem(R.id.action_search_advanced);
        filtersItem.setVisible(false);

        MenuItem videoCallItem = menu.findItem(R.id.action_videoCall);
        if (videoCallMenuVisibility && IMApp.myUserId != IMApp.user_id) {
            videoCallItem.setVisible(true);
            videoCallItem.setEnabled(true);
        } else {
            videoCallItem.setVisible(false);
            videoCallItem.setEnabled(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_videoCall:
                try {
                    JSONObject customer = (JSONObject) IMApp.myProfile.get("customer");
                    JSONObject settings = (JSONObject) IMApp.myProfile.get("settings");
                    int balance = customer.getInt("balance");
                    int min_to_call = settings.getInt("min_to_call");
                    if (balance <= min_to_call) {
                        new AlertDialog.Builder(BadgeDetailsActivity.this)
                                .setTitle(getString(R.string.recharge_title))
                                .setMessage(
                                        String.format(
                                                Locale.getDefault(),
                                                getString(R.string.recharge_message),
                                                balance,
                                                Plural.word(balance, getResources().getStringArray(R.array.coins))
                                        )
                                )
                                .setCancelable(false)
                                .setPositiveButton(R.string.recharge, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(BadgeDetailsActivity.this, ProfileActivity.class);
                                        intent.putExtra("autoClick", "rechargeBtn");
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                })
                                .show();

                    } else if (IMApp.visibility == 0) {
                        Toast.makeText(this, R.string.enabled_offline_mode, Toast.LENGTH_LONG).show();
                    } else if (!IMApp.isConnection) {
                        sendRequest(IMApp.REQUEST_SESSION);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.action_share:
                shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                try {
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, badge.getString("title"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.api_url) + "/badges/" + String.valueOf(badgeId));
                startActivity(Intent.createChooser(shareIntent, "Share it"));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    public void sendRequest(String type) {
        JSONObject data, fcm;
        Intent intent;
        try {
            switch (type) {
                case IMApp.REQUEST_SESSION:
                    // start caller session
                    String response = client.setUrl(SettingsHelper.getUrl(this, "signal_request_session"))
                            .setMethod("POST")
                            .addField("badge_id", IMApp.currentBadge.getInt("id").toString())
                            .addField("id", IMApp.user_id.toString())
                            .send();
                    JSONObject obj = new JSONObject(response);
                    if (obj.opt("error") != null) {
                        String message = obj.optString("message");
                        if (message.isEmpty()) {
                            message = "Error: " + obj.opt("status");
                        }
                        throw new Exception(message);
                    } else {
                        data = (JSONObject) obj.get("data");
                        fcm = (JSONObject) IMApp.myProfile.get("fcm");
                        intent = new Intent(this, VideoChatActivity.class);
                        // OpenTok session ID and token
                        intent.putExtra("session", data.getString("session"));
                        intent.putExtra("token", data.getString("token"));
                        // Caller FCM ID
                        intent.putExtra("caller", fcm.getString("connection_id"));
                        // Callee User ID - this param is presented only for caller
                        intent.putExtra("callee", IMApp.user_id.toString());
                        // selected badge ID
                        intent.putExtra("badge_id", IMApp.currentBadge.getInt("id"));
                        startActivity(intent);
                    }
                    break;
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setFavoriteIcon(View v, String tag, int favorite) {
        View view = v.findViewWithTag(tag);
        if (view != null) {
            int iconRes = favorite == 0 ? R.drawable.ic_heart_border : R.drawable.ic_heart;
            view.setBackground(ContextCompat.getDrawable(this, iconRes));
        }
    }
}
