package michael.findata.external.szse.test;

import michael.findata.external.ReportPublication;
import michael.findata.external.szse.SZSEFinancialReportDailyList;
import michael.findata.util.FinDataConstants;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class SZSEFinancialReportDailyListTest {
	public static void main (String [] args) throws IOException, ParseException {
//		Pattern p1 = Pattern.compile("、\\(([0|2|3][\\d]{5})\\).+：([\\d]{4})(.+度).*主要.+");
//		Pattern p2 = Pattern.compile("、\\(([0|1|2|3][\\d]{5})、.*([0|1|2|3][\\d]{5}).*\\).+：([\\d]{4})(.+度).*主要.+");
//		Matcher m = p1.matcher("五百八十二、(000568) 泸州老窖：2013年一季度报告主要财务指标");
//		Matcher m = p2.matcher("二十二、(300078) 中瑞思创：2011年一季度报告主要财务指标");
//		Matcher m = p2.matcher("十九、(002008) 大族激光：2011年半年度报告主要财务指标及分配预案");
//		Matcher m = p2.matcher("十七、(300054) 鼎龙股份：2010年年度报告主要财务指标及分配预案");
//		Matcher m = p1.matcher("三、(000013、200013) *ST 石化：2003年第三季度主要财务指标");
//		System.out.println(m.find());
//		System.out.println(m.group(1));
//		System.out.println(m.group(2));
//		System.out.println(m.group(3));
//		System.out.println(m.group(4));

		GregorianCalendar gc = new GregorianCalendar();
		gc.set(Calendar.YEAR, 2013);
		gc.set(Calendar.MONTH, 3);
		gc.set(Calendar.DAY_OF_MONTH, 24);
		for (ReportPublication p : new SZSEFinancialReportDailyList(gc.getTime()).getReportPublications()) {
			System.out.println(p.getCode());
			System.out.println(p.getYear());
			System.out.println(p.getSeason());
			System.out.println(FinDataConstants.FORMAT_yyyyDashMMDashdd.format(p.getDate()));
		}
	}
}
