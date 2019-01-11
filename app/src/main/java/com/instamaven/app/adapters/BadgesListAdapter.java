package com.instamaven.app.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.instamaven.app.activities.EditBadgeActivity;
import com.instamaven.app.activities.IMApp;
import com.instamaven.app.R;
import com.instamaven.app.activities.ProfileActivity;
import com.instamaven.app.models.Entity;
import com.instamaven.app.utils.Plural;
import com.instamaven.app.utils.ProfileHelper;
import com.instamaven.app.utils.RequestClient;
import com.instamaven.app.utils.SettingsHelper;

import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.Locale;

public class BadgesListAdapter extends ListAdapter {

    public static final int BADGES_MODE = 0;
    public static final int FAVORITES_MODE = 1;
    public static final int SEARCH_MODE = 2;
    public static final int SEARCH_ADVANCED_MODE = 3;
    public static final int MY_BADGES_MODE = 4;

    private final static int SEND_ACTIVATE = 1;
    private final static int SEND_DEACTIVATE = 2;

    private int badgesListMode;
    protected int currentPosition = 0;
    protected Integer activeState = 0, activeField, favoriteField;
    protected String btnTag, priceType, price_type;
    protected View iconView;
    private Handler mHandler;
    protected RequestClient.Builder client;
    protected TextView amountTextView, txtTitle, txtDescription, priceTextView, txtDistance;
    private Button editBtn, activationBadgeBtn, favoriteBtn;

    public BadgesListAdapter(Context context, ArrayList<Entity> badges, int badgesListMode) {
        super(context, R.layout.badges_list_item, badges);

        this.badgesListMode = badgesListMode;
        mHandler = new MessageHandler();
    }

    private void setActive(int position, int activity) {
        Entity badge = this.items.get(position);
        badge.put("active", activity);
        this.items.set(position, badge);
    }

    private void setActiveIcon(View v, String tag, int activity) {
        int iconRes = activity == 0 ? R.drawable.ic_activation_disable : R.drawable.ic_activation_enable;
        View view = v.findViewWithTag(tag);
        if (view != null) {
            view.setBackground(ContextCompat.getDrawable(getContext(), iconRes));
        } else if (v instanceof Button) {
            v.setBackground(ContextCompat.getDrawable(getContext(), iconRes));
        }
    }

    private void setFavorite(int position, int favorite) {
        Entity badge = this.items.get(position);
        badge.put("favorite", favorite);
        this.items.set(position, badge);
    }


    private void setFavoriteIcon(View v, String tag, int favorite) {
        int iconRes = favorite == 0 ? R.drawable.ic_heart_border : R.drawable.ic_heart;
        View view = v.findViewWithTag(tag);
        if (view != null) {
            view.setBackground(ContextCompat.getDrawable(getContext(), iconRes));
        } else if (v instanceof Button) {
            v.setBackground(ContextCompat.getDrawable(getContext(), iconRes));
        }
    }

