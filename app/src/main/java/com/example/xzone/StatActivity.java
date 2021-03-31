package com.example.xzone;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.example.xzone.Database.DatabaseHelper;
import com.example.xzone.Model.X_zone;

import java.util.ArrayList;
import java.util.List;

public class StatActivity extends AppCompatActivity {

    private Spinner nameSpinner;
    protected List<X_zone> xzoneList;
    protected DatabaseHelper dbHelper;
    protected ListView dataTable;
    protected List<String> dataRows;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stat);
        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button

        nameSpinner=findViewById(R.id.spinnerName);
        dataTable=findViewById(R.id.dataTable);
        dbHelper = new DatabaseHelper(this);
        xzoneList=dbHelper.getAllXzones();


        List<String> names = new ArrayList<>();
        names.add("Choose your Xzone");

        for (int i = 0; i < xzoneList.size(); i++) {
            String temp1=xzoneList.get(i).getName();
            names.add(temp1);
        }

        ArrayAdapter<String> adapter_name =  new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, names);
        adapter_name.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nameSpinner.setAdapter(adapter_name);

        //android:background="@android:drawable/btn_dropdown"
        //android:spinnerMode="dropdown"


        nameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String name=nameSpinner.getSelectedItem().toString();
                if (!name.equals("Choose your Xzone")) {
                    loadDataTable(name);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    void loadDataTable(String name){

        List<String> test= new ArrayList<>();
        dataRows=dbHelper.getFreq(name);

        List<String> rows = new ArrayList<>();

        for (int i=0; i<dataRows.size(); i++) {
            int g=i+1;
            String temp = "hour: " + g + " || frequency: " + dataRows.get(i);
            rows.add(temp);
        }

        ArrayAdapter<String> adapter  = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, rows);
        dataTable.setAdapter(adapter);

    }

}