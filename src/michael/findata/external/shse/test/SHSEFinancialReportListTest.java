package michael.findata.external.shse.test;

import michael.findata.external.shse.SHSEReportPublicationList;

import java.io.IOException;
import java.time.LocalDate;

public class SHSEFinancialReportListTest {
	public static void main (String args []) throws IOException {
		SHSEReportPublicationList list = new SHSEReportPublicationList(LocalDate.parse("2014-08-22"), LocalDate.parse("2014-10-22"));
		list.getReportPublications().stream().forEach(System.out::println);
		System.out.println(list.getReportPublications().size());
	}
}