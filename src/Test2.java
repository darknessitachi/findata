import michael.findata.external.tdx.TDXClient;
import michael.findata.service.DividendService;
import michael.findata.service.StockPriceMinuteService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.text.ParseException;

public class Test2 {
	public static void main (String [] args) throws ParseException, IOException {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		StockPriceMinuteService sr = (StockPriceMinuteService) context.getBean("stockPriceMinuteService");
		DividendService ds = (DividendService) context.getBean("dividendService");
		TDXClient tdxClient = (TDXClient) context.getBean("tdxClient");
		// Need to update data in 70 trading days
		sr.updateMinuteData();
		ds.refreshDividendDataForInterestingFunds(tdxClient);
	}
}