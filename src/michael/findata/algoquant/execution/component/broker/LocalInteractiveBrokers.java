package michael.findata.algoquant.execution.component.broker;

import com.ib.client.*;
import com.numericalmethod.algoquant.execution.datatype.order.Order;
import com.numericalmethod.algoquant.execution.datatype.product.Product;
import com.numericalmethod.algoquant.execution.datatype.product.stock.Exchange;
import michael.findata.algoquant.execution.component.depthprovider.DepthProvider;
import michael.findata.algoquant.execution.datatype.depth.Depth;
import michael.findata.algoquant.execution.datatype.order.HexinOrder;
import michael.findata.algoquant.execution.listener.DepthListener;
import michael.findata.algoquant.execution.listener.OrderListener;
import michael.findata.model.Stock;
import org.slf4j.Logger;

import java.util.*;

import static com.numericalmethod.nmutils.NMUtils.getClassLogger;

public class LocalInteractiveBrokers implements Broker, EWrapper, DepthProvider {
	private static final Logger LOGGER = getClassLogger();

	private EJavaSignal m_signal = new EJavaSignal();
	private EClientSocket m_s = new EClientSocket(this, m_signal);
	private long validId = 0;

	private static int CNH_HKD_TICKER_ID = 1001;
	public static double CNH_HKD_ask = -1;
	public static double CNH_HKD_bid = -1;
	public static double CNH_HKD_last = 1.1180;

	private Map<Long, Order> orderMap;
	private Map<Long, OrderListener> orderListenerMap;
	private int port;
	private Map<Integer, Stock> subscribed;
	private Map<Integer, DepthData> depthDataMap;
	private DepthListener depthListener;

	private static byte MAX_DEPTHS = 1;

	@Override
	public void setDepthListener(DepthListener listener) {
		depthListener = listener;
	}

	private class DepthData {
		private long [] askVol = new long [MAX_DEPTHS];
		private double [] askPrc = new double [MAX_DEPTHS];
		private long [] bidVol = new long [MAX_DEPTHS];
		private double [] bidPrc = new double [MAX_DEPTHS];
		private double lastPrc;
	}

	public LocalInteractiveBrokers (int port, Stock ... subscriptions) {
		this.port = port;
		orderMap = new HashMap<>();
		orderListenerMap = new HashMap<>();
		subscribed = new HashMap<>();
		depthDataMap = new HashMap<>();
		if (subscriptions != null) {
			for (Stock s : subscriptions) {
				subscribed.put(Integer.parseInt(s.getCode()), s);
				depthDataMap.put(Integer.parseInt(s.getCode()), new DepthData());
			}
		}
		start();
	}

	public void start() {
		// this thread is in charge of start / stop the client
		m_s.eConnect("127.0.0.1", port, 0);
		final EReader reader = new EReader(m_s, m_signal);
		reader.start();

		new Thread() {
			public void run() {
				while (m_s.isConnected()) {
					m_signal.waitForSignal();
					try {
						reader.processMsgs();
					} catch (Exception e) {
						LOGGER.warn("Exception: "+e.getMessage());
					}
				}
			}
		}.start();

//		m_s.reqSecDefOptParams(0, "IBM", "",/* "",*/ "STK", 8314);
		m_s.reqAllOpenOrders();
//		m_s.reqGlobalCancel();

		// request CNH.HKD
		Contract contract_CNH_HKD = new Contract();
		contract_CNH_HKD.exchange("IDEALPRO");
		contract_CNH_HKD.symbol("CNH");
		contract_CNH_HKD.currency("HKD");
		contract_CNH_HKD.secType("CASH");
		m_s.reqMktData(CNH_HKD_TICKER_ID, contract_CNH_HKD, "", false, null);

		Contract hkStockContract = new Contract ();
		hkStockContract.exchange("SEHK");
		hkStockContract.secType("STK");
		for (Map.Entry<Integer, Stock> entry : subscribed.entrySet()) {
			hkStockContract.symbol(entry.getKey().toString());
			m_s.reqMktData(entry.getKey(), hkStockContract, "", false, null);
		}

		if (m_s.isConnected()) {
			LOGGER.info("Interactive Brokers client connection succeeded.");
		} else {
			LOGGER.warn("Interactive Brokers client connection failed.");
		}
//		try {
//			Thread.currentThread().sleep(2000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
	}

	public void stop () {
		m_s.eDisconnect();
		LOGGER.info("Interactive Brokers client disconnected.");
	}

