package com.example.appt3raceandroid;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {


	static Shared0 s;
	static volatile boolean done = false;
	WriterTask task;
	Typewriter writer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		writer = new Typewriter(this);
		setContentView(writer);
		writer.setCharacterDelay(50);
		writer.animateText("Start: \n");
		
		task = new WriterTask();
		task.execute();
		
	}
	
	@Override
	public void onBackPressed(){
		task.cancel(true);
		super.onBackPressed();
	}
	
	public class Shared0 {
		protected int x = 0, y = 0;

		public int dif() {
			return x - y;
		}

		public void bump() throws InterruptedException {
			x++;
			Thread.sleep(9);
			y++;
		}
	}
	
	public class Race0 extends Thread {
		public void run() {
			int i;
			try {
				for (i = 0; i < 1000; i++) {
					if (i % 60 == 0) 
						writer.setText("");
					writer.setText(String.valueOf(".X".charAt(s.dif())));
					sleep(20);

				}
				Log.i("Writer" , "Ended");
				writer.setText("");
				done = true;

			} catch (InterruptedException e) {
				return;
			}
		}
	}
	
	public class WriterTask extends AsyncTask<String, Long, Void> {

		@Override
		protected Void doInBackground(String... params) {
			Thread lo = new Race0();
			s = new Shared0();
			try {
				lo.start();
				while (!done) {
					s.bump();
					Thread.sleep(30);
				}
				lo.join();
			} catch (InterruptedException e) {
				Log.e("Writer" , e.getMessage());
			}
			Log.i("Media", "EndedAsync");
			return null;
		}

		
	}
}
