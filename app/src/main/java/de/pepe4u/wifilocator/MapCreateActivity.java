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
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import de.pepe4u.wifilocator.data.WifiLocations;
import de.pepe4u.wifilocator.logic.WifiLocationService;


public class MapCreateActivity extends Activity {

    WifiLocationService mWifiLocationService;
    //WifiLocationsDbHelper mDbHelper = new WifiLocationsDbHelper(this);
    //SQLiteDatabase db = null;
    MapCreateView mcv;
    WifiManager wm;
    WifiScanReceiver wsr;
    int floor_id;
    long curFloorPoint = 0;
    float curX = 0;
    float curY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWifiLocationService = new WifiLocationService(this);

        wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wsr = new WifiScanReceiver();
        registerReceiver(wsr, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        Intent i = getIntent();
        floor_id = i.getIntExtra("FLOORID", 0);

        mcv = new MapCreateView(this, floor_id);

        setContentView(mcv);

        Bitmap myFloorPlan;
        myFloorPlan = BitmapFactory.decodeFile(new File(mWifiLocationService.getFloorImagePath(floor_id)).getAbsolutePath());
        mcv.setBackground(new BitmapDrawable(getResources(), myFloorPlan));


    }

    public class MapCreateView extends View {
        final int floor_id;

        public MapCreateView(Context context, final int floor_id) {
            super(context);
            this.floor_id = floor_id;
            this.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if(curX > 0 || curY >0)
                        {
                            Toast.makeText(mcv.getContext(), "Last scan not complete, stay where you are", Toast.LENGTH_SHORT).show();
                            return true;
                        }


                        curX = event.getX();
                        curY = event.getY();

                        long newFloorPointRowId;
                        newFloorPointRowId = mWifiLocationService.addNewCoordinatesToFloor(floor_id,curX,curY);

                        curFloorPoint = newFloorPointRowId;

                        wm.startScan();
                        Toast.makeText(mcv.getContext(),"Scan started", Toast.LENGTH_SHORT).show();

                        v.invalidate();
                        return true;
                    }
                    return false;
                }
            });
        }


        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.RED);

            List<ContentValues> lPointsOnFloor = mWifiLocationService.getListOfCoordinatesOnFloor(floor_id);
            for (ContentValues c : lPointsOnFloor) {
                int x = c.getAsInteger(WifiLocations.FloorPointEntry.COLUMN_NAME_POSX);
                int y = c.getAsInteger(WifiLocations.FloorPointEntry.COLUMN_NAME_POSY);
                canvas.drawCircle(x, y, 10, paint);
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add("Remove last");
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mWifiLocationService.removeLastCoordinatesAddedOnFloor(floor_id);
                mcv.invalidate();
                return true;
            }
        });
        return true;
    }

    protected void onPause() {
        unregisterReceiver(wsr);
        super.onPause();
    }

    protected void onResume() {
        registerReceiver(wsr, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    public class WifiScanReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            for(ScanResult sr : wm.getScanResults())
            {
                mWifiLocationService.addMeasuredValueToCoordinates(floor_id,curFloorPoint,sr.BSSID,sr.SSID,sr.level);
            }

            curY = 0;
            curX = 0;
            curFloorPoint = 0;
            Toast.makeText(mcv.getContext(),"Scan completed.",Toast.LENGTH_SHORT).show();
        }

    }
}
