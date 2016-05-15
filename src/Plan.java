import michael.findata.model.PairInstance;
import michael.findata.spring.data.repository.PairInstanceRepository;
import org.joda.time.LocalDate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Plan {
	public static void main (String args []) {
		LocalDate now = new LocalDate();
		System.out.println(now.getYear());
		System.out.println(now.getMonthOfYear());
		System.out.println(now.getDayOfMonth());
//		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
//		PairInstanceRepository pir = (PairInstanceRepository) context.getBean("pairInstanceRepository");
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd G 'at' hh:mm:ss z");
//		PairInstance pi = pir.findOne(1);
//		System.out.println(sdf.format(pi.getOpenableDate()));
//		Date testDate = new Date();
//		System.out.println(sdf.format(testDate));
//		pi.setOpenableDate(new Timestamp(testDate.getTime()));
//		System.out.println(sdf.format(pi.getOpenableDate()));
//		pir.save(pi);
//
//		pi = pir.findOne(1);
//		System.out.println(sdf.format(pi.getOpenableDate()));
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

 ����: ֤ȯ�˻����ڵ������Ƕ�̬�ģ�������˭����ǰ��˭�����ڴ����ĵ�һ����Ҫ��һ������ʶ�𴰿ڣ���������
 ����: ��ʼ��TDXClient����PairStrategy
 ��һ����¼��ƽ�ֻ���
 �ڶ����Զ��µ��� ����ͬ�г�ͬʱ�µ��ܷ�ɹ������Բ�ͬ�г���������ʱ����С�ӳ�Ҫ��á�
 ��������̬��λ����
 ����ʱ��̣�
 ��⽻����snapshotsʱ������
 ���Զ�̬��λ����
 1. Remove all adjFunction/adjFactor calls, replace with Adjuster calls
 һ��100%��Э��ETFӦ������100��λ, ���ԶԴ���Ʊ��, ֻҪ����threshold, Ӧ���ж��ٳԶ���. �����Ҫ a)ע������Ʊ��; b)�޸��㷨
 Ʊ�����������ҽ����������ڿ�ƽ�ֵ�ʱ���Ա���Ʋ�λ
 ��̬����ETF Spread������ÿ��ƽ������׼ɸѡСspreadETF
 5. ���Թ�Ʊ�����Զ������µ���
 0. ������������ͺ�Ҫ����Ӧ�Ŀ����������ߣ��ս�����ƽ���������ߣ����ɣ���Ҳ���Ǹ��������ϵ������̬������ƽ��������
 0. ��������Ҫ��ETFʧ�ܽ��׽��г��׷��������򵥷�����ʧ�ܽ��װ����Ĺ�ƱӦ�ñȽϼ��С�
 7. ����ʹ��Kalman Filter
 8. Backup my tdx stock data mysql db data
 8. C#: When credit account is ready, need to customize HexinBroker for Zhongxin Credit account.
 9. C#: HexinBroker SellAndBuy: todo: need to re-process result so it contains errors/successes in the correct order
 10. C# Need to monitor a) positions b) order executions
 ���� java.sql.Timestamp, java.sql.Date �� java.util.Date �ڴ洢����ȡ���ֵ���ޱ仯
 ���Խ�������ڸ������������ݿ⣩��ȱʡʱ���Ƕ����������Ը�ʱ���ʽ���Ի�����

 ETF�Գ�����˹�ά����Ŀ��
 1. ����ȯ���ȯ
 2. ���������������

 ETF�Գ�����Զ�ά����Ŀ��
 1. �ֺ죬�ֲ�����

 ����1��
 ������ȯETF���Թ㷺ETF���� ����ͳ�����

 ����2������,��Ҫ�ѵ㣺1�Գֹɱ��ط����Ա�ﵽ���������
 �Գ�H��ETF��510900��������H�ɣ�160717��, ����ETF(159920)����t+0 ETF ��������ͳ�����

 ����3��׷��ͣ
 T�մ�����ͣ��������ͼ۲�����ͣ�ۣ����ڵ�λ�гɽ�������T+1����߼۳���T����ͣ��2%���ϵļ����ж��
 ��T+1����߼��޷�����T����ͣ��2%������T+1��βƽ�֣�ƽ��������٣�

 **/