    public View getView(final int position, View view, final ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        final View rowView = inflater.inflate(R.layout.badges_list_item, null, true);

        SimpleDraweeView draweeView = rowView.findViewById(R.id.image_view);
        String imgSrc = items.get(position).getString("image");
        if (!imgSrc.equals("")) {
            Uri uri = Uri.parse(imgSrc);
            draweeView.setImageURI(uri);
        }

        // Title TextView
        txtTitle = rowView.findViewById(R.id.title);
        txtTitle.setText(Jsoup.parse(items.get(position).getString("title")).text());

        // Description TextView
        txtDescription = rowView.findViewById(R.id.description);
        txtDescription.setText(Jsoup.parse(items.get(position).getString("description")).text());

        // RatingBar
        RatingBar ratingBar = rowView.findViewById(R.id.ratingBar);
        Double rate = items.get(position).getDouble("rate");
        ratingBar.setRating(rate.floatValue());

        // Amount TextView
        amountTextView = rowView.findViewById(R.id.amountTextView);
        final Integer amount = items.get(position).getInt("amount");
        amountTextView.setText(String.format(Locale.getDefault(), "%s %d %s", getContext().getString(R.string.activation), amount, Plural.word(amount, getContext().getResources().getStringArray(R.array.coins))));

        // Price TextView
        priceTextView = rowView.findViewById(R.id.priceTextView);
        priceType = items.get(position).getString("price_type");
        price_type = "";
        if (priceType != JSONObject.NULL) {
            if ("per_minute".equals(items.get(position).getString("price_type"))) {
                price_type = getContext().getString(R.string.radio_button_per_minutes);
            }
        }
        int price = items.get(position).getInt("price");
        priceTextView.setText(String.format(Locale.getDefault(), "%s %d %s %s", getContext().getString(R.string.edit_price), price, Plural.word(price, getContext().getResources().getStringArray(R.array.coins)), price_type));

        // Distance TextView(Enabled When Advanced Search)
        txtDistance = rowView.findViewById(R.id.distanceTV);

        activeField = items.get(position).getInt("active");
        favoriteField = items.get(position).getInt("favorite");

        // Buttons Edit/Activation/Favorites Badge
        editBtn = rowView.findViewById(R.id.editBadgeBtn);
        activationBadgeBtn = rowView.findViewById(R.id.activeBadgeBtn);
        favoriteBtn = rowView.findViewById(R.id.favoriteBtn);

        // activate/favorite buttons tag is btn_%d, where %d is badge ID
        btnTag = String.format(Locale.getDefault(), "btn_%d", items.get(position).getInt("id"));
        client = new RequestClient.Builder(context)
                .addHeader("Accept", "application/json");

        // BADGES_MODE
        if (badgesListMode == BADGES_MODE) {
            txtDistance.setVisibility(View.GONE);
            editBtn.setVisibility(View.GONE);
            activationBadgeBtn.setVisibility(View.GONE);
            amountTextView.setVisibility(View.GONE);
            if (IMApp.myUserId != items.get(position).getInt("user_id")) {
                favoriteBtn.setVisibility(View.VISIBLE);
                favoriteBtn.setTag(btnTag);
                setFavoriteIcon(rowView, btnTag, favoriteField);
                favoriteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.scale);
//                        favoriteBtn.startAnimation(anim);
                        Integer favorite = items.get(position).getInt("favorite") ^ 1;
                        try {
                            client.setUrl(SettingsHelper.getUrl(context, "post_favorites", items.get(position).getInt("id")))
                                    .setMethod(favorite == 0 ? "DELETE" : "POST")
                                    .send();
                            setFavorite(position, favorite);
                            setFavoriteIcon(v, "", favorite);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                // do not show Add To Favorites button in our own items
                favoriteBtn.setVisibility(View.GONE);
            }

            // MY_BADGES_MODE
        } else if (badgesListMode == MY_BADGES_MODE) {
            editBtn.setVisibility(View.VISIBLE);
            activationBadgeBtn.setVisibility(View.VISIBLE);
            priceTextView.setVisibility(View.VISIBLE);
            if (amount > 0) {
                amountTextView.setVisibility(View.VISIBLE);
            } else {
                amountTextView.setVisibility(View.GONE);
            }
            favoriteBtn.setVisibility(View.GONE);
            txtDistance.setVisibility(View.GONE);
            editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.scale);
                    editBtn.startAnimation(anim);

                    Intent intent = new Intent(context, EditBadgeActivity.class);
                    intent.putExtra("badge_id", items.get(position).getInt("id"));
                    context.startActivity(intent);
                    Toast.makeText(getContext(), R.string.edit_mode, Toast.LENGTH_SHORT).show();
                }
            });

            // Activate/Deactivate Badge Button
            activationBadgeBtn.setTag(btnTag);
            setActiveIcon(rowView, btnTag, activeField);
            activationBadgeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.scale);
                    activationBadgeBtn.startAnimation(anim);
                    activeState = items.get(position).getInt("active") ^ 1;
                    currentPosition = position;
                    iconView = v;
                    try {

                        if (activeState >= 1) {
                            new AlertDialog.Builder(context)
                                    .setCancelable(false)
                                    .setTitle(context.getString(R.string.activation_badge_title))
                                    .setMessage(
                                            String.format(
                                                    Locale.getDefault(),
                                                    context.getString(R.string.activation_badge_message),
                                                    amount,
                                                    Plural.word(amount, getContext().getResources().getStringArray(R.array.coins))
                                            )
                                    )
                                    .setPositiveButton(context.getString(R.string.ok_btn), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            Thread thread = new ThreadSetActive();
                                            thread.start();
                                        }
                                    })
                                    .setNegativeButton(context.getString(R.string.cancel_btn), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .show();
                        } else {
                            new AlertDialog.Builder(context)
                                    .setCancelable(false)
                                    .setTitle(context.getString(R.string.deactivation_badge_title))
                                    .setMessage(context.getString(R.string.deactivation_badge_massages))
                                    .setPositiveButton(context.getString(R.string.ok_btn), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            Thread thread = new ThreadSetInactive();
                                            thread.start();
                                        }
                                    })
                                    .setNegativeButton(context.getString(R.string.cancel_btn), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            // FAVORITES_MODE
        } else if (badgesListMode == FAVORITES_MODE) {
            editBtn.setVisibility(View.GONE);
            activationBadgeBtn.setVisibility(View.GONE);
            amountTextView.setVisibility(View.GONE);
            txtDistance.setVisibility(View.GONE);

            if (IMApp.myUserId != items.get(position).getInt("user_id")) {
                favoriteBtn.setVisibility(View.VISIBLE);

                favoriteBtn.setTag(btnTag);
                setFavoriteIcon(rowView, btnTag, favoriteField);
                favoriteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.scale);
//                        favoriteBtn.startAnimation(anim);
                        Integer favorite = items.get(position).getInt("favorite") ^ 1;
                        try {
                            client.setUrl(SettingsHelper.getUrl(context, "post_favorites", items.get(position).getInt("id")))
                                    .setMethod(favorite == 0 ? "DELETE" : "POST")
                                    .send();
                            setFavorite(position, favorite);
                            setFavoriteIcon(v, "", favorite);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                // do not show Add To Favorites button in our own items
                favoriteBtn.setVisibility(View.GONE);
            }
            // SEARCH_MODE
        } else if (badgesListMode == SEARCH_MODE) {
            txtDistance.setVisibility(View.GONE);
            editBtn.setVisibility(View.GONE);
            activationBadgeBtn.setVisibility(View.GONE);
            amountTextView.setVisibility(View.GONE);
            txtDistance.setVisibility(View.GONE);
            if (IMApp.myUserId != items.get(position).getInt("user_id")) {
                favoriteBtn.setVisibility(View.VISIBLE);
                favoriteBtn.setTag(btnTag);
                setFavoriteIcon(rowView, btnTag, favoriteField);
                favoriteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.scale);
//                        favoriteBtn.startAnimation(anim);
                        Integer favorite = items.get(position).getInt("favorite") ^ 1;
                        try {
                            client.setUrl(SettingsHelper.getUrl(context, "post_favorites", items.get(position).getInt("id")))
                                    .setMethod(favorite == 0 ? "DELETE" : "POST")
                                    .send();
                            setFavorite(position, favorite);
                            setFavoriteIcon(v, "", favorite);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                // do not show Add To Favorites button in our own items
                favoriteBtn.setVisibility(View.GONE);
            }
        }

        return rowView;
    }

    protected class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            final Context context = getContext();
            try {
                String response = client.setUrl(SettingsHelper.getUrl(context, "patch_badge_activate", items.get(currentPosition).getInt("id")))
                        .setMethod("PATCH")
                        .addField("active", activeState.toString())
                        .send();
                JSONObject obj = new JSONObject(response);
                if (obj.opt("error") != null) {
                    String message = obj.optString("message");
                    if (message.isEmpty()) {
                        message = "Error: " + obj.opt("status");
                    }
                    throw new Exception(message);
                } else {
                    setActive(currentPosition, activeState);
                    setActiveIcon(iconView, btnTag, activeState);
                    switch (msg.what) {
                        case SEND_ACTIVATE:
                            amountTextView.setVisibility(View.GONE);
                            Toast.makeText(context, R.string.activation_alert_complete, Toast.LENGTH_SHORT).show();
                            // get updated profile
                            ProfileHelper.getProfile(context);
                            //  TODO: refresh the view
                            break;
                        case SEND_DEACTIVATE:
                            amountTextView.setVisibility(View.VISIBLE);
                            Toast.makeText(context, R.string.deactivation_alarm_complete, Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            } catch (Exception e) {
                //Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                new AlertDialog.Builder(context)
                        .setMessage(e.getMessage())
                        //.setIcon(R.drawable.ic_complete_round)
                        .setCancelable(false)
                        .setPositiveButton(R.string.recharge, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(context, ProfileActivity.class);
                                intent.putExtra("autoClick", "rechargeBtn");
                                context.startActivity(intent);
                            }
                        })
                        .setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();
            }
        }
    }

    public class ThreadSetActive extends Thread {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(SEND_ACTIVATE);
        }
    }

    public class ThreadSetInactive extends Thread {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(SEND_DEACTIVATE);
        }
    }

}