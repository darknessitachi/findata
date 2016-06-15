package michael.findata.demo.algoquant.olsregression;

import com.numericalmethod.suanshu.algebra.linear.matrix.doubles.Matrix;
import com.numericalmethod.suanshu.algebra.linear.matrix.doubles.matrixtype.dense.DenseMatrix;
import com.numericalmethod.suanshu.algebra.linear.matrix.doubles.operation.Inverse;
import com.numericalmethod.suanshu.algebra.linear.vector.doubles.Vector;
import com.numericalmethod.suanshu.algebra.linear.vector.doubles.dense.DenseVector;
import com.numericalmethod.suanshu.stats.regression.linear.LMProblem;
import com.numericalmethod.suanshu.stats.regression.linear.ols.OLSRegression;

public class OLSRegressionTest {
	public static void main(String [] args) {
		Vector y = new DenseVector(
				1d,
				2d,
				3d,
				4d);
		Matrix X = new DenseMatrix(new double[][]{
				{1.12d, 2.23d},
				{2.23d, 4.45d},
				{3.34d, 6.67d},
				{4.45d, 8.89d},
		});
		LMProblem problem = new LMProblem(y, X);
		OLSRegression regression = new OLSRegression(problem);
//		System.out.println(regression.beta().betaHat());
//		System.out.println(X);
		Matrix Xt = X.t();
		System.out.println(new Inverse(Xt.multiply(X)).multiply(Xt.multiply(y)));
	}
}
