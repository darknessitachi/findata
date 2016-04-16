package michael.findata.service;

import michael.findata.algoquant.strategy.pair.StockGroups;
import michael.findata.external.SecurityTimeSeriesDatum;
import michael.findata.external.netease.NeteaseInstantSnapshot;
import michael.findata.model.AdjFunction;
import michael.findata.util.CalendarUtil;
import michael.findata.util.Consumer3;
import michael.findata.util.FinDataConstants;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NeteaseInstantSnapshotService extends JdbcDaoSupport {

	private static final String storageFileNameFormat = "yyyy_MM_dd";

	public static void main (String [] args) throws IOException {
		DateTimeFormatter formatter = DateTimeFormat.forPattern(FinDataConstants.yyyyMMdd);
		NeteaseInstantSnapshotService niss = new NeteaseInstantSnapshotService();

		Set<String> collect = Stream.concat(Stream.concat(Stream.concat(Stream.concat(Stream.concat(Stream.concat(Stream.concat(Stream.concat(Stream.concat(
				Arrays.stream(StockGroups.Securities),
				Arrays.stream(StockGroups.Banking)),
				Arrays.stream(StockGroups.Insurance)),
				Arrays.stream(StockGroups.ETFBlueChips)),
				Arrays.stream(StockGroups.ETFSmallCaps)),
				Arrays.stream(StockGroups.LargeMarketCap)),
				Arrays.stream(StockGroups.Highway)),
				Arrays.stream(StockGroups.Alcohol)),
				Arrays.stream(StockGroups.TPlus0Funds)),
				Arrays.stream(StockGroups.TPlus0Gold))
				.map(stock -> stock.symbol().substring(0, 6)).collect(Collectors.toSet());
		String [] codes = collect.toArray(new String[collect.size()]);

		LocalDate start = formatter.parseLocalDate("20160411");

		niss.walk(start, start, start, codes, false, (tick, snapshot, adjFun)->{
			System.out.println(tick+" "+snapshot);
		});
	}

	public void walk(LocalDate adjStart,
					 LocalDate start,
					 LocalDate end,
					 String[] codes,
					 boolean log,
					 Consumer3<DateTime,
							NeteaseInstantSnapshot,
							HashMap<String, Function<Integer, Integer>>> doStuff) throws IOException {
		DateTimeFormatter formatter = DateTimeFormat.forPattern(FinDataConstants.yyyyMMDDHHmmss);
		BufferedReader br = new BufferedReader(new FileReader(getStorageFileName(start)));
		String line;

		Map<String, Stack<AdjFunction<Integer, Integer>>> adjFctTemp;
		try {
			adjFctTemp = DividendService.getAdjFunctions(adjStart, end, codes, getJdbcTemplate());
		} catch (Exception e) {
			// no adj factor found
			adjFctTemp = new HashMap<>();
		}

		final Map<String, Stack<AdjFunction<Integer, Integer>>> adjFunctions = adjFctTemp;

		HashMap<String, Function<Integer, Integer>> currentAdjFun = new HashMap<>();
		Arrays.stream(codes).forEach(code -> currentAdjFun.put(code, pri -> pri));

		while ((line = br.readLine()) != null) {
			final DateTime tick = formatter.parseDateTime(line.substring(0, line.indexOf('|')));
			final NeteaseInstantSnapshot snapshot = new NeteaseInstantSnapshot(line.substring(line.indexOf('|')+1));
			Arrays.stream(codes).forEach(code -> {
				Stack<AdjFunction<Integer, Integer>> adjFctA = adjFunctions.get(code);
				//ºó¸´È¨
				while (adjFctA != null && (!adjFctA.isEmpty()) && CalendarUtil.daysBetween(adjFctA.peek().paymentDate, tick) >= 0) {
					currentAdjFun.put(code, currentAdjFun.get(code).andThen(adjFctA.pop()));
					System.out.println(code + " adjusted, starting " + tick);
				}
			});
			doStuff.apply(tick, snapshot, currentAdjFun);
			if (log) {
				System.out.println(tick+" "+snapshot);
			}
		}
	}

	private String getStorageFileName (DateTime date) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern(storageFileNameFormat);
		return "e:/"+formatter.print(date)+".txt";
	}

	private String getStorageFileName (LocalDate date) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern(storageFileNameFormat);
		return "e:/"+formatter.print(date)+".txt";
	}

	public void poll(final String[] codes, long interval) throws IOException {
		DateTime now = new DateTime();
		DateTime startTrading = now.withMillisOfDay(0).withHourOfDay(9).withMinuteOfHour(30);
		long firstHalfStop = startTrading.withHourOfDay(11).withMinuteOfHour(30).getMillis();
		long secondHalfStart = startTrading.withHourOfDay(13).withMinuteOfHour(0).getMillis();
		long stopTrading = startTrading.withHourOfDay(15).withMinuteOfHour(0).getMillis();

		SimpleDateFormat tickFormat = new SimpleDateFormat(FinDataConstants.yyyyMMDDHHmmss);
		FileWriter fw = new FileWriter(getStorageFileName(now), true);
		final long[] currentMillis = new long[1];
		Timer timer = new Timer(false);
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				currentMillis[0] = System.currentTimeMillis();
				if (currentMillis[0] < firstHalfStop || currentMillis[0] > secondHalfStart) {
					NeteaseInstantSnapshot snapshot;
					try {
						snapshot = new NeteaseInstantSnapshot(codes);
					} catch (NullPointerException npe) {
						System.out.println("NPE caught when getting data.");
						return;
					}
					System.out.println("Time taken (millis): "+(System.currentTimeMillis() - currentMillis[0]));
					try {
						fw.write(tickFormat.format(new Date()));
						fw.write("|");
						fw.write(snapshot.getData().toJSONString());
						fw.write("\n");
						fw.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (currentMillis[0] > stopTrading) {
					try {
						fw.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					this.cancel();
					System.exit(0);
				}
			}
		}, startTrading.toDate(), interval);
	}
}