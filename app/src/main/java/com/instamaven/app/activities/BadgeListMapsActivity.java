package com.instamaven.app.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.instamaven.app.R;
import com.instamaven.app.adapters.BadgesListAdapter;
import com.instamaven.app.models.Entity;
import com.instamaven.app.providers.BadgesProvider;

import java.util.ArrayList;
import java.util.Locale;

public class BadgeListMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public String query = "", lat = "", lng = "", selected = "", distance = "", status = "", price, imgSrc;
    private BadgesProvider bp;
    private ArrayList<Entity> badges;
    private BadgesListAdapter adapter;
    private Button prevBtn, nextBtn, dialogButtonOk, dialogButtonCancel;
    private TextView titleTV, descriptionTV, priceTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badges_list_map);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            query = bundle.getString("query", "");
            lat = bundle.getString("lat", "");
            lng = bundle.getString("lng", "");
            selected = bundle.getString("selected", "");
            distance = bundle.getString("distance", "");
            status = bundle.getString("status", "");
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.badgesMap);
        mapFragment.getMapAsync(this);

        prevBtn = findViewById(R.id.prevBtn);
        nextBtn = findViewById(R.id.nextBtn);
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bp.getPrevPage();
            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bp.getNextPage();
            }
        });
    }

    private void drawMarkers(ArrayList<Entity> badges) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Entity entity : badges) {
            if (entity.getDouble("latitude") != null && entity.getDouble("longitude") != null) {
                LatLng latLng = new LatLng(entity.getDouble("latitude"), entity.getDouble("longitude"));
                Marker marker = mMap.addMarker(new MarkerOptions().position(latLng));
                marker.setTag(entity);
                builder.include(latLng);
            }
        }
        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 100);
        mMap.animateCamera(cu);
    }

    // Map Ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }

        bp = new BadgesProvider(this, null, BadgesListAdapter.SEARCH_MODE);
        bp.setQuery(query);
        bp.setCategories(selected);
        bp.setLatitude(lat);
        bp.setLongitude(lng);
        bp.setDistance(distance);
        bp.setStatus(status);
        if ((badges = bp.getData()) != null) {
            adapter = new BadgesListAdapter(this, badges, BadgesListAdapter.SEARCH_MODE);
            adapter.setNotifyOnChange(true);
            bp.setAdapter(adapter);
            DataSetObserver observer = new DataSetObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    // TODO: clear old markers
                    mMap.clear();
                    drawMarkers(adapter.getAll());
                }
            };
            adapter.registerDataSetObserver(observer);
            adapter.notifyDataSetChanged();
        }

        mMap.setMyLocationEnabled(true);

        // Marker Listener
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                final Entity entity = (Entity) marker.getTag();
                final Dialog dialog = new Dialog(BadgeListMapsActivity.this);
                dialog.setContentView(R.layout.badge_details_item);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

                SimpleDraweeView draweeView = dialog.findViewById(R.id.imageBadge);
                imgSrc = entity.getString("image");
                if (!imgSrc.equals("")) {
                    Uri uri = Uri.parse(imgSrc);
                    draweeView.setImageURI(uri);
                }

                titleTV = dialog.findViewById(R.id.titleTV);
                titleTV.setText(entity.getString("title"));

                descriptionTV = dialog.findViewById(R.id.descriptionTV);
                descriptionTV.setText(entity.getString("description"));

                priceTV = dialog.findViewById(R.id.priceTV);
                price = entity.get("price") + " " + getString(R.string.coins);
                if (entity.getString("price_type").equals("per_minute")) {
                    price += " " + getString(R.string.per_minute);
                }
                priceTV.setText(String.format(Locale.getDefault(), "%s %s", getString(R.string.edit_price), price));

                RatingBar rateBar = dialog.findViewById(R.id.ratingBar);
                Double rate = entity.getDouble("rate");
                rateBar.setRating(rate.floatValue());

                dialogButtonOk = dialog.findViewById(R.id.detailsBtn);
                dialogButtonCancel = dialog.findViewById(R.id.closeBtn);

                dialogButtonOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(BadgeListMapsActivity.this, BadgeDetailsActivity.class);
                        IMApp.currentBadgeId = (int) entity.getInt("id");
                        startActivity(intent);
                    }
                });
                dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                return false;
            }

        });
    }
}
