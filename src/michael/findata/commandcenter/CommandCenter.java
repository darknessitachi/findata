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
import michael.findata.algoquant.execution.strategy.handler.DividendHandler;
import michael.findata.algoquant.execution.strategy.handler.MarketConditionHandler;
import michael.findata.external.tdx.TDXClient;
import michael.findata.model.Dividend;
import michael.findata.model.Stock;
import michael.findata.service.DividendService;
import michael.findata.spring.data.repository.DividendRepository;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

import static com.numericalmethod.nmutils.NMUtils.getClassLogger;

// This is a virtual machine act like a robot that trades based on plug&play trading strategies
public class CommandCenter {

	private static final Logger LOGGER = getClassLogger();

	@Autowired
	private DividendService dividendService;
	@Autowired
	private DividendRepository dividendRepo;

	private Set<Stock> targetSecurities = null;
	private TDXClient shSzClient = null;
	private TDXClient szClient = null;
	private TDXClient shClient = null;
	private Broker broker;
	private List<Strategy> strategies;
	private Stock [] shSzStocks;
	private Stock [] shStocks;
	private Stock [] szStocks;
	private TDXPollThread shSzThread = null;
	private TDXPollThread shThread = null;
	private TDXPollThread szThread = null;
//	private boolean shSzStopped = true;
//	private boolean shStopped = true;
//	private boolean szStopped = true;
	private long firstHalfStart;
	private long firstHalfEnd;
	private long secondHalfStart;
	private long secondHalfEnd;
	private boolean locked = false;

	public CommandCenter () {
		strategies = new ArrayList<>();
		setFirstHalfStart(new LocalTime(9, 29, 50));
		setFirstHalfEnd(new LocalTime(11, 30, 10));
		setSecondHalfStart(new LocalTime(12, 59, 50));
		setSecondHalfEnd(new LocalTime(15, 0, 10));
	}

	// TODO: 9/16/2016 migrate to addTargetSecurities
	public void setTargetSecurities(Collection<Stock> targetSecurities) {
		if (this.targetSecurities != null) {
			LOGGER.warn("Unable to set targetSecurities again, it is already set.");
			return;
		}
		this.targetSecurities = new HashSet<>();
		this.targetSecurities.addAll(targetSecurities);

		List<Stock> shStocks = new ArrayList<>();
		List<Stock> szStocks = new ArrayList<>();

		targetSecurities.forEach(stock -> {
			if (stock.exchange().equals(Exchange.SHSE)) {
				shStocks.add(stock);
			} else if (stock.exchange().equals(Exchange.SZSE)) {
				szStocks.add(stock);
			}
		});
		this.shStocks = shStocks.toArray(new Stock [shStocks.size()]);
		this.szStocks = szStocks.toArray(new Stock [szStocks.size()]);
		this.shSzStocks = targetSecurities.toArray(new Stock[targetSecurities.size()]);
	}

	public void setBroker(Broker broker) {
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
		LOGGER.info("Adding strategy {}", strategy);
		strategies.add(strategy);
	}

