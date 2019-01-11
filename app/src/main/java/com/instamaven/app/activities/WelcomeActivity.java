package com.instamaven.app.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.instamaven.app.R;
import com.instamaven.app.utils.ProfileHelper;
import com.instamaven.app.utils.RequestClient;
import com.instamaven.app.utils.SettingsHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class WelcomeActivity extends AppCompatActivity {

    protected RequestClient.Builder client;
    protected TextView nameTV, emailTV, balanceTV, statusTV;
    protected SwitchCompat switchStatus;
    protected Button logoutBtn, onlineBtn, ondemandBtn;
    protected Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        statusTV = findViewById(R.id.statusTV);
        switchStatus = findViewById(R.id.switchStatus);
        switchStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    IMApp.visibility = 1;
                    statusTV.setText(R.string.status_visible);
                } else {
                    IMApp.visibility = 0;
                    statusTV.setText(R.string.status_invisible);
                }
            }
        });

        client = new RequestClient.Builder(this)
                .addHeader("Accept", "application/json");

        final JSONObject myProfile = ProfileHelper.getUser(this);
        try {
            // extract user info
            final JSONObject user = (JSONObject) myProfile.get("user");
            JSONObject social = null;
            if (myProfile.get("social") != JSONObject.NULL) {
                social = (JSONObject) myProfile.get("social");
            }
            JSONObject profile = null;
            if (myProfile.get("profile") != JSONObject.NULL) {
                profile = (JSONObject) myProfile.get("profile");
            }
            ImageView iconLoggedWith = findViewById(R.id.loggedWith);
            SimpleDraweeView draweeView = findViewById(R.id.userAvatar);
            draweeView.setImageResource(R.drawable.ic_no_ava);

            if (profile != null && social != null) {
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
                        iconLoggedWith.setImageResource(R.drawable.ic_facebook);
                        break;
                    case "twitter":
                        iconLoggedWith.setImageResource(R.drawable.ic_twitter);
                        break;
                    case "google":
                        iconLoggedWith.setImageResource(R.drawable.ic_google);
                        break;
                    case "user":
                    default:
                        iconLoggedWith.setImageResource(R.drawable.ic_instamaven_logo_round);
                        break;
                }
            }


            nameTV = findViewById(R.id.userNameTV);
            nameTV.setText(user.getString("name"));

            emailTV = findViewById(R.id.emailTV);
            emailTV.setText(user.getString("email"));

        } catch (JSONException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        balanceTV = findViewById(R.id.userBalance);
        balanceTV.setText(IMApp.getBalance(this));

        onlineBtn = findViewById(R.id.onlineBtn);
        onlineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProfileHelper.setVisibility(WelcomeActivity.this, IMApp.visibility);
                intent = new Intent(WelcomeActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                IMApp.current_mode = "online";
                startActivity(intent);
            }
        });

        ondemandBtn = findViewById(R.id.ondemandBtn);
        ondemandBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProfileHelper.setVisibility(WelcomeActivity.this, IMApp.visibility);
                intent = new Intent(WelcomeActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                IMApp.current_mode = "ondemand";
                startActivity(intent);
            }
        });

        // Logout Button
        logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(WelcomeActivity.this)
                        .setMessage(R.string.logout_profile_alert)
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok_btn, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new FCMLogout().execute();
                                try {
                                    client.setUrl(SettingsHelper.getUrl(WelcomeActivity.this, "post_user_logout")).setMethod("POST").send();
                                    ProfileHelper.resetToken(WelcomeActivity.this);
                                    Intent intent = new Intent(WelcomeActivity.this, SignInActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                } catch (Exception e) {
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel_btn,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        Toast.makeText(WelcomeActivity.this, "", Toast.LENGTH_SHORT).show();
                                    }
                                }).show();
            }
        });
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
    }
}

