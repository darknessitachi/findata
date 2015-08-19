package michael.findata.algoquant.execution.component.broker;

import autoitx4java.AutoItX;
import com.jacob.com.LibraryLoader;
import com.numericalmethod.algoquant.execution.component.broker.Broker;
import com.numericalmethod.algoquant.execution.datatype.order.LimitOrder;
import com.numericalmethod.algoquant.execution.datatype.order.Order;
import com.numericalmethod.algoquant.execution.datatype.product.stock.Stock;
import michael.findata.algoquant.product.stock.shse.SHSEStock;
import michael.findata.algoquant.product.stock.szse.SZSEStock;
import org.slf4j.Logger;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;

import static com.numericalmethod.algoquant.execution.datatype.order.BasicOrderDescription.*;
import static com.numericalmethod.nmutils.NMUtils.getClassLogger;

/**
 * Created by nicky on 2015/8/17.
 */
public class HexinBroker implements Broker {

	private static final Logger LOGGER = getClassLogger();
	private String winTitle;
	private AutoItX x;

	public HexinBroker () {
		this("网上股票交易系统5.0", false);
	}

	public HexinBroker (String consoleWinTitle, boolean startConsole) {
		this(consoleWinTitle, startConsole, "E:\\Program Files\\weituo\\华泰联合\\xiadan.exe", "495179", "514979");
	}

	public HexinBroker (String consoleWinTitle,
						boolean startConsole,
						String consoleProgramPath,
						String capitalPass,
						String commPass) {
		File file = new File("lib", "jacob-1.14.3-x64.dll"); //path to the jacob dll
		System.setProperty(LibraryLoader.JACOB_DLL_PATH, file.getAbsolutePath());

		x = new AutoItX();
		winTitle = consoleWinTitle;

		// run the program and login
		// No need to ask the program to start it if you can manually start the console
		if (startConsole) {
			startTradingConsole(consoleProgramPath, capitalPass, commPass);
		}
	}

	private void startTradingConsole(String exePath, String capitalPass, String commPass) {
		x.run(exePath);
		x.winActivate("用户登录");
		x.winWaitActive("用户登录");
		x.send(capitalPass + "\t" + commPass + "\n");
		x.sleep(2000); // must wait for every window that we cannot control
	}

	private void issueBuyOrder(String securityCode,
							   String amtStr,
							   String priceStr) {
		LOGGER.info("Issuing buy order: {} {}@{}", securityCode, amtStr, priceStr);
		x.winActivate(winTitle);
		x.winWaitActive(winTitle);
		x.sleep(100);x.send("{F3}", false);
		x.sleep(100);x.send("{F1}", false);
		x.sleep(100);x.send(securityCode); // code
		x.sleep(100);x.send("\t");
		x.sleep(100);x.send(priceStr); // order price
		x.sleep(100);x.send("\t");
		x.sleep(100);x.send(amtStr); // order amount
		x.sleep(100);x.send("b");
		x.sleep(100);x.send("\n");
		x.sleep(200);x.send("\n");
		x.sleep(200);x.send("\n");
		LOGGER.info("Issued buy order: {} {}@{}", securityCode, amtStr, priceStr);
	}

	private void issueSellOrder(String securityCode,
								String amtStr,
								String priceStr) {
		LOGGER.info("Issuing sell order: {} {}@{}", securityCode, amtStr, priceStr);
		x.winActivate(winTitle);
		x.winWaitActive(winTitle);
		x.sleep(100);x.send("{F3}", false);
		x.sleep(100);x.send("{F2}", false);
		x.sleep(100);x.send(securityCode); // code
		x.sleep(100);x.send("\t");
		x.sleep(100);x.send(priceStr); // order price
		x.sleep(100);x.send("\t");
		x.sleep(100);x.send(amtStr); // order amount
		x.sleep(100);x.send("s");
		x.sleep(100);x.send("\n");
		x.sleep(200);x.send("\n");
		x.sleep(200);x.send("\n");
		LOGGER.info("Issued sell order: {} {}@{}", securityCode, amtStr, priceStr);
	}

	private void issueKeepAlive() {
		x.winActivate(winTitle);
		x.winWaitActive(winTitle);
		x.sleep(100);x.send("{F3}", false);
		x.sleep(100);x.send("{F6}", false);
	}

	@Override
	public void sendOrder(Collection<Order> orders) {
		DecimalFormat chinaStockPriceFormat = new DecimalFormat("#.###");
		DecimalFormat chinaStockQuantityFormat = new DecimalFormat("#");
		for (Order o : orders) {
			if (o.type(o.price()) != Order.OrderExecutionType.LIMIT_ORDER) {
				LOGGER.error("Market order handling is not yet implemented.");
				LOGGER.error("Order: {} discarded.", o);
			} else if (! (o.product() instanceof SHSEStock || o.product() instanceof SZSEStock) ) {
				LOGGER.error("This broker doesn't handle orders for products outside China.");
				LOGGER.error("Order: {} discarded.", o);
			} else {
				LOGGER.info("Handling order: " + o);
				switch (o.side()) {
					case BUY:
						issueBuyOrder(o.product().symbol().substring(0, 6),
								chinaStockQuantityFormat.format(o.quantity()),
								chinaStockPriceFormat.format(o.price()));
						break;
					case SELL:
						issueSellOrder(o.product().symbol().substring(0, 6),
								chinaStockQuantityFormat.format(o.quantity()),
								chinaStockPriceFormat.format(o.price()));
						break;
					default:
						LOGGER.error("This is neither a buy order nor a sell order.");
						LOGGER.error("Order: {} discarded.", o);
				}
			}
		}
	}

	// Test only
	public static void main (String [] args) {
		HexinBroker hxBroker = new HexinBroker("网上股票交易系统5.0", true);
		final Stock stock0 = new SZSEStock("000568.SZ");
		final Stock stock1 = new SHSEStock("600000.SS");
//		.. stopped here ... try using distruptor here since HexinBroker need to be used for two sperated tasks:
//		1. keep alive
//		2. issue order
		hxBroker.sendOrder(Collections.<Order>singletonList(new LimitOrder(stock0, Side.BUY, 100, 20)));
		hxBroker.sendOrder(Collections.<Order>singletonList(new LimitOrder(stock1, Side.SELL, 100, 20)));
	}
}