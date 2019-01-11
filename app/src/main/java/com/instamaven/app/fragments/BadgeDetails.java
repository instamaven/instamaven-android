package com.instamaven.app.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.instamaven.app.activities.MainActivity;
import com.instamaven.app.activities.MapsActivity;
import com.instamaven.app.activities.IMApp;
import com.instamaven.app.R;
import com.instamaven.app.models.BadgeEntity;
import com.instamaven.app.utils.RequestClient;
import com.instamaven.app.utils.SettingsHelper;
import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONObject;
import org.jsoup.Jsoup;

public class BadgeDetails extends Fragment {

    RequestClient.Builder client;
    int badgeId = 0;
    JSONObject badge;
    MainActivity menuActivity;
    String latitude, longitude;

    public static BadgeDetails newInstance(int badgeId) {
        BadgeDetails f = new BadgeDetails();
        f.badgeId = badgeId;

        return f;
    }

    public BadgeDetails() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_badge_details, container, false);

        menuActivity = ((MainActivity) getActivity());
        menuActivity.videoCallMenuVisibility = true;
        menuActivity.addBadgeMenuVisibility = false;
        menuActivity.searchMenuVisibility = false;
        menuActivity.invalidateOptionsMenu();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        client = new RequestClient.Builder(getActivity())
                .addHeader("Accept", "application/json");

        JSONObject obj = null;
        try {
            String response = client.setUrl(SettingsHelper.getUrl(getActivity(), "get_badge_details", badgeId))
                    .setMethod("GET").send();

            obj = new JSONObject(response);
            if (obj.opt("error") != null) {
                // error found
            } else {
                badge = (JSONObject) obj.get("data");

                View view = getView();

                SimpleDraweeView draweeView = view.findViewById(R.id.imageBadge);
                String imgSrc = (String) badge.opt("image");
                if (!imgSrc.equals("")) {
                    Uri uri = Uri.parse(imgSrc);
                    draweeView.setImageURI(uri);
                }

                TextView titleText = view.findViewById(R.id.editTitleText);
                titleText.setText(Jsoup.parse(badge.getString("title")).text());

                TextView descText = view.findViewById(R.id.editDescriptionText);
                if (badge.opt("description") != JSONObject.NULL) {
                    descText.setText(Jsoup.parse(badge.getString("description")).text());
                }

                TextView priceText = view.findViewById(R.id.priceTextView);
                String price = "$" + badge.opt("price");
                String price_type = "";
                if (badge.opt("price_type") != JSONObject.NULL) {
                    if ("fixed".equals(badge.getString("price_type"))) {
                        price_type = getString(R.string.radio_button_fixed);
                    } else {
                        price_type = getString(R.string.radio_button_per_minutes);
                    }
                }
                priceText.setText(String.format("%s (%s)", price, price_type));


                TextView addressText = view.findViewById(R.id.addressTextView);
                if (badge.opt("address") != JSONObject.NULL) {
                    addressText.setText(Jsoup.parse(badge.getString("address")).text());
                }

                TextView urlText = view.findViewById(R.id.urlTextView);
                if (badge.opt("url") != JSONObject.NULL) {
                    urlText.setText(badge.getString("url"));
                }

                latitude = badge.optString("latitude", "");
                longitude = badge.optString("longitude", "");
                ConstraintLayout llMap = view.findViewById(R.id.mapBtnLayout);
                if (latitude.equals("null") ||
                        longitude.equals("null") ||
                        Double.parseDouble(latitude) == 0 ||
                        Double.parseDouble(longitude) == 0) {
                    llMap.setVisibility(View.GONE);
                } else {
                    llMap.setVisibility(View.VISIBLE);
                    Button mapBtn = view.findViewById(R.id.mapBtn);
                    mapBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), MapsActivity.class);
                            intent.putExtra("latitude", latitude);
                            intent.putExtra("longitude", longitude);
                            intent.putExtra("mode", MapsActivity.VIEW_MODE);
                            startActivity(intent);
                        }
                    });
                }

                RatingBar rateBar = view.findViewById(R.id.ratingBarBadge);
                Double rate = badge.getDouble("rate");
                rateBar.setRating(rate.floatValue());

                TextView ratesText = view.findViewById(R.id.numOfRates);
                ratesText.setText(badge.get("rates").toString());

                TextView commentsText = view.findViewById(R.id.numOfComments);
                commentsText.setText(badge.get("comments").toString());

                // Comments Button
                Button comments = view.findViewById(R.id.commentsBtn);
                comments.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((MainActivity) getActivity()).setActionBarTitle(R.string.comments);
                        CommentsList commentsList = CommentsList.newInstance(badgeId);
                        getFragmentManager().beginTransaction()
                            .replace(R.id.flMenu, commentsList)
                            .addToBackStack(null)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                            .commit();
                    }
                });

                // Action Favorite Button
                Button favoriteBtn = view.findViewById(R.id.favoriteBtn);
                if (IMApp.myUserId == badge.getInt("user_id")) {
                    favoriteBtn.setVisibility(View.GONE);
                } else {
                    favoriteBtn.setVisibility(View.VISIBLE);

                    final String btnTag = String.format(getString(R.string.btn_tag), badge.getInt("id"));
                    favoriteBtn.setTag(btnTag);
                    setFavoriteIcon(view, btnTag, badge.getInt("favorite"));
                    favoriteBtn.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            try {
                                Integer favorite = badge.getInt("favorite") ^ 1;
                                client.setUrl(SettingsHelper.getUrl(getActivity(), "post_favorites", badge.getInt("id")))
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
    }

    private void setFavoriteIcon(View v, String tag, int favorite) {
        View view = v.findViewWithTag(tag);
        if (view != null) {
            int iconRes = favorite == 0 ? R.drawable.ic_heart_border : R.drawable.ic_heart;
            view.setBackground(ContextCompat.getDrawable(getActivity(), iconRes));
        }
    }
}
