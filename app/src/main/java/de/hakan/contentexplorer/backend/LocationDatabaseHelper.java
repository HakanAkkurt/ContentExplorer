package de.hakan.contentexplorer.backend;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class LocationDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "location_history.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "location_history";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    public LocationDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_LATITUDE + " REAL," +
                COLUMN_LONGITUDE + " REAL," +
                COLUMN_TIMESTAMP + " DATETIME)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addLocation(double latitude, double longitude) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LATITUDE, latitude);
        values.put(COLUMN_LONGITUDE, longitude);
        String timestamp = getTimestamp();
        values.put(COLUMN_TIMESTAMP, timestamp);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public ArrayList<String> getAllLocationDataFromDB() {

        ArrayList<String> locationDataList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM location_history", null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        int columnCount = cursor.getColumnCount();
                        StringBuilder rowData = new StringBuilder();

                        for (int i = 0; i < columnCount; i++) {
                            rowData.append(cursor.getString(i)).append(", ");
                        }

                        locationDataList.add(rowData.toString());

                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }

        return locationDataList;
    }

    public void deleteAllEntries() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
        db.close();
    }

    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}

