package michael.findata.algoquant.strategy.grid;

import com.numericalmethod.algoquant.execution.component.broker.Broker;
import com.numericalmethod.algoquant.execution.component.tradeblotter.TradeBlotter;
import com.numericalmethod.algoquant.execution.datatype.depth.Depth;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import com.numericalmethod.algoquant.execution.strategy.handler.DepthHandler;
import michael.findata.algoquant.execution.component.broker.MetaBroker;
import michael.findata.algoquant.execution.datatype.order.HexinOrder;
import michael.findata.algoquant.execution.strategy.Strategy;
import michael.findata.algoquant.execution.strategy.handler.DividendHandler;
import michael.findata.email.AsyncMailer;
import michael.findata.model.Dividend;
import michael.findata.model.Stock;
import michael.findata.spring.data.repository.GridStrategyRepository;
import michael.findata.util.DBUtil;
import org.apache.commons.math3.util.FastMath;
import org.hibernate.annotations.GenericGenerator;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.springframework.data.repository.Repository;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.*;

import static com.numericalmethod.nmutils.NMUtils.getClassLogger;
import static michael.findata.algoquant.execution.datatype.order.HexinOrder.HexinType.*;

@Entity
@Table(name = "strategy_instance_grid")
@Access(AccessType.FIELD)
public class GridStrategy implements Strategy, DividendHandler, DepthHandler, Comparable<GridStrategy> {
	@Override
	public Collection<Stock> getTargetSecurities() {
		Set<Stock> s = new HashSet<>(1);
		s.add(stock);
		return s;
	}

	public static class Param implements Strategy.Param {
		private double tenPercentCapital;
		private double buySellAmountThresholdLowerBound;
		private double peakBottomThreshold = 0.0035;
		public Param (double tenPercentCapital, double buySellAmountThresholdLowerBound, double peakBottomThreshold) {
			this.tenPercentCapital = tenPercentCapital;
			this.buySellAmountThresholdLowerBound = buySellAmountThresholdLowerBound;
			this.peakBottomThreshold = peakBottomThreshold;
		}
	}

//	public void setParam (Param p) {
//		this.tenPercentCapital = p.tenPercentCapital;
//		this.buySellAmountThreshold = p.buySellAmountThreshold;
//		this.peakBottomThreshold = p.peakBottomThreshold;
//	}

	public GridStrategy (Param p, Stock s) {
		this.tenPercentCapital = p.tenPercentCapital;
		this.buySellAmountThresholdLowerBound = p.buySellAmountThresholdLowerBound;
		this.peakBottomThreshold = p.peakBottomThreshold;
		this.stock = s;
	}

	public Param getParam () {
		return new Param(tenPercentCapital, buySellAmountThreshold, peakBottomThreshold);
	}

	public GridStrategy () {}

	private static final Logger LOGGER = getClassLogger();

	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	private int id;

	@Basic
	@Column(name = "ten_percent_capital")
	// how much to buy/sell for every 10% drop/climb
	private double tenPercentCapital;

	@Basic
	@Column(name = "buy_sell_amount_threshold")
	// action on a buy/sell signal if calculated amount is higher than this
	private double buySellAmountThreshold;

	@Basic
	@Column(name = "buy_sell_amount_threshold_lower_bound")
	private double buySellAmountThresholdLowerBound;

	@Basic
	@Column(name = "peak_bottom_threshold")
	// eg 0.3% use to judge whether we are too close to recent peak or bottom
	private double peakBottomThreshold;

	@Basic
	@Column(name = "reference_date")
	// date against which price adjustment is done
	// the "time" part of this field is meaningless
	private Timestamp referenceDate;

	@Basic
	@Column(name = "position_date")
	// date against which total and sellable positions are recorded
	// the "time" part of this field is meaningless
	private Timestamp positionDate;

	@ManyToOne
	@JoinColumn(name = "stock_id", nullable = false, updatable = false)
	// the stock this strategy is tracking
	private Stock stock;

	@Basic
	@Column(name = "current_baseline")
	// compare spot quote to this number to tell the drop/climb
	private Double currentBaseline;

