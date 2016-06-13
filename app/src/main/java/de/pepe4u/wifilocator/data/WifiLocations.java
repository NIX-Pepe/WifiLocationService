package de.pepe4u.wifilocator.data;

import android.provider.BaseColumns;

/**
 * Created by PEPE on 23.07.2015.
 */
public class WifiLocations {
    public static abstract class FloorEntry implements BaseColumns {
        public static final String TABLE_NAME = "FLOORS";
        public static final String COLUMN_NAME_FLOOR_ID = "FLOORID";
        public static final String COLUMN_NAME_FLOOR_NAME = "NAME";
        public static final String COLUMN_NAME_FLOOR_PICTURE = "PICTURE";
    }
    public static abstract class FloorApPlanEntry implements BaseColumns {
        public static final String TABLE_NAME = "FLOOR_AP_PLAN";
        public static final String COLUMN_NAME_ID = "FAPID";
        public static final String COLUMN_NAME_FLOOR_ID = "FLOORID";
        public static final String COLUMN_NAME_FLOOR_POINT_ID = "FLOORPOINTID";
        public static final String COLUMN_NAME__BSSID = "BSSID";
        public static final String COLUMN_NAME_SSID = "SSID";
        public static final String COLUMN_NAME_STRENGTH = "STRENGHT";
    }

    public static abstract class FloorPointEntry implements BaseColumns {
        public static final String TABLE_NAME = "FLOOR_POINT";
        public static final String COLUMN_NAME_FLOOR_ID = "FLOORID";
        public static final String COLUMN_NAME_POSX = "POSX";
        public static final String COLUMN_NAME_POSY = "POSY";
    }
}
