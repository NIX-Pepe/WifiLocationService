package de.pepe4u.wifilocator.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Simple library to provide indoor navigation and location services.
 *
 * @license    GPL 2 (http://www.gnu.org/licenses/gpl.html)
 * @author     Philipp Neuser <pneuser@physik.fu-berlin.de>
 *
 * Created by Philipp Neuser on 23.07.2015.
 */
public class WifiLocationsDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 5;
    public static final String DATABASE_NAME = "WifiLocations.db";

    private static final String CREATE_FLOORS = "CREATE TABLE "+ WifiLocations.FloorEntry.TABLE_NAME+" (" +
            WifiLocations.FloorEntry._ID +" INTEGER PRIMARY KEY, " +
            WifiLocations.FloorEntry.COLUMN_NAME_FLOOR_NAME+" TEXT, " +
            WifiLocations.FloorEntry.COLUMN_NAME_FLOOR_PICTURE+" TEXT)";
    private static final String CREATE_FLOOR_AP_PLAN = "CREATE TABLE "+ WifiLocations.FloorApPlanEntry.TABLE_NAME+" ("+ WifiLocations.FloorApPlanEntry._ID
            +" INTEGER PRIMARY KEY, "+ WifiLocations.FloorApPlanEntry.COLUMN_NAME_FLOOR_ID+" INTEGER, " +
            WifiLocations.FloorApPlanEntry.COLUMN_NAME_FLOOR_POINT_ID+" INTEGER, " +
            WifiLocations.FloorApPlanEntry.COLUMN_NAME__BSSID+" TEXT, " +
            WifiLocations.FloorApPlanEntry.COLUMN_NAME_SSID + " TEXT, " +
            WifiLocations.FloorApPlanEntry.COLUMN_NAME_STRENGTH + " INTEGER)";
    private static final String CREATE_FLOOR_POINT = "CREATE TABLE "+ WifiLocations.FloorPointEntry.TABLE_NAME+" (" +
            WifiLocations.FloorPointEntry._ID +" INTEGER PRIMARY KEY, " +
            WifiLocations.FloorPointEntry.COLUMN_NAME_FLOOR_ID+" INTEGER, " +
            WifiLocations.FloorPointEntry.COLUMN_NAME_POSX+" INTEGER, " +
            WifiLocations.FloorPointEntry.COLUMN_NAME_POSY+" INTEGER)";

    public WifiLocationsDbHelper (Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_FLOORS);
        db.execSQL(CREATE_FLOOR_AP_PLAN);
        db.execSQL(CREATE_FLOOR_POINT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nothing to do yet

        db.execSQL("DROP TABLE "+ WifiLocations.FloorEntry.TABLE_NAME);
        db.execSQL("DROP TABLE "+ WifiLocations.FloorApPlanEntry.TABLE_NAME);
        db.execSQL("DROP TABLE "+ WifiLocations.FloorPointEntry.TABLE_NAME);
        db.execSQL(CREATE_FLOORS);
        db.execSQL(CREATE_FLOOR_AP_PLAN);
        db.execSQL(CREATE_FLOOR_POINT);
    }
}
