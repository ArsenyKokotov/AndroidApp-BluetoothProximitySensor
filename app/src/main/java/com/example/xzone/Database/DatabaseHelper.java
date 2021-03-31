package com.example.xzone.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.example.xzone.Model.X_zone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Logcat tag
    private static final String LOG = "DatabaseHelper";
    private static final String TAG = "DatabaseHelper";
    private Context context;

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "X_zone";

    // Table Names
    private static final String TABLE_HISTORY = "history";
    private static final String TABLE_DATA = "data";

    //common columns
    private static final String KEY_NAME="name"; //common to History and Frequency

    //History Table - column names
    private static final String KEY_HISTORY_PROXIMITY="proximity";

    //Data Table - column names
    private static final String KEY_DATA_HOUR="hour";
    private static final String KEY_DATA_FREQUENCY="frequency";

    // Table Create Statements
    // HISTORY table create statement

    private static final String CREATE_TABLE_HISTORY ="CREATE TABLE " + TABLE_HISTORY + "(" + KEY_NAME + " TEXT," + KEY_HISTORY_PROXIMITY
            + " TEXT" + ")";
    private static final String CREATE_TABLE_DATA ="CREATE TABLE " + TABLE_DATA + "(" + KEY_NAME + " TEXT," + KEY_DATA_HOUR
            + " INTEGER," + KEY_DATA_FREQUENCY + " INTEGER" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating required tables
        db.execSQL(CREATE_TABLE_HISTORY);
        db.execSQL(CREATE_TABLE_DATA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA);

        // create new tables
        onCreate(db);
    }

    public long insert_Xzone(X_zone Xzone) {
        long id=-1;

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_NAME, Xzone.getName());
        contentValues.put(KEY_HISTORY_PROXIMITY, Xzone.getProximity_length());

        try {
            id = db.replaceOrThrow(TABLE_HISTORY, null, contentValues);
        } catch(SQLiteException e) {
            Log.d(TAG, "Exception: " + e.getMessage());
            Toast.makeText(context, "Operation Failed: "+ e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            db.close();
        }

        return id;
    }

    //insert into the DATA table 24 rows each representing an hour.
    public void createXzoneTable(String name) {
        int a=-1;
        int[] hour={1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24};
        int initial_frequency=0;
        SQLiteDatabase db = this.getWritableDatabase();


        for (int i=0; i<hour.length; i++) {

            ContentValues someValues = new ContentValues();
            someValues.put(KEY_NAME, name);
            someValues.put(KEY_DATA_HOUR, hour[i]);
            someValues.put(KEY_DATA_FREQUENCY,0);
            db.insert(TABLE_DATA, null, someValues);

            //String DATA_ROW_CREATION = "INSERT INTO " + TABLE_DATA + "(" + KEY_NAME +","+KEY_DATA_HOUR+","+KEY_DATA_FREQUENCY+")"+"VALUES("+
                    //name +"," + hour[i] + "," + initial_frequency + ");";
            //db.execSQL(DATA_ROW_CREATION);
        }
    }

    //increment frequency inside a specific zone during a specific hour
    public void incrementFrequency(String name, int hour) {
        SQLiteDatabase db = this.getWritableDatabase();
        String UPDATE_DATA="UPDATE " +TABLE_DATA+" SET " + KEY_DATA_FREQUENCY + " =" + KEY_DATA_FREQUENCY+ "+1 WHERE "+ KEY_NAME+"='"+ name +
                "' AND "+ KEY_DATA_HOUR + "=" + hour +";";
        db.execSQL(UPDATE_DATA);
    }

    public List<X_zone> getAllXzones() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {

            cursor = db.query(TABLE_HISTORY, null, null, null, null, null,  null);

            if(cursor != null)
            {
                if(cursor.moveToFirst())
                {
                    List<X_zone> zoneList = new ArrayList<>();

                    do{
                        //getting information from cursor

                        String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                        String proximity_length = cursor.getString(cursor.getColumnIndex(KEY_HISTORY_PROXIMITY));

                        //creating a new Course object with the information
                        //adding this course object to courseList

                        zoneList.add(new X_zone(name, proximity_length));

                    } while(cursor.moveToNext());

                    return zoneList;
                }
            }
        }
        catch(Exception e){
            Log.d(TAG, "Exception: " + e.getMessage());
        } finally{
            if(cursor != null)
                cursor.close();

            db.close();
        }
        return Collections.emptyList();
    }

    public List<String> getFreq(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM data WHERE name='" + name+"'";
        Cursor cursor = db.rawQuery(query, null);

        try {
            if (cursor.moveToFirst() && cursor != null) {

                List<String> freqList = new ArrayList<>();

                do {
                    String freq = cursor.getString(cursor.getColumnIndex(KEY_DATA_FREQUENCY));
                    freqList.add(freq);
                } while (cursor.moveToNext());

                return freqList;
            }

        } catch(Exception e){
            Log.d(TAG, "Exception: " + e.getMessage());
        } finally{
            if(cursor != null)
                cursor.close();

            db.close();
        }
        return Collections.emptyList();
    }


}
