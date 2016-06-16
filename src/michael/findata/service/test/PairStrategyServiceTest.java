package michael.findata.service.test;

import com.numericalmethod.algoquant.data.calendar.HolidayCalendarFromYahoo;
import com.numericalmethod.algoquant.execution.datatype.product.stock.Exchange;
import michael.findata.external.tdx.TDXClient;
import michael.findata.service.DividendService;
import michael.findata.service.PairStrategyService;
import michael.findata.service.StockService;
import michael.findata.spring.data.repository.PairRepository;
import michael.findata.util.FinDataConstants;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class PairStrategyServiceTest {
//	2015-10-10 is a holiday.*
//	2015-10-11 is a holiday.*
//
//	2015-10-17 is a holiday.*
//	2015-10-18 is a holiday.*
//
//	2015-10-24 is a holiday.*
//	2015-10-25 is a holiday.*
//
//	2015-10-31 is a holiday.*
//	2015-11-01 is a holiday.*
//
//	2015-11-07 is a holiday.*
//	2015-11-08 is a holiday.*
//
//	2015-11-14 is a holiday.*
//	2015-11-15 is a holiday.*
//
//	2015-11-21 is a holiday.*
//	2015-11-22 is a holiday.*
//
//	2015-11-28 is a holiday.*
//	2015-11-29 is a holiday.*
//
//	2015-12-05 is a holiday.*
//	2015-12-06 is a holiday.*
//
//	2015-12-12 is a holiday.*
//	2015-12-13 is a holiday.*
//
//	2015-12-19 is a holiday.*
//	2015-12-20 is a holiday.*
//
//	2015-12-26 is a holiday.*
//	2015-12-27 is a holiday.*
//
//	2016-01-01 is a holiday.*
//	2016-01-02 is a holiday.*
//	2016-01-03 is a holiday.*
//
//	2016-01-09 is a holiday.*
//	2016-01-10 is a holiday.*
//
//	2016-01-16 is a holiday.*
//	2016-01-17 is a holiday.*
//
//	2016-01-23 is a holiday.*
//	2016-01-24 is a holiday.*
//
//	2016-01-30 is a holiday.*
//	2016-01-31 is a holiday.*

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
//	2016-02-20 is a holiday.*
//	2016-02-21 is a holiday.*
//
//	2016-02-27 is a holiday.*
//	2016-02-28 is a holiday.*
//
//	2016-03-05 is a holiday.*
//	2016-03-06 is a holiday.*
//
//	2016-03-12 is a holiday.*
//	2016-03-13 is a holiday.*
//
//	2016-03-19 is a holiday.*
//	2016-03-20 is a holiday.*
//
//	2016-03-26 is a holiday.*
//	2016-03-27 is a holiday.*
//
//	2016-04-02 is a holiday.*
//	2016-04-03 is a holiday.*
//	2016-04-04 is a holiday.*
//
//	2016-04-09 is a holiday.*
//	2016-04-10 is a holiday.*
//
//	2016-04-16 is a holiday.*
//	2016-04-17 is a holiday.*
//
//	2016-04-23 is a holiday.*
//	2016-04-24 is a holiday.*

	public static void main1 (String args []) throws IOException{
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		PairStrategyService pss = (PairStrategyService) context.getBean("pairStrategyService");
		StockService ss = (StockService) context.getBean("stockService");
//		pss.updatePairs(ss.getStockGroup("michael/findata/algoquant/strategy/pair/group_hk.csv").toArray(new String[0]));
//		pss.updatePairs(ss.getStockGroup("michael/findata/algoquant/strategy/pair/group_gold.csv").toArray(new String[0]));
//		pss.updatePairs(ss.getStockGroup("michael/findata/algoquant/strategy/pair/group_domestic.csv").toArray(new String[0]));
		pss.updatePairs(ss.getStockGroup("michael/findata/algoquant/strategy/pair/group_stock.csv").toArray(new String[0]));
	}

	public static void main (String args []) throws IOException {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		PairStrategyService pss = (PairStrategyService) context.getBean("pairStrategyService");

		HolidayCalendarFromYahoo cal = HolidayCalendarFromYahoo.forExchange(Exchange.SHSE);
		LocalDate simulationStart = LocalDate.parse("2016-06-17");
		LocalDate lastStart = LocalDate.parse("2016-06-17");
		LocalDate today = new LocalDate();
		while (!simulationStart.isAfter(lastStart)) {
			if (simulationStart.getDayOfWeek() != DateTimeConstants.SATURDAY &&
				simulationStart.getDayOfWeek() != DateTimeConstants.SUNDAY &&
				((!simulationStart.isBefore(today)) || !cal.isHoliday(simulationStart.toDateTimeAtStartOfDay().plusHours(2)))) {
				LocalDate trainingEnd = simulationStart.minusDays(1);
				LocalDate trainingStart = trainingEnd.minusDays(FinDataConstants.STRATEGY_PAIR_TRAINING_WINDOW_DAYS);
				System.out.println("Calculating stats for "+trainingStart+" - "+trainingEnd);
				pss.calculateStats(trainingStart, trainingEnd, -1, 999999999);
				System.out.println("Updating Adf P Ma for "+trainingStart+" - "+trainingEnd);
				pss.updateAdfpMovingAverage(trainingEnd);
			} else {
				System.out.println(simulationStart+" is a holiday.");
			}
			simulationStart = simulationStart.plusDays(1);
		}
	}

	public static void main3 (String args []) throws IOException {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		PairStrategyService pss = (PairStrategyService) context.getBean("pairStrategyService");

		HolidayCalendarFromYahoo cal = HolidayCalendarFromYahoo.forExchange(Exchange.SHSE);
		LocalDate simulationStart = LocalDate.parse("2016-06-06");
		LocalDate lastStart = LocalDate.parse("2016-06-06");
		LocalDate today = new LocalDate();
		while (!simulationStart.isAfter(lastStart)) {
			if (simulationStart.getDayOfWeek() != DateTimeConstants.SATURDAY &&
				simulationStart.getDayOfWeek() != DateTimeConstants.SUNDAY &&
				((!simulationStart.isBefore(today)) || !cal.isHoliday(simulationStart.toDateTimeAtStartOfDay().plusHours(2)))) {
				LocalDate trainingEnd = simulationStart.minusDays(1);
				LocalDate trainingStart = trainingEnd.minusDays(FinDataConstants.STRATEGY_PAIR_TRAINING_WINDOW_DAYS);
				System.out.println("Updating Adf P Ma for "+trainingStart+" - "+trainingEnd);
				pss.updateAdfpMovingAverage(trainingEnd);
			} else {
				System.out.println(simulationStart+" is a holiday.");
			}
			simulationStart = simulationStart.plusDays(1);
		}
	}

	public static void main4 (String [] args) throws IOException, SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		DividendService ds = (DividendService) context.getBean("dividendService");
		PairRepository pairRepo = (PairRepository) context.getBean("pairRepository");
		Set<String> codes = new HashSet<>();
		pairRepo.findByEnabled(true).forEach(pair->{
			codes.add(pair.getCodeToLong());
			codes.add(pair.getCodeToShort());
		});
		TDXClient c = new TDXClient("218.6.198.155:7709");
		c.connect();
		for (String code : codes) {
			ds.refreshDividendDataForFund(code, c);
		}
		c.disconnect();
	}

	public static void main5 (String [] args) throws IOException {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		StockService ss = (StockService) context.getBean("stockService");
		ss.updateSpreadForStocks(0.008d, 5000);
	}
}