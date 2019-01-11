package com.instamaven.app.activities;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.instamaven.app.R;
import com.instamaven.app.social.FacebookHelper;
import com.instamaven.app.social.FacebookListener;
import com.instamaven.app.social.GoogleHelper;
import com.instamaven.app.social.GoogleListener;
import com.instamaven.app.social.TwitterHelper;
import com.instamaven.app.social.TwitterListener;
import com.instamaven.app.utils.ProfileHelper;
import com.instamaven.app.utils.RequestClient;
import com.instamaven.app.utils.SettingsHelper;
import com.facebook.FacebookSdk;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import org.json.JSONObject;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class SignInActivity extends AppCompatActivity
        implements FacebookListener, TwitterListener, GoogleListener {

    protected EditText email, password;
    protected Context currentContext;

    protected GoogleHelper mGoogleHelper;
    protected FacebookHelper mFacebookHelper;
    protected TwitterHelper mTwitterHelper;

    protected Button googleBtn;
    protected Button facebookBtn;
    protected TwitterLoginButton twitterBtn;

    private static long back_pressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        Twitter.initialize(this);
        setContentView(R.layout.activity_sign_in);

        requestPermissions();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            CharSequence name = getString(R.string.fcm_channel_name);
            String description = getString(R.string.fcm_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel(
                    IMApp.FCM_BADGES_CHANNEL,
                    name,
                    importance
            );
            mChannel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = (NotificationManager) getSystemService(
                    NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(mChannel);
        }

        // FacebookHelper
        FacebookSdk.setApplicationId(getString(R.string.facebook_application_id));
        //FacebookSdk.sdkInitialize(this);
        mFacebookHelper = new FacebookHelper(this, this);

        facebookBtn = findViewById(R.id.facebookButton);
        facebookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //spinner.setVisibility(View.VISIBLE);
                mFacebookHelper.performSignIn(SignInActivity.this);
            }
        });

        // Google
        mGoogleHelper = new GoogleHelper(this, this);
        googleBtn = findViewById(R.id.googleButton);
        googleBtn.setBackground(getResources().getDrawable(R.drawable.ic_google, getTheme()));
        googleBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //spinner.setVisibility(View.VISIBLE);
                mGoogleHelper.performSignIn(SignInActivity.this);
            }
        });

        // Twitter
        mTwitterHelper = new TwitterHelper(this, this);
        twitterBtn = findViewById(R.id.twitterButton);
        twitterBtn.setBackground(getResources().getDrawable(R.drawable.ic_twitter, getTheme()));
        twitterBtn.setCallback(mTwitterHelper.clickListener());

        // Fresco
        Fresco.initialize(this);
        currentContext = this;

        // Spinner
        LayoutInflater inflater = getLayoutInflater();
        View spView = inflater.inflate(R.layout.big_spinner, null);
        //spinner = spView.findViewById(R.id.progressBarSpinner);

        // Register Button
        findViewById(R.id.buttonRegister).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(SignInActivity.this, RegisterActivity.class));
            }
        });

        // StrictMode
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

        Button loginButton = findViewById(R.id.buttonLogin);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ProfileHelper.login(SignInActivity.this, email.getText().toString(), password.getText().toString());
                    startActivity(new Intent(SignInActivity.this, WelcomeActivity.class));
                    finish();
//                    Toast.makeText(currentContext, getString(R.string.welcome), Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(currentContext, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        // Check API Token
        String token = ProfileHelper.getToken(this);
        if (!token.isEmpty() && ProfileHelper.getProfile(this)) {
            startActivity(new Intent(SignInActivity.this, WelcomeActivity.class));
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mTwitterHelper.onActivityResult(requestCode, resultCode, data);
        mGoogleHelper.onActivityResult(requestCode, resultCode, data);
        mFacebookHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onFacebookSignIn(String token, String userId) {
        try {
            ProfileHelper.loginUser(SignInActivity.this, token, "facebook");
            startActivity(new Intent(SignInActivity.this, WelcomeActivity.class));
            finish();
//            Toast.makeText(currentContext, getString(R.string.signed_in_facebook), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(currentContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onTwitterSignIn(String token, String secret, long userId) {
        try {
            ProfileHelper.loginUser(SignInActivity.this, token, secret, "twitter");
            startActivity(new Intent(SignInActivity.this, WelcomeActivity.class));
            finish();
//            Toast.makeText(currentContext, getString(R.string.signed_in_twitter), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(currentContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onGoogleSignIn(String token, String userId) {
        try {
            ProfileHelper.loginUser(SignInActivity.this, token, "google");
            startActivity(new Intent(SignInActivity.this, WelcomeActivity.class));
            finish();
//            Toast.makeText(currentContext, getString(R.string.signed_in_google), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(currentContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Request Permissions
    @AfterPermissionGranted(IMApp.RC_VIDEO_APP_PERM)
    private void requestPermissions() {
        String[] perms = {
//                android.Manifest.permission.INTERNET,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
//                android.Manifest.permission.WAKE_LOCK
 };

        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, getString(R.string.permissions_alert), IMApp.RC_VIDEO_APP_PERM, perms);
        }
    }

        // Back Button Exit App
    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis()) super.onBackPressed();
        Toast.makeText(this, getString(R.string.click_back_exit), Toast.LENGTH_SHORT).show();
        back_pressed = System.currentTimeMillis();
    }
}
