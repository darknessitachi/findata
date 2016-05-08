package michael.findata.algoquant.strategy.pair;

import com.numericalmethod.algoquant.execution.datatype.product.stock.Stock;
import com.numericalmethod.suanshu.stats.test.timeseries.adf.AugmentedDickeyFuller;
import michael.findata.algoquant.execution.datatype.depth.Depth;
import michael.findata.algoquant.strategy.ETFPair;
import michael.findata.algoquant.strategy.Pair;
import michael.findata.external.SecurityTimeSeriesDatum;
import michael.findata.external.hexun2008.Hexun2008Constants;
import michael.findata.external.netease.NeteaseInstantSnapshot;
import michael.findata.external.shse.SHSEShortableStockList;
import michael.findata.external.szse.SZSEShortableStockList;
import michael.findata.service.*;
import michael.findata.util.Consumer2;
import michael.findata.util.Consumer3;
import michael.findata.util.Consumer5;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static michael.findata.algoquant.strategy.Pair.PairStatus.*;
import static michael.findata.algoquant.strategy.Pair.PairStatus.OPENED;
import static michael.findata.util.FinDataConstants.yyyyMMDDHHmmss;
import static michael.findata.util.FinDataConstants.yyyyMMdd;

public class PairsSearch {

	StockPriceService sps;
	SecurityTimeSeriesDataService stsds;
	DividendService ds;
	NeteaseInstantSnapshotService niss;

	public static void main (String [] args) throws IOException, ParseException {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/findata_spring.xml");
		PairsSearch pairsSearch = new PairsSearch();
//		ReportPubDateService spds = (ReportPubDateService) context.getBean("reportPubDateService");
//		StockService ss = (StockService) context.getBean("stockService");
//		FinDataService fds = (FinDataService) context.getBean("finDataService");
//		ShareNumberChangeService sncs = (ShareNumberChangeService) context.getBean("shareNumberChangeService");
		pairsSearch.sps = (StockPriceService) context.getBean("stockPriceService");
		pairsSearch.stsds = (SecurityTimeSeriesDataService) context.getBean("securityTimeSeriesDataService");
		pairsSearch.ds = (DividendService) context.getBean("dividendService");
		pairsSearch.niss = (NeteaseInstantSnapshotService) context.getBean("neteaseInstantSnapshotService");

		SimpleDateFormat sdf = new SimpleDateFormat(yyyyMMdd);

		// *** When Group Switches, switch the following

		// eft filtering with	0.7/0.10/3.0 -> 0.7 -> 2.0 ->2.8
		// 				or		0.7/0.10/3.0 -> 1.5 -> 2.0 ->2.8
		// 				or		0.7/0.10/3.0 -> 2.0 -> 2.0 ->2.8
		// 				or		0.7/0.12/2.0 -> 1.0 -> 1.0 ->1.5*
		// stock filtering with 0.7/0.1/3.7 -> 2.6 -> 2.6 -> 2.8
		// 				or		0.01/0.1/3.7 -> 2.6 -> 2.6 -> 2.8
		double correlThreshold = 0.7d;
		double cointThreshold = 0.12d;
		double openThresholdCoefficient = 2.0d;

		// Does not allow random pair close on the same day,
		// but still allow t+0 pairs to close on the same day.
		boolean allowSameDayClosure = false;

		Consumer2<Pair, Integer> relaxer = (pair, age) -> {
			// *** When Group Switches, switch the following
			// etf relaxing algo
			if (age <= 7) {
				pair.thresholdClose = pair.stdev * 1.0d;
			} else if (age <= 13) {
				pair.thresholdClose = pair.stdev * 1.0d;
			} else {
				pair.thresholdClose = pair.stdev * 1.5d;
			}

			// banking relaxing algo
//			if (age < 8) {
//				pair.thresholdClose = pair.stdev * 2.6;
//			} else if (age < 14) {
//				pair.thresholdClose = pair.stdev * 2.6;
//			} else {
//				pair.thresholdClose = pair.stdev * 2.8;
//			}
		};

		float amountPerSlot = 20000f;
		int maxShortsPerTickPerStock = 200;
		int maxNetPositionPerStock = 2000;

		DateTime simulationStart = new DateTime(sdf.parse("20151008"));
		// Training window: 61 days
		DateTime trainingEnd = simulationStart.minusDays(1);
		DateTime trainingStart = trainingEnd.minusDays(60);
		// Open/Close-Trade window: 9 days
		DateTime stopOpen = simulationStart.plusDays(9);
		// Close-Trade-Only window: 12 days
		DateTime simulationEnd = stopOpen.plusDays(21);

		// *** When Group Switches, switch the following

		// for stocks
		Set<String> shortables = new SHSEShortableStockList().getShortables();
		shortables.addAll(new SZSEShortableStockList().getShortables());

		// for ETF
//		Set<String> shortables = Arrays
//				.stream(StockGroups.ETFShortable)
//				.map(stock -> stock.symbol().substring(0, 6))
//				.collect(Collectors.toSet());


		ArrayList<Stock[]> stocks = new ArrayList<>();
		stocks.add(StockGroups.ETFBlueChips);
		stocks.add(StockGroups.ETFSmallCaps);
//		stocks.add(StockGroups.GoldETF);
		pairsSearch.search(stocks, shortables,
				correlThreshold, cointThreshold, openThresholdCoefficient, allowSameDayClosure, amountPerSlot,
				maxShortsPerTickPerStock, maxNetPositionPerStock,
				trainingStart, trainingEnd, simulationStart, stopOpen, simulationEnd,
				relaxer);
	}

