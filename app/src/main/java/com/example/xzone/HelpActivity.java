package com.example.xzone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

public class HelpActivity extends AppCompatActivity {

    protected TextView textViewMainPage;
    protected TextView textViewNewZone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button

        /*
        textViewMainPage=findViewById(R.id.textViewMainPage);
        textViewNewZone=findViewById(R.id.textViewNewZone);
        String text1= getResources().getString(R.string.main_page_help);
        textViewMainPage.setText(text1);
        textViewMainPage.setMovementMethod(new ScrollingMovementMethod());
        String text2= getResources().getString(R.string.new_zone_help);
        textViewNewZone.setText(text2);
        textViewNewZone.setMovementMethod(new ScrollingMovementMethod());

        /*
        final VideoView videoView = findViewById(R.id.videoView);
        Uri uri = Uri.parse("android.resource://" +getPackageName()  + "/" + R.raw.help);
        videoView.setVideoURI(uri);

        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

         */

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}