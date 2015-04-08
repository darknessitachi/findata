package michael.findata.external.netease;

import michael.findata.external.ReportPublication;
import michael.findata.external.ReportPublicationList;
import michael.findata.util.FinDataConstants;
import michael.findata.util.StringParserUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: Michael, Tang Ying Jian
 * Date: 2015/2/26
 */
public class NeteaseFinancialReportDailyList implements ReportPublicationList {

	List<ReportPublication> pbs;
	Pattern yearPattern = Pattern.compile(".*(20[\\d]{2}).*");
	public NeteaseFinancialReportDailyList (Date startDate, Date endDate) throws IOException {
		init(startDate.toInstant().atOffset(ZoneOffset.ofHours(0)).toLocalDate(), endDate.toInstant().atOffset(ZoneOffset.ofHours(0)).toLocalDate());
	}

	private void init (LocalDate startDate, LocalDate endDate) throws MalformedURLException {
		String startDateString = FinDataConstants.NEW_FORMATTER_yyyyDashMMDashdd.format(startDate);
		String endDateString = FinDataConstants.NEW_FORMATTER_yyyyDashMMDashdd.format(endDate);
		ArrayList<URL> urls = new ArrayList<>();
		urls.add(new URL("http://quotes.money.163.com/hs/marketdata/service/gsgg" +
				".php?host=/hs/marketdata/service/gsgg.php&page=0&query=leixing:03;start:"+startDateString+";" +
				"end:"+endDateString+"&fields=RN,SYMBOL,SNAME,PUBLISHDATE,ANNOUNMT2," +
				"ANNOUNMT1&sort=PUBLISHDATE&order=desc&count=3000&type=query&req=41430"));
		urls.add(new URL("http://quotes.money.163.com/hs/marketdata/service/gsgg" +
				".php?host=/hs/marketdata/service/gsgg.php&page=0&query=leixing:04;start:"+startDateString+";" +
				"end:"+endDateString+"&fields=RN,SYMBOL,SNAME,PUBLISHDATE,ANNOUNMT2," +
				"ANNOUNMT1&sort=PUBLISHDATE&order=desc&count=3000&type=query&req=41430"));
		urls.add(new URL("http://quotes.money.163.com/hs/marketdata/service/gsgg" +
				".php?host=/hs/marketdata/service/gsgg.php&page=0&query=leixing:06;start:"+startDateString+";" +
				"end:"+endDateString+"&fields=RN,SYMBOL,SNAME,PUBLISHDATE,ANNOUNMT2," +
				"ANNOUNMT1&sort=PUBLISHDATE&order=desc&count=3000&type=query&req=41430"));
		urls.add(new URL("http://quotes.money.163.com/hs/marketdata/service/gsgg" +
				".php?host=/hs/marketdata/service/gsgg.php&page=0&query=leixing:08;start:"+startDateString+";" +
				"end:"+endDateString+"&fields=RN,SYMBOL,SNAME,PUBLISHDATE,ANNOUNMT2," +
				"ANNOUNMT1&sort=PUBLISHDATE&order=desc&count=3000&type=query&req=41430"));
		pbs = Collections.synchronizedList(new ArrayList<>());
		urls.forEach((urlSeason41) -> {
			try {
				grabPublications(urlSeason41);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	public NeteaseFinancialReportDailyList (LocalDate startDate, LocalDate endDate) throws IOException {
		init (startDate, endDate);
	}

	private void grabPublications(URL urlSeason4) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(urlSeason4.openStream()));
		String s = br.readLine();
		((JSONArray)((JSONObject) JSONValue.parse(s)).get("list")).forEach(pub -> {
			try {
				pbs.add(constructReportPublication((JSONObject) pub));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		});
	}

	private ReportPublication constructReportPublication (JSONObject publication) throws ParseException {
		SimpleDateFormat FORMAT_yyyyDashMMDashdd = new SimpleDateFormat(FinDataConstants.yyyyDashMMDashdd);
		String code = (String) (publication).get("SYMBOL");
		int season, year;
		Date publishDate;
		String name = (String) publication.get("SNAME");
		try {
			publishDate = FORMAT_yyyyDashMMDashdd.parse((String)publication.get("PUBLISHDATE"));
		} catch (ParseException e) {
			System.out.println("Can't figure out date: " + code + " " + publication.get("PUBLISHDATE") + " " +
					publication.get ("ANNOUNMT1"));
			throw e;
		} catch (NumberFormatException e) {
			throw e;
		}

		try {
			season = StringParserUtil.inferSeason((String) publication.get("ANNOUNMT1"));
		} catch (ParseException e) {
			System.out.println("Can't figure out season: " + code + " " + publishDate + " " +publication.get("ANNOUNMT1"));
			throw e;
		}

		System.out.print(code);
		System.out.print("\t" + publishDate);
		System.out.print("\t" + season);
		Matcher m = yearPattern.matcher(publication.get("ANNOUNMT2").toString());
		if (m.matches()) {
			year = Integer.parseInt(m.group(1));
		} else {
			if (season == 4) {
				year = publishDate.getYear() + 1900 - 1;
			} else {
				year = publishDate.getYear() + 1900;
			}
		}
		System.out.println("\t" + year);
		ReportPublication r = new ReportPublication(publishDate, code, name, year, season);
		if (!r.isDateMeaningful()) {
			System.out.println("Meaningless data.");
			throw new ParseException(publication.toJSONString(), 0);
		}
		return r;
	}

	@Override
	public Collection<ReportPublication> getReportPublications() {
		return pbs;
	}
}