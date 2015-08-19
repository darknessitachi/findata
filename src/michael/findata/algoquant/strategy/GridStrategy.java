package michael.findata.algoquant.strategy;

import com.numericalmethod.algoquant.execution.component.broker.Broker;
import com.numericalmethod.algoquant.execution.component.tradeblotter.TradeBlotter;
import com.numericalmethod.algoquant.execution.datatype.StockEOD;
import com.numericalmethod.algoquant.execution.datatype.depth.Depth;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import com.numericalmethod.algoquant.execution.datatype.order.BasicOrderDescription;
import com.numericalmethod.algoquant.execution.datatype.order.LimitOrder;
import com.numericalmethod.algoquant.execution.datatype.order.MarketOrder;
import com.numericalmethod.algoquant.execution.datatype.order.Order;
import com.numericalmethod.algoquant.execution.datatype.product.Product;
import com.numericalmethod.algoquant.execution.strategy.Strategy;
import com.numericalmethod.algoquant.execution.strategy.handler.DepthHandler;
import com.numericalmethod.algoquant.execution.strategy.handler.StockEODHandler;
import org.joda.time.DateTime;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static com.numericalmethod.nmutils.NMUtils.getClassLogger;

/**
 * Created by nicky on 2015/8/12.
 */
public class GridStrategy implements Strategy, DepthHandler, StockEODHandler {

	private static class Slot {
		public double buy_ceiling;
		public double sell_floor;
		public int serial;
		public double paid_price;
		public int number_shares = 0;
		public DateTime buying_date;

		Slot(double buy_ceiling, double sell_floor, double paid_price, int number_shares, int serial, DateTime buying_date) {
			this.buy_ceiling = buy_ceiling;
			this.sell_floor = sell_floor;
			this.paid_price = paid_price;
			this.number_shares = number_shares;
			this.serial = serial;
			this.buying_date = buying_date;
		}

		public double getInvestment(){
			return number_shares*paid_price*(1+0.0008f);
		}

		public double getSellTotal(double selling_price) {
			return (selling_price*number_shares)*(1-0.0018f);
		}
	}
	private static final Logger LOGGER = getClassLogger();

	// config that is not likely to be changed
	private double deltaPctg; // selling price gap between each slot
	private double waitThreshold; // wait till we can buy below this threshold
	private final Product product;
	private double historyMax = 0; // historical max of this stock
	private double baseUnitAmount = 10000; // the baseline amount for a slot

	private DateTime startDate = null;
	private ArrayList<Slot> slots = new ArrayList<>();
	private ArrayList<Slot> slotsToBuy = new ArrayList<>();
	private ArrayList<Slot> slotsToSell = new ArrayList<>();
	private DateTime lastDate;
	private double investment; //money * time cost
	private double cashProfit;
	private boolean waiting = true;

	public GridStrategy(Product product, double deltaPctg, double waitThreshold) {
		this(product, deltaPctg, waitThreshold, 0);
	}

	public GridStrategy(Product product, double deltaPctg, double waitThreshold, double historyMax) {
		this(product, deltaPctg, waitThreshold, historyMax, 10000d);
	}

	public GridStrategy(Product product, double deltaPctg, double waitThreshold, double historyMax, double baseUnitAmount) {
		this.product = product;
		this.deltaPctg = deltaPctg;
		this.waitThreshold = waitThreshold;
		this.historyMax = historyMax;
		this.baseUnitAmount = baseUnitAmount;
	}

	private void allocateSlots() {
		double p = historyMax;
		int serial = 1;
		slots.clear();
		while (p > 1f) {
			slots.add(new Slot(Math.min(Math.round(p / deltaPctg * 100) / 100f, historyMax * waitThreshold), Math.round(p * 100) / 100f, 0, 0, serial, null));
			System.out.printf("No. %d\tS.Floor: %.2f\tB.Ceiling: %.2f\n",
					slots.get(slots.size() - 1).serial,
					slots.get(slots.size() - 1).sell_floor,
					slots.get(slots.size() - 1).buy_ceiling);
			p = p / deltaPctg;
			serial ++;
		}
	}

	private double getInvestedCash(ArrayList<Slot> slots) {
		return slots.stream().mapToDouble(Slot::getInvestment).sum();
	}

	@Override
	public void onDepthUpdate(DateTime now,
							  Depth depth,
							  MarketCondition mc,
							  TradeBlotter blotter,
							  Broker broker) {
//		broker.sendOrder(computeOrders(now, depth.ask(0), depth.ask(0), depth.bid(0), depth.bid(0)));
	}

	@Override
	public void onEODUpdate(DateTime now,
							StockEOD eod,
							MarketCondition mc,
							TradeBlotter blotter,
							Broker broker) {
		double low = eod.adjClose()/eod.close()*eod.low();
		double high = eod.adjClose()/eod.close()*eod.high();
		double bestAsk = low;
		double worstAsk = high;
		double bestBid = high;
		double worstBid = low;
		broker.sendOrder(computeOrders(now, bestAsk, worstAsk, bestBid, worstBid));
	}

