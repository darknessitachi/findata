package michael.findata.algoquant.strategy.pair;

import com.numericalmethod.algoquant.execution.datatype.product.stock.Stock;
import com.numericalmethod.suanshu.stats.test.timeseries.adf.AugmentedDickeyFuller;
import michael.findata.algoquant.strategy.ETFPair;
import michael.findata.algoquant.strategy.Pair;
import michael.findata.external.shse.SHSEShortableStockList;
import michael.findata.external.szse.SZSEShortableStockList;
import michael.findata.service.*;
import michael.findata.util.Consumer5;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.joda.time.DateTime;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static michael.findata.algoquant.strategy.Pair.PairStatus.*;
import static michael.findata.algoquant.strategy.Pair.PairStatus.OPENED;
import static michael.findata.util.FinDataConstants.yyyyMMDDHHmmss;
import static michael.findata.util.FinDataConstants.yyyyMMdd;

/**
 * Created by nicky on 2015/11/27.
 */
public class PairsSearch {
	public static void main (String [] args) throws IOException, ParseException {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/findata_spring.xml");
//		ReportPubDateService spds = (ReportPubDateService) context.getBean("reportPubDateService");
//		StockService ss = (StockService) context.getBean("stockService");
//		FinDataService fds = (FinDataService) context.getBean("finDataService");
//		DividendService ds = (DividendService) context.getBean("dividendService");
//		ShareNumberChangeService sncs = (ShareNumberChangeService) context.getBean("shareNumberChangeService");
		StockPriceService sps = (StockPriceService) context.getBean("stockPriceService");
		SecurityTimeSeriesDataService stsds = (SecurityTimeSeriesDataService) context.getBean("securityTimeSeriesDataService");

		pairSearch(sps, stsds);
	}
	// TODO parameterize this, lots need to be parameterized
	public static void pairSearch(StockPriceService sps, SecurityTimeSeriesDataService stsds) throws ParseException, IOException {
		SimpleDateFormat sdfDisplay = new SimpleDateFormat(yyyyMMDDHHmmss);
		SimpleDateFormat sdf = new SimpleDateFormat(yyyyMMdd);
		SortedMap<String, Counts> counts = new TreeMap<>();
		Arrays.stream(filter("20150902", "20151101", 100000, StockGroups.ETF, sps, stsds))
				.flatMap(pair -> simulate("20151102", "20151121", "20151202", pair, stsds).stream())
				.sorted().forEach(pair1 -> {
			int age;
			System.out.print(pair1.toShort.symbol().substring(0, 6) + " -> " + pair1.toLong.symbol().substring(0, 6) + " slope: " + pair1.slope + " stdev: " + pair1.stdev);
			switch (pair1.status) {
				case OPENED:
					System.out.print("\tOpen: Short A, Long B\t\t\t\t");
					System.out.println(sdfDisplay.format(pair1.dateOpened.toDate()) + "\t" + pair1.shortOpen + "\t" + pair1.longOpen + "\t" + pair1.minResidual + "\t\t\t\t" + pair1.maxAmountPossibleOpen);
					updateCount(counts, sdf.format(pair1.dateOpened.toDate()), CountType.OPEN);
					break;
				case CLOSED:
					age = pair1.closureAge();
					System.out.print((age == 0 ? "\tSame-day closure\t" : "\tClosure\t") + pair1.thresholdClose / pair1.stdev + "\tfee\t" + pair1.feeEstimate() + "\t");
					System.out.print(sdfDisplay.format(pair1.dateClosed.toDate()) + "\t" + pair1.shortClose + "\t" + pair1.longClose + "\t" + pair1.minResidual + "\t" + (age == 0 ? 1 : age));
					System.out.println("\tProfit:\t" + pair1.profitPercentageEstimate() + "\t" + pair1.maxAmountPossibleClose);
					updateCount(counts, sdf.format(pair1.dateClosed.toDate()), (age == 0 ? CountType.SAME_DAY_CLOSE : CountType.CLOSE));
					break;
				case FORCED:
					age = pair1.closureAge();
					System.out.print("\tForce closure\t"+pair1.thresholdClose / pair1.stdev + "\tfee\t" + pair1.feeEstimate() + "\t");
					System.out.print(sdfDisplay.format(pair1.dateClosed.toDate()) + "\t" + pair1.shortClose + "\t" + pair1.longClose + "\t" + pair1.minResidual + "\t" + (age == 0 ? 1 : age));
					System.out.println("\tProfit/Loss:\t" + pair1.profitPercentageEstimate() + "\t" + pair1.maxAmountPossibleClose);
					updateCount(counts, sdf.format(pair1.dateClosed.toDate()), (age == 0 ? CountType.SAME_DAY_CLOSE : CountType.CLOSE));
					break;
			}
		});
		System.out.println("Date\tOpen#\tClose#\tSame Day Close#");
		counts.entrySet().stream().forEach(entry -> {
			System.out.println(entry.getKey() + "\t" + entry.getValue().open + "\t" + entry.getValue().close + "\t" + entry.getValue().sameDayClose);
		});
	}

