package ltu.m7019e.t8netudpechoclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
	/** Called when the activity is first created. */    
	private EditText portNumber;    
	private EditText hostName;    
	private EditText inputMsg;    
	private EditText resultMsg;    
	private InetAddress ia;    
	private Socket mySocket;    
	private InputStream isIn;    
	private OutputStream psOut;    
	private byte abIn[];
	private String sHostName;
	private int iPortNumber;
	private Handler mHandler;
	private int iNumRead;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
     
		hostName = (EditText) findViewById(R.id.editText1);        
		portNumber = (EditText) findViewById(R.id.editText2);        
		resultMsg = (EditText) findViewById(R.id.editText3);        
		inputMsg = (EditText) findViewById(R.id.editText4);  
		mHandler= new Handler();

	}
	
	class ClientThread implements Runnable {
		 @Override  
		 public void run() {
			 try {
				 InetAddress serverAddr = InetAddress.getByName(sHostName);
				 try {
					mySocket = new Socket(serverAddr, iPortNumber);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 } 
			 catch (UnknownHostException e1) 
				 {
					 e1.printStackTrace();
				 } 

		 }
	}
	class ResponseThread implements Runnable {

		@Override  
		 
		 public void run() {
 			try {
				isIn = mySocket.getInputStream();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
 			iNumRead = 0;    		
    		abIn = new byte[1024];


			try {
				iNumRead = isIn.read(abIn);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			mHandler.post(new Runnable(){
				public void run(){
		    		String sResponse, sTemp;

					sTemp = new String(abIn, 0, iNumRead);
					sResponse = "We got back: " + sTemp;
		    		resultMsg.setText(sResponse);
				
				}
				
			});

		 }
	}
	
public void myEchoHandler(View view) {    	
	switch (view.getId()) {    	
	case R.id.button1:    		/* This is the connect button */
		sHostName = hostName.getText().toString(); 
   		iPortNumber = Integer.parseInt(portNumber.getText().toString());

		
		new Thread(new ClientThread()).start();

		
		break;
	case R.id.button2:    /* This is the send data button */ 

	    		byte[] sInputMsg = inputMsg.getText().toString().getBytes();
	    		try  {  
	    			psOut = mySocket.getOutputStream();
	    			psOut.write(sInputMsg,0, sInputMsg.length);
	    			new Thread(new ResponseThread()).start();
	    			
	        		}   
	    		catch (Exception ex) {
	   			Toast.makeText(this,"Send data failed.  Exception" + ex + "\n",	 Toast.LENGTH_LONG).show();   	
	    		}
	        
	   

		break;			
	case R.id.button3:   // This is the quit button.
		String sTemp2;
		try {
			mySocket.close();
			inputMsg.setText("");
			sTemp2 = new String ("Goodbye ...");
			resultMsg.setText(sTemp2);
			}    		
		catch (Exception ex) 
		{
			Toast.makeText(this,"Close socket failed.  Exception " + ex + "\n", Toast.LENGTH_LONG).show(); 
		}
	} //end of switch 
}//end of myEchoHandler




	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