	@Basic
	@Column(name = "current_peak")
	// spot quote too close to this means possible higher peak incoming, so we won't consider selling, no matter what
	private Double currentPeak;

	@Basic
	@Column(name = "current_bottom")
	// spot quote too close to this means possible lower bottom incoming, so we won't consider buying, no matter what
	private Double currentBottom;

	@Basic
	@Column(name = "position_total")
	private Integer positionTotal;

	@Basic
	@Column(name = "position_sellable")
	private Integer positionSellable;

	@Basic
	@Column(name = "active")
	private boolean active;

	@Basic
	@Column(name = "valuation")
	private Double shareValuation;

	@Basic
	@Column(name = "resv_limit_under_valuation")
	private Integer reserveLimitUnderValuation;

	@Basic
	@Column(name = "sell_side_broker", columnDefinition = "CHAR(10)")
	private String sellSideBrokerTag;

	@Basic
	@Column(name = "buy_side_broker", columnDefinition = "CHAR(10)")
	private String buySideBrokerTag;

	// set this field to have actions saved
	// Usually, set it in real trading and don't set it in simulation
	@Transient
	private GridStrategyRepository gridRepo;

	private double tradeAmountBaseline (double current) {
		// >0: meaning we should sell;
		// <0: meaning we should buy
		return FastMath.log(1.1, current/getCurrentBaseline()) * getTenPercentCapital();
	}

	private double tradeVolumeActual (double current) {
		int lotSize = stock.getLotSize();
		double bsln = tradeAmountBaseline(current);
		return FastMath.round(FastMath.abs(bsln)/(current*lotSize))*lotSize;
	}

	private double tradeAmountActual (double current) {
		return tradeVolumeActual(current) * current;
	}

//	@Override
//	public void onMarketConditionUpdate(DateTime now, MarketCondition mc, TradeBlotter blotter, Broker broker) {
//		System.out.println("Dummy Strategy: marketCondition updated @ "+now);
//		mc.depths().forEach((product, depth) -> {
//			System.out.println(depth);
//		});
//		System.out.println("Ending @ "+System.currentTimeMillis());
//	}