	// Non-thread-safe
	// for HKSE stock only, will expand later
	// Not thread-safe!!!!!!
	@Override
	public void sendOrder(Collection<? extends Order> orders) {
		Contract contractStock = new Contract();
		contractStock.secType("STK");
		String buyOrSell, symbol, exchange;
		for (Order order : orders) {
			Product p = order.product();
			if (p instanceof Stock) {
				Stock s = (Stock) p;
				switch (s.exchange()) {
					case HKEX:
						// 01288.HK -> 1288
						// 00914.HK -> 914
						symbol = order.product().symbol();
						if (symbol.startsWith("0")) {
							symbol = symbol.substring(1, 4);
						} else {
							symbol = symbol.substring(0, 4);
						}
						exchange = "SEHK";
						break;
					case SHSE:
						symbol = s.getCode();
						exchange = "SEHKNTL";
						break;
					case SZSE:
						symbol = s.getCode();
						exchange = "SEHKSZSE";
						break;
					default:
						LOGGER.error("Unable to handle product: {}", s);
						continue;
				}
			} else {
				// Assume yahoo stock code
				// 600000.SS / 000568.SZ / 0914.HK
				if (p.symbol().endsWith(".HK")) {
					exchange = "SEHKNTL";
					symbol = p.symbol();
					if (symbol.startsWith("0")) {
						symbol = symbol.substring(1, 4);
					} else {
						symbol = symbol.substring(0, 4);
					}
				} else if (p.symbol().endsWith(".SS")) {
					exchange = "SEHKNTL";
					symbol = p.symbol().substring(0, 6);
				} else if (p.symbol().endsWith(".SZ")) {
					exchange = "SEHKSZSE";
					symbol = p.symbol().substring(0, 6);
				} else {
					LOGGER.error("Unable to handle product: {}", p);
					continue;
				}
			}
			contractStock.symbol(symbol);
			contractStock.exchange(exchange);

//			contractStock.secType("CASH");// TODO: 11/16/2016 change it back!!!
//			contractStock.exchange("IDEALPRO");// TODO: 11/16/2016 change it back!!!
//			contractStock.symbol("CNH");// TODO: 11/16/2016 change it back!!!
//			contractStock.currency("HKD");// TODO: 11/16/2016 change it back!!!

			switch (order.side()) {
				case BUY:
					buyOrSell = "BUY";
					break;
				case SELL:
					buyOrSell = "SELL";
					break;
				default:
					LOGGER.error("Unable to handle product: {}", p);
					continue;
			}
			if (order instanceof HexinOrder) {
				if (orderMap.containsKey(order.id())) {
					m_s.placeOrder((int)order.id(), contractStock, OrderSamples.LimitOrder(buyOrSell, order.quantity(), order.price()));
					LOGGER.info("Updated order [{}].", order);
				} else {
					int vId = (int) validId;
					validId ++;
					m_s.placeOrder(vId, contractStock, OrderSamples.LimitOrder(buyOrSell, order.quantity(), order.price()));
					order.id(vId);
					orderMap.put((long)vId, order);
					LOGGER.info("Placed order [{}].", order);
				}
			}
		}
	}

	@Override
	public void cancelOrder(Collection<? extends Order> orders) {
		for (Order order : orders) {
			LOGGER.info("Cancelling order [{}]", order.id());
			m_s.cancelOrder((int) order.id());
		}
	}

	@Override
	public void setOrderListener (Order order, OrderListener listener) {
		orderListenerMap.put(order.id(), listener);
	}

	@Override
	public void nextValidId(int orderId) {
		LOGGER.info("nextValidId: "+orderId);
		validId = orderId;
	}

	@Override
	public void error(Exception e) {
		LOGGER.info("Error: "+e.getMessage());
	}

	@Override
	public void error(int id, int errorCode, String errorMsg) {
		LOGGER.info("Error - id: "+id+", errorCode: "+errorCode+", errorMessage: "+errorMsg);
	}

	@Override
	public void connectionClosed() {
	}

	@Override
	public void error(String str) {
		LOGGER.info("Error: "+str);
	}

	/**
	 * Bid Price	field	1	Highest priced bid for the contract.		IBApi.EWrapper.tickPrice	-
	 * Ask Price	field	2	Lowest price offer on the contract.			IBApi.EWrapper.tickPrice	-
	 * Last Price	field	4	Last price at which the contract traded.	IBApi.EWrapper.tickPrice	-
	 * High			field	6	High price for the day.						IBApi.EWrapper.tickPrice	-
	 * Low			field	7	Low price for the day.						IBApi.EWrapper.tickPrice	-
	 * Close Price	field	9	The last available closing price for the previous day.
	 * 							For US Equities, we use corporate action processing to get the closing price,
	 * 							so the close price is adjusted to reflect forward and reverse splits and cash and stock dividends.
	 * Open Tick	field	14	Today's opening price. The official opening price requires a market data subscription to the native exchange of a contract.
	 */
	@Override
	public void tickPrice(int tickerId, int field, double price, int canAutoExecute) {
//		LOGGER.info("Tick Price. Ticker Id: {}, Field: {}, Price: {}, CanAutoExecute: {}", tickerId, field, price, canAutoExecute);
		if (price <= 0) {
			return;
		}
		if (tickerId == CNH_HKD_TICKER_ID) {
			switch (field) {
				case 1:
					CNH_HKD_bid = price;
					break;
				case 2:
					CNH_HKD_ask = price;
					break;
				case 4:
					CNH_HKD_last = price;
					break;
				case 9:
					CNH_HKD_last = price;
					break;
				default:
			}
			return;
		}
		DepthData dd = depthDataMap.get(tickerId);
		switch (field) {
			case 1:
				dd.bidPrc[0] = price;
				break;
			case 2:
				dd.askPrc[0] = price;
				break;
			case 4:
				dd.lastPrc = price;
				break;
			case 9:
				dd.lastPrc = price;
				if (dd.bidPrc[0] < 0) {
					dd.bidPrc[0] = price;
				}
				if (dd.askPrc[0] < 0) {
					dd.askPrc[0] = price;
				}
				return;
			default:
				return;
		}
		if (dd.bidPrc[0] <= 0f || dd.askPrc[0] <= 0f) {
			return;
		}
		Depth d = new Depth(dd.lastPrc, subscribed.get(tickerId), true, dd.bidPrc[0], dd.askPrc[0]);
		d.setVols(dd.bidVol[0], dd.askVol[0]);
		LOGGER.info("Depth updated: {} ", d);
		if (depthListener != null) {
			depthListener.depthUpdated(d);
		} else {
			LOGGER.info("depthListener is null!!");
		}
	}

