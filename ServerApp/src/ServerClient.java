
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

public class ServerClient extends Thread
{
    private Socket connectionSocket;
    private Logger log;
    private String registrationID;
    private String responseSentence;
    private BufferedReader inFromClient;
    private DataOutputStream outToClient;
    private GCMSender gcmSender;
    private Connection connection = null;
    
    //Remove values on Commit
    private final String DB_USERNAME = "";
    private final String DB_PW = "";
    private final String connectionString = "jdbc:mysql://localhost/AndroidDB";
    private final String AUTH_KEY = "";
 
    public ServerClient(Socket c, GCMSender gcmSender, Logger log) throws IOException
    {
        this.connectionSocket = c;
        this.gcmSender = gcmSender;
        this.log = log;
        log.log(Level.INFO, connectionSocket.getInetAddress().getHostAddress() + " connected\n");
    }
 
    public void run() 
    {
        runClientOperation();
    }
    
    public void runClientOperation(){
    
    	try
        {    
        	//Set up streams from client/socket
            inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            
            log.log(Level.FINE,"Ready to retreive data on socket port: " + connectionSocket.getLocalPort()); 
            //registrationID = inFromClient.readLine();
            JSONObject json = new JSONObject(inFromClient.readLine());
            JSONObject message = json.getJSONObject("Message");
            String action = message.getString("Action");
            String auth = message.getString("Authorization");
            
            log.log(Level.INFO, "JSON retreived: " + json.toString());
            log.log(Level.INFO, "JSON Action: " + action.toString());            
            
            if(auth.toString().equals(AUTH_KEY)) {
	            if(action.toUpperCase().equals("RECORD")){
	            	log.log(Level.INFO, "Inserting recording!");
	            	InsertRecording(message); //Insert recordings into DB
	            } else if(action.toUpperCase().equals("REGISTER")){
	            	log.log(Level.INFO, "Registering Android device!");
	            	RegisterDeviceForGCM(message); //Add registration ID to list
	    			WriteRegistrationIDsToFile(gcmSender.getRegIdList()); //Write list to file, so server can remember
	            } else if(action.toUpperCase().equals("GETDATA")){
	            	log.log(Level.INFO, "Sending back JSON data to device!");
	            	GetMeasurementFromDB(message.getInt("ID"));	//Get measurement from DB and return it as JSON
	            }
            } else {
            	log.log(Level.WARNING, "Unauthorized request!");
            	responseSentence = "Unauthorized!";
            }
                       
            outToClient.writeBytes(responseSentence);
            
            log.log(Level.FINE,"Writing following response back to: " + connectionSocket.getInetAddress().getHostAddress() 
            				+ "\n" + responseSentence + "\n");
            
            inFromClient.close();
            outToClient.close();
            connectionSocket.close();
            
            log.log(Level.FINE,"Closed streams and socket on " + connectionSocket.getInetAddress().getHostAddress() + "\n");
            System.out.println("TCPServer running\nWaiting for connections.....");
            System.out.println("-------------------------------------------------");
            
        }
        catch(IOException e)
        {
            System.out.println("Error: " + e.getMessage());
            
            log.severe(e.getMessage() + "\n");
        } catch (Exception e) {
			// TODO Auto-generated catch block
        	System.out.println("Error: " + e.getMessage());
            
            log.severe(e.getMessage() + "\n");
		}
    }   
    
    
    /**
     * Insert a JSON recording into DB
     * @param json
     */
	public void InsertRecording(JSONObject json) {
		try {
			
			JSONArray values = json.getJSONArray("Values");
			List<JSONObject> list = new ArrayList<JSONObject>();
			for(int i=0; i<values.length();i++){
				list.add((JSONObject)values.get(i));
			}

			Class.forName("com.mysql.jdbc.Driver").newInstance();
		    connection = DriverManager.getConnection(connectionString, DB_USERNAME, DB_PW);
		    
		    String mean = getMean(list);
		    int measurementID = InsertMeasurementIntoDB(mean, json.getInt("Duration"));
		    boolean inserted = true;
		    
		    if(measurementID != 0) {
		    	log.info("X value: " + list.get(0).getDouble("X"));
				for(int i=0;i<list.size();i++){
					inserted = InsertValuesIntoDB(connection, list.get(i).getDouble("X"), list.get(i).getDouble("Y"), list.get(i).getDouble("Z"), measurementID);
					if(!inserted)
						break;
				}				
		    }

            connection.close();
			
		    if(measurementID != 0 && inserted) {
		    	responseSentence = "Measurement with ID: " + measurementID + " was added to DB, mean of recording was: " + mean + "\n";
	    		sendGCMBroadcast(measurementID, list, mean, json.getInt("Duration"));
		    }
		    else
		    	responseSentence = "Error on insert!";
		    

		} catch (Exception ex) {
			log.severe(ex.getMessage() + "\n");
		}
	}
    
