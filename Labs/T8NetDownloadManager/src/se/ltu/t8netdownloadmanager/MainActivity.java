package se.ltu.t8netdownloadmanager;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity {
		 
	
	    private long enqueue;
	    private DownloadManager dm;
	 
	    /** Called when the activity is first created. */
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_main);
	 
	        BroadcastReceiver receiver = new BroadcastReceiver() {
				@Override
	            public void onReceive(Context context, Intent intent) {
	                String action = intent.getAction();
	                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
	                    long downloadId = intent.getLongExtra(
	                            DownloadManager.EXTRA_DOWNLOAD_ID, 0);
	                    Query query = new Query();
	                    query.setFilterById(enqueue);
	                    Cursor c = dm.query(query);
	                    if (c.moveToFirst()) {
	                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
	                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
	                        	
	                        }
	                    }
	                }
	            }
	        };
	 
	        registerReceiver(receiver, new IntentFilter(
	                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
	    }
	 
	    public void onClick(View view) {
	        dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
	        Request request = new Request(Uri.parse("http://54.247.131.48/files/count_down.mp3"));
	        request.setDestinationInExternalPublicDir("/"+Environment.DIRECTORY_MUSIC, "count_down3.mp3");
	        enqueue = dm.enqueue(request);
	 
	    }
	 
	    public void showDownload(View view) {
	        Intent i = new Intent();
	        i.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
	        startActivity(i);
	    }
	}
	

