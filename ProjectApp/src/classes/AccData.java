package classes;

public class AccData {
	private String X,Y,Z;
	private int Measure_ID;
	
	public AccData(String X, String Y, String Z, int Measure_ID){
		this.X = X;
		this.Y = Y;
		this.Z = Z;
		this.Measure_ID = Measure_ID;
	}

	public String getX() {
		return X;
	}

	public void setX(String x) {
		X = x;
	}

	public String getY() {
		return Y;
	}

	public void setY(String y) {
		Y = y;
	}

	public String getZ() {
		return Z;
	}

	public void setZ(String z) {
		Z = z;
	}

	public int getMeasure_ID() {
		return Measure_ID;
	}

	public void setMeasure_ID(int measure_ID) {
		Measure_ID = measure_ID;
	}
	
	
	
}
