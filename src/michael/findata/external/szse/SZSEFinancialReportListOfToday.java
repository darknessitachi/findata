package michael.findata.external.szse;


import michael.findata.external.ReportPublication;
import michael.findata.external.ReportPublicationList;
import michael.findata.util.FinDataConstants;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static michael.findata.util.FinDataConstants.*;
import static michael.findata.util.FinDataConstants.s4Report;

public class SZSEFinancialReportListOfToday implements ReportPublicationList {
	Pattern p1 = Pattern.compile(SZSEFinancialReportDailyList.pt);
	HashSet<ReportPublication> pbs;
	public SZSEFinancialReportListOfToday () throws IOException, ParseException {
		SimpleDateFormat FORMAT_yyyyDashMMDashdd = new SimpleDateFormat(FinDataConstants.yyyyDashMMDashdd);
		pbs = new HashSet<>();
		URL SHSEDailyListUrl = new URL("http://disclosure.szse.cn/m/drgg.htm");
		HttpURLConnection httpCon = (HttpURLConnection) SHSEDailyListUrl.openConnection();
		httpCon.connect();
		InputStream is;
		String name, fin_year, fin_season;
		Date date;
		int season;
		try {
			is = httpCon.getInputStream();
		} catch (FileNotFoundException ex) {
			return;
		}
		String html;
		Matcher m;
		BufferedReader br = new BufferedReader(new InputStreamReader(is, "GB2312"));
		while ((html = br.readLine()) != null) {
			m = p1.matcher(html);
			while (m.find()) {
				date = FORMAT_yyyyDashMMDashdd.parse(m.group(1));
				name = m.group(2);
				fin_year = m.group(3);
				fin_season = m.group(4);
//				System.out.println(m.group(1));
//				System.out.println(m.group(2));
//				System.out.println(m.group(3));
//				System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
				if (fin_season.contains(s1Report)) {
					season = 1;
				} else if (fin_season.contains(s3Report)) {
					season = 3;
				} else if (fin_season.contains(s2Report) || fin_season.contains(s2Report2)) {
					season = 2;
				} else if (fin_season.contains(s4Report) || fin_season.contains(s4Report2)) {
					season = 4;
				} else {
					System.out.println("Can't figure out season: " + name + " " + fin_season);
					continue;
				}
//				System.out.println(name + " " + fin_year + " " + fin_season + " " + FinDataConstants.yyyyDashMMDashdd.format(date));
				pbs.add(new ReportPublication(date, null, name, Integer.parseInt(fin_year), season));
			}
		}
		br.close();
		is.close();
		httpCon.disconnect();
	}

	@Override
	public Collection<ReportPublication> getReportPublications() {
		return pbs;  //To change body of implemented methods use File | Settings | File Templates.
	}
}