	private enum CountType {
		OPEN,
		CLOSE,
		SAME_DAY_CLOSE
	}

	private static class Counts {
		int open = 0;
		int close = 0;
		int sameDayClose = 0;
	}

	private static void updateCount (Map<String, Counts> countMap, String date, CountType type) {
		Counts c;
		if (!countMap.containsKey(date)) {
			c = new Counts();
		} else {
			c = countMap.get(date);
		}
		switch (type) {
			case OPEN:
				c.open ++;
				break;
			case CLOSE:
				c.close ++;
				break;
			case SAME_DAY_CLOSE:
				c.sameDayClose ++;
				break;
		}
		countMap.put(date, c);
	}

	private static Pair[] filter (String trainingStart, String trainingEnd,
								  int maxSteps, Stock[] stocks, StockPriceService sps,
								  SecurityTimeSeriesDataService stsds) throws ParseException, IOException {

		Set<String> shortables = new SHSEShortableStockList().getShortables();
		shortables.addAll(new SZSEShortableStockList().getShortables());

		SimpleDateFormat sdf = new SimpleDateFormat(yyyyMMdd);
		DateTime startTraining = new DateTime(sdf.parse(trainingStart));
		DateTime endTraining = new DateTime(sdf.parse(trainingEnd)).withHourOfDay(23);

		String codeA;
		String codeB;
		ArrayList<Pair> pairs = new ArrayList<>();
		for (int i = 0; i < stocks.length; i++) {
			codeA = stocks[i].symbol().substring(0, 6);
			for (int j = i + 1; j < stocks.length; j++) {
				codeB = stocks[j].symbol().substring(0, 6);
				if (!shortables.contains(codeA)) {
					if (!shortables.contains(codeB)) {
						break;
					}
					codeA += "*";
				} else if (!shortables.contains(codeB)) {
					codeB += "*";
				}
				System.out.print(codeA+"\t"+codeB+"\t");
				codeA = codeA.substring(0, 6);
				codeB = codeB.substring(0, 6);

				// Use file data to do days test
//				cointcorrel(startTraining, endTraining, codeA, codeB, 1000000, stsds, sps, false);
				try {
					// Use file data to do minutes test
					double [] result = cointcorrel(startTraining, endTraining, codeA, codeB, maxSteps, 0.7, 0.05, stsds, sps, true);
					if (null != result) {
						// Make this pair
						if (shortables.contains(codeA)) {
							pairs.add(new ETFPair(stocks[i], stocks[j], result[0], result[1]));
						}
						if (shortables.contains(codeB)) {
							pairs.add(new ETFPair(stocks[j], stocks[i], 1/result[0], result[1]/result[0]));
						}
					}
				} catch (Exception e) {}
				System.out.print("\t");
				try {
					// Use db data to do days test
//					cointcorrel(startTraining, endTraining, codeA, codeB, maxSteps, 0.7, 0.05, null, sps, false);
				} catch (Exception e) {}
				System.out.println();
			}
		}
		return pairs.toArray(new Pair[pairs.size()]);
	}

