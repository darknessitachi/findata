package michael.findata.data.historicaldata.tdx;

import com.numericalmethod.algoquant.data.cache.SequentialCache;
import com.numericalmethod.algoquant.data.cache.SequentialCacheFactory;
import com.numericalmethod.algoquant.data.cache.TimedEntry;
import michael.findata.algoquant.execution.datatype.StockEOM;
import michael.findata.external.SecurityTimeSeriesData;
import michael.findata.external.SecurityTimeSeriesDatum;
import michael.findata.external.tdx.TDXMinuteLine;
import michael.findata.model.Stock;
import michael.findata.util.CalendarUtil;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.*;

/**
 * Tdx End of Minute OHLC data
 */
public class TdxEOMCacheFactory implements SequentialCacheFactory<Stock, StockEOM> {
	@Override
	public SequentialCache<StockEOM> newInstance(Stock product, Interval interval) {
		return new TdxEOMCache(product, interval);
	}

	// This is a method that generates multiple caches in a synchronous manner
	public Map<Stock, SequentialCache<StockEOM>> newInstances (Stock[] stocks, Interval interval) {
		DateTime start = interval.getStart();
		DateTime end = interval.getEnd();
		HashMap<Stock, List<TimedEntry<StockEOM>>> quotes = new HashMap<>();
		SecurityTimeSeriesData [] serieses = new SecurityTimeSeriesData[stocks.length];
		for (int i = stocks.length - 1; i > -1; i--) {
			quotes.put(stocks[i], new ArrayList<>());
			serieses[i] = new TDXMinuteLine(stocks[i].getCode());
		}

		DateTime latestTick;

		while (true) {
			// Ensure all serises have next tick
			for (SecurityTimeSeriesData series : serieses) {
				if (!series.hasNext()) {
					break;
				}
			}
			// Ensure no more than maxTicks
//			if (quotes.get(codes[0]).size() >= maxTicks) {
//				break;
//			}
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
				for (int i = stocks.length - 1; i > -1; i--) {
					if (serieses[i].peekNext() == null || serieses[i].peekNext().getDateTime().getMillis() < latestTick.getMillis()) {
						// dummy data, since there is no trading for this stock at this tick
//						System.out.println(codes[i]+"\t"+latestTick+"\tdummy");
						quotes.get(stocks[i]).add(new TimedEntry<>(latestTick, new SecurityTimeSeriesDatum(latestTick)));
					} else {
//						System.out.println(codes[i]+"\t"+latestTick);
						quotes.get(stocks[i]).add(new TimedEntry<>(latestTick, serieses[i].popNext()));
					}
				}
			} else {
				// after end
				for (int i = stocks.length - 1; i > -1; i--) {
					if (serieses[i].peekNext().getDateTime().getMillis() == latestTick.getMillis()) {
						serieses[i].popNext();
					}
				}
			}
		}
		Arrays.stream(serieses).forEach(SecurityTimeSeriesData::close);

		Map<Stock, SequentialCache<StockEOM>> results = new HashMap<>();

		quotes.entrySet().forEach(entry -> {
			Collections.reverse(entry.getValue());
			results.put(entry.getKey(), new TdxEOMCache(entry.getValue()));
		});

		return results;
	}

	public Map<Stock, SequentialCache<StockEOM>> newInstances(Collection<? extends Stock> stocks, Interval interval) {
		return newInstances(stocks.toArray(new Stock[stocks.size()]), interval);
	}

	private class TdxEOMCache implements SequentialCache<StockEOM> {

		private List<TimedEntry<StockEOM>> list;

		public TdxEOMCache (List<TimedEntry<StockEOM>> list) {
			this.list = list;
		}

		public TdxEOMCache (Stock stock, Interval interval) {
			TDXMinuteLine minuteLine;
			minuteLine = new TDXMinuteLine (stock.getCode());
			SecurityTimeSeriesDatum quote;
			DateTime end = interval.getEnd();
			DateTime cur;
			DateTime start = interval.getStart();
			list = new ArrayList<>();
			while (minuteLine.hasNext()) {
				quote = minuteLine.popNext();
				cur = quote.getDateTime();
				if (cur.isBefore(start)) {
					break;
				} else if (!cur.isAfter(end)) {
					list.add(new TimedEntry<>(cur, quote));
				}
			}
			Collections.reverse(list);
		}

		@Override
		public Iterator<TimedEntry<StockEOM>> iterator() {
			return list.iterator();
		}
	}
}