	public void search(Collection<Stock[]> stocks,
					   Set<String> shortables,
					   double correlThreshold,
					   double cointThreshold,
					   double openThresholdCoefficient,
					   boolean allowSameDayClosure,
					   float amountPerSlot,
					   int maxShortsPerTickPerStock,
					   int maxNetPositionPerStock,
					   DateTime trainingStart,
					   DateTime trainingEnd,
					   DateTime simulationStart,
					   DateTime stopOpen,
					   DateTime simulationEnd,
					   Consumer2<Pair, Integer> closeThresholdRelaxer) throws ParseException, IOException {
		DateTimeFormatter format = DateTimeFormat.forPattern("E yyyyMMdd");

		System.out.println("Training Start: "+ format.print(trainingStart));
		System.out.println("Training End: "+format.print(trainingEnd));
		System.out.println("Simulation Start: "+format.print(simulationStart));
		System.out.println("Open Stop: "+format.print(stopOpen));
		System.out.println("Simulation End: "+format.print(simulationEnd));


		ArrayList<Pair> pairs = new ArrayList<>();
		stocks.forEach(sts -> {
			try {
				pairs.addAll(filter(trainingStart, trainingEnd, sts, shortables, correlThreshold, cointThreshold, sps, stsds));
			} catch (ParseException | IOException e) {
				e.printStackTrace();
			}
		});

		simulate(trainingStart, simulationStart, stopOpen, simulationEnd, pairs.toArray(new Pair[pairs.size()]),
				amountPerSlot,
				openThresholdCoefficient, allowSameDayClosure, maxShortsPerTickPerStock, maxNetPositionPerStock,
				closeThresholdRelaxer).stream()
				.sorted().forEach(pairConsumer);
		System.out.println("Date\tOpen#\tClose#\tSame Day Close#");
		counts.entrySet().stream().forEach(entry -> System.out.println(
						entry.getKey() + "\t" +
						entry.getValue().open + "\t" +
						entry.getValue().close + "\t" +
						entry.getValue().sameDayClose)
		);
	}
	public static SortedMap<String, Counts> counts = new TreeMap<>();
	public static Consumer<Pair> pairConsumer = pair1 -> {
		int age;
		SimpleDateFormat sdfDisplay = new SimpleDateFormat(yyyyMMDDHHmmss);
		SimpleDateFormat sdf = new SimpleDateFormat(yyyyMMdd);
		DecimalFormat df = new DecimalFormat(Hexun2008Constants.ACCURATE_DECIMAL_FORMAT);
		System.out.print(pair1.toShort.symbol().substring(0, 6) + "->" + pair1.toLong.symbol().substring(0, 6) + "\tslope: " + df.format(pair1.slope) + " stdev: " + df.format(pair1.stdev) + " correl: " + df.format(pair1.correlco) + " adf_p: " + df.format(pair1.adf_p));
		switch (pair1.status) {
			case OPENED:
				System.out.print("\tOpen: Short->Long\t\t\t\t");
				System.out.println(sdfDisplay.format(pair1.dateOpened.toDate()) + "\t" + pair1.shortOpen + "\t" + pair1.longOpen + "\t" + pair1.minResidual / pair1.stdev + "\t" + pair1.maxResidual / pair1.stdev + "\t\t\t\t\t\t" + pair1.maxAmountPossibleOpen);
				updateCount(counts, sdf.format(pair1.dateOpened.toDate()), CountType.OPEN);
				break;
			case CLOSED:
				age = pair1.closureAge();
				System.out.print((age == 0 ? "\tSame-day closure\t" : "\tClosure\t") + pair1.thresholdClose / pair1.stdev + "\tfee\t" + pair1.feeEstimate() + "\t");
				System.out.print(sdfDisplay.format(pair1.dateClosed.toDate()) + "\t" + pair1.shortClose + "\t" + pair1.longClose + "\t" + pair1.minResidual / pair1.stdev + "\t" + pair1.maxResidual / pair1.stdev + "\t" + sdfDisplay.format(pair1.minResDate.toDate()) + "\t" + sdfDisplay.format(pair1.maxResDate.toDate()) + "\t" + (age == 0 ? 1 : age));
				System.out.println("\tProfit:\t" + pair1.profitPercentageEstimate() + "\t" + pair1.maxAmountPossibleClose);
				updateCount(counts, sdf.format(pair1.dateClosed.toDate()), (age == 0 ? CountType.SAME_DAY_CLOSE : CountType.CLOSE));
				break;
			case FORCED:
				age = pair1.closureAge();
				System.out.print("\tForce closure\t" + pair1.thresholdClose / pair1.stdev + "\tfee\t" + pair1.feeEstimate() + "\t");
				System.out.print(sdfDisplay.format(pair1.dateClosed.toDate()) + "\t" + pair1.shortClose + "\t" + pair1.longClose + "\t" + pair1.minResidual / pair1.stdev + "\t" + pair1.maxResidual / pair1.stdev + "\t" + sdfDisplay.format(pair1.minResDate.toDate()) + "\t" + sdfDisplay.format(pair1.maxResDate.toDate()) + "\t" + (age == 0 ? 1 : age));
				System.out.println("\tProfit/Loss:\t" + pair1.profitPercentageEstimate() + "\t" + pair1.maxAmountPossibleClose);
				updateCount(counts, sdf.format(pair1.dateClosed.toDate()), (age == 0 ? CountType.SAME_DAY_CLOSE : CountType.CLOSE));
				break;
		}
	};

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

