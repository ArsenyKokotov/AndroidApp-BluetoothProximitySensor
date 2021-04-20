package com.example.xzone;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.xzone.Database.DatabaseHelper;
import com.example.xzone.Model.DayCount;
import com.example.xzone.Model.HourCount;
import com.example.xzone.Model.X_zone;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.example.xzone.ServiceClass.entry_time;
import static com.example.xzone.ServiceClass.exit_time;

public class StatActivity extends AppCompatActivity {

    private static Spinner nameSpinner;
    private static Spinner analysisSpinner;
    private static List<X_zone> xzoneList;
    private static DatabaseHelper dbHelper;
    private static ListView dataTable;
    private static String nameOfZone;
    private static GraphView graph1;
    private static GraphView graph2;
    private static GraphView graph3;

    LineGraphSeries<DataPoint> series;

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
        graph1 = findViewById(R.id.graph1);
        graph2 = findViewById(R.id.graph2);
        graph3 = findViewById(R.id.graph3);


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

        graph1.setVisibility(View.GONE);
        graph2.setVisibility(View.GONE);
        graph3.setVisibility(View.GONE);
        dataTable.setVisibility(View.GONE);

        graph1.invalidate();
        graph2.invalidate();
        graph3.invalidate();
        dataTable.invalidateViews();

        String[] analysisChoice={"Day with longest entry-exit period", "Hour with longest entry-exit period",
                "Today's hourly entries count", "Observation days entries count", "Observation hours entries count",
                "Most frequented hour", "Most frequented day"};



        //Day with longest entry-exit period
        //List view result: single entry
        if (choice.equals(analysisChoice[0])) {
            dataTable.setVisibility(View.VISIBLE);

            dataRows.add(dbHelper.findDayWithLongestEntryExitTime(name));
            ArrayAdapter<String> adapter  = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataRows);
            dataTable.setAdapter(adapter);

