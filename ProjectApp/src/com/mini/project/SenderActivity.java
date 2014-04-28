package com.mini.project;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import db.Settings;

public class SenderActivity extends Activity implements SensorEventListener {

	private TextView xTV, yTV, zTV;
	private Button sendButton;
	private ToggleButton toggleSending;
	private boolean sendingAuto = false, mInitialized;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private long mDelay = 5000;
	private HelperMethods helper = new HelperMethods();
	private Settings settings;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sender);

		InitializeViews();
		
		settings = helper.getSettings(this);
		if(settings != null)
			Log.i("Settings", settings.getID());
		else
			Log.i("Settings", "Could not find settings in Sender");

		toggleSending
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							// The toggle is enabled
							if(settings != null) {
								sendingAuto = true;
								sendButton.setEnabled(false);
								Toast.makeText(getApplicationContext(),
										"Sending automatically to " + settings.getIP(), Toast.LENGTH_SHORT)
										.show();
								startAutomaticSending();
							}

						} else {
							// The toggle is disabled
							sendingAuto = false;
							sendButton.setEnabled(true);
							Toast.makeText(getApplicationContext(),
									"Stopped sending automatically",
									Toast.LENGTH_SHORT).show();
							stopAutomaticSending();
						}
					}
				});

	}

	@Override
	protected void onResume() {
		super.onResume();
		// register Listener for SensorManager and Accelerometer sensor
		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// unregister Listener for SensorManager
		mSensorManager.unregisterListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sender, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onClick(View view) {

		Intent intent;
		switch (view.getId()) {
		case R.id.SendButton:
			Toast.makeText(getApplicationContext(), "Sent data",
					Toast.LENGTH_SHORT).show();
			break;
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		float x, y, z;
	    // TODO Write measurements of the accelerometer to x, y and z
	    x = event.values[0];
	    y = event.values[1];
	    z = event.values[2];

	    if (!mInitialized) {
	    	xTV.setText("X: 0.0");
	    	yTV.setText("Y: 0.0");
	    	zTV.setText("Z: 0.0");
	    	mInitialized = true;
	    } else { 
	   // TODO Set new value into corresponding TextViews 	
	    	xTV.setText("X: " + Float.toString(x));
	    	yTV.setText("Y: " + Float.toString(y));
	    	zTV.setText("Z: " + Float.toString(z));
	    }

	}

	private Handler mHandler = new Handler();
    private Runnable sendAutomatic = new Runnable() {
        @Override
        public void run() {
        	try {
	        	/*Socket client = new Socket(settings.getIP(), Integer.parseInt(settings.getPORT()));
	        	
	        	String clientMessage;            
		        String serverResponse, finalResult = "";    
				
		        DataOutputStream outToServer = new DataOutputStream(client.getOutputStream());        
		        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(client.getInputStream()));
		 
		        clientMessage = "Heia";
		        outToServer.writeBytes(clientMessage + '\n');
	        	//Toast.makeText(getApplicationContext(), "Sent data", Toast.LENGTH_SHORT).show();
	        	
		        serverResponse = null;
		        
		        while((serverResponse = inFromServer.readLine()) != null)
		        	finalResult += serverResponse + "\n";
		        
		        inFromServer.close();
		        outToServer.close();
		        

	        	Toast.makeText(getApplicationContext(), "Got response " + finalResult, Toast.LENGTH_SHORT).show();
	        	*/
	        	
	        	mHandler.postDelayed(sendAutomatic, mDelay);
        	} catch(Exception ex){
        		
        	}
        }
    };
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	public void InitializeViews() {
		xTV = (TextView) findViewById(R.id.XaxisTV);
		yTV = (TextView) findViewById(R.id.YaxisTV);
		zTV = (TextView) findViewById(R.id.ZaxisTV);
		toggleSending = (ToggleButton) findViewById(R.id.StartAutoButton);
		sendButton = (Button) findViewById(R.id.SendButton);

		// Instantiate SensorManager
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		// Get Accelerometer sensor
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		mInitialized = false;
	}
	
	public void startAutomaticSending(){
		 mHandler.removeCallbacks(sendAutomatic);
	     mHandler.postDelayed(sendAutomatic, mDelay);
	}
	
	public void stopAutomaticSending(){
		mHandler.removeCallbacks(sendAutomatic);
	}
	
}