	@Override
	public void onDepthUpdate(DateTime now, Depth depth, MarketCondition mc, TradeBlotter blotter, Broker broker) {
		if (!stock.equals(depth.product())) {
			LOGGER.warn("\t{}\t: Irrelevant depth! {} should not be passed to me!", this, depth);
			return;
		}
		if (getPositionDate() == null || getPositionDate().before(now.toLocalDate().toDate())) {
			onDateUpdate(now.toLocalDate());
		}
		LOGGER.debug("\t{}\t: Grid Strategy: depth for {} updated.", this, depth.product());
		LOGGER.debug("\t{}\t: CurrentBaseline/CurrentPeak/CurrentBottom\t{}\t{}\t{}", this, getCurrentBaseline(), getCurrentPeak(), getCurrentBottom());

		if (depth instanceof michael.findata.algoquant.execution.datatype.depth.Depth) {
			michael.findata.algoquant.execution.datatype.depth.Depth dpt = (michael.findata.algoquant.execution.datatype.depth.Depth) depth;
			if (!dpt.isTraded()) {
				LOGGER.debug("\t{}\t: Security {} is not traded, ending...", this, dpt.product());
				return;
			}
		}

		LOGGER.debug("\t{}\t: Processing depth: {}", this, depth);
		if (getCurrentPeak() == null) {
			setCurrentPeak(depth.bid(1));
		}
		if (getCurrentBottom() == null) {
			if(depth.ask(1) <= 0f) {
				LOGGER.warn("\t{}\t: [1] Invalid bottom price: trying to chang currentButton from {} to {}", this, currentBottom, depth.ask(1));
			} else {
				setCurrentBottom(depth.ask(1));
			}
		}
		if (getCurrentBaseline() == null) {
			LOGGER.warn("\t{}\t: There is no current baseline price. This is very dangerous!!!", this);
			if (depth instanceof michael.findata.algoquant.execution.datatype.depth.Depth) {
				setCurrentBaseline(((michael.findata.algoquant.execution.datatype.depth.Depth) depth).spotPrice());
				LOGGER.warn("\t{}\t: Use last spot price {} as current baseline price.", this, currentBaseline);
			} else {
				setCurrentBaseline(depth.mid());
				LOGGER.warn("\t{}\t: Use depth.mid {} as current baseline price.", this, currentBaseline);
			}
		}
		if (getReferenceDate() == null) {
			setReferenceDate(new Timestamp(now.withTimeAtStartOfDay().getMillis()));
		}
		if (getPositionDate() == null) {
			setPositionDate(new Timestamp(now.withTimeAtStartOfDay().getMillis()));
		}
		if (getPositionTotal() == null) {
			setPositionTotal(0);
		}
		if (getPositionSellable() == null) {
			setPositionSellable(0);
		}
		if (depth.ask(1) < getCurrentBottom()) {
			if(depth.ask(1) <= 0f) {
				LOGGER.warn("\t{}\t: [2] Invalid bottom price: trying to change currentButton from {} to {}", this, currentBottom, depth.ask(1));
			} else {
				setCurrentBottom(depth.ask(1));
				LOGGER.info("\t{}\t: Saving to DB after bottom updated.", this);
				trySave();
				emailNotification("New Bottom");
			}
		}
		if (depth.bid(1) > getCurrentPeak()) {
			setCurrentPeak(depth.bid(1));
			LOGGER.info("\t{}\t: Saving to DB after peak updated.", this);
			trySave();
			emailNotification("New Peak");
		}
		if (depth.ask(1)/getCurrentBottom() < 1+getPeakBottomThreshold()) {
			// too close to bottom. We won't consider buying, no matter what -- there could be possible new bottom incoming
			return;
		}
		if (getCurrentPeak()/depth.bid(1) < 1+getPeakBottomThreshold()) {
			// too close to peak. We won't consider selling, no matter what -- there could be possible new peak incoming
			return;
		}

		HexinOrder.HexinType type;
		List<HexinOrder> orders = new ArrayList<>(1);

		// If you use depth.mid, depth like the following will cause disaster!!

		double baselineAmount;
		// Can we sell?
		baselineAmount = tradeAmountBaseline(depth.bid(1));
		if (baselineAmount > buySellAmountThreshold) {
			// climb not enough for sell
			// can we buy?
			baselineAmount = tradeAmountBaseline(depth.ask(1));
			if (baselineAmount < -buySellAmountThreshold) {
				// drop not enough for buy
				// We can neither buy or sell
				return;
			} else {
				// we can buy
				LOGGER.info("\t{}\t: Calculated baselineAmount {} with depth.ask(1) {}. Looks like we can can do a buy.", this, baselineAmount, depth.ask(1));
			}
		} else {
			// we can sell
			LOGGER.info("\t{}\t: Calculated baselineAmount {} with depth.bid(1) {}. Looks like we can can do a sell.", this, baselineAmount, depth.bid(1));
		}
		double volume;
		double effectivePrice;

		String brokerTag;
		if (baselineAmount > 0) {
			// sell
			// TODO: 9/3/2016 this is only suitable for small/mid volume. For bigger volume, execution price needs to be calculated.
			type = SIMPLE_SELL;
			brokerTag = sellSideBrokerTag;
			effectivePrice = depth.bid(1);
			if (shareValuation == null || reserveLimitUnderValuation == null || (depth.bid(1) < shareValuation && reserveLimitUnderValuation < positionTotal)) {
				// If current price is below valuation and there is no reserve quota available
				// sell according to grid - for the same price gap, sell more volume on lower price, less volume on higher prices
				// This sell style has less reservation side-effect, it doesn't tend to build up position
				volume = tradeVolumeActual(depth.bid(1));
				if (volume * effectivePrice < buySellAmountThreshold) {
					// drop/climb not enough for buy/sell
					return;
				}
			} else {
				// if current price is higher than valuation or there is reservation quota available,
				// sell according to valuation - for the same price gap, sell around the same volume, regardless of the current price
				// This sell style has more reservation side-effect, it tend to gradually build up +position
				volume = FastMath.round(FastMath.abs(baselineAmount)/(shareValuation*stock.getLotSize()))*stock.getLotSize();
				if (volume * effectivePrice * 2 < buySellAmountThreshold) {
					// drop/climb not enough for buy/sell
					return;
				}
			}
		} else {
			// buy
			// TODO: 9/3/2016 this is only suitable for small/mid volume. For bigger volume, execution price needs to be calculated.
			type = SIMPLE_BUY;
			brokerTag = buySideBrokerTag;
			volume = tradeVolumeActual(depth.ask(1));
			effectivePrice = depth.ask(1);
			if (volume * effectivePrice < buySellAmountThreshold) {
				// drop/climb not enough for buy/sell
				return;
			}
		}

		if (type == SIMPLE_SELL) {
			// are there enough to sell?
			if (getPositionSellable() <= 0) {
				LOGGER.info("\t{}\t: No stock to sell: {} @ {} at {}", this, depth.product(), effectivePrice, now);
				return;
			}
			if (getPositionSellable() < volume) {
				LOGGER.info("\t{}\t: Volume to sell is limited by total sellable : {} @ {}, volume reduced from {} to {}", this,
						depth.product(), effectivePrice, volume, getPositionSellable());
				volume = getPositionSellable();
			}
		}
		HexinOrder order = new HexinOrder(depth.product(), volume, effectivePrice, type);
		order.addTag(MetaBroker.ORDER_TAG_BROKER, brokerTag);
		orders.add(order);
		LOGGER.info("\t{}\t: At {}, submitting order {}", this, now, order);
		broker.sendOrder(orders);
		LOGGER.info("\t{}\t: CurrentBaseline/CurrentBottom/CurrentPeak before adjustment: {}/{}/{} ->", this, getCurrentBaseline(), getCurrentBottom(), getCurrentPeak());
		if(effectivePrice <= 0f) {
			LOGGER.warn("\t{}\t: [3] Invalid bottom price: trying to change currentButton from {} to {}", this, currentBottom, effectivePrice);
		}
		setCurrentBottom(effectivePrice);
		setCurrentPeak(effectivePrice);
		setCurrentBaseline(effectivePrice);
		LOGGER.info("\t{}\t: CurrentBaseline/CurrentBottom/CurrentPeak after adjustment: {}/{}/{}", this, getCurrentBaseline(), getCurrentBottom(), getCurrentPeak());
		LOGGER.info("\t{}\t: Baseline amount: {}, actual amount: {}", this, FastMath.abs(baselineAmount), volume*effectivePrice);

		if (!order.submitted()) {
			// Order has not been submitted, something is wrong
			LOGGER.warn("\t{}\t: Is this simulated / back test?", this);
			LOGGER.warn("\t{}\t: If not, then something has gone wrong when submitting order {}", this, order);
		}

		LOGGER.info("\t{}\t: Position total/sellable {}/{} ->", this, getPositionTotal(), getPositionSellable());
		if (type == SIMPLE_BUY) {
			// buy: position total += abs(volume)
			setPositionTotal(getPositionTotal() + (int)volume);
		} else {
			// sell: position total -= abs(volume)
			//		 position sellable -= abs(volume)
			setPositionTotal(getPositionTotal() - (int)volume);
			setPositionSellable(getPositionSellable() - (int)volume);
		}
		LOGGER.info("\t{}\t: Position total/sellable {}/{}",this , getPositionTotal(), getPositionSellable());
		LOGGER.debug("\t{}\t: End. ------------------------------------------------------------------", this);
		LOGGER.info("\t{}\t: Saving to DB after buy/sell.", this);
		trySave(); // only save when there is an action.
		String emailTitle = String.format(type == SIMPLE_BUY ? "Buy Order %.0f@%.3f" : "Sell Order %.0f@%.3f", volume, effectivePrice);
		emailNotification(emailTitle);
	}

