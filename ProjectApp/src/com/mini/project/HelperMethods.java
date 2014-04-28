package com.mini.project;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import db.DBHelper;
import db.Settings;
import db.SettingsContract.SettingsEntry;

public class HelperMethods {

	public HelperMethods() {
		// TODO Auto-generated constructor stub
	}

	public Settings getSettings(Activity context) {
		try {
			DBHelper mDbHelper = new DBHelper(context);
			SQLiteDatabase db = mDbHelper.getReadableDatabase();

			// Define a projection that specifies which columns from the
			// database
			// you will actually use after this query.
			String[] projection = { SettingsEntry._ID, SettingsEntry.IP_COLUMN,
					SettingsEntry.PORT_COLUMN };

			// How you want the results sorted in the resulting Cursor
			String sortOrder = SettingsEntry.ID + " DESC";

			Cursor c = db.query(SettingsEntry.TABLE_NAME, // The table to query
					projection, // The columns to return
					SettingsEntry.ID + "=" + 1, // The columns for the WHERE clause
					null, // The values for the WHERE clause
					null, // don't group the rows
					null, // don't filter by row groups
					sortOrder // The sort order
					);

			c.moveToFirst();
			long itemId = c.getLong(c.getColumnIndexOrThrow(SettingsEntry._ID));
			String ip = c.getString(c.getColumnIndex(SettingsEntry.IP_COLUMN));
			String port = c.getString(c.getColumnIndex(SettingsEntry.PORT_COLUMN));

			return new Settings(Long.toString(itemId), ip, port);
		} catch (Exception ex) {
			Log.e("Settings", "Error: " + ex.getMessage());
			return null;
		}

	}

}
