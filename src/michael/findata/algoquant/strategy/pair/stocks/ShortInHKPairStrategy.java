package michael.findata.algoquant.strategy.pair.stocks;

import com.numericalmethod.algoquant.execution.component.broker.Broker;
import com.numericalmethod.algoquant.execution.component.tradeblotter.TradeBlotter;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import com.numericalmethod.algoquant.execution.datatype.order.Order;
import com.numericalmethod.algoquant.execution.datatype.product.fx.Currencies;
import com.numericalmethod.algoquant.execution.strategy.handler.DepthHandler;
import michael.findata.algoquant.execution.component.broker.LocalInteractiveBrokers;
import michael.findata.algoquant.execution.component.broker.MetaBroker;
import michael.findata.algoquant.execution.datatype.depth.Depth;
import michael.findata.algoquant.execution.datatype.order.HexinOrder;
import michael.findata.algoquant.execution.listener.OrderListener;
import michael.findata.algoquant.execution.strategy.Strategy;
import michael.findata.algoquant.execution.strategy.handler.DividendHandler;
import michael.findata.algoquant.strategy.Pair;
import michael.findata.algoquant.strategy.pair.PairStrategyUtil;
import michael.findata.commandcenter.CommandCenter;
import michael.findata.email.AsyncMailer;
import michael.findata.model.Dividend;
import michael.findata.model.PairStats;
import michael.findata.model.Stock;
import michael.findata.spring.data.repository.ShortInHkPairStrategyRepository;
import michael.findata.util.DBUtil;
import org.hibernate.annotations.GenericGenerator;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.apache.logging.log4j.Logger;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;
import scala.util.regexp.Base;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.*;

import static com.numericalmethod.algoquant.execution.datatype.order.Order.OrderExecutionType.*;
import static michael.findata.util.LogUtil.getClassLogger;
import static michael.findata.algoquant.strategy.pair.PairStrategyUtil.calVolumes;

/**
 * Open suitable for a pair with the short side from HK stock exchange
 * Strategy workflow cycle:
 *
 * Opening the position, meaning the following is executed when position is not yet opened
 * 0. If position is not opened yet, check price delta;
 * 1. If price delta > openThreshold: open short order, and wait for the short order to fill - strategyStatus = OPENING;
 * 2. If price delta drops back to < openThreshold:
 * 		a. if short order is unfilled, cancel it and go back to step 0;
 * 		b. if short order is partially filled, wait for it to be fully filled
 * 3. short order fully filled: execute long order and consider that we have opened our position - strategyStatus = OPENED.
 *
 * Closing the position, meaning the following is executed when we already have our position opened.
 * 4. If position already opened, check price delta;
 * 5. If price delta < close threshold, buy back short and sell back long - strategyStatus = CLOSED;
 */
@Entity
@Table(name = "strategy_instance_pair")
@Access(AccessType.FIELD)
public class ShortInHKPairStrategy implements OrderListener, Strategy, DepthHandler, DividendHandler, Comparable<ShortInHKPairStrategy> {
	private static final Logger LOGGER = getClassLogger();

	public Logger getLogger () {
		return LOGGER;
	}

	// true: in simulation/backtesting
	// false: in real trading
	private boolean backtestMode = false;

	final public boolean isBacktestMode() {
		return backtestMode;
	}

	@Override
	final public void setBacktestMode(boolean backtestMode) {
		this.backtestMode = backtestMode;
	}

	//经验参数：
	public static Map<String, Param> paramMap = new HashMap<>();
	static {
		//中国平安：02318->601318
		// 0.01		-	-0.005
		// 0.02		-	0.005
		// 0.025	-	0.005
		paramMap.put("02318->601318 1", new Param(0.01, -0.005, 57000, 37000));
		paramMap.put("02318->601318 2", new Param(0.02, 0.005, 57000, 37000));
		paramMap.put("02318->601318 3", new Param(0.025, 0.005, 57000, 37000));

		// 潍柴动力：02338->000338
		// 0.025	-	0.005
		// 0.04		-	0.02
		// 0.045	-	0.02
		paramMap.put("02338->000338 1", new Param(0.025, 0.005, 57000, 37000));
		paramMap.put("02338->000338 2", new Param(0.04, 0.02, 57000, 37000));
		paramMap.put("02338->000338 3", new Param(0.045, 0.002, 57000, 37000));

		// 海螺水泥：00914->600585
		// 0.012	-	0
		// 0.022	-	0
		// 0.032	-	0
		paramMap.put("00914->600585 1", new Param(0.020, 0, 57000, 37000));
		paramMap.put("00914->600585 2", new Param(0.025, 0, 57000, 37000));
		paramMap.put("00914->600585 3", new Param(0.035, 0, 57000, 37000));

		// 福耀玻璃：03606->600660
		// 0.016	-	0
		// 0.024	-	0
		// 0.034	-	0
		paramMap.put("03606->600660 1", new Param(0.016, 0, 57000, 37000));
		paramMap.put("03606->600660 2", new Param(0.024, 0, 57000, 37000));
		paramMap.put("03606->600660 3", new Param(0.034, 0, 57000, 37000));
	}

