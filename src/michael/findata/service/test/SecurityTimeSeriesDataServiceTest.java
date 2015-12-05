package michael.findata.service.test;

import michael.findata.service.SecurityTimeSeriesDataService;
import org.joda.time.DateTime;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by nicky on 2015/12/5.
 */
public class SecurityTimeSeriesDataServiceTest {
	public static void main (String [] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/findata_spring.xml");
		SecurityTimeSeriesDataService stsds = (SecurityTimeSeriesDataService) context.getBean("securityTimeSeriesDataService");
		stsds.walkMinutes(new DateTime(2015, 8, 24, 0, 0), new DateTime(2015, 8, 27, 0, 0), 1000000, "600858", "600875", true,
				(date, pA, pB, amtA, amtB) -> {
				});
//		stsds.walkDays(new DateTime(2015, 8, 24, 0, 0), new DateTime(2015, 8, 27, 0, 0), 1000000, "600858", "600875", true,
//				(date, pA, pB, amtA, amtB) -> {
//				});
	}
}
