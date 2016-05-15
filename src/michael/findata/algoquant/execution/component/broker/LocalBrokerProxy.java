package michael.findata.algoquant.execution.component.broker;

import com.numericalmethod.algoquant.execution.component.broker.Broker;
import com.numericalmethod.algoquant.execution.datatype.order.BasicOrderDescription;
import com.numericalmethod.algoquant.execution.datatype.order.Order;
import com.typesafe.config.ConfigException;
import michael.findata.algoquant.execution.datatype.order.HexinOrder;
import michael.findata.model.Stock;
import net.sf.ehcache.search.expression.Or;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.SQLSyntaxErrorException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

// This is a proxy that connects to localhost .net program via tcp/ip
// The .net program then uses windows automation to drive broker accounts
public class LocalBrokerProxy implements Broker {

	int portNormalBuySell;
	int portPairOpen;
	int portPairClose;
	int portMonitor;
	private final DecimalFormat chinaStockPriceFormat = new DecimalFormat("#.###");
	private final DecimalFormat chinaStockQuantityFormat = new DecimalFormat("#");
	private final HashMap<Long, String> results = new HashMap<>();

	public LocalBrokerProxy (int portNormalBuySell, int portPairOpen, int portPairClose, int portMonitor) {
		this.portNormalBuySell = portNormalBuySell;
		this.portPairOpen = portPairOpen;
		this.portPairClose = portPairClose;
		this.portMonitor = portMonitor;
		if (portNormalBuySell != -1) { // test
			if (queryUiInfo(portNormalBuySell).contains("NormalBuySell")){
				System.out.println("NormalBuySell port test passed!");
			} else {
				System.out.println("NormalBuySell port test failed!!!");
			}
		}

		ArrayList<HexinOrder> orderList = new ArrayList<>();
		Stock s600000 = new Stock("600000");
		Stock s600031 = new Stock("600031");

		HexinOrder openShort = new HexinOrder(s600000, 100, 100, HexinOrder.HexinType.PAIR_OPEN_SHORT);
		HexinOrder openLong = new HexinOrder(s600031, 100, 1.85, HexinOrder.HexinType.PAIR_OPEN_LONG);
		HexinOrder closeShortBuyBack = new HexinOrder(s600000, 100, 1.55, HexinOrder.HexinType.PAIR_CLOSE_SHORT_BUY_BACK);
		HexinOrder closeLongSellBack = new HexinOrder(s600031, 100, 666, HexinOrder.HexinType.PAIR_CLOSE_LONG_SELL_BACK);

		boolean testPassed = true;
		if (portPairClose != -1) { // test
			if (queryUiInfo(portPairClose).contains("PairClose")){
				System.out.println("PairClose port test passed!");
			} else {
				System.out.println("PairClose port test failed!!!");
				testPassed = false;
			}
			orderList.clear();
			orderList.add(closeLongSellBack);
			orderList.add(closeShortBuyBack);
			sendOrder(orderList);
			if (closeLongSellBack.getAck().contains("证券可用数量不足")) {
				System.out.println("CloseLongSellBack test passed!");
			} else {
				System.out.println("CloseLongSellBack test failed!!!");
				System.out.println(closeLongSellBack.getAck());
				testPassed = false;
			}
			if (closeShortBuyBack.getAck().contains("买券还券数量不可超过融券负债可还数量加买入单位")) {
				System.out.println("CloseShortBuyBack test passed!");
			} else {
				System.out.println("CloseShortBuyBack test failed!!!");
				System.out.println(closeShortBuyBack.getAck());
				testPassed = false;
			}
		}
		if (portPairOpen != -1) { // test
			if (queryUiInfo(portPairOpen).contains("PairOpen")){
				System.out.println("PairOpen port test passed!");
			} else {
				System.out.println("PairOpen port test failed!!!");
				testPassed = false;
			}
			orderList.clear();
			orderList.add(openShort);
			orderList.add(openLong);
			sendOrder(orderList);
			if (openShort.getAck().contains("证券可用数量不足")) {
				System.out.println("OpenShort test passed!");
			} else {
				System.out.println("OpenShort test failed!!!");
				System.out.println(openShort.getAck());
				testPassed = false;
			}
			if (openLong.getAck().contains("可用资金不足")) {
				System.out.println("OpenLong test passed!");
			} else {
				System.out.println("OpenLong test failed!!!");
				System.out.println(openLong.getAck());
				testPassed = false;
			}
		}
		if (portMonitor != -1) { // test
			if (queryUiInfo(portPairClose).contains("Monitor")){
				System.out.println("Monitor port test passed!");
			} else {
				System.out.println("Monitor port test failed!!!");
				testPassed = false;
			}
		}
		if (!testPassed) {
			System.out.println("LocalBrokerProxy or the .Net broker  ");
			System.exit(-1);
		}
	}