	// for hibernate use only
	protected ShortInHKPairStrategy() {
		openThreshold = 0.03;
		closeThreshold = .013;
		amountUpperLimit = 57000; // default in RMB!!!
		amountLowerLimit = 37000; // default in RMB!!!
		reset(false);
	}

	public static class Param implements Strategy.Param {
		private double openThreshold;
		private double closeThreshold;
		private double amountUpperLimit;
		private double amountLowerLimit;
		public Param (double openThreshold, double closeThreshold, double amountUpperLimit, double amountLowerLimit) {
			this.openThreshold = openThreshold;
			this.closeThreshold = closeThreshold;
			this.amountUpperLimit = amountUpperLimit;
			this.amountLowerLimit = amountLowerLimit;
		}
	}

	public ShortInHKPairStrategy(PairStats stats) {
		this();
		openableDate = LocalDate.fromDateFields(stats.getTrainingEnd()).plusDays(1).toDate();
		stats(stats);
	}

	public ShortInHKPairStrategy(PairStats stats, Param param) {
		this();
		openableDate = LocalDate.fromDateFields(stats.getTrainingEnd()).plusDays(1).toDate();
		stats(stats);
		this.openThreshold = param.openThreshold;
		this.closeThreshold = param.closeThreshold;
		this.amountUpperLimit = param.amountUpperLimit;
		this.amountLowerLimit = param.amountLowerLimit;
	}

	public ShortInHKPairStrategy(PairStats stats, Param param, double amountLowerLimit, double amountUpperLimit) {
		this(stats, param);
		this.amountLowerLimit = amountLowerLimit;
		this.amountUpperLimit = amountUpperLimit;
	}

	public ShortInHKPairStrategy(PairStats stats, double openThreshold, double closeThreshold, double amountLowerLimit, double amountUpperLimit) {
		this(stats);
		this.openThreshold = openThreshold;
		this.closeThreshold = closeThreshold;
		this.amountUpperLimit = amountUpperLimit;
		this.amountLowerLimit = amountLowerLimit;
	}

	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	private int id;

	@Basic
	@Column(name = "status",columnDefinition="CHAR(10) not null")
	@Enumerated(EnumType.STRING)
	private Status status;

	@Basic
	@Column(name = "date_opened")
	private Timestamp dateOpened; // done

	@Basic
	@Column(name = "date_closed")
	private Timestamp dateClosed; // done

	@Basic
	@Column(name = "threshold_open")
	private double openThreshold;

	@Basic
	@Column(name = "threshold_close")
	private double closeThreshold;

	@Basic
	@Column(name = "amount_upper_limit")
	private double amountUpperLimit;

	@Basic
	@Column(name = "amount_lower_limit")
	private double amountLowerLimit;

	@Basic
	@Column(name = "short_open")
	private Double shortOpen; // done

	@Basic
	@Column(name = "long_open")
	private Double longOpen; // done

	@Basic
	@Column(name = "short_close")
	private Double shortClose; // done

	@Basic
	@Column(name = "long_close")
	private Double longClose; // done

	@Basic
	@Column(name = "short_volume_held")
	private Double shortVolumeHeld; // done

	@Basic
	@Column(name = "long_volume_held")
	private Double longVolumeHeld; // done

	@Basic
	@Column(name = "short_volume_calculated")
	private Double shortVolumeCalculated; // done, only meaningful when status is not new;

	@Basic
	@Column(name = "long_volume_calculated")
	private Double longVolumeCalculated; // done, only meaningful when status is not new;

	@Basic
	@Column(name = "min_res")
	private Double minResidual; // done

	@Basic
	@Column(name = "max_res")
	private Double maxResidual; // done

	@Basic
	@Column(name = "min_res_date")
	private Timestamp minResidualDate; // done

	@Basic
	@Column(name = "max_res_date")
	private Timestamp maxResidualDate; // done

	@Basic
	@Column(name = "res_open")
	private Double residualOpen; // done

	@Basic
	@Column(name = "res_close")
	private Double residualClose; // done

	// This is for logging purpose only, don't use it in stategy execution
	@Basic
	@Column(name = "res_latest")
	private Double residualLatest;

	@ManyToOne
	@JoinColumn(name = "pair_stats_id", nullable = false, insertable = true, updatable = false)
	private PairStats stats;

	private void stats(PairStats stats) {
		this.stats = stats;
	}

	public PairStats stats() {
		return this.stats;
	}

	@Basic
	@Column(name = "openable_on")
	private Date openableDate;

	@Basic
	@Column(name = "force_closure_on_or_after")
	private Date forceClosureDate;

	@Basic
	@Column(name = "short_side_broker", columnDefinition = "CHAR(10)")
	private String shortSideBrokerTag = "";

	@Basic
	@Column(name = "long_side_broker", columnDefinition = "CHAR(10)")
	private String longSideBrokerTag = "";

	@Transient
	private String codeToShort = null;

	@Basic
	@Access(AccessType.PROPERTY)
	@Column(name = "code_to_short", columnDefinition = "char(9)")
	public String getCodeToShort () {
		if (codeToShort == null) {
			codeToShort = stats.getPair().getStockToShort().getCode();
		}
		return codeToShort;
	}