	public void start () {
		// Update dividends and then start;
		updateDividendInfo();

		List<Dividend> divs = dividendRepo.findByStock_CodeInOrderByPaymentDate(
				targetSecurities.stream().map(Stock::getCode).collect(Collectors.toList()));
		strategies.forEach(strategy -> {
			if (strategy instanceof DividendHandler) {
				DividendHandler dividendHandler = (DividendHandler) strategy;
				divs.forEach(dividend -> dividendHandler.onDividend(
						new DateTime(dividend.getPaymentDate().getTime()),
						dividend, null, null, null));
			}
		});
		if (shClient != null) {
			shThread = new TDXPollThread("Shanghai Poller", shClient, 4500, shStocks);
			shThread.start();
		}
		if (szClient != null) {
			szThread = new TDXPollThread("Shenzhen Poller", szClient, 2500, szStocks);
			szThread.start();
		}
		if (shSzClient != null) {
			shSzThread = new TDXPollThread("Poller", shSzClient, 5000, shSzStocks);
			shSzThread.start();
		}
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
			LOGGER.info("Refreshing dividend for stock/fund: "+ stock.getCode()+" "+ stock.getName());
			dividendService.refreshDividendDataForFund(stock.getCode(), tdxClient);
		}
		tdxClient.disconnect();
	}

	public void stop () {
		if (shThread != null) shThread.notifyStop();
		if (szThread != null) szThread.notifyStop();
		if (shSzThread != null) shSzThread.notifyStop();
	}

	public void setFirstHalfStart(LocalTime firstHalfStart) {
		this.firstHalfStart = firstHalfStart.toDateTimeToday().getMillis();
	}

	public void setFirstHalfEnd(LocalTime firstHalfEnd) {
		this.firstHalfEnd = firstHalfEnd.toDateTimeToday().getMillis();
	}

	public void setSecondHalfStart(LocalTime secondHalfStart) {
		this.secondHalfStart = secondHalfStart.toDateTimeToday().getMillis();
	}

	public void setSecondHalfEnd(LocalTime secondHalfEnd) {
		this.secondHalfEnd = secondHalfEnd.toDateTimeToday().getMillis();
	}

	private boolean obtainLock() {
		// Performance: as tested, during a 2-client session this section takes 0 ms to execute, acceptable.
		// check and obtain lock
		if (locked) { // already blocked? return false, meaning don't do anything and skip
			return false;
		} else {
			// not blocked yet? block it and return true,
			// meaning: 1. lock obtained; 2. do stuff; and 3. don't for get to unblock after completing
			locked = true;
			return true;
		}
	}

	private synchronized void onStop () {
		if ((shSzThread == null || shSzThread.isStopped()) &&
				(shThread == null || shThread.isStopped()) &&
				(szThread == null || szThread.isStopped())) {
			LOGGER.info("Command Center Stopped");
			strategies.forEach(strategy -> {
				if (strategy instanceof michael.findata.algoquant.execution.strategy.Strategy) {
					((michael.findata.algoquant.execution.strategy.Strategy) strategy).onStop();
				}
			});
		}
	}

	private void depthsUpdated(Map<Product, Depth> depths) {
		// start a new thread to do update
		Thread t = new Thread(() -> {
			// Construct market condition and pass it to MarketConditionHandler
			MarketCondition condition = new SimpleMarketCondition(depths);

			DateTime now = new DateTime();
			strategies.forEach(strategy -> {
//				DateTime now = new DateTime();
				if (strategy instanceof MarketConditionHandler) {
					MarketConditionHandler mch = (MarketConditionHandler) strategy;
					mch.onMarketConditionUpdate(now, condition, null, broker);
				}
				if (strategy instanceof DepthHandler) {
					DepthHandler dh = (DepthHandler) strategy;
					for (Depth dpt : depths.values()) {
						dh.onDepthUpdate(now, dpt, condition, null, broker);
					}
				}
			});
			locked = false;
			System.out.println(now+" lock released.");
		});
		// t.start() // do it with a new thread
		t.run(); // do it within the same thread
	}

	private class TDXPollThread extends Thread {
		private final Logger threadLOGGER = getClassLogger();
		private String name;
		private boolean stopSignal;
		private TDXClient client;
		private long heartbeatInterval;
		private String [] codes;
		private HashMap<String, Stock> codeProductMap;
		private boolean stopped = true;

		private void notifyStop () {
			stopSignal = true;
		}

		private TDXPollThread (String name, TDXClient client, long heartbeatInterval, Stock ... stocks) {
			this.name = name;
			this.client = client;
			this.heartbeatInterval = heartbeatInterval;
			this.stopSignal = false;
			codes = new String [stocks.length];
			codeProductMap = new HashMap<>();
			for (int i = 0; i < stocks.length; i++) {
				codeProductMap.put(stocks[i].getCode(), stocks[i]);
				codes[i] = stocks[i].getCode();
			}
		}

		@Override
		public void run() {
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
					if (client.isConnected()) {
						client.disconnect();
					}
					if (now - lastPrint > 300000) {
						threadLOGGER.debug("{}: {} daily session not started or during lunch break, do nothing ...",LocalDateTime.now(), name);
						lastPrint = now;
					}
					if (heartbeatInterval > 0) {
						try {
							threadLOGGER.debug("{} heartbeat completed, sleeping ...", name);
							Thread.sleep(heartbeatInterval);
						} catch (InterruptedException e) {
							threadLOGGER.debug("{} Interrupt: ", name);
						}
					}
				} else if (secondHalfEnd < now ) { // after daily session
					threadLOGGER.info("{} daily session ended, stopping ...", name);
					break;
				} else {
					if (!client.isConnected()) {
						threadLOGGER.debug("{} connecting ...", name);
						client.connect();
					}
					System.out.printf("%s active, doing stuff ...\n", name);
					poll();
				}
			}
			if (client.isConnected()) {
				client.disconnect();
			}
			if (broker != null && broker instanceof LocalHexinBrokerProxy) {
				((LocalHexinBrokerProxy) broker).stop();
			}
			stopped = true;
			onStop();
		}

		private void poll () {
			Map<Product, Depth> result = client.pollQuotes(100, 20L, codeProductMap, codes);
			if (result.isEmpty()) {
				threadLOGGER.info("{}: empty result.", name);
				return;
			}
//			System.out.println(name+" try obtaining lock. @\t"+System.currentTimeMillis());
			if (!obtainLock()) {
				// System.out.println("synchronized code section executed in(ms) "+(System.currentTimeMillis() - start));
				// if this command center is blocked by broker when executing an order, there is not point notify about the change
				// already blocked, do nothing
//				System.out.println(name+" no lock available. @\t"+System.currentTimeMillis());
				return;
			}
//			System.out.println(name+" locked obtained. @\t"+System.currentTimeMillis());
			depthsUpdated(result);
		}

		public boolean isStopped() {
			return stopped;
		}
	}
}