	// use sz and sh shortable list to create pairs, with correlation and cointegration calculated
	private Collection<Pair> filter (DateTime startTraining,
								  DateTime endTraining,
//								  int maxSteps,
								  Stock[] stocks,
								  Set<String> shortables,
								  double correlThreshold,
								  double cointThreshold,
								  StockPriceService sps,
								  SecurityTimeSeriesDataService stsds) throws ParseException, IOException {

		endTraining = endTraining.withHourOfDay(23);

		String codeA;
		String codeB;
		ArrayList<Pair> pairs = new ArrayList<>();
		int running = 0;
		for (int i = 0; i < stocks.length; i++) {
			codeA = stocks[i].symbol().substring(0, 6);
			for (int j = i + 1; j < stocks.length; j++) {
				codeB = stocks[j].symbol().substring(0, 6);
//				System.out.println(codeA+"\t"+codeB+"\t");
				if (!shortables.contains(codeA)) {
					if (!shortables.contains(codeB)) {
						continue;
					}
					codeA += "*";
				} else if (!shortables.contains(codeB)) {
					codeB += "*";
				}
				System.out.println(codeA+"\t"+codeB+"::::");
				codeA = codeA.substring(0, 6);
				codeB = codeB.substring(0, 6);

				// Use file data to do days test
//				cointcorrel(startTraining, endTraining, codeA, codeB, 1000000, stsds, sps, false);
				try {
						// Make this pair
						if (shortables.contains(codeA)) {
							System.out.print(codeA+"\t"+codeB+"\t");
							// Use file data to do minutes test
							double [] result = cointcorrel(startTraining, endTraining, codeA, codeB, 100000, stsds, sps, true);
							if (result[2] >= correlThreshold && result[3] <= cointThreshold) {
								System.out.print("\tselected");
								pairs.add(new ETFPair(running++, stocks[i], stocks[j], result[0], result[1], result[2], result[3]));
							}
							System.out.print("\t");
							System.out.println();
						}
						if (shortables.contains(codeB)) {
							System.out.print(codeB+"\t"+codeA+"\t");
							// Use file data to do minutes test
							double [] result = cointcorrel(startTraining, endTraining, codeB, codeA, 100000, stsds, sps, true);
							if (result[2] >= correlThreshold && result[3] <= cointThreshold) {
								System.out.print("\tselected");
								pairs.add(new ETFPair(running++, stocks[j], stocks[i], result[0], result[1], result[2], result[3]));
							}
							System.out.print("\t");
							System.out.println();
						}
				} catch (Exception e) {}
			}
		}
		return pairs;
	}

