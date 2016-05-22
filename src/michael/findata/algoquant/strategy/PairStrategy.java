package michael.findata.algoquant.strategy;

import com.numericalmethod.algoquant.execution.component.broker.Broker;
import com.numericalmethod.algoquant.execution.component.tradeblotter.TradeBlotter;
import com.numericalmethod.algoquant.execution.datatype.depth.Depth;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import com.numericalmethod.algoquant.execution.datatype.product.Product;
import com.numericalmethod.algoquant.execution.strategy.Strategy;
import com.numericalmethod.algoquant.execution.strategy.handler.MarketConditionHandler;
import michael.findata.model.PairInstance;
import michael.findata.service.DividendService;
import michael.findata.util.Consumer2;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.sql.Timestamp;
import java.util.*;

import static michael.findata.model.PairInstance.PairStatus.CLOSED;
import static michael.findata.model.PairInstance.PairStatus.OPENED;

public class PairStrategy implements Strategy, MarketConditionHandler {

	/**
	 * 510500<->159902: 3dev->1.5dev
	 *
	 * A.
	 * 300ETF 1%+ -> 0.2%- unlimited position quota
	 * 510300,510310,510330,510360,160706,165309,512990,159919
	 ,
	 *
	 * B.
	 * avg (adf_p) <= 0.01 also unlimited position quota
	 *
	 * Strategy parameters
	 */
	private double amountPerSlot = 12500;
	private boolean allowSameDayClosure;
	double correlThreshold = 0.7d;
	double cointThreshold = 0.12d;
	double openThresholdCoefficient = 2.0d;

	Consumer2<Pair, Integer> relaxer = (pair, age) -> {
		// *** When Group Switches, switch the following
		// etf relaxing algo
		if (age <= 7) {
			pair.thresholdClose = pair.stdev * 1.0d;
		} else if (age <= 13) {
			pair.thresholdClose = pair.stdev * 1.0d;
		} else {
			pair.thresholdClose = pair.stdev * 1.5d;
		}

		// banking relaxing algo
//			if (age < 8) {
//				pair.thresholdClose = pair.stdev * 2.6;
//			} else if (age < 14) {
//				pair.thresholdClose = pair.stdev * 2.6;
//			} else {
//				pair.thresholdClose = pair.stdev * 2.8;
//			}
	};

	int maxShortsPerTickPerStock = 200;
	int maxNetPositionPerStock = 2000;
	/**
	 *  Strategy parameters end
	 */

	private Map<Product, Depth> depthMap = new HashMap<>();
	private PairInstance [] pairs;
	private List<PairInstance> executions = new ArrayList<>();
	private DividendService.PriceAdjuster adjuster;
	private LocalDate executionDate;

	public PairStrategy (LocalDate executionDate, DividendService.PriceAdjuster adjuster, Collection<PairInstance> pairs) {
		 this(executionDate, adjuster, pairs.toArray(new PairInstance[pairs.size()]));
	}

	public PairStrategy (LocalDate executionDate, DividendService.PriceAdjuster adjuster, PairInstance...pairs) {
		this.pairs = pairs;
//		Set<String> codes = new TreeSet<>();
		for (PairInstance pair : pairs) {
//			codes.add(pair.toShort().symbol().substring(0, 6));
//			codes.add(pair.toLong().symbol().substring(0, 6));
			pair.setThresholdOpen(pair.stdev() * openThresholdCoefficient);
			pair.setThresholdClose(pair.stdev() * 0.5);
		}
		this.adjuster = adjuster;
		this.executionDate = executionDate;
	}