	public void setCodeToShort (String codeToShort) {
		this.codeToShort = codeToShort;
	}

	@Transient
	private String codeToLong = null;

	@Basic
	@Access(AccessType.PROPERTY)
	@Column(name = "code_to_long", columnDefinition = "char(9)")
	public String getCodeToLong() {
		if (codeToLong == null) {
			codeToLong = stats.getPair().getStockToLong().getCode();
		}
		return codeToLong;
	}

	public void setCodeToLong(String codeToLong) {
		this.codeToLong = codeToLong;
	}

	@Transient
	private String nameToShort = null;

	@Access(AccessType.PROPERTY)
	@Column(name = "name_to_short", columnDefinition = "char(14)")
	public String getNameToShort() {
		if (nameToShort == null) {
			nameToShort = stats.getPair().getStockToShort().getName();
		}
		return nameToShort;
	}

	public void setNameToShort(String nameToShort) {
		this.nameToShort = nameToShort;
	}

	@Transient
	private String nameToLong = null;

	@Access(AccessType.PROPERTY)
	@Column(name = "name_to_long", columnDefinition = "char(14)")
	public String getNameToLong() {
		if (nameToLong == null) {
			nameToLong = stats.getPair().getStockToLong().getName();
		}
		return nameToLong;
	}

	public void setNameToLong(String nameToLong) {
		this.nameToLong = nameToLong;
	}

	public Stock toShort() {
		return stats.getPair().getStockToShort();
	}

	public Stock toLong() {
		return stats.getPair().getStockToLong();
	}

	public LocalDate trainingStart() {
		return LocalDate.fromDateFields(stats.getTrainingStart());
	}

	public LocalDate trainingEnd() {
		return LocalDate.fromDateFields(stats.getTrainingEnd());
	}

	public double slope() {
		return stats.getSlope();
	}

	public double stdev() {
		return stats.getStdev();
	}

	//correlation coefficient obtained during training pass
	public double correlation() {
		return stats.getCorrelco();
	}

	// p value in adf test obtained during training pass
	public double adf_p() {
		return stats.getAdfp();
	}

	public int age (LocalDate now) {
		if (dateOpened == null) {
			return 0;
		}
		switch (status) {
			case OPENED:
			case CLOSED:
			case FORCED:
				return Days.daysBetween(LocalDate.fromDateFields(dateOpened), now).getDays();
			default:
				return 0;
		}
	}

	public int age (DateTime now) {
		return age (now.toLocalDate());
	}

	public double profitPercentageEstimate () {
		try {
			return (shortOpen - shortClose) / shortOpen + (longClose - longOpen) / longOpen - feeEstimate();
		} catch (NullPointerException npe) {
			return 0;
		}
	}

	public double feeEstimate() {
		int age = ageToClosure();
		double taxShort;
		double taxLong;
		String symbolLong = toLong().symbol();
		if (symbolLong.startsWith("15") || symbolLong.startsWith("5")) {
			taxLong = 0d;
		} else {
			taxLong = 0.001d;
		}
		String symbolShort = toShort().symbol();
		if (symbolShort.startsWith("15") || symbolShort.startsWith("5")) {
			taxShort = 0d;
		} else {
			taxShort = 0.002d;
		}
		return taxLong + taxShort + 4 * 0.0005 + (age==0?1:age) * 0.055 / 360;
	}

	public int ageToClosure() {
		return age(LocalDate.fromDateFields(dateClosed));
	}

	//	private double slope = 1/1.04; // calculated by stats, used to adjust short side effective price

	@Transient
	private HexinOrder openingOrderShort = null;
	@Transient
	private HexinOrder openingOrderLong = null;
	@Transient
	private HexinOrder closingOrderShort = null;
	@Transient
	private HexinOrder closingOrderLong = null;
	@Transient
	private Depth depthToShort = null;
	@Transient
	private Depth depthToLong = null;

	// Calculated best execution prices
	@Transient
	private double actualPriceForShortSide;

	@Transient
	private double actualPriceForLongSide;

	/**
	 * Always apply fx to the short side, meaning use fx()*shortSideEffectivePrice for stockToShort
	 * @return exchange rate, -1 if not available, 1 if the same currency
	 */
	private double fx() {
		Currency currencyShort = toShort().currency();
		Currency currencyLong = toLong().currency();
		if (currencyShort.equals(currencyLong)) {
			return 1;
		} else if (currencyShort.equals(Currencies.HKD) && currencyLong.equals(Currencies.CNY)) {
			if (LocalInteractiveBrokers.CNH_HKD_ask > 0 && LocalInteractiveBrokers.CNH_HKD_bid > 0) {
				return 2/(LocalInteractiveBrokers.CNH_HKD_ask + LocalInteractiveBrokers.CNH_HKD_bid);
			} else if (LocalInteractiveBrokers.CNH_HKD_last > 0) {
				return 1/LocalInteractiveBrokers.CNH_HKD_last;
			} else {
				return -1;
			}
		} else if (currencyShort.equals(Currencies.CNY) && currencyLong.equals(Currencies.HKD)) {
			LOGGER.warn("\t{}\t: How can you short a CNY stock in Hong Kong Exchange?", this);
			LOGGER.warn("\t{}\t: Not supported yet: short currency {}", this, currencyShort, currencyLong);
//			return LocalInteractiveBrokers.CNH_HKD_last;
			return -1;
		} else {
			LOGGER.warn("\t{}\t: Not supported yet: short currency {}", this, currencyShort, currencyLong);
			return -1;
		}
	}