/**
 * !! todo missing fin_data that can't be obtained from any source. please try harder!!
 * 000939 ���ϵ��� 2005 1 balance_sheet_nf missing
 * 000939 ���ϵ��� 2007 1 balance_sheet_nf missing
 * 000939 ���ϵ��� 2005 1 cash_flow_nf missing
 */

/** todo missing report publication dates that can't be obtained from any source. please try harder!!
 * Missing: 000620 �»��� 2008 3

 Missing: 000001 ƽ������ 1988 2
 Missing: 000001 ƽ������ 1989 2
 Missing: 000001 ƽ������ 1990 2
 Missing: 000001 ƽ������ 1991 2
 Missing: 000002 ��  �ƣ� 1990 2
 Missing: 000002 ��  �ƣ� 1991 2
 Missing: 000004 ��ũ�Ƽ� 1991 2
 Missing: 000005 ������Դ 1991 2
 Missing: 000005 ������Դ 1991 4
 Missing: 000005 ������Դ 1992 2
 Missing: 000005 ������Դ 1992 4
 Missing: 000005 ������Դ 1993 2
 Missing: 000006 ����ҵ�� 1991 2
 Missing: 000006 ����ҵ�� 1991 4
 Missing: 000007 ���߹ɷ� 1991 2
 Missing: 000007 ���߹ɷ� 1991 4
 Missing: 000008 ������ 1991 2
 Missing: 000008 ������ 1991 4
 Missing: 000036 �����ع� 1998 2
 Missing: 000419 ͨ�̿ع� 1996 2
 Missing: 000421 �Ͼ��б� 1996 2
 Missing: 000422 �����˻� 1996 2
 Missing: 000425 �칤��е 1996 2
 Missing: 000430 �żҽ� 1996 2
 Missing: 000629 �ʸַ��� 1998 2
 Missing: 200002 ��  �ƣ� 1990 2
 Missing: 200002 ��  �ƣ� 1991 2
 Missing: 600601 �����Ƽ� 1991 2
 Missing: 600601 �����Ƽ� 1992 2
 Missing: 600602 �ǵ���� 1991 2
 Missing: 600602 �ǵ���� 1992 2
 Missing: 600630 ��ͷ�ɷ� 1998 2
 Missing: 600651 �������� 1991 2
 Missing: 600651 �������� 1992 2
 Missing: 600652 ��ʹ�ɷ� 1991 2
 Missing: 600652 ��ʹ�ɷ� 1992 2
 Missing: 600653 �껪�ع� 1991 2
 Missing: 600653 �껪�ع� 1992 2
 Missing: 600654 ���ֹɷ� 1991 2
 Missing: 600654 ���ֹɷ� 1992 2
 Missing: 600655 ԥ԰�̳� 1991 2
 Missing: 600655 ԥ԰�̳� 1992 2
 Missing: 600656 ��ԪͶ�� 1991 2
 Missing: 600656 ��ԪͶ�� 1992 2
 Missing: 600667 ̫��ʵҵ 1998 2
 Missing: 600689 �Ϻ���ë 1998 2
 Missing: 600720 ����ɽ 1996 2
 Missing: 600724 �������� 1996 2
 Missing: 600732 �Ϻ���÷ 1996 2
 Missing: 600736 ���ݸ��� 1996 2
 Missing: 600739 �����ɴ� 1996 2
 Missing: 600740 ɽ������ 1996 2
 Missing: 600746 �������� 1996 2
 Missing: 600747 �����ع� 1996 2
 Missing: 600749 �������� 1996 2
 Missing: 600750 ����ҩҵ 1996 2
 Missing: 600753 �������� 1996 2
 Missing: 600755 ���Ź�ó 1996 2
 Missing: 600756 �˳���� 1996 2
 Missing: 600757 ������ý 1996 2
 Missing: 600758 ������Դ 1996 2
 Missing: 601607 �Ϻ�ҽҩ 1998 2
 Missing: 900901 �ǵ�¹� 1991 2
 Missing: 900901 �ǵ�¹� 1992 2
 Missing: 900922 ��ë�¹� 1998 2
 **/