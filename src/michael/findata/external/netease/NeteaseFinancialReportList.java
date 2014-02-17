package michael.findata.external.netease;

import michael.findata.external.ReportPublication;
import michael.findata.external.ReportPublicationList;
import michael.findata.external.shse.SHSEReportPublication;
import michael.findata.util.FinDataConstants;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static michael.findata.util.FinDataConstants.*;
import static michael.findata.util.FinDataConstants.s4Report;

public class NeteaseFinancialReportList implements ReportPublicationList{

	//todo: '?' from the web page still cannot be recognized.
	private static Pattern p1 = Pattern.compile("<td class=\"td_text\"><a href=\"/f10/ggmx_(\\d{6})_[\\d_]+.html\" target=\"_blank\" title='.+([\\d?һ�����������߰˾ţ�������������������]{4})��?.+'>");
	private static Pattern p2 = Pattern.compile("<td class=\"align_c\">(\\d\\d\\d\\d-\\d\\d-\\d\\d)</td>");
	private static Pattern p3 = Pattern.compile("<td class=\"td_text\">(һ���ȱ���|���ڱ���|�����ȱ���|��ȱ���)</td>");
	HashSet<ReportPublication> pbs;
	public NeteaseFinancialReportList (String code) throws IOException {
		String d = "", dt = null;
		pbs = new HashSet<>();
		int year, season;
		Date date;
		URL url;
		HttpURLConnection httpCon;
		int totalPages = 5;
		InputStream is;
		BufferedReader l_reader;
		String sBuffer3 = "", sBuffer2 = "", sBuffer1 = "";
		Matcher m1, m2, m3;
		for (int page = 1; page <= totalPages; page++) {
			url = new URL("http://quotes.money.163.com/f10/gsgg_"+code+",dqbg,"+page+".html");
			httpCon = (HttpURLConnection) url.openConnection();
			httpCon.connect();
			is = httpCon.getInputStream();
			l_reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			while ((sBuffer3 = l_reader.readLine()) != null) {
//				System.out.println(sBuffer3);
				m1 = p1.matcher(sBuffer1);
				m2 = p2.matcher(sBuffer2);
				m3 = p3.matcher(sBuffer3);
				if (m1.find() && m2.find() && m3.find()) {
//					System.out.println(m1.group(1));
//					System.out.println(m1.group(2));
					try {
						year = Integer.parseInt(m1.group(2));
					} catch (NumberFormatException ex) {
						year = Integer.parseInt(m1.group(2)
								.replace('��', '0').replace('��', '1').replace('��', '2').replace('��', '3').replace('��', '4').replace('��', '5').replace('��', '6').replace('��', '7').replace('��', '8').replace('��', '9')
								.replace('?', '0').replace('һ', '1').replace('��', '2').replace('��', '3').replace('��', '4').replace('��', '5').replace('��', '6').replace('��', '7').replace('��', '8').replace('��', '9')
						);
					}
//					System.out.println(m2.group(1));

					try {
						date = FinDataConstants.yyyyDashMMDashdd.parse(m2.group(1));
					} catch (ParseException e) {
						System.out.println("Cannot understand "+m2.group(1)+" for date.");
						continue;
					}
					switch (m3.group(1)) {
						case "һ���ȱ���":
							season = 1;
							break;
						case "���ڱ���":
							season = 2;
							break;
						case "�����ȱ���":
							season = 3;
							break;
						case "��ȱ���":
							season = 4;
							break;
						default:
							System.out.println("Cannot understand "+m3.group(1)+" for season.");
							continue;
					}
					pbs.add(new ReportPublication(date, code, null, year, season));
				}
				sBuffer1 = sBuffer2;
				sBuffer2 = sBuffer3;
			}
			l_reader.close();
			is.close();
		}

//		URLConnection connection = szseListedCompanyReportUrl.openConnection();
//		connection.setDoOutput(true);
//		OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "gb2312");
//		out.write("leftid=1&lmid=drgg&pageNo=1&stockCode="+code+"&keyword=&noticeType="+seasonParam[season-1]+"&startTime="+y+"-01-01&endTime="+(y+1)+"-12-31&tzy=&imageField.x=16&imageField.y=8");
//		out.flush();
//		out.close();
//
//		InputStream l_urlStream;
//		l_urlStream = connection.getInputStream();
//
//		Matcher m, n;
//		l_reader = new BufferedReader(new InputStreamReader(l_urlStream));
//		String year;
//		l_reader.close();
//		l_urlStream.close();
	}

	@Override
	public Collection<ReportPublication> getReportPublications() {
		return pbs;
	}
}