	/**
	 * Bid Size		field	0	Number of contracts or lots offered at the bid price.	IBApi.EWrapper.tickSize	-
	 * Ask Size		field	3	Number of contracts or lots offered at the ask price.	IBApi.EWrapper.tickSize	-
	 * Last Size	field	5	Number of contracts or lots traded at the last price.
	 * Volume		field	8	Trading volume for the day for the selected contract (US Stocks: multiplier 100).
	 */
	@Override
	public void tickSize(int tickerId, int field, int size) {
//		LOGGER.info("Tick Size. Ticker Id:" + tickerId + ", Field: " + field + ", Size: " + size);
		if (tickerId == CNH_HKD_TICKER_ID) {
			return;
		}
		switch (field) {
			case 0:
				depthDataMap.get(tickerId).bidVol[0] = size;
				break;
			case 3:
				depthDataMap.get(tickerId).askVol[0] = size;
				break;
		}
//		DepthData dd = depthDataMap.get(tickerId);
//		Depth d = new Depth(dd.lastPrc, subscribed.get(tickerId), true, dd.bidPrc[0], dd.askPrc[0]);
//		d.setVols(dd.bidVol[0], dd.askVol[0]);
//		System.out.println(d);
	}

	@Override
	public void tickOptionComputation(int tickerId, int field, double impliedVol, double delta, double optPrice, double pvDividend, double gamma, double vega, double theta, double undPrice) {
	}

	/**
	 *	Halted			field	49		Indicates if a contract is halted.
	 *									Value	Description
	 *									0		Not halted.
	 *									1		General halt.		Trading halt is imposed for purely regulatory reasons with/without volatility halt.
	 *									2		Volatility halt.	Trading halt is imposed by the exchange to protect against extreme volatility.
	 *
	 *	Shortable		field	46		Describes the level of difficulty with which the contract can be sold short. Generic tick required: 236
	 *									Range					Description
	 *									Value higher than 2.5	There are at least 1000 shares available for short selling.
	 *									Value higher than 1.5	This contract will be available for short selling if shares can be located.
	 *									1.5 or less				Contract is not available for short selling.
	 * @param tickerId
	 * @param tickType
	 * @param value
	 */
	@Override
	public void tickGeneric(int tickerId, int tickType, double value) {
//		LOGGER.info("Tick Generic. Ticker Id:" + tickerId + ", Field: " + TickType.getField(tickType) + ", Value: " + value);
	}

	/**
	 * Last Timestamp	field	45	Time of the last trade (in UNIX time).
	 * @param tickerId
	 * @param tickType
	 * @param value
	 */
	@Override
	public void tickString(int tickerId, int tickType, String value) {
//		LOGGER.info("Tick String. Ticker Id:" + tickerId + ", Type: " + tickType + ", Value: " + value);
	}

	@Override
	public void tickEFP(int tickerId, int tickType, double basisPoints, String formattedBasisPoints,
						double impliedFuture, int holdDays, String futureLastTradeDate, double dividendImpact, double dividendsToLastTradeDate) {
	}


	/**
	 * Gives the up-to-date information of an order every time it changes.
	 * @param orderId	the order's client id.
	 * @param status	the current status of the order:
	 *                  PendingSubmit - indicates that you have transmitted the order, but have not yet received confirmation that it has been accepted by the order destination.
	 *                  				NOTE: This order status is not sent by TWS and should be explicitly set by the API developer when an order is submitted.
	 *                  PendingCancel - indicates that you have sent a request to cancel the order but have not yet received cancel confirmation from the order destination.
	 *                  				At this point, your order is not confirmed canceled. You may still receive an execution while your cancellation request is pending.
	 *                  				NOTE: This order status is not sent by TWS and should be explicitly set by the API developer when an order is canceled.
	 *                  PreSubmitted - indicates that a simulated order type has been accepted by the IB system and that this order has yet to be elected.
	 *                  			   The order is held in the IB system until the election criteria are met.
	 *                  			   At that time the order is transmitted to the order destination as specified.
	 *                  Submitted - indicates that your order has been accepted at the order destination and is working.
	 *                  ApiCanceled - after an order has been submitted and before it has been acknowledged, an API client client can request its cancellation, producing this state.
	 *                  Cancelled - indicates that the balance of your order has been confirmed canceled by the IB system.
	 *                  			This could occur unexpectedly when IB or the destination has rejected your order.
	 *                  Filled - indicates that the order has been completely filled.
	 *                  Inactive - indicates that the order has been accepted by the system (simulated orders) or an exchange (native orders) but that currently the order is inactive due to system, exchange or other issues.
	 * @param filled	number of filled positions.
	 * @param remaining	the remnant positions.
	 * @param avgFillPrice	average filling price.
	 * @param permId	the order's permId used by the TWs to identify orders.
	 * @param parentId	parent's id. Used for bracker and auto trailing stop orders.
	 * @param lastFillPrice	price at which the last positions were filled.
	 * @param clientId	API client which submitted the order.
	 * @param whyHeld	this field is used to identify an order held when TWS is trying to locate shares for a short sell.
	 *                  The value used to indicate this is 'locate'.
	 */
	@Override
	public void orderStatus(int orderId, String status, double filled, double remaining,
							double avgFillPrice, int permId, int parentId, double lastFillPrice, int clientId, String whyHeld) {
		LOGGER.info(String.format("orderStatus: orderId: %s, status: %s, filled: %f, remaining: %f, avgFillPrice: %f, permId: %d, parentId: %d, lastFillPrice: %f, clientId: %d, whyHeld: %s\n",
				orderId, status, filled, remaining, avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld));
		Order order = orderMap.get((long)orderId);
		if (order == null) {
			LOGGER.debug("Cannot find corresponding order with id {} in my order map.", orderId);
			return;
		}
		switch (status) {
			// TODO: 11/14/2016 deal with other status
			case "Cancelled":
				order.state(Order.OrderState.CANCELLED);
				break;
			case "ApiCanceled":
				order.state(Order.OrderState.API_CANCELLED);
				break;
			case "Inactive":
				order.state(Order.OrderState.INACTIVE);
				break;
			case "PendingSubmit":
				order.state(Order.OrderState.PENDING_SUBMIT);
				break;
			case "PendingCancel":
				order.state(Order.OrderState.PENDING_CANCEL);
				break;
			case "PreSubmitted":
				order.state(Order.OrderState.PRE_SUBMITTED);
//				order.fill(order.quantity());
				break;
			case "Filled":
				if (!order.state().equals(Order.OrderState.FILLED)) {
					// make sure our corresponding order is in "FILLED" state
					order.fill(order.quantity());
					LOGGER.info("Order fully filled: {}", order);
				}
				break;
			default:
				if (filled > order.filledQuantity()) {
					LOGGER.info("Order partially filled: {}", order);
					if (order.state().equals(Order.OrderState.UNFILLED)) {
						order.fill(filled);
					} else {
						order.fill(filled - order.filledQuantity());
					}
				}
				break;
		}
		OrderListener listener = orderListenerMap.get((long)orderId);
		if (listener == null) {
			LOGGER.info("No listener found for order {}", order);
			return;
		}
		LOGGER.info("notifying listener {} for order {}", listener, order);
		listener.orderUpdated(order, this);
	}

