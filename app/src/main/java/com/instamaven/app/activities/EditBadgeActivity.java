package com.instamaven.app.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.instamaven.app.R;
import com.instamaven.app.utils.RequestClient;
import com.instamaven.app.utils.SelectedFile;
import com.instamaven.app.utils.SettingsHelper;
import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.Locale;

public class EditBadgeActivity extends AppCompatActivity {

    private final static int RESET_CATEGORIES = 1;
    private static final int SELECT_PICTURE = 100;
    private static final int GALLERY_REQUEST = 101;
    private static final int CHANGE_COORDINATES = 102;

    protected RequestClient.Builder client;
    protected EditText editTitle, editDescription, editPrice, editAddress, editUrl;
    protected RadioGroup radioGroup, radioGroupBadgeType;
    protected RadioButton fixedRadio, perMinRadio, onlineBtn, onDemandBtn;
    protected TextView textLat, textLng, amountCategoriesTV;
    protected Button addBtn, addImageBtn, mapBtn, btnCategories;
    protected ImageView imageView;
    protected SelectedFile selectedFile;
    protected ArrayList<CategoryEntity> categories;
    protected ArrayList<Integer> selected, tags;
    protected CategoriesListAdapter categoriesAdapter;
    protected String latitude, longitude, price_type, badge_type;
    private Intent intent;

    JSONObject badge;
    Integer badgeId = 0;
    boolean isError = false;
    private Handler mHandler;

