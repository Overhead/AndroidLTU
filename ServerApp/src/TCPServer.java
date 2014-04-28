import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TCPServer {

	public static void main(String args[]) throws Exception {
		ServerSocket welcomeSocket = new ServerSocket(9999);
		
		int nrOfIterations = 0;

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
			
			System.out.println("TCPServer running\nWaiting for connections.....");
			while (true) {
				Socket connectionSocket = (Socket)welcomeSocket.accept();
				if (connectionSocket != null) {

					
					//Start new thread
					ServerClient client = new ServerClient(connectionSocket, log);
					client.start();
				}
			}
		} else
			System.out.println("Termianl Usage --> java TCPServer ALL, INFO, WARNING or SEVERE");
			System.out.println("Eclipse Usage --> paremeters = ALL, INFO, WARNING or SEVERE");
	}

}
