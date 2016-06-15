package michael.findata.demo.aparapi;

import michael.findata.service.PairStrategyService;
import michael.findata.service.SecurityTimeSeriesDataService;
import michael.findata.service.StockPriceService;
import org.joda.time.DateTime;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static michael.findata.util.FinDataConstants.OPENCL_REGRESSION_SIZE;

public class PairTest {

	public static void main(String[] args) throws Exception {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		PairStrategyService pss = (PairStrategyService) context.getBean("pairStrategyService");
		SecurityTimeSeriesDataService stsds = (SecurityTimeSeriesDataService) context.getBean("securityTimeSeriesDataService");
		StockPriceService sps = (StockPriceService) context.getBean("stockPriceService");
		long start = System.currentTimeMillis();
		double [][] result = pss.cointcorrel(
				DateTime.parse("2016-04-03").withTimeAtStartOfDay(),
				DateTime.parse("2016-06-03").withTimeAtStartOfDay().plusHours(23),
				new String [][] {{"160706","510300"}}, OPENCL_REGRESSION_SIZE, stsds, sps, true);

		System.out.println("Time take: "+(System.currentTimeMillis() - start));
		// slope, std, correl, adf_p
		System.out.println(result[0][0]+"\t"+result[0][1]);
	}
}