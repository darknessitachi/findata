package michael.findata.data.historicaldata.tdx.test;

import com.numericalmethod.algoquant.data.cache.SequentialCache;
import michael.findata.algoquant.execution.datatype.StockEOM;
import michael.findata.data.historicaldata.tdx.TdxEOMCacheFactory;
import michael.findata.model.Stock;
import org.joda.time.DateTime;
import org.joda.time.Interval;

public class TdxEOMCacheFactoryTest {
	public static void main (String [] args) {
		TdxEOMCacheFactory factory = new TdxEOMCacheFactory();
		SequentialCache<StockEOM> cache = factory.newInstance(new Stock("510050"), new Interval(DateTime.parse("2016-05-23"), DateTime.parse("2016-05-24").plusHours(23)));
		cache.forEach(System.out::println);
//		new YahooEODCacheFactory (SimTemplateYahooEOD.DEFAULT_DATA_FOLDER).newInstance(new SimpleStock("600000.SS", Currencies.CNY, Exchange.SHSE), new Interval(DateTime.parse("2016-05-23"), DateTime.parse("2016-05-27").plusHours(23)))
//		.forEach((x) -> System.out.println(x.data().close()));
	}
}