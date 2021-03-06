package michael.findata.util;

import com.numericalmethod.algoquant.data.cache.SequentialCache;
import com.numericalmethod.algoquant.data.cache.TimedEntry;
import com.numericalmethod.algoquant.execution.datatype.depth.Depth;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import com.numericalmethod.algoquant.execution.datatype.order.LimitOrder;
import com.numericalmethod.algoquant.execution.datatype.order.Order;
import com.numericalmethod.algoquant.execution.datatype.product.Product;
import michael.findata.algoquant.execution.component.broker.HexinBroker;
import michael.findata.algoquant.strategy.PairStrategy;
import michael.findata.external.netease.NeteaseInstantSnapshotFactory;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import static com.numericalmethod.algoquant.execution.datatype.order.BasicOrderDescription.Side.BUY;
import static com.numericalmethod.algoquant.execution.datatype.order.BasicOrderDescription.Side.SELL;
import static michael.findata.util.LogUtil.getClassLogger;
import static michael.findata.util.CalculationUtil.findBestPrice;

public class AutoOrder {
//	private static final Logger LOGGER = getClassLogger();
//	public static void main (String [] args) {
//
//		long lastSilentMilli = System.currentTimeMillis();
//		HexinBroker hxBroker = new HexinBroker("网上股票交易系统5.0", false);
//
//		// add the following into the pairs
//		// stocks with * can only open long positions
////		冀中能源 000937.SZ	上海能源 600508.SS*
////		平煤股份 601666.SS	开滦股份 600997.SS*
////		深圳能源 000027.SZ	岷江水电 600131.SS*
////		国元证券 000728.SZ	中信证券 600030.SS
////		兴业证券 601377.SS	华泰证券 601688.SS
////		中国平安 601318.SS	中国太保 601601.SS
////		阳泉煤业 600348.SS	恒源煤电 600971.SS
////		深圳能源 000027.SZ	福能股份 600483.SS
////		川投能源 600674.SS	国投电力 600886.SS
//
////		华电国际 600027.SS	大唐发电 601991.SS -- little weak
////
//		// Pair Trading
//		PairStrategy.Pair[] pairs = new PairStrategy.Pair[2];
//		pairs[0] = new PairStrategy.Pair(new SZSEStock("000858.SZ"), new SZSEStock("000568.SZ"), 1.052362448, 1.083541845, 1.0032, false);
//		pairs[1] = new PairStrategy.Pair(new SZSEStock("000568.SZ"), new SZSEStock("000858.SZ"), 0.950242952, 0.983178327, 1.0032, false);
//		Depth depShort;
//		Depth depLong;
//		double setRatio; // ratio to determine whether we should start short pair1 & long pair2
//		double settleRatio; // ratio to determine whether we should settle the positions
//		double baseAmount = 40000;
//		// End Pair Trading
//
//		LimitOrder[] orders = new LimitOrder[] {
//				new LimitOrder(new SZSEStock("000568.SZ"), BUY, 200, 22.75)
//		};
//		HashSet<Product> stocks = new HashSet<>();
//		for (LimitOrder o: orders) {
//			stocks.add(o.product());
//		}
//		for (PairStrategy.Pair pair : pairs) {
//			stocks.add(pair.getToShort());
//			stocks.add(pair.getToLong());
//		}
//		SequentialCache<MarketCondition> c = new NeteaseInstantSnapshotFactory().newInstance(9000, stocks.toArray(new Product[stocks.size()]));
//		Depth depth;
//		Iterator<TimedEntry<MarketCondition>> i = c.iterator();
//		while (i.hasNext()) {
//			System.out.println();
//			Map<Product, Depth> depths;
//			try {
//				depths = i.next().data().depths();
//			} catch (NullPointerException npe) {
//				LOGGER.info("NPE Caught!");
//				continue;
//			}
//			if ((System.currentTimeMillis() % 86400000) < 5400000 || 25200000 < (System.currentTimeMillis() % 86400000)) {
//				// Make sure time is between 9:30 AM and 3:00PM
//				LOGGER.info("Not during trading hours (between 9:30 AM and 3:00PM), skipping...");
//				continue;
//			}
//
//			// Pair Trading
//			for (PairStrategy.Pair pair : pairs) {
//				LOGGER.info("Pair: "+pair.getToShort().symbol()+" -> "+pair.getToLong().symbol()+".\tPositionHeld: " + pair.isPositionHeld());
//				depShort = depths.get(pair.getToShort());
//				depLong = depths.get(pair.getToLong());
//
//				if (!pair.isPositionHeld()) {
//					// Situations when we should construct position
//					// calculate position to short according to base amount;
//					long targetShortPosition = (long) (Math.floor(baseAmount / depShort.bid(1) / 100) * 100);
//					// calculate position to long according to position to short
//					long targetLongPosition = Math.round(targetShortPosition * depShort.bid(1) / depLong.ask(1) / 100) * 100;
//					LOGGER.info("Target short position: "+targetShortPosition+";\tTarget long position: "+targetLongPosition);
//					// calculate limit price to short
//					double shortPrice = findBestPrice(targetShortPosition, true, (michael.findata.algoquant.execution.datatype.depth.Depth) depShort);
//					// calculate limit price to long
//					double longPrice = findBestPrice(targetLongPosition, false, (michael.findata.algoquant.execution.datatype.depth.Depth) depLong);
//					LOGGER.info("Shorting price: "+shortPrice+";\tLonging price: "+longPrice);
//
//					if (shortPrice > 0 && longPrice > 0) {
//						setRatio = shortPrice / longPrice;
//						LOGGER.info(pair.getToShort().symbol() + " - can sell@" + shortPrice+";\t"+pair.getToLong().symbol() + " - can buy@" + longPrice + ", normalized: " + longPrice * pair.getNormalizationRatio());
//						LOGGER.info("Current ratio for " + pair.getToShort().symbol() + "->" + pair.getToLong().symbol() + " : " + setRatio+";\tPositionThreshold: "+pair.getPositionThreshold());
//						if (setRatio > pair.getPositionThreshold()) {
//							// stock toShort is too over-priced
//							// short toShort & long toLong
//							LOGGER.info(
//									pair.getToShort().symbol() + " is too over-priced. "
//											+ "Short " + pair.getToShort().symbol() + "@" + shortPrice + ":" + targetShortPosition + " & long " + pair.getToLong().symbol() + "@" + longPrice + ":" + targetLongPosition);
//							hxBroker.sendOrder(new LimitOrder(pair.getToShort(), SELL, targetShortPosition, shortPrice));
//							hxBroker.sendOrder(new LimitOrder(pair.getToLong(), BUY, targetLongPosition, longPrice));
//							pair.setPositionHeld(true);
//							pair.setShortPositionHeld(targetShortPosition);
//							pair.setLongPositionHeld(targetLongPosition);
//						}
//					} else {
//						LOGGER.info("Sadly, amount not enough in the depths provided.");
//					}
//				} else {
//					// Situations when we should settle (clear) position
//					LOGGER.info("Short position held: "+pair.getShortPositionHeld()+";\tLong position held: "+pair.getLongPositionHeld());
//					// calculate limit price to buy back short position
//					double buyShortPrice = findBestPrice(pair.getShortPositionHeld(), false, (michael.findata.algoquant.execution.datatype.depth.Depth) depShort);
//					// calculate limit price to sell out long position
//					double sellLongPrice = findBestPrice(pair.getLongPositionHeld(), true, (michael.findata.algoquant.execution.datatype.depth.Depth) depLong);
//					LOGGER.info("Short buy back price: "+buyShortPrice+";\tLong sell out price: "+sellLongPrice);
//					if (buyShortPrice > 0 && sellLongPrice > 0) {
//						settleRatio = buyShortPrice / sellLongPrice;
//						LOGGER.info(pair.getToShort().symbol() + " - can buy@" + buyShortPrice+";\t"+pair.getToLong().symbol() + " - can sell@" + sellLongPrice + ", normalized: " + sellLongPrice * pair.getNormalizationRatio());
//						LOGGER.info("Current ratio for " + pair.getToShort().symbol()+"<-"+pair.getToLong().symbol()+" : "+settleRatio+";\tActual settle threshold: "+pair.getNormalizationRatio() * pair.getSettleRelaxation());
//						if (settleRatio < pair.getNormalizationRatio() * pair.getSettleRelaxation()) {
//							// toLong and toShort prices have converged
//							// sell toLong buy back toShort
//							LOGGER.info(
//									pair.getToShort().symbol() + " and " + pair.getToLong().symbol() +" converged. "
//											+ "Sell out " + pair.getToLong().symbol() + "@" + sellLongPrice + ":" + pair.getLongPositionHeld() + " & buy back " + pair.getToShort().symbol() + "@" + buyShortPrice + ":" + pair.getShortPositionHeld());
//							hxBroker.sendOrder(new LimitOrder(pair.getToLong(), SELL, pair.getLongPositionHeld(), sellLongPrice));
//							hxBroker.sendOrder(new LimitOrder(pair.getToShort(), BUY, pair.getShortPositionHeld(), buyShortPrice));
//							pair.setPositionHeld(false);
//							pair.setShortPositionHeld(0);
//							pair.setLongPositionHeld(0);
//						}
//					} else {
//						LOGGER.info("Sadly, amount not enough in the depths provided.");
//					}
//				}
//			}
//			// End Pair Trading
//
//			for (LimitOrder o : orders) {
//				if (Order.OrderState.UNFILLED.equals(o.state())) {
//					depth = depths.get(o.product());
////					LOGGER.info("Depth: " + depth);
//					if (BUY.equals(o.side()) && depth.ask(1) <= o.price() && depth.ask(1) != 0.0) {
//						LOGGER.info("Depth: " + depth);
//						hxBroker.sendOrder(new LimitOrder(o.product(), BUY, o.quantity(), depth.ask(1)));
//						o.markAsFilled();
//					} else if (SELL.equals(o.side()) && depth.bid(1) >= o.price() && depth.bid(1) != 0.0) {
//						LOGGER.info("Depth: " + depth);
//						hxBroker.sendOrder(new LimitOrder(o.product(), SELL, o.quantity(), depth.bid(1)));
//						o.markAsFilled();
//					}
//				}
//			}
//		}
//	}
}
