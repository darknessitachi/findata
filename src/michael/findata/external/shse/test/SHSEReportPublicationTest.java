package michael.findata.external.shse.test;

import michael.findata.external.shse.SHSEReportPublication;

import java.io.IOException;
import java.text.ParseException;

public class SHSEReportPublicationTest {
	public static void main (String [] args ) throws IOException, ParseException {
//		new SHSEReportPublication("600519", 2002, 1);
		System.out.println(new SHSEReportPublication("600519", 2002, 2).getDate());
		new SHSEReportPublication("600519", 2002, 3);
		new SHSEReportPublication("600519", 2002, 4);
		new SHSEReportPublication("600519", 2003, 1);
		new SHSEReportPublication("600519", 2003, 2);
		new SHSEReportPublication("600519", 2003, 3);
		new SHSEReportPublication("600519", 2003, 4);
	}
}
