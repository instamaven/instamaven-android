package com.instamaven.app.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.instamaven.app.R;
import com.instamaven.app.utils.RequestClient;
import com.instamaven.app.utils.SettingsHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    private final static int RESET_CATEGORIES = 1;
    private static final int CHANGE_COORDINATES = 2;

    private static final Double ML_TO_KM = 1.60934;

    private RequestClient.Builder client;
    private ArrayList<CategoryEntity> categories;
    private ArrayList<Integer> selected;
    private CategoriesListAdapter categoriesAdapter;
    private JSONObject badge;

    private Spinner spinner;
    private Handler mHandler;
    private Button mapBtn, categoriesBtn, searchBtn, clearBtn;
    private EditText keywordsET;
    private TextView textLat, textLng;
    private RadioGroup radioGroupStatus;
    private RadioButton onlineBtn, offlineBtn;

    protected String response, latitude = "", longitude = "", distance = "", status = "1";
    protected ArrayAdapter<?> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        client = new RequestClient.Builder(this).addHeader("Accept", "application/json");
        mHandler = new MessageHandler();

        try {
            if (getCategoriesTree()) {
                categoriesAdapter = new CategoriesListAdapter(this, categories);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        keywordsET = findViewById(R.id.keywordsET);
        textLat = findViewById(R.id.textLat);
        textLng = findViewById(R.id.textLng);
        if (IMApp.location == 0) {
            textLat.setText("0.0");
            textLng.setText("0.0");
        } else {
            textLat.setText(String.format(Locale.getDefault(), "%.6f", Double.parseDouble(IMApp.lat)));
            textLng.setText(String.format(Locale.getDefault(), "%.6f", Double.parseDouble(IMApp.lng)));
        }

        // Spinner Distance
        spinner = findViewById(R.id.spinner);
        if (IMApp.distance.equals("km")) {
            if (IMApp.high_density) {
                adapter = ArrayAdapter.createFromResource(this, R.array.distance_population_high_km, android.R.layout.simple_spinner_item);
            } else {
                adapter = ArrayAdapter.createFromResource(this, R.array.distance_population_low_km, android.R.layout.simple_spinner_item);
            }
        } else {
            if (IMApp.high_density) {
                adapter = ArrayAdapter.createFromResource(this, R.array.distance_population_high_ml, android.R.layout.simple_spinner_item);
            } else {
                adapter = ArrayAdapter.createFromResource(this, R.array.distance_population_low_ml, android.R.layout.simple_spinner_item);
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected,
                                       int selectedItemPosition, long selectedId) {
                String[] size_values = getResources().getStringArray(IMApp.high_density ? R.array.item_value_high : R.array.item_value_low);
                distance = size_values[selectedItemPosition];
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Choose Categories Button
        categoriesBtn = findViewById(R.id.categoriesBtn);
        categoriesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(SearchActivity.this)
                        .setTitle(getString(R.string.select_categories))
                        .setAdapter(categoriesAdapter, null)
                        .setPositiveButton(R.string.next,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.dismiss();
                                    }
                                })
                        .setNegativeButton(R.string.clear_btn,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.cancel();
                                        Thread thread = new ThreadResetCategories();
                                        thread.start();
                                    }
                                }).show();
            }
        });

        // Map Button
        mapBtn = findViewById(R.id.mapBtn);
        mapBtn.setBackground(getResources().getDrawable(R.drawable.ic_google_maps, getTheme()));
        mapBtn.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SearchActivity.this, MapsActivity.class);
                i.putExtra("latitude", latitude);
                i.putExtra("longitude", longitude);
                i.putExtra("mode", MapsActivity.EDIT_MODE);
                startActivityForResult(i, CHANGE_COORDINATES);
            }
        });

        // Radio Buttons
        onlineBtn = findViewById(R.id.onlineBtn);
        offlineBtn = findViewById(R.id.offlineBtn);

        // Radio Group Status Online/Offline
        radioGroupStatus = findViewById(R.id.radioGroupStatus);
        radioGroupStatus.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.onlineBtn:
                        status = "1";
                        break;
                    case R.id.offlineBtn:
                        status = "0";
                        break;
                    case R.id.allBtn:
                        status = "";
                        break;
                }
            }
        });

        // Search Button
        searchBtn = findViewById(R.id.searchBtn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    search();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        // Action Button Clean Search Filters
        clearBtn = findViewById(R.id.prevBtn);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
                builder.setCancelable(false);
                builder.setMessage(getString(R.string.message_clean_search_filters));
                builder.setPositiveButton(R.string.ok_btn,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent i = new Intent(SearchActivity.this, SearchActivity.class);
                                finish();
                                startActivity(i);
                            }
                        })
                        .setNegativeButton(R.string.cancel_btn,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.dismiss();
                                    }
                                }
                        ).show();
            }
        });
    }

    private JSONObject search() throws JSONException {
        JSONObject result = new JSONObject();

        final String keywords = keywordsET.getText().toString();
        if (TextUtils.isEmpty(keywords)) {
            keywordsET.setError(getString(R.string.check_empty_search_keywords));
            keywordsET.requestFocus();
            return null;
        }

        try {
            Double range = Double.parseDouble(distance);
            if (IMApp.distance.equals("ml")) {
                range *= ML_TO_KM;
            }

            response = client
                    .setUrl(SettingsHelper.getUrl(this, "get_search"))
                    .setMethod("GET")
                    .addField("q", keywords)
                    .addField("latitude", latitude)
                    .addField("longitude", longitude)
                    .addField("categories", categoriesAdapter.getSelected().toString())
                    .addField("distance", range.toString())
                    .addField("online", status)
                    .send();
            result = new JSONObject(response);

            Intent i = new Intent(SearchActivity.this, BadgeListActivity.class);
            i.putExtra("q", keywords);
            i.putExtra("lng", longitude);
            i.putExtra("lat", latitude);
            i.putExtra("categories", selected.toString());
            i.putExtra("distance", range.toString());
            i.putExtra("status", status);
            startActivity(i);

        } catch (Exception e) {
            result.put("error", true);
            result.put("message", e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    protected class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RESET_CATEGORIES:
                    try {
                        resetCategoriesSelection();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    public class ThreadResetCategories extends Thread {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(RESET_CATEGORIES);
        }
    }

    private void resetCategoriesSelection() throws JSONException {
        if (badge == null) {
            categoriesAdapter.setSelected(new JSONArray());
        } else if (badge.opt("categories") != JSONObject.NULL) {
            JSONArray cats = (JSONArray) badge.get("categories");
            categoriesAdapter.setSelected(cats);
        }
    }

    private boolean getCategoriesTree() throws Exception {
        JSONObject obj;
        String response = client
                .setUrl(SettingsHelper.getUrl(this, "get_categories_tree"))
                .setMethod("GET")
                .send();
        obj = new JSONObject(response);
        if (obj.opt("error") != null) {
            // error found
        } else {
            categories = new ArrayList<>();
            flattenLevel((JSONArray) obj.get("data"));
            return true;
        }

        return false;
    }

    private void flattenLevel(JSONArray jsonObjects) {
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                CategoryEntity entity = new CategoryEntity(jsonObjects.getJSONObject(i));
                categories.add(entity);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class CategoryEntity {
        private int id;
        private String title;
        private int parent_id;
        private JSONArray children;
        private boolean selected;

        public CategoryEntity(JSONObject object) throws JSONException {
            this.id = object.getInt("id");
            this.title = object.getString("title");
            this.parent_id = object.getInt("parent_id");
            if (object.opt("children") != null && object.opt("children") != JSONObject.NULL) {
                this.children = (JSONArray) object.get("children");
            }
        }

        public String getTitle() {
            return title;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean isRoot() {
            return parent_id == 0;
        }

        public JSONArray getChildren() {
            return children;
        }
    }

    private class CategoriesListAdapter extends ArrayAdapter<CategoryEntity> {
        private Context mContext;
        private ArrayList<CategoryEntity> categories;
        private boolean isFromView = false;

        private CategoriesListAdapter(Context context, ArrayList<CategoryEntity> categories) {
            super(context, R.layout.multi_spinner_item, categories);
            this.mContext = context;
            this.categories = categories;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        private View getCustomView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;
            if (convertView == null) {
                LayoutInflater layoutInflator = LayoutInflater.from(mContext);
                convertView = layoutInflator.inflate(R.layout.multi_spinner_item, null);
                holder = new ViewHolder();
                holder.mTextView = convertView.findViewById(R.id.text);
                holder.mCheckBox = convertView.findViewById(R.id.checkbox);
                holder.cat_id = categories.get(position).id;
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.mTextView.setText(categories.get(position).getTitle());

            isFromView = true;
            holder.mCheckBox.setChecked(categories.get(position).isSelected());
            isFromView = false;

            View msContainer = convertView.findViewById(R.id.msContainer);
            holder.mCheckBox.setVisibility(View.VISIBLE);
            msContainer.setBackgroundColor(getResources().getColor(R.color.white));
            holder.mTextView.setTextColor(getResources().getColor(R.color.black));
            holder.mCheckBox.setTag(position);
            holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (!isFromView) {
                        int getPosition = (Integer) buttonView.getTag();
                        categories.get(getPosition).setSelected(isChecked);
                    }
                }
            });
            return convertView;
        }

        private class ViewHolder {
            private TextView mTextView;
            private CheckBox mCheckBox;
            private int cat_id;
        }

        private void setSelected(JSONArray selected) throws JSONException {
            for (CategoryEntity category : categories) {
                // reset selection
                category.setSelected(false);
                for (int i = 0; i < selected.length(); i++) {
                    if (category.id == selected.getJSONObject(i).getInt("id")) {
                        category.setSelected(true);
                        break;
                    }
                }
            }
            this.notifyDataSetChanged();
        }

        private ArrayList<Integer> getSelected() {
            selected = new ArrayList<>();
            for (CategoryEntity category : categories) {
                if (category.isSelected()) {
                    selected.add(category.id);
                }
            }
            return selected;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHANGE_COORDINATES) {
            latitude = data.getExtras().getString("latitude");
            longitude = data.getExtras().getString("longitude");
            textLat.setText(String.format(Locale.getDefault(), "%.6f", Double.parseDouble(latitude)));
            textLng.setText(String.format(Locale.getDefault(), "%.6f", Double.parseDouble(longitude)));
        }
    }
}