	public void emailNotification(String titlePrefix) {
		AsyncMailer.instance.email(String.format("%s: %s", titlePrefix, this), notification());
	}

	@Override
	// Contract: it is the DividendHandler(strategy)'s responsibility to filter repeated or out-of-order dividend events
	public void onDividend(DateTime now, Dividend dividend, MarketCondition mc, TradeBlotter blotter, Broker broker) {
		if (!dividend.getStock().equals(stock)) {
			LOGGER.warn("\t{}\t: Irrelevant dividend!! {} should not be passed to me!", this, dividend);
			return;
		}
		if (getCurrentBaseline() == null) {
			return;
		}
		LOGGER.debug("\t{}\t: Processing dividend: {}", this, dividend);
		LocalDate divDate = LocalDate.fromDateFields(dividend.getPaymentDate());
		LocalDate refDate = LocalDate.fromDateFields(getReferenceDate());
		if (refDate.isBefore(divDate)) {
			setReferenceDate(new Timestamp (divDate.toDate().getTime()));
			LOGGER.info("\t{}\t: Accepted {}", this, dividend);
			LOGGER.info("\t{}\t: CurrentBaseline/CurrentBottom/CurrentPeak before adjustment: {}/{}/{} ->", this, getCurrentBaseline(), getCurrentBottom(), getCurrentPeak());
			if (dividend.getAdjustmentFactor() == null) {
//				price = price*(1+div.getBonus())+div.getAmount();
				setCurrentBaseline((getCurrentBaseline() - dividend.getAmount())/(1+dividend.getBonus()));
				setCurrentBottom((getCurrentBottom() - dividend.getAmount())/(1+dividend.getBonus()));
				setCurrentPeak((getCurrentPeak() - dividend.getAmount())/(1+dividend.getBonus()));
			} else {
				setCurrentBaseline(getCurrentBaseline()/dividend.getAdjustmentFactor());
				setCurrentBottom(getCurrentBottom()/dividend.getAdjustmentFactor());
				setCurrentPeak(getCurrentPeak()/dividend.getAdjustmentFactor());
			}
			setPositionTotal((int)(getPositionTotal() * (1+dividend.getBonus())));
			setPositionSellable((int)(getPositionSellable() * (1+dividend.getBonus())));
			LOGGER.info("\t{}\t: CurrentBaseline/CurrentBottom/CurrentPeak after adjustment: {}/{}/{}", this, getCurrentBaseline(), getCurrentBottom(), getCurrentPeak());
			LOGGER.info("\t{}\t: Saving to DB after dealing with dividends.", this);
			trySave(); // save after dealing with dividends, max once per day.
		}
	}

