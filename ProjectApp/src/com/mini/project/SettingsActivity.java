package com.mini.project;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import classes.HelperMethods;
import classes.ServerSettings;

public class SettingsActivity extends Activity {

	private EditText ipField, portField;
	private String SERVER_IP = "0.0.0.0";
	private int SERVER_PORT = 9999;
	private ServerSettings settings;


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
		settings = new HelperMethods(this).GetSettings();
		
		if(settings == null) {
			ipField.setText(SERVER_IP);
			portField.setText(Integer.toString(SERVER_PORT));
		} else {
			ipField.setText(settings.getIP());
			portField.setText(Integer.toString(settings.getPORT()));
		}
		
	} 

	public void saveSettings(){
		try {
		if(ipField.getText().toString().equals("") || portField.getText().toString().equals("")){
			Toast.makeText(this, "IP and Port must be set", Toast.LENGTH_SHORT).show();
		} else {
			final SharedPreferences prefs = new HelperMethods(this).getSharedPreferences();
	        SharedPreferences.Editor editor = prefs.edit();
	        editor.putString("IP", ipField.getText().toString());
	        editor.putInt("PORT", Integer.parseInt(portField.getText().toString()));
	        editor.commit();
			Toast.makeText(getApplicationContext(), "Saved settings",
					Toast.LENGTH_SHORT).show();
			
			Intent returnIntent = new Intent();
			setResult(RESULT_OK,returnIntent);     
			finish();
		}
		} catch(Exception ex) {
			Toast.makeText(this, "Error on saving settings: " + ex.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	
}