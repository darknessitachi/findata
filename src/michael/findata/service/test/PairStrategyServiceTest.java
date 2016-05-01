package michael.findata.service.test;

import com.numericalmethod.algoquant.data.calendar.HolidayCalendarFromYahoo;
import com.numericalmethod.algoquant.execution.datatype.product.stock.Exchange;
import michael.findata.service.PairStrategyService;
import org.joda.time.LocalDate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class PairStrategyServiceTest {

//	2015-10-10 is a holiday.
//	2015-10-11 is a holiday.
//
//	2015-10-17 is a holiday.
//	2015-10-18 is a holiday.
//
//	2015-10-24 is a holiday.
//	2015-10-25 is a holiday.
//
//	2015-10-31 is a holiday.
//	2015-11-01 is a holiday.
//
//	2015-11-07 is a holiday.
//	2015-11-08 is a holiday.
//
//	2015-11-14 is a holiday.
//	2015-11-15 is a holiday.
//
//	2015-11-21 is a holiday.
//	2015-11-22 is a holiday.
//
//	2015-11-28 is a holiday.
//	2015-11-29 is a holiday.
//
//	2015-12-05 is a holiday.
//	2015-12-06 is a holiday.
//
//	2015-12-12 is a holiday.
//	2015-12-13 is a holiday.
//
//	2015-12-19 is a holiday.
//	2015-12-20 is a holiday.
//
//	2015-12-26 is a holiday.
//	2015-12-27 is a holiday.
//
//	2016-01-01 is a holiday.
//	2016-01-02 is a holiday.
//	2016-01-03 is a holiday.
//
//	2016-01-09 is a holiday.
//	2016-01-10 is a holiday.
//
//	2016-01-16 is a holiday.
//	2016-01-17 is a holiday.
//
//	2016-01-23 is a holiday.
//	2016-01-24 is a holiday.
//
//	2016-01-30 is a holiday.
//	2016-01-31 is a holiday.

//	2016-02-06 is a holiday.
//	2016-02-07 is a holiday.
//	2016-02-08 is a holiday.
//	2016-02-09 is a holiday.
//	2016-02-10 is a holiday.
//	2016-02-11 is a holiday.
//	2016-02-12 is a holiday.
//	2016-02-13 is a holiday.
//	2016-02-14 is a holiday.
//
//	2016-02-20 is a holiday.
//	2016-02-21 is a holiday.
//
//	2016-02-27 is a holiday.
//	2016-02-28 is a holiday.
//
//	2016-03-05 is a holiday.
//	2016-03-06 is a holiday.
//
//	2016-03-12 is a holiday.
//	2016-03-13 is a holiday.
//
//	2016-03-19 is a holiday.
//	2016-03-20 is a holiday.
//
//	2016-03-26 is a holiday.
//	2016-03-27 is a holiday.
//
//	2016-04-02 is a holiday.
//	2016-04-03 is a holiday.
//	2016-04-04 is a holiday.
//
//	2016-04-09 is a holiday.
//	2016-04-10 is a holiday.
//
//	2016-04-16 is a holiday.
//	2016-04-17 is a holiday.
//
//	2016-04-23 is a holiday.
//	2016-04-24 is a holiday.

	public static void main (String args []) throws IOException {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		PairStrategyService pss = (PairStrategyService) context.getBean("pairStrategyService");
//		pss.updatePairs(StockGroups.ETFBlueChipsCodes);
//		pss.updatePairs(StockGroups.ETFSmallCapCodes);
		HolidayCalendarFromYahoo cal = HolidayCalendarFromYahoo.forExchange(Exchange.SHSE);
		LocalDate simulationStart = LocalDate.parse("2016-04-28");
		int count = 0;
		while (count < 2) {
//			if (!cal.isHoliday(simulationStart.toDateTimeAtStartOfDay().plusHours(2))) {
//				LocalDate trainingEnd = simulationStart.minusDays(1);
//				LocalDate trainingStart = trainingEnd.minusDays(60);
//				System.out.println("Calculating stats for "+trainingStart+" - "+trainingEnd);
//				pss.calculateStats(trainingStart, trainingEnd);
//				count ++;
//			} else {
//				System.out.println(simulationStart+" is a holiday.");
//			}

			LocalDate trainingEnd = simulationStart.minusDays(1);
			LocalDate trainingStart = trainingEnd.minusDays(60);
			System.out.println("Calculating stats for "+trainingStart+" - "+trainingEnd);
			pss.calculateStats(trainingStart, trainingEnd);
			count ++;

			simulationStart = simulationStart.plusDays(1);
		}
	}
}