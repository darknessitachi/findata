package michael.findata.commandcenter;

import com.numericalmethod.algoquant.execution.component.broker.Broker;
import com.numericalmethod.algoquant.execution.datatype.depth.Depth;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.SimpleMarketCondition;
import com.numericalmethod.algoquant.execution.datatype.product.Product;
import com.numericalmethod.algoquant.execution.datatype.product.stock.Exchange;
import com.numericalmethod.algoquant.execution.strategy.Strategy;
import com.numericalmethod.algoquant.execution.strategy.handler.DepthHandler;
import michael.findata.algoquant.execution.component.broker.LocalHexinBrokerProxy;
import michael.findata.algoquant.execution.component.broker.MetaBroker;
import michael.findata.algoquant.execution.listener.DepthListener;
import michael.findata.algoquant.execution.strategy.handler.DividendHandler;
import michael.findata.algoquant.execution.strategy.handler.MarketConditionHandler;
import michael.findata.email.AsyncMailer;
import michael.findata.external.sina.SinaHkInstantSnapshot;
import michael.findata.external.tdx.TDXClient;
import michael.findata.model.Dividend;
import michael.findata.model.Stock;
import michael.findata.service.DividendService;
import michael.findata.spring.data.repository.DividendRepository;
import michael.findata.util.DBUtil;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static michael.findata.util.LogUtil.getClassLogger;

// This is a virtual machine act like a robot that trades based on plug&play trading strategies
public class CommandCenter implements DepthListener {

	private static final Logger LOGGER = getClassLogger();

	@Autowired
	private DividendService dividendService;
	@Autowired
	private DividendRepository dividendRepo;

	private HashMap<Stock, List<Strategy>> relevantStrs = new HashMap<>();
	private Set<Stock> targetSecurities = null;

	private TDXClient shSzClient = null;
	private TDXClient szClient = null;
	private TDXClient shClient = null;

	private Broker broker;
	private List<Strategy> strategies;
	private Stock[] shSzStocks;
	private Stock[] shStocks;
	private Stock[] szStocks;
	private Stock[] hkStocks;

	private TDXPollThread shSzThread = null;
	private TDXPollThread shThread = null;
	private TDXPollThread szThread = null;
	private HkStockPollThread hkThread = null;

//	private boolean shSzStopped = true;
//	private boolean shStopped = true;
//	private boolean szStopped = true;

	public static long firstHalfStartCN;
	public static long firstHalfEndCN;
	public static long secondHalfStartCN;
	public static long secondHalfEndCN;

	public static long firstHalfStartHK;
	public static long firstHalfEndHK;
	public static long secondHalfStartHK;
	public static long secondHalfEndHK;

	private AtomicBoolean locked = new AtomicBoolean(false);

	public CommandCenter () {
		strategies = new ArrayList<>();
		setFirstHalfStartCN(new LocalTime(9, 29, 50));
		setFirstHalfStartHK(new LocalTime(9, 29, 50));
		setFirstHalfEndCN(new LocalTime(11, 30, 10));
		setFirstHalfEndHK(new LocalTime(12, 0, 10));
		setSecondHalfStartCN(new LocalTime(12, 59, 50));
		setSecondHalfStartHK(new LocalTime(12, 59, 50));
		setSecondHalfEndCN(new LocalTime(15, 0, 10));
		setSecondHalfEndHK(new LocalTime(16, 0, 10));
	}

	public void addTargetSecurities(Collection<Stock> targetSecurities) {
		if (this.targetSecurities == null) {
			this.targetSecurities = new HashSet<>();
		}
		this.targetSecurities.addAll(targetSecurities);
	}

	// TODO: 9/16/2016 migrate to addTargetSecurities - do this at start ()
	private void setTargetSecurities() {
//		if (this.targetSecurities != null) {
//			LOGGER.warn("Unable to set targetSecurities again, it is already set.");
//			return;
//		}
//		this.targetSecurities = new HashSet<>();
//		this.targetSecurities.addAll(targetSecurities);

		List<Stock> shStocks = new ArrayList<>();
		List<Stock> szStocks = new ArrayList<>();
		List<Stock> hkStocks = new ArrayList<>();

		targetSecurities.forEach(stock -> {
			if (stock.exchange().equals(Exchange.SHSE)) {
				shStocks.add(stock);
			} else if (stock.exchange().equals(Exchange.SZSE)) {
				szStocks.add(stock);
			} else if (stock.exchange().equals(Exchange.HKEX)) {
				hkStocks.add(stock);
			}
		});
		this.shStocks = shStocks.toArray(new Stock [shStocks.size()]);
		this.szStocks = szStocks.toArray(new Stock [szStocks.size()]);
		shStocks.addAll(szStocks);
		this.shSzStocks = shStocks.toArray(new Stock [shStocks.size()]);
		this.hkStocks = hkStocks.toArray(new Stock [hkStocks.size()]);
	}

