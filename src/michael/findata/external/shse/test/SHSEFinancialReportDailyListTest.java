package michael.findata.external.shse.test;

import michael.findata.external.ReportPublication;
import michael.findata.external.shse.SHSEFinancialReportDailyList;
import michael.findata.util.FinDataConstants;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class SHSEFinancialReportDailyListTest {
	public static void main (String [] args ) throws IOException, ParseException {
		GregorianCalendar gc = new GregorianCalendar();
		gc.set(Calendar.YEAR, 2012);
		gc.set(Calendar.MONTH, 7);
		gc.set(Calendar.DAY_OF_MONTH, 28);
		for (ReportPublication p : new SHSEFinancialReportDailyList(gc.getTime()).getReportPublications()) {
			System.out.println(p.getCode());
			System.out.println(p.getYear());
			System.out.println(p.getSeason());
			System.out.println(FinDataConstants.yyyyDashMMDashdd.format(p.getDate()));
		}
	}
}