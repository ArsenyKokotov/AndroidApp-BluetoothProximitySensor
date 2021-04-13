package com.example.xzone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.xzone.Database.DatabaseHelper;
import com.example.xzone.Model.X_zone;

import java.util.ArrayList;
import java.util.List;

public class DeleteActivity extends AppCompatActivity {

    private static Spinner spinnerOldZoneName;
    private static DatabaseHelper dbHelper;
    private static Button delete;
    private static List<X_zone> xzoneList;
    private static String oldZoneName="";
    private static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);
        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button

        spinnerOldZoneName=findViewById(R.id.spinnerOldZoneNameToDelete);
        delete=findViewById(R.id.buttonDeleteOldZone);
        dbHelper = new DatabaseHelper(this);

        xzoneList=dbHelper.getAllXzones();
        mContext=this;


        List<String> names = new ArrayList<>();
        names.add("Choose your Zone-X");
        for (int i = 0; i < xzoneList.size(); i++) {
            String temp1=xzoneList.get(i).getName();
            names.add(temp1);
        }

        ArrayAdapter<String> adapter_name =  new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, names);
        adapter_name.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOldZoneName.setAdapter(adapter_name);


        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE);
                String name = sharedPreferences.getString("name", "");
                if (name.equals(oldZoneName)) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("name", "NA");
                    editor.apply();

                    MainActivity.connectedThread.write("stop");
                    Intent serviceIntent = new Intent(mContext, ServiceClass.class);
                    stopService(serviceIntent);

                }

                dbHelper.deleteZone(oldZoneName);
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);

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
        View view = DeleteActivity.this.getWindow().getDecorView();
        view.setBackgroundColor(color);

        spinnerOldZoneName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String buffer=spinnerOldZoneName.getSelectedItem().toString();
                oldZoneName="";
                if (!buffer.equals("Choose your Zone-X")) {
                    oldZoneName=buffer;
                    delete.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getApplicationContext(), "Select a Zone-X", Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}