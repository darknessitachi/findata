package michael.findata.algoquant.strategy;

import com.numericalmethod.algoquant.execution.component.broker.Broker;
import com.numericalmethod.algoquant.execution.component.tradeblotter.TradeBlotter;
import com.numericalmethod.algoquant.execution.datatype.depth.Depth;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import com.numericalmethod.algoquant.execution.datatype.order.Order;
import com.numericalmethod.algoquant.execution.datatype.order.OrderUtils;
import com.numericalmethod.algoquant.execution.datatype.product.Product;
import com.numericalmethod.algoquant.execution.datatype.product.basket.BasketUtils;
import com.numericalmethod.algoquant.execution.strategy.PlottableSignals;
import com.numericalmethod.algoquant.execution.strategy.Strategy;
import com.numericalmethod.algoquant.execution.strategy.handler.DepthHandler;
import com.numericalmethod.algoquant.model.elliott2005.offline.SSOfflineSmoother;
import com.numericalmethod.algoquant.model.elliott2005.strategy.Elliott2005Strategy;
import com.numericalmethod.algoquant.model.util.movingwindow.MovingWindowOfSynchronousPriceBasket;
import com.numericalmethod.suanshu.algebra.linear.vector.doubles.Vector;
import com.numericalmethod.suanshu.stats.dlm.univariate.LinearKalmanFilter;
import org.joda.time.DateTime;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.numericalmethod.nmutils.NMUtils.getClassLogger;
import static com.numericalmethod.nmutils.collection.CollectionUtils.newHashMap;
import static java.lang.Math.abs;


/**
 * This prototype is implemented according to the original paper, well, more or less.
 *
 * @author Haksun Li
 */
public class Elliott2005 implements Strategy, PlottableSignals, DepthHandler { // TODO: separate signal from strategy

	private static final Logger LOGGER = getClassLogger();
	private final Elliott2005Strategy.Customization customization;
	private final List<Product> products = new ArrayList<>();
	private final MovingWindowOfSynchronousPriceBasket window;
	private final double epsilon = 0.01;
	private double mu;
	private double xt;
	private double zt;
	// Michael
	private double mRatio = 0;
	// End Michael

	public Elliott2005(List<? extends Product> products,
							   Elliott2005Strategy.Customization customization,
							   int calibrationWindow) {
		this.customization = customization;
		this.products.addAll(products);
		this.window = new MovingWindowOfSynchronousPriceBasket(products, calibrationWindow);
	}

	@Override
	public void onDepthUpdate(DateTime now,
							  Depth depth,
							  MarketCondition mc,
							  TradeBlotter blotter,
							  Broker broker) {
		// track moving basket prices
		if (!window.updateDepth(now, depth)) {
			return; // not all prices have arrived yet for, e.g., today
		}

		// not enough data point to fill the window
		if (!window.isReady()) {
			return;
		}

		// calibrate beta for the basket
		double[][] ts = window.getRef();
		System.out.println("Date: "+now);
		System.out.println("price: "+depth.mid());
		System.out.println("price1: " + ts[ts.length-1][0]);
		System.out.println("price2: " + ts[ts.length-1][1]);
		mRatio = ts[ts.length-1][0]/ts[ts.length-1][1];
//        // scale the prices
//        for (int t = 1; t < ts.length; ++t) {
//            for (int i = 0; i < ts[0].length; ++i) {
//                ts[t][i] /= ts[0][i];
//            }
//        }

		Vector beta = customization.beta(now, ts);
		if (beta == null) {
			return; // cannot trade yet without a good calibration
		}

		// compute the historical basket price/spread
		double[] z = BasketUtils.spread(beta.toArray(), ts);

		// Elliott's 2005 model for true mean estimation
		SSOfflineSmoother model1 = new SSOfflineSmoother(z, 1, 0.5, 1, 1, 200);
		LinearKalmanFilter KF = new LinearKalmanFilter(model1);
		KF.filtering(z);

		this.mu = model1.mu(); // long term mean
		this.xt = KF.getFittedState(z.length); // current hidden state (true price) estimation
		this.zt = z[z.length - 1]; // current observation (published price)

		if (!isMuValid()) {
			return; // invalid mu estimation
		}

//         int holdingTime = (int) model1.holdingTimeByThreshold(1.05 * mu);
		LOGGER.info(String.format("recalibration of strategy @ %s: beta = %s, mu = %f, xt = %f, zt = %f",
				now.toString(),
				beta.toString(),
				mu,
				xt,
				zt));

		// create orders from signals, if any
		List<Order> orders = new ArrayList<Order>();

		Collection<Order> sell = customization.getSellOrders(mu, xt, z, products, blotter); // sell signal
		orders.addAll(sell);

		if (orders.isEmpty()) {
			Collection<Order> buy = customization.getBuyOrders(mu, xt, z, products, blotter); // buy signal
			orders.addAll(buy);
		}

		if (orders.isEmpty()) {
			if (customization.isToUnwind(mu, xt, z, products, blotter)) { // unwind position
				Collection<Order> close = OrderUtils.newClosingOrders(blotter.positions(), epsilon);
				orders.addAll(close);
			}
		}

		orders = OrderUtils.filterOutSmallOrders(orders, epsilon); // remove tiny orders

		if (!orders.isEmpty()) {
			broker.sendOrder(orders);
		}

	}

	// avoid acting/trading on mu that is too far away from the observed price
	private boolean isMuValid() {
		return !(abs((zt - mu) / zt) > 2); // zt is always right because it is merely an "observation"
	}

	@Override
	public String toString() {
		return String.format("%s: trading %s vs. %s; calibration window = %d",
				this.getClass().getSimpleName(),
				products.get(0),
				products.get(1),
				window.size());
	}

	@Override
	public Map<String, Double> plottableValues() {
		Map<String, Double> signalValues = newHashMap();
//		signalValues.put("Z", zt);
//		signalValues.put("equilibrium", xt);
//		signalValues.put("long term mean", mu);
		// Michael
		signalValues.put("michael's ratio", mRatio);
		// End Michael
		return signalValues;
	}

	@Override
	public boolean areSignalsReady() {
		return isMuValid() && window.isReady();
	}
}
