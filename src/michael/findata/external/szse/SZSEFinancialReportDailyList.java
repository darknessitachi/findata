package michael.findata.external.szse;

import michael.findata.external.ReportPublication;
import michael.findata.external.ReportPublicationList;
import michael.findata.util.FinDataConstants;
import michael.findata.util.StringParserUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static michael.findata.util.FinDataConstants.*;

public class SZSEFinancialReportDailyList implements ReportPublicationList {
	public static String pt = "finalpage/(\\d\\d\\d\\d-\\d\\d-\\d\\d)/\\d+.PDF' target=new>(.{2,8})��(\\d\\d\\d\\d)��?(��һ��|����|������|��)�ȱ���";

	Pattern p1 = Pattern.compile("��\\(([0|2|3][\\d]{5})\\).+��([\\d]{4})([^��Ԥ]+��)[^��Ԥ]*��Ҫ.+");
	Pattern p2 = Pattern.compile("��\\(([0|1|2|3][\\d]{5})��.*([0|1|2|3][\\d]{5}).*\\).+��([\\d]{4})(.+��).*��Ҫ.+");
	Pattern p3 = Pattern.compile("��\\(([0|1|2|3][\\d]{5})\\)(.+)��([\\d]{4})(.+��)����.+\\2�ַ���\\3\\4");
	ArrayList<ReportPublication> pbs;
	public SZSEFinancialReportDailyList(Date date) throws IOException, ParseException {
		pbs = new ArrayList<>();
		SimpleDateFormat FORMAT_yyMMdd = new SimpleDateFormat(yyMMdd);
		URL SHSEDailyListUrl = new URL("http://www.szse.cn/szseWeb/common/szse/files/text/gs/gs" + FORMAT_yyMMdd.format(date) + ".txt");
		HttpURLConnection httpCon = (HttpURLConnection) SHSEDailyListUrl.openConnection();
		httpCon.connect();
		InputStream is;
		try {
			is = httpCon.getInputStream();
		} catch (FileNotFoundException ex) {
			return;
		}
		String sBuffer3, sBufferPrev = "", sLine;
		String code1, code2, fin_year, fin_season;
		int season;
		BufferedReader l_reader = new BufferedReader(new InputStreamReader(is, "GB2312"));
		Matcher matcher;

		while ((sBuffer3 = l_reader.readLine()) != null) {
			code1 = "";
			code2 = "";
//			System.out.println(sBuffer3);
			sLine = sBufferPrev + sBuffer3;
//			System.out.println(sLine);
			sBufferPrev = sBuffer3;
			matcher = p1.matcher(sLine);
			if (matcher.find()) {
				code1 = matcher.group(1);
				fin_year = matcher.group(2);
				fin_season = matcher.group(3);
			} else {
				matcher = p2.matcher(sLine);
				if (matcher.find()) {
					code1 = matcher.group(1);
					code2 = matcher.group(2);
					fin_year = matcher.group(3);
					fin_season = matcher.group(4);
				} else {
					matcher = p3.matcher(sLine);
					if (matcher.find()) {
						code1 = matcher.group(1);
						fin_year = matcher.group(3);
						fin_season = matcher.group(4);
					} else {
						continue;
					}
				}
			}
//			if (fin_season.contains(s1Report)) {
//				season = 1;
//			} else if (fin_season.contains(s3Report)) {
//				season = 3;
//			} else if (fin_season.contains(s2Report) || fin_season.contains(s2Report2)) {
//				season = 2;
//			} else if (fin_season.contains(s4Report)) {
//				season = 4;
//			} else {
//				System.out.println("Can't figure out season: " + code1 + " " + code2 + date + " " + fin_season);
//				continue;
//			}
			try {
				season = StringParserUtil.inferSeason(fin_season);
			} catch (ParseException e) {
				System.out.println("Can't figure out season: " + code1 + " " + code2 + date + " " + fin_season);
				continue;
			}
			pbs.add(new ReportPublication(date, code1, null, Integer.parseInt(fin_year), season));
			code1 = FinDataConstants.ABShareCodeRef.get(code1);
			if (code1 != null) {
				pbs.add(new ReportPublication(date, code1, null, Integer.parseInt(fin_year), season));
			}
//			System.out.println(code1 + " " + fin_year + " " + fin_season + " " + FinDataConstants.yyyyDashMMDashdd.format(date));
			if (!code2.equals("")) {
				pbs.add(new ReportPublication(date, code2, null, Integer.parseInt(fin_year), season));
//				System.out.println(code2 + " " + fin_year + " " + fin_season + " " + FinDataConstants.yyyyDashMMDashdd.format(date));
				code2 = FinDataConstants.ABShareCodeRef.get(code2);
				if (code2 != null) {
					pbs.add(new ReportPublication(date, code2, null, Integer.parseInt(fin_year), season));
				}
			}
		}
		l_reader.close();
		is.close();
		httpCon.disconnect();
	}

	@Override
	public Collection<ReportPublication> getReportPublications() {
		return pbs;
	}
}