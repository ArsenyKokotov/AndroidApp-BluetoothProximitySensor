package com.example.xzone.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.xzone.Model.DayCount;
import com.example.xzone.Model.HourCount;
import com.example.xzone.Model.X_zone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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
    public static final String TABLE_DETECT="detection";

    //common columns
    private static final String KEY_NAME="name"; //common to History and Frequency

    //History Table - column names
    private static final String KEY_HISTORY_PROXIMITY="proximity";

    //DETECT Table - columns names
    public static final String KEY_ENTRY_TIME="entry_time";
    public static final String KEY_EXIT_TIME="exit_time";
    public static final String KEY_SPENT_TIME="spent_time";

    // Table Create Statements
    // HISTORY table create statement

    private static final String CREATE_TABLE_HISTORY ="CREATE TABLE " + TABLE_HISTORY + "(" + KEY_NAME + " TEXT," + KEY_HISTORY_PROXIMITY
            + " TEXT" + ")";
    public static final String CREATE_TABLE_DETECT="CREATE TABLE " + TABLE_DETECT + "(" + KEY_NAME + " TEXT," + KEY_ENTRY_TIME
            + " TEXT," + KEY_EXIT_TIME + " TEXT," + KEY_SPENT_TIME + " INT" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating required tables
        db.execSQL(CREATE_TABLE_HISTORY);
        db.execSQL(CREATE_TABLE_DETECT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DETECT);

        // create new tables
        onCreate(db);
    }

    //--------------------------------------------------------------------------------------------//

    //insert data row into table
    public long insert_DetectionData(String name, String entry_time, String exit_time, long difference) {
        long id=-1;
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_NAME, name);
        contentValues.put(KEY_ENTRY_TIME, entry_time);
        contentValues.put(KEY_EXIT_TIME, exit_time);
        contentValues.put(KEY_SPENT_TIME, difference);

        try {
            id = db.replaceOrThrow(TABLE_DETECT, null, contentValues);
        } catch(SQLiteException e) {
            Log.d(TAG, "Exception: " + e.getMessage());
            Toast.makeText(context, "Operation Failed: "+ e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            db.close();
        }
        return id;
    }

    public void deleteZone(String id){
        SQLiteDatabase db=this.getWritableDatabase();
        db.delete(TABLE_HISTORY,"name = ?",new String[] {id});
        db.delete(TABLE_DETECT,"name = ?",new String[] {id});
    }

    public String findDayWithLongestEntryExitTime(String Zone_name) {
        String statement = "There are no detected values for this X-zone.";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String day="";
        int buffer_diff=0;

        try {
            cursor = db.query(TABLE_DETECT, null, null, null, null, null,  null);

            if(cursor != null)
            {
                if(cursor.moveToFirst())
                {

                    do{
                        //getting information from cursor

                         String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                         String entry = cursor.getString(cursor.getColumnIndex(KEY_ENTRY_TIME));
                         int diff = cursor.getInt(cursor.getColumnIndex(KEY_SPENT_TIME));

                         //this will find only a single value
                         if (Zone_name.equals(name) && diff>buffer_diff) { //comparing the times spent inside the zone to find the biggest one
                             day=entry.substring(0,10); //get YYYY-MM_dd out of the string
                             buffer_diff=diff;
                         }

                    } while(cursor.moveToNext());

                    if (buffer_diff>0) {

                        if (buffer_diff>60000) {
                            statement="The day with the longest time spent inside the zone was: "+ day + ", time spent inside: "+ buffer_diff/60000+ " (min)";
                        }
                        else if (buffer_diff>1000) {
                            statement="The day with the longest time spent inside the zone was: "+ day + ", time spent inside: "+ buffer_diff/1000+ " (s)";
                        }
                        else {
                            statement="The day with the longest time spent inside the zone was: "+ day + ", time spent inside: "+ buffer_diff+ " (ms)";
                        }

                    }
                    return statement;
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
        return statement;
    }

    public String findHourWithLongestEntryExitTime(String Zone_name) {
        String statement = "There are no detected values for this X-zone.";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String day="";
        int buffer_diff=0;

        try {
            cursor = db.query(TABLE_DETECT, null, null, null, null, null,  null);

            if(cursor != null)
            {
                if(cursor.moveToFirst())
                {

                    do{
                        //getting information from cursor

                        String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                        String entry = cursor.getString(cursor.getColumnIndex(KEY_ENTRY_TIME));
                        int diff = cursor.getInt(cursor.getColumnIndex(KEY_SPENT_TIME));

                        //this will find only a single value
                        if (Zone_name.equals(name) && diff>buffer_diff) { //comparing the times spent inside the zone to find the biggest one
                            day=entry.substring(11,13); //HH out of the string
                            buffer_diff=diff;
                        }

                    } while(cursor.moveToNext());

                    if (buffer_diff>0) {
                        if (buffer_diff>60000) {
                            statement="The day with the longest time spent inside the zone was: "+ day + ", time spent inside: "+ buffer_diff/60000+ " (min)";
                        }
                        else if (buffer_diff>1000) {
                            statement="The day with the longest time spent inside the zone was: "+ day + ", time spent inside: "+ buffer_diff/1000+ " (s)";
                        }
                        else {
                            statement="The day with the longest time spent inside the zone was: "+ day + ", time spent inside: "+ buffer_diff+ " (ms)";
                        }
                    }
                    return statement;
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
        return statement;
    }

    public List<Integer> findTodayHourlyEntryCount(String Zone_name, String today) {

            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = null;


            int[] hour={1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24};
            int[] count={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

            try {
                cursor = db.query(TABLE_DETECT, null, null, null, null, null,  null);

                if(cursor != null)
                {
                    if(cursor.moveToFirst())
                    {

                        List<Integer> hourlyList=new ArrayList<>();;

                        do{

                            String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                            String entry = cursor.getString(cursor.getColumnIndex(KEY_ENTRY_TIME));

                            if (Zone_name.equals(name) && entry.substring(0,10).equals(today)) { //select by zone name and by zone's observation day
                                for (int i=0; i<hour.length; i++) {
                                    if (Integer.parseInt(entry.substring(11,13))==hour[i]) { //select by zone's observation hour
                                        count[i]+=1;
                                    }
                                }
                            }

                        } while(cursor.moveToNext());

                        for (int i=0; i<hour.length; i++) {
                            //String statement=hour[i]+"h: "+count[i];
                            //hourlyList.add(statement);
                            hourlyList.add(count[i]);
                        }
                        return hourlyList;
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

    public DayCount findDaysEntryCount(String Zone_name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        List<String> dayList=new ArrayList<>();
        List<Integer> dayCount=new ArrayList<>();
        DayCount dcResults=new DayCount();

        String buffer_day="";
        int buffer_count=0;
        int same_day=0;

        try {
            cursor = db.query(TABLE_DETECT, null, null, null, null, null,  null);

            if(cursor != null)
            {
                if(cursor.moveToFirst())
                {

                    do {

                        String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                        String entry = cursor.getString(cursor.getColumnIndex(KEY_ENTRY_TIME));

                        if (Zone_name.equals(name)) { //select by zone name and by zone's observation day
                            if (buffer_day.equals("")) {  //find first day of zone under observation
                                buffer_day = entry.substring(0, 10);
                                buffer_count = 1;
                            } else if (buffer_day.equals(entry.substring(0, 10))) { //if its the same day but a different time
                                ++buffer_count;
                            } else if (!buffer_day.equals(entry.substring(0, 10))) { //if its a new day
                                dayCount.add(buffer_count);
                                dayList.add(buffer_day);
                                buffer_day = entry.substring(0, 10);
                                buffer_count = 1;
                            }
                        }

                    } while(cursor.moveToNext());

                     dayCount.add(buffer_count);
                     dayList.add(buffer_day);


                    dcResults.setDays(dayList);
                    dcResults.setCount(dayCount);

                    return dcResults;
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
        return dcResults;
    }




    public HourCount findHourlyCount(String Zone_name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        String[] hours = {"01", "02", "03","04", "05", "06","07", "08", "09","10", "11", "12","13", "14", "15",
                          "16", "17", "18","19", "20", "21","22", "23", "24"};
        int[] count={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,};

        HourCount hcResults=new HourCount();

        //List<String> hourCountList=new ArrayList<>();

        try {
            cursor = db.query(TABLE_DETECT, null, null, null, null, null,  null);

            if(cursor != null)
            {
                if(cursor.moveToFirst())
                {

                    do{

                        String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                        String entry = cursor.getString(cursor.getColumnIndex(KEY_ENTRY_TIME));

                        if (Zone_name.equals(name)) {
                            for (int i=0; i<hours.length; i++) { //loop through the hour array to compare hour values
                                if (entry.substring(11,13).equals(hours[i])) { //once an equal hour is found increment count at right index and break loop
                                    count[i]+=1;
                                    break;
                                }
                            }
                        }

                    } while(cursor.moveToNext());

                    List<String> h=new ArrayList<>();
                    List<Integer> c=new ArrayList<>();

                    for (int i=0; i<count.length; i++) {
                       h.add(hours[i]);
                       c.add(count[i]);
                    }

                    hcResults.setHours(h);
                    hcResults.setCount(c);

                    return hcResults;
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
        return hcResults;
    }

    public String findMostVisitedHour(String Zone_name) {
        String result="No motion was detected in this zone";
        HourCount buffer=findHourlyCount(Zone_name);
        List<String> hours=buffer.getHours();
        List<Integer> count=buffer.getCount();

        if (!hours.isEmpty()) {
            int index=count.indexOf(Collections.max(count,null));
            result="The most visited hour is "+hours.get(index) + " with an entry count value of " + count.get(index);
            return result;
        }
        return result;

    }

    public String findMostVisitedDay(String Zone_name) {
        String result="No motion was detected in this zone";
        DayCount buffer=findDaysEntryCount(Zone_name);
        List<String> days=buffer.getDays();
        List<Integer> count=buffer.getCount();

        if (!days.isEmpty()) {
            int index=count.indexOf(Collections.max(count,null));
            result="The most visited day is "+days.get(index) + " with an entry count value of " + count.get(index);
            return result;
        }
        return result;

    }



    //-------------------------------------------------------------------------------------------//
    //-------------------------------------------------------------------------------------------//
    //-------------------------------------------------------------------------------------------//

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


}
