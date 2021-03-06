package michael.findata.service;

import com.numericalmethod.algoquant.data.calendar.HolidayCalendarFromYahoo;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import com.numericalmethod.algoquant.execution.datatype.product.stock.Exchange;
import com.numericalmethod.suanshu.stats.descriptive.rank.Max;
import com.numericalmethod.suanshu.stats.descriptive.rank.Min;
import com.numericalmethod.suanshu.stats.descriptive.rank.Quantile;
import com.numericalmethod.suanshu.stats.test.timeseries.adf.AugmentedDickeyFuller;
import com.numericalmethod.suanshu.zzz.con.prn.aux.nul.D;
import michael.findata.algoquant.strategy.PairStrategy;
import michael.findata.algoquant.strategy.pair.stocks.ShortInHKPairStrategy;
import michael.findata.demo.aparapi.simpleregression.OcRegressionFloat;
import michael.findata.external.SecurityTimeSeriesDatum;
import michael.findata.external.netease.NeteaseInstantSnapshot;
import michael.findata.external.shse.SHSEShortableStockList;
import michael.findata.external.szse.SZSEShortableStockList;
import michael.findata.external.tdx.TDXClient;
import michael.findata.model.*;
import michael.findata.spring.data.repository.*;
import michael.findata.util.*;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.commons.math3.util.DoubleArray;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

import static michael.findata.algoquant.execution.strategy.Strategy.LOGGER;
import static michael.findata.algoquant.strategy.pair.stocks.ShortInHKPairStrategy.paramMap;

@Service
public class PairStrategyService {

	@Autowired
	StockRepository stockRepo;

	@Autowired
	PairRepository pairRepo;

	@Autowired
	PairStatsRepository pairStatsRepo;

	@Autowired
	PairInstanceRepository pairInstanceRepo;

	@Autowired
	CacheRepository cacheRepo;

	@Autowired
	SecurityTimeSeriesDataService stsds;

	@Autowired
	StockPriceMinuteService spms;

	@Autowired
	NeteaseInstantSnapshotService niss;

	@Autowired
	StockPriceService sps;

	@Autowired
	DividendService ds;

	@Autowired
	StockService ss;

	@Autowired
	ShortInHkPairStrategyRepository shortInHkPairStrategyRepo;