	private void setBroker(Broker broker) {
		this.broker = broker;
	}

	public void setSzClient(TDXClient szClient) {
		if (this.szClient != null) {
			LOGGER.warn("Unable to set szClient again, it is already set.");
			return;
		}
		this.szClient = szClient;
	}

	public void setShClient(TDXClient shClient) {
		if (this.shClient != null) {
			LOGGER.warn("Unable to set shClient again, it is already set.");
			return;
		}
		this.shClient = shClient;
	}

	public void setShSzHqClient(TDXClient shSzClient) {
		if (this.shSzClient != null) {
			LOGGER.warn("Unable to set szClient again, it is already set.");
			return;
		}
		this.shSzClient = shSzClient;
	}

	public void addStrategy (Strategy strategy) {
		if (strategy instanceof michael.findata.algoquant.execution.strategy.Strategy) {
			LOGGER.info("Adding strategy {}", strategy);
			Collection<Stock> stocks = ((michael.findata.algoquant.execution.strategy.Strategy) strategy).getTargetSecurities();
			stocks.forEach(stock -> {
				List<Strategy> stras = relevantStrs.getOrDefault(stock, new ArrayList<>());
				stras.add(strategy);
				relevantStrs.put(stock, stras);
			});
			addTargetSecurities(stocks);
			strategies.add(strategy);
		} else {
			LOGGER.warn("Cannot add {}, because it is not a michael.findata.algoquant.execution.strategy.Strategy.", strategy);
		}
	}

	private void updateDividendAndAdjFactorsForAShares () {
		TDXClient c = new TDXClient("218.6.198.155:7709");
		c.connect();
		targetSecurities.forEach(stock -> {
			if (Exchange.SHSE.equals(stock.exchange()) || Exchange.SZSE.equals(stock.exchange())) {
				dividendService.refreshDividendDataForFund(stock.getCode(), c);
			}
			dividendService.calculateAdjFactorForStock(stock.getCode());
		});
		c.disconnect();
	}

