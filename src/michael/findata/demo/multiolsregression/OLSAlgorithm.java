package michael.findata.demo.multiolsregression;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.stat.regression.SimpleRegression;

public class OLSAlgorithm {
	public static void helpMeUnderstandEstimateRegressionParamters() {
		OLSMultipleLinearRegression regression2 = new OLSMultipleLinearRegression();
		double[] y = {
				0,
				0,
				0,
				0
		};
//		double[][] x2 =	{
//				{1.12d},
//				{2.23d},
//				{3.34d},
//				{4.45d},
//		};
		double[][] x2 =	{
				{1.12d,1},
				{2.23d,2},
				{3.34d,3},
				{4.45d,4},
		};

//		regression2.newSampleData(y, x2);
		regression2.setNoIntercept(true);
		regression2.newSampleData(y, x2);
		double[] beta = regression2.estimateRegressionParameters();
		for (double d : beta) {
			System.out.println("D: " + d);
		}
	}

	public static void helpMeUnderstandSimpleRegression () {
		SimpleRegression sr = new SimpleRegression(false);
		sr.addData(1.12, 1);
		sr.addData(2.23, 2);
		sr.addData(3.34, 3);
		sr.addData(4.45, 4);
		System.out.println(sr.getSlope());
//		System.out.println(sr.getSlopeStdErr());
//		double d = sr.getSlope();
//		double std [] = new double[] {1*d/1.12 - 1, 2*d/2.23 - 1, 3*d/3.34 - 1, 4*d/4.45 -1};
//		System.out.println(new StandardDeviation().evaluate(std));
	}

	public static void main (String [] args) {
		helpMeUnderstandEstimateRegressionParamters();
		helpMeUnderstandSimpleRegression();
	}
}