	/**
	 * Method to insert a measurement item into DB
	 * @param avg
	 * @param duration
	 * @return
	 */
	public int InsertMeasurementIntoDB(String avg, int duration){
		PreparedStatement preparedStatement = null;
		int measurementID = 0;
		
		try {
	    	
            String insertMeasurementSQL = "INSERT INTO Measurement"
            		+ "(RegDate, Avg, Duration) VALUES"
            		+ "(?,?,?)";
           
            preparedStatement = connection.prepareStatement(insertMeasurementSQL, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setTimestamp(1, getCurrentTimeStamp());
            preparedStatement.setString(2, avg);
            preparedStatement.setInt(3, duration);
            
            // execute insert SQL stetement
            preparedStatement.executeUpdate();
            
            //Return ID of last insert
            ResultSet rs = preparedStatement.getGeneratedKeys();
            if(rs.next())
            {
                measurementID = rs.getInt(1);
            }
             
            preparedStatement.close();
            
    		return measurementID;
    		
	    	} catch(Exception ex) {
	    		log.severe(ex.getMessage() + "\n");
	    		return 0;
	    	}
	}
	
	/**
	 * Method that insert MeasurementValue into DB, returns false if something failed
	 * @param connection
	 * @param x
	 * @param y
	 * @param z
	 * @param measurementID
	 * @return
	 */
	public boolean InsertValuesIntoDB(Connection connection, double x, double y, double z, int measurementID){
		PreparedStatement preparedStatement = null;
		
		try {
            String insertValues = "INSERT INTO MeasureValues"
            		+ "(X, Y, Z, Measurement_ID) VALUES"
            		+ "(?,?,?,?)";
            preparedStatement = connection.prepareStatement(insertValues);
            preparedStatement.setString(1, Double.toString(x));
            preparedStatement.setString(2, Double.toString(y));
            preparedStatement.setString(3, Double.toString(z));
            preparedStatement.setInt(4, measurementID);
            
            // execute insert SQL stetement
            preparedStatement.executeUpdate();
            
            preparedStatement.close();
    		return true;
    		
	    	} catch(Exception ex) {
	    		log.severe(ex.getMessage() + "\n");
	    		return false;
	    	}
		
		
	}
	
	private java.sql.Timestamp getCurrentTimeStamp() {
		 
		java.util.Date today = new java.util.Date();
		return new java.sql.Timestamp(today.getTime());
 
	}
	
	/**
	 * Get mean out of recorded data
	 * @param list
	 * @return
	 */
	private String getMean(List<JSONObject> list){
		try {
			double mean = 0;
			int count = 0;
		
			for(int i=0; i<list.size();i++){
				double x = list.get(i).getDouble("X");
				double y = list.get(i).getDouble("Y");
				double z = list.get(i).getDouble("Z");
				mean += x + y + z;
				count += 3;
			}
			
			mean /= count;
			
			return Double.toString(mean);
			
		} catch(Exception ex) {
			log.severe(ex.getMessage() + "\n");
			return "";
		}
		
	}
	
	/**
	 * Method used to send GCM broadcast to all registered Android apps
	 * @param measurementID
	 * @param list
	 * @param mean
	 * @param duration
	 */
	private void sendGCMBroadcast(int measurementID, List<JSONObject> list, String mean, int duration){
		try {
			JSONObject data = new JSONObject();
			JSONObject dataValues = new JSONObject();
			dataValues.put("MeasurementID", measurementID);
			data.put("data", dataValues);
			data.put("registration_ids", gcmSender.getRegIdList());
	        
			log.info("Sending broadcast to subscribers: " + data.toString());
	        gcmSender.sendPost(data);	
		} catch(Exception ex) {
			log.severe(ex.getMessage() + "\n");
		}
	}
	
	/**
	 * Method that registers ID of android device
	 * @param json
	 */
	public void RegisterDeviceForGCM(JSONObject json) {
		try {
			registrationID = json.getString("REGID");
			gcmSender.addRegistrationID(registrationID);
			responseSentence = "Device is registerd on server: ";
			log.info("Registered device on server: " + registrationID);
		} catch (Exception e) {
			log.severe(e.getMessage() + "\n");
		}
	}
    
	
	/**
	 * Fetches a Measurement from DB based on ID
	 * @param ID
	 */
	public void GetMeasurementFromDB(int ID){
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			connection = DriverManager.getConnection(connectionString,
					DB_USERNAME, DB_PW);
			JSONObject data = getMeasurement(connection, ID);
			responseSentence = data.toString();
			connection.close();    
		} catch(Exception ex){
			log.severe(ex.getMessage());
		}
	}
	