	private static List<Pair> simulate (String simStart, String openStop, String simEnd, Pair pair, SecurityTimeSeriesDataService stsds) {
		float amountPerSlot = 15000f;
		pair.thresholdOpen = pair.stdev * 3;
		DateTime startSim;
		DateTime endSim;
		DateTime stopOpen;
		SimpleDateFormat sdf = new SimpleDateFormat(yyyyMMdd);
		List<Pair> executions = new ArrayList<>();
		try {
			startSim = new DateTime(sdf.parse(simStart));
			stopOpen = new DateTime(sdf.parse(openStop));
			endSim = new DateTime(sdf.parse(simEnd)).withHourOfDay(23);
		} catch (ParseException pe) {
			pe.printStackTrace();
			return executions;
		}

		// find ops
		String codeA = pair.toShort.symbol().substring(0, 6);
		String codeB = pair.toLong.symbol().substring(0, 6);
		stsds.walkMinutes(startSim, endSim, 1000000, codeA, codeB, false,
				(dateTime, priceA, priceB, amountA, amountB) -> {
					double residual = priceA * pair.slope - priceB;
					long age = 0;
					if (pair.status == OPENED || pair.status == CLOSED || pair.status == FORCED) {
						age = pair.age(dateTime);
					}
//					if ((dateTime.withMillisOfDay(1).compareTo(endSim.withMillisOfDay(1)) == 0) && dateTime.getMinuteOfDay() > 880 )
//						System.out.println(sdfDisplay.format(dateTime.toDate()) + "\t\t" + priceA + "\t" + priceB + "\t"+residual+"\t"+(dateTime.getMinuteOfDay() == 890 || (residual < pair.stdev * 2.5))+"\t"+pair.status);
					// forcefully close positions 10 minutes before ending
					if (dateTime.withMillisOfDay(1).compareTo(endSim.withMillisOfDay(1)) == 0 && pair.status == OPENED) {
						if (dateTime.getMinuteOfDay() >= 890 || (residual < pair.stdev * 3 && amountA >= amountPerSlot && amountB >= amountPerSlot)) {
							pair.status = FORCED;
							pair.dateClosed = dateTime;
							pair.shortClose = priceA;
							pair.longClose = priceB;
							pair.thresholdClose = residual;
							pair.maxAmountPossibleClose = Math.min(amountA, amountB);
//							System.out.print(codeA + " -> " + codeB + " slope: " + pair.slope + " stdev: " + pair.stdev);
//							System.out.print("\tForce closure\t"+pair.thresholdClose/pair.stdev+"\tfee\t" + pair.feeEstimate() + "\t");
//							System.out.print(sdfDisplay.format(dateTime.toDate()) + "\t" + priceA + "\t" + priceB + "\t" + pair.minResidual + "\t" + (age == 0 ? 1 : age));
//							System.out.println("\tProfit/Loss:\t" + pair.profitPercentageEstimate() + "\t" + pair.maxAmountPossibleClose);
							executions.add(pair.copy());
							pair.reset();
							return;
						}
					}

					if (pair.status == OPENED) {
						if (Math.abs(amountA) < amountPerSlot || Math.abs(amountB) < amountPerSlot)
							return; // do not close if vol = 0
						pair.minResidual = pair.minResidual > residual ? residual : pair.minResidual;
						if (age <= 7) {
							pair.thresholdClose = pair.stdev * 0.7;
						} else if (age <= 13) {
							pair.thresholdClose = pair.stdev * 2.0;
						} else {
							pair.thresholdClose = pair.stdev * 2.8;
						}
						if (residual < pair.thresholdClose) {
//							double fee = 4 * 0.0003 + (age==0?1:age)* 0.1085 / 360; // ETF cost
							pair.dateClosed = dateTime;
							pair.shortClose = priceA;
							pair.longClose = priceB;
							pair.status = CLOSED;
							pair.minResidual = residual;
							pair.maxAmountPossibleClose = Math.min(amountA, amountB);
//							System.out.print(codeA + " -> " + codeB + " slope: " + pair.slope + " stdev: " + pair.stdev);
//							System.out.print((age == 0 ? "\tSame-day closure\t" : "\tClosure\t") + pair.thresholdClose / pair.stdev + "\tfee\t" + pair.feeEstimate() + "\t");
//							System.out.print(sdfDisplay.format(dateTime.toDate()) + "\t" + priceA + "\t" + priceB + "\t" + pair.minResidual + "\t" + (age == 0 ? 1 : age));
//							System.out.println("\tProfit:\t" + pair.profitPercentageEstimate() + "\t" + pair.maxAmountPossibleClose);
							executions.add(pair.copy());
							pair.reset();
						}
					} else if (pair.status == NEW && residual >= pair.thresholdOpen && dateTime.compareTo(stopOpen) < 0) {
						if (Math.abs(amountA) < amountPerSlot || Math.abs(amountB) < amountPerSlot)
							return; // do not open if vol = 0
						pair.status = OPENED;
						pair.shortOpen = priceA;
						pair.longOpen = priceB;
						pair.dateOpened = dateTime;
						pair.minResidual = residual;
						pair.maxAmountPossibleOpen = Math.min(amountA, amountB);
//						System.out.print(codeA + " -> " + codeB + " slope: " + pair.slope + " stdev: " + pair.stdev);
//						System.out.print("\tOpen: Short A, Long B\t\t\t\t");
//						System.out.println(sdfDisplay.format(dateTime.toDate()) + "\t" + priceA + "\t" + priceB + "\t" + residual + "\t\t\t\t" + pair.maxAmountPossibleOpen);
						executions.add(pair.copy());
					}
				}
		);
//		System.out.println();
		return executions;
	}

