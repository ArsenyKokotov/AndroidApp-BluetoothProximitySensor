package com.example.xzone;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Notification;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity {

    private static Switch GUI;
    private static Switch Notifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button

        GUI=findViewById(R.id.switchBackground);
        Notifications=findViewById(R.id.switchNotifications);

        if (MainActivity.notificationOnOff==true) {
            Notifications.setChecked(true);
        } else {
            Notifications.setChecked(false);
        }

        if (MainActivity.backgroundWhiteOrBlack==true) {
            GUI.setChecked(true);
        } else {
            GUI.setChecked(false);
        }

        Notifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked==true) {
                    MainActivity.notificationOnOff=true;
                } else {
                    MainActivity.notificationOnOff=false;
                }
            }
        });

        GUI.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked==true) {
                    MainActivity.backgroundWhiteOrBlack=true;
                } else {
                    MainActivity.backgroundWhiteOrBlack=false;
                }
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        int color;
        if (MainActivity.backgroundWhiteOrBlack==false) {
            color = Color.parseColor("#545657");
        } else {
            color = Color.parseColor("#FFFFFF");
        }
        View view = SettingsActivity.this.getWindow().getDecorView();
        view.setBackgroundColor(color);

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}