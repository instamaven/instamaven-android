package com.instamaven.app.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.instamaven.app.R;
import com.instamaven.app.utils.ProfileHelper;
import com.instamaven.app.utils.RequestClient;
import com.instamaven.app.utils.SettingsHelper;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    protected EditText name, email, password, passwordConfirm;
    protected Button buttonRegister, backBtn;
    protected JSONObject obj;
    private static long back_pressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        passwordConfirm = findViewById(R.id.passwordConfirm);
//        password = findViewById(R.id.password);
//        passwordConfirm = findViewById(R.id.password);

        // Register Button
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonRegister.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                try {
                    registerUser();
                    startActivity(new Intent(RegisterActivity.this, WelcomeActivity.class));
                    finish();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        // Back Button To SignInActivity
        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, SignInActivity.class));
                finish();
            }
        });

        // Request new FCM token
        new FCMGetToken().execute();
    }

    // Register User
    private void registerUser() throws Exception {
        final String username = name.getText().toString();
        final String userEmail = email.getText().toString();
        final String userPassword = password.getText().toString();
        final String userPasswordConfirm = passwordConfirm.getText().toString();

        // Test empty fields
        if (TextUtils.isEmpty(username)) {
            name.setError("Please enter username");
            name.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(userEmail)) {
            email.setError("Please enter email");
            email.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(userPassword)) {
            password.setError("Please enter password");
            password.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(userPasswordConfirm)) {
            passwordConfirm.setError("Please enter the password again");
            passwordConfirm.requestFocus();
            return;
        }

        String response = new RequestClient.Builder(this)
                .setAuth(false)
                .addHeader("Accept", "application/json")
                .setUrl(SettingsHelper.getUrl(this, "post_user_register"))
                .setMethod("POST")
                .addField("name", username)
                .addField("email", userEmail)
                .addField("password", userPassword)
                .addField("password_confirmation", userPasswordConfirm)
                .addField("connection", IMApp.myFCMtoken)
                .send();
        obj = new JSONObject(response);
        if (obj.opt("error") != null) {
            String message = obj.optString("message");
            if (message.isEmpty()) {
                message = "Error: " + obj.opt("status");
            }
            throw new Exception(message);
        } else {
            ProfileHelper.saveUser(this, (JSONObject) obj.get("data"));
        }
    }

    public class FCMGetToken extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            IMApp.myFCMtoken = FirebaseInstanceId.getInstance().getToken();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //call_anim your activity where you want to land after log out
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
