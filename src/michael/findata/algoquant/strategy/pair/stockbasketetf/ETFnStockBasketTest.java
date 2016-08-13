package michael.findata.algoquant.strategy.pair.stockbasketetf;

import com.numericalmethod.algoquant.execution.component.chart.ChartUtils;
import com.numericalmethod.pluto.api.Chart;
import com.numericalmethod.suanshu.algebra.linear.matrix.doubles.matrixtype.dense.DenseMatrix;
import com.numericalmethod.suanshu.algebra.linear.matrix.doubles.matrixtype.dense.diagonal.DiagonalMatrix;
import com.numericalmethod.suanshu.algebra.linear.vector.doubles.Vector;
import com.numericalmethod.suanshu.algebra.linear.vector.doubles.dense.DenseVector;
import com.numericalmethod.suanshu.stats.descriptive.moment.Variance;
import com.numericalmethod.suanshu.stats.descriptive.rank.Max;
import com.numericalmethod.suanshu.stats.descriptive.rank.Min;
import com.numericalmethod.suanshu.stats.regression.linear.LMProblem;
import com.numericalmethod.suanshu.stats.regression.linear.ols.OLSRegression;
import com.numericalmethod.suanshu.stats.test.timeseries.adf.AugmentedDickeyFuller;
import com.numericalmethod.suanshu.stats.test.timeseries.adf.TrendType;
import com.sun.jna.ptr.IntByReference;
import michael.findata.external.SecurityTimeSeriesDatum;
import michael.findata.service.DividendService;
import michael.findata.service.StockPriceMinuteService;
import michael.findata.service.StockService;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.*;

import static org.apache.commons.math3.util.FastMath.log;

public class ETFnStockBasketTest {
	public static void main(String args[]) throws IOException {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
//		SecurityTimeSeriesDataService stsds = (SecurityTimeSeriesDataService) context.getBean("securityTimeSeriesDataService");
		StockPriceMinuteService spms = (StockPriceMinuteService) context.getBean("stockPriceMinuteService");
		StockService ss = (StockService) context.getBean("stockService");
		DividendService ds = (DividendService) context.getBean("dividendService");

		String mainStock = "510050";
		Set<String> banks = ss.getStockGroup("michael/findata/algoquant/strategy/pair/group_stock_bank.csv");
		Set<String> insurances = ss.getStockGroup("michael/findata/algoquant/strategy/pair/group_stock_insurance.csv");
		Set<String> brokers = ss.getStockGroup("michael/findata/algoquant/strategy/pair/group_stock_broker.csv");
		Set<String> all = new HashSet<>();
		all.addAll(banks);
		all.addAll(insurances);
		all.addAll(brokers);
		all.add(mainStock);

		String[] allCodes = all.toArray(new String[all.size()]);
		DateTime start = new DateTime(2016, 4, 22, 0, 0);
		DateTime end = new DateTime(2016, 5, 22, 0, 0);
		DividendService.PriceAdjuster pa = ds.newPriceAdjuster(start.toLocalDate(), new LocalDate(2016, 7, 29), allCodes);
		IntByReference tickCountRefResult = new IntByReference(), outSampleTickCount = new IntByReference();

		Map<String, Vector> pVectors = getPrices(allCodes, spms, start, end, pa, tickCountRefResult);
		int sampleTicks = tickCountRefResult.getValue();

		Map<String, Vector> pVectorsOutSample = getPrices(allCodes, spms, new DateTime(2016, 5, 23, 0, 0), new DateTime(2016, 7, 29, 0, 0), pa, outSampleTickCount);
		int outSampleTicks = outSampleTickCount.getValue();
		DenseMatrix outSampleX = new DenseMatrix(outSampleTicks, 3);
		Vector outSampleY = pVectorsOutSample.get(mainStock);

		Vector y = pVectors.get(mainStock);
		DenseMatrix x = new DenseMatrix(sampleTicks, 3);
		int found = 0;

		all.remove(mainStock);
		allCodes = all.toArray(new String[all.size()]);
		for (int i = allCodes.length - 1; i > -1; i--) {
			x.setColumn(1, pVectors.get(allCodes[i]));
			for (int j = i - 1; j > -1; j--) {
				x.setColumn(2, pVectors.get(allCodes[j]));
				for (int k = j - 1; k > -1; k --) {
					x.setColumn(3, pVectors.get(allCodes[k]));
					LMProblem problem = new LMProblem(y, x, false);
					OLSRegression regression = new OLSRegression(problem);
					Vector beta = regression.beta().betaHat();
					Vector weights = new DiagonalMatrix(x.getRow(sampleTicks).toArray()).multiply(beta);
					weights = weights.scaled(1/weights.get(1));
					// make sure the basket is balanced in $ terms
					if (weights.get(2) > 2 || weights.get(2) < 0.5 || weights.get(3) > 2 || weights.get(3) < 0.5) {
						continue;
					}
					double[] residuals = x.multiply(beta).divide(y).minus(1).toArray();
					double adfp = new AugmentedDickeyFuller(residuals, TrendType.NO_CONSTANT, 300, null).pValue();
					if (adfp < 0.03) {
						// out-sample verification
						outSampleX.setColumn(1, pVectorsOutSample.get(allCodes[i]));
						outSampleX.setColumn(2, pVectorsOutSample.get(allCodes[j]));
						outSampleX.setColumn(3, pVectorsOutSample.get(allCodes[k]));
						double [] outSampleResiduals = outSampleX.multiply(beta).divide(outSampleY).minus(1).toArray();
						double outSampleAdfP = new AugmentedDickeyFuller(outSampleResiduals, TrendType.NO_CONSTANT, 300, null).pValue();

						if (outSampleAdfP < 0.02) {
							found ++;
							String title = mainStock+" vs "+allCodes[i]+" "+allCodes[j]+" "+allCodes[k]+" OS_Adf_P "+outSampleAdfP;
							double std = new Variance(residuals).standardDeviation();
							double max = new Max(residuals).value();
							double min = new Min(residuals).value();
//							double halfLife = calculateMRHalfLife(residuals);

							System.out.println(title);
							System.out.println("------------In-sample parameters------------");
							System.out.println("Beta: " + beta);
							System.out.println("Beta in $: "+weights);
							System.out.printf("Std: %.7f\n", std);
							System.out.printf("Max: %.7f\n", max);
							System.out.printf("Min: %.7f\n", min);
							System.out.printf("Adf_P in-sample: %.2f\n", adfp);
							System.out.printf("Adf_P out-sample: %.2f\n\n", outSampleAdfP);

//							System.out.println("MR Speed: "+ halfLife);
//							System.out.println();
							Chart chart = ChartUtils.plotXYSeries(
									title,
									"x",
									"y",
									new String[]{"In-Sample", "Out-Sample"},
									new double[][]{
											residuals,
											outSampleResiduals
									});
							chart.show();
						}
					}
				}
			}
		}
		System.out.println("Found pairs: "+found);
	}