	// Date updated, meaning a new day has arrived
	private void onDateUpdate(LocalDate exeDate) {
		if (getPositionDate() == null || exeDate.isAfter(LocalDate.fromDateFields(getPositionDate()))) {
			setPositionDate(new Timestamp(exeDate.toDate().getTime()));
			LOGGER.info("\t{}\t: New date: {}", this, getPositionDate());
			LOGGER.debug("\t{}\t: Position total/sellable {}/{} ->", this, getPositionTotal(), getPositionSellable());
			setPositionSellable(getPositionTotal());
			LOGGER.debug("\t{}\t: Position total/sellable {}/{}", this, getPositionTotal(), getPositionSellable());
		} else {
			LOGGER.debug("\t{}\t: Meaningless date update.", this);
		}
	}

	@Override
	public int hashCode () {
		return id;
	}

	@Override
	public boolean equals (Object another) {
		return another != null
				&& another instanceof GridStrategy
				&& id == ((GridStrategy) another).id;
	}

	@Override
	public int compareTo(GridStrategy another) {
		if (another == null) return 1;
		return id - another.id;
	}

	@Override
	public String toString() {
		return String.format("GridStrategy [id=%d] for [stock= %s %s]", id, stock.getCode(), stock.getName());
	}

	@Override
	public void onStop() {
		// Save to DB
		LOGGER.info("\t{}\t: Saving to DB and stop.", this);
		trySave();
		emailNotification("Trading Ended");
	}

	public void trySave() {
		if (gridRepo != null) {
			try {
				gridRepo.save(this);
			} catch (Exception ex) {
				LOGGER.warn("\t{}\t: Failed to save -- exception {} caught", this, ex.getClass());
				ex.printStackTrace();
				DBUtil.dealWithDBAccessError(ex);
			}
		} else {
			LOGGER.warn("\t{}\t: Failed to save -- repository is null.", this);
		}
	}