	@Override
	public void openOrder(int orderId, Contract contract, com.ib.client.Order order, OrderState orderState) {
		LOGGER.info("openOrder: orderId: "+orderId+" stock: "+contract.symbol()+" order: "+order.getOrderType()+" "+order.action()+" "+order.totalQuantity()+" state: "+orderState);
	}

	@Override
	public void openOrderEnd() {
	}

	@Override
	public void updateAccountValue(String key, String value, String currency, String accountName) {
	}

	@Override
	public void updatePortfolio(Contract contract, double position, double marketPrice, double marketValue, double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {
	}

	@Override
	public void updateAccountTime(String timeStamp) {
	}

	@Override
	public void accountDownloadEnd(String accountName) {
	}

	@Override
	public void contractDetails(int reqId, ContractDetails contractDetails) {
	}

	@Override
	public void bondContractDetails(int reqId, ContractDetails contractDetails) {
	}

	@Override
	public void contractDetailsEnd(int reqId) {
	}

	@Override
	public void execDetails(int reqId, Contract contract, Execution execution) {
	}

	@Override
	public void execDetailsEnd(int reqId) {
	}

	@Override
	public void updateMktDepth(int tickerId, int position, int operation, int side, double price, int size) {
	}

	@Override
	public void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation, int side, double price, int size) {
	}

	@Override
	public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
	}

	@Override
	public void managedAccounts(String accountsList) {
	}

	@Override
	public void receiveFA(int faDataType, String xml) {
	}

	@Override
	public void historicalData(int reqId, String date, double open, double high, double low, double close, int volume, int count, double WAP, boolean hasGaps) {
		LOGGER.info("HistoricalData. "+reqId+" - Date: "+date+", Open: "+open+", High: "+high+", Low: "+low+", Close: "+close+", Volume: "+volume+", Count: "+count+", WAP: "+WAP+", HasGaps: "+hasGaps);
	}

	@Override
	public void scannerParameters(String xml) {
	}

	@Override
	public void scannerData(int reqId, int rank, ContractDetails contractDetails, String distance, String benchmark, String projection, String legsStr) {
	}

	@Override
	public void scannerDataEnd(int reqId) {
	}

	@Override
	public void realtimeBar(int reqId, long time, double open, double high, double low, double close, long volume, double wap, int count) {
	}

	@Override
	public void currentTime(long time) {
	}

	@Override
	public void fundamentalData(int reqId, String data) {
	}

	@Override
	public void deltaNeutralValidation(int reqId, DeltaNeutralContract underComp) {
	}

	@Override
	public void tickSnapshotEnd(int reqId) {
	}

	@Override
	public void marketDataType(int reqId, int marketDataType) {
	}

	@Override
	public void commissionReport(CommissionReport commissionReport) {
	}

	@Override
	public void position(String account, Contract contract, double pos, double avgCost) {
	}

	@Override
	public void positionEnd() {
	}

	@Override
	public void accountSummary(int reqId, String account, String tag, String value, String currency) {
	}

	@Override
	public void accountSummaryEnd(int reqId) {
	}

	@Override
	public void verifyMessageAPI(String apiData) {
	}

	@Override
	public void verifyCompleted(boolean isSuccessful, String errorText) {
	}

	@Override
	public void verifyAndAuthMessageAPI(String apiData, String xyzChallenge) {
	}

	@Override
	public void verifyAndAuthCompleted(boolean isSuccessful, String errorText) {
	}

	@Override
	public void displayGroupList(int reqId, String groups) {
	}

	@Override
	public void displayGroupUpdated(int reqId, String contractInfo) {
	}

	@Override
	public void positionMulti(int reqId, String account, String modelCode, Contract contract, double pos, double avgCost) {
	}

	@Override
	public void positionMultiEnd(int reqId) {
	}

	@Override
	public void accountUpdateMulti(int reqId, String account, String modelCode, String key, String value, String currency) {
	}

	@Override
	public void accountUpdateMultiEnd(int reqId) {
	}

	public void connectAck() {
	}

	@Override
	public void securityDefinitionOptionalParameter(int reqId, String exchange, int underlyingConId, String tradingClass,
													String multiplier, Set<String> expirations, Set<Double> strikes) {
		System.out.print(reqId + ", " + exchange + ", " + underlyingConId + ", " + tradingClass + ", " + multiplier);

		for (String exp : expirations) {
			System.out.print(", " + exp);
		}

		for (double strk : strikes) {
			System.out.print(", " + strk);
		}

		System.out.println();
	}

	@Override
	public void securityDefinitionOptionalParameterEnd(int reqId) {
		System.out.println("done");
	}

	@Override
	public void softDollarTiers(int reqId, SoftDollarTier[] tiers) {
	}

	public void printListeners() {
		orderListenerMap.entrySet().forEach(entry -> {
			System.out.printf("IB: %s -> %s\n", entry.getKey(), entry.getValue());
		});
	}
}

