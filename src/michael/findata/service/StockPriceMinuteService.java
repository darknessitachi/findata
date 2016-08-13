package michael.findata.service;

import michael.findata.external.SecurityTimeSeriesData;
import michael.findata.external.SecurityTimeSeriesDatum;
import michael.findata.external.tdx.TDXClient;
import michael.findata.external.tdx.TDXMinuteLine;
import michael.findata.model.Stock;
import michael.findata.model.StockPriceMinute;
import michael.findata.spring.data.repository.StockPriceMinuteRepository;
import michael.findata.spring.data.repository.StockRepository;
import michael.findata.util.Consumer2;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

import static michael.findata.util.FinDataConstants.yyyyMMdd;

public class StockPriceMinuteService {

	@Autowired
	private StockRepository stockRepo;

	@Autowired
	private StockPriceMinuteRepository stockPriceMinuteRepo;

	@Autowired
	private StockService ss;

	public void updateMinuteData() throws IOException {
		saveAllMinuteLines("000000", true);
	}

	public void saveAllMinuteLines(String afterCode, boolean stopOrContinueOnExistingRecord) throws IOException {
		TDXClient client1 = new TDXClient(
//				"182.131.3.245:7709",    // 上证云行情J330 - 9ms)
				"221.237.158.106:7709",    // 西南证券金点子成都电信主站1
				"221.237.158.107:7709",    // 西南证券金点子成都电信主站2
				"221.237.158.108:7709"    // 西南证券金点子成都电信主站3
		);
		client1.connect();

		List<Stock> stocks = stockRepo.findByIgnoredAndFundAndCodeGreaterThanAndLatestSeasonIsNotNullOrderByCodeAsc(false, false, afterCode);
		stocks.addAll(stockRepo.findByIgnoredAndFundAndInterestingOrderByCodeAsc(false, true, true));
		for (Stock stock : stocks) {
			try {
				System.out.println("Starting: " + stock.getCode());
				boolean dup = false;
				short start = 0, step = 3;
				// Need to update data in 70 trading days
				while (start < 70 && !dup) {
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
	@Transactional(propagation = Propagation.REQUIRED)
	private boolean saveMinuteLineFromTDXClient(Stock stock, StockPriceMinuteRepository spmr, TDXClient client1, boolean stopOrContinueOnExistingRecord, short startOnDay, short daysCount) throws ParseException {
		SecurityTimeSeriesData data = client1.getEOMs(stock.getCode(), startOnDay, daysCount);

		long end = new DateTime(new SimpleDateFormat(yyyyMMdd).parse("20150801")).getMillis();
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

	public void walk(DateTime start,
					 DateTime end,
					 String[] codes,
					 boolean log,
					 Consumer2<DateTime, Map<String, SecurityTimeSeriesDatum>> doStuff) {
		Timestamp tsStart = new Timestamp(start.getMillis());
		Timestamp tsEnd = new Timestamp(end.getMillis());
		List<StockPriceMinute> spms = stockPriceMinuteRepo.findByDateBetweenAndStock_CodeInOrderByDate(tsStart, tsEnd, codes);

		Map<String, StockPriceMinute> dataPerDay = new HashMap<>();
		Consumer<DateTime> consumer = d -> {
			d = d.plusHours(9).plusMinutes(30);
			Map<String, SecurityTimeSeriesDatum> datumMap = new HashMap<>();
			for (int minute = 0; minute < 240; minute++) {
				if (minute == 119){
					d = d.plusMinutes(91);
				} else {
					d = d.plusMinutes(1);
				}
				datumMap.clear();
				for (String code : codes) {
					if (dataPerDay.containsKey(code)) {
						StockPriceMinute minuteData = dataPerDay.get(code);
						datumMap.put(code, new SecurityTimeSeriesDatum(
								d,
								minuteData.getOpen(minute),
								minuteData.getHigh(minute),
								minuteData.getLow(minute),
								minuteData.getClose(minute),
								minuteData.getVolum(minute),
								minuteData.getAmount(minute)
						));
					} else {
						datumMap.put(code, new SecurityTimeSeriesDatum(d));
					}
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
}