package michael.findata.demo.aparapi.simpleregression;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;

import static michael.findata.util.FinDataConstants.OPENCL_REGRESSION_SIZE;

public class OcRegressionDouble64 {

	final int mod = OPENCL_REGRESSION_SIZE - 1;
	final int num_of_series = 2;

	private int n = 0;
	final double[] x = new double[OPENCL_REGRESSION_SIZE*num_of_series];
//	final double[] y = new double[OPENCL_REGRESSION_SIZE];
//	final double[] sumXX = new double[OPENCL_REGRESSION_SIZE];
	final double[] sumXY = new double[num_of_series*num_of_series*OPENCL_REGRESSION_SIZE];
	int intg = Integer.MAX_VALUE;
	final int[] shift = new int [] {1, 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384};

	final Kernel sum = new Kernel(){

		// This is a special Aparapi thing
		final double[] x = OcRegressionDouble64.this.x;
//		final double[] y = OcRegressionDouble1.this.y;
//		final double[] sumXX = OcRegressionDouble1.this.sumXX;
		public final double[] sumXY = OcRegressionDouble64.this.sumXY;
		final int[] shift = OcRegressionDouble64.this.shift;

		@Override
		public void run() {
			int gid1 = getGlobalId(0);
			int gid2 = getLocalId(1);
			int gid3 = getGlobalId(2);
			int xIndex = gid1 * OPENCL_REGRESSION_SIZE + gid3;
			int yIndex = gid2 * OPENCL_REGRESSION_SIZE + gid3;
			int xyIndex = gid1 * OPENCL_REGRESSION_SIZE * num_of_series + gid2 * OPENCL_REGRESSION_SIZE + gid3;
			int pass = getPassId();
			if (pass == 0) {
				sumXY[xyIndex] = x[xIndex] * x[yIndex];
			} else {
				int p = gid1 * OPENCL_REGRESSION_SIZE * num_of_series + gid2 * OPENCL_REGRESSION_SIZE + (gid3 + shift[pass]);
				if ((gid3 & (shift[pass+1]-1)) == 0) {
					sumXY[xyIndex] = sumXY[xyIndex] + sumXY[p];
				}
			}
		}
	};

	// todo
	public void addData(double x, double y) {
		this.x[n] = x;
		this.x[n+OPENCL_REGRESSION_SIZE] = y;
		++this.n;
	}

	public double getSlope() {

		// Execute Kernel.
		sum.execute(Range.create3D(num_of_series, num_of_series, OPENCL_REGRESSION_SIZE), 1+(int)(Math.log10(OPENCL_REGRESSION_SIZE)/Math.log10(2)));
//		sum.execute(Range.create3D(num_of_series, num_of_series, OPENCL_REGRESSION_SIZE), 2);
		// Dispose Kernel resources.
		sum.dispose();
//		sum.put(sumXY);

		// todo
		System.out.println("\nsumXY: "+sumXY[OPENCL_REGRESSION_SIZE]);
		System.out.println("sumXX: "+sumXY[3*OPENCL_REGRESSION_SIZE]);
		return sumXY[OPENCL_REGRESSION_SIZE]/sumXY[3*OPENCL_REGRESSION_SIZE];
	}

	public long getN() {
		return this.n;
	}
}