	/**
	 * @return calculated price delta, null if any of the depth is not available
	 */
	private Double residual() {
		if (depthToShort == null || depthToLong == null) {
			LOGGER.debug("\t{}\t: depthToShort: {}, depthToLong: {}. At least one of the above is null. Cannot calculate residual.", this, depthToShort, depthToLong);
			return null;
		}
		double fx = fx();
		if (fx < 0) {
			LOGGER.debug("\t{}\t: Fx rate not available. Cannot calculate residual.", this);
			return null;
		}
		// fx/dividend/split adjusted prices
		double effectivePriceForShortSide;
		double effectivePriceForLongSide;
		double bestShortSide;
		double bestLongSide;

		switch (status) {
			case NEW:
				// use fx to translate the amount limit from hkd to rmb
				bestShortSide = depthToShort.bestBid(amountUpperLimit/fx);
				bestLongSide = depthToLong.bestAsk(amountUpperLimit);
				break;
			case OPENING:
				// use fx to translate the amount limit from hkd to rmb
				bestShortSide = depthToShort.bestBid((shortVolumeCalculated-openingOrderShort.filledQuantity())*openingOrderShort.price());
				bestLongSide = depthToLong.bestAsk(longVolumeCalculated*actualPriceForLongSide);
				break;
			case OPENED:
				bestShortSide = depthToShort.bestAsk(shortVolumeHeld*depthToShort.ask(1));
				bestLongSide = depthToLong.bestBid(longVolumeHeld*depthToLong.bid(1));
				break;
			default:
				LOGGER.debug("\t{}\t: Pair status is {}, no need to continue.", this, status);
				return null;
		}

		if (bestShortSide < 0 || bestLongSide < 0) {
			LOGGER.debug("\t{}\t: Short side {} and/or long side {} volume not enough, no need to continue.", this, bestShortSide, bestLongSide);
			return null;
		}

		actualPriceForShortSide = bestShortSide;
		// and the use fx again to translate the bestBid price calculated from hkd to rmb
		effectivePriceForShortSide = actualPriceForShortSide*fx;
		effectivePriceForLongSide = actualPriceForLongSide = bestLongSide;

		LOGGER.debug("\t{}\t: Ratio: {}", this, effectivePriceForLongSide / effectivePriceForShortSide);
		if (effectivePriceForLongSide < 0 || effectivePriceForShortSide < 0) {
			LOGGER.debug("\t{}\t: Negative ratio is invalid! effectivePriceForLongSide={} / effectivePriceForShortSide={} / fx={}", this, effectivePriceForLongSide, effectivePriceForShortSide, fx);
			LOGGER.debug("\t{}\t: Investigation: depthToShort.bestBid(shortVolumeHeld*depthToShort.ask(1))={}", this, depthToShort.bestBid(shortVolumeHeld*depthToShort.ask(1)));
			LOGGER.debug("\t{}\t: Investigation: depthToLong.bestBid(longVolumeHeld*depthToLong.bid(1))={}", this, depthToLong.bestBid(longVolumeHeld*depthToLong.bid(1)));
			return null;
		}
		residualLatest = effectivePriceForShortSide * slope() / effectivePriceForLongSide - 1;
		return residualLatest;
	}

