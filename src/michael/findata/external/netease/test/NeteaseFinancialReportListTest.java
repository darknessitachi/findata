package michael.findata.external.netease.test;

import michael.findata.external.ReportPublication;
import michael.findata.external.netease.NeteaseFinancialReportList;
import michael.findata.util.FinDataConstants;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class NeteaseFinancialReportListTest {
	public static void main (String [] args) throws IOException, ParseException {
		SimpleDateFormat FORMAT_yyyyDashMMDashdd = new SimpleDateFormat(FinDataConstants.yyyyDashMMDashdd);
		for (ReportPublication p : new NeteaseFinancialReportList("300189").getReportPublications()) {
			System.out.println(p.getCode() + " " + p.getYear() + " " + p.getSeason() + ": "+ FORMAT_yyyyDashMMDashdd.format(p.getDate()));
		}
	}
}