package michael.findata.external.ib;

import com.ib.client.*;
import michael.findata.ib.InteractiveBrokersAPI;
import michael.findata.model.ExchangeRateDaily;
import michael.findata.model.Stock;
import michael.findata.model.StockPrice;
import michael.findata.model.StockPriceMinute;
import michael.findata.spring.data.repository.ExchangeRateDailyRepository;
import michael.findata.spring.data.repository.StockPriceMinuteRepository;
import michael.findata.spring.data.repository.StockPriceRepository;
import michael.findata.spring.data.repository.StockRepository;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static michael.findata.util.LogUtil.getClassLogger;
import static michael.findata.util.FinDataConstants.yyyyMMdd;

/**
 * 2333 as an example, IB historical data has been adjusted with split but not dividend, how to handle it?
 * Answer: IB adjusts historical price when a new split/reverse split happens. In other words, once a split occurs,
 * all pre-split historically data from IB becomes invalid in our context (since we need raw data and compute adjustments according to both splits
 * and dividends. The best work-around is to update data in 5 hours after each trading session so we store all valid historical data in our DB
 */
public class HistoricalData {

	private static final Logger LOGGER = getClassLogger();

	@Autowired
	private ExchangeRateDailyRepository exchangeRateDailyRepo;

	@Autowired
	private StockPriceMinuteRepository stockPriceMinuteRepo;

	@Autowired
	private StockPriceRepository stockPriceRepo;

	@Autowired
	private StockRepository stockRepo;

	public HistoricalData () {
	}

	public void update (boolean recursive, int ... codes) throws InterruptedException {
		Updater u = new Updater(exchangeRateDailyRepo, stockPriceMinuteRepo, stockPriceRepo, stockRepo, recursive, codes);
//		try {
//			System.in.read();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		u.disconnect();
	}

	private static class Updater extends InteractiveBrokersAPI {

		private ExchangeRateDailyRepository exchangeRateDailyRepo;

		private StockPriceMinuteRepository stockPriceMinuteRepo;

		private StockPriceRepository stockPriceRepo;

		private StockRepository stockRepo;

		private static Contract contract_CNH_HKD  = new Contract();
		static {
			contract_CNH_HKD.exchange("IDEALPRO");
			contract_CNH_HKD.secType("CASH");
			contract_CNH_HKD.symbol("CNH");
			contract_CNH_HKD.currency("HKD");
		}
		private static Contract contract_USD_CNH = new Contract();
		static {
			contract_USD_CNH.exchange("IDEALPRO");
			contract_USD_CNH.secType("CASH");
			contract_USD_CNH.symbol("USD");
			contract_USD_CNH.currency("CNH");
		}
		private static final int TICK_ID_CNH_HKD = contract_CNH_HKD.hashCode();
		private static final int TICK_ID_USD_CNH = contract_USD_CNH.hashCode();
		private static final int DAILY_DATA_OFFSET = 100000;
		private static final int RESERVED_DATA_OFFSET = 200000;

		private static DateTimeFormatter shortDateFormat = DateTimeFormat.forPattern(yyyyMMdd);
		private static DateTimeFormatter shortDateTimeFormat = DateTimeFormat.forPattern("yyyyMMdd  HH:mm:ss");

		private Thread thread;

		private boolean recursive = false;

		private Map<Integer, Boolean> finished;

		private Thread mainThread;

		private Updater (ExchangeRateDailyRepository exchangeRateDailyRepo,
						 StockPriceMinuteRepository spmRepo,
						 StockPriceRepository spRepo,
						 StockRepository stockRepo,
						 boolean recursive,
						 int ... codes) throws InterruptedException {
			this.exchangeRateDailyRepo = exchangeRateDailyRepo;
			this.stockPriceMinuteRepo = spmRepo;
			this.stockPriceRepo = spRepo;
			this.stockRepo = stockRepo;
			spmMap = new HashMap<>();
			finished = new ConcurrentHashMap<>();
			this.recursive = recursive;
			this.run(codes);
		}

