import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TCPServer {

	
	public static void main(String args[]) throws Exception {
		TCPServer server = new TCPServer();
		ServerSocket welcomeSocket = new ServerSocket(9999);
		GCMSender gcmSender = new GCMSender();
		
		//Create Loghandler
		FileHandler hand = new FileHandler("TCPServerLog.log", true);
		Logger log = Logger.getLogger("log_file");
		log.addHandler(hand);
		
		//Set logging
		
//		SEVERE (highest value)
//		WARNING
//		INFO
//		CONFIG
//		FINE
//		FINER
//		FINEST (lowest value)
		
		if (args.length == 1 || args.length == 2) {
			if (args[0].equals("INFO"))
				log.setLevel(Level.INFO);
			else if(args[0].equals("WARNING"))
				log.setLevel(Level.WARNING);
			else if(args[0].equals("SEVERE"))
				log.setLevel(Level.SEVERE);
			else if(args[0].equals("ALL"))
				log.setLevel(Level.FINEST);
			
			server.GetRegistrationIDs(gcmSender, log);
			System.out.println("TCPServer running\nWaiting for connections.....");
			while (true) {
				Socket connectionSocket = (Socket)welcomeSocket.accept();
				if (connectionSocket != null) {

					
					//Start new thread
					ServerClient client = new ServerClient(connectionSocket, gcmSender, log);
					client.start();
				}
			}
		} else
			System.out.println("Termianl Usage --> java -jar TCPServer.jar ALL, INFO, WARNING or SEVERE");
			System.out.println("Eclipse Usage --> paremeters = ALL, INFO, WARNING or SEVERE");
	}
	
	public void GetRegistrationIDs(GCMSender sender, Logger log){
		try (BufferedReader br = new BufferedReader(new FileReader(ServerClient.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"/RegistrationIDs.txt")))
		{
 
			String sCurrentLine, finalResult = "";
 
			while ((sCurrentLine = br.readLine()) != null) {
				finalResult += sCurrentLine;
			}
			
			JSONObject json = new JSONObject(finalResult);
			JSONArray arr = json.getJSONArray("REGIDS");
			
			for(int i=0; i<arr.length();i++){
				sender.getRegIdList().add(arr.get(i).toString());
			}
			
			log.log(Level.INFO, "Got list of registration IDS with size: " + sender.getRegIdList().size());
 
		} catch (IOException e) {
    		log.severe(e.getMessage() + "\n");
		} catch (JSONException e) {
    		log.severe(e.getMessage() + "\n");
		} catch (Exception e){
    		log.severe(e.getMessage() + "\n");
		}
	}
	

}
