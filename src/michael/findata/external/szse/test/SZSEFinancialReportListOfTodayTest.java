package michael.findata.external.szse.test;


import michael.findata.external.ReportPublication;
import michael.findata.external.szse.SZSEFinancialReportListOfToday;
import michael.findata.util.FinDataConstants;

import java.io.IOException;
import java.text.ParseException;

public class SZSEFinancialReportListOfTodayTest {
	public static void main (String [] args) throws IOException, ParseException {
		SZSEFinancialReportListOfToday t = new SZSEFinancialReportListOfToday();
		for(ReportPublication rp : t.getReportPublications()){
			System.out.println(rp.getName() + " " + rp.getYear() + " " + rp.getSeason() + " " + FinDataConstants.yyyyDashMMDashdd.format(rp.getDate()));
		}
	}
}
