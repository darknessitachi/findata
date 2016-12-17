package michael.findata.service;

import com.numericalmethod.algoquant.execution.datatype.product.stock.Exchange;
import michael.findata.external.SecurityTimeSeriesData;
import michael.findata.external.SecurityTimeSeriesDatum;
import michael.findata.external.tdx.TDXClient;
import michael.findata.external.tdx.TDXMinuteLine;
import michael.findata.model.ExchangeRateDaily;
import michael.findata.model.Stock;
import michael.findata.model.StockPriceMinute;
import michael.findata.spring.data.repository.ExchangeRateDailyRepository;
import michael.findata.spring.data.repository.StockPriceMinuteRepository;
import michael.findata.spring.data.repository.StockRepository;
import michael.findata.util.Consumer2;
import org.joda.time.DateTime;
import org.joda.time.DateTimeField;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

import static michael.findata.util.FinDataConstants.yyyyMMdd;
@Service
public class StockPriceMinuteService {

	@Autowired
	private StockRepository stockRepo;

	@Autowired
	private StockPriceMinuteRepository stockPriceMinuteRepo;

	@Autowired
	private StockService ss;

	@Autowired
	private ExchangeRateDailyRepository exchangeRateDailyRepo;

	public void updateMinuteData() throws IOException {
		saveAllMinuteLines("000000", "999999", true);
	}

	public void saveAllMinuteLines(String codeStart, String codeEnd, boolean stopOrContinueOnExistingRecord) throws IOException {
		TDXClient client1 = new TDXClient(
//				"182.131.3.245:7709",    // 上证云行情J330 - 9ms)
				"221.237.158.107:7709",    // 西南证券金点子成都电信主站2
				"221.237.158.108:7709",    // 西南证券金点子成都电信主站3
				"221.237.158.106:7709"    // 西南证券金点子成都电信主站1
		);
		client1.connect();

		List<Stock> stocks = stockRepo.findByIgnoredAndFundAndCodeBetweenAndLatestSeasonIsNotNullOrderByCodeAsc(false, false, codeStart, codeEnd);
//		List<Stock> stocks = new ArrayList<>();
		stocks.addAll(stockRepo.findByIgnoredAndFundAndInterestingOrderByCodeAsc(false, true, true));
		for (Stock stock : stocks) {
			try {
				System.out.println("Starting: " + stock.getCode());
				boolean dup = false;
				short start = 0, step = 3;
				// Need to update data in 70 trading days
				while (start < 110 && !dup) {
//					System.out.println(start+" "+dup);
					dup = saveMinuteLineFromTDXClient(stock, stockPriceMinuteRepo, client1, stopOrContinueOnExistingRecord, start, step);
					start += step;
				}
				System.out.println("Completed: " + stock.getCode());
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				System.out.println("Error: " + stock.getCode());
				System.out.println(e.getMessage());
			}
		}
		client1.disconnect();
	}

