package de.pepe4u.wifilocator.logic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;

import java.util.ArrayList;
import java.util.List;

import de.pepe4u.wifilocator.data.WifiLocations;
import de.pepe4u.wifilocator.data.WifiLocationsDbHelper;

/**
 * Simple library to provide indoor navigation and location services.
 *
 * @license    GPL 2 (http://www.gnu.org/licenses/gpl.html)
 * @author     Philipp Neuser <pneuser@physik.fu-berlin.de>
 *
 * Created by Philipp Neuser on 12.06.2016.
 */
public class WifiLocationService {

    Context mContext;
    WifiLocationsDbHelper mDbHelper;
    SQLiteDatabase db;
    Boolean compareSSID = true;


    public Boolean getCompareSSID() {
        return compareSSID;
    }

    public void setCompareSSID(Boolean compareSSID) {
        this.compareSSID = compareSSID;
    }

    public WifiLocationService(Context pContext)
    {
        this.mContext = pContext;
        this.mDbHelper = new WifiLocationsDbHelper(mContext);
        this.db = mDbHelper.getWritableDatabase();
    }

    public long addFloor(String pFloorName, String pPicture)
    {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(WifiLocations.FloorEntry.COLUMN_NAME_FLOOR_NAME, pFloorName);
        values.put(WifiLocations.FloorEntry.COLUMN_NAME_FLOOR_PICTURE, pPicture);

        long newRowId;
        newRowId = db.insert(
                WifiLocations.FloorEntry.TABLE_NAME,
                null,
                values);
        return  newRowId;
    }

    public String getFloorImagePath(int pFloorId)
    {
        // Load image:
        Cursor c = db.rawQuery("SELECT * FROM " + WifiLocations.FloorEntry.TABLE_NAME + " WHERE " + WifiLocations.FloorEntry._ID + " = ?", new String[]{pFloorId + ""});
        c.moveToFirst();
        String picturePath = c.getString(c.getColumnIndex(WifiLocations.FloorEntry.COLUMN_NAME_FLOOR_PICTURE));
        c.close();

        return picturePath;
    }

    public long addNewCoordinatesToFloor(float pFloorId, float pPosX, float pPosY)
    {
        // Create a new Point with its coordinates on given floor
        ContentValues values = new ContentValues();
        values.put(WifiLocations.FloorPointEntry.COLUMN_NAME_POSX, pPosX);
        values.put(WifiLocations.FloorPointEntry.COLUMN_NAME_POSY, pPosY);
        values.put(WifiLocations.FloorPointEntry.COLUMN_NAME_FLOOR_ID, pFloorId);
        return db.insert(
                WifiLocations.FloorPointEntry.TABLE_NAME,
                null,
                values);
    }

    public List<ContentValues> getListOfCoordinatesOnFloor(int pFloorId)
    {
        Cursor c = db.rawQuery("SELECT * FROM " + WifiLocations.FloorPointEntry.TABLE_NAME + " WHERE " + WifiLocations.FloorPointEntry.COLUMN_NAME_FLOOR_ID + " = ?", new String[]{pFloorId + ""});
        List<ContentValues> ret = new ArrayList<>();

        while(c.moveToNext())
        {
            ContentValues map = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(c,map);
            ret.add(map);
        }
        return ret;
    }

    public void removeLastCoordinatesAddedOnFloor(int pFloorId)
    {
        Cursor c1 = db.rawQuery("SELECT * FROM " + WifiLocations.FloorPointEntry.TABLE_NAME + " WHERE " + WifiLocations.FloorPointEntry.COLUMN_NAME_FLOOR_ID + " = ? ORDER BY " + WifiLocations.FloorPointEntry._ID + " DESC", new String[]{pFloorId + ""});
        c1.moveToFirst();
        db.delete(WifiLocations.FloorApPlanEntry.TABLE_NAME, WifiLocations.FloorApPlanEntry.COLUMN_NAME_FLOOR_POINT_ID + " = " + c1.getInt(c1.getColumnIndex(WifiLocations.FloorPointEntry._ID)), null);
        db.delete(WifiLocations.FloorPointEntry.TABLE_NAME, WifiLocations.FloorPointEntry._ID + " = " + c1.getInt(c1.getColumnIndex(WifiLocations.FloorPointEntry._ID)), null);
        c1.close();
    }