	public LocalBrokerProxy (int portPairOpen, int portPairClose, int portMonitor) {
		this(-1, portPairOpen, portPairClose, portMonitor);
	}

	private static String queryUiInfo (int port) {
		Socket s;
		String result = null;
		try {
			s = new Socket(InetAddress.getByName("127.0.0.1"), port);
			OutputStream ops = s.getOutputStream();
			ops.write("QueryUiInfo".getBytes());
			ops.flush();
			result = new BufferedReader(new InputStreamReader(s.getInputStream())).readLine();
			ops.close();
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	private void sendOrder(ArrayList<? extends Order> orders, int port) {
		Order [] oArray = orders.toArray(new Order[orders.size()]);
		Socket s;
		try {
			s = new Socket(InetAddress.getByName("127.0.0.1"), port);
			OutputStream ops = s.getOutputStream();

			StringBuilder sb = new StringBuilder();
			for (Order o : oArray) {
				switch (o.side()) {
					case SELL:
						sb.append("Sell");
						break;
					case BUY:
						sb.append("Buy");
				}
				sb.append('|').append(o.product() instanceof Stock ? ((Stock) o.product()).getCode() : o.product().symbol());
				sb.append('|').append(chinaStockPriceFormat.format(o.price()));
				sb.append('|').append(chinaStockQuantityFormat.format(o.quantity())).append('\n');
			}
			ops.write(sb.toString().getBytes());
			ops.flush();
			BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			String line;
			int count = 0;
			while (null != (line = br.readLine())) {
				if (line.length() > 2) {
//					System.out.println(line);
					results.put(oArray[count].id(), line);
					if (oArray[count] instanceof HexinOrder) {
						((HexinOrder) oArray[count]).setAck(line);
					}
					count++;
				}
			}
			ops.close();
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sendOrder(Collection<? extends Order> orders) {
		ArrayList<Order> normalOrders = new ArrayList<>();
		ArrayList<Order> pairOpenOrders = new ArrayList<>();
		ArrayList<Order> pairCloseOrders = new ArrayList<>();
		for (Order o : orders) {
			if (o instanceof HexinOrder) {
				switch (((HexinOrder) o).hexinType()) {
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
		if (!pairOpenOrders.isEmpty()) {
			sendOrder(pairOpenOrders, portPairOpen);
		}
		if (!pairCloseOrders.isEmpty()) {
			sendOrder(pairCloseOrders, portPairClose);
		}
		if (!normalOrders.isEmpty()) {
			sendOrder(normalOrders, portNormalBuySell);
		}
	}

	@Override
	public void cancelOrder(Collection<? extends Order> orders) {
		// todo
	}

	public static void main (String [] args) throws InterruptedException {
		long start = System.currentTimeMillis();
		LocalBrokerProxy broker = new LocalBrokerProxy(-1, 19021, 19019, -1);
//		Stock s600016 = new Stock("600016");
//		Stock s000568 = new Stock("000568");
		System.out.println(System.currentTimeMillis()-start);
	}
}