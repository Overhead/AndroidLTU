package classes;

import com.example.porjectappsender.MainActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class HelperMethods {

	Context context;
	public HelperMethods(Context context) {
		this.context = context;
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
        return context.getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
	}

}
