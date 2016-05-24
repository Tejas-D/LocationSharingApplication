/**
 * File Name:               SQLite.java
 * File Description:        Creating the class that will create and add to the SQLite DB
 *
 * Author:                  Tejas Dwarkaram
 */

package com.example.tejas.finalv2.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.LinkedList;
import java.util.List;

public class SQLite extends SQLiteOpenHelper {

    private static final String database_NAME = "Location_DB";
    private static final int database_VERSION = 1;
    private static final String table_Name = "locationinformation";
    private static final String latitude = "latitude";
    private static final String longitude = "longitude";
    private static final String isAlice = "isAlice";
    private static final String timeSent = "timeSent";

    public SQLite(Context context){
        super(context, database_NAME, null, database_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOCATION_TABLE = "CREATE TABLE locationinformation( " +
                "locationID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "latitude DOUBLE, " +
                "longitude DOUBLE, " +
                "isAlice TEXT," +
                "timeSent TEXT" +
                ")";
        db.execSQL(CREATE_LOCATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS locationinformation");
        this.onCreate(db);
    }

    public void addLocation(LocationInformation locationInformation){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(latitude, locationInformation.getLatitude());
        values.put(longitude, locationInformation.getLongitude());
        values.put(isAlice, locationInformation.getConnectedUser());
        values.put(timeSent, locationInformation.getTimeSent());

        db.insert(table_Name, null, values);

        db.close();
    }

    public List<LocationInformation> getAllLocationDetails(){

        List<LocationInformation> locationInformationList = new LinkedList<>();

        String query = "SELECT * FROM " + table_Name + " ORDER BY locationID DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        LocationInformation locationInformationEntry = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    locationInformationEntry = new LocationInformation();

                    locationInformationEntry.setLocationID(Integer.parseInt(cursor.getString(0)));
                    locationInformationEntry.setLatitude(Double.parseDouble(cursor.getString(1)));
                    locationInformationEntry.setLongitude(Double.parseDouble(cursor.getString(2)));
                    locationInformationEntry.setConnectedUser(cursor.getString(3));
                    locationInformationEntry.setTimeSent(cursor.getString(4));

                    locationInformationList.add(locationInformationEntry);
                } while (cursor.moveToNext());
            }
        }

        return locationInformationList;
    }
}
