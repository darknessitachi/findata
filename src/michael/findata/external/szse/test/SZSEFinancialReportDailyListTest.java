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
		Pattern p3 = Pattern.compile("��\\(([0|1|2|3][\\d]{5})\\)(.+)��([\\d]{4})(.+��)����.+\\2�ַ���\\3\\4");
		Matcher m = p3.matcher("��ʮ�š�(002075)ɳ�ֹɷݣ�2015����ȱ���    ɳ�ֹɷ��ַ���2015����ȱ���");
//		Matcher m = p2.matcher("��ʮ����(300078) ����˼����2011��һ���ȱ�����Ҫ����ָ��");
//		Matcher m = p2.matcher("ʮ�š�(002008) ���弤�⣺2011�����ȱ�����Ҫ����ָ�꼰����Ԥ��");
//		Matcher m = p2.matcher("ʮ�ߡ�(300054) �����ɷݣ�2010����ȱ�����Ҫ����ָ�꼰����Ԥ��");
//		Matcher m = p1.matcher("����(000013��200013) *ST ʯ����2003�����������Ҫ����ָ��");
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
