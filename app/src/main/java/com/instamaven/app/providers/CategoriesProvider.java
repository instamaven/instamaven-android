package com.instamaven.app.providers;

import android.content.Context;
import android.view.View;

import com.instamaven.app.activities.IMApp;
import com.instamaven.app.adapters.ListAdapter;
import com.instamaven.app.models.CategoryEntity;
import com.instamaven.app.models.Entity;
import com.instamaven.app.utils.RequestClient;
import com.instamaven.app.utils.SettingsHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class CategoriesProvider extends EntitiesProvider implements EntitiesProvider.ListProvider {

    private Context context;
    private ListAdapter adapter;
    private View spinnerView;
    private int currentLevel = 0;

    public CategoriesProvider(Context ctx, View view, int currentLevel) {
        super(view);
        this.context = ctx;
        this.currentLevel = currentLevel;
        //spinnerView = inflator.inflate(R.layout.progress_spinner, null);
        this.client = new RequestClient.Builder(context)
                .addHeader("Accept", "application/json");
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

        try {
            String response = client
                    .setUrl(SettingsHelper.getUrl(context, "get_categories", currentLevel))
                    .setMethod("GET")
                    .addField("tags", IMApp.current_mode)
                    .send();
            obj = new JSONObject(response);
            if (obj.opt("error") != null) {
                String message = obj.optString("message");
                if (message.isEmpty()) {
                    message = "Error: " + obj.opt("status");
                }
                throw new Exception(message);
            } else {
                JSONArray items = (JSONArray) obj.get("data");
                list = getEntityList(CategoryEntity.class, items, page);
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
