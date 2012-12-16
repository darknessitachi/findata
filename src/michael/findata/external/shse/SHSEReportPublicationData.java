package michael.findata.external.shse;

import michael.findata.external.ReportPublication;
import michael.findata.external.ReportPublicationData;
import michael.findata.util.FinDataConstants;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;

public class SHSEReportPublicationData implements ReportPublicationData {

	private ReportPublication rp;

	public SHSEReportPublicationData (String code, int year, int season) throws IOException, ParseException {
		URL SHSEReportPublicationUrl = new URL("http://www.sse.com.cn/sseportal/webapp/datapresent/SSEPeriodicPDF?COMPANY_CODE="+code+"&REPORTYEAR="+year+"&REPORTTYPE="+(season == 4? "n" : (season == 2? "z" : season)));
		HttpURLConnection httpCon = (HttpURLConnection) SHSEReportPublicationUrl.openConnection();
		httpCon.connect();
		httpCon.getInputStream().close();
		httpCon.disconnect();
		String redirectedURL = httpCon.getURL().toString();
		rp = new ReportPublication(FinDataConstants.yyyyMMdd.parse(redirectedURL.substring(55, 59) + redirectedURL.substring(60, 62) + redirectedURL.substring(63, 65)), code, year, season);
	}

	@Override
	public Collection<ReportPublication> getReportPublications() {
		ArrayList<ReportPublication> temp = new ArrayList<>();
		temp.add(rp);
		return temp;
	}

	public ReportPublication getReportPublication () {
		return rp;
	}
}