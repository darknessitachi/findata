package michael.findata.algoquant.execution.component.broker;

import com.numericalmethod.algoquant.execution.component.tradeblotter.TradeBlotter;
import com.numericalmethod.algoquant.execution.datatype.depth.Depth;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import com.numericalmethod.algoquant.execution.datatype.execution.Execution;
import com.numericalmethod.algoquant.execution.datatype.order.Order;
import com.numericalmethod.algoquant.execution.strategy.handler.ExecutionHandler;
import michael.findata.algoquant.execution.component.depthprovider.DepthProvider;
import michael.findata.algoquant.execution.datatype.order.HexinOrder;
import michael.findata.algoquant.execution.listener.DepthListener;
import michael.findata.model.Stock;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static michael.findata.util.LogUtil.getClassLogger;

public class MetaBroker implements Broker, ExecutionHandler, DepthListener, DepthProvider{
	private static final Logger LOGGER = getClassLogger();
	private Broker huataiBroker;
	private Broker defaultHBroker;
	private Broker defaultABroker;

	private Map<Order, ExecutionHandler> orderListenerMap;
	private Map<String, Broker> brokerMap;
	private DepthListener depthListener;

	public MetaBroker (Stock ... subscriptions) {
		LocalInteractiveBrokers ib = new LocalInteractiveBrokers(4001, subscriptions);
		defaultHBroker = new DisruptorBrokerMarshaller(ib);
		defaultABroker = new DisruptorBrokerMarshaller(new LocalNativeTdxBroker("180.153.18.180", (short)7708, (short)2, "8009145070", "8009145070", LocalNativeTdxBroker.password, "123456"));
		huataiBroker = new DisruptorBrokerMarshaller(new LocalTdxBrokerProxy(10001));

//		defaultHBroker = ib;
//		defaultABroker = new LocalNativeTdxBroker("180.153.18.180", (short)7708, (short)2, "8009145070", "8009145070", LocalNativeTdxBroker.password, "123456");
//		huataiBroker = new LocalTdxBrokerProxy(10001);

		orderListenerMap = new HashMap<>();
		brokerMap = new HashMap<>();
		brokerMap.put("IB", defaultHBroker);
		brokerMap.put("Zhongxin", defaultABroker);
		brokerMap.put("Huatai", huataiBroker);
		ib.setDepthListener(this);
	}

	@Override
	public void sendOrder(Collection<? extends Order> orders) {
		ArrayList<Order> ordersAShare = new ArrayList<>();
		ArrayList<Order> ordersHKShare = new ArrayList<>();
		Map<String, Collection<Order>> splittedOrders = new HashMap<>();
		splitOrders(orders, ordersAShare, ordersHKShare, splittedOrders);
		for(Map.Entry<String, Collection<Order>> entry : splittedOrders.entrySet()) {
			Broker broker = brokerMap.get(entry.getKey());
			Collection<Order> odrs = entry.getValue();
			broker.sendOrder(odrs);
			odrs.forEach(order -> broker.setOrderListener(order, MetaBroker.this));
		}
		if (!ordersAShare.isEmpty()) {
			defaultABroker.sendOrder(ordersAShare);
			ordersAShare.forEach(order -> defaultABroker.setOrderListener(order, MetaBroker.this));
		}
		if (!ordersHKShare.isEmpty()) {
			defaultHBroker.sendOrder(ordersHKShare);
			ordersHKShare.forEach(order -> defaultHBroker.setOrderListener(order, MetaBroker.this));
		}
//		for (Order o : orders) {
//			orderMap.put(o.id(), o);
//		}
	}

	@Override
	public void cancelOrder(Collection<? extends Order> orders) {
		ArrayList<Order> ordersAShare = new ArrayList<>();
		ArrayList<Order> ordersHKShare = new ArrayList<>();
		Map<String, Collection<Order>> splittedOrders = new HashMap<>();
		splitOrders(orders, ordersAShare, ordersHKShare, splittedOrders);
		for(Map.Entry<String, Collection<Order>> entry : splittedOrders.entrySet()) {
			brokerMap.get(entry.getKey()).cancelOrder(entry.getValue());
		}
		if (!ordersAShare.isEmpty()) {
			defaultABroker.cancelOrder(ordersAShare);
		}
		if (!ordersHKShare.isEmpty()) {
			defaultHBroker.cancelOrder(ordersHKShare);
		}
	}

	private void splitOrders(Collection<? extends Order> orders, Collection<Order> ordersAShare, Collection<Order> ordersHKShare, Map<String, Collection<Order>> splittedOrders ) {
		for (Order order : orders) {
			String brokerTag = (String) order.getTag(HexinOrder.ORDER_TAG_BROKER);
			if (brokerTag != null) {
				Broker broker = brokerMap.get(brokerTag);
				if (broker != null) {
					Collection<Order> brokerOrders = splittedOrders.get(brokerTag);
					if (brokerOrders == null) {
						brokerOrders = new ArrayList<>();
						splittedOrders.put(brokerTag, brokerOrders);
					}
					LOGGER.debug("Added(0) to {}: {}", brokerTag, ((Stock) order.product()).getCode());
					brokerOrders.add(order);
					continue;
				}
			}
			if (order.product() instanceof Stock) {
				switch (((Stock) order.product()).exchange()) {
					case HKEX:
						LOGGER.debug("Added(1) to default HK: {}", ((Stock) order.product()).getCode());
						ordersHKShare.add(order);
						break;
					case SHSE:
					case SZSE:
						LOGGER.debug("Added(1) to default A: {}", ((Stock) order.product()).getCode());
						ordersAShare.add(order);
				}
			} else {
				String symbol = order.product().symbol();
				if (symbol.endsWith("HK")) {
					LOGGER.debug("Added(2) to default HK: {}", ((Stock) order.product()).getCode());
					ordersHKShare.add(order);
				} else if (symbol.endsWith("SH") || symbol.endsWith("SS") || symbol.endsWith("SZ")) {
					LOGGER.debug("Added(2) to default A: {}", ((Stock) order.product()).getCode());
					ordersAShare.add(order);
				}
			}
		}
	}

	public void stop () {
		brokerMap.values().forEach(Broker::stop);
	}

	@Override
	public void setOrderListener(Order o, ExecutionHandler handler) {
		orderListenerMap.put(o, handler);
		LOGGER.debug("Meta orderListenerMap put: {} -> {}", o, handler);
	}

	public void printOrderListeners() {
		orderListenerMap.entrySet().forEach(entry -> {
			LOGGER.info("Meta broker: {} -> {}", entry.getKey(), entry.getValue());
		});
		defaultHBroker.printOrderListeners();
	}

	@Override
	public void depthUpdated(Depth depth) {
		if (depthListener != null) {
			depthListener.depthUpdated(depth);
		}
	}

	@Override
	public void setDepthListener(DepthListener listener) {
		this.depthListener = listener;
	}

	@Override
	public void onExecution(DateTime dt, Execution execution, MarketCondition mc, TradeBlotter blotter,
							com.numericalmethod.algoquant.execution.component.broker.Broker broker) {
		Order order = execution.order();
		ExecutionHandler handler = orderListenerMap.get(order);
		if (handler == null) {
			LOGGER.info("No listener found for order {}", order);
			return;
		}
		LOGGER.info("notifying listener {} for order {}", handler, order);
		handler.onExecution(dt, execution, mc, blotter, this);
	}
}