	// Simulate real world orders for a group of pairs and produce a list of executions
	// This uses TDXMinute data
	private List<Pair> simulate(final DateTime adjStart,
								final DateTime startSim,
								final DateTime stopOpen,
								final DateTime endSim,
								Pair[] pairs,

								// If the amount traded in a tick is less than this amount, do not execute an order, ignore it.
								// This is used to filter out ticks with too little trades
								float amountPerSlot,

								double openThresholdCoefficient,
								boolean allowSameDayClosure,

								// for etf, liquidity is a real issue, this limits the shorts of a ticker
								// not yet in use
								int maxShortsPerTickPerStock,

								// for mass pair-trading, we limit the net long/short position of a stock
								int maxNetPositionPerStock,
								Consumer2<Pair, Integer> closeThresholdRelaxer) {

		final int maxNetPosition = maxNetPositionPerStock > 0 ? maxNetPositionPerStock : -maxNetPositionPerStock;

		// How many stdev of a price gap to open the trade?
		Arrays.stream(pairs).forEach(pair -> pair.thresholdOpen = pair.stdev * openThresholdCoefficient);

		String [] codes = Arrays.stream(pairs)
				.flatMap(pair -> Stream.of(pair.toLong.symbol().substring(0,6), pair.toShort.symbol().substring(0,6)))
				.collect(Collectors.toSet())
				.stream().toArray(String[]::new);

		HashMap<String, Integer> netPosition = new HashMap<>();
		Arrays.stream(codes).forEach(code -> netPosition.put(code, 0));

		List<Pair> executions = new ArrayList<>();
		DateTime startOfEndSim = endSim.withMillisOfDay(1);

		Consumer3<DateTime, HashMap<String, SecurityTimeSeriesDatum>, HashMap<String, Function<Integer, Integer>>>
				tickOp = (dateTime, snapshots, adjFuns) -> {
			String codeShort;
			String codeLong;
			SecurityTimeSeriesDatum datumShort;
			SecurityTimeSeriesDatum datumLong;
			double adjustedPriceShort;
			double adjustedPriceLong;
			double actualPriceShort;
			double actualPriceLong;
			float amountShort;
			float amountLong;
			for (Pair pair : pairs) {

				// if a is not traded, skip
				codeShort = pair.toShort.symbol().substring(0, 6);
				datumShort = snapshots.get(codeShort);
				if ((!datumShort.isTraded()) || datumShort.getVolume() == 0) {
					continue;
				}

				// if b is not traded, skip
				codeLong = pair.toLong.symbol().substring(0, 6);
				datumLong = snapshots.get(codeLong);
				if ((!datumLong.isTraded()) || datumLong.getVolume() == 0) {
					continue;
				}

				actualPriceShort = datumShort.getClose()/1000d;
				adjustedPriceShort = adjFuns.get(codeShort).apply(datumShort.getClose())/1000d;
				amountShort = datumShort.getAmount();

				actualPriceLong = datumLong.getClose()/1000d;
				adjustedPriceLong = adjFuns.get(codeLong).apply(datumLong.getClose())/1000d;
				amountLong = datumLong.getAmount();

				double residual = adjustedPriceShort * pair.slope - adjustedPriceLong;
				int age = 0;
				age = pair.age(dateTime);
				// forcefully close positions 10 minutes before ending
				if ((age >= 20 || dateTime.withMillisOfDay(1).compareTo(startOfEndSim) == 0) && pair.status == OPENED) {
					if (dateTime.getMinuteOfDay() >= 890 || (residual < pair.thresholdOpen && amountShort >= amountPerSlot && amountLong >= amountPerSlot)) {
						pair.status = FORCED;
						pair.dateClosed = dateTime;
						pair.shortClose = actualPriceShort;
						pair.longClose = actualPriceLong;
						pair.thresholdClose = residual;
						pair.maxAmountPossibleClose = Math.min(amountShort, amountLong);
//						System.out.print(codeA + " -> " + codeB + " slope: " + pair.slope + " stdev: " + pair.stdev);
//						System.out.print("\tForce closure\t"+pair.thresholdClose/pair.stdev+"\tfee\t" + pair.feeEstimate() + "\t");
//						System.out.print(sdfDisplay.format(dateTime.toDate()) + "\t" + priceA + "\t" + priceB + "\t" + pair.minResidual + "\t" + (age == 0 ? 1 : age));
//						System.out.println("\tProfit/Loss:\t" + pair.profitPercentageEstimate() + "\t" + pair.maxAmountPossibleClose);
						executions.add(pair.copy());
						pair.reset();
						netPosition.put(codeShort, netPosition.get(codeShort) + 1);
						netPosition.put(codeLong, netPosition.get(codeLong) - 1);
						continue;
					}
				}

				if (pair.status == OPENED) {
					if (Math.abs(amountShort) < amountPerSlot || Math.abs(amountLong) < amountPerSlot)
						continue; // do not close if vol = 0
//						pair.minResidual = pair.minResidual > residual ? residual : pair.minResidual;
					if (pair.minResidual > residual) {
						pair.minResidual = residual;
						pair.minResDate = dateTime;
					}
//						pair.maxResidual = pair.maxResidual < residual ? residual : pair.maxResidual;
					if (pair.maxResidual < residual) {
						pair.maxResidual = residual;
						pair.maxResDate = dateTime;
					}

					// relax closing threshold according to eg etc.
					closeThresholdRelaxer.apply(pair, age);

					if (residual < pair.thresholdClose && (allowSameDayClosure || age > 0 || StockGroups.TPlus0.contains(codeLong))) {
						pair.dateClosed = dateTime;
						pair.shortClose = actualPriceShort;
						pair.longClose = actualPriceLong;
						pair.status = CLOSED;
						pair.maxAmountPossibleClose = Math.min(amountShort, amountLong);
//						System.out.print(codeA + " -> " + codeB + " slope: " + pair.slope + " stdev: " + pair.stdev);
//						System.out.print((age == 0 ? "\tSame-day closure\t" : "\tClosure\t") + pair.thresholdClose / pair.stdev + "\tfee\t" + pair.feeEstimate() + "\t");
//						System.out.print(sdfDisplay.format(dateTime.toDate()) + "\t" + priceA + "\t" + priceB + "\t" + pair.minResidual + "\t" + (age == 0 ? 1 : age));
//						System.out.println("\tProfit:\t" + pair.profitPercentageEstimate() + "\t" + pair.maxAmountPossibleClose);
						executions.add(pair.copy());
						pair.reset();
						netPosition.put(codeShort, netPosition.get(codeShort) + 1);
						netPosition.put(codeLong, netPosition.get(codeLong) - 1);
					}
				} else if (pair.status == NEW && residual >= pair.thresholdOpen && dateTime.compareTo(stopOpen) < 0) {
					// do not open if vol = 0
					if (Math.abs(amountShort) < amountPerSlot || Math.abs(amountLong) < amountPerSlot) {
						continue;
					}

					// do not open if maxNetPositionPerStock is reached
					if (netPosition.get(codeShort) <= -maxNetPosition || netPosition.get(codeLong) >= maxNetPosition) {
						continue;
					}

					pair.status = OPENED;
					pair.shortOpen = actualPriceShort;
					pair.longOpen = actualPriceLong;
					pair.dateOpened = dateTime;
					pair.minResidual = residual;
					pair.maxResidual = residual;
					pair.minResDate = dateTime;
					pair.maxResDate = dateTime;
					pair.maxAmountPossibleOpen = Math.min(amountShort, amountLong);
//						System.out.print(codeA + " -> " + codeB + " slope: " + pair.slope + " stdev: " + pair.stdev);
//						System.out.print("\tOpen: Short A, Long B\t\t\t\t");
//						System.out.println(sdfDisplay.format(dateTime.toDate()) + "\t" + priceA + "\t" + priceB + "\t" + residual + "\t\t\t\t" + pair.maxAmountPossibleOpen);
					executions.add(pair.copy());
					netPosition.put(codeShort, netPosition.get(codeShort) - 1);
					netPosition.put(codeLong, netPosition.get(codeLong) + 1);
				}
			}
		};

		stsds.walkMinutes(adjStart, startSim, endSim.withHourOfDay(23), 1000000, codes, false, tickOp);

		return executions;
	}

