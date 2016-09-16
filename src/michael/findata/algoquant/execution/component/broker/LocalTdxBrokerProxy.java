package michael.findata.algoquant.execution.component.broker;

import com.numericalmethod.algoquant.execution.datatype.order.Order;
import michael.findata.algoquant.execution.datatype.order.HexinOrder;
import michael.findata.model.Stock;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;

import static com.numericalmethod.nmutils.NMUtils.getClassLogger;
import static michael.findata.algoquant.execution.datatype.order.HexinOrder.HexinType.SIMPLE_BUY;
import static michael.findata.algoquant.execution.datatype.order.HexinOrder.HexinType.SIMPLE_SELL;

// This is a proxy that connects to localhost .net program via tcp/ip
// The .net program then uses windows automation to drive broker accounts
public class LocalTdxBrokerProxy extends LocalBrokerProxy {

	private static final Logger LOGGER = getClassLogger();
	private int port;
	public LocalTdxBrokerProxy (int port) {
		this.port = port;

		// Pre-test the UIs
		test();
	}

	// This boolean lock mechanism only supports two contending parties
	private volatile boolean lock = false; // false - not held; true - held by a thread
	public void waitLock () {
		LOGGER.info("Waiting lock.");
		while (lock) {
			// wait until lock is released
		}
		lock = true;
		LOGGER.info("Lock obtained.");
	}

	public void releaseLock () {
		LOGGER.info("Lock released.");
		lock = false;
	}

	@Override
	public void sendOrder(Collection<? extends Order> orders) {
		waitLock();
		ArrayList<Order> normalOrders = new ArrayList<>();
		ArrayList<Order> creditOrders = new ArrayList<>();
		ArrayList<Order> pairOpenOrders = new ArrayList<>();
		ArrayList<Order> pairCloseOrders = new ArrayList<>();
		for (Order o : orders) {
			if (o instanceof HexinOrder) {
				switch (((HexinOrder) o).hexinType()) {
					case CREDIT_SELL:
					case CREDIT_BUY:
						creditOrders.add(o);
						break;
					case PAIR_OPEN_SHORT:
					case PAIR_OPEN_LONG:
						// Pair Open
						pairOpenOrders.add(o);
						break;
					case PAIR_CLOSE_SHORT_BUY_BACK:
					case PAIR_CLOSE_LONG_SELL_BACK:
						// Pair Close
						pairCloseOrders.add(o);
						break;
					default:
						normalOrders.add(o);
				}
			} else { // Normal Buy/Sell
				normalOrders.add(o);
			}
		}
		if (!normalOrders.isEmpty()) {
			orderOutToDotNetComponent(normalOrders, port);
		}
		if (!creditOrders.isEmpty()) {
			orderOutToDotNetComponent(creditOrders, port);
		}
		if (!pairOpenOrders.isEmpty()) {
			orderOutToDotNetComponent(pairOpenOrders, port);
		}
		if (!pairCloseOrders.isEmpty()) {
			orderOutToDotNetComponent(pairCloseOrders, port);
		}
		releaseLock();
	}

	@Override
	public void cancelOrder(Collection<? extends Order> orders) {
		waitLock();
		cancelOrderToDotNetComponent(orders, port);
		releaseLock();
	}

	public boolean test (){
		long start = System.currentTimeMillis();
		ArrayList<HexinOrder> orderList = new ArrayList<>();
		Stock s000338 = new Stock("000338");
		Stock s600519 = new Stock("600519");
		boolean testPassed = true;

		HexinOrder normalSell = new HexinOrder(s000338, 100, 666, SIMPLE_SELL);
		HexinOrder normalBuy = new HexinOrder(s600519, 100, 1.55, SIMPLE_BUY);
		orderList.clear();
		orderList.add(normalSell);
		orderList.add(normalBuy);
		sendOrder(orderList);

		if (normalSell.submitted()
				|| normalSell.getAck().contains("证券可用数量不足")
				|| normalSell.getAck().contains("当前时间不允许委托")) {
			System.out.println("normalSell test passed!");
		} else {
			System.out.println("normalSell test failed!!!");
			System.out.println(normalSell.getAck());
			testPassed = false;
		}

		if (normalBuy.submitted()
				|| normalBuy.getAck().contains("当前时间不允许委托")
				|| normalBuy.getAck().contains("可用资金不足")) {
			System.out.println("normalBuy test passed!");
		} else {
			System.out.println("normalBuy test failed!!!");
			System.out.println(normalBuy.getAck());
			testPassed = false;
		}

		if (!normalBuy.submitted()) {
			orderList.remove(normalBuy);
		}
		if (!normalSell.submitted()) {
			orderList.remove(normalSell);
		}
		cancelOrder(orderList);
		System.out.println("Test time taken (1 sell + 1 buy + possible cancels): "+(System.currentTimeMillis()-start)+" ms.");
//		if (!testPassed) {
//			System.out.println("LocalHexinBrokerProxy or the .Net broker is not setup properly. Terminating...");
//		}
		return testPassed;
	}
}