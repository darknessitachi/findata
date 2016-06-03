package michael.findata.data.historicaldata.tdx.test;

import com.numericalmethod.algoquant.data.cache.TimedEntry;
import com.numericalmethod.algoquant.execution.datatype.depth.Depth;
import michael.findata.data.historicaldata.tdx.TdxMinuteDepthCacheFactory;
import michael.findata.model.Stock;
import org.joda.time.DateTime;
import org.joda.time.Interval;

public class TdxMinuteDepthCacheFactoryTest {
	public static void main (String args []) {
		TdxMinuteDepthCacheFactory factory = new TdxMinuteDepthCacheFactory();
		for (TimedEntry<Depth> entry : factory.newInstance(new Stock("510300", "300ETF"), new Interval(DateTime.parse("2016-05-27"), DateTime.parse("2016-05-27").plusHours(23)))) {
			System.out.println(entry);
		}
	}
}