	@Override
	public void onDepthUpdate(DateTime now, com.numericalmethod.algoquant.execution.datatype.depth.Depth depth, MarketCondition mc, TradeBlotter blotter, Broker broker) {
		try {
			if (depth.product().equals(toShort())) {
				depthToShort = (Depth) depth;
			} else if (depth.product().equals(toLong())) {
				depthToLong = (Depth) depth;
			} else {
				LOGGER.warn("\t{}\t: Irrelevant depth!! {} should not be passed to me!", this, depth);
				return;
			}
		} catch (ClassCastException e) {
			LOGGER.warn("\t{}\t: Only michael.findata.algoquant.execution.datatype.depth.Depth is supported. Strategy depth update cancelled.", this);
			return;
		}
		double fx = fx();
		if (fx < 0) {
			LOGGER.debug("\t{}\t: FX rate not available, returning.", this);
			return;
		}
		LOGGER.debug("\t{}\t: Processing depth: {}", this, depth);
		//
		Double residual = residual();
		if (residual == null) {
			LOGGER.debug("\t{}\t: Residual not available, returning.", this);
			return;
		}
		if (System.currentTimeMillis() + 1000 * 60 * 3 > CommandCenter.secondHalfEndCN) {
			LOGGER.debug("\t{}\t: Trading session about to end in less than 3 minutes.", this);
			switch (status) {
				case NEW:
				case CLOSED:
				case FORCED:
				case OPENED:
					LOGGER.debug("\t{}\t: Status is {}, returning...", this, status);
					return;
				case OPENING:
					LOGGER.info("\t{}\t: Status is OPENING when trading session is about to end in less than 3 minutes.", this);
					if (openingOrderShort == null) {
						LOGGER.warn("\t{}\t: No short leg opening order while status is OPENING, returning...", this);
						return;
					}
					if (Order.OrderState.UNFILLED.equals(openingOrderShort.state())) {
						LOGGER.info("\t{}\t: Trying to cancel the short leg since 0% filled.", this, residual, openThreshold);
						cancelUnfilledOpening(broker);
						LOGGER.info("\t{}\t: Opening short leg has been cancelled, returning...", this);
						return;
					} else if (Order.OrderState.FILLED.equals(openingOrderShort.state())){
						LOGGER.warn("\t{}\t: Strange: pair status is opening, but short leg has already been fully filled: {}. Returning...", this, openingOrderShort);
						return;
					} else {
						// TODO: 11/20/2016 how to deal with partially filled opening short when today's trading is about to end?
						LOGGER.info("\t{}\t: TODO: Short leg {}, has been partially filled, when trading session is about to end.", this, openingOrderShort);
						// if filled portion is > 5000 then cancel the rest and quickly submit the long leg for immediate execution

						// if filled portion is <= 5000 then cancel it and issue a sell order at a price so that we can cover the cost;
					}
					return;
			}
		}
		switch (status) {
			case NEW:
				if (residual > openThreshold) {
					LOGGER.info("\t{}\t: Pair is currently NEW. Residual vs OpenThreshold: {} vs {} - above open threshold, trying to open the pair.", this, residual, openThreshold);
					// Close position:
					// Calculate suitable volume - it should minimize the amount difference between buy/sell
					double shortSideAvailableBidVol = depthToShort.totalBidAtOrAbove(actualPriceForShortSide);
					double longSideAvailableAskVol = depthToLong.totalAskAtOrBelow(actualPriceForLongSide);
					double[] sellBuyVol = calVolumes(
							actualPriceForShortSide*fx, shortSideAvailableBidVol, toShort().getLotSize(),
							actualPriceForLongSide, longSideAvailableAskVol, toLong().getLotSize(),
							amountUpperLimit, amountLowerLimit, PairStrategyUtil.BalanceOption.CLOSEST_MATCH,
							0.005, 0.01, 0.02, 0.05);
					if (sellBuyVol == null) {
						// unable to find suitable sell/buy volumes
						LOGGER.info("\t{}\t: Unable to find suitable sell/buy volumes with available bidVol/askVol {}/{}.", this, shortSideAvailableBidVol, longSideAvailableAskVol);
						return;
					}

					// Found suitable sell/buy volumes
					shortVolumeCalculated = sellBuyVol[0];
					longVolumeCalculated = sellBuyVol[1];
					LOGGER.info("\t{}\t: Calculated short/long volumes: {}/{} @ short/long prices: {}/{} with fx: {}", this,
							shortVolumeCalculated, longVolumeCalculated, actualPriceForShortSide, actualPriceForLongSide, fx);
					LOGGER.info("\t{}\t: Calculated short/long ratio: {}/{}", this,
							shortVolumeCalculated*actualPriceForShortSide*fx, longVolumeCalculated*actualPriceForLongSide);

					// Executed short order
					openingOrderShort = new HexinOrder(toShort(), shortVolumeCalculated, actualPriceForShortSide, HexinOrder.HexinType.SIMPLE_SELL);
					openingOrderShort.addTag(MetaBroker.ORDER_TAG_BROKER, shortSideBrokerTag);
					List<HexinOrder> orders = new ArrayList<>(1);
					orders.add(openingOrderShort);
					broker.sendOrder(orders);
					if (!openingOrderShort.submitted()) {
						// Order has not been submitted, something is wrong
						LOGGER.warn("\t{}\t: Is this simulated / back test?", this);
						LOGGER.warn("\t{}\t: If not, then something has gone wrong when submitting order {}", this, openingOrderShort);
					} else {
						if (broker instanceof michael.findata.algoquant.execution.component.broker.Broker) {
							((michael.findata.algoquant.execution.component.broker.Broker) broker).setOrderListener(openingOrderShort, this);
						}
					}
					LOGGER.info("\t{}\t: Short leg opening order submitted: {}", this, openingOrderShort);
					residualOpen = residual;
					maxResidual = residual;
					minResidual = residual;
					// Save after status change
					updateStatusAndSave(Status.OPENING);
				} else {
					LOGGER.debug("\t{}\t: Pair is currently new. Residual vs OpenThreshold: {} vs {} - still below threshold.", this, residual, openThreshold);
				}
				break;
			case OPENING:
				if (openingOrderShort == null) {
					LOGGER.warn("\t{}\t: No short leg opening order while status is OPENING, returning...", this);
					return;
				}
				switch (openingOrderShort.state()) {
					case UNFILLED:
						LOGGER.debug("\t{}\t: Pair opening, short leg not filled at all: {}", this, openingOrderShort);
						if (residual < openThreshold) {
							LOGGER.info("\t{}\t: Residual vs OpenThreshold: {} vs {} - drops below threshold, trying to cancel the short leg since 0% filled.", this, residual, openThreshold);
							// cancel opening
							cancelUnfilledOpening(broker);
						} else {
							LOGGER.debug("\t{}\t: Residual vs OpenThreshold: {} vs {} - above open threshold, keep opening...", this, residual, openThreshold);
							if (openingOrderShort.price() > actualPriceForShortSide) {
								LOGGER.info("\t{}\t: Best short price dropped, adjusting opening short order price from {} -> {}", this, openingOrderShort.price(), actualPriceForShortSide);
								// If current top level price is different from original short order price,
								// Adjust it and updated the short order price
								List<HexinOrder> orders = new ArrayList<>(1);
								orders.add(openingOrderShort);
								openingOrderShort.price(actualPriceForShortSide);
								broker.sendOrder(orders);
							} else {
								LOGGER.debug("\t{}\t: Short price still ok. {} <= {}. Do nothing.", this, openingOrderShort.price(), actualPriceForShortSide);
							}
							residualOpen = residual;
							maxResidual = residual;
							minResidual = residual;
						}
						break;
					case PARTIALLY_FILLED:
						LOGGER.debug("\t{}\t: Pair opening, short leg has been partially filled: {}", this, openingOrderShort);
						if (residual < openThreshold) {
							// TODO: 11/20/2016 how to deal with partially filled opening short? When residual drop below threshold hold?
							LOGGER.info("\t{}\t: TODO: Short leg has been partially filled, when residual [{}] dropped below threshold [{}].", this, residual, openThreshold);
							// if filled portion is > 5000 then cancel the rest and quickly submit the long leg for immediate execution
							// if filled portion is <= 5000 then cancel it and issue a sell order at a price so that we can cover the cost;
						} else {
							LOGGER.debug("\t{}\t: Residual vs OpenThreshold: {} vs {} - above open threshold, keep opening...", this, residual, openThreshold);
							if (openingOrderShort.price() > actualPriceForShortSide) {
								// TODO: 11/20/2016 how to deal with partially filled opening short when short side bid(1) has dropped?
								LOGGER.info("\t{}\t: TODO: Short leg has been partially filled while actual best short price dropped below short price. Adjust opening short order price? from {} -> {}", this, openingOrderShort.price(), actualPriceForShortSide);

								// If current top level price is different from original short order price,
								// Adjust it and updated the short order price
//							List<HexinOrder> orders = new ArrayList<>(1);
//							orders.add(openingOrderShort);
//							openingOrderShort.price(depth.bid(1));
//							broker.sendOrder(orders);

							} else {
								LOGGER.debug("\t{}\t: Short price <= shortDepth.bid (1). {} <= {}. Do nothing.", this, openingOrderShort.price(), actualPriceForShortSide);
							}
							residualOpen = residual;
							maxResidual = residual;
							minResidual = residual;
						}
						break;
					case FILLED:
						LOGGER.warn("\t{}\t: Pair opening, but short leg has already been fully filled: {}", this, openingOrderShort);
						break;
					default:
						LOGGER.warn("\t{}\t: Pair opening, unhandled order status: {}", this, openingOrderShort);
						break;
				}
				break;
			case OPENED:
				if (residual > maxResidual) {
					maxResidual = residual;
					maxResidualDate = new Timestamp(System.currentTimeMillis());
				}
				if (residual < minResidual) {
					minResidual = residual;
					minResidualDate = new Timestamp(System.currentTimeMillis());
				}
				if (residual < closeThreshold) {
					LOGGER.info("\t{}\t: Pair already opened. Residual vs CloseThreshold: {} vs {} - below close threshold, trying to close the pair.", this, residual, closeThreshold);
					if (!dateOpened.toLocalDateTime().toLocalDate().equals(
							new Timestamp(System.currentTimeMillis()).toLocalDateTime().toLocalDate())) {
						residualClose = residual;
						close(broker, now, false);
					} else {
						LOGGER.info("\t{}\t: Pair opened on the same day, cannot close.", this);
					}
				} else {
					LOGGER.debug("\t{}\t: Pair already opened. Residual vs CloseThreshold: {} vs {} - still above close threshold.", this, residual, closeThreshold);
				}
		}
	}