	private JSONObject getMeasurement(Connection connection, int ID){
    	try {
	    	PreparedStatement preparedStatement = null;
	
			String selectQuery = "select * from Measurement WHERE ID = ?";
			preparedStatement = connection.prepareStatement(selectQuery);
			preparedStatement.setInt(1, ID);
			
			// execute select SQL stetement
			ResultSet rs = preparedStatement.executeQuery();
			JSONObject dataValues = new JSONObject();
			while (rs.next()) {
				Date date = rs.getDate("RegDate");
				float avg = rs.getFloat("Avg");
				int duration = rs.getInt("Duration");
				dataValues.put("Mean", avg);
				dataValues.put("Duration", duration);
				dataValues.put("Time", date);
			}

			dataValues.put("MeasurementID", ID);
			dataValues.put("Values", getValues(connection, ID));
			preparedStatement.close();
			
			return dataValues;
    	} catch(Exception ex) {
    		log.severe(ex.getMessage());
    		return null;
    	}
    }

    private List<JSONObject> getValues(Connection connection, int ID){
    	try {

			PreparedStatement preparedStatement = null;

			String selectQuery = "select * from MeasureValues WHERE Measurement_ID= ?";
			preparedStatement = connection.prepareStatement(selectQuery);
			preparedStatement.setInt(1, ID);
			
			// execute select SQL stetement
			ResultSet rs = preparedStatement.executeQuery();
			List<JSONObject>lastAccDataList = new ArrayList<JSONObject>();
			while (rs.next()) {
				JSONObject xyz = new JSONObject();
				xyz.put("X", rs.getString("X"));
				xyz.put("Y", rs.getString("Y"));
				xyz.put("Z", rs.getString("Z"));
				lastAccDataList.add(xyz);
			}
			
			preparedStatement.close();
			
			return lastAccDataList;
			
    	} catch(Exception ex) {
    		log.severe(ex.getMessage());
    		return null;
    	}
    }
    
    private void WriteRegistrationIDsToFile(List<String> listOfIds){
    	try {
    		JSONObject values = new JSONObject().put("REGIDS", listOfIds);
	    	String content = values.toString();
	    	 
			File file = new File(ServerClient.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"/RegistrationIDs.txt");
	
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
	
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
	
			System.out.println("Done");
    	} catch(Exception ex) {
    		log.severe(ex.getMessage());
    	}
    }

	
}