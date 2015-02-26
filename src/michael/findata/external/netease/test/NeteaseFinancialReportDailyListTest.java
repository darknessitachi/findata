package michael.findata.external.netease.test;

import michael.findata.external.ReportPublicationList;
import michael.findata.external.netease.NeteaseFinancialReportDailyList;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Author: Michael, Tang Ying Jian
 * Date: 2015/2/26
 */
public class NeteaseFinancialReportDailyListTest {
	public static void main (String [] args) throws IOException {
		LocalDate date = java.time.LocalDate.of(2015, 2, 17);
		ReportPublicationList l = new NeteaseFinancialReportDailyList(date, date);
		System.out.println();
	}
}
