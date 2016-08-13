package michael.findata.model.test;

import michael.findata.service.DividendService;
import michael.findata.service.StockPriceMinuteService;
import org.joda.time.DateTime;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.text.ParseException;

public class StockPriceMinuteTest {

	public static void main (String [] args) throws ParseException, IOException {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		StockPriceMinuteService sr = (StockPriceMinuteService) context.getBean("stockPriceMinuteService");
		DividendService ds = (DividendService) context.getBean("dividendService");
		DateTime start = new DateTime(2016, 8, 4, 0, 0);
		DateTime end = new DateTime(2016, 8, 5, 0, 0);
//		String [] codes = new String [] {""};
		DividendService.PriceAdjuster pa = ds.newPriceAdjuster(start.toLocalDate(), end.toLocalDate(), "601009", "000568");

//		sr.saveAllMinuteLines("600467", false, (short)27);

		long t = System.currentTimeMillis();
		sr.walk(start,
				end,
				new String [] {"601009", "000568", "510230"}, false,
				(date, data) -> {
					System.out.print(date);
					System.out.print("\tAdjusted:\t" + pa.adjust("601009", start.toLocalDate(), date.toLocalDate(), data.get("601009").getClose()/1000d));
					System.out.print("\tAdjusted:\t" + pa.adjust("000568", start.toLocalDate(), date.toLocalDate(), data.get("000568").getClose()/1000d));
					System.out.println("\tAdjusted:\t" + pa.adjust("510230", start.toLocalDate(), date.toLocalDate(), data.get("510230").getClose()/1000d));
				});
		System.out.println(System.currentTimeMillis() - t);
	}
}