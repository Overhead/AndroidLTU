package classes;

import java.sql.Date;
import java.util.List;


public class Measurement {
	private String time;
	private int Avg, Duration;
	private List<AccData> accDataList;
	
	public Measurement(String time, int Avg, int Duration, List<AccData> accDataList){
		this.time = time;
		this.Avg = Avg;
		this.Duration = Duration;
		this.accDataList = accDataList;
	}

	public String getTime() {
		return time;
	}

	public int getAvg() {
		return Avg;
	}

	public int getDuration() {
		return Duration;
	}
	
	public List<AccData> getAccDataList(){
		return accDataList;
	}
	
	
	
}
