package michael.findata.algoquant.execution.component.broker;

import com.numericalmethod.algoquant.execution.component.broker.Broker;
import com.numericalmethod.algoquant.execution.datatype.order.BasicOrderDescription;
import com.numericalmethod.algoquant.execution.datatype.order.Order;
import com.typesafe.config.ConfigException;
import michael.findata.algoquant.execution.datatype.order.HexinOrder;
import michael.findata.model.Stock;
import net.sf.ehcache.search.expression.Or;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.SQLSyntaxErrorException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

// This is a proxy that connects to localhost .net program via tcp/ip
// The .net program then uses windows automation to drive broker accounts
public class LocalBrokerProxy implements Broker {

	private final static int portNA = -1;
	private int portCreditBuySell = portNA;
	private int portNormalBuySell = portNA;
	private int portPairOpen = portNA;
	private int portPairClose = portNA;
	private int portMonitor = portNA;
	private Process closeUi = null;
	private Process openUi = null;
	private final DecimalFormat chinaStockPriceFormat = new DecimalFormat("#.###");
	private final DecimalFormat chinaStockQuantityFormat = new DecimalFormat("#");
	private final HashMap<Long, String> results = new HashMap<>();

	public void stop () {
		try{
			closeUi.destroy();
		} catch (Exception e) {
		}
		try {
			openUi.destroy();
		} catch (Exception e) {
		}
	}