class OrderSamples {

	public static com.ib.client.Order AtAuction(String action, double quantity, double price) {
		//! [auction]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.tif("AUC");
		order.orderType("MTL");
		order.totalQuantity(quantity);
		order.lmtPrice(price);
		//! [auction]
		return order;
	}

	public static com.ib.client.Order Discretionary(String action, double quantity, double price, double discretionaryAmt) {
		//! [discretionary]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("LMT");
		order.totalQuantity(quantity);
		order.lmtPrice(price);
		order.discretionaryAmt(discretionaryAmt);
		//! [discretionary]
		return order;
	}

	public static com.ib.client.Order MarketOrder(String action, double quantity) {
		//! [market]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("MKT");
		order.totalQuantity(quantity);
		//! [market]
		return order;
	}

	public static com.ib.client.Order MarketIfTouched(String action, double quantity, double price) {
		//! [market_if_touched]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("MIT");
		order.totalQuantity(quantity);
		order.auxPrice(price);
		//! [market_if_touched]
		return order;
	}

	public static com.ib.client.Order MarketOnClose(String action, double quantity) {
		//! [market_on_close]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("MOC");
		order.totalQuantity(quantity);
		//! [market_on_close]
		return order;
	}

	public static com.ib.client.Order MarketOnOpen(String action, double quantity) {
		//! [market_on_open]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("MKT");
		order.totalQuantity(quantity);
		order.tif("OPG");
		//! [market_on_open]
		return order;
	}

	public static com.ib.client.Order MidpointMatch(String action, double quantity) {
		//! [midpoint_match]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("MKT");
		order.totalQuantity(quantity);
		//! [midpoint_match]
		return order;
	}

	public static com.ib.client.Order PeggedToMarket(String action, double quantity, double marketOffset) {
		//! [pegged_market]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("PEG MKT");
		order.totalQuantity(100);
		order.auxPrice(marketOffset);//Offset price
		//! [pegged_market]
		return order;
	}

	public static com.ib.client.Order PeggedToStock(String action, double quantity, double delta, double stockReferencePrice, double startingPrice) {
		//! [pegged_stock]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("PEG STK");
		order.totalQuantity(quantity);
		order.delta(delta);
		order.lmtPrice(stockReferencePrice);
		order.startingPrice(startingPrice);
		//! [pegged_stock]
		return order;
	}

	public static com.ib.client.Order RelativePeggedToPrimary(String action, double quantity, double priceCap, double offsetAmount) {
		//! [relative_pegged_primary]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("REL");
		order.totalQuantity(quantity);
		order.lmtPrice(priceCap);
		order.auxPrice(offsetAmount);
		//! [relative_pegged_primary]
		return order;
	}

	public static com.ib.client.Order SweepToFill(String action, double quantity, double price) {
		//! [sweep_to_fill]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("LMT");
		order.totalQuantity(quantity);
		order.lmtPrice(price);
		order.sweepToFill(true);
		//! [sweep_to_fill]
		return order;
	}

	public static com.ib.client.Order AuctionLimit(String action, double quantity, double price, int auctionStrategy) {
		//! [auction_limit]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("LMT");
		order.totalQuantity(quantity);
		order.lmtPrice(price);
		order.auctionStrategy(auctionStrategy);
		//! [auction_limit]
		return order;
	}

	public static com.ib.client.Order AuctionPeggedToStock(String action, double quantity, double startingPrice, double delta) {
		//! [auction_pegged_stock]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("PEG STK");
		order.totalQuantity(quantity);
		order.delta(delta);
		order.startingPrice(startingPrice);
		//! [auction_pegged_stock]
		return order;
	}

	public static com.ib.client.Order AuctionRelative(String action, double quantity, double offset) {
		//! [auction_relative]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("REL");
		order.totalQuantity(quantity);
		order.auxPrice(offset);
		//! [auction_relative]
		return order;
	}

	public static com.ib.client.Order Block(String action, double quantity, double price) {
		// ! [block]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("LMT");
		order.totalQuantity(quantity);//Large volumes!
		order.lmtPrice(price);
		order.blockOrder(true);
		// ! [block]
		return order;
	}

	public static com.ib.client.Order BoxTop(String action, double quantity) {
		// ! [boxtop]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("BOX TOP");
		order.totalQuantity(quantity);
		// ! [boxtop]
		return order;
	}

