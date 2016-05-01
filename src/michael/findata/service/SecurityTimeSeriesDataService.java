package michael.findata.service;

import michael.findata.external.SecurityTimeSeriesData;
import michael.findata.external.SecurityTimeSeriesDatum;
import michael.findata.external.tdx.TDXMinuteLine;
import michael.findata.external.tdx.TDXPriceHistory;
import michael.findata.model.AdjFunction;
import michael.findata.util.CalendarUtil;
import michael.findata.util.Consumer3;
import michael.findata.util.Consumer5;
import org.joda.time.DateTime;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.util.*;
import java.util.function.Function;

public class SecurityTimeSeriesDataService extends JdbcDaoSupport {

	public void walkMinutes(DateTime start,
							DateTime end,
							int maxTicks,
							String codeA,
							String codeB,
							boolean log,
							Consumer5<DateTime, Double, Double, Float, Float> doStuff) {
		SecurityTimeSeriesData seriesA = new TDXMinuteLine(codeA);
		SecurityTimeSeriesData seriesB = new TDXMinuteLine(codeB);
		walk(start, end, maxTicks, seriesA, seriesB, codeA, codeB, log, doStuff);
	}

	public void walkDays(DateTime start,
						 DateTime end,
						 int maxTicks,
						 String codeA,
						 String codeB,
						 boolean log,
						 Consumer5<DateTime, Double, Double, Float, Float> doStuff) {
		SecurityTimeSeriesData seriesA = new TDXPriceHistory(codeA);
		SecurityTimeSeriesData seriesB = new TDXPriceHistory(codeB);
		walk(start, end, maxTicks, seriesA, seriesB, codeA, codeB, log, doStuff);
	}

	// seriesA and seriesB must be from the same concrete class
	private void walk(DateTime start,
					  DateTime end,
					  int maxTicks,
					  SecurityTimeSeriesData seriesA,
					  SecurityTimeSeriesData seriesB,
					  String codeA,
					  String codeB,
					  boolean log,
					  Consumer5<DateTime, Double, Double, Float, Float> doStuff) {
		Stack<AdjFunction<Integer, Integer>> adjFctA = new Stack<>();
		Stack<AdjFunction<Integer, Integer>> adjFctB = new Stack<>();
		try {
			DividendService.getAdjFunctions(start, end, codeA, codeB, adjFctA, adjFctB, getJdbcTemplate());
		} catch (Exception e) {
			// no adj factor found
		}

		SecurityTimeSeriesDatum quoteA;
		SecurityTimeSeriesDatum quoteB;
		Stack<SecurityTimeSeriesDatum> quotesA = new Stack<>();
		Stack<SecurityTimeSeriesDatum> quotesB = new Stack<>();
		boolean seriesEnded = false;
		while (seriesA.hasNext() && seriesB.hasNext()) {
			quoteA = seriesA.popNext();
			quoteB = seriesB.popNext();
			while (quoteA.getDateTime().getMillis() > quoteB.getDateTime().getMillis()) {
				if (seriesA.hasNext()) {
					quoteA = seriesA.popNext();
				} else {
					seriesEnded = true;
					break;
				}
			}
			if (seriesEnded) {
				break;
			}
			while (quoteB.getDateTime().getMillis() > quoteA.getDateTime().getMillis()) {
				if (seriesB.hasNext()) {
					quoteB = seriesB.popNext();
				} else {
					seriesEnded = true;
					break;
				}
			}
			if (seriesEnded) {
				break;
			}
//			while (quoteA.getMinute() > quoteB.getMinute()) {
//				if (seriesA.hasNext()) {
//					quoteA = seriesA.next();
//				} else {
//					seriesEnded = true;
//					break;
//				}
//			}
//			if (seriesEnded) {
//				break;
//			}
//			while (quoteB.getMinute() > quoteA.getMinute()) {
//				if (seriesB.hasNext()) {
//					quoteB = seriesB.next();
//				} else {
//					seriesEnded = true;
//					break;
//				}
//			}
//			if (seriesEnded) {
//				break;
//			}

			if (CalendarUtil.daysBetween(quoteA.getDateTime(), end) >= 0) {
				if (CalendarUtil.daysBetween(start, quoteA.getDateTime()) < 0) {
					break;
				}
				quotesA.push(quoteA);
				quotesB.push(quoteB);
				if (quotesA.size() == maxTicks) {
					break;
				}
			}
		}
		Function<Integer, Integer> currentAdjFunA = pri -> pri;
		Function<Integer, Integer> currentAdjFunB = pri -> pri;
		while ((!quotesA.isEmpty()) && !quotesB.isEmpty()) {
			quoteA = quotesA.pop();
			quoteB = quotesB.pop();
			while ((!adjFctA.isEmpty()) && CalendarUtil.daysBetween(adjFctA.peek().paymentDate, quoteA.getDateTime()) >= 0) {
				currentAdjFunA = currentAdjFunA.andThen(adjFctA.pop());
				System.out.println(codeA + " adjusted starting " + quoteA.getDateTime());
			}
			while ((!adjFctB.isEmpty()) && CalendarUtil.daysBetween(adjFctB.peek().paymentDate, quoteB.getDateTime()) >= 0) {
				currentAdjFunB = currentAdjFunB.andThen(adjFctB.pop());
				System.out.println(codeB + " adjusted starting " + quoteA.getDateTime());
			}
			//前复权
			double prA = currentAdjFunA.apply(quoteA.getClose()) / 1000d;
			double prB = currentAdjFunB.apply(quoteB.getClose()) / 1000d;
			if (Double.isInfinite(prA) || Double.isInfinite(prB)) {
				System.out.println("@@@@");
			}
			doStuff.apply(quoteA.getDateTime(), prA, prB, quoteA.getAmount(), quoteB.getAmount());
			if (log) {
				System.out.println(quoteA.getDateTime() + "\t" + "\t" + prA + "\t" + quoteB.getDateTime() + "\t" + prB + "\t");
			}
		}
		seriesA.close();
		seriesB.close();
	}