	// Simulate real world orders for a group of pairs and produce a list of executions
	// This uses NeteaseSnapshot data
	private List<Pair> simulate(final DateTime adjStart,
								final DateTime startSim,
								final DateTime stopOpen,
								final DateTime endSim,
								Pair[] pairs,

								// If the amount traded in a tick is less than this amount, do not execute an order, ignore it.
								// This is used to filter out ticks with too little trades
								float amountPerSlot,

								double openThresholdCoefficient,
								boolean allowSameDayClosure,

								// for etf, liquidity is a real issue, this limits the shorts of a ticker
								// not yet in use
								int maxShortsPerTickPerStock,

								// for mass pair-trading, we limit the net long/short position of a stock
								int maxNetPositionPerStock,
								Consumer2<Pair, Integer> closeThresholdRelaxer,
								NeteaseInstantSnapshotService niss) throws IOException {

		final int maxNetPosition = maxNetPositionPerStock > 0 ? maxNetPositionPerStock : -maxNetPositionPerStock;

		// How many stdev of a price gap to open the trade?
		Arrays.stream(pairs).forEach(pair -> pair.thresholdOpen = pair.stdev * openThresholdCoefficient);

		String [] codes = Arrays.stream(pairs)
				.flatMap(pair -> Stream.of(pair.toLong.symbol().substring(0,6), pair.toShort.symbol().substring(0,6)))
				.collect(Collectors.toSet())
				.stream().toArray(String[]::new);

		HashMap<String, Integer> netPosition = new HashMap<>();
		Arrays.stream(codes).forEach(code -> netPosition.put(code, 0));

		List<Pair> executions = new ArrayList<>();
		DateTime startOfEndSim = endSim.withMillisOfDay(1);

		Consumer3<DateTime, Map<String, Depth>, HashMap<String, Function<Integer, Integer>>>
				tickOp = (dateTime, snapshot, adjFuns) -> {
			String codeShort;
			String codeLong;
			Depth datumShort;
			Depth datumLong;
			double adjustedPriceShort;
			double adjustedPriceLong;
			double actualPriceShort;
			double actualPriceLong;
			float amountShort;
			float amountLong;
			for (Pair pair : pairs) {

				// if a is not traded, skip
				codeShort = pair.toShort.symbol().substring(0, 6);
				datumShort = snapshot.get(codeShort);
				if (!datumShort.isTraded()) {
					continue;
				}

				// if b is not traded, skip
				codeLong = pair.toLong.symbol().substring(0, 6);
				datumLong = snapshot.get(codeLong);
				if (!datumLong.isTraded()) {
					continue;
				}

				actualPriceShort = datumShort.bestBid(amountPerSlot);
				adjustedPriceShort = adjFuns.get(codeShort).apply((int)(actualPriceShort*1000))/1000d;
				amountShort = datumShort.totalBidAtOrAbove(actualPriceShort);

				actualPriceLong = datumLong.bestAsk(amountPerSlot);
				adjustedPriceLong = adjFuns.get(codeLong).apply((int)(actualPriceLong*1000))/1000d;
				amountLong = datumLong.totalAskAtOrBelow(actualPriceLong);

				double residual = adjustedPriceShort * pair.slope - adjustedPriceLong;
				int age = 0;
				age = pair.age(dateTime);
				// forcefully close positions 10 minutes before ending
				if ((age >= 20 || dateTime.withMillisOfDay(1).compareTo(startOfEndSim) == 0) && pair.status == OPENED) {
					if (dateTime.getMinuteOfDay() >= 890 || (residual < pair.thresholdOpen && amountShort >= amountPerSlot && amountLong >= amountPerSlot)) {
						pair.status = FORCED;
						pair.dateClosed = dateTime;
						pair.shortClose = actualPriceShort;
						pair.longClose = actualPriceLong;
						pair.thresholdClose = residual;
						pair.maxAmountPossibleClose = Math.min(amountShort, amountLong);
//						System.out.print(codeA + " -> " + codeB + " slope: " + pair.slope + " stdev: " + pair.stdev);
//						System.out.print("\tForce closure\t"+pair.thresholdClose/pair.stdev+"\tfee\t" + pair.feeEstimate() + "\t");
//						System.out.print(sdfDisplay.format(dateTime.toDate()) + "\t" + priceA + "\t" + priceB + "\t" + pair.minResidual + "\t" + (age == 0 ? 1 : age));
//						System.out.println("\tProfit/Loss:\t" + pair.profitPercentageEstimate() + "\t" + pair.maxAmountPossibleClose);
						executions.add(pair.copy());
						pair.reset();
						netPosition.put(codeShort, netPosition.get(codeShort) + 1);
						netPosition.put(codeLong, netPosition.get(codeLong) - 1);
						continue;
					}
				}

				if (pair.status == OPENED) {
					if (Math.abs(amountShort) < amountPerSlot || Math.abs(amountLong) < amountPerSlot)
						continue; // do not close if vol = 0
//						pair.minResidual = pair.minResidual > residual ? residual : pair.minResidual;
					if (pair.minResidual > residual) {
						pair.minResidual = residual;
						pair.minResDate = dateTime;
					}
//						pair.maxResidual = pair.maxResidual < residual ? residual : pair.maxResidual;
					if (pair.maxResidual < residual) {
						pair.maxResidual = residual;
						pair.maxResDate = dateTime;
					}

					// relax closing threshold according to eg etc.
					closeThresholdRelaxer.apply(pair, age);

					if (residual < pair.thresholdClose && (allowSameDayClosure || age > 0 || StockGroups.TPlus0.contains(codeLong))) {
						pair.dateClosed = dateTime;
						pair.shortClose = actualPriceShort;
						pair.longClose = actualPriceLong;
						pair.status = CLOSED;
						pair.maxAmountPossibleClose = Math.min(amountShort, amountLong);
//						System.out.print(codeA + " -> " + codeB + " slope: " + pair.slope + " stdev: " + pair.stdev);
//						System.out.print((age == 0 ? "\tSame-day closure\t" : "\tClosure\t") + pair.thresholdClose / pair.stdev + "\tfee\t" + pair.feeEstimate() + "\t");
//						System.out.print(sdfDisplay.format(dateTime.toDate()) + "\t" + priceA + "\t" + priceB + "\t" + pair.minResidual + "\t" + (age == 0 ? 1 : age));
//						System.out.println("\tProfit:\t" + pair.profitPercentageEstimate() + "\t" + pair.maxAmountPossibleClose);
						executions.add(pair.copy());
						pair.reset();
						netPosition.put(codeShort, netPosition.get(codeShort) + 1);
						netPosition.put(codeLong, netPosition.get(codeLong) - 1);
					}
				} else if (pair.status == NEW && residual >= pair.thresholdOpen && dateTime.compareTo(stopOpen) < 0) {
					// do not open if vol = 0
					if (Math.abs(amountShort) < amountPerSlot || Math.abs(amountLong) < amountPerSlot) {
						continue;
					}

					// do not open if maxNetPositionPerStock is reached
					if (netPosition.get(codeShort) <= -maxNetPosition || netPosition.get(codeLong) >= maxNetPosition) {
						continue;
					}

					pair.status = OPENED;
					pair.shortOpen = actualPriceShort;
					pair.longOpen = actualPriceLong;
					pair.dateOpened = dateTime;
					pair.minResidual = residual;
					pair.maxResidual = residual;
					pair.minResDate = dateTime;
					pair.maxResDate = dateTime;
					pair.maxAmountPossibleOpen = Math.min(amountShort, amountLong);
//						System.out.print(codeA + " -> " + codeB + " slope: " + pair.slope + " stdev: " + pair.stdev);
//						System.out.print("\tOpen: Short A, Long B\t\t\t\t");
//						System.out.println(sdfDisplay.format(dateTime.toDate()) + "\t" + priceA + "\t" + priceB + "\t" + residual + "\t\t\t\t" + pair.maxAmountPossibleOpen);
					executions.add(pair.copy());
					netPosition.put(codeShort, netPosition.get(codeShort) - 1);
					netPosition.put(codeLong, netPosition.get(codeLong) + 1);
				}
			}
		};

		niss.walk(adjStart.toLocalDate(), startSim.toLocalDate(), endSim.withHourOfDay(23).toLocalDate(), codes, false, tickOp);

		return executions;
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

		return new double[] {slope, std, correl, adf_p};
	}

