package michael.findata.service;

import michael.findata.external.SecurityTimeSeriesData;
import michael.findata.external.SecurityTimeSeriesDatum;
import michael.findata.external.tdx.TDXMinuteLine;
import michael.findata.external.tdx.TDXPriceHistory;
import michael.findata.model.AdjFactor;
import michael.findata.util.CalendarUtil;
import michael.findata.util.Consumer5;
import org.joda.time.DateTime;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.util.Stack;

/**
 * Created by nicky on 2015/11/29.
 */
public class SecurityTimeSeriesDataService extends JdbcDaoSupport {

	public void walkMinutes(DateTime start,
							DateTime end,
							int maxTicks,
							String codeA,
							String codeB,
							boolean log,
							Consumer5<DateTime, Double, Double, Float, Float> doStuff) {
		SecurityTimeSeriesData seriesA = new TDXMinuteLine(codeA);
		SecurityTimeSeriesData seriesB = new TDXMinuteLine(codeB);
		walk(start, end, maxTicks, seriesA, seriesB, codeA, codeB, log, doStuff);
	}

	public void walkDays(DateTime start,
							DateTime end,
							int maxTicks,
							String codeA,
							String codeB,
							boolean log,
							Consumer5<DateTime, Double, Double, Float, Float> doStuff) {
		SecurityTimeSeriesData seriesA = new TDXPriceHistory(codeA);
		SecurityTimeSeriesData seriesB = new TDXPriceHistory(codeB);
		walk(start, end, maxTicks, seriesA, seriesB, codeA, codeB, log, doStuff);
	}

	// seriesA and seriesB must be from the same concrete class
	private void walk(DateTime start,
					 DateTime end,
					 int maxTicks,
					 SecurityTimeSeriesData seriesA,
					 SecurityTimeSeriesData seriesB,
					 String codeA,
					 String codeB,
					 boolean log,
					 Consumer5<DateTime, Double, Double, Float, Float> doStuff) {
		Stack<AdjFactor> adjFctA = new Stack<>();
		Stack<AdjFactor> adjFctB = new Stack<>();
		try {
			DividendService.getAdjFactors(start, end, codeA, codeB, adjFctA, adjFctB, getJdbcTemplate());
		} catch (Exception e) {
			// no adj factor found
		}

		SecurityTimeSeriesDatum quoteA;
		SecurityTimeSeriesDatum quoteB;
		Stack<SecurityTimeSeriesDatum> quotesA = new Stack<>();
		Stack<SecurityTimeSeriesDatum> quotesB = new Stack<>();
		boolean seriesEnded = false;
		while (seriesA.hasNext() && seriesB.hasNext()) {
			quoteA = seriesA.next();
			quoteB = seriesB.next();
			while (quoteA.getDateTime().getMillis() > quoteB.getDateTime().getMillis()) {
				if (seriesA.hasNext()) {
					quoteA = seriesA.next();
				} else {
					seriesEnded = true;
					break;
				}
			}
			if (seriesEnded) {
				break;
			}
			while (quoteB.getDateTime().getMillis() > quoteA.getDateTime().getMillis()) {
				if (seriesB.hasNext()) {
					quoteB = seriesB.next();
				} else {
					seriesEnded = true;
					break;
				}
			}
			if (seriesEnded) {
				break;
			}
//			while (quoteA.getMinute() > quoteB.getMinute()) {
//				if (seriesA.hasNext()) {
//					quoteA = seriesA.next();
//				} else {
//					seriesEnded = true;
//					break;
//				}
//			}
//			if (seriesEnded) {
//				break;
//			}
//			while (quoteB.getMinute() > quoteA.getMinute()) {
//				if (seriesB.hasNext()) {
//					quoteB = seriesB.next();
//				} else {
//					seriesEnded = true;
//					break;
//				}
//			}
//			if (seriesEnded) {
//				break;
//			}

			if (CalendarUtil.daysBetween(quoteA.getDateTime(), end) >= 0) {
				if (CalendarUtil.daysBetween(start, quoteA.getDateTime()) < 0) {
					break;
				}
				quotesA.push(quoteA);
				quotesB.push(quoteB);
				if (quotesA.size() == maxTicks) {
					break;
				}
			}
		}
		while ((!quotesA.isEmpty()) && !quotesB.isEmpty()) {
			quoteA = quotesA.pop();
			quoteB = quotesB.pop();
			while ((!adjFctA.isEmpty()) && CalendarUtil.daysBetween(adjFctA.peek().paymentDate, quoteA.getDateTime()) >= 0) {
				adjFctA.pop();
			}
			while ((!adjFctB.isEmpty()) && CalendarUtil.daysBetween(adjFctB.peek().paymentDate, quoteB.getDateTime()) >= 0) {
				adjFctB.pop();
			}
			double prA = quoteA.getClose() / 1000d / (adjFctA.empty() ? 1.0d : adjFctA.peek().factor);
			double prB = quoteB.getClose() / 1000d / (adjFctB.empty() ? 1.0d : adjFctB.peek().factor);
			doStuff.apply(quoteA.getDateTime(), prA, prB, quoteA.getAmount(), quoteB.getAmount());
			if (log) {
				System.out.println(quoteA.getDateTime() + "\t" + "\t" + prA + "\t" + quoteB.getDateTime() + "\t" + prB + "\t");
			}
		}
		seriesA.close();
		seriesB.close();
	}
}