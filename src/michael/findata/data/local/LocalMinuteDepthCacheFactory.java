package michael.findata.data.local;

import com.numericalmethod.algoquant.data.cache.SequentialCache;
import com.numericalmethod.algoquant.data.cache.processor.CacheProcessor;
import com.numericalmethod.algoquant.data.cache.processor.transformer.EntryDataCacheTransformer;
import com.numericalmethod.algoquant.execution.datatype.depth.Depth;
import com.numericalmethod.algoquant.execution.datatype.depth.cache.DepthCacheFactory;
import com.numericalmethod.nmutils.iterator.DataTransformer;
import michael.findata.algoquant.execution.datatype.StockEOM;
import michael.findata.algoquant.execution.datatype.depth.cache.StockEOM2DepthTransformer;
import michael.findata.model.Stock;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class LocalMinuteDepthCacheFactory implements DepthCacheFactory<Stock> {

	@Autowired
	private final LocalEOMCacheFactory eom;

	public LocalMinuteDepthCacheFactory() {
		this.eom = new LocalEOMCacheFactory();
	}

	public SequentialCache<Depth> newInstance(Stock stock,
											  Interval interval,
											  DataTransformer<StockEOM, Depth> transformer) {
		SequentialCache<StockEOM> cache = eom.newInstance(stock, interval);
		CacheProcessor<StockEOM, Depth> processor = new EntryDataCacheTransformer<>(transformer);
		return processor.process(cache);
	}

	public SequentialCache<Depth> newInstance(Stock stock,
											  Interval interval,
											  StockEOM2DepthTransformer.Type conversionType) {
		DataTransformer<StockEOM, Depth> transformer = new StockEOM2DepthTransformer(stock, conversionType);
		return newInstance(stock, interval, transformer);
	}

	@Override
	public SequentialCache<Depth> newInstance(Stock stock, Interval interval) {
		return newInstance(stock, interval, StockEOM2DepthTransformer.Type.CLOSE);
	}

	public Map<Stock, SequentialCache<Depth>> newInstances(Collection<? extends Stock> stocks,
														   Interval interval,
														   Map<Stock, DataTransformer<StockEOM, Depth>> transformerMap) {
		Map<Stock, SequentialCache<Depth>> results = new HashMap<>();
		Map<Stock, SequentialCache<StockEOM>> cache = eom.newInstances(stocks, interval);
		cache.entrySet().forEach(entry -> {
			CacheProcessor<StockEOM, Depth> processor = new EntryDataCacheTransformer<>(transformerMap.get(entry.getKey()));
			results.put(entry.getKey(), processor.process(entry.getValue()));
		});
		return results;
	}

	public Map<Stock, SequentialCache<Depth>> newInstances(Collection<? extends Stock> stocks,
														   Interval interval,
														   StockEOM2DepthTransformer.Type conversionType) {
		HashMap<Stock, DataTransformer<StockEOM, Depth>> transformerMap = new HashMap<>();
		for (Stock stock : stocks) {
			transformerMap.put(stock, new StockEOM2DepthTransformer(stock, conversionType));
		}
		return newInstances(stocks, interval, transformerMap);
	}

	public Map<Stock, SequentialCache<Depth>> newInstances(Collection<? extends Stock> stocks, Interval interval) {
		return newInstances(stocks, interval, StockEOM2DepthTransformer.Type.CLOSE);
	}
}