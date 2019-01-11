package com.instamaven.app.providers;

import android.content.Context;
import android.view.View;
import android.widget.ListView;

import com.instamaven.app.activities.IMApp;
import com.instamaven.app.adapters.BadgesListAdapter;
import com.instamaven.app.adapters.ListAdapter;
import com.instamaven.app.models.BadgeEntity;
import com.instamaven.app.models.Entity;
import com.instamaven.app.utils.RequestClient;
import com.instamaven.app.utils.SettingsHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class BadgesProvider extends EntitiesProvider implements EntitiesProvider.ListProvider {

    private Context context;
    private ListAdapter adapter;
    private int mode;
    private View spinnerView;
    private int categoryId = 0;
    private String query = "", latitude = "", longitude = "", distance = "", categories, status = "";

    public BadgesProvider(Context ctx, ListView view, int listMode) {
        super(view);
        context = ctx;
        mode = listMode;
        //spinnerView = inflator.inflate(R.layout.progress_spinner, null);
        client = new RequestClient.Builder(context)
                .addHeader("Accept", "application/json")
                .addHeader("X-SCREEN-DENSITY", String.format(
                        Locale.getDefault(), "%d",
                        context.getResources().getDisplayMetrics().densityDpi)
                );
    }

    private int getCurrentCategory() {
        return categoryId;
    }

//    private String getCategories() { return categories; }

    private String getQuery() {
        return query;
    }

    public void setQuery(String q) {
        query = q;
    }

    public void setCurrentCategory(int id) {
        categoryId = id;
    }

    private String getCategories() {
        return categories;
    }

    public void setCategories(String cat) {
        categories = cat;
    }

    public void setLatitude(String lat) {
        latitude = lat;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setDistance(String dist) {
        distance = dist;
    }

    public String getDistance() {
        return distance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String s) {
        status = s;
    }

    @Override
    public void setAdapter(ListAdapter mAdapter) {
        adapter = mAdapter;
    }

    @Override
    public ListAdapter getAdapter() {
        return adapter;
    }

    public ArrayList<Entity> getData() {
        return getData(1);
    }

    @Override
    public ArrayList<Entity> getData(Integer page) {
        ArrayList<Entity> list;
        JSONObject obj;

        if (page < 1) {
            page = 1;
        }

        try {
            switch (mode) {
                case BadgesListAdapter.MY_BADGES_MODE:
                    client.setUrl(SettingsHelper.getUrl(context, "get_my_badges"));
                    break;
                case BadgesListAdapter.FAVORITES_MODE:
                    client.setUrl(SettingsHelper.getUrl(context, "get_favorites"));
                    break;
                case BadgesListAdapter.SEARCH_MODE:
                    if (getQuery().isEmpty()) {
                        return null;
                    }
                    client.setUrl(SettingsHelper.getUrl(context, "get_search"))
                        .addField("q", getQuery())
                        .addField("categories", getCategories())
                        .addField("lat", getLatitude())
                        .addField("lng", getLongitude())
                        .addField("distance", getDistance())
                        .addField("online", getStatus());
                    break;
                case BadgesListAdapter.BADGES_MODE:
                default:
                    client.setUrl(SettingsHelper.getUrl(context, "get_categories_show", getCurrentCategory()));
                    break;
            }
            String response = client.setMethod("GET")
                    .addField("tags", IMApp.current_mode)
                    .addField("page", page.toString()).send();
            obj = new JSONObject(response);
            if (obj.opt("error") != null) {
                String message = obj.optString("message");
                if (message.isEmpty()) {
                    message = "Error: " + obj.opt("status");
                }
                throw new Exception(message);
            } else {
                JSONArray items = (JSONArray) obj.get("data");
                list = getEntityList(BadgeEntity.class, items, page);
                currentPage = obj.optInt("current_page", 1);
                lastPage = obj.optInt("last_page", 1);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        return list;
    }

    public void setSpinnerView(View spinner) {
        spinnerView = spinner;
    }

    @Override
    public View getSpinnerView() {
        return spinnerView;
    }
}
