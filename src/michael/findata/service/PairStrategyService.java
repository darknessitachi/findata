package michael.findata.service;

import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import com.numericalmethod.suanshu.stats.test.timeseries.adf.AugmentedDickeyFuller;
import michael.findata.algoquant.strategy.PairStrategy;
import michael.findata.external.netease.NeteaseInstantSnapshot;
import michael.findata.external.shse.SHSEShortableStockList;
import michael.findata.external.szse.SZSEShortableStockList;
import michael.findata.model.*;
import michael.findata.spring.data.repository.*;
import michael.findata.util.Consumer5;
import michael.findata.util.FinDataConstants;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

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
	NeteaseInstantSnapshotService niss;

	@Autowired
	StockPriceService sps;

	@Autowired
	DividendService ds;

	@Autowired
	StockService ss;

	@Transactional(propagation = Propagation.REQUIRED)
	public void updatePairs (String ... codes) throws IOException {
		Set<Pair> currentPairs = new HashSet<>();
		long allStart = System.currentTimeMillis();
		long start = allStart;
		long end;
		pairRepo.findAll().forEach(currentPairs::add);
		List<Stock> stocks = stockRepo.findByCodeIn(codes);
		end = System.currentTimeMillis();
		System.out.println("updatePairs step 1(s): "+(end - start)/1000d);
		start = end;
		Set<String> shortables = getShortablesUpdateIfRequired();
		end = System.currentTimeMillis();
		System.out.println("updatePairs step 2(s): "+(end - start)/1000d);
		start = end;
		for (Stock toShort : stocks) {
			if (shortables.contains(toShort.getCode())) {
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
			}
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
				result = ss.getStockGroup("classification_shseshortables.csv");
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
	public void calculateStats (LocalDate trainingStart, LocalDate trainingEnd) {
		long start = System.currentTimeMillis();
		pairRepo.findByEnabled(true).forEach(pair -> {
			if (pair.getId() <= 436) {
				return;
			}
			double [] result = cointcorrel(
					trainingStart.toDateTimeAtStartOfDay(),
					trainingEnd.toDateTimeAtStartOfDay().plusHours(23),
					pair.getCodeToShort(),
					pair.getCodeToLong(),
					1000000, stsds, sps, true);
			PairStats stats = new PairStats();
			stats.setPair(pair);
			stats.setTimeSeriesType(PairStats.TimeSeriesType.MINUTE);
			stats.setTrainingStart(trainingStart.toDate());
			stats.setTrainingEnd(trainingEnd.toDate());
			stats.setSlope(result[0]);
			stats.setStdev(result[1]);
			stats.setCorrelco(result[2]);
			stats.setAdfp(result[3]);
			pairStatsRepo.save(stats);
		});
		System.out.println("calculateStats total(s): "+(System.currentTimeMillis() - start)/1000d);
	}

	// Step 2: populate PairInstances after stats are calculated
	@Transactional(propagation = Propagation.REQUIRED)
	public void populatePairInstances (LocalDate openableDate) {
		// todo: this is for open/close spotting. There will be another strategy to populate in real trading
		// find all pairs
		List<PairStats> stats = pairStatsRepo.findByTrainingEndAndCorrelcoGreaterThanAndAdfpLessThan(
				openableDate.minusDays(1).toDate(), -2d, 0.9999d);
		List<PairInstance> pairs = new ArrayList<>();
		stats.forEach(stat -> {
			PairInstance p = new PairInstance();
			p.setStats(stat);
			p.setOpenableDate(openableDate.toDate());
			p.setForceClosureDate(openableDate.plusDays(20).toDate()); // todo parameter
			p.setStatus(PairInstance.PairStatus.NEW);
			pairs.add(p);
		});
		pairInstanceRepo.save(pairs);
	}

	public Collection<PairInstance> findSimpleOpenCloseOpportunities (LocalDate openStart, LocalDate openEnd, LocalDate startExe, LocalDate endExe, String[] codesToShort, String[] codesToLong) {
//		DateTimeFormatter formatter = DateTimeFormat.forPattern(FinDataConstants.yyyyMMdd);
//		LocalDate current = formatter.parseLocalDate("20160410");
//		LocalDate end = formatter.parseLocalDate("20160410");
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

	/**
	 *	if stsds is not null then {
	 *		use SecurityTimeSeriesDataService stsds to do tests
	 *		minutesOrDays: true - minutes - do minutes test
	 *		minutesOrDays: false - days - do days test
	 *	} else {
	 *  	use StockPriceService sps to do days test, ignoring minutesOrDays
	 *	}
	 *	Print output: 	[Correlation][n-Sample][regression slop][adf p-value][residual standard deviation][reference price]
	 *	Return value: if fail test - null
	 *				  if pass test - [slope, stdev]
	 */
	public static double[] cointcorrel (DateTime startTraining,
										DateTime endTraining,
										String codeA,
										String codeB,
										int maxSteps,
										SecurityTimeSeriesDataService stsds,
										StockPriceService sps,
										boolean minutesOrDays) {
		SimpleRegression regression = new SimpleRegression (false);
		PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();

		ArrayList<Double> pA = new ArrayList<>();
		ArrayList<Double> pB = new ArrayList<>();

		Consumer5<DateTime, Double, Double, Float, Float> doTest = (dateTime, prA, prB, amountA, amountB) -> {
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

		System.out.print(codeA+"->"+codeB+"\t");

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

		// ADF test for regression residuals on previously collected end-of-day / end-of-minute data
		double [] residuals = new double[priceListA.length];
		for (int i = 0; i <priceListA.length; i++) {
			// Calculate residuals according to parameters obtains from linear regression
			// ie. residual = 1 - slope * quoteA / quoteB
			residuals[i] = 1 - priceListA[i] * slope / priceListB[i];
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
		System.out.println("\t"+priceListB[0]);

		return new double[] {slope, std, correl, adf_p};
	}
}