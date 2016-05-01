import com.numericalmethod.algoquant.data.calendar.HolidayCalendarFromYahoo;
import com.numericalmethod.algoquant.execution.datatype.product.stock.Exchange;
import michael.findata.algoquant.product.stock.shse.SHSEStock;
import michael.findata.algoquant.product.stock.szse.SZSEStock;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

public class Plan {
	public static void main (String args []) {
//		DateTime date = LocalDate.parse("2015-10-08").plusDays(1).toDateTimeAtStartOfDay().plusHours(2);
//		System.out.println(date);
//		System.out.println(HolidayCalendarFromYahoo.forExchange(Exchange.SZSE).isHoliday(date));
//		System.out.println(HolidayCalendarFromYahoo.forExchange(Exchange.SHSE).isHoliday(date));
		System.out.println(new SHSEStock("600000").equals(new SHSEStock("600000", "�ַ�����")));
		System.out.println(new SHSEStock("600000").symbol());
		System.out.println(new SZSEStock("000001").symbol());
	}
}
/**
 5������������ͺ�Ҫ����Ӧ��ƽ����������
 ע��������Թ���������ߵ�0.95������etf�������漣�ᷢ����

 �����㷨��
 1. ȷ�ϼ۸���뽻������

 2. ���� A - G ��ѡ����Сֵ H

 һ����ȯ������
 1. ȷ�Ͽ���ȯ���� A
 2. ȷ�Ͽ������� B
 3. ȷ����ȯ�����㹻 C

 �������룺
 1. ȷ���ʽ� D
 2. ȷ���������� E

 �����ʲ��ܸ����� F

 �ġ���ֻƱ���������� G

 3. ��HΪ��׼������ȯ����
 4. ��HΪ��׼������������

 0. How to know whether the stock is even traded in the first place from Netease Snapshot? need to take care of this when doing real trading.
 status:0 ������
 status:4 ͣ��
 status:1 ����

 0. ������һ��100%��ص�ETFӦ������100��λ�����ԶԴ���Ʊ�ԣ�Ӧ���ж��ٳԶ��٣�ֻҪ����threshold.��ˣ���Ҫa��ע������Ʊ�ԣ�b���޸��㷨
 0. ��������Ҫ�Ѽ�etf����ֲ����ݷ��㻹Ȩ����Ȼ�����漰���Ļ���û���ڽ��ڷֲ�.
 0. ������������ͺ�Ҫ����Ӧ�Ŀ����������ߣ��ս�����ƽ���������ߣ����ɣ���Ҳ���Ǹ��������ϵ������̬������ƽ��������
 0. order execution: dynamic amount setting, adjust target amount according to liquidity (1k-3k)
 0. ��������Ҫ��ETFʧ�ܽ��׽��г��׷��������򵥷�����ʧ�ܽ��װ����Ĺ�ƱӦ�ñȽϼ��С�
 0. ����������ɸѡ�����ײ�����Ҫ���ǻ�Ȩ�۸�������ֻ����training���в�ֺͷֺ죬���׼۸���Ҫ���ǻ�Ȩ��
 2. ���ڿ����յĹ�Ʊ����inputstream encoding
 5. ���Թ�Ʊ�����Զ������µ���
 6. ���� adjustment function����ȫȡ�� adjustment factor��
 7. ����ʹ��Kalman Filter
 8. ��ʱ���ٽ�����Ѿ��Ӻ�Ѷ�Ѽ�etf����ֺ��������ݣ��Ա�Ի���۸���л�Ȩ�������������������ǡ��Һ�ǣ�浽��26����ͨETF�ķֺ����ݶ���׼ȷ�ġ�ǣ�浽��4���ƽ�ETF����һ����ʱ�ƽ�159937�ֺ������⣬�������ڷֺ���2015���ϰ��꣨�ȽϾ�Զ�������Բ����ܵ�Ӱ�쵽ͳ�������ļ��㡣
 8. ��ʱ���ٽ��������ֻ������ʵʱ�ɽ�����Ҫ��������ѩ���Ѷʵʱ�ɽ����Ա�DR
 8. ��һ��spread�������������������ʱ�䣬ÿ��etf��spread������ɸѡetfpair

 ETF�Գ����
 �ֹ�ά����Ŀ��
 1. �ֺ�����
 2. �ֲ�����
 3. ����ȯ���ȯ
 4. ���������������

 159903+
 159919
 510050-
 510180
 510300
 510330+
 510510-
 510880+

 0.3 2


 ����1������,��Ҫ�ѵ㣺1�Գֹɱ��ط����Ա�ﵽ���������
 �Գ�H��ETF��510900��������H�ɣ�160717��, ����ETF(159920)����t+0 ETF ��������ͳ�����

 ����4��
 ������ȯETF���Թ㷺ETF���� ����ͳ�����
 **/