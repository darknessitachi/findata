package michael.findata.util;

public class TrivialRegressionDouble {

	//	private double xbar;
//	private double ybar;
//	private double sumX;
//	private double sumY;
	private double sumXX;
	//	private double sumYY;
	private double sumXY;
	private long n;

	public void addData(double x, double y) {
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

	public double getSlope() {
		System.out.println("\nsumXY: "+sumXY);
		System.out.println("sumXX: "+sumXX);
		return this.sumXY / this.sumXX;
	}
}