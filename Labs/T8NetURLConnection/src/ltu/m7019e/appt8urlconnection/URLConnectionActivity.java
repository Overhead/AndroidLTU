package ltu.m7019e.appt8urlconnection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import ltu.m7019e.appt3asynctask.R;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class URLConnectionActivity extends Activity {
	Button btnSlowWork, btnSlowWork2;
	Button btnQuickWork;
	EditText etMsg;
	Long startingMillis;
	
    private static String mFileName = null;
    private static File outFile=null;
	private FileOutputStream fos;
	private FileInputStream fis;

	
@Override
public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_async_task);
	etMsg= (EditText) findViewById(R.id.EditText01);
	btnSlowWork= (Button) findViewById(R.id.Button01);
	btnSlowWork2 = (Button)findViewById(R.id.Button03);
	// delete all data from database (when delete button is clicked)
	
	this.btnSlowWork.setOnClickListener(new OnClickListener() {
		public void onClick(final View v) {
			new VerySlowTask().execute();
		}
	});
	
	this.btnSlowWork2.setOnClickListener(new OnClickListener() {
		public void onClick(final View v) {
			new SlowTask().execute();
		}
	});
	
	btnQuickWork= (Button) findViewById(R.id.Button02);
	// delete all data from database (when delete button is clicked)
	this.btnQuickWork.setOnClickListener(new OnClickListener() {
		public void	 onClick(final View v) {
			
			Intent myActivity2 =
					new Intent(android.content.Intent.ACTION_VIEW);
					Uri data = Uri.parse("file:///sdcard/europe_comp_5.mp3");
					String type = "audio/mp3";
					myActivity2.setDataAndType(data, type);
					startActivity(myActivity2);
			
		}
	});
	}
	
	private class VerySlowTask extends AsyncTask<String, Long, Void> {
		
		private final ProgressDialog dialog= new ProgressDialog(URLConnectionActivity.this);

		// can use UI thread here
		protected void onPreExecute() {
			startingMillis= System.currentTimeMillis();
			etMsg.setText("Start Time: "+ startingMillis);
			this.dialog.setMessage("Wait\nDowloading is on the way...");
			this.dialog.show();
		}
		
		// automatically done on worker thread (separate from UI thread)
		protected	Void doInBackground(final String... args) {
			try{
				InputStream in = null;


				URL u = null;
				try {
					u = new URL("ftp://downloader:qwerty@54.247.131.48/count_down.mp3");
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				

				URLConnection uc = null;
				try {

				
					uc = u.openConnection();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
					in = uc.getInputStream();
					
			        mFileName = "europe_comp_5.mp3";

			    	String state = Environment.getExternalStorageState();
			        if (Environment.MEDIA_MOUNTED.equals(state)) {
			        outFile = new File(Environment.getExternalStorageDirectory(), mFileName);
					
					if (!outFile.exists()) 
			        {
			         //File does not exists
			         try {
						outFile.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			        } 
			        }
					try {
						fos = new FileOutputStream(outFile);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					 byte[] buffer = new byte[4096];
					   int len;
					   try {
						while ((len = in.read(buffer)) > 0) {
							   fos.write(buffer, 0, len);
							   }
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					   try {
						fos.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					   try {
						in.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		// periodic updates -it is OK to change UI
		@Override
		protected void onProgressUpdate(Long... value) {
			super.onProgressUpdate(value);
			etMsg.append("\nworking..."+ value[0]);
		}
		
		// can use UI thread here
		protected void onPostExecute(final Void unused) {
			if(this.dialog.isShowing()) {
				this.dialog.dismiss();
		}
		// cleaning-up all done
		etMsg.append("\nEndTime:"
		+ (System.currentTimeMillis()-startingMillis)/1000);
		etMsg.append("\ndone!");
		}
	}
	
	private class SlowTask extends AsyncTask<String, Long, Void> {

		private final ProgressDialog dialog= new ProgressDialog(URLConnectionActivity.this);

		// can use UI thread here
		protected void onPreExecute() {
			startingMillis= System.currentTimeMillis();
			etMsg.setText("Start Time: "+ startingMillis);
			this.dialog.setMessage("Wait\nUploading is on the way...");
			this.dialog.show();
		}
		
		@Override
		protected Void doInBackground(String... arg0) {
			URL url = null;
			URLConnection urlC = null;
			OutputStream out = null;
			
			try {
				mFileName = "europe_comp_5.mp3";
				url = new URL("ftp://ftpuser:qwerty@54.216.219.88/"+mFileName);

				urlC = url.openConnection();
				Log.i("FTP", "Opened connection");
				urlC.setDoOutput(true);
				out = urlC.getOutputStream();
				Log.i("FTP", "Got stream");
		    	String state = Environment.getExternalStorageState();
		        if (Environment.MEDIA_MOUNTED.equals(state)) {
			        outFile = new File(Environment.getExternalStorageDirectory(), mFileName);
			        Log.i("FTP", outFile.toString());
					if (outFile.exists()) 
			        {
						fis = new FileInputStream(outFile);
						byte[] buffer = new byte[4096];
						int bytesRead = -1;
			            while ((bytesRead = fis.read(buffer)) != -1) {
			                out.write(buffer, 0, bytesRead);
			            }
			            out.close();
			            fis.close();
				            
			        } else {
			        	etMsg.setText("No file");
			        }
					
		        }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e("FTP", e.getMessage());
			}
			
			return null;
		}
		
		// periodic updates -it is OK to change UI
				@Override
				protected void onProgressUpdate(Long... value) {
					super.onProgressUpdate(value);
					etMsg.append("\nworking..."+ value[0]);
				}
				
				// can use UI thread here
				protected void onPostExecute(final Void unused) {
					if(this.dialog.isShowing()) {
						this.dialog.dismiss();
				}
				// cleaning-up all done
				etMsg.append("\nEndTime:"
				+ (System.currentTimeMillis()-startingMillis)/1000);
				etMsg.append("\ndone!");
				}
		
	}

}