	private void scheduleStopTask () {
		// schedule stop 30 minutes after hk market stops
		Timer stopTimer = new Timer(true); // Daemon timer, stops when command center stops
		stopTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				LOGGER.info("Stop timer activated.");
				onStop();
			}
		}, new Date(secondHalfEndHK+3*60*1000)); // 30 min after hk market stops;
	}

	public void start () {
		// Caution: better put this line as the first statement in this method, quite a
		// number of statements depend on it!
		setTargetSecurities();

		updateDividendAndAdjFactorsForAShares();
		scheduleStopTask();

		// Caution: this has to be put after setTargetSecurities!
		MetaBroker broker = new MetaBroker(hkStocks);
		setBroker(broker);
		broker.setDepthListener(this);

		// Caution: this has to be put after setTargetSecurities!
		// Update dividends and then start;
		updateDividendInfo();

		List<Dividend> divs = dividendRepo.findByStock_CodeInOrderByPaymentDate(
				targetSecurities.stream().map(Stock::getCode).collect(Collectors.toList()));
//		strategies.forEach(strategy -> {
//			if (strategy instanceof DividendHandler) {
//				DividendHandler dividendHandler = (DividendHandler) strategy;
//				divs.forEach(dividend -> dividendHandler.onDividend(
//						new DateTime(dividend.getPaymentDate().getTime()),
//						dividend, null, null, null));
//			}
//		});
		divs.forEach(dividend -> relevantStrs.get(dividend.getStock()).forEach(strategy -> {
			if (strategy instanceof DividendHandler) {
				((DividendHandler) strategy).onDividend(new DateTime(dividend.getPaymentDate().getTime()),
						dividend, null, null, null);
			}
		}));

		if (shSzClient != null && shSzStocks.length > 0) {
			shSzThread = new TDXPollThread("SH&SZ Poller", shSzClient, 0, firstHalfStartCN, firstHalfEndCN, secondHalfStartCN, secondHalfEndCN, shSzStocks);
			shSzThread.start();
		}

		// disabled shClient
//		if (shClient != null && shStocks.length > 0) {
//			shThread = new TDXPollThread("Shanghai Poller", shClient, 0, firstHalfStartCN, firstHalfEndCN, secondHalfStartCN, secondHalfEndCN, shStocks);
//			shThread.start();
//		}
		// disabled szClient
//		if (szClient != null && szStocks.length > 0) {
//			szThread = new TDXPollThread("Shenzhen Poller", szClient, 0, firstHalfStartCN, firstHalfEndCN, secondHalfStartCN, secondHalfEndCN, szStocks);
//			szThread.start();
//		}
		// disabled hkThread
//		if (hkStocks.length > 0) {
//			hkThread = new HkStockPollThread("HK Poller", 11000, firstHalfStartHK, firstHalfEndHK, secondHalfStartHK, secondHalfEndHK, hkStocks);
//			hkThread.start();
//		}
	}

	private void updateDividendInfo() {
		TDXClient tdxClient;
		if (shSzClient != null) {
			tdxClient = shSzClient;
		} else if (shClient != null) {
			tdxClient = shClient;
		} else if (szClient != null) {
			tdxClient = szClient;
		} else {
			LOGGER.warn("Error! there is no available TdxClient");
			return;
		}
		tdxClient.connect();
		Collection<Stock> interestingFunds = targetSecurities;
		for (Stock stock : interestingFunds) {
			if (stock.exchange() == Exchange.SHSE || stock.exchange() == Exchange.SZSE) {
				LOGGER.info("Refreshing dividend for stock/fund: "+ stock.getCode()+" "+ stock.getName());
				dividendService.refreshDividendDataForFund(stock.getCode(), tdxClient);
			}
		}
		tdxClient.disconnect();
	}

	public void stop () {
		if (shThread != null) shThread.notifyStop();
		if (szThread != null) szThread.notifyStop();
		if (shSzThread != null) shSzThread.notifyStop();
		if (hkThread != null) hkThread.notifyStop();
	}

	public void setFirstHalfStartCN(LocalTime firstHalfStartCN) {
		this.firstHalfStartCN = firstHalfStartCN.toDateTimeToday().getMillis();
	}

	public void setFirstHalfEndCN(LocalTime firstHalfEndCN) {
		this.firstHalfEndCN = firstHalfEndCN.toDateTimeToday().getMillis();
	}

	public void setSecondHalfStartCN(LocalTime secondHalfStartCN) {
		this.secondHalfStartCN = secondHalfStartCN.toDateTimeToday().getMillis();
	}

	public void setSecondHalfEndCN(LocalTime secondHalfEndCN) {
		this.secondHalfEndCN = secondHalfEndCN.toDateTimeToday().getMillis();
	}

	public void setFirstHalfStartHK(LocalTime firstHalfStartHK) {
		this.firstHalfStartHK = firstHalfStartHK.toDateTimeToday().getMillis();
	}

	public void setFirstHalfEndHK(LocalTime firstHalfEndHK) {
		this.firstHalfEndHK = firstHalfEndHK.toDateTimeToday().getMillis();
	}

	public void setSecondHalfStartHK(LocalTime secondHalfStartHK) {
		this.secondHalfStartHK = secondHalfStartHK.toDateTimeToday().getMillis();
	}

	public void setSecondHalfEndHK(LocalTime secondHalfEndHK) {
		this.secondHalfEndHK = secondHalfEndHK.toDateTimeToday().getMillis();
	}