	/**
	 * Executed when some product quotes change.
	 * Only products whose quotes have change are included in the MarketCondition
	 *
	 * @param now     the current time
	 * @param mc      the current market condition
	 * @param blotter the current trade blotter
	 * @param broker  a broker service
	 */
	@Override
	public void onMarketConditionUpdate(DateTime now, MarketCondition mc, TradeBlotter blotter, Broker broker) {
		depthMap.putAll(mc.depths());
		Set<Product> dirtyPrd = mc.depths().keySet();
		michael.findata.algoquant.execution.datatype.depth.Depth depthShort, depthLong;
		String codeShort, codeLong;
		double actualPriceShort, adjustedPriceShort, actualPriceLong, adjustedPriceLong;
		double residual;
		int volumeShort, volumeLong;
		for (PairInstance pair : pairs) {
			if (dirtyPrd.contains(pair.toShort()) || dirtyPrd.contains(pair.toLong())) {
				depthShort = (michael.findata.algoquant.execution.datatype.depth.Depth) depthMap.get(pair.toShort());
				if (depthShort == null || !depthShort.isTraded()) {
					continue;
				}
				depthLong = (michael.findata.algoquant.execution.datatype.depth.Depth) depthMap.get(pair.toLong());
				if (depthLong == null || !depthLong.isTraded()) {
					continue;
				}
				codeShort = pair.toShort().symbol().substring(0, 6);
				codeLong = pair.toLong().symbol().substring(0, 6);
//				adjShort = adjFuns.get(codeShort);
//				adjLong = adjFuns.get(codeLong);
				// todo: check if there is position left to open
				if (executionDate.toDate().getTime() == pair.getOpenableDate().getTime()) {
//				if (true) {
					// Note: if spotPrice > bid(1), according to Chinese short sell regulation,
					// you cannot set the sell price = bid(1), thus there is no guarantee that your short leg order
					// will be fulfilled. Therefore, in such case, we do not open a position
					if (depthShort.spotPrice() <= depthShort.bid(1)) {
						actualPriceShort = depthShort.spotPrice();
//						adjustedPriceShort = adjShort.apply((int)(actualPriceShort*1000))/1000d;
						adjustedPriceShort = adjuster.adjust(codeShort, pair.trainingStart(), executionDate, (int)(actualPriceShort*1000))/1000d;
						volumeShort = depthShort.totalBidAtOrAbove(actualPriceShort);

						actualPriceLong = depthLong.bestAsk(amountPerSlot);
//						adjustedPriceLong = adjLong.apply((int)(actualPriceLong*1000))/1000d;
						adjustedPriceLong = adjuster.adjust(codeLong, pair.trainingStart(), executionDate, (int)(actualPriceLong*1000))/1000d;
						volumeLong = depthLong.totalAskAtOrBelow(actualPriceLong);

						residual = adjustedPriceShort * pair.slope() - adjustedPriceLong;
						double possibleAmount = Math.min(actualPriceShort*volumeShort, actualPriceLong*volumeLong);
						// todo: check pair status etc.
						if (residual >= pair.getThresholdOpen() && possibleAmount > amountPerSlot) {
							// todo make it a method
							pair.setStatus(OPENED);
							pair.setDateOpened(new Timestamp(now.toDate().getTime()));
							pair.setShortOpen(actualPriceShort);
							pair.setLongOpen(actualPriceLong);
							pair.setMaxAmountPossibleOpen(possibleAmount);
							pair.setResidualOpen(residual);
							pair.setMaxResidualDate(new Timestamp(now.toDate().getTime()));
							pair.setMinResidualDate(new Timestamp(now.toDate().getTime()));
							pair.setMaxResidual(residual);
							pair.setMinResidual(residual);
							executions.add(pair.copy());
						}
					}
				}

				// todo check: if there is position left to close
				if (pair.getOpenableDate().getTime() < executionDate.toDate().getTime()) {
//				if (true) {
					long age = 0;
					if (pair.getStatus() == OPENED || pair.getStatus() == PairInstance.PairStatus.CLOSED || pair.getStatus() == PairInstance.PairStatus.FORCED) {
						age = pair.age(now);
					}

					// buy back short
					actualPriceShort = depthShort.bestAsk(amountPerSlot);
//					adjustedPriceShort = adjShort.apply((int)(actualPriceShort*1000))/1000d;
					adjustedPriceShort = adjuster.adjust(codeShort, pair.trainingStart(), executionDate, (int)(actualPriceShort*1000))/1000d;
					volumeShort = depthShort.totalAskAtOrBelow(actualPriceShort);

					// sell out toLong
					actualPriceLong = depthLong.bestBid(amountPerSlot);
//					adjustedPriceLong = adjLong.apply((int)(actualPriceLong*1000))/1000d;
					adjustedPriceLong = adjuster.adjust(codeLong, pair.trainingStart(), executionDate, (int)(actualPriceLong*1000))/1000d;
					volumeLong = depthLong.totalBidAtOrAbove(actualPriceLong);

					residual = adjustedPriceShort * pair.slope() - adjustedPriceLong;
					// todo: check pair status etc. && (allowSameDayClosure || age > 0 || StockGroups.TPlus0.contains(codeLong))
					double possibleAmount = Math.min(actualPriceShort*volumeShort, actualPriceLong*volumeLong);
					if (residual < pair.getThresholdClose() && possibleAmount > amountPerSlot) {
						// todo make it a method
						pair.setStatus(CLOSED);
						pair.setDateClosed(new Timestamp(now.toDate().getTime()));
						pair.setShortClose(actualPriceShort);
						pair.setLongClose(actualPriceLong);
						pair.setMaxAmountPossibleClose(possibleAmount);
						pair.setResidualClose(residual);
						pair.setMaxResidualDate(new Timestamp(now.toDate().getTime()));
						pair.setMinResidualDate(new Timestamp(now.toDate().getTime()));
						pair.setMaxResidual(residual);
						pair.setMinResidual(residual);
						executions.add(pair.copy());
					}
				}
			}
		}
	}

	public Collection<PairInstance> getExecutions () {
		return executions;
	}
}