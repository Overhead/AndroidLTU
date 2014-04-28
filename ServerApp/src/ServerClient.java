
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerClient extends Thread
{
    private Socket connectionSocket;
    private Logger log;
    private String clientSentence;
    private String capitalizedSentence;
    private BufferedReader inFromClient;
    private DataOutputStream outToClient;
 
    public ServerClient(Socket c, Logger log) throws IOException
    {
        this.connectionSocket = c;
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
            clientSentence = inFromClient.readLine();
            
            log.log(Level.FINE,"\nReceived data");
            
            capitalizedSentence = "Heia tilbake";
            
            outToClient.writeBytes("Was Connected to " + connectionSocket.getLocalAddress());
            
            log.log(Level.FINE,"Writing following response back to: " + connectionSocket.getInetAddress().getHostAddress() 
            				+ "\n" + capitalizedSentence + "\n");
            
            inFromClient.close();
            outToClient.close();
            connectionSocket.close();
            
            log.log(Level.FINE,"Closed streams and socket on " + connectionSocket.getInetAddress().getHostAddress() + "\n");
        }
        catch(IOException e)
        {
            System.out.println("Error: " + e);
            
            log.severe(e.getMessage() + "\n");
        }
    }    
}