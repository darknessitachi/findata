package michael.findata.external.szse;

import michael.findata.external.ReportPublication;
import michael.findata.external.ReportPublicationList;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static michael.findata.util.FinDataConstants.*;

public class SZSEReportPublication extends ReportPublication {

	private static URL szseListedCompanyReportUrl;
	private static String [] seasonParam = {"010305","010303", "010307", "010301"};
	public static Pattern p = Pattern.compile("target=\"new\">.*([\\d,O]{4})\\s*年(.*)报告(摘要|正文|全文)?(（更新后）|（已取消）|\\(修订版\\))?<.*");
	public static Pattern q = Pattern.compile("(\\d\\d\\d\\d-\\d\\d-\\d\\d)");

//	private ReportPublication rp;

	static {
		try {
			szseListedCompanyReportUrl = new URL("http://disclosure.szse.cn/m/search0425.jsp");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public SZSEReportPublication(String code, int y, int season) throws IOException, ParseException {
		String s, d = "", dt = null;
		URLConnection connection = szseListedCompanyReportUrl.openConnection();
		connection.setDoOutput(true);
		OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "gb2312");
		out.write("leftid=1&lmid=drgg&pageNo=1&stockCode="+code+"&keyword=&noticeType="+seasonParam[season-1]+"&startTime="+y+"-01-01&endTime="+(y+1)+"-12-31&tzy=&imageField.x=16&imageField.y=8");
		out.flush();
		out.close();

		String sBuffer3 = "", sBuffer2 = "", sBuffer1 = "";
		InputStream l_urlStream;
		l_urlStream = connection.getInputStream();

		Matcher m, n;
		BufferedReader l_reader = new BufferedReader(new InputStreamReader(l_urlStream));
		String year;
		while ((sBuffer3 = l_reader.readLine()) != null) {
			m = p.matcher(sBuffer1);
			n = q.matcher(sBuffer3);
			if (m.find() && n.find()) {
				year = m.group(1).replace('O','0');
				dt = n.group(1);
				if (!year.equals(y+"")) {
					continue;
				}
				s = m.group(2);
				if ((s.contains(s1Report) && season == 1) || ((s.contains(s2Report) || s.contains(s2Report2)) && season == 2) || (s.contains(s3Report) && season == 3) || (s.contains(s4Report) && season == 4)) {
					if ("".equals(d) || d.compareTo(dt) > 0) {
						d = dt;
					} else {
						continue;
					}
				} else {
					continue;
				}
			}
			sBuffer1 = sBuffer2;
			sBuffer2 = sBuffer3;
		}
		l_reader.close();
		l_urlStream.close();
//		System.out.println(d);
//		rp = new ReportPublication(yyyyDashMMDashdd.parse(d), code, y, season);
		super.setDate(yyyyDashMMDashdd.parse(d));
		super.setCode(code);
		super.setYear(y);
		super.setSeason(season);
	}

//	@Override
//	public Collection<ReportPublication> getReportPublications() {
//		ArrayList<ReportPublication> temp = new ArrayList<>();
//		temp.add(rp);
//		return temp;
//	}

//	@Override
//	public ReportPublication getReportPublication () {
//		return rp;
//	}
}