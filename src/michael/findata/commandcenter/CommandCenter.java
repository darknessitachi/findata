package michael.findata.commandcenter;

import com.numericalmethod.algoquant.execution.component.broker.Broker;
import com.numericalmethod.algoquant.execution.datatype.product.stock.Exchange;
import com.numericalmethod.algoquant.execution.strategy.Strategy;
import com.numericalmethod.algoquant.execution.strategy.handler.MarketConditionHandler;
import michael.findata.external.netease.NeteaseInstantSnapshot;
import michael.findata.external.tdx.TDXClient;
import michael.findata.model.Stock;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// This is a virtual machine act like a robot that trades
public class CommandCenter {

	private Set<Stock> targetSecurities = null;
	private TDXClient szClient = null;
	private TDXClient shClient = null;
	private Broker broker;
	private List<Strategy> strategies;
	private String [] shCodes;
	private String [] szCodes;
	private TDXPollThread shThread;
	private TDXPollThread szThread;
	private long firstHalfStart;
	private long firstHalfEnd;
	private long secondHalfStart;
	private long secondHalfEnd;

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

		List<String> shCodes = new ArrayList<>();
		List<String> szCodes = new ArrayList<>();

		targetSecurities.forEach(stock -> {
			if (stock.exchange().equals(Exchange.SHSE)) {
				shCodes.add(stock.getCode());
			} else if (stock.exchange().equals(Exchange.SZSE)) {
				szCodes.add(stock.getCode());
			}
		});
		this.shCodes = shCodes.toArray(new String [shCodes.size()]);
		this.szCodes = szCodes.toArray(new String [szCodes.size()]);
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

	public void addStrategy (Strategy strategy) {
		strategies.add(strategy);
	}

	public void start () {
		shThread = new TDXPollThread("Shanghai Poller", shClient, 4500, shCodes);
		szThread = new TDXPollThread("Shenzhen Poller", szClient, 2500, szCodes);
		shThread.start();
		szThread.start();
	}

	public void stop () {
		shThread.notifyStop();
		szThread.notifyStop();
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

	private class TDXPollThread extends Thread {

		private String name;
		private boolean stopSignal;
		private TDXClient client;
		private long heartbeatInterval;
		private String [] codes;

		private void notifyStop () {
			stopSignal = true;
		}

		private TDXPollThread (String name, TDXClient client, long heartbeatInterval, String ... codes) {
			this.name = name;
			this.client = client;
			this.heartbeatInterval = heartbeatInterval;
			this.stopSignal = false;
			this.codes = codes;
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
					return;
				}
				now = System.currentTimeMillis();
				if (now < firstHalfStart || // before daily session
					(firstHalfEnd < now && now < secondHalfStart)) {// lunch time break
					if (client.isConnected()) {
						client.disconnect();
					}
					System.out.println(name+" daily session not started or during lunch break, do nothing ...");
				} else if (secondHalfEnd < now ) { // after daily session
					if (client.isConnected()) {
						client.disconnect();
					}
					System.out.println(name+" daily session ended, stopping ...");
					return;
				} else {
					if (!client.isConnected()) {
						System.out.println(name+" connecting ...");
						client.connect();
					}
					System.out.println(name+" active, doing stuff ...");
					poll();
				}
				try {
//					System.out.println(name+" heartbeat completed, sleeping ...");
					Thread.sleep(heartbeatInterval);
				} catch (InterruptedException e) {
					System.out.println(name+" Interrupt: ");
				}
			}
		}

		private void poll () {
			client.pollQuotes(100, codes);
			// todo if there are depth changes, pass those changes to strategies
			strategies.forEach(strategy -> {
				DateTime now = new DateTime();
				if (strategy instanceof MarketConditionHandler) {
					MarketConditionHandler mch = (MarketConditionHandler) strategy;
					mch.onMarketConditionUpdate(now, new NeteaseInstantSnapshot(), null, broker); // todo, need to use real Broker
				}
			});
		}
	}
}