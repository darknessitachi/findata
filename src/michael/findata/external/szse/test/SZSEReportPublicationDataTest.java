package michael.findata.external.szse.test;

import michael.findata.external.szse.SZSEReportPublicationData;

import java.io.IOException;
import java.text.ParseException;

public class SZSEReportPublicationDataTest {
	public static void main (String [] args) throws IOException, ParseException {
		new SZSEReportPublicationData("000767", 2007, 1);
		new SZSEReportPublicationData("000767", 2007, 2);
		new SZSEReportPublicationData("000767", 2007, 3);
//		new SZSEReportPublicationData("000767", 2007, 4);
	}
}