	/**
	 *	if stsds is not null then {
	 *		use stsds to do tests
	 *		minutesOrDays: true - minutes - do minutes test
	 *		minutesOrDays: false - days - do days test
	 *	} else {
	 *		use sps to do days test
	 *	}
	 *	Print output: 	[Correlation][n-Sample][regression slop][adf p-value][residual standard deviation][reference price]
	 *	Return value: if fail test - null
	 *				  if pass test - [slope, stdev]
	 */
	private static double[] cointcorrel (DateTime startTraining, DateTime endTraining, String codeA, String codeB, int maxSteps,
										 double correlThreshold, double cointThreshold,
										 SecurityTimeSeriesDataService stsds,
										 StockPriceService sps,
										 boolean minutesOrDays) {
		SimpleRegression regression = new SimpleRegression (false);
		PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();

		ArrayList<Double> pA = new ArrayList<>();
		ArrayList<Double> pB = new ArrayList<>();

		Consumer5<DateTime, Double, Double, Float, Float> doTest = (dateTime, prA, prB, amountA, amountB) -> {
//			System.out.println(date + "\t" + prA + "\t" + prB);
//			if (volA == 0 || volB == 0) return;
			regression.addData(prA, prB);
			pA.add(prA);
			pB.add(prB);
		};

		if (stsds != null) {
			if (minutesOrDays) {
				stsds.walkMinutes(startTraining, endTraining, maxSteps, codeA, codeB, false, doTest);
			} else {
				stsds.walkDays(startTraining, endTraining, maxSteps, codeA, codeB, false, doTest);
			}
		} else {
			sps.walk(startTraining, endTraining, maxSteps, codeA, codeB, false, doTest);
		}

		double [] priceListA, priceListB;
		priceListA = pA.stream().mapToDouble(d -> d).toArray();
		priceListB = pB.stream().mapToDouble(d -> d).toArray();

		// Correlation coefficient
		double correl = pearsonsCorrelation.correlation(priceListA, priceListB);
		System.out.print(correl);

		long ticks = regression.getN();
		System.out.print("\t"+ticks);

		// Regression Parameter slope: pB = slope * pA;
		double slope = regression.getSlope();
		System.out.print("\t"+slope);

		// ADF test for regression residuals on previously collected end-of-day data
		double [] residuals = new double[priceListA.length];
		for (int i = 0; i <priceListA.length; i++) {
			// Calculate residuals according to parameters obtains from linear regression
			// ie. residual = quoteB - slope * quoteA
			residuals[i] = priceListB[i] - priceListA[i] * slope;
		}
		double adf_p = new AugmentedDickeyFuller(residuals).pValue();
		System.out.print("\t" + adf_p);

		// ADF test for regression residuals on minute data
//		ArrayList<Double> res = new ArrayList<>();
//		stsds.walkMinutes(startTraining, endTraining, 100000, codeA, codeB, false,
//				(date, prA, prB) -> {
//					// Calculate residuals according to parameters obtains from linear regression
//					// ie. residual = quoteB - slope * quoteA
//					res.add(prB - prA * slope);
//				}
//		);
//		double [] residuals = res.stream().mapToDouble(d->d).toArray();
//		adf_p = new AugmentedDickeyFuller(residuals).pValue();
//		System.out.println("adf p: " + adf_p);

		// Residual standard deviation
		double std = new StandardDeviation().evaluate(residuals);
		System.out.print("\t"+std);
		System.out.print("\t"+priceListB[0]);

		if (correl >= correlThreshold && adf_p <= cointThreshold) {
			return new double[] {slope, std};
		} else {
			return null;
		}
	}
}
