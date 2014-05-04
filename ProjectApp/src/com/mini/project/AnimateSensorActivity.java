package com.mini.project;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import classes.AccData;
import classes.Measurement;

import com.mini.project.AnimationView.AnimationThread;

/**
 * This is the Activity that holds and runs the view we care about, the
 * {@code AnimationView}.
 * 
 * @author Samuel Frank Smith
 * 
 */
public class AnimateSensorActivity extends Activity {

	/** Surface holder for drawing our frames */
	AnimationView animateView;

	/** Animation loop in which positions are updated and drawn to the canvas. */
	AnimationThread animationThread;

	/** Manager used for detecting changes to the phone's direction and gravity */
	SensorManager sensorManager;
	
	private List<Measurement> measurementList = new ArrayList<Measurement>();
	private final int ID = 29;
    public static final String TAG = "GCMDemo";
    public int counter = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gyro);


		try {
			Intent data = getIntent();
			
			if(data != null)
			{
				JSONObject json = new JSONObject(data.getStringExtra("JSON"));
				PopulateMeasurementList(json);
			}
			
	
			startAnimation();

	    	mHandler.removeCallbacks(recordAccData);
		    mHandler.postDelayed(recordAccData, 100);
			
		} catch(Exception ex){
			Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		animationThread.setRunning(true);
	}

	@Override
	protected void onPause() {
		animationThread.setRunning(false);
		super.onPause();
	}

	private void startAnimation() {
		animateView = (AnimationView) findViewById(R.id.animate_view);
		animationThread = animateView.getThread();
	}

	private void PopulateMeasurementList(JSONObject extras) {
		try {
				String id = extras.getString("MeasurementID");
				String time = extras.getString("Time");
				int duration = extras.getInt("Duration");
				int mean = extras.getInt("Mean");
				JSONArray jsonList = new JSONArray(extras.getString("Values"));
	        	Log.i(TAG, "Duration " + duration);
	        	Log.i(TAG, "Mean " + mean);
	        	Log.i(TAG, "Time " + time.toString());
				//Log.i(TAG, jsonList.toString());
				Measurement measure = new Measurement(time, mean, duration, populateAccDataList(jsonList, id));
				measurementList.add(measure);
		}catch(Exception ex){
			Log.e(TAG, "Populate measure " + ex.getMessage());
			Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	
	private List<AccData> populateAccDataList(JSONArray jsonList, String ID) {
		try {
			List<JSONObject> list = new ArrayList<JSONObject>();
			List<AccData> accList = new ArrayList<AccData>();
			for(int i=0; i<jsonList.length();i++){
				list.add((JSONObject)jsonList.get(i));
			}
			Log.i(TAG, "JSON Size: " + list.size());
			for(int i=0; i<list.size(); i++){
	
				String x = Double.toString(list.get(i).getDouble("X"));
				String y = Double.toString(list.get(i).getDouble("Y"));
				String z = Double.toString(list.get(i).getDouble("Z"));
				AccData acc = new AccData(x,y,z,Integer.parseInt(ID));
				accList.add(acc);
			}
			Log.i(TAG, accList.get(0).getX());
			return accList;
		} catch(Exception ex){
			Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
			Log.e(TAG, "Populate acc" + ex.getMessage());
			return null;
		}
	}
	
	private Handler mHandler = new Handler();
    private Runnable recordAccData = new Runnable() {
        @Override
        public void run() {
        	try {
        		if(!measurementList.isEmpty() && counter < measurementList.get(0).getAccDataList().size()) {
    				//for(int i=0; i<measurementList.get(0).getAccDataList().size();i ++){
    					float x = Float.parseFloat(measurementList.get(0).getAccDataList().get(counter).getX());
    					float y = Float.parseFloat(measurementList.get(0).getAccDataList().get(counter).getY());
    					counter++;
    					Log.i(TAG, "x: " + x + " Y: " + y + " Counter: " + counter);
    					animationThread.setGravityXandY(x,y);
    		        	mHandler.postDelayed(recordAccData, 100);
    				//}
    			} else {
    		    	mHandler.removeCallbacks(recordAccData);
    		    	ShowDialog();
    			}
        	} catch(Exception ex){
        		Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        	}
        }
    };
    
    public void ShowDialog(){
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Done!");
        builder.setMessage("Animation is done, show again?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                AnimateSensorActivity.this.recreate();
           }
       });
       builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
        	   AnimateSensorActivity.this.finish();
           }
       });
       builder.show();
    }

}