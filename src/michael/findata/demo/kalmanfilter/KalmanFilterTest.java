package michael.findata.demo.kalmanfilter;

import michael.findata.external.SecurityTimeSeriesDatum;
import michael.findata.service.SecurityTimeSeriesDataService;
import michael.findata.util.Consumer2;
import org.apache.commons.math3.filter.*;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.HashMap;

public class KalmanFilterTest {
	public static void main (String [] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
//		DividendService ds = (DividendService) context.getBean("dividendService");
		SecurityTimeSeriesDataService stsds = (SecurityTimeSeriesDataService) context.getBean("securityTimeSeriesDataService");
		LocalDate startDate = LocalDate.parse("2016-05-23");
		LocalDate endDate = LocalDate.parse("2016-06-03");

		double constantVoltage = 3.3d;
		double measurementNoise = 0.00001d;
		double processNoise = 0.0000d;

// A = [ 1 ]
		RealMatrix A = new Array2DRowRealMatrix(new double[] { 1d });
// B = null
		RealMatrix B = null;
// x = [ 10 ]
		RealVector x = new ArrayRealVector(new double[] { constantVoltage });
// Q = [ 1e-5 ]
		RealMatrix Q = new Array2DRowRealMatrix(new double[] { processNoise });
// P = [ 1 ]
		RealMatrix P0 = new Array2DRowRealMatrix(new double[] { 1d });

		ProcessModel pm = new DefaultProcessModel(A, B, Q, x, P0);

// H = [ 1 ]
		RealMatrix H = new Array2DRowRealMatrix(new double[] { 1d });
// R = [ 0.1 ]
		RealMatrix R = new Array2DRowRealMatrix(new double[] { measurementNoise });

		MeasurementModel mm = new DefaultMeasurementModel(H, R);

		KalmanFilter filter = new KalmanFilter(pm, mm);

// process and measurement noise vectors
//		RealVector pNoise = new ArrayRealVector(1);
//		RealVector mNoise = new ArrayRealVector(1);

		RandomGenerator rand = new JDKRandomGenerator();

		Consumer2<DateTime, HashMap<String, SecurityTimeSeriesDatum>> doTest = (date, data) -> {
			SecurityTimeSeriesDatum datumA = data.get("160706");
			SecurityTimeSeriesDatum datumB = data.get("159919");
			if (datumA.isTraded() && datumB.isTraded()) {
				// predict the state estimate one time-step ahead
				// optionally provide some control input
				filter.predict();

				// obtain measurement
//				double quote = ((double)datumA.getClose())/datumB.getClose();
				double quote = ((double)datumA.getClose())/datumB.getClose();

				// correct the state estimate with the latest measurement
				filter.correct(new double[] {quote});

				double[] stateEstimate = filter.getStateEstimation();
				// do something with it
				System.out.println(date.toString("yyyy-MM-dd-hh:mm")+"\t"+quote+"\t"+stateEstimate[0]);
			}
		};

		stsds.walkMinutes(
				startDate.toDateTimeAtStartOfDay(),
				endDate.toDateTimeAtStartOfDay().plusHours(23),
				999999, new String [] {"160706", "159919"}, false, doTest);
	}
}