	public static com.ib.client.Order LimitOrder(String action, double quantity, double limitPrice) {
		// ! [limitorder]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("LMT");
		order.totalQuantity(quantity);
		order.lmtPrice(limitPrice);
		// ! [limitorder]
		return order;
	}

	public static com.ib.client.Order LimitIfTouched(String action, double quantity, double limitPrice, double triggerPrice) {
		// ! [limitiftouched]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("LIT");
		order.totalQuantity(quantity);
		order.lmtPrice(limitPrice);
		order.auxPrice(triggerPrice);
		// ! [limitiftouched]
		return order;
	}

	public static com.ib.client.Order LimitOnClose(String action, double quantity, double limitPrice) {
		// ! [limitonclose]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("LOC");
		order.totalQuantity(quantity);
		order.lmtPrice(limitPrice);
		// ! [limitonclose]
		return order;
	}

	public static com.ib.client.Order LimitOnOpen(String action, double quantity, double limitPrice) {
		// ! [limitonopen]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.tif("OPG");
		order.orderType("LOC");
		order.totalQuantity(quantity);
		order.lmtPrice(limitPrice);
		// ! [limitonopen]
		return order;
	}

	public static com.ib.client.Order PassiveRelative(String action, double quantity, double offset) {
		// ! [passive_relative]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("PASSV REL");
		order.totalQuantity(quantity);
		order.auxPrice(offset);
		// ! [passive_relative]
		return order;
	}

	public static com.ib.client.Order PeggedToMidpoint(String action, double quantity, double offset) {
		// ! [pegged_midpoint]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("PEG MID");
		order.totalQuantity(quantity);
		order.auxPrice(offset);
		// ! [pegged_midpoint]
		return order;
	}

	//! [bracket]
	public static List<com.ib.client.Order> BracketOrder(int parentOrderId, String action, double quantity, double limitPrice, double takeProfitLimitPrice, double stopLossPrice) {
		//This will be our main or "parent" order
		com.ib.client.Order parent = new com.ib.client.Order();
		parent.orderId(parentOrderId);
		parent.action(action);
		parent.orderType("LMT");
		parent.totalQuantity(quantity);
		parent.lmtPrice(limitPrice);
		//The parent and children orders will need this attribute set to false to prevent accidental executions.
		//The LAST CHILD will have it set to true.
		parent.transmit(false);

		com.ib.client.Order takeProfit = new com.ib.client.Order();
		takeProfit.orderId(parent.orderId() + 1);
		takeProfit.action(action.equals("BUY") ? "SELL" : "BUY");
		takeProfit.orderType("LMT");
		takeProfit.totalQuantity(quantity);
		takeProfit.lmtPrice(takeProfitLimitPrice);
		takeProfit.parentId(parentOrderId);
		takeProfit.transmit(false);

		com.ib.client.Order stopLoss = new com.ib.client.Order();
		stopLoss.orderId(parent.orderId() + 2);
		stopLoss.action(action.equals("BUY") ? "SELL" : "BUY");
		stopLoss.orderType("STP");
		//Stop trigger price
		stopLoss.auxPrice(stopLossPrice);
		stopLoss.totalQuantity(quantity);
		stopLoss.parentId(parentOrderId);
		//In this case, the low side order will be the last child being sent. Therefore, it needs to set this attribute to true
		//to activate all its predecessors
		stopLoss.transmit(true);

		List<com.ib.client.Order> bracketOrder = new ArrayList<com.ib.client.Order>();
		bracketOrder.add(parent);
		bracketOrder.add(takeProfit);
		bracketOrder.add(stopLoss);

		return bracketOrder;
	}
	//! [bracket]

	public static com.ib.client.Order MarketToLimit(String action, double quantity) {
		// ! [markettolimit]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("MTL");
		order.totalQuantity(quantity);
		// ! [markettolimit]
		return order;
	}

	public static com.ib.client.Order MarketWithProtection(String action, double quantity) {
		// ! [marketwithprotection]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("MKT PRT");
		order.totalQuantity(quantity);
		// ! [marketwithprotection]
		return order;
	}

	public static com.ib.client.Order Stop(String action, double quantity, double stopPrice) {
		// ! [stop]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("STP");
		order.auxPrice(stopPrice);
		order.totalQuantity(quantity);
		// ! [stop]
		return order;
	}

	public static com.ib.client.Order StopLimit(String action, double quantity, double limitPrice, double stopPrice) {
		// ! [stoplimit]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("STP LMT");
		order.lmtPrice(limitPrice);
		order.auxPrice(stopPrice);
		order.totalQuantity(quantity);
		// ! [stoplimit]
		return order;
	}

	public static com.ib.client.Order StopWithProtection(String action, double quantity, double stopPrice) {
		// ! [stopwithprotection]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("STP PRT");
		order.auxPrice(stopPrice);
		order.totalQuantity(quantity);
		// ! [stopwithprotection]
		return order;
	}

	public static com.ib.client.Order TrailingStop(String action, double quantity, double trailingPercent, double trailStopPrice) {
		// ! [trailingstop]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("TRAIL");
		order.trailingPercent(trailingPercent);
		order.trailStopPrice(trailStopPrice);
		order.totalQuantity(quantity);
		// ! [trailingstop]
		return order;
	}

	public static com.ib.client.Order TrailingStopLimit(String action, double quantity, double trailingAmount, double trailStopPrice, double limitPrice) {
		// ! [trailingstoplimit]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("TRAIL LIMIT");
		order.lmtPrice(limitPrice);
		order.auxPrice(trailingAmount);
		order.trailStopPrice(trailStopPrice);
		order.totalQuantity(quantity);
		// ! [trailingstoplimit]
		return order;
	}

