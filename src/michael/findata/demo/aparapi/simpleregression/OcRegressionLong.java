package michael.findata.demo.aparapi.simpleregression;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;
import static michael.findata.util.FinDataConstants.OPENCL_REGRESSION_SIZE;

public class OcRegressionLong {

	final int mod = OPENCL_REGRESSION_SIZE - 1;

	private int n = 0;
	final long[] x = new long[OPENCL_REGRESSION_SIZE];
	final long[] y = new long[OPENCL_REGRESSION_SIZE];
	final long[] sumXX = new long[OPENCL_REGRESSION_SIZE];
	final long[] sumXY = new long[OPENCL_REGRESSION_SIZE];
	final int[] shift = new int [] {1, 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384};

	public void addData(long x, long y) {
		this.x[n] = x;
		this.y[n] = y;
		++this.n;
	}

	public double getSlope() {

		final long[] x = this.x;
		final long[] y = this.y;
		final long[] sumXX = this.sumXX;
		final long[] sumXY = this.sumXY;
		final int[] shift = this.shift;

		final Kernel sum = new Kernel(){
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
			}
		};

		// Execute Kernel.
		sum.execute(Range.create(OPENCL_REGRESSION_SIZE), 1+(int)(Math.log10(OPENCL_REGRESSION_SIZE)/Math.log10(2)));

		// Dispose Kernel resources.
		sum.dispose();

		System.out.println("\nsumXY: "+sumXY[0]);
		System.out.println("sumXX: "+sumXX[0]);
		return sumXY[0]/(double)sumXX[0];
	}

	public long getN() {
		return this.n;
	}
}
