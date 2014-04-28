package db;

public class Settings {

	private String ID, IP, PORT;
	
	public Settings(String ID, String IP, String PORT) {
		this.ID = ID;
		this.IP = IP;
		this.PORT = PORT;
	}

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public String getIP() {
		return IP;
	}

	public void setIP(String iP) {
		IP = iP;
	}

	public String getPORT() {
		return PORT;
	}

	public void setPORT(String pORT) {
		PORT = pORT;
	}

	
	
}
