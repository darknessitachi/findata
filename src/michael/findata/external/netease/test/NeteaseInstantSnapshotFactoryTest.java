package michael.findata.external.netease.test;

import com.numericalmethod.algoquant.data.cache.SequentialCache;
import com.numericalmethod.algoquant.data.cache.TimedEntry;
import com.numericalmethod.algoquant.execution.datatype.depth.Depth;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import com.numericalmethod.algoquant.execution.datatype.order.LimitOrder;
import com.numericalmethod.algoquant.execution.datatype.order.Order;
import com.numericalmethod.algoquant.execution.datatype.product.Product;
import michael.findata.algoquant.execution.component.broker.HexinBroker;
import michael.findata.algoquant.strategy.Pair;
import michael.findata.external.netease.NeteaseInstantSnapshotFactory;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static com.numericalmethod.algoquant.execution.datatype.order.BasicOrderDescription.Side.*;
import static michael.findata.util.LogUtil.getClassLogger;
import static michael.findata.util.CalculationUtil.*;

public class NeteaseInstantSnapshotFactoryTest {
//	private static final Logger LOGGER = getClassLogger();
//	public static void main (String [] args) {
//
//		long lastSilentMilli = System.currentTimeMillis();
//		HexinBroker hxBroker = new HexinBroker("���Ϲ�Ʊ����ϵͳ5.0", false);
//
//		// add the following into the pairs
//		// stocks with * can only open long positions
////		������Դ 000937.SZ	�Ϻ���Դ 600508.SS*
////		ƽú�ɷ� 601666.SS	���йɷ� 600997.SS*
////		������Դ 000027.SZ	ẽ�ˮ�� 600131.SS*
////		��Ԫ֤ȯ 000728.SZ	����֤ȯ 600030.SS
////		��ҵ֤ȯ 601377.SS	��̩֤ȯ 601688.SS
////		�й�ƽ�� 601318.SS	�й�̫�� 601601.SS
////		��Ȫúҵ 600348.SS	��Դú�� 600971.SS
////		������Դ 000027.SZ	���ܹɷ� 600483.SS
////		��Ͷ��Դ 600674.SS	��Ͷ���� 600886.SS
//
////		������� 600027.SS	���Ʒ��� 601991.SS -- little weak
////
//		// Pair Trading
//		Pair [] pairs = new Pair [2];
//		pairs[0] = new Pair(-1, new SZSEStock("000858.SZ"), new SZSEStock("000568.SZ"), 1.052362448, 1.083541845, 1.0032, false);
//		pairs[1] = new Pair(-1, new SZSEStock("000568.SZ"), new SZSEStock("000858.SZ"), 0.950242952, 0.983178327, 1.0032, false);
//		Depth depShort;
//		Depth depLong;
//		double setRatio; // ratio to determine whether we should start short pair1 & long pair2
//		double settleRatio; // ratio to determine whether we should settle the positions
//		double baseAmount = 40000;
//		// End Pair Trading
//
//		LimitOrder [] orders = new LimitOrder[] {
//				new LimitOrder(new SZSEStock("000568.SZ"), BUY, 200, 22.75)
//		};
//		HashSet<Product> stocks = new HashSet<>();
//		for (LimitOrder o: orders) {
//			stocks.add(o.product());
//		}
//		for (Pair pair : pairs) {
//			stocks.add(pair.toShort);
//			stocks.add(pair.toLong);
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
//			for (Pair pair : pairs) {
//				LOGGER.info("Pair: "+pair.toShort.symbol()+" -> "+pair.toLong.symbol()+".\tPositionHeld: " + pair.status);
//				depShort = depths.get(pair.toShort);
//				depLong = depths.get(pair.toLong);
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
//						LOGGER.info(pair.toShort.symbol() + " - can sell@" + shortPrice+";\t"+pair.toLong.symbol() + " - can buy@" + longPrice + ", normalized: " + longPrice * pair.getNormalizationRatio());
//						LOGGER.info("Current ratio for " + pair.toShort.symbol() + "->" + pair.toLong.symbol() + " : " + setRatio+";\tPositionThreshold: "+pair.getPositionThreshold());
//						if (setRatio > pair.open.getPositionThreshold()) {
//							// stock toShort is too over-priced
//							// short toShort & long toLong
//							LOGGER.info(
//									pair.toShort.symbol() + " is too over-priced. "
//									+ "Short " + pair.toShort.symbol() + "@" + shortPrice + ":" + targetShortPosition + " & long " + pair.toLong.symbol() + "@" + longPrice + ":" + targetLongPosition);
//							hxBroker.sendOrder(new LimitOrder(pair.toShort, SELL, targetShortPosition, shortPrice));
//							hxBroker.sendOrder(new LimitOrder(pair.toLong, BUY, targetLongPosition, longPrice));
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
//						LOGGER.info(pair.toShort.symbol() + " - can buy@" + buyShortPrice+";\t"+pair.toLong.symbol() + " - can sell@" + sellLongPrice + ", normalized: " + sellLongPrice * pair.getNormalizationRatio());
//						LOGGER.info("Current ratio for " + pair.toShort.symbol()+"<-"+pair.toLong.symbol()+" : "+settleRatio+";\tActual settle threshold: "+pair.getNormalizationRatio() * pair.getSettleRelaxation());
//						if (settleRatio < pair.getNormalizationRatio() * pair.getSettleRelaxation()) {
//							// toLong and toShort prices have converged
//							// sell toLong buy back toShort
//							LOGGER.info(
//									pair.toShort.symbol() + " and " + pair.toLong.symbol() +" converged. "
//									+ "Sell out " + pair.toLong.symbol() + "@" + sellLongPrice + ":" + pair.getLongPositionHeld() + " & buy back " + pair.toShort.symbol() + "@" + buyShortPrice + ":" + pair.getShortPositionHeld());
//							hxBroker.sendOrder(new LimitOrder(pair.toLong, SELL, pair.getLongPositionHeld(), sellLongPrice));
//							hxBroker.sendOrder(new LimitOrder(pair.toShort, BUY, pair.getShortPositionHeld(), buyShortPrice));
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