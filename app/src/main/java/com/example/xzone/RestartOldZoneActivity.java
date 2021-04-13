package com.example.xzone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.xzone.Database.DatabaseHelper;
import com.example.xzone.Model.X_zone;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.example.xzone.MainActivity.nameHolder;
import static com.example.xzone.ServiceClass.entry_time;
import static com.example.xzone.ServiceClass.exit_time;

public class RestartOldZoneActivity extends AppCompatActivity {

    private static Button buttonCalibrate;
    private static Button buttonSubmit;
    private static Spinner spinnerOldZoneName;
    private static DatabaseHelper dbHelper;
    private static List<X_zone> xzoneList;

    private static String oldZoneName="";
    private static String oldZoneProximityLength;

    private static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restart_old_zone);
        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button

        buttonCalibrate=findViewById(R.id.buttonCalibrateOldZone);
        buttonSubmit=findViewById(R.id.buttonDeleteOldZone);
        spinnerOldZoneName=findViewById(R.id.spinnerOldZoneName);
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
        View view = RestartOldZoneActivity.this.getWindow().getDecorView();
        view.setBackgroundColor(color);

        spinnerOldZoneName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String buffer=spinnerOldZoneName.getSelectedItem().toString();
                oldZoneName="";
                if (!buffer.equals("Choose your Zone-X")) {
                    oldZoneName=buffer;

                    for (int i=0; i<xzoneList.size(); i++) {
                        if (xzoneList.get(i).getName().equals(oldZoneName)) {
                            oldZoneProximityLength=xzoneList.get(i).getProximity_length();
                            break;
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getApplicationContext(), "Select a Zone-X", Toast.LENGTH_SHORT).show();
            }
        });

        buttonCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!oldZoneName.equals("Choose your Zone-X") && !oldZoneName.equals("") ) {
                    String msg =">" + oldZoneProximityLength;
                    MainActivity.connectedThread.write(msg);
                } else {
                    Toast.makeText(getApplicationContext(), "Select a Zone-X", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg ="<" + oldZoneProximityLength;
                MainActivity.connectedThread.write(msg);
                nameHolder=oldZoneName;

                SharedPreferences sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("name", oldZoneName);
                editor.apply();

                Intent serviceIntent = new Intent(mContext, ServiceClass.class);
                stopService(serviceIntent); //stop old service
                serviceIntent.putExtra("name", oldZoneName);
                startService(serviceIntent); //start new service

                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            }
        });


        //Second most important piece of Code. GUI Handler
        MainActivity.handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg){
                if (msg.what==2) {
                    String arduinoMsg = msg.obj.toString(); // Read message from Arduino

                    if (arduinoMsg.equals("ok")) {
                        buttonSubmit.setEnabled(true);
                    } else if (arduinoMsg.equals("no")) {
                        Toast.makeText(getApplicationContext(), "There is an immobile object inside the inputted Xzone that interfere with the " +
                                "functioning of the proximity sensor, please remove it.", Toast.LENGTH_SHORT).show();
                    } else if (arduinoMsg.equals("stopped")) {
                        Toast.makeText(getApplicationContext(), "Sensor has stopped.", Toast.LENGTH_SHORT).show();
                    } else if (arduinoMsg.equals("in")) {
                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                        entry_time=df.format(c.getTime());
                        addNotification(1);
                    } else if (arduinoMsg.equals("out")) {
                        //somebody exited the zone
                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                        exit_time=df.format(c.getTime());
                        try {
                            Date d1=df.parse(entry_time);
                            Date d2=df.parse(exit_time);
                            long diff=d2.getTime()-d1.getTime();
                            dbHelper.insert_DetectionData(nameHolder, entry_time, exit_time, diff);
                        } catch (Exception e) {
                            //System.out.println("Hello World");
                        }
                        addNotification(2);
                    } else if (arduinoMsg.equals("stuck")) {
                        //something is stuck inside the zone for about a minute
                        addNotification(3);
                    }  else if (arduinoMsg.equals("restarted")) {
                        Toast.makeText(getApplicationContext(), "Sensor has restarted.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public void addNotification(int i) {

        if (MainActivity.notificationOnOff==true) {
            String title = "default";
            String content = "default";
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("kk");
            String formattedDate = df.format(c.getTime());

            if (i == 1) {
                Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ getApplicationContext().getPackageName() + "/" + R.raw.alarm);
                title = "Penetration notification";
                content = "At " + formattedDate + " hours something entered the zone!";
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setSound(soundUri)
                        .setVibrate(new long[]{0, 500, 1000})
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE);

                Intent notificationIntent = new Intent(this, MainActivity.class);
                PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(contentIntent);

                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(0, builder.build());
            } else if (i == 2) {
                title = "Exit notification";
                content = "Something exited the zone!";
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
            } else if (i == 3) {
                title = "Stuck notification";
                content = "Something is stuck inside the zone! Please clear it to allow application to function!";
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


            /*
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

             */
        }

    }
}