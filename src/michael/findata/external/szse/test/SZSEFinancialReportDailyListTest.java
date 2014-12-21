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
//		Pattern p1 = Pattern.compile("��\\(([0|2|3][\\d]{5})\\).+��([\\d]{4})(.+��).*��Ҫ.+");
//		Pattern p2 = Pattern.compile("��\\(([0|1|2|3][\\d]{5})��.*([0|1|2|3][\\d]{5}).*\\).+��([\\d]{4})(.+��).*��Ҫ.+");
//		Matcher m = p1.matcher("��ٰ�ʮ����(000568) �����Ͻѣ�2013��һ���ȱ�����Ҫ����ָ��");
//		Matcher m = p2.matcher("��ʮ����(300078) ����˼����2011��һ���ȱ�����Ҫ����ָ��");
//		Matcher m = p2.matcher("ʮ�š�(002008) ���弤�⣺2011�����ȱ�����Ҫ����ָ�꼰����Ԥ��");
//		Matcher m = p2.matcher("ʮ�ߡ�(300054) �����ɷݣ�2010����ȱ�����Ҫ����ָ�꼰����Ԥ��");
//		Matcher m = p1.matcher("����(000013��200013) *ST ʯ����2003�����������Ҫ����ָ��");
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
