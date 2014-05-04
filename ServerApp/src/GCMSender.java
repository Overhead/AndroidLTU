import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;
 
public class GCMSender {
 
	private List<String> regIdList = new ArrayList<String>();
	
	public GCMSender() {
		
	}
	
	
	// HTTP POST request
	public void sendPost(JSONObject jsonData) throws Exception {
 
		String url = "https://android.googleapis.com/gcm/send";
		URL obj = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
 
		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("Accept-Language", "en-US");
		con.setRequestProperty("Authorization", "key=");
		con.setRequestProperty("Content-Type", "application/json");
 
		// Send post request
		con.setDoOutput(true);
		
		OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
		wr.write(jsonData.toString());
		wr.flush();
		wr.close();
 
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + jsonData.toString());
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		//print result
		System.out.println(response.toString());
 
	}

	public List<String> getRegIdList() {
		return regIdList;
	}

	public void addRegistrationID(String ID) {
		this.regIdList.add(ID);
	}	
 
}