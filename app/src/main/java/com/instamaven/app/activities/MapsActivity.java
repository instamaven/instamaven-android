package com.instamaven.app.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.instamaven.app.R;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String VIEW_MODE = "view";
    public static final String EDIT_MODE = "edit";

    private GoogleMap mMap;
    private PlaceAutocompleteFragment placeAutoComplete;
    private Double latitude = 0.0, longitude = 0.0;
    private String viewMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            viewMode = bundle.getString("mode", EDIT_MODE);
            latitude = Double.parseDouble(bundle.getString("latitude", "0.0"));
            longitude = Double.parseDouble(bundle.getString("longitude", "0.0"));
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button saveBtn = findViewById(R.id.saveBtn);
        Button clearBtn = findViewById(R.id.prevBtn);

        if (viewMode.equals(EDIT_MODE)) {
            // Search Address
            placeAutoComplete = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete);
            placeAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    addMarker(place);
                }

                @Override
                public void onError(Status status) {
                    Log.d("Maps", "An error occurred: " + status);
                }
            });

            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent data = new Intent();
                    data.putExtra("latitude", latitude.toString());
                    data.putExtra("longitude", longitude.toString());
                    setResult(RESULT_OK, data);
                    finish();
                }
            });

            clearBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    latitude = 0.0;
                    longitude = 0.0;
                    mMap.clear();
                }
            });

        } else {
            CardView searchView = findViewById(R.id.searchView);
            searchView.setVisibility(View.GONE);
            saveBtn.setVisibility(View.GONE);
            clearBtn.setVisibility(View.GONE);
        }
    }

    // Add Marker Place
    private void addMarker(Place p) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(p.getLatLng());
        markerOptions.title(p.getName() + "");

        mMap.clear();
        mMap.addMarker(markerOptions);
//        mMap.addMarker(new MarkerOptions()
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_placeholder)));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(p.getLatLng()));
        latitude = p.getLatLng().latitude;
        longitude = p.getLatLng().longitude;

    }

    // Add Marker Place
    private void addMarker(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);

        mMap.clear();
        mMap.addMarker(markerOptions);
//        mMap.addMarker(new MarkerOptions()
//        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_placeholder)));
        latitude = latLng.latitude;
        longitude = latLng.longitude;
    }

    // Map Ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (latitude != 0 && longitude != 0) {
            LatLng latLng = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(latLng));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        if (viewMode.equals(EDIT_MODE)) {
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    addMarker(latLng);
                }
            });
        }
    }
}
