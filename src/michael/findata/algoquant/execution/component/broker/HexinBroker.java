package michael.findata.algoquant.execution.component.broker;

import autoitx4java.AutoItX;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.LibraryLoader;
import com.jacob.com.Variant;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.numericalmethod.algoquant.execution.component.broker.Broker;
import com.numericalmethod.algoquant.execution.datatype.order.LimitOrder;
import com.numericalmethod.algoquant.execution.datatype.order.Order;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.numericalmethod.algoquant.execution.datatype.order.BasicOrderDescription.*;
import static michael.findata.util.LogUtil.getClassLogger;

public class HexinBroker implements Broker{
	@Override
	public void cancelOrder(Collection<? extends Order> orders) {

	}

	private static final Logger LOGGER = getClassLogger();
	private String winTitle;
	private AutoItX x;
	private RingBuffer<OrderEvent> ringBuffer;
	private Timer timer;
	private String hWnd;

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

		hWnd = x.winGetHandle(winTitle);
		// start disruptor
		startDisruptor();

		// start keep-alive timer
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				scheduleKeepAlive();
			}
		}, 60000, 60000);
	}

	private void scheduleKeepAlive () {
		// Dummy order just for keep-alive
		sendOrder(new LimitOrder(null, Side.UNKNOWN, 1, 1));
	}

	private void startTradingConsole(String exePath, String capitalPass, String commPass) {
		x.run(exePath);
		x.winActivate("用户登录");
		x.winWaitActive("用户登录");
		x.send(capitalPass + "\t" + commPass + "\n");
		x.sleep(2000); // must wait for every window that we cannot control
	}

	// not thread safe
	// do not call from anywhere order than issueOrder(Order o)
	private void issueBuyOrder(String securityCode,
							   String amtStr,
							   String priceStr) {
		LOGGER.info("Issuing buy order: {} {}@{}", securityCode, amtStr, priceStr);
		x.winActivate(winTitle);
		x.winWaitActive(winTitle);
		x.sleep(100);x.send("{F3}", false);
		x.sleep(100);x.send("{F1}", false);
		x.sleep(100);x.send(securityCode); // code
		x.sleep(100);x.send("{TAB}", false);
		x.sleep(100);x.send(priceStr); // order price
		x.sleep(100);x.send("{TAB}", false);
		x.sleep(100);x.send(amtStr); // order amount
		x.sleep(100);x.send("b");
		x.sleep(100);x.send("{ENTER}", false);
		x.sleep(200);x.send("{ESCAPE}", false);
		x.sleep(200);x.send("{ESCAPE}", false);
		LOGGER.info("Issued buy order: {} {}@{} !!", securityCode, amtStr, priceStr);
	}

	// not thread safe
	// do not call from anywhere order than issueOrder(Order o)
	private void issueSellOrder(String securityCode,
								String amtStr,
								String priceStr) {
		LOGGER.info("Issuing sell order: {} {}@{}", securityCode, amtStr, priceStr);
		x.winActivate(winTitle);
		x.winWaitActive(winTitle);
		x.sleep(100);x.send("{F3}", false);
		x.sleep(100);x.send("{F2}", false);
		x.sleep(100);x.send(securityCode); // code
		x.sleep(100);x.send("{TAB}", false);
		x.sleep(100);x.send(priceStr); // order price
		x.sleep(100);x.send("{TAB}", false);
		x.sleep(100);x.send(amtStr); // order amount
		x.sleep(100);x.send("s");
		x.sleep(100);x.send("{ENTER}", false);
		x.sleep(200);x.send("{ESCAPE}", false);
		x.sleep(200);x.send("{ESCAPE}", false);
		LOGGER.info("Issued sell order: {} {}@{} !!", securityCode, amtStr, priceStr);
	}

	// not thread safe
	// do not call from anywhere order than issueOrder(Order o)
	private void issueKeepAlive() {
		LOGGER.info("Issuing keep-alive.");
		x.winActivate(winTitle);
		x.winWaitActive(winTitle);
		x.sleep(100);x.send("{F6}", false);
		x.sleep(100);x.send("{F3}", false);
		LOGGER.info("Issued keep-alive !!");
	}

	// not thread safe
	// do not use anywhere order than in startDisruptor
	private void issueOrder(Order o) {
		if (Side.UNKNOWN.equals(o.side())) {
			LOGGER.info("This is neither a buy order nor a sell order, we treat it as a keep-alive empty action.");
			issueKeepAlive();
		} else if (o.type() != Order.OrderExecutionType.LIMIT_ORDER) {
			LOGGER.error("Market order handling is not yet implemented.");
			LOGGER.error("Order: {} discarded.", o);
		} else if (! (o.product() instanceof michael.findata.model.Stock) ) {
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
					LOGGER.info("This is neither a buy order nor a sell order, we treat it as a keep-alive empty action.");
					issueKeepAlive();
			}
		}
	}

	public void issueBuyOrder2 (String securityCode,
								String amtStr,
								String priceStr) {
//		System.out.println(x.controlTreeViewExists(winTitle, "", "SysTreeView321", "ETF网下"));

		x.controlTreeViewSelect(winTitle, "", "SysTreeView321", "ETF网下");
		ActiveXComponent autoItX = x.autoItX;
		Variant vTitle = new Variant(winTitle);
		Variant vText = new Variant("");
		Variant vControl = new Variant("SysTreeView321");
		Variant vCommand = new Variant("Expand1");
		Variant vOption = new Variant("ETF网下");
		Variant vOption2 = new Variant("");
//		Variant[] params = new Variant[]{vTitle, vText, vControl, vCommand, vOption, vOption2};
//		System.out.println(autoItX.invoke("ControlTreeView", params));
//		System.out.println(x.controlGetText(winTitle, "", "CVirtualGridCtrl1"));
//		x.controlTreeViewGetItemCount(winTitle, "", "SysTreeView321", "ETF网下");
//		System.out.println(autoItX.invoke("ControlTreeView",params));
//		for (int i = 0; i < 2; i++) {
//			System.out.print("Static"+i+":\t\t");
//			System.out.println(autoItX.invoke(
//					"ControlGetText",
//					vTitle,
//					vText,
//					new Variant("Static"+i)));
//		}
	}

	private DecimalFormat chinaStockPriceFormat = new DecimalFormat("#.###");
	private DecimalFormat chinaStockQuantityFormat = new DecimalFormat("#");

	@Override
	public void sendOrder(Collection<? extends Order> orders) {
		orders.parallelStream().forEach(this::sendOrder);
	}

	public void sendOrder(Order order) {
		ringBuffer.publishEvent((event, sequence, buffer) -> event.set(order), order);
	}

	// for disruptor
	private static class OrderEvent {
		private Order value;
		public void set(Order value) {
			this.value = value;
		}
		public Order getOrder () {
			return value;
		}
	}

	// for disruptor, call once only in new
	private void startDisruptor () {
		// Executor that will be used to construct new threads for consumers
		Executor executor = Executors.newCachedThreadPool();

		// Specify the size of the ring buffer, must be power of 2.
		int bufferSize = 16;

		// Construct the Disruptor
		Disruptor<OrderEvent> disruptor = new Disruptor<>(OrderEvent::new, bufferSize, executor);

		// Connect the handler
		disruptor.handleEventsWith((event, sequence, endOfBatch) -> issueOrder(event.getOrder()));

		// Start the Disruptor, starts all threads running
		disruptor.start();

		// Get the ring buffer from the Disruptor to be used for publishing.
		ringBuffer = disruptor.getRingBuffer();
	}
}