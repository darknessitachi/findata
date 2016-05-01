package michael.findata.service;

import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import michael.findata.algoquant.execution.datatype.depth.Depth;
import michael.findata.algoquant.strategy.pair.StockGroups;
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

	public List<MarketCondition> getDailyData (LocalDate date) throws FileNotFoundException {
		DateTimeFormatter formatter = DateTimeFormat.forPattern(FinDataConstants.yyyyMMDDHHmmss);
		BufferedReader br;
		br = new BufferedReader(new FileReader(getStorageFileName(date)));
		String line;
		DateTime tick;
		NeteaseInstantSnapshot snapshot;
		List<MarketCondition> result = new ArrayList<>();
		try {
			while ((line = br.readLine()) != null) {
				tick = formatter.parseDateTime(line.substring(0, line.indexOf('|')));
				snapshot = new NeteaseInstantSnapshot(line.substring(line.indexOf('|') + 1));
				snapshot.setTick(tick);
				snapshot.purgeData();
				result.add(snapshot);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	// todo: simplify this with getDailyData(date) call
	public void walk(LocalDate adjStart,
					 LocalDate start,
					 LocalDate end,
					 String[] codes,
					 boolean log,
					 Consumer3<DateTime,
							Map<String, Depth>,
							HashMap<String, Function<Integer, Integer>>> doStuff) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern(FinDataConstants.yyyyMMDDHHmmss);

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

		LocalDate day = start;
		long endMillis = end.toDate().getTime();
		while (day.toDate().getTime() <= endMillis) {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(getStorageFileName(day)));
			} catch (FileNotFoundException e) {
				continue;
			} finally {
				day = day.plusDays(1);
			}
			String line;

			try {
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
					doStuff.apply(tick, snapshot.getDepthMap(), currentAdjFun);
					if (log) {
						System.out.println(tick+" "+snapshot);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
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