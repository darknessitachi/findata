package michael.findata.util;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;

import static michael.findata.util.FinDataConstants.OPENCL_REGRESSION_SIZE;

public class OcRegressionDouble {

	final int mod = OPENCL_REGRESSION_SIZE - 1;

	private int n = 0;
	final double[] x = new double[OPENCL_REGRESSION_SIZE];
	final double[] y = new double[OPENCL_REGRESSION_SIZE];
	final double[] sumXX = new double[OPENCL_REGRESSION_SIZE];
	final double[] sumXY = new double[OPENCL_REGRESSION_SIZE];
	final int[] shift = new int [] {1, 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8129};

	final Kernel sum = new Kernel(){

		// This is a special Aparapi thing
		final double[] x = OcRegressionDouble.this.x;
		final double[] y = OcRegressionDouble.this.y;
		final double[] sumXX = OcRegressionDouble.this.sumXX;
		final double[] sumXY = OcRegressionDouble.this.sumXY;
		final int[] shift = OcRegressionDouble.this.shift;

		@Override
		public void run() {
			int gid = getGlobalId();
			int pass = getPassId();
			if (pass == 0) {
				sumXX[gid] = x[gid] * x[gid];
				sumXY[gid] = y[gid] * x[gid];
			} else {
				int p = (gid + shift[pass]) & mod;
				sumXX[gid] = sumXX[gid] + sumXX[p];
				sumXY[gid] = sumXY[gid] + sumXY[p];
			}
			localBarrier();
		}
	};

	public void addData(double x, double y) {
		this.x[n] = x;
		this.y[n] = y;
		++this.n;
	}

	public double getSlope() {

		// Execute Kernel.
		sum.execute(Range.create(OPENCL_REGRESSION_SIZE), 1+(int)(Math.log10(OPENCL_REGRESSION_SIZE)/Math.log10(2)));
		// Dispose Kernel resources.
		sum.dispose();

		System.out.println("\nsumXY: "+sumXY[0]);
		System.out.println("sumXX: "+sumXX[0]);
		return sumXY[0]/sumXX[0];
	}

	public long getN() {
		return this.n;
	}
}