	// Simulate real world orders for a pair of stocks and produce a list of executions
	@Deprecated
	private List<Pair> simulate (DateTime startSim,
										DateTime stopOpen,
										final DateTime endSim,
										Pair pair,
										double openThresholdCoefficient,
										boolean allowSameDayClosure,
										// for etf, liquidity is a real issue, this switch, if true, limits short sale of
										// a ticker to 1 per minute
//										boolean oneShortPerMinute,
										SecurityTimeSeriesDataService stsds) {
		float amountPerSlot = 100000f;
		pair.thresholdOpen = pair.stdev * openThresholdCoefficient;
		List<Pair> executions = new ArrayList<>();
		DateTime startOfEndSim = endSim.withMillisOfDay(1);

		// find ops
		String codeA = pair.toShort.symbol().substring(0, 6);
		String codeB = pair.toLong.symbol().substring(0, 6);
		stsds.walkMinutes(startSim, endSim.withHourOfDay(23), 1000000, codeA, codeB, false,
				(dateTime, priceA, priceB, amountA, amountB) -> {
					double residual = priceA * pair.slope - priceB;
					long age = 0;
					if (pair.status == OPENED || pair.status == CLOSED || pair.status == FORCED) {
						age = pair.age(dateTime);
					}
//					if ((dateTime.withMillisOfDay(1).compareTo(endSim.withMillisOfDay(1)) == 0) && dateTime.getMinuteOfDay() > 880 )
//						System.out.println(sdfDisplay.format(dateTime.toDate()) + "\t\t" + priceA + "\t" + priceB + "\t"+residual+"\t"+(dateTime.getMinuteOfDay() == 890 || (residual < pair.stdev * 2.5))+"\t"+pair.status);
					// forcefully close positions 10 minutes before ending
					if ((age >= 20 || dateTime.withMillisOfDay(1).compareTo(startOfEndSim) == 0) && pair.status == OPENED) {
						if (dateTime.getMinuteOfDay() >= 890 || (residual < pair.thresholdOpen && amountA >= amountPerSlot && amountB >= amountPerSlot)) {
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
						pair.maxResidual = pair.maxResidual < residual ? residual : pair.maxResidual;

						// *** When Group Switches, switch the following
						// etf relaxing algo
//						if (age <= 7) {
//							pair.thresholdClose = pair.stdev * 2.0;
//						} else if (age <= 13) {
//							pair.thresholdClose = pair.stdev * 2.0;
//						} else {
//							pair.thresholdClose = pair.stdev * 2.8;
//						}

						// banking relaxing algo
						if (age <= 7) {
							pair.thresholdClose = pair.stdev * 2.6;
						} else if (age <= 13) {
							pair.thresholdClose = pair.stdev * 2.6;
						} else {
							pair.thresholdClose = pair.stdev * 2.8;
						}

						if (residual < pair.thresholdClose && (allowSameDayClosure || age > 0)) {
//							double fee = 4 * 0.0003 + (age==0?1:age)* 0.1085 / 360; // ETF cost
							pair.dateClosed = dateTime;
							pair.shortClose = priceA;
							pair.longClose = priceB;
							pair.status = CLOSED;
//							pair.minResidual = residual;
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
						pair.maxResidual = residual;
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
}