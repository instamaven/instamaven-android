package com.instamaven.app.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.instamaven.app.R;
import com.instamaven.app.utils.RequestClient;
import com.instamaven.app.utils.SettingsHelper;

public class AddCommentActivity extends AppCompatActivity {

    protected RequestClient.Builder client;
    private RatingBar ratingBar;
    private EditText editCommentText;
    private Button addCommentBtn;
    private Button cancelBtn;
    private TextView rateType;
    private Integer badgeId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_comment);

        // Add Headers
        client = new RequestClient.Builder(this)
                .addHeader("Accept", "application/json");

        badgeId = getIntent().getExtras().getInt("badge_id", 0);

        setTitle(getString(R.string.add_comment));

        rateType = findViewById(R.id.rateType);
        ratingBar = findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                switch ((int) ratingBar.getRating()) {
                    case 1:
                        rateType.setText(R.string.rate_type_1);
                        break;
                    case 2:
                        rateType.setText(R.string.rate_type_2);
                        break;
                    case 3:
                        rateType.setText(R.string.rate_type_3);
                        break;
                    case 4:
                        rateType.setText(R.string.rate_type_4);
                        break;
                    case 5:
                        rateType.setText(R.string.rate_type_5);
                        break;
                    default:
                        rateType.setText("");
                }
            }
        });

        editCommentText = findViewById(R.id.editCommentText);

        addCommentBtn = findViewById(R.id.addCommentBtn);
        addCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    addComment();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        cancelBtn = findViewById(R.id.prevBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void addComment() throws Exception {

        final String message = editCommentText.getText().toString();
        final Float rating = ratingBar.getRating();

//        // Test empty fields
//        if (TextUtils.isEmpty(message)) {
//            editCommentText.setError(getString(R.string.check_empty_comment));
//            editCommentText.requestFocus();
//            return;
//        }

        String response = client
                .setUrl(SettingsHelper.getUrl(this, "post_comments", badgeId))
                .setMethod("POST")
                .addField("message", message)
                .addField("rate", rating.toString())
                .send();
//        JSONObject result = new JSONObject(response);

        // Alert Dialog
        new AlertDialog.Builder(AddCommentActivity.this)
                .setTitle(getString(R.string.alarm_title_complete))
                .setMessage(getString(R.string.alarm_text_comment))
                .setIcon(R.drawable.ic_complete_round)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
//                        Intent intent = new Intent(AddCommentActivity.this, MainActivity.class);
//                        startActivity(intent);
                        finish();
                    }
                })
                .show();
    }
}
