package com.mini.project;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import db.DBHelper;
import db.Settings;
import db.SettingsContract.SettingsEntry;

public class SettingsActivity extends Activity {

	private EditText ipField, portField;
	private DBHelper mDbHelper;
	private HelperMethods helper = new HelperMethods();
	private Settings settings;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		InitializeViews();
	}

	
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.saveSettingsButton:
			saveSettings();
			break;
		}
	}
	
	public void InitializeViews(){
		ipField = (EditText)findViewById(R.id.SettingsIPET);
		portField = (EditText)findViewById(R.id.SettingsPortET);
		mDbHelper = new DBHelper(this);
		
		settings = helper.getSettings(this);
		
		if(settings != null) {
			ipField.setText(settings.getIP());
			portField.setText(settings.getPORT());
		}
		else {
			ipField.setText("127.0.0.1");
			portField.setText("9999");
		}
	} 
	
	public void saveSettings(){
		if(ipField.getText().toString().equals("") || portField.getText().toString().equals("")){
			Toast.makeText(this, "IP and Port must be set", Toast.LENGTH_SHORT).show();
		} else {
			if(!updateSettings())
				createSettings();
			
			Toast.makeText(getApplicationContext(), "Saved settings",
					Toast.LENGTH_SHORT).show();
			finish();
		}
	}
	
	public boolean createSettings(){
		
		// Gets the data repository in write mode
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(SettingsEntry.ID, "1");
		values.put(SettingsEntry.IP_COLUMN, ipField.getText().toString());
		values.put(SettingsEntry.PORT_COLUMN, portField.getText().toString());

		// Insert the new row, returning the primary key value of the new row
		long newRowId;
		newRowId = db.insert(
		         SettingsEntry.TABLE_NAME,
		         null,
		         values); 
		
		if(newRowId != -1) {
			Log.i("Settings", "Updated settings, rowId: " + newRowId);
			return true;
		}
		else {
			Log.i("Settings", "Could not update settings, rowId: " + newRowId);
			return false;
		}
	}
	
	public boolean updateSettings(){
		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		// New value for one column
		ContentValues values = new ContentValues();
		values.put(SettingsEntry.IP_COLUMN, ipField.getText().toString());
		values.put(SettingsEntry.PORT_COLUMN, portField.getText().toString());

		// Which row to update, based on the ID
		String selection = SettingsEntry.ID + " LIKE ?";
		String[] selectionArgs = { String.valueOf(1) };

		int count = db.update(
		    SettingsEntry.TABLE_NAME,
		    values,
		    selection,
		    selectionArgs);
		
		if(count == 0)
			return false;
		else 
			return true;
	}
	
}
