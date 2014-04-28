package db;

import android.provider.BaseColumns;

public final class SettingsContract {

	public SettingsContract() {
		// TODO Auto-generated constructor stub
	}
	
	/* Inner class that defines the table contents */
    public static abstract class SettingsEntry implements BaseColumns {
        public static final String TABLE_NAME = "Settings";
        public static final String ID = "settingsid";
        public static final String IP_COLUMN = "ip";
        public static final String PORT_COLUMN = "port";
        
    }

}
