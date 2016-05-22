package michael.findata.service.test;

import michael.findata.external.hexun2008.Hexun2008Constants;
import michael.findata.model.PairInstance;
import michael.findata.service.PairStrategyService;
import michael.findata.util.FinDataConstants;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;

import static michael.findata.util.FinDataConstants.yyyyMMDDHHmmss;
import static michael.findata.util.FinDataConstants.yyyyMMdd;

public class NeteaseInstantSnapshotServiceTest2 {
	public static void main (String [] args) throws IOException {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		PairStrategyService pss = (PairStrategyService) context.getBean("pairStrategyService");

		DateTimeFormatter formatter = DateTimeFormat.forPattern(FinDataConstants.yyyyMMdd);

		LocalDate openStart = formatter.parseLocalDate("20160415");
		LocalDate openEnd = formatter.parseLocalDate("20160415");
		LocalDate exeStart = formatter.parseLocalDate("20160416");
		LocalDate exeEnd = formatter.parseLocalDate("20160420");

//		todo ... too many ... limit it
		pss.findSimpleOpenCloseOpportunities(openStart, openEnd, exeStart, exeEnd, new String[] {"510310"}, new String[] {"510300"}).forEach(showResults);
	}

	private static class Counts {
		int open = 0;
		int close = 0;
		int sameDayClose = 0;
	}
	private enum CountType {
		OPEN,
		CLOSE,
		SAME_DAY_CLOSE
	}
	public static SortedMap<String, Counts> counts = new TreeMap<>();
	public static Consumer<PairInstance> showResults = pair1 -> {
		int age;
		SimpleDateFormat sdfDisplay = new SimpleDateFormat(yyyyMMDDHHmmss);
		SimpleDateFormat sdf = new SimpleDateFormat(yyyyMMdd);
		DecimalFormat df = new DecimalFormat(Hexun2008Constants.ACCURATE_DECIMAL_FORMAT);
		System.out.print(pair1.toShort().symbol().substring(0, 6) + "->" + pair1.toLong().symbol().substring(0, 6) + "\tslope: " + df.format(pair1.slope()) + " stdev: " + df.format(pair1.stdev()) + " correl: " + df.format(pair1.correlco()) + " adf_p: " + df.format(pair1.adf_p()));
		switch (pair1.getStatus()) {
			case OPENED:
				System.out.print("\tOpen: Short->Long\t\t\t\t");
				System.out.println(sdfDisplay.format(pair1.getDateOpened()) + "\t" + pair1.getShortOpen() + "\t" + pair1.getLongOpen() + "\t" + pair1.getMinResidual() / pair1.stdev() + "\t" + pair1.getMaxResidual() / pair1.stdev() + "\t\t\t\t\t\t" + pair1.getMaxAmountPossibleOpen());
				updateCount(counts, sdf.format(pair1.getDateOpened()), CountType.OPEN);
				break;
			case CLOSED:
				age = pair1.closureAge();
				System.out.print((age == 0 ? "\tSame-day closure\t" : "\tClosure\t") + pair1.getThresholdClose() / pair1.stdev() + "\tfee\t" + pair1.feeEstimate() + "\t");
				System.out.print(sdfDisplay.format(pair1.getDateClosed()) + "\t" + pair1.getShortClose() + "\t" + pair1.getLongClose() + "\t" + pair1.getMinResidual() / pair1.stdev() + "\t" + pair1.getMaxResidual() / pair1.stdev() + "\t" + sdfDisplay.format(pair1.getMinResidualDate()) + "\t" + sdfDisplay.format(pair1.getMaxResidualDate()) + "\t" + (age == 0 ? 1 : age));
				System.out.println("\tProfit:\t" + pair1.profitPercentageEstimate() + "\t" + pair1.getMaxAmountPossibleClose());
				updateCount(counts, sdf.format(pair1.getDateClosed()), (age == 0 ? CountType.SAME_DAY_CLOSE : CountType.CLOSE));
				break;
			case FORCED:
				age = pair1.closureAge();
				System.out.print("\tForce closure\t" + pair1.getThresholdClose() / pair1.stdev() + "\tfee\t" + pair1.feeEstimate() + "\t");
				System.out.print(sdfDisplay.format(pair1.getDateClosed()) + "\t" + pair1.getShortClose() + "\t" + pair1.getLongClose() + "\t" + pair1.getMinResidual() / pair1.stdev() + "\t" + pair1.getMaxResidual() / pair1.stdev() + "\t" + sdfDisplay.format(pair1.getMinResidualDate()) + "\t" + sdfDisplay.format(pair1.getMaxResidualDate()) + "\t" + (age == 0 ? 1 : age));
				System.out.println("\tProfit/Loss:\t" + pair1.profitPercentageEstimate() + "\t" + pair1.getMaxAmountPossibleClose());
				updateCount(counts, sdf.format(pair1.getDateClosed()), (age == 0 ? CountType.SAME_DAY_CLOSE : CountType.CLOSE));
				break;
		}
	};
	private static void updateCount (Map<String, Counts> countMap, String date, CountType type) {
		Counts c;
		if (!countMap.containsKey(date)) {
			c = new Counts();
		} else {
			c = countMap.get(date);
		}
		switch (type) {
			case OPEN:
				c.open ++;
				break;
			case CLOSE:
				c.close ++;
				break;
			case SAME_DAY_CLOSE:
				c.sameDayClose ++;
				break;
		}
		countMap.put(date, c);
	}
}