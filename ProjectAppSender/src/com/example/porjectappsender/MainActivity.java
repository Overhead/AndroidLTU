package com.example.porjectappsender;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import classes.HelperMethods;
import classes.ServerSettings;

public class MainActivity extends Activity implements SensorEventListener {

	private TextView xTV, yTV, zTV, lastSentTV;
	private float x,y,z;
	private Button recordButton;
	private boolean mInitialized;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	JSONArray recordedValues = new JSONArray();
	private long mDelay = 100;
	private final String TAG = "SENDER";
	private ServerSettings settings = null;
	private int RercordDuration = 0;
	private Timer T;
	
	
	private final String AUTH_KEY = "";
	private String SERVER_IP = "";
	private int SERVER_PORT = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		InitializeViews();
		

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
		getMenuInflater().inflate(R.menu.main, menu);
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
			startActivityForResult(intent, 1);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onClick(View view) {

		boolean on = ((ToggleButton) view).isChecked();
		switch (view.getId()) {
		case R.id.RecordButton:
			if(on && settings != null) {
				Toast.makeText(getApplicationContext(), "Started recording", Toast.LENGTH_SHORT).show();
				StartRecording();
			} else if (settings != null){
				Toast.makeText(getApplicationContext(), "Stopped recording", Toast.LENGTH_SHORT).show();
				StopRecording();
			} else {
				ShowSettingsDialog();
			}
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
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

	private void SendRecordedMeasurement() {
		try {
			new AsyncTask<Void, Void, String>() {
				
				@Override
				protected String doInBackground(Void... params) {
					
					String msg = "";
					try {
						Socket socket;
	
						String serverResponse, finalResult = "";
	
						InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
						socket = new Socket(serverAddr, SERVER_PORT);
						
						if(socket.isConnected()) {
						
							PrintWriter outToServer = new PrintWriter(new BufferedWriter(
									new OutputStreamWriter(socket.getOutputStream())), true);
		
							JSONObject data = new JSONObject();
							JSONObject content = new JSONObject();
							content.put("Values", recordedValues);
							content.put("Duration", RercordDuration);
							content.put("Action", "Record");
							content.put("Authorization", AUTH_KEY);
							data.put("Message", content);
		
							Log.i(TAG, "Data sent to server: " + data.toString());
							
							outToServer.println(data);
		
							BufferedReader inFromServer = new BufferedReader(
									new InputStreamReader(socket.getInputStream()));
		
							serverResponse = null;
		
							while ((serverResponse = inFromServer.readLine()) != null)
								finalResult += serverResponse + "\n";
		
							inFromServer.close();
							outToServer.close();
							socket.close();
							RercordDuration = 0;
		
							msg = finalResult;
						
						} else {
							RercordDuration = 0;
							msg = "Could not connect to server";
						}
	
					} catch (IOException | JSONException ex) {
						msg = "Error :" + ex.getMessage();
						// If there is an error, don't just keep trying to register.
						// Require the user to click a button again, or perform
						// exponential back-off.
					}
					return msg;
				}
	
				@Override
				protected void onPostExecute(String msg) {
					lastSentTV.setText(msg + "\n");
				}
			}.execute(null, null, null);
		} catch(Exception ex){
			Log.i(TAG, ex.getMessage());
			lastSentTV.setText(ex.getMessage());
		}
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	public void InitializeViews() {
		xTV = (TextView) findViewById(R.id.XaxisTV);
		yTV = (TextView) findViewById(R.id.YaxisTV);
		zTV = (TextView) findViewById(R.id.ZaxisTV);
		lastSentTV = (TextView)findViewById(R.id.LastMeasureTV);
		recordButton = (Button) findViewById(R.id.RecordButton);
		

		// Instantiate SensorManager
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		// Get Accelerometer sensor
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		mInitialized = false;
		
		
		
		//Check if settings are set, if not, make user set them
		setSettingsObject();
		
	}	
	
	private Handler mHandler = new Handler();
    private Runnable recordAccData = new Runnable() {
        @Override
        public void run() {
        	try {
        		JSONObject xyz = new JSONObject();
				xyz.put("X", x);
				xyz.put("Y", y);
				xyz.put("Z", z);
				recordedValues.put(xyz);
	        	mHandler.postDelayed(recordAccData, mDelay);
        	} catch(Exception ex){
        		Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        	}
        }
    };
    
    public void StartRecording(){
    	recordedValues = new JSONArray();
    	mHandler.removeCallbacks(recordAccData);
	    mHandler.postDelayed(recordAccData, mDelay);
	    StartCounter();
    }
    
    public void StopRecording(){
		try {	
	    	mHandler.removeCallbacks(recordAccData);
	    	T.cancel();
	    	SendRecordedMeasurement();
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	
    
    public void ShowSettingsDialog(){
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Error!");
        builder.setMessage("There is no server settings\nSet them now?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                startActivityForResult(new Intent(getApplicationContext(), SettingsActivity.class), 1);
           }
       });
       builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
        	   dialog.dismiss();
           }
       });
       builder.show();
    }
    
    public void StartCounter(){
    	T=new Timer();
    	T.scheduleAtFixedRate(new TimerTask() {         
    	        @Override
    	        public void run() {
    	            runOnUiThread(new Runnable()
    	            {
    	                @Override
    	                public void run()
    	                {
    	                	RercordDuration++;                
    	                }
    	            });
    	        }
    	    }, 1000, 1000);

    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
            	setSettingsObject();
            }
        }
    }
    
    private void setSettingsObject(){
    	settings = new HelperMethods(this).GetSettings();

		if(settings != null){
			SERVER_IP = settings.getIP();
			SERVER_PORT = settings.getPORT();
		} else {
			ShowSettingsDialog();
		}
    }
  	
}
