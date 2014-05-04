package classes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import com.mini.project.RecieverActivity;

public class HelperMethods {

	Context context;
	public HelperMethods(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
	}

	public void writeToFile(String data, String fileName) {
	    try {
	    	File outFile = null;
	        String state = Environment.getExternalStorageState();
	        if (Environment.MEDIA_MOUNTED.equals(state)) {

	    		outFile = new File(Environment.getExternalStorageDirectory(), fileName);
	    		if (!outFile.exists()) 
                {
                 //File does not exists
                 outFile.createNewFile();
                } 
	        }
			FileOutputStream fos = new FileOutputStream(outFile);
			OutputStreamWriter  osw = new OutputStreamWriter(fos);
			BufferedWriter bw = new BufferedWriter(osw);
			bw.write(data);
			
			bw.close();
			osw.close();
			fos.close();

	    }
	    catch (IOException e) {
	        Log.e("FILE", "File write failed: " + e.toString());
	    } 
	}


	public String readFromFile(String fileName) {

	    String ret = "";
	    File outFile = null;
        StringBuilder stringbuilder = new StringBuilder();
	    try {
	    	outFile = new File(Environment.getExternalStorageDirectory(), fileName);
	        FileInputStream fis = new FileInputStream(outFile);
	        InputStreamReader isw = new InputStreamReader(fis);
	        BufferedReader bf = new BufferedReader(isw);
	        
	        while((ret = bf.readLine()) != null)
	        {
	        	stringbuilder.append(ret);
	        }
	        
	        fis.close();
	        isw.close();
	        bf.close();

	    }
	    catch (FileNotFoundException e) {
	        Log.e("FILE", "File not found: " + e.toString());
	        return "";
	    } catch (IOException e) {
	        Log.e("FILE", "Can not read file: " + e.toString());
	        return "";
	    }
	    return stringbuilder.toString();
	}
	
	public ServerSettings GetSettings(){
		final SharedPreferences prefs = getSharedPreferences();
		String IP = prefs.getString("IP", "");
		int PORT = prefs.getInt("PORT", 0);
		
		if(IP.isEmpty() || PORT == 0){
			return null;
		} else {
			return new ServerSettings(IP, PORT);
		}	
	}
		
	public SharedPreferences getSharedPreferences(){
        return context.getSharedPreferences(RecieverActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
	}
	
}
