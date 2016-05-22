package michael.findata.commandcenter;

import com.numericalmethod.algoquant.execution.component.broker.Broker;
import com.numericalmethod.algoquant.execution.datatype.depth.Depth;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.SimpleMarketCondition;
import com.numericalmethod.algoquant.execution.datatype.product.Product;
import com.numericalmethod.algoquant.execution.datatype.product.stock.Exchange;
import com.numericalmethod.algoquant.execution.strategy.Strategy;
import com.numericalmethod.algoquant.execution.strategy.handler.MarketConditionHandler;
import michael.findata.algoquant.execution.component.broker.LocalBrokerProxy;
import michael.findata.external.tdx.TDXClient;
import michael.findata.model.Stock;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.util.*;

// This is a virtual machine act like a robot that trades
public class CommandCenter {

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

	public void setTargetSecurities(Set<Stock> targetSecurities) {
		if (this.targetSecurities != null) {
			System.out.println("Unable to set targetSecurities again, it is already set.");
			return;
		}
		this.targetSecurities = targetSecurities;

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
			System.out.println("Unable to set szClient again, it is already set.");
			return;
		}
		this.szClient = szClient;
	}

	public void setShClient(TDXClient shClient) {
		if (this.shClient != null) {
			System.out.println("Unable to set shClient again, it is already set.");
			return;
		}
		this.shClient = shClient;
	}

	public void setShSzClient(TDXClient shSzClient) {
		if (this.shSzClient != null) {
			System.out.println("Unable to set szClient again, it is already set.");
			return;
		}
		this.shSzClient = shSzClient;
	}

	public void addStrategy (Strategy strategy) {
		strategies.add(strategy);
	}

	public void start () {
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

	private void depthsUpdated(Map<Product, Depth> depths) {
		// start a new thread to do update
		Thread t = new Thread(() -> {
			// Construct market condition and pass it to MarketConditionHandler
			MarketCondition condition = new SimpleMarketCondition(depths);

			strategies.forEach(strategy -> {
				DateTime now = new DateTime();
				if (strategy instanceof MarketConditionHandler) {
					MarketConditionHandler mch = (MarketConditionHandler) strategy;
					mch.onMarketConditionUpdate(now, condition, null, broker);
				}
			});
			locked = false;
			System.out.println("lock released.");
		});
		// t.start() // do it with a new thread
		t.run(); // do it within the same thread
	}

	private class TDXPollThread extends Thread {

		private String name;
		private boolean stopSignal;
		private TDXClient client;
		private long heartbeatInterval;
		private String [] codes;
		private HashMap<String, Stock> codeProductMap;

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
			long now;
			while (true) {
//				System.out.println(name+" heartbeat ... ");
				if (stopSignal) {
					if (client.isConnected()) {
						client.disconnect();
					}
					System.out.println(name+" stop signal received, stopping ...");
					if (broker != null && broker instanceof LocalBrokerProxy) {
						((LocalBrokerProxy) broker).stop();
					}
					return;
				}
				now = System.currentTimeMillis();
				if (now < firstHalfStart || // before daily session
					(firstHalfEnd < now && now < secondHalfStart)) {// lunch time break
					if (client.isConnected()) {
						client.disconnect();
					}
//					System.out.println(name+" daily session not started or during lunch break, do nothing ...");
					if (heartbeatInterval > 0) {
						try {
//							System.out.println(name+" heartbeat completed, sleeping ...");
							Thread.sleep(heartbeatInterval);
						} catch (InterruptedException e) {
							System.out.println(name+" Interrupt: ");
						}
					}
				} else if (secondHalfEnd < now ) { // after daily session
					if (client.isConnected()) {
						client.disconnect();
					}
					System.out.println(name+" daily session ended, stopping ...");
					if (broker != null && broker instanceof LocalBrokerProxy) {
						((LocalBrokerProxy) broker).stop();
					}
					return;
				} else {
					if (!client.isConnected()) {
						System.out.println(name+" connecting ...");
						client.connect();
					}
//					System.out.println(name+" active, doing stuff ...");
					poll();
				}
			}
		}

		private void poll () {
			Map<Product, Depth> result = client.pollQuotes(100, 20L, codeProductMap, codes);
			if (result.isEmpty()) {
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
	}
}