	private void cancelUnfilledOpening(Broker broker) {
		List<HexinOrder> orders = new ArrayList<>(1);
		orders.add(openingOrderShort);
		broker.cancelOrder(orders);
		reset(true);
	}

	/**
	 * reset to new, waiting to be opened
	 */
	private void reset(boolean save) {
		this.openingOrderShort = null;
		this.openingOrderLong = null;
		this.closingOrderShort = null;
		this.closingOrderLong = null;

		this.shortVolumeHeld = 0d;
		this.longVolumeHeld = 0d;
		this.shortOpen = null;
		this.longOpen = null;
		this.shortClose = null;
		this.longClose = null;
		this.dateOpened = null;
		this.dateClosed = null;
		this.maxResidual = null;
		this.minResidual = null;
		this.maxResidualDate = null;
		this.minResidualDate = null;
		this.shortVolumeCalculated = null;
		this.longVolumeCalculated = null;
		this.residualOpen = null;
		this.residualClose = null;
		if (save) {
			updateStatusAndSave(Status.NEW);
		} else {
			this.status = Status.NEW;
		}
	}

	/**
	 * forcefully = true: closed forcefully, quite likely making a loss
	 * closed as expected
	 */
	private void close(Broker broker, DateTime now, boolean forcefully) {
		// Close position:
		// Execute short buy back and long sell back
		closingOrderShort = new HexinOrder(toShort(), shortVolumeHeld, actualPriceForShortSide, HexinOrder.HexinType.SIMPLE_BUY);
		closingOrderShort.addTag(MetaBroker.ORDER_TAG_BROKER, shortSideBrokerTag);
		// Minus 5 cents to sell order to make sure it fills.
		closingOrderLong = new HexinOrder(toLong(), longVolumeHeld, actualPriceForLongSide-0.05, HexinOrder.HexinType.SIMPLE_SELL, MARKET_ORDER);
		closingOrderLong.addTag(MetaBroker.ORDER_TAG_BROKER, longSideBrokerTag);
		List<HexinOrder> orders = new ArrayList<>(2);
		orders.add(closingOrderShort);
		orders.add(closingOrderLong);
		broker.sendOrder(orders);
		if (!closingOrderShort.submitted()) {
			// Order has not been submitted, something is wrong
			LOGGER.warn("\t{}\t: Is this simulated / back test?", this);
			LOGGER.warn("\t{}\t: If not, then something has gone wrong when submitting order {}", this, closingOrderShort);
		}
		if (!closingOrderLong.submitted()) {
			// Order has not been submitted, something is wrong
			LOGGER.warn("\t{}\t: Is this simulated / back test?", this);
			LOGGER.warn("\t{}\t: If not, then something has gone wrong when submitting order {}", this, closingOrderLong);
		}
		shortClose = closingOrderShort.price();
		longClose = closingOrderLong.price();
//		shortVolumeHeld = 0d;
//		longVolumeHeld = 0d;
		dateClosed = new Timestamp(System.currentTimeMillis());
		// Save after status change
		updateStatusAndSave(forcefully ? Status.FORCED : Status.CLOSED);
		ShortInHKPairStrategy archive = archive();
		archive.emailNotification(forcefully ? "Pair Forcefully Closed" : "Pair Closed");
		openableDate = now.toLocalDate().toDate();
		reset(true);
		emailNotification("Pair Reset");
//		stats = ;
	}