        //Hour with longest entry-exit period
        //List view result: single entry
        } else if (choice.equals(analysisChoice[1])) {
            dataTable.setVisibility(View.VISIBLE);

            dataRows.add(dbHelper.findHourWithLongestEntryExitTime(name));
            ArrayAdapter<String> adapter  = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataRows);
            dataTable.setAdapter(adapter);

        //Today's hourly entries count
        //Graph view: 24 entries
        } else if (choice.equals(analysisChoice[2])) {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String time=df.format(c.getTime());
            //dataRows=dbHelper.findTodayHourlyEntryCount(name, time.substring(0,10));

            List<DataPoint> dataPoints=new ArrayList<DataPoint>();
            List<Integer> hourlyCount=dbHelper.findTodayHourlyEntryCount(name, time.substring(0,10));
            int[] h= {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24};

            if (!hourlyCount.isEmpty()) {
                //ArrayAdapter<String> adapter  = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataRows);
                //dataTable.setAdapter(adapter);
                graph1.setVisibility(View.VISIBLE);

                for (int i=0; i<hourlyCount.size(); i++) {
                    dataPoints.add(new DataPoint(h[i], hourlyCount.get(i)));
                }

                BarGraphSeries<DataPoint> series = new BarGraphSeries<>(dataPoints.toArray(new DataPoint[0]));
                graph1.addSeries(series);

                // set date label formatter
                graph1.setTitle("Entries per hour");
                graph1.getGridLabelRenderer().setHorizontalAxisTitle("Hours");
                graph1.getGridLabelRenderer().setTextSize(8);
                graph1.getGridLabelRenderer().setNumHorizontalLabels(24); //

                series.setOnDataPointTapListener(new OnDataPointTapListener() {
                    @Override
                    public void onTap(Series series, DataPointInterface dataPoint) {
                        String x=String.valueOf(dataPoint.getX());
                        String y=String.valueOf(dataPoint.getY());
                        Toast.makeText(getApplicationContext(),  "hour "+ x + " number of entries: "+y, Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                dataTable.setVisibility(View.VISIBLE);
                List<String> empty=new ArrayList<>();
                empty.add("There are no detected values for this X-zone");
                ArrayAdapter<String> adapter  = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, empty);
                dataTable.setAdapter(adapter);
            }

        //Observation days entries count
        //Graph view: many entries
        } else if (choice.equals(analysisChoice[3])) {
            DayCount dCount =dbHelper.findDaysEntryCount(name);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

            List<String> days=dCount.getDays();
            List<Integer> count=dCount.getCount();


            List<DataPoint> dataPoints=new ArrayList<DataPoint>();

            if (!days.isEmpty()) {
                graph2.setVisibility(View.VISIBLE);

                for (int i=0; i<days.size(); i++) {
                    try {
                        Date d1=df.parse(days.get(i));
                        dataPoints.add(new DataPoint(d1, count.get(i)));
                    } catch (Exception e) {
                    }

                }

                BarGraphSeries<DataPoint> series = new BarGraphSeries<>(dataPoints.toArray(new DataPoint[0]));
                series.setSpacing(50);

                graph2.addSeries(series);

                // set date label formatter
                graph2.setTitle("Entries per day");
                graph2.getGridLabelRenderer().setHorizontalAxisTitle("Days");
                graph2.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter((this)));
                graph2.getGridLabelRenderer().setNumHorizontalLabels(4); // only 4 because of the space
                graph2.getGridLabelRenderer().setTextSize(8);
                graph2.getViewport().setScalable(true);  // activate horizontal zooming and scrolling
                graph2.getViewport().setScrollable(true);  // activate horizontal scrolling
                graph2.getViewport().setScalableY(true);  // activate horizontal and vertical zooming and scrolling
                graph2.getViewport().setScrollableY(true);  // activate vertical scrolling

                // set manual x bounds to have nice steps

                try {
                    Date d1=df.parse(days.get(0));
                    Date d2=df.parse(days.get(days.size()));
                    graph2.getViewport().setMinX(d1.getTime());
                    graph2.getViewport().setMaxX(d2.getTime());
                    graph2.getViewport().setXAxisBoundsManual(true);
                } catch (Exception e) {
                }

                series.setOnDataPointTapListener(new OnDataPointTapListener() {
                    @Override
                    public void onTap(Series series, DataPointInterface dataPoint) {
                        String y=String.valueOf(dataPoint.getY());
                        Date x=new Date((long)(dataPoint.getX()));
                        DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
                        Toast.makeText(getApplicationContext(), "date: "+df.format(x)+" number of entries: "+y, Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                dataTable.setVisibility(View.VISIBLE);
                dataRows.add("There are no detected values for this X-zone");
                ArrayAdapter<String> adapter  = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataRows);
                dataTable.setAdapter(adapter);
            }

        //Observation hours entries count
        //Graph view: 24 entries
        } else if (choice.equals(analysisChoice[4])) {
            HourCount hCount =dbHelper.findHourlyCount(name);

            List<DataPoint> dataPoints=new ArrayList<DataPoint>();
            int[] h= {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24};
            List<Integer> count=hCount.getCount();

            if (!hCount.getCount().isEmpty()) {
                graph3.setVisibility(View.VISIBLE);

                for (int i=0; i<count.size(); i++) {
                    dataPoints.add(new DataPoint(h[i], count.get(i)));
                }

                BarGraphSeries<DataPoint> series = new BarGraphSeries<>(dataPoints.toArray(new DataPoint[0]));
                graph3.addSeries(series);

                // set date label formatter
                graph3.setTitle("Entries per hour");
                graph3.getGridLabelRenderer().setHorizontalAxisTitle("Hours");
                graph3.getGridLabelRenderer().setTextSize(8);
                graph3.getGridLabelRenderer().setNumHorizontalLabels(24); //

                series.setOnDataPointTapListener(new OnDataPointTapListener() {
                    @Override
                    public void onTap(Series series, DataPointInterface dataPoint) {
                        String x=String.valueOf(dataPoint.getX());
                        String y=String.valueOf(dataPoint.getY());
                        Toast.makeText(getApplicationContext(),  "hour "+ x + " number of entries: "+y, Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                dataTable.setVisibility(View.VISIBLE);
                dataRows.add("There are no detected values for this X-zone");
                ArrayAdapter<String> adapter  = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataRows);
                dataTable.setAdapter(adapter);
            }

        //Most frequented hour
        //List view: single entry
        } else if (choice.equals(analysisChoice[5])) {
            dataTable.setVisibility(View.VISIBLE);
            String result=dbHelper.findMostVisitedHour(name);
            dataRows.add(result);
            ArrayAdapter<String> adapter  = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataRows);
            dataTable.setAdapter(adapter);

        //Most frequented day
        // List view: single entry
        } else if (choice.equals(analysisChoice[6])) {
            dataTable.setVisibility(View.VISIBLE);
            String result=dbHelper.findMostVisitedDay(name);
            dataRows.add(result);
            ArrayAdapter<String> adapter  = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataRows);
            dataTable.setAdapter(adapter);
        }

    }
}