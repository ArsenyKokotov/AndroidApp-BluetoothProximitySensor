package com.example.xzone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TermsActivity extends AppCompatActivity {

    protected Button agree;
    protected TextView textViewTerms;
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        agree=findViewById(R.id.buttonAgree);
        textViewTerms=findViewById(R.id.Terms);
        String text= getResources().getString(R.string.terms);
        textViewTerms.setText(text);

        textViewTerms.setMovementMethod(new ScrollingMovementMethod());

        agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TermsActivity.this, MainActivity.class);
                MainActivity.TERMS_AND_CONDITIONS_FLAG=1;
                startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed() {
        // Terminate Bluetooth Connection and close app
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);

    }
}