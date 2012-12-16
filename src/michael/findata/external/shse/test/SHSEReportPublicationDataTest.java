package michael.findata.external.shse.test;

import michael.findata.external.shse.SHSEReportPublicationData;

import java.io.IOException;
import java.text.ParseException;

public class SHSEReportPublicationDataTest {
	public static void main (String [] args ) throws IOException, ParseException {
//		new SHSEReportPublicationData("600519", 2002, 1);
		System.out.println(new SHSEReportPublicationData("600519", 2002, 2).getReportPublication().getDate());
		new SHSEReportPublicationData("600519", 2002, 3);
		new SHSEReportPublicationData("600519", 2002, 4);
		new SHSEReportPublicationData("600519", 2003, 1);
		new SHSEReportPublicationData("600519", 2003, 2);
		new SHSEReportPublicationData("600519", 2003, 3);
		new SHSEReportPublicationData("600519", 2003, 4);
	}
}
