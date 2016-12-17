package michael.findata.algoquant.execution.component.broker;

import com.numericalmethod.algoquant.execution.datatype.depth.Depth;
import com.numericalmethod.algoquant.execution.datatype.order.Order;
import com.numericalmethod.algoquant.execution.datatype.product.stock.Exchange;
import michael.findata.algoquant.execution.component.depthprovider.DepthProvider;
import michael.findata.algoquant.execution.listener.DepthListener;
import michael.findata.algoquant.execution.listener.OrderListener;
import michael.findata.model.Stock;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.numericalmethod.nmutils.NMUtils.getClassLogger;

public class MetaBroker implements Broker, OrderListener, DepthListener, DepthProvider{
	private static final Logger LOGGER = getClassLogger();
	private LocalTdxBrokerProxy tdxBroker;
	private LocalInteractiveBrokers iBrokers;
	private LocalNativeTdxBroker zhongxinBrokers;
	private Map<Long, Order> orderMap;
	private Map<Long, OrderListener> orderListenerMap;
	private Map<String, Broker> brokerMap;
	private DepthListener depthListener;
	public static final String ORDER_TAG_BROKER = "Broker";

	public MetaBroker (Stock ... subscriptions) {
		tdxBroker = new LocalTdxBrokerProxy(10001);
		iBrokers = new LocalInteractiveBrokers(4001, subscriptions);
		zhongxinBrokers = new LocalNativeTdxBroker("180.153.18.180", (short)7708, (short)2, "8009145070", "8009145070", "495179", "495179");
		orderMap = new HashMap<>();
		orderListenerMap = new HashMap<>();
		brokerMap = new HashMap<>();
		brokerMap.put("IB", iBrokers);
		brokerMap.put("Huatai", tdxBroker);
		brokerMap.put("Zhongxin", zhongxinBrokers);
	}

	@Override
	public void sendOrder(Collection<? extends Order> orders) {
		ArrayList<Order> ordersAShare = new ArrayList<>();
		ArrayList<Order> ordersHKShare = new ArrayList<>();
		Map<String, Collection<Order>> splittedOrders = new HashMap<>();
		splitOrders(orders, ordersAShare, ordersHKShare, splittedOrders);
		for(Map.Entry<String, Collection<Order>> entry : splittedOrders.entrySet()) {
			brokerMap.get(entry.getKey()).sendOrder(entry.getValue());
		}
		if (!ordersAShare.isEmpty()) {
			tdxBroker.sendOrder(ordersAShare);
		}
		if (!ordersHKShare.isEmpty()) {
			iBrokers.sendOrder(ordersHKShare);
			for (Order o : orders) {
				orderMap.put(o.id(), o);
			}
		}
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
			tdxBroker.cancelOrder(ordersAShare);
		}
		if (!ordersHKShare.isEmpty()) {
			iBrokers.cancelOrder(ordersHKShare);
		}
	}

	private void splitOrders(Collection<? extends Order> orders, Collection<Order> ordersAShare, Collection<Order> ordersHKShare, Map<String, Collection<Order>> splittedOrders ) {
		for (Order order : orders) {
			String brokerTag = (String) order.getTag(ORDER_TAG_BROKER);
			if (brokerTag != null) {
				Broker broker = brokerMap.get(brokerTag);
				if (broker != null) {
					Collection<Order> brokerOrders = splittedOrders.get(brokerTag);
					if (brokerOrders == null) {
						brokerOrders = new ArrayList<>();
						splittedOrders.put(brokerTag, brokerOrders);
					}
					brokerOrders.add(order);
					continue;
				}
			}
			if (order.product() instanceof Stock) {
				switch (((Stock) order.product()).exchange()) {
					case HKEX:
						LOGGER.info("Added(1) to HK: {}", ((Stock) order.product()).getCode());
						ordersHKShare.add(order);
						break;
					case SHSE:
					case SZSE:
						LOGGER.info("Added(1) to A: {}", ((Stock) order.product()).getCode());
						ordersAShare.add(order);
				}
			} else {
				String symbol = order.product().symbol();
				if (symbol.endsWith("HK")) {
					LOGGER.info("Added(2) to HK: {}", ((Stock) order.product()).getCode());
					ordersHKShare.add(order);
				} else if (symbol.endsWith("SH") || symbol.endsWith("SS") || symbol.endsWith("SZ")) {
					LOGGER.info("Added(2) to A: {}", ((Stock) order.product()).getCode());
					ordersAShare.add(order);
				}
			}
		}
	}

	public void stop () {
		iBrokers.stop();
		tdxBroker.stop();
	}

	@Override
	public void setOrderListener(Order o, OrderListener listener) {
		if (orderMap.containsValue(o)) {
			orderListenerMap.put(o.id(), listener);
			iBrokers.setOrderListener(o, this);
		}
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
	public void orderUpdated(Order order, com.numericalmethod.algoquant.execution.component.broker.Broker broker) {
		OrderListener listener = orderListenerMap.get(order.id());
		if (listener == null) {
			LOGGER.info("No listener found for order {}", order);
			return;
		}
		LOGGER.info("notifying listener {} for order {}", listener, order);
		listener.orderUpdated(order, this);
	}
}