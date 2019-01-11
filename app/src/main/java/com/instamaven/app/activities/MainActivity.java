package com.instamaven.app.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.TextView;

import com.instamaven.app.R;
import com.instamaven.app.fragments.BadgesList;
import com.instamaven.app.fragments.CategoriesGrid;
import com.instamaven.app.fragments.NotificationsList;
import com.instamaven.app.models.PlaceEntity;
import com.instamaven.app.providers.SuggestionProvider;
import com.instamaven.app.utils.ProfileHelper;
import com.instamaven.app.utils.RequestClient;
import com.instamaven.app.utils.SelectedFile;
import com.instamaven.app.utils.SettingsHelper;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import android.os.AsyncTask;
import android.widget.Toast;
import android.widget.ToggleButton;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    RequestClient.Builder client;
    JSONObject obj;
    private Button profileBtn;
    private Integer status;

    public boolean searchMenuVisibility = true;
    public boolean videoCallMenuVisibility = false;
    public boolean addBadgeMenuVisibility = false;

    private static View hView;
    private static TextView statusTV, nav_user, balanceTV, mNotificationsTV, badgeTypeTV;
    private Button editImageBtn;

    private static Switch switchStatus, switchBadgeType;
    private Intent intent;

    public String lat, lng, response;
    public SearchView searchView;

    public boolean gps_enabled = false;
    public boolean network_enabled = false;

    private LocationManager myLocationManager;
    private LocationListener myLocationListener;
    private static long back_pressed;
    private Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        startLocationManager();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // reset connection status
        IMApp.isConnection = false;

        client = new RequestClient.Builder(this)
                .addHeader("Accept", "application/json");
        try {
            // reset FCM token
            if (!IMApp.myFCMtoken.isEmpty()) {
                saveNewFcmToken(IMApp.myFCMtoken);
            } else {
                new FCMGetToken().execute();
            }
            IMApp.myProfile = ProfileHelper.getUser(this);
            // extract user info
            JSONObject user = (JSONObject) IMApp.myProfile.get("user");
            if (user == JSONObject.NULL) {
                throw new JSONException(getString(R.string.profile_corrupted));
            }
            IMApp.myUserId = user.getInt("id");
            JSONObject social = null;
            if (IMApp.myProfile.get("social") != JSONObject.NULL) {
                social = (JSONObject) IMApp.myProfile.get("social");
            }

            JSONObject profile = null;
            if (IMApp.myProfile.get("profile") != JSONObject.NULL) {
                profile = (JSONObject) IMApp.myProfile.get("profile");
            }

            hView = navigationView.getHeaderView(0);

            SimpleDraweeView draweeView = hView.findViewById(R.id.imageProfileUser);
            draweeView.setImageResource(R.drawable.ic_no_ava);
            ImageView socialIcon = hView.findViewById(R.id.socialIcon);

            if (social != null && profile != null) {
                String imgSrc = "";
                if (profile.opt("avatar") != JSONObject.NULL) {
                    imgSrc = profile.optString("avatar");
                }
                if (imgSrc.equals("") && social.opt("avatar") != JSONObject.NULL) {
                    imgSrc = social.optString("avatar");
                }
                if (!imgSrc.equals("")) {
                    Uri uri = Uri.parse(imgSrc);
                    draweeView.setImageURI(uri);
                }
                switch ((String) social.opt("provider")) {
                    case "facebook":
                        socialIcon.setImageResource(R.drawable.ic_facebook);
                        break;
                    case "twitter":
                        socialIcon.setImageResource(R.drawable.ic_twitter);
                        break;
                    case "google":
                        socialIcon.setImageResource(R.drawable.ic_google);
                        break;
                    case "user":
                    default:
                        socialIcon.setImageResource(R.drawable.ic_instamaven_logo_round);
                        break;
                }
            }

            editImageBtn = hView.findViewById(R.id.editImageBtn);
            editImageBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CropImage.activity()
                            .setAspectRatio(1, 1)
                            .setCropShape(CropImageView.CropShape.OVAL)
                            .setRequestedSize(400, 400)
                            .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                            .setOutputCompressQuality(50)
                            .start(MainActivity.this);
                }
            });

            profileBtn = hView.findViewById(R.id.profileBtn);
            profileBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    closeDrawer();
                }
            });

            nav_user = hView.findViewById(R.id.username);
            nav_user.setText(user.getString("name"));

            balanceTV = hView.findViewById(R.id.balanceTV);
            balanceTV.setText(IMApp.getBalance(this));

            statusTV = hView.findViewById(R.id.statusTV);
            statusTV.setText(IMApp.getVisibility(this));


            // Status visible/invisible
            switchStatus = hView.findViewById(R.id.switchNavStatus);
            switchStatus.setChecked(IMApp.visibility == 1);
            switchStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, final boolean checked) {
                    IMApp.visibility = checked ? 1 : 0;
                    ProfileHelper.setVisibility(MainActivity.this, IMApp.visibility);
                    recreate();
                }
            });

            ToggleButton toggleButton = hView.findViewById(R.id.toggleButton);
            if (IMApp.current_mode.equals("online")) {
                toggleButton.setChecked(true);
            }

            toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (isChecked) {
                        IMApp.current_mode = "online";
                        recreate();
                        closeDrawer();
                    } else {
                        IMApp.current_mode = "ondemand";
                        recreate();
                        closeDrawer();
                    }
                }
            });

        } catch (Exception e) {
            // something wrong with profile?
        }

        // Default Fragments
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flMenu, CategoriesGrid.newInstance(0))
                .commit();

        // Notifications Show
        mNotificationsTV = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().
                findItem(R.id.notifications));
    }

    // Save New Token
    public void saveNewFcmToken(String refreshedToken) throws Exception {
        String response = client.setUrl(SettingsHelper.getUrl(this, "post_fcm"))
                .setMethod("POST")
                .addField("connection", refreshedToken)
                .send();
        obj = new JSONObject(response);
        if (obj.opt("data") != null) {
            // save updated profile
            ProfileHelper.saveUser(this, (JSONObject) obj.get("data"));
        }
    }

    public void setActionBarTitle(int resId) {
        getSupportActionBar().setTitle(getString(resId));
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void onBackPressed() {
        if (!closeDrawer()) {
            super.onBackPressed();
        }
    }

    private boolean closeDrawer() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        final SearchRecentSuggestions suggestions = new SearchRecentSuggestions(MainActivity.this,
                SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                suggestions.saveRecentQuery(query, null);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.flMenu, BadgesList.newInstance(query))
                        .addToBackStack(null)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return true;
            }
        });

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                String query = getSuggestion(position);
                searchView.setQuery(query, true);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.flMenu, BadgesList.newInstance(query))
                        .addToBackStack(null)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
                searchView.clearFocus();
                return true;
            }
        });

        MenuItem videoCallItem = menu.findItem(R.id.action_videoCall);
        if (videoCallMenuVisibility && IMApp.myUserId != IMApp.user_id) {
            videoCallItem.setVisible(true);
            videoCallItem.setEnabled(true);
        } else {
            videoCallItem.setVisible(false);
            videoCallItem.setEnabled(false);
        }
        //return true;
        return super.onCreateOptionsMenu(menu);
    }

    private String getSuggestion(int position) {
        Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(
                position);
        String suggestion = cursor.getString(cursor
                .getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
        return suggestion;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        JSONObject result = new JSONObject();
        switch (item.getItemId()) {
            case R.id.action_search:
                return true;
            case R.id.action_search_advanced:
                try {
                    response = client.setUrl(SettingsHelper.getUrl(this, "post_place"))
                            .setMethod("POST")
                            .addField("lat", lat)
                            .addField("lng", lng)
                            .send();
                    result = new JSONObject(response);
                    if (result.opt("data") != null) {
                        JSONObject settings = (JSONObject) IMApp.myProfile.get("settings");
                        int density_threshold = settings.getInt("population_threshold");
                        PlaceEntity pe = new PlaceEntity(result.getJSONObject("data"));
                        if (pe.getDouble("density") > density_threshold) {
                            IMApp.high_density = true;
                        }
                    }
                } catch (Exception e) {
                }
                intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        switch (item.getItemId()) {
            case R.id.helper:
                ft.replace(R.id.flMenu, CategoriesGrid.newInstance(0));
                break;
            case R.id.addBadge:
                intent = new Intent(MainActivity.this, EditBadgeActivity.class);
                intent.putExtra("title", getString(R.string.create_badge_app_bar));
                startActivity(intent);
                break;
            case R.id.accountUser:
                intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
                break;
            case R.id.my_badges:
                ft.replace(R.id.flMenu, BadgesList.newInstance(true));
                break;
            case R.id.favorites:
                ft.replace(R.id.flMenu, BadgesList.newInstance());
                break;
            case R.id.notifications:
                setActionBarTitle(R.string.notifications);
                ft.replace(R.id.flMenu, new NotificationsList());
                break;
            case R.id.settings:
                setActionBarTitle(R.string.settings);
                intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                builder.setMessage(getString(R.string.logout_profile_alert));
                builder.setPositiveButton(R.string.ok_btn,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                new FCMLogout().execute();
                                try {
                                    client.setUrl(SettingsHelper.getUrl(MainActivity.this, "post_user_logout")).setMethod("POST").send();
                                    ProfileHelper.resetToken(MainActivity.this);
                                    intent = new Intent(MainActivity.this, SignInActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                } catch (Exception e) {
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel_btn,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.dismiss();
                                    }
                                }
                        ).show();
                break;
        }
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();

        closeDrawer();

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call_anim the superclass method first

        new FCMGetToken().execute();
    }

    // Request Permissions Result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private class FCMLogout extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            IMApp.myFCMtoken = "";
            try {
                FirebaseInstanceId.getInstance().deleteInstanceId();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //call_anim your activity where you want to land after log out
        }
    }

    public class FCMGetToken extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            String token = FirebaseInstanceId.getInstance().getToken();
            try {
                IMApp.sendRegistrationToServer(getApplicationContext(), token);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //call_anim your activity where you want to land after log out
        }
    }

    // Location Manager
    private void startLocationManager() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                myLocationManager = getSystemService(LocationManager.class);
            }
            gps_enabled = myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            network_enabled = myLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        myLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        myLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                IMApp.lat = String.valueOf(location.getLatitude());
                IMApp.lng = String.valueOf(location.getLongitude());

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onProviderEnabled(String s) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onProviderDisabled(String s) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        };

        if (gps_enabled) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                gps_enabled = false;
            }
        }
        if (gps_enabled) {
            myLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
        }

        if (network_enabled) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                network_enabled = false;
            }
        }
        if (network_enabled) {
            myLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                try {
                    String response = client
                            .setUrl(SettingsHelper.getUrl(this, "post_create_avatar"))
                            .setMethod("POST")
                            .addFile("image", new SelectedFile(getContentResolver(), resultUri))
                            .send();
                    JSONObject obj = new JSONObject(response);
                    if (obj.opt("data") != null) {
                        ProfileHelper.saveUser(this, (JSONObject) obj.get("data"));
                        recreate();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "" + error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
