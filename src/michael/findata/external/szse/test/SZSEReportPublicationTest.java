package michael.findata.external.szse.test;

import michael.findata.external.szse.SZSEReportPublication;

import java.io.IOException;
import java.text.ParseException;

public class SZSEReportPublicationTest {
	public static void main (String [] args) throws IOException, ParseException {
		new SZSEReportPublication("000767", 2007, 1);
		new SZSEReportPublication("000767", 2007, 2);
		new SZSEReportPublication("000767", 2007, 3);
		new SZSEReportPublication("000767", 2007, 4);
		new SZSEReportPublication("200011", 2013, 1);
		System.out.println();
	}
}