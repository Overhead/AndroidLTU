package com.mini.project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class RecieverActivity extends Activity {

	public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private Socket socket;

	private static int SERVERPORT = 0;
	private static String SERVER_IP = "";
	
    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     * http://developer.android.com/google/gcm/gs.html
     */
    String SENDER_ID = "";
	private final String AUTH_KEY="";

    /**
     * Tag used on log messages.
     */
    static final String TAG = "GCMDemo";

    TextView mDisplay, regView, lastValueTV;
    ToggleButton regButton;
    Button getLastMeasureButton, showLastMeasureButton;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    Context context;

    String regid;
    
    private int MeasureID = 0;
    private JSONObject lastJSON;
    private ServerSettings settings = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reciever);

		try {
			mDisplay = (TextView)findViewById(R.id.gcmView);
			regView = (TextView)findViewById(R.id.RegisteredView);
			lastValueTV = (TextView)findViewById(R.id.LastValueTV);
			regButton = (ToggleButton)findViewById(R.id.GcmRegisterButton);
			getLastMeasureButton = (Button)findViewById(R.id.GetLatestMeasure);
			showLastMeasureButton = (Button)findViewById(R.id.ShowAnimationButton);
			
			setSettingsObject();
			
			context = getApplicationContext();
			checkIntent();
			getLastJSON();
		} catch(Exception ex) {
			Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
        // Check device for Play Services APK.
        checkPlayServices();
        checkIntent();
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
		
		switch (view.getId()) {
		case R.id.GcmRegisterButton:
			boolean on = ((ToggleButton) view).isChecked();
			if(on) {
				// Check device for Play Services APK. If check succeeds, proceed with GCM registration.
				if(settings != null) {
			        if (checkPlayServices()) {
			            gcm = GoogleCloudMessaging.getInstance(this);
			            regid = getRegistrationId(context);
		
			            if (regid.isEmpty()) {
			                registerInBackground();
			            	regButton.setEnabled(false);
			            	regButton.setVisibility(View.GONE);
			            } else {
			            	Toast.makeText(context, "App is already registered", Toast.LENGTH_SHORT).show();
			            	regView.setText("Already Registered");
			            	regButton.setEnabled(false);
			            	regButton.setVisibility(View.GONE);
			            	regView.setTextSize(20);
			            }
			        } else { //IF playservice
			            Log.i(TAG, "No valid Google Play Services APK found.");
			        }
				} else { //IF Settings != null
					ShowSettingsDialog();
				}
			} else { //If ON
				Toast.makeText(context, "Unregistered", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.ShowAnimationButton:
			//Reference to code
			//http://www.samuelfrank.me/animating-on-a-surfaceview-using-sensors/
			Intent intent = new Intent(context, AnimateSensorActivity.class);
			intent.putExtra("JSON", new HelperMethods(context).readFromFile("LastMeasure.txt"));
			startActivity(intent);
			break;
			
		case R.id.GetLatestMeasure:
			try {
				getLastJSON();
			} catch(Exception ex){
				Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}
	
	/***
	 * If there is a bundle with field called MeasurementID, the global variable is set and the latest data is downloaded from server
	 */
	private void checkIntent(){
		Intent data = getIntent();
		Bundle extras = data.getBundleExtra("BUNDLE");
		
		if(extras != null)
		{
			MeasureID = Integer.parseInt(extras.getString("MeasurementID", "0"));
			Log.i(TAG, "Got this ID: " + MeasureID);
			if(MeasureID != 0) {
				lastValueTV.setText(getResources().getString(R.string.UpdatedLastValues) + "\n" +  MeasureID);
				GetLatestMeasurement();
			}
		}
	}
	
	private void getLastJSON() {
		try {
			lastJSON = new JSONObject(new HelperMethods(getApplicationContext()).readFromFile("LastMeasure.txt"));
			
			if(lastJSON != null || lastJSON.equals("")) {

				String time = lastJSON.getString("Time");
				int duration = lastJSON.getInt("Duration");
				int mean = lastJSON.getInt("Mean");
				int id = lastJSON.getInt("MeasurementID");
				lastValueTV.setText(getResources().getString(R.string.LastValues) + " \n ID: " + id
						 + "\nMean: " + mean + "\nDuration: " + duration + "\nDate: " + time);
				showLastMeasureButton.setEnabled(true);
			} else {
				lastValueTV.setText(getResources().getString(R.string.LastValues) + " : No ID found!");
				showLastMeasureButton.setEnabled(false);
			}
		} catch(Exception ex){
			Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
			lastValueTV.setText(getResources().getString(R.string.LastValues) + " : No ID found!");
			showLastMeasureButton.setEnabled(false);
		}
	}
	
	/**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = new HelperMethods(this).getSharedPreferences();
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
    
    /**
     * Removes the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void removeRegistrationId(Context context, String regId){
    	final SharedPreferences prefs = new HelperMethods(this).getSharedPreferences();
    	int appVersion = getAppVersion(context);
    	Log.i(TAG, "Removing regId on app version " + appVersion);
    	SharedPreferences.Editor editor = prefs.edit();
    	editor.remove(PROPERTY_REG_ID);
    	editor.remove(PROPERTY_APP_VERSION);
    	editor.commit();
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = new HelperMethods(this).getSharedPreferences();
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);
    }
    
    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Sends the registration ID to your server over Socket, so it can use GCM/HTTP or CCS to send
     * messages to your app.
     */
    private void sendRegistrationIdToBackend() {
      // Your implementation here.
    	try {
	    	new AsyncTask<Void, Void, String>() {
	    		String finalResult = ""; 
	            @Override
	            protected String doInBackground(Void... params) {
	                String msg = "";
	                try {
	                	String serverResponse;
	                	
	                	InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
	            		socket = new Socket(serverAddr, SERVERPORT);
	            		Log.i(TAG, "Sending to " + serverAddr + ":"+SERVERPORT);
	            		PrintWriter outToServer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
	            		
	            		JSONObject data = new JSONObject();
	            		JSONObject content = new JSONObject();
	            		content.put("REGID", regid);
	            		content.put("Action", "REGISTER");
	            		content.put("Authorization", AUTH_KEY);
	            		data.put("Message", content);
	            		
	            		
	            		outToServer.println(data);
	            		
	            		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	            		
	            		serverResponse = null;
	                    
	                    while((serverResponse = inFromServer.readLine()) != null)
	                    	finalResult += serverResponse + "\n";
	                    
	            		
	                    inFromServer.close();
	                    outToServer.close();
	                    
	                } catch (IOException ex) {
	                	regView.setText(getResources().getString(R.string.NotregisteredGCM));
	                	Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
	                } catch (JSONException e) {
	                	regView.setText(getResources().getString(R.string.NotregisteredGCM));
	                	Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
					}
	                return null;
	            }
	
	            @Override
	            protected void onPostExecute(String msg) {
	    	    	regView.setText(getResources().getString(R.string.RegisteredGCM) + "\nServer response: "+finalResult);
	            }
	        }.execute(null, null, null);
    	} catch(Exception ex){
    		regView.setText(getResources().getString(R.string.NotregisteredGCM));
    	}
    }
    
    
    /**
     * Sends a "GETDATA" request over socket to TCPServer and receives the latest Measurement that was published about over GCM.
     */
    private void GetLatestMeasurement(){
    	// Your implementation here.
	    	new AsyncTask<Void, Void, String>() {
	    		String finalResult = ""; 
                JSONObject json = null;
                JSONArray jsonArr = null;
	            @Override
	            protected String doInBackground(Void... params) {
	                String msg = "";
	                try {
	                	String serverResponse;
	                	
	                	InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
	            		socket = new Socket(serverAddr, SERVERPORT);
	            		Log.i(TAG, "Sending to " + serverAddr + ":"+SERVERPORT);
	            		PrintWriter outToServer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
	            		
	            		JSONObject data = new JSONObject();
	        			JSONObject content = new JSONObject();
	        			content.put("ID", MeasureID);
	        			content.put("Action", "GETDATA");
	        			content.put("Authorization",AUTH_KEY);
	        			data.put("Message", content);
	            		
	            		outToServer.println(data);
	            		
	            		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	            		
	            		serverResponse = null;
	                    
	                    while((serverResponse = inFromServer.readLine()) != null)
	                    	finalResult += serverResponse + "\n";
	                    
	            		new HelperMethods(getApplicationContext()).writeToFile(new JSONObject(finalResult).toString(), "LastMeasure.txt");
	                    
	            	    json = new JSONObject(finalResult);
	            	    jsonArr = json.getJSONArray("Values");
	            		
	                    inFromServer.close();
	                    outToServer.close();
	                    
	                } catch (IOException ex) {
	                	regView.setText(getResources().getString(R.string.NotregisteredGCM));
	                	Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
	                } catch (JSONException e) {
	                	regView.setText(getResources().getString(R.string.NotregisteredGCM));
	                	Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
					}
	                return null;
	            }
	
	            @Override
	            protected void onPostExecute(String msg) {
	            	try {
	            	lastValueTV.setText(getResources().getString(R.string.LastValues) + "\n: "+ json.getInt("MeasurementID"));
	            	} catch(Exception ex){
	            		lastValueTV.setText(ex.getMessage());
	                	Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
	            	}
	            }
	        }.execute(null, null, null);
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
			SERVERPORT = settings.getPORT();
		} else {
			ShowSettingsDialog();
		}
    }
    
}
