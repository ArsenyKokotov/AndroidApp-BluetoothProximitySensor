package com.example.xzone;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.example.xzone.Database.DatabaseHelper;
import com.example.xzone.Model.X_zone;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.core.app.NotificationCompat;
import android.app.NotificationManager;
import android.app.PendingIntent;




public class SetZoneActivity extends AppCompatActivity {  //extends AppCompatActivity

    private static Button submitButton;
    //public static Button submitButton;
    private static Button calibrateButton;
    private static Button stopButton;
    private static Button restartButton;
    private static AutoCompleteTextView zoneName;
    private static AutoCompleteTextView proximityLength;

    protected List<X_zone> xzoneList;
    protected DatabaseHelper dbHelper;

    public static String nameHolder;
    private static Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_zone);
        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button

        submitButton=findViewById(R.id.buttonSubmit);
        calibrateButton=findViewById(R.id.buttonCalibrate);
        //zoneName=findViewById(R.id.editTextZoneName);
        //proximityLength=findViewById(R.id.editTextProximityLength);
        zoneName=findViewById(R.id.autoCompleteTextView_ZoneName);
        proximityLength=findViewById(R.id.autoCompleteTextView_ProximityLength);
        stopButton=findViewById(R.id.buttonStop);
        restartButton=findViewById(R.id.buttonRestart);
        dbHelper = new DatabaseHelper(this);

        mContext=this;

        //send a message to Arduino to calibrate sensor
        calibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (proximityLength.getText().toString().isEmpty() || zoneName.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Fill in the text boxes", Toast.LENGTH_SHORT).show();
                } else if (Integer.parseInt(proximityLength.getText().toString())>500) {
                    Toast.makeText(getApplicationContext(), "Length must be less than 500 cm", Toast.LENGTH_SHORT).show();
                } else {
                    String msg =">" + proximityLength.getText().toString();
                    MainActivity.connectedThread.write(msg);
                }


            }});

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (proximityLength.getText().toString().isEmpty() || zoneName.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Fill in the text boxes", Toast.LENGTH_SHORT).show();
                } else if (Integer.parseInt(proximityLength.getText().toString())>500) {
                    Toast.makeText(getApplicationContext(), "Length must be less than 500 cm", Toast.LENGTH_SHORT).show();
                } else {
                    String msg ="<" + proximityLength.getText().toString();
                    MainActivity.connectedThread.write(msg);
                    submitButton.setEnabled(false);
                    stopButton.setEnabled(true);

                    SharedPreferences sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("name", zoneName.getText().toString());
                    editor.apply();

                    X_zone newZone = new X_zone(zoneName.getText().toString(), proximityLength.getText().toString());
                    dbHelper.insert_Xzone(newZone);
                    dbHelper.createXzoneTable(zoneName.getText().toString());
                    nameHolder=zoneName.getText().toString();

                    Intent serviceIntent = new Intent(mContext, ServiceClass.class);
                    serviceIntent.putExtra("name", zoneName.getText().toString());
                    startService(serviceIntent);

                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.connectedThread.write("stop");
                Intent serviceIntent = new Intent(mContext, ServiceClass.class);
                stopService(serviceIntent);
            }
        });

        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.connectedThread.write("restart");
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        xzoneList=dbHelper.getAllXzones();

        List<String> names = new ArrayList<>();
        List<String> lengths = new ArrayList<>();

        for (int i = 0; i < xzoneList.size(); i++) {
            String temp1=xzoneList.get(i).getName();
            String temp2=xzoneList.get(i).getProximity_length();

            names.add(temp1);
            lengths.add(temp2);
        }

        ArrayAdapter<String> adapter_name =  new ArrayAdapter<String>(this,android.R.layout.select_dialog_singlechoice, names);
        ArrayAdapter<String> adapter_lengths =  new ArrayAdapter<String>(this,android.R.layout.select_dialog_singlechoice, lengths);

        zoneName.setThreshold(1);
        proximityLength.setThreshold(1);

        zoneName.setAdapter(adapter_name);
        proximityLength.setAdapter(adapter_lengths);

        //Second most important piece of Code. GUI Handler
        MainActivity.handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg){
                if (msg.what==2) {
                    String arduinoMsg = msg.obj.toString(); // Read message from Arduino

                    if (arduinoMsg.equals("ok")) {
                        submitButton.setEnabled(true);
                    } else if (arduinoMsg.equals("no")) {
                        Toast.makeText(getApplicationContext(), "There is an immobile object inside the inputted Xzone that interfere with the " +
                                "functioning of the proximity sensor, please remove it.", Toast.LENGTH_SHORT).show();
                    } else if (arduinoMsg.equals("stopped")) {
                        Toast.makeText(getApplicationContext(), "Sensor has stopped.", Toast.LENGTH_SHORT).show();
                    } else if (arduinoMsg.equals("in")) {
                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat df = new SimpleDateFormat("HH");
                        String formattedDate = df.format(c.getTime());
                        int hour = Integer.parseInt(formattedDate);
                        dbHelper.incrementFrequency(nameHolder, hour);
                        addNotification(1);
                    } else if (arduinoMsg.equals("out")) {
                        //somebody exited the zone
                        addNotification(2);
                    } else if (arduinoMsg.equals("stuck")) {
                        //something is stuck inside the zone for about a minute
                        addNotification(3);
                    } else if (arduinoMsg.equals("restarted")) {
                        Toast.makeText(getApplicationContext(), "Sensor has restarted.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

    }

    @Override //creating a back button on application
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override //so that when pressing back button on phone to not disconnect bluetooth and return to main activity
    public void onBackPressed() {
       SetZoneActivity.this.finish();
    }


    public void addNotification(int i) {

        String title="default";
        String content="default";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("HH");
        String formattedDate = df.format(c.getTime());

        if (i==1) {
            title="Penetration notification";
            content="At "+ formattedDate + " hours something entered the zone!";
        } else if (i==2) {
            title="Exit notification";
            content="Something exited the zone!";
        } else if (i==3) {
            title="Stuck notification";
            content="Something is stuck inside the zone! Please clear it to allow application to function!";
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());

    }



    public static Context getContext() {
        return mContext;
    }
}