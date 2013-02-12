package michael.findata.external.shse;

import michael.findata.external.ReportPublication;
import michael.findata.external.ReportPublicationList;
import michael.findata.util.FinDataConstants;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SHSEFinancialReportDailyList implements ReportPublicationList {
	private static Pattern p1 = Pattern.compile("http://static.sse.com.cn/disclosure/listedinfo/announcement/c/(\\d\\d\\d\\d-\\d\\d-\\d\\d)/([\\d]{6})_([\\d]{4})_([n,z,1,3]).pdf");
	HashSet<ReportPublication> pbs;
	public SHSEFinancialReportDailyList (Date date) throws IOException, ParseException {
		pbs = new HashSet<>();

		String code, fin_year, fin_season, dt;
		Matcher matcher;
		URL SHSEDailyListUrl;
		HttpURLConnection httpCon;
		SHSEDailyListUrl = new URL("http://query.sse.com.cn/infodisplay/queryLatestBulletin.do?jsonCallBack=&isPagination=false&productId=&reportType2=DQGG&reportType=ALL&beginDate=" +
				FinDataConstants.yyyyDashMMDashdd.format(date) +
				"&endDate=" +
				FinDataConstants.yyyyDashMMDashdd.format(date) +
				"&pageHelp.pageSize=2000&pageHelp.beginPage=1&pageHelp.endPage=1000&_=1359207649232");
		httpCon = (HttpURLConnection) SHSEDailyListUrl.openConnection();
		httpCon.setRequestProperty("Referer", "http://www.sse.com.cn/disclosure/listedinfo/announcement/search_result_index.shtml?x=1&productId=&startDate=2012-02-09&endDate=2012-02-09&reportType2=%E5%AE%9A%E6%9C%9F%E5%85%AC%E5%91%8A&reportType=ALL&moreConditions=true");
		httpCon.connect();
		JSONObject jsonObject = (JSONObject) JSONValue.parse(new InputStreamReader(httpCon.getInputStream(), "UTF-8"));
		JSONArray array = (JSONArray)jsonObject.get("result");
		for (Object o : array) {
			jsonObject = (JSONObject) o;
			matcher = p1.matcher((String) jsonObject.get("URL"));
			if (matcher.find()) {
				dt = matcher.group(1);
				code = matcher.group(2);
				fin_year = matcher.group(3);
				fin_season = matcher.group(4);
				pbs.add(new ReportPublication(FinDataConstants.yyyyDashMMDashdd.parse(dt), code, Integer.parseInt(fin_year), ("n".equals(fin_season) ? 4 : ("z".equals(fin_season) ? 2 : Integer.parseInt(fin_season)))));
				code = FinDataConstants.ABShareCodeRef.get(code);
				if (code != null) {
					pbs.add(new ReportPublication(FinDataConstants.yyyyDashMMDashdd.parse(dt), code, Integer.parseInt(fin_year), ("n".equals(fin_season) ? 4 : ("z".equals(fin_season) ? 2 : Integer.parseInt(fin_season)))));
				}
//				System.out.println(code + " " + fin_year + " " + fin_season + " " + dt);
			}
		}
		httpCon.disconnect();
	}

	@Override
	public Set<ReportPublication> getReportPublications() {
		return pbs;
	}
}