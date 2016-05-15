package michael.findata.external.cninfo.test;

import michael.findata.external.ReportPublicationList;
import michael.findata.external.cninfo.CnInfoReportPublicationList;

import java.io.IOException;

public class CnInfoReportPublicationListTest {
	public static void main (String [] args) throws IOException {
		ReportPublicationList list = new CnInfoReportPublicationList("200530");
		list.getReportPublications().forEach(System.out::println);
	}
}
