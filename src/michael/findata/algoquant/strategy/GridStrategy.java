package michael.findata.algoquant.strategy;

import com.numericalmethod.algoquant.execution.component.broker.Broker;
import com.numericalmethod.algoquant.execution.component.tradeblotter.TradeBlotter;
import com.numericalmethod.algoquant.execution.datatype.depth.Depth;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import com.numericalmethod.algoquant.execution.datatype.order.MarketOrder;
import com.numericalmethod.algoquant.execution.datatype.order.Order;
import com.numericalmethod.algoquant.execution.datatype.product.Product;
import com.numericalmethod.algoquant.execution.strategy.Strategy;
import com.numericalmethod.algoquant.execution.strategy.handler.DepthHandler;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by nicky on 2015/8/12.
 */
public class GridStrategy implements Strategy, DepthHandler {

	private final double scale = 1;
	private final Product product;
	private Double previousPrice = null;

	public GridStrategy(Product product) {
		this.product = product;
	}

	@Override
	public void onDepthUpdate(DateTime now,
							  Depth depth,
							  MarketCondition mc,
							  TradeBlotter blotter,
							  Broker broker) {
		double currentPrice = depth.mid();
		if (previousPrice != null) {
			Collection<Order> orders = computeOrders(currentPrice, blotter.position(product));
			broker.sendOrder(orders);
		}
		previousPrice = currentPrice;
	}

	private Collection<Order> computeOrders(double currentPrice, double position) {
		double qty = 0.;
		if (currentPrice >= previousPrice) {
			qty = scale - position;
		} else if (currentPrice < previousPrice) {
			qty = -scale - position;
		}

		if (qty == 0.) {
			return Collections.emptySet();
		}

		return Collections.<Order>singletonList(new MarketOrder(product, qty));
	}
}