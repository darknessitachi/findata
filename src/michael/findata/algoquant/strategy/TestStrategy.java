package michael.findata.algoquant.strategy;

import com.numericalmethod.algoquant.execution.component.broker.Broker;
import com.numericalmethod.algoquant.execution.component.tradeblotter.TradeBlotter;
import com.numericalmethod.algoquant.execution.datatype.depth.Depth;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import com.numericalmethod.algoquant.execution.datatype.order.Order;
import com.numericalmethod.algoquant.execution.strategy.handler.DepthHandler;
import michael.findata.algoquant.execution.component.broker.MetaBroker;
import michael.findata.algoquant.execution.datatype.order.HexinOrder;
import michael.findata.algoquant.execution.listener.OrderListener;
import michael.findata.algoquant.execution.strategy.Strategy;
import michael.findata.commandcenter.CommandCenter;
import michael.findata.external.tdx.TDXClient;
import michael.findata.model.Stock;
import michael.findata.spring.data.repository.StockRepository;
import michael.findata.util.DBUtil;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static michael.findata.util.LogUtil.getClassLogger;

public class TestStrategy implements Strategy, DepthHandler, OrderListener {
	private static Logger LOGGER = getClassLogger();
	private boolean executed = false;
	private StockRepository stockRepo;

	@Override
	public void onStop() {
	}

	@Override
	public void trySave() {
	}

	@Override
	public void setRepository(CrudRepository repository) {
	}

	@Override
	public CrudRepository getRepository () {
		return null;
	}

	@Override
	public Collection<Stock> getTargetSecurities() {
		return stockRepo.findByCodeIn("600000", "00914", "00966", "600048", "03606");
	}

	public TestStrategy (StockRepository sr) {
		stockRepo = sr;
	}

