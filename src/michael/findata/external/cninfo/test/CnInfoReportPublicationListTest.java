package michael.findata.external.cninfo.test;

import michael.findata.external.ReportPublicationList;
import michael.findata.external.cninfo.CnInfoReportPublicationList;

import java.io.IOException;

/**
 * Created by Michael Tang on 2014/12/20.
 */
public class CnInfoReportPublicationListTest {
	public static void main (String [] args) throws IOException {
		ReportPublicationList list = new CnInfoReportPublicationList("600000");
		list.getReportPublications().forEach(System.out::println);
	}
}
