package com.example.xzone;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.xzone.Database.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.example.xzone.ForegroundRun.CHANNEL_1_ID;

public class ServiceClass extends Service {

    protected DatabaseHelper dbHelper;

    public static String entry_time;
    public static String exit_time;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DatabaseHelper(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);

        String name = intent.getStringExtra("name");

        Intent notificationIntent = new Intent(this, SetZoneActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID) //use foreground run class
                .setSmallIcon(R.drawable.ic_baseline_looks_one_24)
                .setContentTitle("Xzone")
                .setContentText("Xzone is currently running!")
                .setContentIntent(pendingIntent)
                .build();

        //handle exchange info from bluetooth in the background
        MainActivity.handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg){
                if (msg.what==2) {
                    String arduinoMsg = msg.obj.toString(); // Read message from Arduino

                    if  (arduinoMsg.equals("in")) {
                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                        //String formattedDate = df.format(c.getTime());
                        //int hour = Integer.parseInt(formattedDate);
                        //dbHelper.incrementFrequency(name, hour);
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
                            dbHelper.insert_DetectionData(name, entry_time, exit_time, diff);
                        } catch (Exception e) {
                            //System.out.println("Hello World");
                        }


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

        startForeground(1, notification);
        return START_NOT_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
        }

    }
}