	public void test () {
		long start = System.currentTimeMillis();
		ArrayList<HexinOrder> orderList = new ArrayList<>();
		Stock s600000 = new Stock("600000");
		Stock s600031 = new Stock("600031");
		boolean testPassed = true;
		if (portNormalBuySell != portNA) { // test
			if (queryUiInfo(portNormalBuySell).contains("NormalBuySell")){
				System.out.println("NormalBuySell port test passed!");
			} else {
				System.out.println("NormalBuySell port test failed!!!");
				testPassed = false;
			}
			HexinOrder normalSell = new HexinOrder(s600031, 100, 666, HexinOrder.HexinType.SIMPLE_SELL);
			HexinOrder normalBuy = new HexinOrder(s600000, 100, 1.55, HexinOrder.HexinType.SIMPLE_BUY);
			orderList.clear();
			orderList.add(normalSell);
			orderList.add(normalBuy);
			sendOrder(orderList);
			if (normalSell.getAck().contains("证券可用数量不足") || normalSell.getAck().contains("当前时间不允许委托")) {
				System.out.println("normalSell test passed!");
			} else {
				System.out.println("normalSell test failed!!!");
				System.out.println(normalSell.getAck());
				testPassed = false;
			}
			if (normalBuy.getAck().contains("您的买入委托已成功提交") || normalBuy.getAck().contains("当前时间不允许委托")) {
				System.out.println("normalBuy test passed!");
			} else {
				System.out.println("normalBuy test failed!!!");
				System.out.println(normalBuy.getAck());
				testPassed = false;
			}
		}
		if (portCreditBuySell != portNA) { // test
			if (queryUiInfo(portCreditBuySell).contains("CreditBuySell")) {
				System.out.println("CreditBuySell port test passed!");
			} else {
				System.out.println("CreditBuySell port test failed!!!");
				testPassed = false;
			}

			HexinOrder creditSell = new HexinOrder(s600031, 100, 666, HexinOrder.HexinType.CREDIT_SELL);
			HexinOrder creditBuy = new HexinOrder(s600000, 100, 1.55, HexinOrder.HexinType.CREDIT_BUY);
			orderList.clear();
			orderList.add(creditSell);
			orderList.add(creditBuy);
			sendOrder(orderList);
			if (creditSell.getAck().contains("证券可用数量不足") || creditSell.getAck().contains("当前时间不允许委托")) {
				System.out.println("creditSell test passed!");
			} else {
				System.out.println("creditSell test failed!!!");
				System.out.println(creditSell.getAck());
				testPassed = false;
			}
			if (creditBuy.getAck().contains("您的买入委托已成功提交") || creditBuy.getAck().contains("当前时间不允许委托")) {
				System.out.println("creditBuy test passed!");
			} else {
				System.out.println("creditBuy test failed!!!");
				System.out.println(creditBuy.getAck());
				testPassed = false;
			}
		}

		if (portPairClose != portNA) { // test
			if (queryUiInfo(portPairClose).contains("PairClose")){
				System.out.println("PairClose port test passed!");
			} else {
				System.out.println("PairClose port test failed!!!");
				testPassed = false;
			}
			HexinOrder closeLongSellBack = new HexinOrder(s600031, 100, 666, HexinOrder.HexinType.PAIR_CLOSE_LONG_SELL_BACK);
			HexinOrder closeShortBuyBack = new HexinOrder(s600000, 100, 1.55, HexinOrder.HexinType.PAIR_CLOSE_SHORT_BUY_BACK);
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
		if (portPairOpen != portNA) { // test
			if (queryUiInfo(portPairOpen).contains("PairOpen")){
				System.out.println("PairOpen port test passed!");
			} else {
				System.out.println("PairOpen port test failed!!!");
				testPassed = false;
			}
			HexinOrder openShort = new HexinOrder(s600000, 100, 100, HexinOrder.HexinType.PAIR_OPEN_SHORT);
			HexinOrder openLong = new HexinOrder(s600031, 100, 1.85, HexinOrder.HexinType.PAIR_OPEN_LONG);
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
		if (portMonitor != portNA) { // test
			if (queryUiInfo(portMonitor).contains("Monitor")){
				System.out.println("Monitor port test passed!");
			} else {
				System.out.println("Monitor port test failed!!!");
				testPassed = false;
			}
		}
		System.out.println("Test time taken (one sell + one buy): "+(System.currentTimeMillis()-start)/1000d);
		if (!testPassed) {
			System.out.println("LocalBrokerProxy or the .Net broker is not setup properly. Terminating...");
			stop();
			System.exit(-1);
		}
	}

	public LocalBrokerProxy () throws IOException {

		// Step 1 randomize port numbers for the UIs
		portCreditBuySell = new Random (System.currentTimeMillis()).nextInt(9900)+10051;
//		portNormalBuySell = new Random (System.currentTimeMillis()).nextInt(9900)+10051;
//		portPairOpen = portPairClose + 1;

		// Step 2 build windows processes for the UIs
//		ProcessBuilder pbClose = new ProcessBuilder("E:/Projects/C#/Autobet/HexinBrokerTest/bin/Release/HexinBrokerTest", ""+portPairClose, "0", "PairClose", "huatai");
//		ProcessBuilder pbOpen = new ProcessBuilder("E:/Projects/C#/Autobet/HexinBrokerTest/bin/Release/HexinBrokerTest", ""+portPairOpen, "1", "PairOpen", "huatai");
//		ProcessBuilder pbClose = new ProcessBuilder("HexinBroker", ""+portPairClose, "0", "PairClose", "huatai");
//		ProcessBuilder pbOpen = new ProcessBuilder("HexinBroker", ""+portPairOpen, "1", "PairOpen", "huatai");
		ProcessBuilder pbOpen = new ProcessBuilder("HexinBroker", ""+portCreditBuySell, "0", "CreditBuySell", "huatai");
//		ProcessBuilder pbOpen = new ProcessBuilder("HexinBroker", ""+portNormalBuySell, "0", "NormalBuySell", "huatai");

		// Step 3 output redirection for the UIs
//		pbClose.redirectOutput(new File("pair_close.log"));
		pbOpen.redirectOutput(new File("pair_open.log"));

		// Step 4 start windows processes for the UIs
//		closeUi = pbClose.start();
		openUi = pbOpen.start();

		// Pre-test the UIs
		test();
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
		if (!creditOrders.isEmpty()) {
			sendOrder(creditOrders, portCreditBuySell);
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

	public static void main (String [] args) throws InterruptedException, IOException {
		LocalBrokerProxy broker = new LocalBrokerProxy();
		broker.stop();
	}
}