	private static Map<String, Vector> getPrices (String [] codes,
												 StockPriceMinuteService spms,
												 DateTime start,
												 DateTime end,
												 DividendService.PriceAdjuster pa,
												 IntByReference resultCount) {
		LocalDate startDate = start.toLocalDate();
		HashMap<String, ArrayList<Double>> prices = new HashMap<>();
//		HashMap<String, Vector> pVectors = new HashMap<>();
		for (String code : codes) {
			prices.put(code, new ArrayList<>());
		}
		final int[] count = {0};
		spms.walk(
				start,
				end,
//				1000000,
				codes, false,
				(date, data) -> {
					// step 1: make sure no stock is missing
					LocalDate curDate = date.toLocalDate();
					if (data.size() != codes.length) return;
					for (SecurityTimeSeriesDatum datum : data.values()) {
						// if a price for a stock/etf is missing, invalidate this time point
						if (datum.getClose() <= 0 || !datum.isTraded()) {
							return;
						}
					}

					// step 2: store adjusted prices:
					for (Map.Entry<String, SecurityTimeSeriesDatum> e : data.entrySet()) {
						String code = e.getKey();
						SecurityTimeSeriesDatum datum = e.getValue();
						double adj = pa.adjust(code, startDate, curDate, datum.getClose()) / 1000d;
						prices.get(code).add(adj);
					}

//					System.out.println(count[0] + " " + date);
					count[0]++;
				});
		HashMap<String, Vector> pVectors = new HashMap<>();
		for (String code : codes) {
			pVectors.put(code, new DenseVector(prices.get(code)));
		}
		resultCount.setValue(count[0]);
		return pVectors;
	}

	public static double calculateMRHalfLife(double [] residuals) {
		DenseVector v = new DenseVector(Arrays.copyOfRange(residuals, 1, residuals.length));
//		System.out.println(v);
		DenseVector vPrevious = new DenseVector(Arrays.copyOfRange(residuals, 0, residuals.length-1));
//		System.out.println(vMinusOne);
		v = v.minus(vPrevious);
//		System.out.println(v);
		LMProblem problem = new LMProblem(v, new DenseMatrix(vPrevious.toArray(), residuals.length - 1, 1), false);
//		LMProblem problem = new LMProblem(v, new DenseMatrix(vMinusOne), false);
		OLSRegression regression = new OLSRegression(problem);
		return -log(2)/regression.beta().betaHat().get(1)/24;
	}
}