    private ConstraintLayout loadingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_badge);

        loadingLayout = findViewById(R.id.loadingPanel);
        mHandler = new MessageHandler();

        try {
            String title = getIntent().getExtras().getString("title", "");
            getSupportActionBar().setTitle(title.isEmpty() ? getString(R.string.edit_badge_app_bar) : title);
        } catch (NullPointerException e) {

        }

        client = new RequestClient.Builder(this)
                .addHeader("Accept", "application/json");

        try {
            if (getCategoriesTree()) {
                categoriesAdapter = new CategoriesListAdapter(this, categories);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Radio Buttons Price Type
        radioGroup = findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.fixedRadioBtn:
                        price_type = "fixed";
                        break;
                    case R.id.perMinRadioBtn:
                        price_type = "per_minute";
                        break;
                }
            }
        });

        // Radio Buttons Badge Type
        radioGroupBadgeType = findViewById(R.id.radioGroupBadgeType);
        radioGroupBadgeType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.onlineBtn:
                        badge_type = "online";
                        break;
                    case R.id.onDemandBtn:
                        badge_type = "ondemand";
                        break;
                }
            }
        });

        // Map Button
        mapBtn = findViewById(R.id.mapBtn);
        mapBtn.setBackground(getResources().getDrawable(R.drawable.ic_google_maps, getTheme()));

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditBadgeActivity.this, MapsActivity.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                intent.putExtra("mode", MapsActivity.EDIT_MODE);
                startActivityForResult(intent, CHANGE_COORDINATES);
            }
        });

        // Load Image
        addImageBtn = findViewById(R.id.addImageBadgeBtn);
        addImageBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale);
                addImageBtn.startAnimation(anim);
                intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);

            }
        });

        // Save Badge
        addBtn = findViewById(R.id.addBadgeBtn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //progressBar.setVisibility(progressBar.VISIBLE);
                    //loadingLayout.setVisibility(View.VISIBLE);
                    if (categoriesAdapter.getSelected().isEmpty()) {
                        new AlertDialog.Builder(EditBadgeActivity.this)
                                .setTitle(getString(R.string.alarm_title_error))
                                .setMessage(getString(R.string.categories_choice))
                                .setCancelable(false)
                                .setNegativeButton(R.string.ok_btn, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                        //loadingLayout.setVisibility(View.GONE);
                                    }
                                }).show();
                    } else {
                        saveBadge();
                        //loadingLayout.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        // categories chooser
        btnCategories = findViewById(R.id.btnCategories);
        btnCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(EditBadgeActivity.this)
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
                                }
                        ).show();
            }
        });

        editTitle = findViewById(R.id.editTitleText);
        editDescription = findViewById(R.id.editDescriptionText);
        editPrice = findViewById(R.id.editPriceText);
        fixedRadio = findViewById(R.id.fixedRadioBtn);
        perMinRadio = findViewById(R.id.perMinRadioBtn);
        onlineBtn = findViewById(R.id.onlineBtn);
        onDemandBtn = findViewById(R.id.onDemandBtn);
        editAddress = findViewById(R.id.editAddressText);
        editUrl = findViewById(R.id.editUrlText);

        textLat = findViewById(R.id.textLat);
        textLng = findViewById(R.id.textLng);
        if (IMApp.location == 0) {
            textLat.setText("0.0");
            textLng.setText("0.0");
        } else {
            textLat.setText(String.format(Locale.getDefault(), "%.6f", Double.parseDouble(IMApp.lat)));
            textLng.setText(String.format(Locale.getDefault(), "%.6f", Double.parseDouble(IMApp.lng)));
        }
        // Check Badge
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            badgeId = bundle.getInt("badge_id", 0);
            if (badgeId > 0) {
                JSONObject obj;
                try {
                    String response = client
                            .setUrl(SettingsHelper.getUrl(this, "get_badge_details", badgeId))
                            .setMethod("GET")
                            .send();
                    obj = new JSONObject(response);
                    if (obj.opt("error") != null) {
                        String message = obj.optString("message");
                        if (message.isEmpty()) {
                            message = obj.optString("status");
                        }
                        throw new Exception(message);
                    } else {
                        getSupportActionBar().setTitle(getString(R.string.edit_badge_app_bar));

                        badge = (JSONObject) obj.get("data");

                        SimpleDraweeView draweeView = findViewById(R.id.imageBadge);
                        String imgSrc = badge.optString("image", "");
                        if (!imgSrc.equals("")) {
                            Uri uri = Uri.parse(imgSrc);
                            draweeView.setImageURI(uri);
                        }

                        editTitle.setText(badge.getString("title"));

                        if (badge.opt("description") != JSONObject.NULL) {
                            editDescription.setText(Jsoup.parse(badge.getString("description")).text());
                        }

                        if (badge.opt("price") != JSONObject.NULL) {
                            editPrice.setText(badge.getString("price"));
                        }

                        if (badge.opt("price_type") != JSONObject.NULL) {
                            price_type = badge.getString("price_type");
                            switch (price_type) {
                                case "fixed":
                                    fixedRadio.setChecked(true);
                                    perMinRadio.setChecked(false);
                                    break;
                                case "per_minute":
                                    fixedRadio.setChecked(false);
                                    perMinRadio.setChecked(true);
                                    break;
                            }
                        }

                        if (badge.opt("address") != JSONObject.NULL) {
                            editAddress.setText(Jsoup.parse(badge.getString("address")).text());
                        }

                        if (badge.opt("url") != JSONObject.NULL) {
                            editUrl.setText(badge.getString("url"));
                        }

                        latitude = badge.optString("latitude", "0.0");
                        longitude = badge.optString("longitude", "0.0");
                        try {
                            textLat.setText(String.format(Locale.getDefault(), "%.6f", Double.parseDouble(latitude)));
                            textLng.setText(String.format(Locale.getDefault(), "%.6f", Double.parseDouble(longitude)));
                        } catch (NumberFormatException e) {
                            latitude = "0.0";
                            longitude = "0.0";
                        }

                        if (badge.opt("tags") != JSONObject.NULL) {
                            JSONArray tags = (JSONArray) badge.opt("tags");
                            for (int i = 0; i < tags.length(); i++) {
                                JSONObject tag = tags.getJSONObject(i);
                                if (tag.optString("tag", "").equals("online")) {
                                    onlineBtn.setChecked(true);
                                    onDemandBtn.setChecked(false);
                                    break;
                                } else if (tag.optString("tag", "").equals("ondemand")) {
                                    onlineBtn.setChecked(false);
                                    onDemandBtn.setChecked(true);
                                    break;
                                }
                            }
                        }
                        resetCategoriesSelection();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
        //IMApp.animateView(progressOverlay, View.GONE, 0, 200);
        //progressBar.setVisibility(ProgressBar.GONE);
        loadingLayout.setVisibility(View.GONE);
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
//                if (entity.isRoot()) {
//                    flattenLevel(entity.getChildren());
//                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class CategoryEntity {
        private int id;
        private String title;
        private int parent_id;
        private JSONArray children;
        private boolean selected;

        private CategoryEntity(JSONObject object) throws JSONException {
            this.id = object.getInt("id");
            this.title = object.getString("title");
            this.parent_id = object.getInt("parent_id");
            if (object.opt("children") != null && object.opt("children") != JSONObject.NULL) {
                this.children = (JSONArray) object.get("children");
            }
        }

        private String getTitle() {
            return title;
        }

        private boolean isSelected() {
            return selected;
        }

        private void setSelected(boolean selected) {
            this.selected = selected;
        }

        private boolean isRoot() {
            return parent_id == 0;
        }

        private JSONArray getChildren() {
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
//            if (categories.get(position).isRoot()) {
//                holder.mCheckBox.setVisibility(View.GONE);
//                msContainer.setBackgroundColor(getResources().getColor(R.color.info_text));
//                holder.mTextView.setTextColor(getResources().getColor(R.color.white));
//            } else {
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

    private JSONObject saveBadge() throws JSONException {
        String response = "";
        String url, method;
        JSONObject result = new JSONObject();

        final String title = editTitle.getText().toString();
        final String description = editDescription.getText().toString();
        final String price = editPrice.getText().toString();
        final String address = editAddress.getText().toString();
        final String webSite = editUrl.getText().toString();
        //final String amount = amountCategoriesTV.getText().toString();

        // Test empty fields
        if (TextUtils.isEmpty(title)) {
            editTitle.setError(getString(R.string.check_empty_title));
            editTitle.requestFocus();
            return null;
        }

        if (TextUtils.isEmpty(description)) {
            editDescription.setError(getString(R.string.check_empty_description));
            editDescription.requestFocus();
            return null;
        }

        if (TextUtils.isEmpty(price)) {
            editPrice.setError(getString(R.string.check_empty_price));
            editPrice.requestFocus();
            return null;
        }

        if (!webSite.isEmpty() && !URLUtil.isNetworkUrl(webSite)) {
            editUrl.setError(getString(R.string.check_wrong_type_url));
            editUrl.requestFocus();
            return null;
        }

        try {
            loadingLayout.setVisibility(View.VISIBLE);
            if (badgeId == 0) {
                url = SettingsHelper.getUrl(this, "post_create_badge");
                method = "POST";
            } else {
                url = SettingsHelper.getUrl(this, "patch_edit_badge", badgeId);
                method = "PATCH";
            }
            response = client
                    .setUrl(url)
                    .setMethod(method)
                    .addField("title", title)
                    .addField("description", description)
                    .addField("price", price)
                    .addField("price_type", price_type)
                    .addField("latitude", latitude)
                    .addField("longitude", longitude)
                    .addField("address", address)
                    .addField("url", webSite)
                    .addField("categories", categoriesAdapter.getSelected().toString())
                    .addField("tags", badge_type)
                    .addFile("image", selectedFile)
                    .send();

            result = new JSONObject(response);
            String alarmTitle, alarmText;

            if (result.opt("error") != null) {
                alarmTitle = getString(R.string.alarm_title_error);
                alarmText = result.optString("message");
                if (alarmText.isEmpty()) {
                    alarmText = "Error: " + result.opt("status");
                }
                isError = true;
            } else {
                alarmTitle = getString(R.string.alarm_title_complete);
                alarmText = getString(R.string.alarm_text_complete);
            }

            new AlertDialog.Builder(EditBadgeActivity.this)
                    .setTitle(alarmTitle)
                    .setMessage(alarmText)
                    .setIcon(R.drawable.ic_complete_round)
                    .setCancelable(false)
                    .setNegativeButton(R.string.ok_btn, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            if (!isError) {
                                finish();
                                Intent intent = new Intent(EditBadgeActivity.this, MyBadgeListActivity.class);
                                startActivity(intent);
                            }
                        }
                    }).show();
            //progressBar.setVisibility(progressBar.GONE);
            loadingLayout.setVisibility(View.GONE);


        } catch (Exception e) {
            result.put("error", true);
            result.put("message", e.getMessage());
            e.printStackTrace();
        }
        //IMApp.animateView(progressOverlay, View.GONE, 0, 200);
        loadingLayout.setVisibility(View.GONE);


        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == SELECT_PICTURE) {
            imageView = findViewById(R.id.imageBadge);

            Uri selectedImage = data.getData();
            imageView.setImageURI(selectedImage);
            selectedFile = new SelectedFile(getContentResolver(), selectedImage);
        } else if (requestCode == CHANGE_COORDINATES) {
            latitude = data.getExtras().getString("latitude");
            longitude = data.getExtras().getString("longitude");
            textLat.setText(String.format(Locale.getDefault(), "%.6f", Double.parseDouble(latitude)));
            textLng.setText(String.format(Locale.getDefault(), "%.6f", Double.parseDouble(longitude)));
        }
    }
}