		private void run(int ... codes) throws InterruptedException {
			// this thread is in charge of start / stop the client
			mainThread = Thread.currentThread();
			m_s.eConnect("127.0.0.1", 4001, 0);
			final EReader reader = new EReader(m_s, m_signal);
			reader.start();

			thread = new Thread() {
				public void run() {
					while (m_s.isConnected()) {
						m_signal.waitForSignal();
						try {
							reader.processMsgs();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			};
			thread.start();
			for (int code : codes) {
				finished.put(code, false);
				finished.put(code+DAILY_DATA_OFFSET, false);
			}
			finished.put(TICK_ID_CNH_HKD, false);
			finished.put(TICK_ID_USD_CNH, false);

			for (int code : codes) {
				updateHKStockByMinute(code);
				Thread.sleep(200);
				updateHKStockByDay(code);
				Thread.sleep(200);
			}
			updateForexCNHHKD();
			Thread.sleep(200);
			updateForexUSDCNH();
			try {
				while (true) {
					Thread.sleep(999999999);
				}
			} catch (InterruptedException e) {
				LOGGER.info("Main thread interrupted. Terminating.");
			}
		}

		/**
		 * Endlessly go back to history and try to update as much as possible
		 * @param code hk stock code, for example 914 (as 0914 or 00914 in other places) is
		 *             Anhui Conch Cement Co Ltd.
		 */
		private void updateHKStockByMinute(int code) {
			Contract stockContract = new Contract();
			stockContract.exchange("SEHK");
			stockContract.secType("STK");
			stockContract.symbol(code+"");
			StockPriceMinute minSpm = stockPriceMinuteRepo.findTopByStock_CodeOrderByDateAsc(code > 999? "0"+code : "00"+code);
			long minMillis;
			String now;
			if (minSpm != null && recursive) {
				minMillis = minSpm.getDate().getTime();
				now = shortDateFormat.print(minMillis)+" 00:01:01 GMT";
			} else {
				minMillis = System.currentTimeMillis();
				now = shortDateFormat.print(minMillis)+" 17:01:01 GMT";
			}
//			now = shortDateFormat.print(minMillis)+" 00:01:01 GMT";
			m_s.reqHistoricalData(code, stockContract, now, "20 D", "1 min", "TRADES", 1, 1, null);
		}

		private void updateHKStockByDay(int code) {
			Contract stockContract = new Contract();
			stockContract.exchange("SEHK");
			stockContract.secType("STK");
			stockContract.symbol(code+"");
			StockPrice minSp = stockPriceRepo.findTopByStock_CodeOrderByDateAsc(code > 999? "0"+code : "00"+code);
			long minMillis;
			String now;
			if (minSp != null && recursive) {
				minMillis = minSp.getDate().getTime();
				now = shortDateFormat.print(minMillis)+" 00:01:01 GMT";
			} else {
				minMillis = System.currentTimeMillis();
				now = shortDateFormat.print(minMillis)+" 17:01:01 GMT";
			}
			m_s.reqHistoricalData(code+DAILY_DATA_OFFSET, stockContract, now, "20 D", "1 day", "TRADES", 1, 1, null);
		}

		private void updateForexCNHHKD() {
			// update exchange rates for the past 40 trading days
			String now = shortDateFormat.print(System.currentTimeMillis())+" 22:59:59 GMT";
			m_s.reqHistoricalData(TICK_ID_CNH_HKD, contract_CNH_HKD, now, "40 D", "1 day", "MIDPOINT", 1, 1, null);
		}

		private void updateForexUSDCNH() {
			// update exchange rates for the past 40 trading days
			String now = shortDateFormat.print(System.currentTimeMillis())+" 22:59:59 GMT";
			m_s.reqHistoricalData(TICK_ID_USD_CNH, contract_USD_CNH, now, "40 D", "1 day", "MIDPOINT", 1, 1, null);
		}

		@Override
		public void error(int id, int errorCode, String errorMsg) {
			LOGGER.info("Error - id: "+id+", errorCode: "+errorCode+", errorMessage: "+errorMsg);
		}

		@Override
		public void error(String str) {
			LOGGER.info("Error: "+str);
		}

		private Map<Integer, StockPriceMinute> spmMap;

		private LocalDate tempCurrentDate = null;

		private int lastMinuteIndex = 329;

		@Override
		public void historicalData(int reqId, String date, double open, double high, double low, double close, int volume, int count, double WAP, boolean hasGaps) {
//			LOGGER.info("HistoricalData: "+reqId+" - Date: "+date+", Open: "+open+", High: "+high+", Low: "+low+", Close: "+close+", Volume: "+volume+", Count: "+count+", WAP: "+WAP+", HasGaps: "+hasGaps);
			ExchangeRateDaily dailyEx = new ExchangeRateDaily();
			if (reqId == TICK_ID_CNH_HKD) {
				dailyEx.currency("HKD");
				dailyEx.open(1 / open);
				dailyEx.high(1 / high);
				dailyEx.low(1 / low);
				dailyEx.close(1 / close);
			} else if (reqId == TICK_ID_USD_CNH) {
				dailyEx.currency("USD");
				dailyEx.open(open);
				dailyEx.high(high);
				dailyEx.low(low);
				dailyEx.close(close);
			} else if (DAILY_DATA_OFFSET < reqId  && reqId < RESERVED_DATA_OFFSET) {
				// Daily data for hk stocks
				processDailyData (reqId, date, open, high, low, close, volume, count, WAP, hasGaps);
				return;
			} else {
				DateTime dateTime;
				try {
					dateTime = shortDateTimeFormat.parseDateTime(date);
				} catch (IllegalArgumentException e) {
					LOGGER.info("Historical data finished: {}", reqId);
					if (recursive) {
						updateHKStockByMinute(reqId);
					} else {
						finished.put(reqId, true);
						StockPriceMinute spm = spmMap.get(reqId);
						if (spm != null) {
							// Save old
							saveSpm(spm);
						}
						disconnectIfAllFinished();
					}
					return;
				}

				StockPriceMinute spm = spmMap.get(reqId);
				if (tempCurrentDate == null || !tempCurrentDate.equals(dateTime.toLocalDate())) {
					if (spm != null) {
						// Save old
						saveSpm(spm);
					}

					// Create new
					LOGGER.info("Start of 1st half: {}", date);
					Stock stock = stockRepo.findOneByCode(reqId > 999 ? "0"+reqId : "00"+reqId);
					spm = new StockPriceMinute(330);
					Timestamp ts = new Timestamp(dateTime.toLocalDate().toDate().getTime());
					spm.setStock(stock);
					spm.setDate(ts);
					spmMap.put(reqId, spm);
					tempCurrentDate = dateTime.toLocalDate();
				}
				int minuteIndex = dateTime.getMinuteOfDay();

				if (minuteIndex < 720) {
					minuteIndex -= 570;
				} else if (minuteIndex < 960) {
					minuteIndex -= 630;
				}

				if (minuteIndex < 330) {
					spm.setOpen(minuteIndex, (int)(open*1000));
					spm.setHigh(minuteIndex, (int)(high*1000));
					spm.setLow(minuteIndex, (int)(low*1000));
					spm.setClose(minuteIndex, (int)(close*1000));
					spm.setVolume(minuteIndex, volume);
					spm.setAmount(minuteIndex, (float)(volume*WAP));
					if (minuteIndex > 327) {
						LOGGER.info("Count: {} HistoricalData: "+reqId+" - Date: "+date+", Open: "+open+", High: "+high+", Low: "+low+", Close: "+close+", Volume: "+volume+", Count: "+count+", WAP: "+WAP+", HasGaps: "+hasGaps, minuteIndex);
					}
					if (minuteIndex - lastMinuteIndex != 1 && !(minuteIndex == 0 && lastMinuteIndex == 329)) {
						LOGGER.warn("Strange: minuteIndex: {}, lastMinuteIndex {}", minuteIndex, lastMinuteIndex);
					}
					lastMinuteIndex = minuteIndex;
				}
				return;
			}

			// for exchange hkd usd exchange rates only
			try {
				dailyEx.date(new Timestamp(DateTime.parse(date, shortDateFormat).getMillis()));
				exchangeRateDailyRepo.save(dailyEx);
//				LOGGER.info("HistoricalData. "+reqId+" - Date: "+date+", Open: "+open+", High: "+high+", Low: "+low+", Close: "+close+", Volume: "+volume+", Count: "+count+", WAP: "+WAP+", HasGaps: "+hasGaps);
			} catch (IllegalArgumentException e) {
//				thread.stop();
				finished.put(reqId, true);
				disconnectIfAllFinished();
//				m_s.eDisconnect();
				// todo somehow the threads doesn't stop
			} catch (Exception e) {
//				LOGGER.info(e.getMessage());
			}
		}

		private void saveSpm(StockPriceMinute spm) {
			try {
				stockPriceMinuteRepo.save(spm);
				LOGGER.info("EOM Saved:\t"+spm.getStock().getCode()+"\t"+spm.getStock().getName()+"\t"+spm.getDate());
				// Verification
				LOGGER.info("328: Open: {}, High: {}, Low: {}, Close: {}, Volume: {}, Amount {}",spm.getOpen(328),spm.getHigh(328),spm.getLow(328),spm.getClose(328),spm.getVolum(328),spm.getAmount(328));
				LOGGER.info("329: Open: {}, High: {}, Low: {}, Close: {}, Volume: {}, Amount {}",spm.getOpen(329),spm.getHigh(329),spm.getLow(329),spm.getClose(329),spm.getVolum(329),spm.getAmount(329));
			} catch (DataIntegrityViolationException ex) {
				LOGGER.warn("Duplicate met when saving EOM:\t"+spm.getStock().getCode()+"\t"+spm.getStock().getName()+"\t"+spm.getDate());
			}
		}

		private void disconnectIfAllFinished () {
			for (boolean f : finished.values()) {
				if (!f) {
					LOGGER.info("Not all updates finished, returning.");
					return;
				}
			}
			LOGGER.info("All updates finished, disconnecting.");
			m_s.eDisconnect();
			mainThread.interrupt();
		}

		private void processDailyData(int reqId, String date, double open, double high, double low, double close, int volume, int count, double WAP, boolean hasGaps) {
			int code = reqId-DAILY_DATA_OFFSET;
			if (date.startsWith("finished")) {
				LOGGER.info("Ended: {}", code);
				if (recursive) {
					// TODO: 12/25/2016
					updateHKStockByDay (code);
				} else {
					finished.put(reqId, true);
					disconnectIfAllFinished();
				}
				return;
			}
			StockPrice price = new StockPrice();
			price.setStock(stockRepo.findOneByCode(code > 999 ? "0"+code : "00"+code));
			price.setDate(new Timestamp(DateTime.parse(date, shortDateFormat).getMillis()));
			price.setOpen((int)(open*1000));
			price.setHigh((int)(high*1000));
			price.setLow((int)(low*1000));
			price.setClose((int)(close*1000));
			price.setAvg((int)(WAP*1000));
			try {
				stockPriceRepo.save(price);
				LOGGER.info("EOD price saved:\t"+price.getStock().getCode()+"\t"+price.getStock().getName()+"\t"+price.getDate());
			} catch (DataIntegrityViolationException ex) {
				LOGGER.warn("Duplicate met when saving EOD:\t"+price.getStock().getCode()+"\t"+price.getStock().getName()+"\t"+price.getDate());
			}
		}
	}
}