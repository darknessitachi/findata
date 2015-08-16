package michael.findata.algoquant.strategy;

import com.numericalmethod.algoquant.execution.component.broker.Broker;
import com.numericalmethod.algoquant.execution.component.tradeblotter.TradeBlotter;
import com.numericalmethod.algoquant.execution.datatype.StockEOD;
import com.numericalmethod.algoquant.execution.datatype.depth.Depth;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import com.numericalmethod.algoquant.execution.datatype.order.MarketOrder;
import com.numericalmethod.algoquant.execution.datatype.order.Order;
import com.numericalmethod.algoquant.execution.datatype.product.Product;
import com.numericalmethod.algoquant.execution.strategy.Strategy;
import com.numericalmethod.algoquant.execution.strategy.handler.DepthHandler;
import com.numericalmethod.algoquant.execution.strategy.handler.StockEODHandler;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Collections;

/**
 * This strategy tries to re-balanced cash/equity ration of 50/50 whenever the imbalance is over the thresholds
 * Created by nicky on 2015/8/16.
 */
public class FixedPositionStrategy implements Strategy, DepthHandler, StockEODHandler {

	private final double startingCash = 1000000;
	private double currentCash = startingCash;
	private double thresholdDelta = .05;
	private double balancePoint = .50;
//	private final int startingStock = 0;
//	private int currentStock = startingStock;
	private final Product product;
	private Double previousPrice = null;

	public FixedPositionStrategy(Product product) {
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
			Collection<Order> orders = computeOrders(now, currentPrice, blotter.position(product));
			broker.sendOrder(orders);
		}
		previousPrice = currentPrice;
	}

	private Collection<Order> computeOrders(DateTime now, double currentPrice, double position) {
		double equity = currentPrice * position;
		double total = currentCash + equity;
		double equityPosition = equity / total;
		double delta = equityPosition - balancePoint;
		double qty;
		if (delta >  thresholdDelta || delta < - thresholdDelta) {
			// buy or sell
			qty = -(delta * total) / currentPrice;
			qty = Math.ceil(qty/100)*100;

			if (qty == 0.) {
				return Collections.emptySet();
			}

			System.out.println(now);
			System.out.println("Eqty "+equity);
			System.out.println("Cash "+currentCash);
			System.out.println("Eqty "+equityPosition*100+"%");
			System.out.println(qty+"@"+currentPrice+"="+qty*currentPrice);
			System.out.println();

			currentCash -= qty*currentPrice;
		} else {
			// do nothing
			return Collections.emptySet();
		}
		return Collections.<Order>singletonList(new MarketOrder(product, qty));
	}

	@Override
	public void onEODUpdate(DateTime now,
							StockEOD eod,
							MarketCondition mc,
							TradeBlotter blotter,
							Broker broker) {
//		System.out.println("onEODUpdate: now "+now);
//		System.out.println("onEODUpdate: adc "+eod.adjClose());
//		double currentPrice = eod.low();
//		if (previousPrice != null) {
//			Collection<Order> orders = computeOrders(currentPrice, blotter.position(product));
//			broker.sendOrder(orders);
//		}
//		previousPrice = currentPrice;
	}
}