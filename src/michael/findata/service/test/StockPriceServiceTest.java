package michael.findata.service.test;

import michael.findata.service.StockPriceService;
import org.joda.time.DateTime;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class StockPriceServiceTest {
	public static void main (String [] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/findata_spring.xml");
		StockPriceService sps = (StockPriceService) context.getBean("stockPriceService");
		sps.walk(new DateTime(2015, 6, 18, 0, 0), new DateTime(2015, 8, 24, 0, 0), 1000000, "000568", "600000", true,
				(date, pA, pB, amtA, amtB)->{});
	}
}