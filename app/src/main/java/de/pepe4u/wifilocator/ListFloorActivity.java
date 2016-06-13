package de.pepe4u.wifilocator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import de.pepe4u.wifilocator.data.WifiLocations;
import de.pepe4u.wifilocator.logic.WifiLocationService;

public class ListFloorActivity extends Activity {

    FloorCursorAdapter adapter = null;
    Cursor c = null;

    WifiLocationService mWifiLocationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWifiLocationService = new WifiLocationService(this);
        c = mWifiLocationService.getFloorListCursor();
        adapter = new FloorCursorAdapter(
                this, R.layout.list_item_floor_item, c, 0 );

        setContentView(R.layout.activity_list_floor);
        ListView lv = (ListView)findViewById(R.id.listFloorView);
        lv.setAdapter(adapter);

        // Click listener:
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                Intent intent = new Intent(parent.getContext(), MapCreateActivity.class);
                intent.putExtra("FLOORID",cursor.getInt(cursor.getColumnIndex(WifiLocations.FloorEntry._ID)));
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_floor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_floor) {
            Intent intent = new Intent(this,AddFloorActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_map_watcj) {
            Intent intent = new Intent(this,MapWatchActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public class FloorCursorAdapter extends ResourceCursorAdapter {

        public FloorCursorAdapter(Context context, int layout, Cursor c, int flags) {
            super(context, layout, c, flags);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView name = (TextView) view.findViewById(R.id.txtFloorNameListItem);
            name.setText(cursor.getString(cursor.getColumnIndex(WifiLocations.FloorEntry.COLUMN_NAME_FLOOR_NAME)));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        c = mWifiLocationService.getFloorListCursor();
        adapter = new FloorCursorAdapter(
                this, R.layout.list_item_floor_item, c, 0 );
        ListView lv = (ListView)findViewById(R.id.listFloorView);
        lv.setAdapter(adapter);
    }
}
