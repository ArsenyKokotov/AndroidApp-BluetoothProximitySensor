package com.example.xzone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

import com.example.xzone.Database.DatabaseHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import static android.content.ContentValues.TAG;
import static com.example.xzone.ServiceClass.entry_time;
import static com.example.xzone.ServiceClass.exit_time;

public class MainActivity extends AppCompatActivity {

    //buttons
    private static Button connectButton;
    private static Button helpButton;
    private static Button zoneButton;
    private static Button statsButton;
    private static Button continueButton;
    private static Button settingsButton;
    private static Button deleteButton;

    //zone name
    private static TextView zoneName_tv;

    //bluetooth device
    private String deviceName = null;
    private String deviceAddress;
    private static int status=0;

    //bluetooth connection
    public static Handler handler;
    private static Toolbar toolbar;

    public static BluetoothSocket mmSocket;
    public static ConnectedThread connectedThread;
    public static CreateConnectThread createConnectThread;
    private final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update

    protected DatabaseHelper dbHelper;

    public static int TERMS_AND_CONDITIONS_FLAG=0; //sot that terms activity show up only once
    public static String nameHolder;
    public static boolean notificationOnOff=true; //true=on
    public static boolean backgroundWhiteOrBlack=true; //true=white

    private static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectButton=findViewById(R.id.buttonConnect);
        helpButton=findViewById(R.id.buttonHelp);
        zoneButton=findViewById(R.id.buttonZone);
        statsButton=findViewById(R.id.buttonStat);
        continueButton=findViewById(R.id.buttonRestartOldZone);
        settingsButton=findViewById(R.id.buttonSettings);
        deleteButton=findViewById(R.id.buttonDelete);

        zoneName_tv=findViewById(R.id.textViewZoneName);
        toolbar = findViewById(R.id.toolbar);
        dbHelper = new DatabaseHelper(this);
        mContext=this;

        //go to terms activity upon first activation of app
        if (TERMS_AND_CONDITIONS_FLAG==0) {
            Intent intent = new Intent(MainActivity.this, TermsActivity.class);
            startActivity(intent);
        }


        //select Bluetooth device
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               if (status==0) { //if not connected go to select device page
                   Intent intent = new Intent(MainActivity.this, SelectDeviceActivity.class);
                   startActivity(intent);
               } else if (status==1) { //if connected then disconnect
                   if (createConnectThread != null){
                       createConnectThread.cancel();
                       connectButton.setText("Connect");
                       toolbar.setSubtitle("Device is not connected");
                       status=0;
                       Intent serviceIntent = new Intent(mContext, ServiceClass.class);
                       stopService(serviceIntent); //stop old service
                   }
               }

            }
        });

        //go to help page
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, HelpActivity.class);
                startActivity(intent);

            }
        });

        //go to set zone page
        zoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status==1) {
                    Intent intent = new Intent(MainActivity.this, SetZoneActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "To create a new X-zone, please connect a Bluetooth device.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //go to statistics page
        statsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, StatActivity.class);
                startActivity(intent);

            }
        });

        //go to continue page
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status==1) {
                    Intent intent = new Intent(MainActivity.this, RestartOldZoneActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "To observe an X-zone, please connect a Bluetooth device.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //got to settings page
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        //go to delete zone page
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DeleteActivity.class);
                startActivity(intent);
            }
        });



//---------------------------------------------------------------------------------------------------------------------------------//

        // If a bluetooth device has been selected from SelectDeviceActivity
        deviceName = getIntent().getStringExtra("deviceName");
        if (deviceName != null){
            // Get the device address to make BT Connection
            deviceAddress = getIntent().getStringExtra("deviceAddress");
            // Show connection status
            toolbar.setSubtitle("Connecting to " + deviceName + "...");
            connectButton.setEnabled(false);


            //This is the most important piece of code. When "deviceName" is found
            //the code will call a new thread to create a bluetooth connection to the
            //selected device (see the thread code below)

            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            createConnectThread = new CreateConnectThread(bluetoothAdapter,deviceAddress);
            createConnectThread.start();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String name = sharedPreferences.getString("name", "");

        if (name!="") {
            zoneName_tv.setText(name);
        }

        //if bluetooth phone button is off
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if ((!mBluetoothAdapter.isEnabled())) {
            if (createConnectThread != null){
                createConnectThread.cancel();
                connectButton.setText("Connect");
                toolbar.setSubtitle("Device is not connected");
                status=0;
            }
            connectButton.setText("Connect");
            toolbar.setSubtitle("Device is not connected");
        }

        //change background if signaled
        int color;
        if (backgroundWhiteOrBlack==false) {
            color = Color.parseColor("#545657");
        } else {
            color = Color.parseColor("#FFFFFF");
        }
        View view = MainActivity.this.getWindow().getDecorView();
        view.setBackgroundColor(color);

        ///*
        //Second most important piece of Code. GUI Handler
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg){
                switch (msg.what){
                    case CONNECTING_STATUS:
                        switch(msg.arg1){
                            case 1:
                                toolbar.setSubtitle("Connected to " + deviceName);
                                connectButton.setText("Disconnect");
                                connectButton.setEnabled(true);
                                status=1;
                                break;
                            case -1:
                                toolbar.setSubtitle("Device fails to connect");
                                connectButton.setEnabled(true);
                                status=0;
                                break;
                        }
                        break;


                    case MESSAGE_READ:
                        String arduinoMsg = msg.obj.toString(); // Read message from Arduino

                        //when motion is detected add some functionality Sprint 2
                        if (arduinoMsg.equals("in")) {
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
                                dbHelper.insert_DetectionData(nameHolder, entry_time, exit_time, diff);
                            } catch (Exception e) {
                                //System.out.println("Hello World");
                            }
                            addNotification(2);
                        } else if (arduinoMsg.equals("stuck")) {
                            //something is stuck inside the zone for about a minute
                            addNotification(3);
                        }

                        break;







                }
            }
        };


    }
// ============================ Thread to Create Bluetooth Connection =================================== //

    public static class CreateConnectThread extends Thread {

        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address) {

            //Use a temporary object that is later assigned to mmSocket
            //because mmSocket is final.

            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket tmp = null;
            UUID uuid = bluetoothDevice.getUuids()[0].getUuid();

            try {

                //Get a BluetoothSocket to connect with the given BluetoothDevice.
                //Due to Android device varieties,the method below may not work fo different devices.
                //You should try using other methods i.e. :
                //tmp = device.createRfcommSocketToServiceRecord(MY_UUID);

                tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);

            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.cancelDiscovery();
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                Log.e("Status", "Device connected");
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                    Log.e("Status", "Cannot connect to device");
                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.run();
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
                status=0;
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    // =============================== Thread for Data Transfer =========================================== //
    public static class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes = 0; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {

                    //Read from the InputStream from Arduino until termination character is reached.
                    //Then send the whole String message to GUI Handler.

                    buffer[bytes] = (byte) mmInStream.read();
                    String readMessage;
                    if (buffer[bytes] == '\n'){
                        readMessage = new String(buffer,0,bytes);
                        Log.e("Arduino Message",readMessage);
                        handler.obtainMessage(MESSAGE_READ,readMessage).sendToTarget();
                        bytes = 0;
                    } else {
                        bytes++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device //
        public void write(String input) {
            byte[] bytes = input.getBytes(); //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e("Send Error","Unable to send message",e);
            }
        }

        // Call this from the main activity to shutdown the connection //
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
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

    // ============================ Terminate Connection at BackPress ====================== //
    @Override
    public void onBackPressed() {

        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);

    }


}