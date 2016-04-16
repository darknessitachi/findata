package michael.findata.service.test;

import michael.findata.service.SecurityTimeSeriesDataService;
import org.joda.time.DateTime;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SecurityTimeSeriesDataServiceTest {
	public static void main (String [] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/findata_spring.xml");
		SecurityTimeSeriesDataService stsds = (SecurityTimeSeriesDataService) context.getBean("securityTimeSeriesDataService");
		stsds.walkMinutes(
				new DateTime(2015, 9, 23, 0, 0),
				new DateTime(2015, 9, 25, 0, 0),
				1000000, "601009", "000568", true,
				(date, pA, pB, amtA, amtB) -> {
				});

		stsds.walkMinutes(
				new DateTime(2015, 9, 23, 0, 0),
				new DateTime(2015, 9, 23, 0, 0),
				new DateTime(2015, 9, 25, 0, 0),
				1000000,
				new String [] {"601009", "000568"}, true,
				(date, data, adjFunctions) ->{
					System.out.println("Adjusted: " + adjFunctions.get("000568").apply(data.get("000568").getClose()));
					System.out.println("Adjusted: " + adjFunctions.get("601009").apply(data.get("601009").getClose()));
				});
	}
}