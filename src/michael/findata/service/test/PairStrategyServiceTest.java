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

	public static void main1 (String args []) throws IOException{
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		PairStrategyService pss = (PairStrategyService) context.getBean("pairStrategyService");
		StockService ss = (StockService) context.getBean("stockService");
//		pss.updatePairs(ss.getStockGroup("michael/findata/algoquant/strategy/pair/group_hk.csv").toArray(new String[0]));
//		pss.updatePairs(ss.getStockGroup("michael/findata/algoquant/strategy/pair/group_gold.csv").toArray(new String[0]));
//		pss.updatePairs(ss.getStockGroup("michael/findata/algoquant/strategy/pair/group_domestic.csv").toArray(new String[0]));
		pss.updatePairs(ss.getStockGroup("michael/findata/algoquant/strategy/pair/hopping_strategy_scope.csv").toArray(new String[0]));
	}

	public static void main2 (String [] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		PairStrategyService pss = (PairStrategyService) context.getBean("pairStrategyService");
		pss.updateDividends();
	}

	public static void main (String args []) throws IOException {
		String date = "2017-01-03";
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/app_context_no_tdx_client.xml");
		PairStrategyService pss = (PairStrategyService) context.getBean("pairStrategyService");
		pss.massCreatePairStatsForExecutionStartRange(date, date, FinDataConstants.STRATEGY_PAIR_TRAINING_WINDOW_DAYS);
		pss.massCreateShortInHKPairStrategyInstancesBasedOnCalculatedStats(date, date);
	}

	// updating moving average only
	public static void main4 (String args []) throws IOException {
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

	public static void main5 (String [] args) throws IOException {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		StockService ss = (StockService) context.getBean("stockService");
		ss.updateSpreadForStocks(0.008d, 5000);
	}
}