	// Walk through a group of stocks
	public void walkDays(DateTime adjStart,
					 DateTime start,
					 DateTime end,
					 int maxTicks,
					 String[] codes,
					 boolean log,
					 Consumer3<DateTime,
							 HashMap<String, SecurityTimeSeriesDatum>,
							 HashMap<String, Function<Integer, Integer>>> doStuff) {
		SecurityTimeSeriesData[] serieses = Arrays.stream(codes).map(TDXPriceHistory::new).toArray(TDXPriceHistory[]::new);
		walk(adjStart, start, end, maxTicks, serieses, codes, log, doStuff);
	}

	// Walk through a group of stocks
	public void walkMinutes(DateTime adjStart,
							DateTime start,
							DateTime end,
							int maxTicks,
							String[] codes,
							boolean log,
							Consumer3<DateTime,
									HashMap<String, SecurityTimeSeriesDatum>,
									HashMap<String, Function<Integer, Integer>>> doStuff) {
		SecurityTimeSeriesData[] serieses = Arrays.stream(codes).map(TDXMinuteLine::new).toArray(TDXMinuteLine[]::new);
		walk(adjStart, start, end, maxTicks, serieses, codes, log, doStuff);
	}

	// Walk through a group of stocks
	// Serieses must be from the same concrete class
	private void walk(DateTime adjStart,
					 DateTime start,
					 DateTime end,
					 int maxTicks,
					 SecurityTimeSeriesData[] serieses,
					 String[] codes,
					 boolean log,
					 Consumer3<DateTime,
							HashMap<String, SecurityTimeSeriesDatum>,
							HashMap<String, Function<Integer, Integer>>> doStuff) {
		Map<String, Stack<AdjFunction<Integer, Integer>>> adjFct;
		try {
			adjFct = DividendService.getAdjFunctions(adjStart.toLocalDate(), end.toLocalDate(), codes, getJdbcTemplate());
		} catch (Exception e) {
			// no adj factor found
			adjFct = new HashMap<>();
		}

		final Map<String, Stack<AdjFunction<Integer, Integer>>> adjFctn = adjFct;

		HashMap<String, Stack<SecurityTimeSeriesDatum>> quotes = new HashMap<>();
		Arrays.stream(codes).forEach(code -> quotes.put(code, new Stack<>()));

		DateTime latestTick;

		while (true) {
			// Ensure all serises have next tick
			for (SecurityTimeSeriesData series : serieses) {
				if (!series.hasNext()) {
					break;
				}
			}
			// Ensure no more than maxTicks
			if (quotes.get(codes[0]).size() >= maxTicks) {
				break;
			}
			// Find the latest tick
			latestTick = Arrays.stream(serieses).map(s -> {
				if (s.peekNext() == null) {
					return new DateTime(1015, 10, 30, 0, 0);
				} else {
					return s.peekNext().getDateTime();
				}
			}).max(DateTime::compareTo).get();
			if (CalendarUtil.daysBetween(start, latestTick) < 0) {
				// earlier than start
				break;
			}
			if (CalendarUtil.daysBetween(latestTick, end) >= 0) {
				// between start and end, valid ticks
//				System.out.println("Found latest:\t"+latestTick);
				for (int i = codes.length - 1; i > -1; i--) {
					if (serieses[i].peekNext() == null || serieses[i].peekNext().getDateTime().getMillis() < latestTick.getMillis()) {
						// dummy data, since there is no trading for this stock at this tick
//						System.out.println(codes[i]+"\t"+latestTick+"\tdummy");
						quotes.get(codes[i]).push(new SecurityTimeSeriesDatum(latestTick));
					} else {
//						System.out.println(codes[i]+"\t"+latestTick);
						quotes.get(codes[i]).push(serieses[i].popNext());
					}
				}
			} else {
				// after end
				for (int i = codes.length - 1; i > -1; i--) {
					if (serieses[i].peekNext().getDateTime().getMillis() == latestTick.getMillis()) {
						serieses[i].popNext();
					}
				}
			}
		}
		Arrays.stream(serieses).forEach(SecurityTimeSeriesData::close);

		HashMap<String, Function<Integer, Integer>> currentAdjFun = new HashMap<>();
		Arrays.stream(codes).forEach(code -> currentAdjFun.put(code, pri -> pri));
		HashMap<String, SecurityTimeSeriesDatum> minuteSnapshot = new HashMap<>();
		while (!quotes.get(codes[0]).isEmpty()) {
			final DateTime tick = quotes.get(codes[0]).peek().getDateTime();
			Arrays.stream(codes).forEach(code -> {
				Stack<AdjFunction<Integer, Integer>> adjFctA = adjFctn.get(code);
				SecurityTimeSeriesDatum quoteA = quotes.get(code).pop();
				//后复权
				while ((!adjFctA.isEmpty()) && CalendarUtil.daysBetween(adjFctA.peek().paymentDate, quoteA.getDateTime()) >= 0) {
					currentAdjFun.put(code, currentAdjFun.get(code).andThen(adjFctA.pop()));
					System.out.println(code + " adjusted, starting " + quoteA.getDateTime());
				}
				minuteSnapshot.put(code, quoteA);

				assert tick.getMillis() == quoteA.getDateTime().getMillis() : "Error on " + code + ": date time should be " + tick + ", but is " + quoteA.getDateTime();
			});
			doStuff.apply(tick, minuteSnapshot, currentAdjFun);
			if (log) {
				Arrays.stream(codes).forEach(code -> System.out.println(code + "\t" + minuteSnapshot.get(code)));
			}
		}
	}
}