package com.example.xzone;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.example.xzone.Database.DatabaseHelper;
import com.example.xzone.Model.DayCount;
import com.example.xzone.Model.HourCount;
import com.example.xzone.Model.X_zone;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static com.example.xzone.ServiceClass.entry_time;

public class StatActivity extends AppCompatActivity {

    private static Spinner nameSpinner;
    private static Spinner analysisSpinner;
    private static List<X_zone> xzoneList;
    private static DatabaseHelper dbHelper;
    private static ListView dataTable;
    private static String nameOfZone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stat);
        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button

        nameSpinner=findViewById(R.id.spinnerName);
        analysisSpinner=findViewById(R.id.spinnerAnalysis);
        dataTable=findViewById(R.id.dataTable);
        dbHelper = new DatabaseHelper(this);
        xzoneList=dbHelper.getAllXzones();


        List<String> names = new ArrayList<>();
        List<String> analysis = new ArrayList<>();
        names.add("Choose your Xzone");

        analysis.add("Pick your data choice");
        String[] analysisChoice={"Day with longest entry-exit period", "Hour with longest entry-exit period",
                                 "Today's hourly entries count", "Observation days entries count", "Observation hours entries count",
                                  "Most frequented hour", "Most frequented day"};

        for (int i = 0; i < xzoneList.size(); i++) {
            String temp1=xzoneList.get(i).getName();
            names.add(temp1);
        }

        analysis.addAll(Arrays.asList(analysisChoice));

        ArrayAdapter<String> adapter_name =  new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, names);
        ArrayAdapter<String> adapter_analysis =  new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, analysis);
        adapter_name.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter_analysis.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nameSpinner.setAdapter(adapter_name);
        analysisSpinner.setAdapter(adapter_analysis);
        analysisSpinner.setEnabled(false);

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
        View view = StatActivity.this.getWindow().getDecorView();
        view.setBackgroundColor(color);

        nameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                nameOfZone=nameSpinner.getSelectedItem().toString();

                if (!nameOfZone.equals("Choose your Xzone")) {
                    //loadDataTable(name);
                    analysisSpinner.setEnabled(true);
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        analysisSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String analysisChoice=analysisSpinner.getSelectedItem().toString();
                if (!analysisChoice.equals("Pick your data choice")) {
                    loadData(nameOfZone, analysisChoice);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    void loadData(String name, String choice) {

        List<String> dataRows=new ArrayList<>();

        String[] analysisChoice={"Day with longest entry-exit period", "Hour with longest entry-exit period",
                "Today's hourly entries count", "Observation days entries count", "Observation hours entries count",
                "Most frequented hour", "Most frequented day"};

        if (choice.equals(analysisChoice[0])) {
            dataRows.add(dbHelper.findDayWithLongestEntryExitTime(name));
            ArrayAdapter<String> adapter  = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataRows);
            dataTable.setAdapter(adapter);
        } else if (choice.equals(analysisChoice[1])) {
            dataRows.add(dbHelper.findHourWithLongestEntryExitTime(name));
            ArrayAdapter<String> adapter  = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataRows);
            dataTable.setAdapter(adapter);

        } else if (choice.equals(analysisChoice[2])) {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String time=df.format(c.getTime());
            dataRows=dbHelper.findTodayHourlyEntryCount(name, time.substring(0,10));
            if (!dataRows.isEmpty()) {
                ArrayAdapter<String> adapter  = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataRows);
                dataTable.setAdapter(adapter);
            } else {
                List<String> empty=new ArrayList<>();
                empty.add("There are no detected values for this X-zone");
                ArrayAdapter<String> adapter  = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, empty);
                dataTable.setAdapter(adapter);
            }

        } else if (choice.equals(analysisChoice[3])) {
            DayCount dCount =dbHelper.findDaysEntryCount(name);

            List<String> days=dCount.getDays();
            List<Integer> count=dCount.getCount();

            if (!days.isEmpty()) {
                for (int i=0; i<days.size(); i++) {
                    String holder=days.get(i) + "|| entries count: " + count.get(i);
                    dataRows.add(holder);
                }
                ArrayAdapter<String> adapter  = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataRows);
                dataTable.setAdapter(adapter);
            } else {
                dataRows.add("There are no detected values for this X-zone");
                ArrayAdapter<String> adapter  = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataRows);
                dataTable.setAdapter(adapter);
            }


        } else if (choice.equals(analysisChoice[4])) {
            HourCount hCount =dbHelper.findHourlyCount(name);

            List<String> hours=hCount.getHours();
            List<Integer> count=hCount.getCount();

            if (!hCount.getCount().isEmpty()) {
                for (int i=0; i<hours.size(); i++) {
                    String holder=hours.get(i) + "|| entries count: " + count.get(i);
                    dataRows.add(holder);
                }
                ArrayAdapter<String> adapter  = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataRows);
                dataTable.setAdapter(adapter);
            } else {
                dataRows.add("There are no detected values for this X-zone");
                ArrayAdapter<String> adapter  = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataRows);
                dataTable.setAdapter(adapter);
            }

        } else if (choice.equals(analysisChoice[5])) {
            String result=dbHelper.findMostVisitedHour(name);
            dataRows.add(result);
            ArrayAdapter<String> adapter  = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataRows);
            dataTable.setAdapter(adapter);

        } else if (choice.equals(analysisChoice[6])) {
            String result=dbHelper.findMostVisitedDay(name);
            dataRows.add(result);
            ArrayAdapter<String> adapter  = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataRows);
            dataTable.setAdapter(adapter);
        }

    }
}