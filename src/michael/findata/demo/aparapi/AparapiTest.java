package michael.findata.demo.aparapi;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;

public class AparapiTest {
	public static void main(String[] _args) {

		final int size = 4;

		/** Input float array for which square values need to be computed. */
		final float[] x = new float[size];
		final float[] y = new float[size];
		final float[] sp1 = new float[size];
//		final float[] sp2 = new float[size];

		/** Initialize input array. */
		x[0] = 1.1f; y[0] = 2.3f;
		x[1] = 2.3f; y[1] = 4.3f;
		x[2] = 3.4f; y[2] = 5.5f;
		x[3] = 3.9f; y[3] = 7.6f;

		/** Output array which will be populated with square values of corresponding input array elements. */
		final float[] sumXX = new float[size];
		final float[] sumXY = new float[size];

		Kernel kernel = new Kernel(){
			@Override
			public void run() {
				int gid = getGlobalId();
				sumXX[gid] = x[gid] * x[gid];
				sumXY[gid] = y[gid] * x[gid];
				sumXX[gid] = sumXX[gid] + sumXX[(gid+1)&3];
				sumXY[gid] = sumXY[gid] + sumXY[(gid+1)&3];
				sumXX[gid] = sumXX[gid] + sumXX[(gid+2)&3];
				sumXY[gid] = sumXY[gid] + sumXY[(gid+2)&3];
			}
		};

		// Execute Kernel.
		kernel.execute(Range.create(size));

		// Report target execution mode: GPU or JTP (Java Thread Pool).
		System.out.println("Max=" + Integer.MAX_VALUE);
		System.out.println("Maotai=" + Integer.MAX_VALUE/(28389*28389));
		System.out.println("Execution mode=" + kernel.getExecutionMode());
		System.out.printf("%8.16f\n", sumXY[0]/sumXX[0]);

		// Dispose Kernel resources.
		kernel.dispose();
	}
}