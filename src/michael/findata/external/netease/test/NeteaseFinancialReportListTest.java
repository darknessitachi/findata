package michael.findata.external.netease.test;

import michael.findata.external.ReportPublication;
import michael.findata.external.netease.NeteaseFinancialReportList;
import michael.findata.util.FinDataConstants;

import java.io.IOException;
import java.text.ParseException;

public class NeteaseFinancialReportListTest {
	public static void main (String [] args) throws IOException, ParseException {
		for (ReportPublication p : new NeteaseFinancialReportList("600704").getReportPublications()) {
			System.out.println(p.getCode() + " " + p.getYear() + " " + p.getSeason() + ": "+ FinDataConstants.yyyyDashMMDashdd.format(p.getDate()));
		}
	}
}