	@Override
	public void onDepthUpdate(DateTime now, Depth depth, MarketCondition mc, TradeBlotter blotter, Broker broker) {
		LOGGER.warn("\t{}\t: Irrelevant depth!! {} should not be passed to me!", this, depth);
		if (!executed) {
			List<Order> orders = new ArrayList<>();
			// 2 orders to default A share broker
			HexinOrder order1 = new HexinOrder(stockRepo.findOneByCode("600000"), 1000, 10, HexinOrder.HexinType.SIMPLE_BUY); // huatai*
			HexinOrder order2 = new HexinOrder(stockRepo.findOneByCode("600048"), 1000, 1000, HexinOrder.HexinType.SIMPLE_SELL); // huatai*
			orders.add(order1);
			orders.add(order2);
			broker.sendOrder(orders);
			orders.stream().filter(o -> broker instanceof michael.findata.algoquant.execution.component.broker.Broker).forEach(o -> (
					(michael.findata.algoquant.execution.component.broker.Broker) broker).setOrderListener(o, this)
			);
			broker.cancelOrder(orders);
			orders.clear();

			// 2 orders to default H share broker
			order1 = new HexinOrder(stockRepo.findOneByCode("00914"), 2000, 1, HexinOrder.HexinType.SIMPLE_BUY); // ib*
			order2 = new HexinOrder(stockRepo.findOneByCode("00966"), 2000, 1000, HexinOrder.HexinType.SIMPLE_SELL); // ib*
			orders.add(order1);
			orders.add(order2);
			broker.sendOrder(orders);
			orders.stream().filter(o -> broker instanceof michael.findata.algoquant.execution.component.broker.Broker).forEach(o -> (
					(michael.findata.algoquant.execution.component.broker.Broker) broker).setOrderListener(o, this)
			);
			broker.cancelOrder(orders);
			orders.clear();

			// 2 orders to broker tagged as "IB"
			order1 = new HexinOrder(stockRepo.findOneByCode("600048"), 3000, 1, HexinOrder.HexinType.SIMPLE_BUY); // ib*
			order2 = new HexinOrder(stockRepo.findOneByCode("03606"), 3200, 1000, HexinOrder.HexinType.SIMPLE_SELL); // ib*
			order1.addTag(MetaBroker.ORDER_TAG_BROKER, "IB");
			order2.addTag(MetaBroker.ORDER_TAG_BROKER, "IB");
			orders.add(order1);
			orders.add(order2);
			broker.sendOrder(orders);
			orders.stream().filter(o -> broker instanceof michael.findata.algoquant.execution.component.broker.Broker).forEach(o -> (
					(michael.findata.algoquant.execution.component.broker.Broker) broker).setOrderListener(o, this)
			);
			broker.cancelOrder(orders);
			orders.clear();

			// 2 order to broker tagged as "Huatai"
			order1 = new HexinOrder(stockRepo.findOneByCode("600000"), 4000, 1, HexinOrder.HexinType.SIMPLE_BUY); // huatai*
			order2 = new HexinOrder(stockRepo.findOneByCode("600660"), 1000, 1000, HexinOrder.HexinType.SIMPLE_SELL); // huatai*
			order1.addTag(MetaBroker.ORDER_TAG_BROKER, "Huatai");
			order2.addTag(MetaBroker.ORDER_TAG_BROKER, "Huatai");
			orders.add(order1);
			orders.add(order2);
			broker.sendOrder(orders);
			orders.stream().filter(o -> broker instanceof michael.findata.algoquant.execution.component.broker.Broker).forEach(o -> (
					(michael.findata.algoquant.execution.component.broker.Broker) broker).setOrderListener(o, this)
			);
			broker.cancelOrder(orders);
			orders.clear();

			// 2 A share order to broker tagged as "Unknown";
			order1 = new HexinOrder(stockRepo.findOneByCode("600016"), 6000, 1, HexinOrder.HexinType.SIMPLE_BUY); // huatai*
			order2 = new HexinOrder(stockRepo.findOneByCode("600104"), 1000, 100, HexinOrder.HexinType.SIMPLE_SELL); // huatai*
			order1.addTag(MetaBroker.ORDER_TAG_BROKER, "Unknown");
			order2.addTag(MetaBroker.ORDER_TAG_BROKER, "Unknown");
			orders.add(order1);
			orders.add(order2);
			broker.sendOrder(orders);
			orders.stream().filter(o -> broker instanceof michael.findata.algoquant.execution.component.broker.Broker).forEach(o -> (
					(michael.findata.algoquant.execution.component.broker.Broker) broker).setOrderListener(o, this)
			);
			broker.cancelOrder(orders);
			orders.clear();

			// 2 HK order to broker tagged as "to-be-determined"
			order1 = new HexinOrder(stockRepo.findOneByCode("01288"), 7000, 1, HexinOrder.HexinType.SIMPLE_BUY); // ib*
			order2 = new HexinOrder(stockRepo.findOneByCode("01288"), 7000, 1000, HexinOrder.HexinType.SIMPLE_SELL); // ib*
			order1.addTag(MetaBroker.ORDER_TAG_BROKER, "to-be-determined");
			order2.addTag(MetaBroker.ORDER_TAG_BROKER, "to-be-determined");
			orders.add(order1);
			orders.add(order2);
			broker.sendOrder(orders);
			orders.stream().filter(o -> broker instanceof michael.findata.algoquant.execution.component.broker.Broker).forEach(o -> (
					(michael.findata.algoquant.execution.component.broker.Broker) broker).setOrderListener(o, this)
			);
			broker.cancelOrder(orders);
			orders.clear();

			// 2 order to broker tagged as "Zhongxin";
			order1 = new HexinOrder(stockRepo.findOneByCode("600026"), 5000, 1, HexinOrder.HexinType.SIMPLE_BUY); // zhongxin
			order2 = new HexinOrder(stockRepo.findOneByCode("000338"), 5000, 1000, HexinOrder.HexinType.SIMPLE_SELL); // zhongxin
			order1.addTag(MetaBroker.ORDER_TAG_BROKER, "Zhongxin");
			order2.addTag(MetaBroker.ORDER_TAG_BROKER, "Zhongxin");
			orders.add(order1);
			orders.add(order2);
			broker.sendOrder(orders);
			orders.stream().filter(o -> broker instanceof michael.findata.algoquant.execution.component.broker.Broker).forEach(o -> (
					(michael.findata.algoquant.execution.component.broker.Broker) broker).setOrderListener(o, this)
			);
			broker.cancelOrder(orders);

			((MetaBroker)broker).printListeners();
			System.out.printf("Listener printed!!!\n");
		}
		executed = true;
	}

	@Override
	public void orderUpdated(Order order, Broker broker) {
		LOGGER.info("Order updated: {} from broker {}", order, broker);
	}

	public static void main (String args []) {

		Process dbProcess = DBUtil.tryToStartDB();

		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		CommandCenter cc = (CommandCenter) context.getBean("commandCenter");
		StockRepository stockRepo = (StockRepository) context.getBean("stockRepository");
		cc.setShSzHqClient(new TDXClient(TDXClient.TDXClientConfigs));
		cc.addStrategy(new TestStrategy(stockRepo));
		// Set up command center
		int hour = 23;
		int minute = 12;
		cc.setFirstHalfEndCN(new LocalTime(hour, minute, 10));
		cc.setSecondHalfStartCN(new LocalTime(hour, minute, 25));
		cc.setSecondHalfEndCN(new LocalTime(hour, minute, 59));
		cc.setFirstHalfEndHK(new LocalTime(hour, minute, 10));
		cc.setSecondHalfStartHK(new LocalTime(hour, minute, 30));
		cc.setSecondHalfEndHK(new LocalTime(hour, minute, 55));

		cc.start();
	}
}