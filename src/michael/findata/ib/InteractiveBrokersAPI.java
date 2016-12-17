package michael.findata.ib;

import com.ib.client.*;

import java.io.IOException;
import java.util.Set;

public class InteractiveBrokersAPI implements EWrapper {
	protected final EJavaSignal m_signal = new EJavaSignal();
	protected final EClientSocket m_s = new EClientSocket(this, m_signal);

	private void run() {
		// this thread is in charge of start / stop the client
		m_s.eConnect("127.0.0.1", 4001, 0);
		final EReader reader = new EReader(m_s, m_signal);
		reader.start();

		new Thread() {
			public void run() {
				while (m_s.isConnected()) {
					m_signal.waitForSignal();
//					try {
//						javax.swing.SwingUtilities
//								.invokeAndWait(new Runnable() {
//									@Override
//									public void run() {
//										try {
//											reader.processMsgs();
//										} catch (IOException e) {
//											error(e);
//										}
//									}
//								});
//					} catch (Exception e) {
//						error(e);
//					}

					try {
						reader.processMsgs();
					} catch (Exception e) {
						System.out.println("Exception: "+e.getMessage());
					}
				}
			}
		}.start();

//		m_s.reqSecDefOptParams(0, "IBM", "",/* "",*/ "STK", 8314);



		Contract contract_CNH_HKD = new Contract();
		contract_CNH_HKD.exchange("IDEALPRO");
		contract_CNH_HKD.symbol("CNH");
		contract_CNH_HKD.currency("HKD");
		contract_CNH_HKD.secType("CASH");
//		m_s.reqMktData(1001, contract_CNH_HKD, "", false, null); // request CNH.HKD
		m_s.reqHistoricalData(1002, contract_CNH_HKD, "20150118 23:59:59 GMT", "365 D", "1 day", "MIDPOINT", 1, 1, null);
//		Order order = OrderSamples.LimitOrder("SELL", 30000, 1.1265);
//		order.tif(Types.TimeInForce.GTC);
//		m_s.placeOrder(validId+1, contract_CNH_HKD, order);

		Contract contract_914 = new Contract();
		contract_914.exchange("SEHK");
		contract_914.secType("STK");
		contract_914.symbol("914");
//		m_s.reqMktData(4001, contract_914, "", false, null);
//		m_s.reqHistoricalData(4002, contract_914, "20161105 23:59:59 GMT", "1 D", "1 min", "TRADES", 1, 1, null);


		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_s.eDisconnect();
	}

	@Override
	public void nextValidId(int orderId) {
	}

	@Override
	public void error(Exception e) {
		System.out.println("Error: "+e.getMessage());
	}

	@Override
	public void error(int id, int errorCode, String errorMsg) {
	}

	@Override
	public void connectionClosed() {
	}

	@Override
	public void error(String str) {
		System.out.println("Error: "+str);
	}

	@Override
	public void tickPrice(int tickerId, int field, double price, int canAutoExecute) {
	}

	@Override
	public void tickSize(int tickerId, int field, int size) {
	}

	@Override
	public void tickOptionComputation(int tickerId, int field, double impliedVol, double delta, double optPrice, double pvDividend, double gamma, double vega, double theta, double undPrice) {
	}

	@Override
	public void tickGeneric(int tickerId, int tickType, double value) {
	}

	@Override
	public void tickString(int tickerId, int tickType, String value) {
	}

	@Override
	public void tickEFP(int tickerId, int tickType, double basisPoints, String formattedBasisPoints, double impliedFuture, int holdDays, String futureLastTradeDate, double dividendImpact,
						double dividendsToLastTradeDate) {
	}

	@Override
	/**
	 * Gives the up-to-date information of an order every time it changes.
	 *
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
	 * @return
	 */
	public void orderStatus(int orderId, String status, double filled, double remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice, int clientId, String whyHeld) {
	}

	@Override
	public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
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
	}

	@Override
	public void securityDefinitionOptionalParameterEnd(int reqId) {
	}

	@Override
	public void softDollarTiers(int reqId, SoftDollarTier[] tiers) {
	}
}
