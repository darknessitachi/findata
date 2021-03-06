package michael.findata.service.test;

import michael.findata.service.DividendService;
import michael.findata.service.SecurityTimeSeriesDataService;
import org.joda.time.DateTime;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SecurityTimeSeriesDataServiceTest {
	public static void main (String [] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		SecurityTimeSeriesDataService stsds = (SecurityTimeSeriesDataService) context.getBean("securityTimeSeriesDataService");
		DividendService ds = (DividendService) context.getBean("dividendService");
		DateTime start = new DateTime(2016, 4, 16, 0, 0);
		DateTime end = new DateTime(2016, 4, 18, 0, 0);
//		String [] codes = new String [] {""};
		DividendService.PriceAdjuster pa = ds.newPriceAdjuster(start.toLocalDate(), end.toLocalDate(), "601009", "000568");

		long t = System.currentTimeMillis();
		stsds.walkMinutes(
				start,
				end,
				1000000,
				new String [] {"000001", "600999", "002142", "510230"}, false,
				(date, data) ->{
					System.out.print(date);
					System.out.print("\tAdjusted:\t" + pa.adjust("000001", start.toLocalDate(), date.toLocalDate(), data.get("000001").getClose()/1000d));
					System.out.print("\tAdjusted:\t" + pa.adjust("600999", start.toLocalDate(), date.toLocalDate(), data.get("600999").getClose()/1000d));
					System.out.print("\tAdjusted:\t" + pa.adjust("002142", start.toLocalDate(), date.toLocalDate(), data.get("002142").getClose()/1000d));
					System.out.println("\tAdjusted:\t" + pa.adjust("510230", start.toLocalDate(), date.toLocalDate(), data.get("510230").getClose()/1000d));
				});

//		stsds.walkMinutes(
//				new DateTime(2015, 9, 23, 0, 0),
//				new DateTime(2015, 9, 25, 0, 0),
//				1000000, "601009", "000568", false,
//				(date, pA, pB, amtA, amtB) -> {
//					System.out.print(date);
//					System.out.print("\tAdjusted:\t" + pA);
//					System.out.println("\tAdjusted:\t" + pB);
//				});

//		stsds.walkMinutes(
//				start,
//				start,
//				end,
//				1000000,
//				new String [] {"601009", "000568"}, false,
//				(date, data, adjFunctions) ->{
//					System.out.print(date);
//					System.out.print("\tAdjusted:\t" + adjFunctions.get("601009").apply(data.get("601009").getClose())/1000d);
//					System.out.println("\tAdjusted:\t" + adjFunctions.get("000568").apply(data.get("000568").getClose())/1000d);
//				});
		System.out.println(System.currentTimeMillis() - t);
	}
}