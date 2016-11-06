package michael.findata.algoquant.strategy.grid;

import com.numericalmethod.algoquant.execution.component.broker.Broker;
import com.numericalmethod.algoquant.execution.component.tradeblotter.TradeBlotter;
import com.numericalmethod.algoquant.execution.datatype.depth.Depth;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import com.numericalmethod.algoquant.execution.strategy.handler.DepthHandler;
import michael.findata.algoquant.execution.datatype.order.HexinOrder;
import michael.findata.algoquant.execution.strategy.Strategy;
import michael.findata.algoquant.execution.strategy.handler.DividendHandler;
import michael.findata.model.Dividend;
import michael.findata.model.Stock;
import michael.findata.spring.data.repository.GridInstanceRepository;
import org.apache.commons.math3.util.FastMath;
import org.hibernate.annotations.GenericGenerator;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.*;

import static com.numericalmethod.nmutils.NMUtils.getClassLogger;
import static michael.findata.algoquant.execution.datatype.order.HexinOrder.HexinType.SIMPLE_BUY;
import static michael.findata.algoquant.execution.datatype.order.HexinOrder.HexinType.SIMPLE_SELL;

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
		private double buySellAmountThreshold;
		private double peakBottomThreshold = 0.0035;
		public Param (double tenPercentCapital, double buySellAmountThreshold, double peakBottomThreshold) {
			this.tenPercentCapital = tenPercentCapital;
			this.buySellAmountThreshold = buySellAmountThreshold;
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
		this.buySellAmountThreshold = p.buySellAmountThreshold;
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

	// set this field to have actions saved
	// Usually, set it in real trading and don't set it in simulation
	@Transient
	private GridInstanceRepository gridRepo;

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
			LOGGER.warn("Irrelevant depth!!");
			LOGGER.warn("{} should not be passed to strategy {}", depth, this);
			return;
		}
		if (getPositionDate() == null || getPositionDate().before(now.toLocalDate().toDate())) {
			onDateUpdate(now.toLocalDate());
		}
		LOGGER.debug("Grid Strategy: depth for {} updated.", depth.product());
		LOGGER.debug("CurrentBaseline/CurrentPeak/CurrentBottom\t{}\t{}\t{}", getCurrentBaseline(), getCurrentPeak(), getCurrentBottom());

		if (depth instanceof michael.findata.algoquant.execution.datatype.depth.Depth) {
			michael.findata.algoquant.execution.datatype.depth.Depth dpt = (michael.findata.algoquant.execution.datatype.depth.Depth) depth;
			if (!dpt.isTraded()) {
				LOGGER.debug("Security {} is not traded, ending...", dpt.product());
				return;
			}
		}

		LOGGER.debug("Processing depth: {}", depth);
		if (getCurrentPeak() == null) {
			setCurrentPeak(depth.mid());
		}
		if (getCurrentBottom() == null) {
			setCurrentBottom(depth.mid());
		}
		if (getCurrentBaseline() == null) {
			setCurrentBaseline(depth.mid());
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
			setCurrentBottom(depth.ask(1));
		}
		if (depth.bid(1) > getCurrentPeak()) {
			setCurrentPeak(depth.bid(1));
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
		double baselineAmount = tradeAmountBaseline(depth.mid());
		double volume, actualAmount;
		double effectivePrice;
		if (FastMath.abs(baselineAmount) < buySellAmountThreshold) {
			// drop/climb not enough for buy/sell
			return;
		}
		if (baselineAmount > 0) {
			// sell
			// TODO: 9/3/2016 this is only suitable for small/mid volume. For bigger volume, execution price needs to be calculated.
			type = SIMPLE_SELL;
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
			}
			if (volume * effectivePrice * 2 < buySellAmountThreshold) {
				// drop/climb not enough for buy/sell
				return;
			}
		} else {
			// buy
			// TODO: 9/3/2016 this is only suitable for small/mid volume. For bigger volume, execution price needs to be calculated.
			type = SIMPLE_BUY;
			volume = tradeVolumeActual(depth.ask(1));
			effectivePrice = depth.ask(1);
			if (volume * effectivePrice < buySellAmountThreshold) {
				// drop/climb not enough for buy/sell
				return;
			}
		}

		// todo Compare baseline amount with actual, cannot be too different

		if (type == SIMPLE_SELL) {
			// are there enough to sell?
			if (getPositionSellable() <= 0) {
				LOGGER.info("No stock to sell: {} @ {} at {}", depth.product(), effectivePrice, now);
				return;
			}
			if (getPositionSellable() < volume) {
				LOGGER.info("Volume to sell is limited by total sellable : {} @ {}, volume reduced from {} to {}",
						depth.product(), effectivePrice, volume, getPositionSellable());
				volume = getPositionSellable();
			}
		}
		HexinOrder order = new HexinOrder(depth.product(), volume, effectivePrice, type);
		orders.add(order);
		LOGGER.info("At {}, submitting order {}", now, order);
		broker.sendOrder(orders);
		LOGGER.info("CurrentBaseline/CurrentBottom/CurrentPeak before adjustment: {}/{}/{} ->", getCurrentBaseline(), getCurrentBottom(), getCurrentPeak());
		currentBottom = currentPeak = currentBaseline = effectivePrice;
		LOGGER.info("CurrentBaseline/CurrentBottom/CurrentPeak after adjustment: {}/{}/{}", getCurrentBaseline(), getCurrentBottom(), getCurrentPeak());
		LOGGER.info("Baseline amount: {}, actual amount: {}", FastMath.abs(baselineAmount), volume*effectivePrice);

		if (!order.submitted()) {
			// Order has not been submitted, something is wrong
			LOGGER.warn("Is this simulated / back test?");
			LOGGER.warn("If not, then something has gone wrong when submitting order {}", order);
		}

		LOGGER.info("Position total/sellable {}/{} ->", getPositionTotal(), getPositionSellable());
		if (type == SIMPLE_BUY) {
			// buy: position total += abs(volume)
			setPositionTotal(getPositionTotal() + (int)volume);
		} else {
			// sell: position total -= abs(volume)
			//		 position sellable -= abs(volume)
			setPositionTotal(getPositionTotal() - (int)volume);
			setPositionSellable(getPositionSellable() - (int)volume);
		}
		LOGGER.info("Position total/sellable {}/{}", getPositionTotal(), getPositionSellable());
		LOGGER.debug("Ending.");
		LOGGER.debug("------------------------------------------------------------------");
		if (gridRepo != null) {
			gridRepo.save(this);
		}
	}

	@Override
	// Contract: it is the DividendHandler(strategy)'s responsibility to filter repeated or out-of-order dividend events
	public void onDividend(DateTime now, Dividend dividend, MarketCondition mc, TradeBlotter blotter, Broker broker) {
		if (!dividend.getStock().equals(stock)) {
			LOGGER.warn("Irrelevant dividend!!");
			LOGGER.warn("{} should not be passed to strategy {}", dividend, this);
			return;
		}
		if (getCurrentBaseline() == null) {
			return;
		}
		LOGGER.debug("{} is processing dividend: {}", this, dividend);
		LocalDate divDate = LocalDate.fromDateFields(dividend.getPaymentDate());
		LocalDate refDate = LocalDate.fromDateFields(getReferenceDate());
		if (refDate.isBefore(divDate)) {
			setReferenceDate(new Timestamp (divDate.toDate().getTime()));
			LOGGER.info("{} accepted {}", this, dividend);
			LOGGER.info("CurrentBaseline/CurrentBottom/CurrentPeak before adjustment: {}/{}/{} ->", getCurrentBaseline(), getCurrentBottom(), getCurrentPeak());
			if (dividend.getAdjustmentFactor() == null) {
//				price = price*(1+div.getBonus())+div.getAmount();
				setCurrentBaseline((getCurrentBaseline() - dividend.getAmount())/(1+dividend.getBonus()));
				setCurrentBottom((getCurrentBottom() - dividend.getAmount())/(1+dividend.getBonus()));
				setCurrentPeak((getCurrentPeak() - dividend.getAmount())/(1+dividend.getBonus()));
			} else {
				setCurrentBaseline(getCurrentBaseline() /dividend.getAdjustmentFactor());
				setCurrentBottom(getCurrentBottom() /dividend.getAdjustmentFactor());
				setCurrentPeak(getCurrentPeak() /dividend.getAdjustmentFactor());
			}
			setPositionTotal((int)(getPositionTotal() * (1+dividend.getBonus())));
			setPositionSellable((int)(getPositionSellable() * (1+dividend.getBonus())));
			LOGGER.info("CurrentBaseline/CurrentBottom/CurrentPeak after adjustment: {}/{}/{}", getCurrentBaseline(), getCurrentBottom(), getCurrentPeak());
			if (gridRepo != null) {
				gridRepo.save(this);
			}
		}
	}

	// Date updated, meaning a new day has arrived
	private void onDateUpdate(LocalDate exeDate) {
		if (getPositionDate() == null || exeDate.isAfter(LocalDate.fromDateFields(getPositionDate()))) {
			setPositionDate(new Timestamp(exeDate.toDate().getTime()));
			LOGGER.info("New date: {}", getPositionDate());
			LOGGER.debug("Position total/sellable {}/{} ->", getPositionTotal(), getPositionSellable());
			setPositionSellable(getPositionTotal());
			LOGGER.debug("Position total/sellable {}/{}", getPositionTotal(), getPositionSellable());
		} else {
			LOGGER.debug("Meaningless date update.");
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
		LOGGER.info("Saving {} to DB and stop.", this);
		gridRepo.save(this);
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

	// Set this, so that when CommandCenter stops, the strategy itself can be saved.
	public void setGridInstanceRepository(GridInstanceRepository gridRepo) {
		this.gridRepo = gridRepo;
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
	}

	public void setCurrentPeak(Double currentPeak) {
		this.currentPeak = currentPeak;
	}

	public void setCurrentBottom(Double currentBottom) {
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