	public static com.ib.client.Order ComboLimitOrder(String action, double quantity, boolean nonGuaranteed, double limitPrice) {
		// ! [combolimit]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("LMT");
		order.lmtPrice(limitPrice);
		order.totalQuantity(quantity);
		if (nonGuaranteed)
		{
			List<TagValue> smartComboRoutingParams = new ArrayList<TagValue>();
			smartComboRoutingParams.add(new TagValue("NonGuaranteed", "1"));
		}
		// ! [combolimit]
		return order;
	}

	public static com.ib.client.Order ComboMarketOrder(String action, double quantity, boolean nonGuaranteed) {
		// ! [combomarket]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("MKT");
		order.totalQuantity(quantity);
		if (nonGuaranteed)
		{
			List<TagValue> smartComboRoutingParams = new ArrayList<TagValue>();
			smartComboRoutingParams.add(new TagValue("NonGuaranteed", "1"));
		}
		// ! [combomarket]
		return order;
	}

	public static com.ib.client.Order LimitOrderForComboWithLegPrices(String action, double quantity, boolean nonGuaranteed, double[] legPrices) {
		// ! [limitordercombolegprices]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("LMT");
		order.totalQuantity(quantity);
		order.orderComboLegs(new ArrayList<OrderComboLeg>());

		for(double price : legPrices) {
			OrderComboLeg comboLeg = new OrderComboLeg();
			comboLeg.price(5.0);
			order.orderComboLegs().add(comboLeg);
		}

		if (nonGuaranteed)
		{
			List<TagValue> smartComboRoutingParams = new ArrayList<TagValue>();
			smartComboRoutingParams.add(new TagValue("NonGuaranteed", "1"));
		}
		// ! [limitordercombolegprices]
		return order;
	}

	public static com.ib.client.Order RelativeLimitCombo(String action, double quantity, boolean nonGuaranteed, double limitPrice) {
		// ! [relativelimitcombo]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("REL + LMT");
		order.totalQuantity(quantity);
		order.lmtPrice(limitPrice);
		if (nonGuaranteed)
		{
			List<TagValue> smartComboRoutingParams = new ArrayList<TagValue>();
			smartComboRoutingParams.add(new TagValue("NonGuaranteed", "1"));
		}
		// ! [relativelimitcombo]
		return order;
	}

	public static com.ib.client.Order RelativeMarketCombo(String action, double quantity, boolean nonGuaranteed) {
		// ! [relativemarketcombo]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("REL + MKT");
		order.totalQuantity(quantity);
		if (nonGuaranteed)
		{
			List<TagValue> smartComboRoutingParams = new ArrayList<TagValue>();
			smartComboRoutingParams.add(new TagValue("NonGuaranteed", "1"));
		}
		// ! [relativemarketcombo]
		return order;
	}

	// ! [oca]
	public static List<com.ib.client.Order> OneCancelsAll(String ocaGroup, List<com.ib.client.Order> ocaOrders, int ocaType) {

		for (com.ib.client.Order o : ocaOrders) {
			o.ocaGroup(ocaGroup);
			o.ocaType(ocaType);
		}
		return ocaOrders;
	}
	// ! [oca]

	public static com.ib.client.Order Volatility(String action, double quantity, double volatilityPercent, int volatilityType) {
		// ! [volatility]
		com.ib.client.Order order = new com.ib.client.Order();
		order.action(action);
		order.orderType("VOL");
		order.volatility(volatilityPercent);//Expressed in percentage (40%)
		order.volatilityType(volatilityType);// 1=daily, 2=annual
		order.totalQuantity(quantity);
		// ! [volatility]
		return order;
	}

	//! [fhedge]
	public static com.ib.client.Order MarketFHedge(int parentOrderId, String action) {
		//FX Hedge orders can only have a quantity of 0
		com.ib.client.Order order = MarketOrder(action, 0);
		order.parentId(parentOrderId);
		order.hedgeType("F");
		return order;
	}
	//! [fhedge]

	public static com.ib.client.Order PeggedToBenchmark(String action, double quantity, double startingPrice, boolean peggedChangeAmountDecrease, double peggedChangeAmount, double referenceChangeAmount, int referenceConId, String referenceExchange, double stockReferencePrice,
														double referenceContractLowerRange, double referenceContractUpperRange) {
		//! [pegged_benchmark]
		com.ib.client.Order order = new com.ib.client.Order();
		order.orderType("PEG BENCH");
		//BUY or SELL
		order.action(action);
		order.totalQuantity(quantity);
		//Beginning with price...
		order.startingPrice(startingPrice);
		//increase/decrease price...
		order.isPeggedChangeAmountDecrease(peggedChangeAmountDecrease);
		//by... (and likewise for price moving in opposite direction)
		order.peggedChangeAmount(peggedChangeAmount);
		//whenever there is a price change of...
		order.referenceChangeAmount(referenceChangeAmount);
		//in the reference contract...
		order.referenceContractId(referenceConId);
		//being traded at...
		order.referenceExchangeId(referenceExchange);
		//starting reference price is...
		order.stockRefPrice(stockReferencePrice);
		//Keep order active as long as reference contract trades between...
		order.stockRangeLower(referenceContractLowerRange);
		//and...
		order.stockRangeUpper(referenceContractUpperRange);
		//! [pegged_benchmark]
		return order;
	}

