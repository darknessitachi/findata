package michael.findata.demo.aparapi.simpleregression;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;

import static michael.findata.util.FinDataConstants.OPENCL_REGRESSION_SIZE;

public class OcRegressionFloat {

	final int mod = OPENCL_REGRESSION_SIZE - 1;

	private int n = 0;

	final float[] x = new float[OPENCL_REGRESSION_SIZE];
	final float[] y = new float[OPENCL_REGRESSION_SIZE];
	final float[] sumXX = new float[OPENCL_REGRESSION_SIZE];
	final float[] sumXY = new float[OPENCL_REGRESSION_SIZE];

	final Kernel sum = new Kernel(){

		// This is a special Aparapi thing
		final float[] x = OcRegressionFloat.this.x;
		final float[] y = OcRegressionFloat.this.y;
		final float[] sumXX = OcRegressionFloat.this.sumXX;
		final float[] sumXY = OcRegressionFloat.this.sumXY;
		final float[] slope = new float[1];
		final int[] shift = new int [] {1, 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384};

		@Override
		public void run() {
			int gid = getGlobalId();
			int pass = getPassId();
			if (pass == 0) {
				sumXX[gid] = x[gid] * x[gid];
				sumXY[gid] = y[gid] * x[gid];
			} else {
//				int p = (gid + shift[pass]) & mod;
				int p = gid + shift[pass];
				if ((gid & (shift[pass+1]-1)) == 0) {
					sumXX[gid] = sumXX[gid] + sumXX[p];
					sumXY[gid] = sumXY[gid] + sumXY[p];
				}
			}
		}
	};

	public void addData(float x, float y) {

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