package michael.findata.util;

public class TrivialRegressionFloat {

	//	private double xbar;
//	private double ybar;
//	private double sumX;
//	private double sumY;
	private float sumXX;
	//	private double sumYY;
	private float sumXY;
	private long n;

	public void addData(float x, float y) {
//		if(this.n == 0L) {
//			this.xbar = x;
//			this.ybar = y;
//		}

		this.sumXX += x * x;
//		this.sumYY += y * y;
		this.sumXY += x * y;

//		this.sumX += x;
//		this.sumY += y;
		++this.n;
	}

	public long getN() {
		return this.n;
	}

	public float getSlope() {
		return this.sumXY / this.sumXX;
	}
}