	private void updateStatusAndSave(Status s) {
		LOGGER.info("\t{}\t: Saving to DB after status changed from {} to {}.", this, status, s);
		status = s;
		trySave();
	}

	private ShortInHKPairStrategy archive () {
		ShortInHKPairStrategy archive = new ShortInHKPairStrategy(stats);

		archive.stats(stats);
		archive.status = status;
		archive.openThreshold = openThreshold;
		archive.closeThreshold = closeThreshold;
		archive.amountUpperLimit = amountUpperLimit;
		archive.amountLowerLimit = amountLowerLimit;
		archive.dateOpened = dateOpened;
		archive.dateClosed = dateClosed;
		archive.longOpen = longOpen;
		archive.longClose = longClose;
		archive.shortOpen = shortOpen;
		archive.shortClose = shortClose;
		archive.maxResidual = maxResidual;
		archive.minResidual = minResidual;
		archive.maxResidualDate = maxResidualDate;
		archive.minResidualDate = minResidualDate;
		archive.shortVolumeCalculated = shortVolumeCalculated;
		archive.longVolumeCalculated = longVolumeCalculated;
		archive.shortVolumeHeld = shortVolumeHeld;
		archive.longVolumeHeld = longVolumeHeld;
		archive.residualOpen = residualOpen;
		archive.residualClose = residualClose;
		archive.openableDate = openableDate;
		archive.forceClosureDate = forceClosureDate;
		archive.shortSideBrokerTag = shortSideBrokerTag;
		archive.longSideBrokerTag = longSideBrokerTag;
		archive.repo = repo;

		if (repo != null) {
			repo.save(archive);
			LOGGER.info("\t{}\t: Archived myself to {}", this, archive);
		} else {
			LOGGER.warn("\t{}\t: Repo is null, cannot archive myself.", this);
		}
		return archive;
	}

	@Override
	public int compareTo(ShortInHKPairStrategy o) {
		return 0;
	}

	@Transient
	private ShortInHkPairStrategyRepository repo;

	@Override
	public void setRepository(CrudRepository repository) {
		repo = (ShortInHkPairStrategyRepository) repository;
	}

	public CrudRepository getRepository() {
		return repo;
	}