	@Transactional(propagation = Propagation.REQUIRED)
	public void updatePairs (String ... codes) throws IOException {
		Set<Pair> currentPairs = new HashSet<>();
		long allStart = System.currentTimeMillis();
		long start = allStart;
		long end;
		pairRepo.findAll().forEach(currentPairs::add);
		Set<Stock> stocks = stockRepo.findByCodeIn(codes);
		end = System.currentTimeMillis();
		System.out.println("updatePairs step 1(s): "+(end - start)/1000d);
		start = end;
//		Set<String> shortables = getShortablesUpdateIfRequired();
		end = System.currentTimeMillis();
		System.out.println("updatePairs step 2(s): "+(end - start)/1000d);
		start = end;
		for (Stock toShort : stocks) {
//			if (shortables.contains(toShort.getCode())) {
				for (Stock toLong : stocks) {
					if (toLong.equals(toShort)) {
						continue;
					}
					Pair pair = new Pair();
					pair.setStockToShort(toShort);
					pair.setStockToLong(toLong);
					pair.setMaxAmountAllowed(0);
					pair.setEnabled(true);
					if (!currentPairs.contains(pair)) {
						pairRepo.save(pair);
					}
				}
//			}
		}
		end = System.currentTimeMillis();
		System.out.println("updatePairs step 3(s): "+(end - start)/1000d);
		System.out.println("updatePairs total(s): "+(end - allStart)/1000d);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public Set<String> getShortablesUpdateIfRequired () throws IOException {
		Cache shortablesCache = cacheRepo.findOneByName(Cache.CACHE_NAME_SHORTABLES);
		Set<String> result;
		if (System.currentTimeMillis() - shortablesCache.getLastUpdated().getTime() > FinDataConstants.SHORTABLES_UPDATE_THRESHOLD_MILLIS) {
			try {
				result = new SZSEShortableStockList().getShortables();
			} catch (Exception e) {
				result = ss.getStockGroup("classification_szseshortables.csv");
			}
			result.addAll(new SHSEShortableStockList().getShortables());
			shortablesCache.setLastUpdated(new Timestamp (System.currentTimeMillis()));
			shortablesCache.setValue(String.join(",", result));
			cacheRepo.save(shortablesCache);
		} else {
			result = new HashSet<>();
			Collections.addAll(result, shortablesCache.getValue().split(","));
		}
		return result;
	}

	// Step 1: calculate stats
	@Transactional(propagation = Propagation.REQUIRED)
	public void calculateStats(LocalDate trainingStart, LocalDate trainingEnd, int idGreaterThanThis, int idLessThanThis) {
		long start = System.currentTimeMillis();
		List<Pair> pairs = pairRepo.findByEnabledAndIdGreaterThanAndIdLessThan(true, idGreaterThanThis, idLessThanThis);
		String [][] pairCodes = new String [pairs.size()][];
		for (int i = 0; i < pairCodes.length; i++) {
			pairCodes[i] = new String [2];
			pairCodes[i][0] = pairs.get(i).getCodeToShort();
			pairCodes[i][1] = pairs.get(i).getCodeToLong();
		}
		double [][] result = cointcorrel(
				trainingStart.toDateTimeAtStartOfDay(),
				trainingEnd.toDateTimeAtStartOfDay().plusHours(23),
				pairCodes, 1000000, null, spms, sps, true);
		for (int i = result.length - 1; i > -1; i--) {
			PairStats stats = new PairStats();
			stats.setPair(pairs.get(i));
			stats.setTimeSeriesType(PairStats.TimeSeriesType.MINUTE);
			stats.setTrainingStart(trainingStart.toDate());
			stats.setTrainingEnd(trainingEnd.toDate());
			stats.setSlope(result[i][0]);
			stats.setStdev(result[i][1]);
			stats.setCorrelco(result[i][2]);
			stats.setAdfp(result[i][3]);
			stats.setMin(result[i][4]);
			stats.setPercentile01(result[i][5]);
			stats.setPercentile05(result[i][6]);
			stats.setPercentile95(result[i][7]);
			stats.setPercentile99(result[i][8]);
			stats.setMax(result[i][9]);
			pairStatsRepo.save(stats);
		}
		System.out.println("calculateStats total time (s): "+(System.currentTimeMillis() - start)/1000d);
	}

	// Step 2: update adf p value moving average
	@Transactional(propagation = Propagation.REQUIRED)
	public int updateAdfpMovingAverage (LocalDate date) {
		return pairStatsRepo.updateAdfpMovingAverage(date.toDate(), FinDataConstants.STRATEGY_PAIR_ADF_P_MA_WINDOW_DAYS);
	}

	// Step 3: update dividend and adj factor
	@Transactional(propagation = Propagation.REQUIRED)
	public void updateDividends () {
		Set<String> codes = new HashSet<>();
		pairRepo.findByEnabled(true).forEach(pair->{
			codes.add(pair.getCodeToLong());
			codes.add(pair.getCodeToShort());
		});
		TDXClient c = new TDXClient("218.6.198.155:7709");
		c.connect();
		for (String code : codes) {
			if (ds.refreshDividendDataForFund(code, c)) {
				ds.calculateAdjFactorForStock(code);
			}
		}
		c.disconnect();
	}

	public Collection<PairInstance> findSimpleOpenCloseOpportunities (LocalDate openStart, LocalDate openEnd, LocalDate startExe, LocalDate endExe, String[] codesToShort, String[] codesToLong) {
		LocalDate current = startExe;
		Collection<PairInstance> result = null;

		while (Days.daysBetween(current, endExe).getDays() >= 0) {
			if (result == null) {
				result = findSimpleOpenCloseOpportunities(openStart, openEnd, current, codesToShort, codesToLong);
			} else {
				result.addAll(findSimpleOpenCloseOpportunities(openStart, openEnd, current, codesToShort, codesToLong));
			}
			current = current.plusDays(1);
		}
		return result;
	}

	public Collection<PairInstance> findSimpleOpenCloseOpportunities (LocalDate openStart, LocalDate openEnd, LocalDate dateExe, String[] codesToShort, String[] codesToLong) {
		PairStrategy strategy = null;
		try {
			// find all pairs
			List<PairInstance> pairs = pairInstanceRepo.findByOpenableDateBetweenAndStats_CorrelcoGreaterThanAndStats_AdfpLessThanAndCodeToShortInAndCodeToLongIn
					(openStart.toDate(), openEnd.toDate(), 0.70d, 0.12d, codesToShort, codesToLong);
			Set<String> codes = new TreeSet<>();
			LocalDate earliest = dateExe;
			for (PairInstance pair : pairs) {
				codes.add(pair.getCodeToShort());
				codes.add(pair.getCodeToLong());
				if (pair.trainingStart().isBefore(earliest)) {
					earliest = pair.trainingStart();
				}
			}

			strategy = new PairStrategy(dateExe, ds.newPriceAdjuster(earliest, dateExe, codes.toArray(new String [codes.size()])), pairs);
			List<MarketCondition> mcs = niss.getDailyData(dateExe);
			if (mcs.size() > 0) {
				if (mcs.get(0) instanceof NeteaseInstantSnapshot) {
					for (MarketCondition mc : mcs) {
						strategy.onMarketConditionUpdate(((NeteaseInstantSnapshot)mc).getTick(), mc, null, null);
					}
				} else {
					DateTime tick = dateExe.toDateTimeAtStartOfDay().plusHours(1);
					for (MarketCondition mc : mcs) {
						strategy.onMarketConditionUpdate(tick, mc, null, null);
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return strategy.getExecutions();
	}

	//slope, std, correl, adf_p, min, 1% quantile, 5% quantile, 95% quantile, 99% quantile, max
	public double[][] cointcorrel (DateTime startTraining,
										DateTime endTraining,
										String [][] codePairs,
										int maxSteps,
										SecurityTimeSeriesDataService stsds,
								   		StockPriceMinuteService spms,
										StockPriceService sps,
										boolean minutesOrDays) {
		Set<String> codeSet = new HashSet<>();
		SimpleRegression [] regression = new SimpleRegression [codePairs.length];
//		TrivialRegressionDouble [] regression = new TrivialRegressionDouble[codePairs.length];
//		OcRegressionDouble [] regression = new OcRegressionDouble[codePairs.length];
//		OcRegressionDouble1 [] regression = new OcRegressionDouble1[codePairs.length];
//		OcRegressionLong [] regression = new OcRegressionLong[codePairs.length];
//		OcRegressionFloat [] regression = new OcRegressionFloat[codePairs.length];

		PearsonsCorrelation [] pearsonsCorrelation = new PearsonsCorrelation [codePairs.length];
		ArrayList<Double> [] pA = new ArrayList [codePairs.length];
		ArrayList<Double> [] pB = new ArrayList [codePairs.length];
		for (int i = codePairs.length - 1; i > -1; i--) {
			regression[i] = new SimpleRegression (false);
//			regression[i] = new TrivialRegressionDouble();
//			regression[i] = new OcRegressionDouble();
//			regression[i] = new OcRegressionDouble1();
//			regression[i] = new OcRegressionLong();
//			regression[i] = new OcRegressionFloat();

			pearsonsCorrelation[i] = new PearsonsCorrelation();
			pA[i] = new ArrayList<>();
			pB[i] = new ArrayList<>();
			codeSet.add(codePairs[i][0]);
			codeSet.add(codePairs[i][1]);
		}
		String [] codes = codeSet.toArray(new String [codeSet.size()]);
		HashMap<String, Double> prices = new HashMap<>();
		LocalDate startDate = startTraining.toLocalDate();
		LocalDate endDate = endTraining.toLocalDate();
		DividendService.PriceAdjuster pa = ds.newPriceAdjuster(startDate, endDate, codes);

		Consumer2<DateTime, Map<String, SecurityTimeSeriesDatum>> doTest = (date, data) -> {
			LocalDate curDate = date.toLocalDate();
			prices.clear();
			double forexHKD;
			double forexUSD;
			try {
				forexHKD = data.get("HKD").getAmount();
			} catch (NullPointerException npe) {
				LOGGER.error("Not able to obtain HKD exchange on {}", curDate);
				return;
			}
			try {
				forexUSD = data.get("USD").getAmount();
			} catch (NullPointerException npe) {
				LOGGER.error("Not able to obtain USD exchange on {}", curDate);
				return;
			}
//			System.out.printf("%s\t", date);
			data.forEach((code, datum) -> {
				if (datum.isTraded() && datum.getClose() > 0) {
					double adj;
//					System.out.printf("%s\t%s\t", datum.getDateTime(), code);
					if (code.length() == 6) {
//						System.out.printf("%f\t\t", datum.getClose()/1000d);
						adj = pa.adjust(code, startDate, curDate, datum.getClose())/1000d;
					} else {
						// H share
//						System.out.printf("%f\t%f\t", datum.getClose()*forexHKD/1000d, datum.getClose()/1000d);
						adj = pa.adjust(code, startDate, curDate, datum.getClose()*forexHKD/1000d);
					}
//					if (adj <= 0.0) {
//						System.out.println("Strange");
//					}
					prices.put(code, adj);
				}
			});
			for (int i = codePairs.length - 1; i > -1; i--) {
				String codeA = codePairs[i][0];
				if (!prices.containsKey(codeA)){
//					System.out.println("Skipped: "+curDate);
					continue;
				}
				String codeB = codePairs[i][1];
				if (!prices.containsKey(codeB)){
//					System.out.println("Skipped: "+curDate);
					continue;
				}
				double prA = prices.get(codeA);
				double prB = prices.get(codeB);
//				regression[i].addData((float)prA, (float)prB);
				regression[i].addData(prA, prB);
//				regression[i].addData((int)(prA*1000), (int)(prB*1000));
				pA[i].add(prA);
				pB[i].add(prB);
//				System.out.printf("%f\t%f\t", prA, prB);
			}
//			System.out.printf("%f\n", forexHKD);
		};

		if (stsds != null) {
			if (minutesOrDays) {
				stsds.walkMinutes(startTraining, endTraining, maxSteps, codes, false, doTest);
			} else {
				stsds.walkDays(startTraining, endTraining, maxSteps, codes, false, doTest);
			}
		} else if (spms != null) {
			spms.walk(startDate, endDate, false, doTest, codes);
		} else {
			sps.walk(startDate, endDate, false, doTest, codes);
		}

		String codeA, codeB;
		double [][] result = new double[codePairs.length][];
		for (int i = 0; i < codePairs.length; i++) {
			codeA = codePairs[i][0];
			codeB = codePairs[i][1];
			System.out.print(codeA+"->"+codeB+"\t");

			double [] priceListA, priceListB;
			priceListA = pA[i].stream().mapToDouble(d -> d).toArray();
			priceListB = pB[i].stream().mapToDouble(d -> d).toArray();

			// Correlation coefficient
			double correl = pearsonsCorrelation[i].correlation(priceListA, priceListB);
//			System.out.printf("correl:\t%.5f",correl);

			long ticks = regression[i].getN();
//			System.out.printf("\tticks:\t%d",ticks);

			// Regression Parameter slope: pB = slope * pA;
			double slope = regression[i].getSlope();
//			System.out.printf("\tslope:\t%.5f",slope);

			// ADF test for regression residuals on previously collected end-of-day / end-of-minute data
			double [] residuals = new double[priceListA.length];
			for (int j = 0; j <priceListA.length; j++) {
				// Calculate residuals according to parameters obtains from linear regression
				// ie. residual = 1 - slope * quoteA / quoteB
				residuals[j] = priceListA[j] * slope / priceListB[j] - 1;
//				if (residuals[j] < -11111) {
//					System.out.println("Strange");
//				}
			}
			double adf_p;
			adf_p = new AugmentedDickeyFuller(residuals).pValue();
//			try {
//				adf_p = new AugmentedDickeyFuller(residuals).pValue();
//			} catch (Exception e) {
//				adf_p = 1;
//			}
//			System.out.printf("\tadf_p:\t%.2f", adf_p);

			// Residual standard deviation
			double std = new StandardDeviation().evaluate(residuals);
//			System.out.printf("\tstd:\t%.5f\t", std);
//			System.out.println(""+priceListB[0]);


//			long start = System.currentTimeMillis();
			Quantile q = new Quantile(residuals);
			Max max = new Max(residuals);
			Min min = new Min(residuals);
//			System.out.println(q.value(0.05));
//			System.out.println(q.value(0.95));
//			System.out.println(max.value());
//			System.out.println(min.value());
//			System.out.println("Time: "+(System.currentTimeMillis()-start));
			result[i] = new double[] {slope, std, correl, adf_p, min.value(), q.value(0.01), q.value(0.05), q.value(0.95), q.value(0.99), max.value()};
			System.out.printf("%s->%s\tcorrel:\t%.5f\tticks:\t%d\tslope:\t%.5f\tadf_p:\t%.2f\tstd:\t%.5f\tmin: %.5f\t1pct: %.5f\t5pct: %.5f\t95pct: %.5f\t99pct: %.5f\tmax: %.5f\n",
					codeA, codeB, correl, ticks, slope, adf_p, std, result[i][4], result[i][5],  result[i][6],  result[i][7],  result[i][8],  result[i][9]);
		}

		return result;
	}

	public void createPair (String codeToShort, String codeToLong) {
		Stock stockToShort =  stockRepo.findOneByCode(codeToShort);
		Stock stockToLong = stockRepo.findOneByCode(codeToLong);
		Pair pair = new Pair();
		pair.setStockToShort(stockToShort);
		pair.setStockToLong(stockToLong);
		pair.setMaxAmountAllowed(100000);
		pair.setEnabled(true);
		pairRepo.save(pair);
	}

	public void inspectPairByMinute(String codeToShort, String codeToLong, String startDate, String endDate, double slope) {
		LocalDate start = LocalDate.parse(startDate);
		LocalDate end = LocalDate.parse(endDate);
		spms.walk(start,
				end,
				false,
				(date, data) -> {
					if (!data.get(codeToShort).isTraded()) {
						return;
					} else if (!data.get(codeToLong).isTraded()) {
						return;
					}
					double ex = data.get("HKD").getAmount();
					System.out.print("Ex:\t" + ex);
					System.out.print("\tClose:\t" + data.get(codeToShort).isTraded() + "\t"+data.get(codeToShort).close());
					System.out.print("\tSlope:\t" + slope);
					System.out.print("\tClose:\t" + data.get(codeToLong).isTraded() + "\t"+data.get(codeToLong).close());
					System.out.print("\t"+date);
					System.out.println("\t"+(data.get(codeToShort).close()*slope*ex/data.get(codeToLong).close() - 1));
				},
				codeToShort, codeToLong
		);
	}

	public void inspectPairByDay (String codeToShort, String codeToLong, String startDate, String endDate, double slope) {
		LocalDate start = LocalDate.parse(startDate);
		LocalDate end = LocalDate.parse(endDate);
		sps.walk(start,
				end,
				false,
				(date, data) -> {
					if (!data.get(codeToShort).isTraded()) {
						return;
					} else if (!data.get(codeToLong).isTraded()) {
						return;
					}
					double ex = data.get("HKD").getAmount();
					System.out.print("Ex:\t" + ex);
					System.out.print("\tClose:\t" + data.get(codeToShort).isTraded() + "\t"+data.get(codeToShort).close());
					System.out.print("\tSlope:\t" + slope);
					System.out.print("\tClose:\t" + data.get(codeToLong).isTraded() + "\t"+data.get(codeToLong).close());
					System.out.print("\t"+date);
					System.out.println("\t"+(data.get(codeToShort).close()*slope*ex/data.get(codeToLong).close() - 1));
				},
				codeToShort, codeToLong
		);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void massCreatePairStatsForExecutionStartRange (String earliestExecutionStart, String latestExecutionStart, int trainingWindowDays) {
		HolidayCalendarFromYahoo cal = HolidayCalendarFromYahoo.forExchange(Exchange.SHSE);
		LocalDate simulationStart = LocalDate.parse(earliestExecutionStart);
		LocalDate lastStart = LocalDate.parse(latestExecutionStart);
		LocalDate today = new LocalDate();
		while (!simulationStart.isAfter(lastStart)) {
			if (simulationStart.getDayOfWeek() != DateTimeConstants.SATURDAY &&
					simulationStart.getDayOfWeek() != DateTimeConstants.SUNDAY &&
					((!simulationStart.isBefore(today)) || !cal.isHoliday(simulationStart.toDateTimeAtStartOfDay().plusHours(2)))) {
				LocalDate trainingEnd = simulationStart.minusDays(1);
				LocalDate trainingStart = trainingEnd.minusDays(trainingWindowDays);
				System.out.println("Calculating stats for "+trainingStart+" - "+trainingEnd);
				calculateStats(trainingStart, trainingEnd, -1, 999999999);
				System.out.println("Updating Adf P Ma for "+trainingStart+" - "+trainingEnd);
				updateAdfpMovingAverage(trainingEnd);
			} else {
				System.out.println(simulationStart+" is a holiday.");
			}
			simulationStart = simulationStart.plusDays(1);
		}
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void massCreateShortInHKPairStrategyInstancesBasedOnCalculatedStats (String earliestExecutionStart, String latestExecutionStart) {
		Date trainingEndStart = LocalDate.parse(earliestExecutionStart).minusDays(1).toDate();
		Date trainingEndEnd = LocalDate.parse(latestExecutionStart).minusDays(1).toDate();
		List<PairStats> pairStatsList = pairStatsRepo.findByTrainingEndBetween(trainingEndStart, trainingEndEnd);
		pairStatsList.forEach(pairStats -> {
			shortInHkPairStrategyRepo.save(new ShortInHKPairStrategy(pairStats, paramMap.get(pairStats.getCodeToShort()+"->"+pairStats.getCodeToLong()+" 1")));
			shortInHkPairStrategyRepo.save(new ShortInHKPairStrategy(pairStats, paramMap.get(pairStats.getCodeToShort()+"->"+pairStats.getCodeToLong()+" 2")));
			shortInHkPairStrategyRepo.save(new ShortInHKPairStrategy(pairStats, paramMap.get(pairStats.getCodeToShort()+"->"+pairStats.getCodeToLong()+" 3")));
		});
	}
}