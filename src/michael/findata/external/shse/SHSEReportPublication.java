package michael.findata.external.shse;

import michael.findata.external.ReportPublication;
import michael.findata.external.ReportPublicationList;
import michael.findata.util.FinDataConstants;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class SHSEReportPublication extends ReportPublication {

	public SHSEReportPublication(String code, int year, int season) throws IOException, ParseException {
		URL SHSEReportPublicationUrl = new URL("http://query.sse.com.cn/infodisplay/queryPeriodicPDFURL.do?companyCode="+code+"&reportYear="+year+"&reportType="+(season == 4? "n" : (season == 2? "z" : season)));
		HttpURLConnection httpCon = (HttpURLConnection) SHSEReportPublicationUrl.openConnection();
		httpCon.setRequestProperty("Referer", "http://listxbrl.sse.com.cn/ssexbrl/presentAction.do");
		httpCon.connect();
		httpCon.getInputStream().close();
		httpCon.disconnect();
		String redirectedURL = httpCon.getURL().toString();
//		ReportPublication rp = new ReportPublication(FinDataConstants.yyyyMMdd.parse(redirectedURL.substring(55, 59) + redirectedURL.substring(60, 62) + redirectedURL.substring(63, 65)), code, year, season);
		try {
			super.setDate(FinDataConstants.yyyyMMdd.parse(redirectedURL.substring(62, 66) + redirectedURL.substring(67, 69) + redirectedURL.substring(70, 72)));
		} catch (ParseException e) {
			System.out.println("Can't parese: "+redirectedURL);
			throw e;
		}
		super.setCode(code);
		super.setYear(year);
		super.setSeason(season);
	}

//	public SHSEReportPublication(String code, int year, int season, Date pubDate) {
//	}

//	@Override
//	public Collection<ReportPublication> getReportPublications() {
//		ArrayList<ReportPublication> temp = new ArrayList<>();
//		temp.add(rp);
//		return temp;
//	}

//	public ReportPublication getReportPublication () {
//		return rp;
//	}
}