	// Used in email notification message body
	@Override
	public String notification () {
		return String.format(
				"<pre><b>%s</b>\n<b>ID:</b> %d\n<b>Status:</b> %s\n<b>Short-side:</b> %s-%s\n<b>Long-side:</b> %s-%s\n<b>Slope:</b> %.5f\n<b>Residual latest:</b> %.3f\n<b>Open threshold:</b> %.3f\n<b>Close threshold:</b> %.3f\n<b>Date opened:</b> %s\n<b>Date closed:</b> %s\n<b>Short-side open price:</b> %.4f\n<b>Long-side open price:</b> %.4f\n<b>Short-side close price:</b> %.4f\n<b>Long-side close price:</b> %.4f\n<b>Short volume held:</b> %.0f\n<b>Long volume held:</b> %.0f\n<b>Short volume calculated:</b> %.0f\n<b>Long volume calculated:</b> %.0f\n<b>Minimum residual:</b> %.4f\n<b>Maximum Residual:</b> %.4f\n<b>Minimum residual date:</b> %s\n<b>Maximum residual date:</b> %s\n<b>Residual open:</b> %.4f\n<b>Residual close:</b> %.4f\n<b>Openable date:</b> %s\n<b>Force closure date:</b> %s\n<b>Upper limit amount:</b> %.0f\n<b>Lower limit amount:</b> %.0f</pre>",
				"ShortInHKPairStrategy",
				id,
				status,
				getNameToShort(), getCodeToShort(),
				getNameToLong(), getCodeToLong(),
				slope(),
				residualLatest,
				openThreshold,
				closeThreshold,
				dateOpened,
				dateClosed,
				shortOpen,
				longOpen,
				shortClose,
				longClose,
				shortVolumeHeld,
				longVolumeHeld,
				shortVolumeCalculated,
				longVolumeCalculated,
				minResidual,
				maxResidual,
				minResidualDate,
				maxResidualDate,
				residualOpen,
				residualClose,
				openableDate,
				forceClosureDate,
				amountUpperLimit,
				amountLowerLimit
		);
	}

	@Override
	public Collection<Stock> getTargetSecurities() {
		Set<Stock> s = new HashSet<>(1);
		s.add(toLong());
		s.add(toShort());
		return s;
	}

	@Override
	public void orderUpdated(Order order, Broker broker) {
		LOGGER.info("\t{}\t: Order {} updated.", this, order);
		switch (status) {
			case NEW:
				LOGGER.warn("\t{}\t: Strategy status is OPENED when receiving order update.", this);
				LOGGER.warn("\t{}\t: This should only happen after opening short order is cancelled. And it is very likely caused by unhandled order update message.", this);
				break;
			case OPENING:
				LOGGER.info("\t{}\t: Strategy status is OPENING.", this);
				if (openingOrderShort.state().equals(Order.OrderState.FILLED)) {
					LOGGER.info("\t{}\t: Opening short order has just been fully filled, executing opening long order...", this);
					// If short order is fully filled, execute long order.
					// Add 5 cents to buy order to make sure it fills.
					openingOrderLong = new HexinOrder(toLong(), longVolumeCalculated, actualPriceForLongSide+0.05, HexinOrder.HexinType.SIMPLE_BUY);
					openingOrderLong.addTag(MetaBroker.ORDER_TAG_BROKER, longSideBrokerTag);
					List<HexinOrder> orders = new ArrayList<>(1);
					orders.add(openingOrderLong);
					broker.sendOrder(orders);
					if (!openingOrderLong.submitted()) {
						// Order has not been submitted, something is wrong
						LOGGER.warn("\t{}\t: Is this simulated / back test?", this);
						LOGGER.warn("\t{}\t: If not, then something has gone wrong when submitting order {}", this, openingOrderLong);
					}
					shortVolumeHeld = shortVolumeCalculated;
					longVolumeHeld = longVolumeCalculated;
					shortOpen = openingOrderShort.price();
					longOpen = openingOrderLong.price();
					dateOpened = new Timestamp(System.currentTimeMillis());
					maxResidualDate = dateOpened;
					minResidualDate = dateOpened;
					// Save after status change
					updateStatusAndSave(Status.OPENED);
					emailNotification("Pair Opened");
				}
				break;
			case OPENED:
				LOGGER.warn("\t{}\t: Strategy status is OPENED when receiving order update.", this);
				LOGGER.warn("\t{}\t: This should only happen immediately after using IB to open the long leg. And it is very likely caused by unhandled order update message.", this);
				break;
			case CLOSED:
				LOGGER.warn("\t{}\t: Strategy status is CLOSED when receiving order update.", this);
				LOGGER.warn("\t{}\t: This should only happen immediately after using IB to execute position closing orders. And it is very likely caused by unhandled order update message.", this);
				break;
			case FORCED:
				LOGGER.warn("\t{}\t: Strategy status is FORCED when receiving order update.", this);
				LOGGER.warn("\t{}\t: This should only happen immediately after using IB to execute position closing orders (forcefully). And it is very likely caused by unhandled order update message.", this);
				break;
		}
	}

	@Override
	public void onDividend(DateTime now, Dividend dividend, MarketCondition mc, TradeBlotter blotter, Broker broker) {
	}

	public enum Status {
		NEW("NEW"),
		OPENING("OPENING"),
		OPENED("OPENED"),
		CLOSED("CLOSED"),
		FORCED("FORCED");
		private final String label;
		Status(String label) {
			this.label = label;
		}
	}

	@Override
	public String toString () {
		return String.format("ShortInHKStrategy [ID: %d, %s->%s, s:%.3f, o:%.3f, c:%.3f, %s]", id, getNameToShort(), getNameToLong(), slope(), openThreshold, closeThreshold, status);
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object another) {
		return another != null
				&& another instanceof ShortInHKPairStrategy
				&& id == ((ShortInHKPairStrategy) another).id;
	}
}