package michael.findata.demo.pca;

import com.numericalmethod.suanshu.algebra.linear.matrix.doubles.Matrix;
import com.numericalmethod.suanshu.algebra.linear.matrix.doubles.matrixtype.dense.DenseMatrix;
import com.numericalmethod.suanshu.stats.pca.PCA;
import com.numericalmethod.suanshu.stats.pca.PCAbyEigen;
import com.numericalmethod.suanshu.stats.pca.PCAbySVD;

public class PCATest {
	public static void main(String [] args) {
//		Matrix X = new DenseMatrix(new double[][]{
//				{1, 4},
//				{10, 40},
//				{100, 400},
//				{1000, 4000},
//				{10000, 40000},
//				{100000, 400000},
//				{1000000, 4000000},
//		});
		Matrix X = new DenseMatrix(new double[][]{
				{1, 2, 3, 4, 5, },
//				{0.1, .2, .3, .4, .5, },
				{-1, 2, -3, 4, -5, },
//				{1, 2, 3, 4, 5, },
//				{2, 4, 6, 8, 10, },
//				{3, 6, 9, 12, 15, },
//				{4, 8, 12, 16, 20, },
		});
		System.out.println(new PCAbySVD(X).svd().U());
		System.out.println(new PCAbyEigen(X).V());
	}
}