    public long addMeasuredValueToCoordinates(int pFloorId, long pCoordinatesId, String pBSSID, String pSSID, int pLevel)
    {
        ContentValues values = new ContentValues();
        values.put(WifiLocations.FloorApPlanEntry.COLUMN_NAME__BSSID, pBSSID);
        values.put(WifiLocations.FloorApPlanEntry.COLUMN_NAME_SSID, pSSID);
        values.put(WifiLocations.FloorApPlanEntry.COLUMN_NAME_STRENGTH, pLevel);
        values.put(WifiLocations.FloorApPlanEntry.COLUMN_NAME_FLOOR_ID, pFloorId);
        values.put(WifiLocations.FloorApPlanEntry.COLUMN_NAME_FLOOR_POINT_ID, pCoordinatesId);

        return db.insert(WifiLocations.FloorApPlanEntry.TABLE_NAME,null,values);
    }

    public Cursor getFloorListCursor()
    {
        return db.rawQuery("SELECT * FROM " + WifiLocations.FloorEntry.TABLE_NAME, null);
    }

    public ContentValues getMyCurrentLocation(List<ScanResult> pListOfScanResults)
    {
        ContentValues ret = null;
        ScanResult strengthBSSID = null;
        for(ScanResult sr : pListOfScanResults)
        {
            if(strengthBSSID == null)
                strengthBSSID = sr;
            else
            if(strengthBSSID.level < sr.level)
                strengthBSSID = sr;
        }

        // Get all floor points for this BSSID
        Cursor cPoints = db.query(true, WifiLocations.FloorApPlanEntry.TABLE_NAME,
                new String[]{WifiLocations.FloorApPlanEntry.COLUMN_NAME_FLOOR_POINT_ID}, WifiLocations.FloorApPlanEntry.COLUMN_NAME__BSSID + " = ? ",
                new String[]{strengthBSSID.BSSID}, null, null, null, null);

        int curPointId = 0;
        int curPointFitness = 0;
        boolean first = true;

        while(cPoints.moveToNext())
        {
            int tmpFitness = 0;
            Cursor cPointBSSIDs = db.rawQuery("SELECT * FROM "+ WifiLocations.FloorApPlanEntry.TABLE_NAME + " WHERE "+ WifiLocations.FloorApPlanEntry.COLUMN_NAME_FLOOR_POINT_ID + " = ? ",new String[]{cPoints.getInt(0)+""});
            while(cPointBSSIDs.moveToNext())
            {
                boolean found = false;
                for(ScanResult sr : pListOfScanResults)
                {
                    if(sr.BSSID.equals(cPointBSSIDs.getString(cPointBSSIDs.getColumnIndex(WifiLocations.FloorApPlanEntry.COLUMN_NAME__BSSID)))
                            && (!compareSSID || sr.SSID.equals(cPointBSSIDs.getString(cPointBSSIDs.getColumnIndex(WifiLocations.FloorApPlanEntry.COLUMN_NAME_SSID))))
                            )
                    {
                        found = true;
                        tmpFitness += Math.abs(Math.abs(sr.level) - Math.abs(cPointBSSIDs.getInt(cPointBSSIDs.getColumnIndex(WifiLocations.FloorApPlanEntry.COLUMN_NAME_STRENGTH))));
                    }
                }

                if(!found)
                {
                    tmpFitness += 50;
                }
            }

            if(first)
            {
                curPointId = cPoints.getInt(0);
                curPointFitness = tmpFitness;
                first = false;
            }else{
                if(tmpFitness < curPointFitness)
                {
                    curPointId = cPoints.getInt(0);
                    curPointFitness = tmpFitness;
                }
            }
        }

        // Get point Data
        Cursor cPointData = db.rawQuery("SELECT * FROM "+ WifiLocations.FloorPointEntry.TABLE_NAME + " WHERE " + WifiLocations.FloorPointEntry._ID + " = ? ", new String[] {curPointId + ""});
        if(cPointData.moveToNext())
        {
            ret = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(cPointData,ret);
        }
        return ret;
    }
}
