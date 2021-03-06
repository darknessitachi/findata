package michael.findata.external.szse.test;

import michael.findata.external.ReportPublication;
import michael.findata.external.szse.SZSEFinancialReportDailyList;
import michael.findata.util.FinDataConstants;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SZSEFinancialReportDailyListTest {
	public static void main (String [] args) throws IOException, ParseException {
		Pattern p3 = Pattern.compile("、\\(([0|1|2|3][\\d]{5})\\)(.+)：([\\d]{4})(.+度)报告.+\\2现发布\\3\\4");
		Matcher m = p3.matcher("七十九、(002075)沙钢股份：2015年年度报告    沙钢股份现发布2015年年度报告");
//		Matcher m = p2.matcher("二十二、(300078) 中瑞思创：2011年一季度报告主要财务指标");
//		Matcher m = p2.matcher("十九、(002008) 大族激光：2011年半年度报告主要财务指标及分配预案");
//		Matcher m = p2.matcher("十七、(300054) 鼎龙股份：2010年年度报告主要财务指标及分配预案");
//		Matcher m = p1.matcher("三、(000013、200013) *ST 石化：2003年第三季度主要财务指标");
//		System.out.println(m.find());
//		System.out.println(m.group(1));
//		System.out.println(m.group(2));
//		System.out.println(m.group(3));
//		System.out.println(m.group(4));

		SimpleDateFormat FORMAT_yyyyDashMMDashdd = new SimpleDateFormat(FinDataConstants.yyyyDashMMDashdd);
		GregorianCalendar gc = new GregorianCalendar();
		gc.set(Calendar.YEAR, 2016);
		gc.set(Calendar.MONTH, 2);
		gc.set(Calendar.DAY_OF_MONTH, 31);
		for (ReportPublication p : new SZSEFinancialReportDailyList(gc.getTime()).getReportPublications()) {
			System.out.println(p.getCode());
			System.out.println(p.getYear());
			System.out.println(p.getSeason());
			System.out.println(FORMAT_yyyyDashMMDashdd.format(p.getDate()));
		}
	}
}