	public static com.ib.client.Order AttachAdjustableToStop(com.ib.client.Order parent, double attachedOrderStopPrice, double triggerPrice, double adjustStopPrice) {
		//! [adjustable_stop]
		com.ib.client.Order order = new com.ib.client.Order();
		//Attached order is a conventional STP order in opposite direction
		order.action(parent.action().equals("BUY") ? "SELL" : "BUY");
		order.totalQuantity(parent.totalQuantity());
		order.auxPrice(attachedOrderStopPrice);
		order.parentId(parent.orderId());
		//When trigger price is penetrated
		order.triggerPrice(triggerPrice);
		//The parent order will be turned into a STP order
		order.adjustedOrderType(OrderType.STP);
		//With the given STP price
		order.adjustedStopPrice(adjustStopPrice);
		//! [adjustable_stop]
		return order;
	}

	public static com.ib.client.Order AttachAdjustableToStopLimit(com.ib.client.Order parent, double attachedOrderStopPrice, double triggerPrice, double adjustStopPrice, double adjustedStopLimitPrice) {
		//! [adjustable_stop_limit]
		com.ib.client.Order order = new com.ib.client.Order();
		//Attached order is a conventional STP order
		order.action(parent.action().equals("BUY") ? "SELL" : "BUY");
		order.totalQuantity(parent.totalQuantity());
		order.auxPrice(attachedOrderStopPrice);
		order.parentId(parent.orderId());
		//When trigger price is penetrated
		order.triggerPrice(triggerPrice);
		//The parent order will be turned into a STP LMT order
		order.adjustedOrderType(OrderType.STP_LMT);
		//With the given stop price
		order.adjustedStopPrice(adjustStopPrice);
		//And the given limit price
		order.adjustedStopLimitPrice(adjustedStopLimitPrice);
		//! [adjustable_stop_limit]
		return order;
	}

	public static PriceCondition PriceCondition(int conId, String exchange, double price, boolean isMore, boolean isConjunction) {
		//! [price_condition]
		//Conditions have to be created via the OrderCondition.Create
		PriceCondition priceCondition = (PriceCondition)OrderCondition.create(OrderConditionType.Price);
		//When this contract...
		priceCondition.conId(conId);
		//traded on this exchange
		priceCondition.exchange(exchange);
		//has a price above/below
		priceCondition.isMore(isMore);
		//this quantity
		priceCondition.price(price);
		//AND | OR next condition (will be ignored if no more conditions are added)
		priceCondition.conjunctionConnection(isConjunction);
		//! [price_condition]
		return priceCondition;
	}

	public static ExecutionCondition ExecutionCondition(String symbol, String secType, String exchange, boolean isConjunction) {
		//! [execution_condition]
		ExecutionCondition execCondition = (ExecutionCondition)OrderCondition.create(OrderConditionType.Execution);
		//When an execution on symbol
		execCondition.symbol(symbol);
		//at exchange
		execCondition.exchange(exchange);
		//for this secType
		execCondition.secType(secType);
		//AND | OR next condition (will be ignored if no more conditions are added)
		execCondition.conjunctionConnection(isConjunction);
		//! [execution_condition]
		return execCondition;
	}

	public static MarginCondition MarginCondition(int percent, boolean isMore, boolean isConjunction) {
		//! [margin_condition]
		MarginCondition marginCondition = (MarginCondition)OrderCondition.create(OrderConditionType.Margin);
		//If margin is above/below
		marginCondition.isMore(isMore);
		//given percent
		marginCondition.percent(percent);
		//AND | OR next condition (will be ignored if no more conditions are added)
		marginCondition.conjunctionConnection(isConjunction);
		//! [margin_condition]
		return marginCondition;
	}

	public static PercentChangeCondition PercentageChangeCondition(double pctChange, int conId, String exchange, boolean isMore, boolean isConjunction) {
		//! [percentage_condition]
		PercentChangeCondition pctChangeCondition = (PercentChangeCondition)OrderCondition.create(OrderConditionType.PercentChange);
		//If there is a price percent change measured against last close price above or below...
		pctChangeCondition.isMore(isMore);
		//this amount...
		pctChangeCondition.changePercent(pctChange);
		//on this contract
		pctChangeCondition.conId(conId);
		//when traded on this exchange...
		pctChangeCondition.exchange(exchange);
		//AND | OR next condition (will be ignored if no more conditions are added)
		pctChangeCondition.conjunctionConnection(isConjunction);
		//! [percentage_condition]
		return pctChangeCondition;
	}

	public static TimeCondition TimeCondition(String time, boolean isMore, boolean isConjunction) {
		//! [time_condition]
		TimeCondition timeCondition = (TimeCondition)OrderCondition.create(OrderConditionType.Time);
		//Before or after...
		timeCondition.isMore(isMore);
		//this time...
		timeCondition.time(time);
		//AND | OR next condition (will be ignored if no more conditions are added)
		timeCondition.conjunctionConnection(isConjunction);
		//! [time_condition]
		return timeCondition;
	}

	public static VolumeCondition VolumeCondition(int conId, String exchange, boolean isMore, int volume, boolean isConjunction) {
		//! [volume_condition]
		VolumeCondition volCon = (VolumeCondition)OrderCondition.create(OrderConditionType.Volume);
		//Whenever contract...
		volCon.conId(conId);
		//When traded at
		volCon.exchange(exchange);
		//reaches a volume higher/lower
		volCon.isMore(isMore);
		//than this...
		volCon.volume(volume);
		//AND | OR next condition (will be ignored if no more conditions are added)
		volCon.conjunctionConnection(isConjunction);
		//! [volume_condition]
		return volCon;
	}
}