package com.instamaven.app.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.instamaven.app.R;
import com.instamaven.app.adapters.AvatarsListAdapter;
import com.instamaven.app.utils.ProfileHelper;
import com.instamaven.app.utils.RequestClient;
import com.instamaven.app.utils.SettingsHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class AvatarsGridActivity extends AppCompatActivity {

    protected ArrayList<String> avatarsImg;
    protected GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatars);

        String theme = getIntent().getExtras().getString("theme");
        try {
            String response = new RequestClient.Builder(this)
                    .addHeader("Accept", "application/json")
                    .setUrl(SettingsHelper.getUrl(this, "get_avatars_category", theme))
                    .setMethod("GET")
                    .send();
            JSONObject obj = new JSONObject(response);
            if (obj.opt("error") != null) {
                String message = obj.optString("message");
                if (message.isEmpty()) {
                    message = "Error: " + obj.opt("status");
                }
                throw new Exception(message);
            } else {
                JSONArray jsonArray = obj.getJSONArray("data");
                avatarsImg = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    avatarsImg.add(jsonArray.getString(i));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        gridView = findViewById(R.id.gridView);
        AvatarsListAdapter adapter = new AvatarsListAdapter(this, avatarsImg);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AvatarsGridActivity.this);
                builder.setCancelable(true);
                builder.setMessage(getString(R.string.message_use_avatar));
                builder.setPositiveButton(R.string.ok_btn,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                try {
                                    String response = new RequestClient.Builder(AvatarsGridActivity.this)
                                            .addHeader("Accept", "application/json")
                                            .setUrl(SettingsHelper.getUrl(AvatarsGridActivity.this, "patch_change_avatar"))
                                            .setMethod("PATCH")
                                            .addField("url", avatarsImg.get(position))
                                            .send();
                                    ProfileHelper.saveProfile(AvatarsGridActivity.this, response);
                                    Intent i = new Intent(AvatarsGridActivity.this, ProfileActivity.class);
                                    startActivity(i);
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
            }
        });
    }
}
