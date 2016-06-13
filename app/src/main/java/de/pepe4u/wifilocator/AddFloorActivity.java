package de.pepe4u.wifilocator;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import de.pepe4u.wifilocator.logic.WifiLocationService;

/**
 * Simple library to provide indoor navigation and location services.
 *
 * @license    GPL 2 (http://www.gnu.org/licenses/gpl.html)
 * @author     Philipp Neuser <pneuser@physik.fu-berlin.de>
 *
 * Created by Philipp Neuser on 23.07.2015.
 */
public class AddFloorActivity extends Activity {

    private static final int FIND_IMAGE = 1002407;

    WifiLocationService mWifiLocationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWifiLocationService = new WifiLocationService(this);
        setContentView(R.layout.activity_add_floor);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_floor, menu);
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

    public void onAddNewFloor(View view)
    {
        TextView tP = (TextView) findViewById(R.id.txtAFAPicture);
        TextView tN = (TextView) findViewById(R.id.txtAFAName);
        if(tP.getText() != null && tP.getText().toString().isEmpty() == false &&
                tN.getText() != null && tN.getText().toString().isEmpty() == false )
        {
            long newRowId;
            // Add new Floor to Locationservice
            newRowId = mWifiLocationService.addFloor(tN.getText().toString(), tP.getText().toString());
            this.finish();
        }else{
            Toast.makeText(this, "Please fill all fields first.", Toast.LENGTH_SHORT).show();
        }
    }

    public void onBrowseForPicture(View view)
    {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, FIND_IMAGE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FIND_IMAGE && resultCode == Activity.RESULT_OK) {
            if(data != null)
            {
                TextView t = (TextView) findViewById(R.id.txtAFAPicture);
                t.setText(getFileName((Uri) data.getData()));
            }
        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}