//	private boolean obtainLock() {
//		// Performance: as tested, during a 2-client session this section takes 0 ms to execute, acceptable.
//		// check and obtain lock
//		// already blocked? return false, meaning don't do anything and skip
//		// not blocked yet? block it and return true,
//		// meaning: 1. lock obtained; 2. do stuff; and 3. don't forget to unblock after completing
//		return locked.compareAndSet(false, true);
//	}
//
//	// This should only be done by a thread that has already obtained the lock by obtainLock() = true;
//	private void releaseLock () {
//		locked.set(false);
//	}

	private synchronized void onStop() {
		if ((shSzThread == null || shSzThread.isStopped()) &&
				(shThread == null || shThread.isStopped()) &&
				(szThread == null || szThread.isStopped()) &&
				(hkThread == null || hkThread.isStopped())) {

			LOGGER.info("Command Center Stopping.");
			strategies.forEach(strategy -> {
				if (strategy instanceof michael.findata.algoquant.execution.strategy.Strategy) {
					((michael.findata.algoquant.execution.strategy.Strategy) strategy).onStop();
				}
			});
			if (broker instanceof michael.findata.algoquant.execution.component.broker.Broker) {
				((michael.findata.algoquant.execution.component.broker.Broker) broker).stop();
			}
			DBUtil.tryToStopDB();
			LOGGER.info("Command Center Stopped.");
		} else {
			LOGGER.warn("Some depth thread has stopped. Command center cannot stop.");
		}
	}

	public void depthUpdated (Depth depth) {
		// no need to use lock any more, we use disruptor now
//		if (obtainLock()) {
		Product product = depth.product();
		if (product instanceof Stock) {
			DateTime now = new DateTime();
			relevantStrs.get(product).forEach(strategy -> {
				if (strategy instanceof DepthHandler) {
					((DepthHandler) strategy).onDepthUpdate(now, depth, null, null, broker);
				}
			});
		}
//			releaseLock();
//		} else {
//			LOGGER.warn("Depth update messaging failure due to locking [1]. {}", depth);
//		}
	}

	private void depthsUpdated(Map<Product, Depth> depths) {
		// MarketCondition handlers
		// Note: no point to use relevantStrs. just blindly invoke every MarketCondition handler on this
		// Construct market condition and pass it to MarketConditionHandler
		MarketCondition condition = new SimpleMarketCondition(depths);
		DateTime now = new DateTime();
		strategies.forEach(strategy -> {
//				DateTime now = new DateTime();
			if (strategy instanceof MarketConditionHandler) {
				MarketConditionHandler mch = (MarketConditionHandler) strategy;
				mch.onMarketConditionUpdate(now, condition, null, broker);
			}
		});

		Product product;
		// Depth handlers
		for (Map.Entry<Product, Depth> entry : depths.entrySet()) {
			product = entry.getKey();
			if (product instanceof Stock) {
				relevantStrs.get(product).forEach(strategy -> {
					if (strategy instanceof DepthHandler) {
						((DepthHandler) strategy).onDepthUpdate(now, entry.getValue(), condition, null, broker);
					}
				});
			}
		}
	}

	private abstract class PollThread extends Thread {
		private final Logger threadLOGGER = getClassLogger();
		protected String name;
		protected String [] codes;
		HashMap<String, Stock> codeProductMap;
		private boolean stopped = true;
		private boolean stopSignal;
		private long heartbeatInterval;
		private long firstHalfStart;
		private long firstHalfEnd;
		private long secondHalfStart;
		private long secondHalfEnd;

		PollThread (String name, long heartbeatInterval, long firstHalfStart, long firstHalfEnd, long secondHalfStart, long secondHalfEnd, Stock ... stocks) {
			this.name = name;
			this.heartbeatInterval = heartbeatInterval;
			this.firstHalfStart = firstHalfStart;
			this.firstHalfEnd = firstHalfEnd;
			this.secondHalfStart = secondHalfStart;
			this.secondHalfEnd = secondHalfEnd;
			this.stopSignal = false;
			codes = new String [stocks.length];
			codeProductMap = new HashMap<>();
			for (int i = 0; i < stocks.length; i++) {
				codeProductMap.put(stocks[i].getCode(), stocks[i]);
				codes[i] = stocks[i].getCode();
			}
		}

		void notifyStop() {
			stopSignal = true;
		}

		boolean isStopped() {
			return stopped;
		}

		abstract void beforeStopOrPause ();

		abstract Map<Product, Depth> poll ();

		@Override
		public void run() {
			Map<Product, Depth> pollResult;
			stopped = false;
			long now;
			long lastPrint = System.currentTimeMillis();
			while (true) {
				threadLOGGER.debug("{} heartbeat ... ", name);
				if (stopSignal) {
					threadLOGGER.info("{} stop signal received, stopping ...", name);
					break;
				}
				now = System.currentTimeMillis();
				if (now < firstHalfStart || // before daily session
						(firstHalfEnd < now && now < secondHalfStart)) {// lunch time break
					beforeStopOrPause();
					if (now - lastPrint > 300000) {
						threadLOGGER.debug("{}: {} daily session not started or during lunch break, do nothing ...",LocalDateTime.now(), name);
						lastPrint = now;
					}
					try {
						threadLOGGER.info("{} heartbeat completed, sleeping ...", name);
						Thread.sleep(13000); // the gap between every poll() is 1.3 second
					} catch (InterruptedException e) {
						threadLOGGER.debug("{} interrupted.", name);
					}
				} else if (secondHalfEnd < now ) { // after daily session
					threadLOGGER.info("{} daily session ended, stopping ...", name);
					break;
				} else {
					threadLOGGER.debug("{} active, doing stuff ...\n", name);
					pollResult = poll();
					if (pollResult.isEmpty()) {
						threadLOGGER.debug("{}: empty result.", name);
					} else {
						threadLOGGER.debug("{}: non-empty result.", name);

						// No need to obtain lock any more, we used disruptors now.
						depthsUpdated(pollResult);

////						System.out.println(name+" try obtaining lock. @\t"+System.currentTimeMillis());
//						if (obtainLock()) {
//							LOGGER.debug("{}: lock obtained.", name);
////							System.out.println(name+" locked obtained. @\t"+System.currentTimeMillis());
//
//							// Multi-threaded version
//							// do it with a new thread
////							Thread t = new Thread(() -> {
////								depthsUpdated(pollResult);
////							});t.start();
//
//							// Single-threaded version
//							// do it within the same thread
//							depthsUpdated(pollResult);
//							releaseLock();
//							LOGGER.debug("{}: lock released.", name);
//						} else {
//							// System.out.println("synchronized code section executed in(ms) "+(System.currentTimeMillis() - start));
//							// if this command center is blocked by broker when executing an order, there is not point notify about the change
//							// already blocked, do nothing
//							LOGGER.warn("{} Depths update messaging failure due to locking [2].", name);
//						}
					}
					threadLOGGER.debug("{} heartbeat completed.", name);
					if (heartbeatInterval > 0) {
						try {
							threadLOGGER.info("{} sleeping ...", name);
							Thread.sleep(heartbeatInterval);
						} catch (InterruptedException e) {
							threadLOGGER.warn("{} interrupted.", name);
						}
					}
				}
			}
			beforeStopOrPause();
			if (broker != null && broker instanceof LocalHexinBrokerProxy) {
				((LocalHexinBrokerProxy) broker).stop();
			}
			stopped = true;
		}
	}

	private class TDXPollThread extends PollThread {
		private final Logger threadLOGGER = getClassLogger();

		private TDXClient client;

//		private String name;
//		private boolean stopSignal;
//		private long heartbeatInterval;
//		private String [] codes;
//		private HashMap<String, Stock> codeProductMap;
//		private boolean stopped = true;

		TDXPollThread (String name, TDXClient client, long heartbeatInterval, long firstHalfStart, long firstHalfEnd, long secondHalfStart, long secondHalfEnd, Stock ... stocks) {
			super(name, heartbeatInterval, firstHalfStart, firstHalfEnd, secondHalfStart, secondHalfEnd, stocks);
			this.client = client;
		}

		void beforeStopOrPause() {
			if (client.isConnected()) {
				client.disconnect();
			}
		}

		Map<Product, Depth> poll () {
			if (!client.isConnected()) {
				threadLOGGER.debug("{} connecting ...", name);
				client.connect();
			}
			return client.pollQuotes(32L, codeProductMap, codes);
		}
	}

	private class HkStockPollThread extends PollThread {
		private final Logger threadLOGGER = getClassLogger();

		private HkStockPollThread(String name, long heartbeatInterval, long firstHalfStart, long firstHalfEnd, long secondHalfStart, long secondHalfEnd, Stock... stocks) {
			super(name, heartbeatInterval, firstHalfStart, firstHalfEnd, secondHalfStart, secondHalfEnd, stocks);
		}

		void beforeStopOrPause() {
		}

		Map<Product, Depth> poll () {
			threadLOGGER.debug("{} polling ...", name);
			return new SinaHkInstantSnapshot(codes).depths();
		}
	}
}