package michael.findata.external.netease;

import com.numericalmethod.algoquant.data.cache.SequentialCache;
import com.numericalmethod.algoquant.data.cache.TimedEntry;
import com.numericalmethod.algoquant.execution.datatype.depth.Depth;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.SimpleMarketCondition;
import com.numericalmethod.algoquant.execution.datatype.product.Product;
import org.joda.time.DateTime;
import org.slf4j.Logger;

import java.util.*;

import static com.numericalmethod.nmutils.NMUtils.getClassLogger;

/**
 * Created by nicky on 2015/8/23.
 */
public class NeteaseInstantSnapshotFactory {

	private static final Logger LOGGER = getClassLogger();

	public SequentialCache<MarketCondition> newInstance(Product ... p) {
		return new SequentialCache<MarketCondition> () {
			private final Timer timer = new Timer();
			private Product [] products = p;
			private String [] productCodes = (String[]) Arrays.stream(products).map(prod -> prod.symbol().substring(0, 6)).toArray(String[]::new);
			private Depth [] updatedDepths;
			{
				timer.scheduleAtFixedRate(new TimerTask() {
					@Override
					public void run() {
						updatedDepths = new NeteaseInstantSnapshot(productCodes).getDepths();
						LOGGER.info("Got depths, size: "+updatedDepths.length);
						LOGGER.info("Calling notifyAll.");
						synchronized (timer) {
							timer.notifyAll();
						}
					}
				}, 0, 15000);
			}
			@Override
			public Iterator<TimedEntry<MarketCondition>> iterator() {

				return new Iterator<TimedEntry<MarketCondition>>() {
					@Override
					public boolean hasNext() {
						return true;
					}

					@Override
					public TimedEntry<MarketCondition> next() {
						try {
							LOGGER.info("Calling wait.");
							synchronized (timer) {
								timer.wait();
							}
							return constructUpdatedMarketConditionTimedEntry();
						} catch (InterruptedException e) {
							return constructUpdatedMarketConditionTimedEntry();
						}
					}

					private TimedEntry<MarketCondition> constructUpdatedMarketConditionTimedEntry() {
						LOGGER.info("Calling constructUpdatedMarketConditionTimedEntry, updatedDepths.length: "+updatedDepths.length);
						HashMap<Product, Depth> hashMap = new HashMap<>();
						for (Depth depth : updatedDepths) {
							hashMap.put(depth.product(), depth);
						}
						return new TimedEntry<>(DateTime.now(), new SimpleMarketCondition(hashMap));
					}
				};
			}
		};
	}
}