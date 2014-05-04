package classes;

public class ServerSettings {

	private String IP;
	private int PORT;
	
	public ServerSettings(String IP, int PORT) {
		this.IP = IP;
		this.PORT = PORT;
	}

	public String getIP() {
		return IP;
	}

	public void setIP(String iP) {
		IP = iP;
	}

	public int getPORT() {
		return PORT;
	}

	public void setPORT(int pORT) {
		PORT = pORT;
	}
	
	

}
