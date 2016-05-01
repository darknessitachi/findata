package michael.findata.algoquant.strategy;

import com.numericalmethod.algoquant.execution.component.broker.Broker;
import com.numericalmethod.algoquant.execution.component.tradeblotter.TradeBlotter;
import com.numericalmethod.algoquant.execution.datatype.depth.Depth;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import com.numericalmethod.algoquant.execution.datatype.product.Product;
import com.numericalmethod.algoquant.execution.strategy.Strategy;
import com.numericalmethod.algoquant.execution.strategy.handler.MarketConditionHandler;
import michael.findata.algoquant.strategy.pair.StockGroups;
import michael.findata.model.AdjFunction;
import michael.findata.service.DividendService;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;
import java.util.function.Function;

import static michael.findata.algoquant.strategy.Pair.PairStatus.CLOSED;
import static michael.findata.algoquant.strategy.Pair.PairStatus.FORCED;
import static michael.findata.algoquant.strategy.Pair.PairStatus.OPENED;

public class PairStrategy implements Strategy, MarketConditionHandler {

	Map<Product, Depth> depthMap = new HashMap<>();
	Pair [] pairs;
	double amountPerSlot;
	boolean allowSameDayClosure;
	Map<String, Function<Integer, Integer>> adjFuns;

	public PairStrategy (LocalDate executionDate, JdbcTemplate jdbcTemplate, Pair...pairs) {
		this.pairs = pairs;
		Set<String> codes = new TreeSet<>();
		for (Pair pair : pairs) {
			codes.add(pair.toShort.symbol().substring(0, 6));
			codes.add(pair.toLong.symbol().substring(0, 6));
		}
		adjFuns = getAdjFunctions(pairs[0].trainingStart, executionDate, codes.toArray(new String[codes.size()]), jdbcTemplate);
	}

	public static Map<String, Function<Integer, Integer>> getAdjFunctions(LocalDate start, LocalDate end, String [] codes, JdbcTemplate jdbcTemplate) {
		Map<String, Function<Integer, Integer>> adjFuns = new HashMap<>();
		try {
			DividendService.getAdjFunctions(start, end, codes, jdbcTemplate)
					.entrySet().forEach(entry ->{
				Stack<AdjFunction<Integer, Integer>> adjFunStack = entry.getValue();
				Function<Integer, Integer> adjFunction = null;
				while (!adjFunStack.isEmpty()) {
					if (adjFunction == null) {
						adjFunction = adjFunStack.pop();
					} else {
						adjFunction = adjFunction.andThen(adjFunStack.pop());
					}
				}
				adjFuns.put(entry.getKey(), adjFunction);
			});
		} catch (Exception e) {
			// no adj factor found
		}
		return adjFuns;
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
		Function<Integer, Integer> adjShort, adjLong;
		for (Pair pair : pairs) {
			if (dirtyPrd.contains(pair.toShort) || dirtyPrd.contains(pair.toLong)) {
				depthShort = (michael.findata.algoquant.execution.datatype.depth.Depth) depthMap.get(pair.toShort);
				if (!depthShort.isTraded()) {
					continue;
				}
				depthLong = (michael.findata.algoquant.execution.datatype.depth.Depth) depthMap.get(pair.toLong);
				if (!depthLong.isTraded()) {
					continue;
				}
				codeShort = pair.toShort.symbol().substring(0, 6);
				codeLong = pair.toLong.symbol().substring(0, 6);
				adjShort = adjFuns.get(codeShort);
				adjLong = adjFuns.get(codeLong);
				// todo: check if there is position left to open
				if (true) {
					// if spotPrice > bid(1), according to Chinese short sell regulation,
					// you cannot set the sell price = bid(1), thus there is no guarantee that your short leg order
					// will be fulfilled. Therefore, in such case, we do not open any position
					if (depthShort.spotPrice() <= depthShort.bid(1)) {
						actualPriceShort = depthShort.spotPrice();
						adjustedPriceShort = adjShort.apply((int)(actualPriceShort*1000))/1000d;
						volumeShort = depthShort.totalBidAtOrAbove(actualPriceShort);

						actualPriceLong = depthLong.bestAsk(amountPerSlot);
						adjustedPriceLong = adjLong.apply((int)(actualPriceLong*1000))/1000d;
						volumeLong = depthLong.totalAskAtOrBelow(actualPriceLong);

						residual = adjustedPriceShort * pair.slope - adjustedPriceLong;
						// todo: check pair status etc.
						if (residual >= pair.thresholdOpen) {
							System.out.println("Open"); // todo
						}
					}
				}

				// todo check: if there is position left to close
				if (true) {
					long age = 0;
					if (pair.status == OPENED || pair.status == CLOSED || pair.status == FORCED) {
						age = pair.age(now);
					}

					// buy back short
					actualPriceShort = depthShort.bestAsk(amountPerSlot);
					adjustedPriceShort = adjShort.apply((int)(actualPriceShort*1000))/1000d;
					volumeShort = depthShort.totalAskAtOrBelow(actualPriceShort);

					// sell out toLong
					actualPriceLong = depthLong.bestBid(amountPerSlot);
					adjustedPriceLong = adjLong.apply((int)(actualPriceLong*1000))/1000d;
					volumeLong = depthLong.totalBidAtOrAbove(actualPriceLong);

					residual = adjustedPriceShort * pair.slope - adjustedPriceLong;
					// todo: check pair status etc.
					if (residual < pair.thresholdClose && (allowSameDayClosure || age > 0 || StockGroups.TPlus0.contains(codeLong))) {
						System.out.println("Close"); // todo
					}
				}
			}
		}
	}

//	public static class Pair {
//		private Product toShort;
//		private Product toLong;
//		private double normalizationRatio; // price ratio falls into this threshold and we settle the position
//		private double positionThreshold; // price ratio rises over this threshold and we set position
//		private double settleRelaxation; // relax the settling a little bit (1 means we don't want to relax it)
//		private boolean positionHeld; //True means "short & long " position already taken, next thing to do is to settle it when price converges.
//		private long shortPositionHeld;
//		private long longPositionHeld;
//
//		public Pair(Product toShort, Product toLong, double normalizationRatio, double positionThreshold, double settleRelaxation, boolean positionHeld) {
//			this.toShort = toShort;
//			this.toLong = toLong;
//			this.normalizationRatio = normalizationRatio;
//			this.positionThreshold = positionThreshold;
//			this.settleRelaxation = settleRelaxation;
//			this.positionHeld = positionHeld;
//		}
//
//		public Product getToShort() {
//			return toShort;
//		}
//
//		public Product getToLong() {
//			return toLong;
//		}
//
//		public double getNormalizationRatio() {
//			return normalizationRatio;
//		}
//
//		public double getPositionThreshold() {
//			return positionThreshold;
//		}
//
//		public double getSettleRelaxation() {
//			return settleRelaxation;
//		}
//
//		public boolean isPositionHeld() {
//			return positionHeld;
//		}
//
//		public void setPositionHeld(boolean positionHeld) {
//			this.positionHeld = positionHeld;
//		}
//
//		public long getShortPositionHeld() {
//			return shortPositionHeld;
//		}
//
//		public void setShortPositionHeld(long shortPositionHeld) {
//			this.shortPositionHeld = shortPositionHeld;
//		}
//
//		public long getLongPositionHeld() {
//			return longPositionHeld;
//		}
//
//		public void setLongPositionHeld(long longPositionHeld) {
//			this.longPositionHeld = longPositionHeld;
//		}
//	}
}