	private Collection<Order> computeOrders(double currentPrice, double position) {
		double qty = 0.;

		if (position == 0.) {
			qty = 100;
		} else {
			return Collections.emptySet();
		}

		return Collections.<Order>singletonList(new MarketOrder(product, qty));
	}
	private Collection<Order> computeOrders(DateTime now, double bestAsk, double worstAsk, double bestBid, double worstBid) {
		Slot pTemp;
		slotsToBuy.clear();
		slotsToSell.clear();

		// Update history high
		if (historyMax < bestBid) {
			System.out.println(now);
			System.out.printf("historic high updated: %.2f\n",bestBid);
			historyMax = bestBid;
		}
		if ((bestAsk < historyMax * waitThreshold) && waiting) {
			allocateSlots();
			waiting = false;
		}
		if (waiting) {
			return Collections.<Order>emptySet();
		}
		// Calculate money * time cost first;
		DateTime currentDate = now;
		if (lastDate == null) {
			investment = 0;
			startDate = now;
		} else {
			investment += getInvestedCash(slots)*((currentDate.getMillis()-lastDate.getMillis())/1000/60/60/24);
		}
		lastDate = currentDate;

		// First find out what slots can be bought
		int i = 0;
		for (; i < slots.size(); i ++) {
			pTemp = slots.get(i);
			if (pTemp.buy_ceiling >= bestAsk) {
				if (pTemp.number_shares <= 0) {
					slotsToBuy.add(pTemp);
				}
			} else {
				break;
			}
		}

		// Find out what slots can be sold
		for (i = slots.size() - 1; i > -1; i --) {
			pTemp = slots.get(i);
			if (pTemp.sell_floor <= bestBid) {
				if (pTemp.number_shares > 0) {
					slotsToSell.add(pTemp);
				}
			} else {
				break;
			}
		}

		if (slotsToBuy.size()+slotsToSell.size() > 0) {
			System.out.println(currentDate);
		}

		ArrayList<Order> orders = new ArrayList<>(slotsToBuy.size() + slotsToSell.size());
		// Buy
		slotsToBuy.stream().forEach(slot -> {
			slot.paid_price = worstAsk > slot.buy_ceiling ? slot.buy_ceiling : worstAsk;
			slot.number_shares = (int) (Math.ceil(baseUnitAmount / slot.paid_price / 100) * 100);
			slot.buying_date = currentDate;
//			orders.add(new LimitOrder(product, BasicOrderDescription.Side.BUY, slot.number_shares, slot.paid_price));
			orders.add(new MarketOrder(product, BasicOrderDescription.Side.BUY, slot.number_shares));
			System.out.printf("Buying No.%d: %d@%.2f\n", slot.serial, slot.number_shares, slot.paid_price);
		});
		// Sell
		slotsToSell.stream().forEach(slot -> {
			double selling_price = worstBid < slot.sell_floor ? slot.sell_floor : worstBid;
			double profit = slot.getSellTotal(selling_price) - slot.getInvestment();
			if (slot.paid_price == -1f) {
				System.out.println("error");
			}
//			orders.add(new LimitOrder(product, BasicOrderDescription.Side.SELL, slot.number_shares, selling_price));
			orders.add(new MarketOrder(product, BasicOrderDescription.Side.SELL, slot.number_shares));
			System.out.printf("Selling No.%d: %d@%.2f(%.2f) %.2f\n",
							  slot.serial,
							  slot.number_shares,
							  selling_price,
							  slot.paid_price,
							  profit);
			cashProfit += profit;
			slot.buying_date = null;
			slot.paid_price = -1f;
			slot.number_shares = 0;
			if (slot.serial == 1) {
				// if the first slot is sold, it means our history ceiling is reached, we enter "waiting mode".
				waiting = true;
				System.out.println("Start waiting.");
			}
		});
		if (slotsToBuy.size()+slotsToSell.size() > 0) {
			System.out.printf("Total Investment: %.2f\n", getInvestedCash(slots));
			System.out.printf("Total Profit: %.2f\n", cashProfit);
			long period = (now.getMillis()-startDate.getMillis())/1000/60/60/24;
			double averageInv = investment / period;
			double profitR = (cashProfit + averageInv)/averageInv;
			System.out.printf("Period: %d days (%.2f years)\n", period, period/365f);
			System.out.printf("Time*Investment: %.2f\n", investment);
			System.out.printf("Average investment: %.2f\n", averageInv);
			System.out.printf("Profit rate: %.2f\n", profitR);
			System.out.printf("Annualized: %.4f\n", Math.pow(profitR, 365f / period));
			System.out.println();
		}
		return orders;
	}
}