package de.pepe4u.wifilocator;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;

import de.pepe4u.wifilocator.data.WifiLocations;
import de.pepe4u.wifilocator.logic.WifiLocationService;

/**
 * Simple library to provide indoor navigation and location services.
 *
 * @license    GPL 2 (http://www.gnu.org/licenses/gpl.html)
 * @author     Philipp Neuser <pneuser@physik.fu-berlin.de>
 *
 * Created by Philipp Neuser on 23.07.2015.
 */
public class MapWatchActivity extends Activity {

    int curX = 0;
    int curY = 0;
    int floor_id = 0;

    //WifiLocationsDbHelper mDbHelper = new WifiLocationsDbHelper(this);
    //SQLiteDatabase db = null;
    WifiLocationService mWifiLocationService;
    MapCreateView mcv;
    WifiManager wm;
    WifiScanReceiver wsr;

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            // This method does all the work
            wm.startScan();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWifiLocationService = new WifiLocationService(this);

        wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wsr = new WifiScanReceiver();
        registerReceiver(wsr, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        mcv = new MapCreateView(this);

        setContentView(mcv);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuItem item = menu.add("Compare SSID");
        item.setCheckable(true);
        item.setChecked(mWifiLocationService.getCompareSSID());
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.isChecked())
                {
                    mWifiLocationService.setCompareSSID(false);
                    item.setChecked(false);
                }else{
                    mWifiLocationService.setCompareSSID(true);
                    item.setChecked(true);
                }
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class MapCreateView extends View {
        public MapCreateView(Context context) {
            super(context);
        }



        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if(floor_id > 0) {
                Bitmap myFloorPlan = BitmapFactory.decodeFile(new File(mWifiLocationService.getFloorImagePath(floor_id)).getAbsolutePath());
                this.setBackground(new BitmapDrawable(getResources(), myFloorPlan));

                Paint paint = new Paint();
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.RED);
                canvas.drawCircle(curX, curY, 10, paint);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(wsr);
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    public void onResume(){
        super.onResume();
        registerReceiver(wsr, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        timerHandler.postDelayed(timerRunnable, 0);
    }

    public class WifiScanReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {

            ContentValues cPointData = mWifiLocationService.getMyCurrentLocation(wm.getScanResults());

            if(cPointData != null)
            {
                curX = cPointData.getAsInteger(WifiLocations.FloorPointEntry.COLUMN_NAME_POSX); //cPointData.getInt(cPointData.getColumnIndex(WifiLocations.FloorPointEntry.COLUMN_NAME_POSX));
                curY = cPointData.getAsInteger(WifiLocations.FloorPointEntry.COLUMN_NAME_POSY); //cPointData.getInt(cPointData.getColumnIndex(WifiLocations.FloorPointEntry.COLUMN_NAME_POSY));
                floor_id = cPointData.getAsInteger(WifiLocations.FloorPointEntry.COLUMN_NAME_FLOOR_ID); //cPointData.getInt(cPointData.getColumnIndex(WifiLocations.FloorPointEntry.COLUMN_NAME_FLOOR_ID));
            }

            mcv.invalidate();

            timerHandler.postDelayed(timerRunnable, 5000);
        }

    }
}
