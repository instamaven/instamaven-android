package com.instamaven.app.providers;

import android.content.Context;
import android.view.View;
import android.widget.ListView;

import com.instamaven.app.adapters.ListAdapter;
import com.instamaven.app.models.Entity;
import com.instamaven.app.models.PlaceEntity;
import com.instamaven.app.utils.RequestClient;
import com.instamaven.app.utils.SettingsHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class PlaceProvider extends EntitiesProvider implements EntitiesProvider.ListProvider {

    private Context context;
    private ListAdapter adapter;
    private View spinnerView;

    public PlaceProvider(Context ctx, ListView view) {
        super(view);
        this.context = ctx;
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
                    .setUrl(SettingsHelper.getUrl(context, "post_place"))
                    .setMethod("POST")
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
                list = getEntityList(PlaceEntity.class, items, page);
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
