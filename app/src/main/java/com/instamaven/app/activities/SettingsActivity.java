package com.instamaven.app.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.instamaven.app.R;

public class SettingsActivity extends AppCompatActivity {

    private CheckBox locationCheckBox;
    private RadioGroup radioGroup;
    private RadioButton kmBtn, mlBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        locationCheckBox = findViewById(R.id.locationCheckBox);
        if (IMApp.location == 1) {
            locationCheckBox.setChecked(true);
        }

        locationCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    IMApp.location = 1;
                } else {
                    IMApp.location = 0;
                }
            }
        });

        kmBtn = findViewById(R.id.kmBtn);
        mlBtn = findViewById(R.id.mlBtn);

        radioGroup = findViewById(R.id.radioGroup);
        if (IMApp.distance.equals("km")) {
            radioGroup.check(R.id.kmBtn);
        } else {
            radioGroup.check(R.id.mlBtn);
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.kmBtn:
                        IMApp.distance = "km";
                        break;
                    case R.id.mlBtn:
                        IMApp.distance = "ml";
                        break;
                }
            }
        });
    }
}