	@Override
	public String notification() {
		return String.format(
				"<pre><b>%s</b>\n<b>ID:</b> %d\n<b>Stock:</b> %s\n<b>Active:</b> %s\n<b>Current peak:</b> %.3f\n<b>Current baseline:</b> %.3f\n<b>Current bottom:</b> %.3f\n<b>Buy/Sell amount threshold:</b> %.1f\n<b>Reference date:</b> %s\n<b>Position date:</b> %s\n<b>Position total:</b> %d\n<b>Position sellable:</b> %d\n<b>Share valuation:</b> %.3f\n<b>Reserve limit under valuation:</b> %d\n<b>Ten-percent capital:</b> %.1f\n<b>Buy/Sell amount threshold lower bound:</b> %.1f\n<b>Peak/Bottom threshold:</b> %.3f</pre>",
				"GridStrategy",
				id,
				stock,
				active,
				currentPeak,
				currentBaseline,
				currentBottom,
				buySellAmountThreshold,
				referenceDate,
				positionDate,
				positionTotal,
				positionSellable,
				shareValuation,
				reserveLimitUnderValuation,
				tenPercentCapital,
				buySellAmountThresholdLowerBound,
				peakBottomThreshold);
	}

	public double getTenPercentCapital() {
		return tenPercentCapital;
	}

	public double getBuySellAmountThreshold() {
		return buySellAmountThreshold;
	}

	public Timestamp getReferenceDate() {
		return referenceDate;
	}

	public Stock getStock() {
		return stock;
	}

	public double getPeakBottomThreshold() {
		return peakBottomThreshold;
	}

	public Double getCurrentBaseline() {
		return currentBaseline;
	}

	public Double getCurrentPeak() {
		return currentPeak;
	}

	public Double getCurrentBottom() {
		return currentBottom;
	}

	public Integer getPositionTotal() {
		return positionTotal;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	// Set this, so that when CommandCenter stops, the strategy itself can be saved.
	public void setRepository(Repository gridRepo) {
		this.gridRepo = (GridStrategyRepository) gridRepo;
	}

	public void setReferenceDate(Timestamp referenceDate) {
		this.referenceDate = referenceDate;
	}

	public Timestamp getPositionDate() {
		return positionDate;
	}

	public void setPositionDate(Timestamp positionDate) {
		this.positionDate = positionDate;
	}

	public void setCurrentBaseline(Double currentBaseline) {
		this.currentBaseline = currentBaseline;
		// when baseline is set, we also adjust buySellAmountThreshold according to baseline and buySellAmountThresholdLowerBound
		buySellAmountThreshold = FastMath.ceil(buySellAmountThresholdLowerBound/(currentBaseline*stock.getLotSize()))*stock.getLotSize()*currentBaseline;
		LOGGER.info("\t{}\t: buySellAmountThreshold: {}", this, buySellAmountThreshold);
//		buySellAmountThreshold = buySellAmountThresholdLowerBound;
	}

	public void setCurrentPeak(Double currentPeak) {
		this.currentPeak = currentPeak;
	}

	public void setCurrentBottom(Double currentBottom) {
		if (currentBottom == 0) {
			LOGGER.warn("\t{}\t: Setting currentButton to {}", this, currentBottom);
		}
		this.currentBottom = currentBottom;
	}

	public void setPositionTotal(Integer positionTotal) {
		this.positionTotal = positionTotal;
	}

	public Integer getPositionSellable() {
		return positionSellable;
	}

	public void setPositionSellable(Integer positionSellable) {
		this.positionSellable = positionSellable;
	}

	public Double getShareValuation() {
		return shareValuation;
	}

	public void setShareValuation(Double shareValuation) {
		this.shareValuation = shareValuation;
	}

	public Integer getReserveLimitUnderValuation() {
		return reserveLimitUnderValuation;
	}

	public void setReserveLimitUnderValuation(Integer reserveLimitUnderValuation) {
		this.reserveLimitUnderValuation = reserveLimitUnderValuation;
	}
}