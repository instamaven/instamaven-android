package com.instamaven.app.providers;

import android.content.Context;
import android.view.View;
import android.widget.ListView;

import com.instamaven.app.adapters.ListAdapter;
import com.instamaven.app.models.ChatLogEntity;
import com.instamaven.app.models.Entity;
import com.instamaven.app.utils.RequestClient;
import com.instamaven.app.utils.SettingsHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class ChatLogsProvider extends EntitiesProvider implements EntitiesProvider.ListProvider {

    private Context context;
    private ListAdapter adapter;
    private View spinnerView;

    public ChatLogsProvider(Context ctx, ListView view) {
        super(view);
        this.context = ctx;
//        spinnerView = inflater.inflate(R.layout.progress_spinner, null);
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

        if (page < 1) {
            page = 1;
        }

        try {
            String response = client
                    .setUrl(SettingsHelper.getUrl(context, "get_chatlogs"))
                    .setMethod("GET")
                    .addField("page", page.toString())
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
                list = getEntityList(ChatLogEntity.class, items, page);
                currentPage = obj.optInt("current_page");
                lastPage = obj.optInt("last_page");
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
