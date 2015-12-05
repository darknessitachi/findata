package michael.findata.external.tdx.test;

import com.numericalmethod.suanshu.stats.descriptive.rank.Quantile;
import michael.findata.external.SecurityTimeSeriesDatum;
import michael.findata.external.tdx.TDXMinuteLine;
import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static michael.findata.util.FinDataConstants.yyyyMMdd;

/**
 * Created by nicky on 2015/11/15.
 */
public class TDXMinuteLineTest {
	public static void main (String [] args) throws ParseException {
		ArrayList<Double> stats = new ArrayList<>();
		String codeA = "510180";
		String codeB = "512990";
		TDXMinuteLine seriesA = new TDXMinuteLine(codeA);
		TDXMinuteLine seriesB = new TDXMinuteLine(codeB);
		SecurityTimeSeriesDatum quoteA, quoteB;
		DateTime start = new DateTime(new SimpleDateFormat(yyyyMMdd).parse("20151122"));
		// Now calculate baseline moving average ratio
		// 4 Day ma to calculate ratio, 5 days operating window
		int maLimit = 15 * 240, opLimit = 10 * 240, tCounter = 0;
		int maCounter = 0;
		int totalA = 0, totalB = 0;
		double ratio = 0;
		while (seriesA.hasNext() && seriesB.hasNext()) {
			quoteA = seriesA.next();
			quoteB = seriesB.next();
			tCounter ++;
			if (quoteA.getDateTime().isBefore(start)) {
				maCounter++;
				if (maCounter > maLimit) {
					break;
				}
				totalA += quoteA.getClose();
				totalB += quoteB.getClose();
				stats.add(((double) quoteA.getClose()) / quoteB.getClose());
				System.out.print(quoteA.getDateTime() + "\t" + "\t" + quoteA.getClose() + "\t");
				System.out.print(quoteB.getDateTime() + "\t" + "\t" + quoteB.getClose() + "\t");
				System.out.println();
			}
		}

		// enough accumulated, calculate ratio
//		ratio = ((double) totalA) / totalB;
		// Calculate 50% quantile
		double [] statistics = stats.stream().mapToDouble(Double::doubleValue).toArray();
		Quantile q = new Quantile(statistics);
		ratio = q.value(0.5d);
		double perct1 = q.value(0.03d);
		double perctOpen = q.value(0.97d);
		System.out.println("50%:\t" + ratio);
//		System.out.println("5%: \t" + perct1);
		System.out.println("97%:\t" + perctOpen);

		seriesA.close();
		seriesB.close();

		seriesA = new TDXMinuteLine(codeA);
		seriesB = new TDXMinuteLine(codeB);
		int oc = 0;
		int oStart = tCounter - opLimit - maLimit;
		int oEnd = tCounter - maLimit;
		System.out.println("tCounter " + tCounter);
		while (seriesA.hasNext() && seriesB.hasNext()) {
			quoteA = seriesA.next();
			quoteB = seriesB.next();
			if (oc + 2 > oStart) {
				double current = ((double) quoteA.getClose()) / quoteB.getClose();
//				if (current < perct1 || current > perct99) {
					if (true) {
					System.out.print(quoteA.getDateTime() + "\t" + "\t" + quoteA.getClose() + "\t");
					System.out.print(quoteB.getDateTime() + "\t" + "\t" + quoteB.getClose() + "\t");
					System.out.print(current+"\t");
					System.out.print(oc);
					System.out.println();
				}
			}
			if (oc + 3 > oEnd) {
				break;
			}
			oc ++;
		}
	}
}