	// return true if any duplicate was met during the update
	// for mainland china stocks only
	@Transactional(propagation = Propagation.REQUIRED)
	private boolean saveMinuteLineFromTDXClient(Stock stock, StockPriceMinuteRepository spmr, TDXClient client1,
												boolean stopOrContinueOnExistingRecord, short startOnDay, short daysCount) throws ParseException {
		SecurityTimeSeriesData data = client1.getEOMs(stock.getCode(), startOnDay, daysCount);

		StockPriceMinute spm = null;
		SecurityTimeSeriesDatum minuteDatum;
		int count = 0, minuteIndex, updateCount = 0;
		int volAccumulated = 0;
		boolean dupMet = false;
		while (data.hasNext()) {
			minuteDatum = data.popNext();
			minuteIndex = 240 - count % 240 - 1;
			if (minuteIndex == 239) {
				// Last minute of a day
				// Do some verification:
				assert minuteDatum.getDateTime().getHourOfDay() == 15;
				assert minuteDatum.getDateTime().getMinuteOfHour() == 0;

				// create a new record
				spm = new StockPriceMinute();
				Timestamp ts = new Timestamp(minuteDatum.getDateTime().toLocalDate().toDate().getTime());
				spm.setStock(stock);
				spm.setDate(ts);
				volAccumulated = 0;
			}
			spm.setOpen(minuteIndex, minuteDatum.getOpen());
			spm.setHigh(minuteIndex, minuteDatum.getHigh());
			spm.setLow(minuteIndex, minuteDatum.getLow());
			spm.setClose(minuteIndex, minuteDatum.getClose());
			spm.setVolume(minuteIndex, minuteDatum.getVolume());
			spm.setAmount(minuteIndex, minuteDatum.getAmount());
			volAccumulated += minuteDatum.getVolume();
			if (minuteIndex == 0 && volAccumulated != 0) {
				// First minute of the day
				// Do some verification:
				assert minuteDatum.getDateTime().getHourOfDay() == 9;
				assert minuteDatum.getDateTime().getMinuteOfHour() == 30;

				// Save the record
//				System.out.println("Saved: "+spm);
				try {
					spmr.save(spm);
					System.out.println("Saved:\t"+stock.getCode()+"\t"+stock.getName()+"\t"+spm.getDate());
					updateCount++;
				} catch (DataIntegrityViolationException e) {
					if (e.getCause().getCause().getMessage().contains("Duplicate")) {
						if (stopOrContinueOnExistingRecord) {
							// stop
							System.out.println("Meet a duplicate, stop on "+stock.getCode());
							System.out.println(stock.getCode()+"\t"+stock.getName()+"\tUpdate count:\t"+updateCount);
							return true;
						} else {
							System.out.println("Meet a duplicate, continue on "+stock.getCode());
							count ++;
							dupMet = true;
							continue;
						}
					} else {
						throw e;
					}
				}
				// ignore data earlier than this date, since there shouldn't be any
				long end = new DateTime(new SimpleDateFormat(yyyyMMdd).parse("20150801")).getMillis();
				if (spm.getDate().getTime() == end) {
					System.out.println(stock.getCode()+"\t"+stock.getName()+"\tUpdate count:\t"+updateCount);
					return dupMet;
				}
			}
			count++;
		}
		System.out.println(stock.getCode()+"\t"+stock.getName()+"\tUpdate count:\t"+updateCount);
		return dupMet;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	private void saveMinuteLineFromFile(Stock stock, StockPriceMinuteRepository spmr) throws ParseException {
		long start = new DateTime(new SimpleDateFormat(yyyyMMdd).parse("20150711")).getMillis();
		StockPriceMinute spm = null;
		TDXMinuteLine line = new TDXMinuteLine(stock.getCode());
		SecurityTimeSeriesDatum minuteDatum;
		int count = 0, minuteIndex;
		while (line.hasNext() && line.peekNext().getDateTime().getMillis() > start) {
//			System.out.println(count);
			minuteDatum = line.popNext();
			minuteIndex = 240 - count % 240 - 1;
			if (minuteIndex == 239) {
				// Last minute of a day
				// Do some verification:
				assert minuteDatum.getDateTime().getHourOfDay() == 15;
				assert minuteDatum.getDateTime().getMinuteOfHour() == 0;

				// create a new record
				spm = new StockPriceMinute();
				Timestamp ts = new Timestamp(minuteDatum.getDateTime().toLocalDate().toDate().getTime());
				spm.setStock(stock);
				spm.setDate(ts);
			}
//			System.out.print("Minute: \t"+minuteIndex);
//			System.out.print("\tTime:\t"+minuteDatum.getDateTime());
//			System.out.print("\tOpen:\t"+minuteDatum.getOpen());
//			System.out.print("\tHigh:\t"+minuteDatum.getHigh());
//			System.out.print("\tLow:\t"+minuteDatum.getLow());
//			System.out.print("\tClose:\t"+minuteDatum.getClose());
//			System.out.print("\tVol:\t"+minuteDatum.getVolume());
//			System.out.println("\tAmt:\t"+minuteDatum.getAmount());
			spm.setOpen(minuteIndex, minuteDatum.getOpen());
			spm.setHigh(minuteIndex, minuteDatum.getHigh());
			spm.setLow(minuteIndex, minuteDatum.getLow());
			spm.setClose(minuteIndex, minuteDatum.getClose());
			spm.setVolume(minuteIndex, minuteDatum.getVolume());
			spm.setAmount(minuteIndex, minuteDatum.getAmount());
			if (minuteIndex == 0) {
				// First minute of the day
				// Do some verification:
				assert minuteDatum.getDateTime().getHourOfDay() == 9;
				assert minuteDatum.getDateTime().getMinuteOfHour() == 30;

				// Save the record
//				System.out.println("Saved: "+spm);
				try {
					System.out.println("Try saving: "+spm.getDate()+" "+stock.getCode());
					spmr.save(spm);
				} catch (DataIntegrityViolationException e) {
					count ++;
					continue;
				}
			}
			count++;
		}
	}

	// for A share only or H share only, doesn't handle mix
	public void walk(LocalDate start,
					 LocalDate end,
					 boolean log,
					 Consumer2<DateTime, Map<String, SecurityTimeSeriesDatum>> doStuff,
					 String ... codes) {
		Timestamp tsStart = new Timestamp(start.toDateTimeAtStartOfDay().getMillis());
		Timestamp tsEnd = new Timestamp(end.toDateTimeAtStartOfDay().getMillis());
		List<StockPriceMinute> spms = stockPriceMinuteRepo.findByDateBetweenAndStock_CodeInOrderByDate(tsStart, tsEnd, Arrays.asList(codes));
		// exchange rates
		Map<Timestamp, ExchangeRateDaily> forexHKD = new HashMap<>();
		Map<Timestamp, ExchangeRateDaily> forexUSD = new HashMap<>();
		exchangeRateDailyRepo.findByDateBetweenAndCurrency(tsStart, tsEnd, "HKD").forEach(forex -> {
			forexHKD.put(forex.date(), forex);
		});
		exchangeRateDailyRepo.findByDateBetweenAndCurrency(tsStart, tsEnd, "USD").forEach(forex -> {
			forexUSD.put(forex.date(), forex);
		});
		boolean containsHK = false, containsA = false;
		for (String code: codes) {
			if (code.length() == 5) {
				containsHK = true;
			} else {
				containsA = true;
			}
			if (containsA && containsHK) {
				break;
			}
		}
		boolean mixed;
		int var1, var2, var3, noOfMinutes;
		if (containsA && !containsHK) {
			var1 = 30;
			noOfMinutes = 240;
			var2 = 119;
			var3 = 91;
			mixed = false;
		} else if (containsHK && !containsA) {
			// H share only
			var1 = 29;
			noOfMinutes = 330;
			var2 = 150;
			var3 = 61;
			mixed = false;
		} else if (containsHK) {
			// Both A and H stocks are present, mixed)
			var1 = 30;
			noOfMinutes = 240;
			var2 = 119;
			var3 = 91;
			mixed = true;
		} else {
			return;
		}

		Map<String, StockPriceMinute> dataPerDay = new HashMap<>();
		Consumer<DateTime> consumer = d -> {
			// Forex
			ExchangeRateDaily hkdRate = forexHKD.get(new Timestamp(d.toDate().getTime()));
			ExchangeRateDaily usdRate = forexUSD.get(new Timestamp(d.toDate().getTime()));

			int minuteIndexHShare, effectiveMinuteIndex;
			d = d.plusHours(9).plusMinutes(var1);
			Map<String, SecurityTimeSeriesDatum> datumMap = new HashMap<>();
			for (int minute = 0; minute < noOfMinutes; minute++) {
				if (minute == var2){
					d = d.plusMinutes(var3);
				} else {
					d = d.plusMinutes(1);
				}
				minuteIndexHShare = transformMinuteIndexA2H(minute);
				datumMap.clear();
				for (String code : codes) {
					if (dataPerDay.containsKey(code)) {
						StockPriceMinute minuteData = dataPerDay.get(code);
						effectiveMinuteIndex = mixed && minuteData.getCountPerDay() == 330? minuteIndexHShare : minute;
						if (minuteData.getClose(effectiveMinuteIndex) == 0) {
							datumMap.put(code, new SecurityTimeSeriesDatum(d));
						} else {
							datumMap.put(code, new SecurityTimeSeriesDatum(
									d,
									minuteData.getOpen(effectiveMinuteIndex),
									minuteData.getHigh(effectiveMinuteIndex),
									minuteData.getLow(effectiveMinuteIndex),
									minuteData.getClose(effectiveMinuteIndex),
									minuteData.getVolum(effectiveMinuteIndex),
									minuteData.getAmount(effectiveMinuteIndex)
							));
						}
					} else {
						datumMap.put(code, new SecurityTimeSeriesDatum(d));
					}
				}
				// exchange rate
				if (hkdRate != null) {
					datumMap.put("HKD", new SecurityTimeSeriesDatum(d, 0, 0, 0, 0, 0, (float)hkdRate.close(), false));
				}
				if (usdRate != null) {
					datumMap.put("USD", new SecurityTimeSeriesDatum(d, 0, 0, 0, 0, 0, (float)usdRate.close(), false));
				}
				doStuff.apply(d, datumMap);
				if (log) {
					Arrays.stream(codes).forEach(c -> System.out.println(c + "\t" + datumMap.get(c)));
				}
			}
		};

		DateTime date = null;
		DateTime oldDate;
		for(StockPriceMinute spm : spms) {
			oldDate = date;
			date = new DateTime(spm.getDate());
			if (oldDate != null && !oldDate.equals(date)) {
				// date changed, meaning we have all data for the last day...
				// run for every minute of the trading day (240)
				consumer.accept(oldDate);
				dataPerDay.clear();
			}
			dataPerDay.put(spm.getStock().getCode(), spm);
		}
		// last day
		if (date != null) {
			consumer.accept(date);
		}
	}

	private int transformMinuteIndexA2H(int minuteIndexAShare) {
		if (minuteIndexAShare < 119) {
			return minuteIndexAShare + 1;
		} else {
			return minuteIndexAShare + 31;
		}
	}
}