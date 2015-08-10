package michael.findata.external.shse;

import michael.findata.external.ReportPublication;
import michael.findata.external.ReportPublicationList;
import michael.findata.util.FinDataConstants;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

/**
 * Created by MyPC on 2015/3/29.
 */
public class SHSEReportPublicationList implements ReportPublicationList {

	HashSet<ReportPublication> pbs;

	public SHSEReportPublicationList(Date start, Date end) throws IOException {
		this(start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
		end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
	}
	public SHSEReportPublicationList(LocalDate start, LocalDate end) throws IOException {
		CloseableHttpClient httpClient = FinDataConstants.httpClient;
		long pageCount = 1000000;
		long endPage = 0;
		String code, dt, fin_year;
		String type;
		int fin_season;
		JSONObject rec;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FinDataConstants.yyyyDashMMDashdd);
		SimpleDateFormat sdf = new SimpleDateFormat(FinDataConstants.yyyyDashMMDashdd);
		pbs = new HashSet<>();
		for (int begigPage = 1; endPage < pageCount ;begigPage += 5) {
			HttpGet get = new HttpGet("http://query.sse.com.cn/infodisplay/queryLatestBulletinNew.do?jsonCallBack=jsonpCallback78585&isPagination=false&productId=&reportType2=DQGG&reportType=ALL&beginDate="+formatter.format(start)+"&endDate="+formatter.format(end)+"&pageHelp.pageNo=3&pageHelp.beginPage="+begigPage+"&pageHelp.endPage="+(begigPage+4)+"&_=1427632814243");
			get.setHeader("Accept", "*/*");
			get.setHeader("Accept-Encoding", "gzip, deflate");
			get.setHeader("Accept-Language", "en-US,en;q=0.5");
			get.setHeader("Connection", "keep-alive");
			get.setHeader("Cookie", "_gscu_1808689395=2763174481i7r075; _gscs_1808689395=27631744rpdxwt75|pv:11; _gscbrs_1808689395=1");
			get.setHeader("Host", "query.sse.com.cn");
			get.setHeader("Referer", "http://www.sse.com.cn/disclosure/listedinfo/announcement/search_result_index_n.shtml?");
			CloseableHttpResponse response = httpClient.execute(get);
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "GBK"));
				String line;
				String result = "";
				while ((line = br.readLine()) != null) {
					result += line;
				}
				result = result.substring(result.indexOf('(') + 1, result.length() - 1);
				JSONObject resultObj = (JSONObject) JSONValue.parse(result);
				JSONObject pageHelp = (JSONObject) resultObj.get("pageHelp");
				endPage = (Long) pageHelp.get("endPage");
				pageCount = (Long) pageHelp.get("pageCount");
				for (Object o : (JSONArray) pageHelp.get("data")) {
					rec = (JSONObject) o;
					dt = (String) rec.get("SSEDate");
					fin_year = (String) rec.get("bulletin_Year");
					code = (String) rec.get("security_Code");
					type = (String) rec.get("bulletin_Type");
					if ("第一季度季报".equals(type)) {
						fin_season = 1;
					} else if ("半年报".equals(type) || "半年报摘要".equals(type)) {
						fin_season = 2;
					} else if ("第三季度季报".equals(type)) {
						fin_season = 3;
					} else if ("年报".equals(type) || "年报摘要".equals(type)) {
						fin_season = 4;
					} else {
						System.out.println("Unable to deduce fin_season from '" + rec.get("bulletin_Type") + "'");
						System.out.println("\tCode: " + code);
						System.out.println("\tDate: " + dt);
						System.out.println("\tYear: " + fin_year);
						continue;
					}
					try {
						pbs.add(new ReportPublication(sdf.parse(dt), code, null, Integer.parseInt(fin_year), fin_season));
						code = FinDataConstants.ABShareCodeRef.get(code);
						if (code != null) {
							pbs.add(new ReportPublication(sdf.parse(dt), code, null, Integer.parseInt(fin_year), fin_season));
						}
					} catch (ParseException e) {
						System.out.println("Cannot parse date: '"+dt+"'");
						System.out.println("\tCode: " + code);
						System.out.println("\tYear: " + fin_year);
						System.out.println("\tSeason: " + fin_season);
					}
				}
			} finally {
				response.close();
			}
		}
//		System.out.println(count);
	}

	@Override
	public Collection<ReportPublication> getReportPublications() {
		return pbs;
	}
}