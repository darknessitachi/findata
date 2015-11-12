package michael.findata.external.netease.test;

import com.numericalmethod.algoquant.data.cache.SequentialCache;
import com.numericalmethod.algoquant.data.cache.TimedEntry;
import com.numericalmethod.algoquant.execution.datatype.depth.Depth;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import com.numericalmethod.algoquant.execution.datatype.order.LimitOrder;
import com.numericalmethod.algoquant.execution.datatype.order.Order;
import com.numericalmethod.algoquant.execution.datatype.product.Product;
import michael.findata.algoquant.execution.component.broker.HexinBroker;
import michael.findata.algoquant.product.stock.shse.SHSEStock;
import michael.findata.algoquant.product.stock.szse.SZSEStock;
import michael.findata.external.netease.NeteaseInstantSnapshotFactory;
import org.slf4j.Logger;

import java.util.*;

import static com.numericalmethod.algoquant.execution.datatype.order.BasicOrderDescription.Side.*;
import static com.numericalmethod.nmutils.NMUtils.getClassLogger;

/**
 * Created by nicky on 2015/8/23.
 */
public class NeteaseInstantSnapshotFactoryTest {
	private static final Logger LOGGER = getClassLogger();
	public static void main (String [] args) {

		long lastSilentMilli = System.currentTimeMillis();
		HexinBroker hxBroker = new HexinBroker("网上股票交易系统5.0", false);

		LimitOrder [] orders = new LimitOrder[] {

			new LimitOrder(new SZSEStock("000568.SZ"), BUY, 400, 24.02)
//			,new LimitOrder(new SZSEStock("000568.SZ"), SELL, 400, 24.98)
			,new LimitOrder(new SHSEStock("600104.SS"), BUY, 500, 19.28)
			,new LimitOrder(new SZSEStock("000338.SZ"), BUY, 1000, 9.01)
//			,new LimitOrder(new SHSEStock("600104.SS"), BUY, 500, 20)
		};
		HashSet<Product> stocks = new HashSet<>();
		for (LimitOrder o: orders) {
			stocks.add(o.product());
		}
		SequentialCache<MarketCondition> c = new NeteaseInstantSnapshotFactory().newInstance(stocks.toArray(new Product[stocks.size()]));
		Depth depth;
		Iterator<TimedEntry<MarketCondition>> i = c.iterator();
		while (i.hasNext()) {
			Map<Product, Depth> depths;
			try {
				depths = i.next().data().depths();
			} catch (NullPointerException npe) {
				LOGGER.info("NPE Caught!");
				continue;
			}
			if ((System.currentTimeMillis() % 86400000) < 5400000 || 25200000 < (System.currentTimeMillis() % 86400000)) {
				// Make sure time is between 9:30 AM and 3:00PM
				LOGGER.info("Not during trading hours (between 9:30 AM and 3:00PM), skipping...");
				continue;
			}
			for (LimitOrder o : orders) {
				if (Order.OrderState.UNFILLED.equals(o.state())) {
					depth = depths.get(o.product());
//					LOGGER.info("Depth: " + depth);
					if (BUY.equals(o.side()) && depth.ask(1) <= o.price() && depth.ask(1) != 0.0) {
						LOGGER.info("Depth: " + depth);
						hxBroker.sendOrder(new LimitOrder(o.product(), BUY, o.quantity(), depth.ask(1)));
						o.markAsFilled();
					} else if (SELL.equals(o.side()) && depth.bid(1) >= o.price() && depth.bid(1) != 0.0) {
						LOGGER.info("Depth: " + depth);
						hxBroker.sendOrder(new LimitOrder(o.product(), SELL, o.quantity(), depth.bid(1)));
						o.markAsFilled();
					}
				}
			}
		}
	}
}