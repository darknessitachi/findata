import com.numericalmethod.suanshu.algebra.linear.vector.doubles.dense.DenseVector;

import java.io.IOException;
import java.util.Arrays;

public class Plan {

	public static void main (String args []) throws IOException {

//		Double [] n = new Double[] {1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d};
		double [] n = new double [] {1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d};
		DenseVector v = new DenseVector(Arrays.copyOfRange(n, 1, n.length));
		System.out.println(v);
		DenseVector vMinusOne = new DenseVector(Arrays.copyOfRange(n, 0, n.length-1));
		System.out.println(vMinusOne);
		v = v.minus(vMinusOne).divide(vMinusOne);
		System.out.println(v);

		// a
//		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
//		StockService ss = (StockService) context.getBean("stockService");
//
//		Set<String> liquidETFs = ss.getStockGroup("michael/findata/algoquant/strategy/pair/group_commodity.csv");
//		liquidETFs.addAll(ss.getStockGroup("michael/findata/algoquant/strategy/pair/group_domestic_bluechip.csv"));
//		liquidETFs.addAll(ss.getStockGroup("michael/findata/algoquant/strategy/pair/group_domestic_smallcap.csv"));
//		liquidETFs.addAll(ss.getStockGroup("michael/findata/algoquant/strategy/pair/group_gold.csv"));
//
//		StockRepository stockRepo = (StockRepository) context.getBean("stockRepository");
//		Collection<Stock> stocks = stockRepo.findByCodeIn(liquidETFs);
//		for (Stock s :stocks) {
//			s.setInteresting(true);
//		}
//		stockRepo.save(stocks);

		// b
//		System.out.println(sizeof );
//		Chart chart = ChartUtils.plotXYSeries("Demo XY Plot",
//				"x",
//				"y",
//				new String[]{"series 1", "series 2"},
//				new double[][]{
//						{1, 3, 4, 6, 2, 3, 1, 3, 5, 3, 4, 6, 8, 3, 2, 4},
//						{4, 6, 8, 3, 2, 4, 1, 3, 4, 6, 2, 3, 1, 3, 5, 3}});
//
////		File dir = new File("log");
////		dir.mkdir();
////		chart.saveAsPNG(new File(dir, "xy.png"), 400, 300);
//		chart.show();
	}
}
/**
 *

 select pi.code_to_short, pi.code_to_long, openable_on, pctg(max_res) max_res, pctg(ps.stdev) stdev,
 format(max_res/stdev,2) ratio, pctg(adf_p_ma) adf_p_ma
 from pair_instance pi, pair_stats ps
 where pi.pair_stats_id = ps.id and max_res > 0.01 and max_res/stdev > 1.5 order by max_res desc;

 select * from pair_stats where adf_p_ma < 0.07 and adf_p < 0.011 and training_end = date(now()) and code_to_short in ('510060', '510070', '159943');

 select * from pair_stats where concat(code_to_short,'->',code_to_long)='601398->600016' order by training_end;

 select count(*), training_end from pair_stats group by training_end having count(*) <> 5740;
 select count(*), training_end from pair_stats group by training_end order by training_end;

 select * from pair_stats where code_to_short = 510300 and code_to_long = 510160 order by training_end desc limit 10;

 delete from pair_instance where max_res is null;

 select count(*) from pair_instance where max_res is not null;

 select max_res, code_to_short, code_to_long from pair_instance where max_res is not null and openable_on = '2016-06-08' order by max_res desc;

 select * from stock where id in (select stock_id from dividend where payment_date is null and announcement_date < '2016-01-01');

 *
 PairStrategy��Ҫ��̬��λ����
 HoppingStrategy����Ҫ��̬��λ����

 0��
 ����1��ֻ���������������õĲ����20-30��ֻETF������Ҫ��ְ������������ȥ����Ҫ����Щ��������etf���½��лز⡣
 ������ȯETF���Թ㷺ETF���� ����ͳ�����

 0. regularly test the running of xiadan2. if crashed, restart it.
 1. market order on hexin broker: this can be achieved with limit order: just relax the price much more
 0. test/revise hopping strategy on stocks (instead of ETFs)
 0. OLMAR on more diversified chinese stocks: prepare getting stock data from my own local mysql
 0. ÿ������CommandCenter��ʱ���Զ�̽��ɽ��ײ�λ
 0. OcRegressionDouble & OcRegressionDouble64 be able to calculate stdev same as in apache math
 0. C#������������������ͨ���µ��ɹ��������ȷ���ص��ţ�getAck�������������µ��ɹ����᷵�ص��š���Ҫͳһ��
 1. Remove all adjFunction/adjFactor calls, replace with Adjuster calls
 0. ������������ͺ�Ҫ����Ӧ�Ŀ����������ߣ��ս�����ƽ���������ߣ����ɣ���Ҳ���Ǹ��������ϵ������̬������ƽ��������
 0. ��������Ҫ��ETFʧ�ܽ��׽��г��׷��������򵥷�����ʧ�ܽ��װ����Ĺ�ƱӦ�ñȽϼ��С�
 8. Backup my tdx stock data mysql db data
 10. C# Need to monitor a) positions b) order executions

 ���� java.sql.Timestamp, java.sql.Date �� java.util.Date �ڴ洢����ȡ���ֵ���ޱ仯
 ���Խ�������ڸ������������ݿ⣩��ȱʡʱ���Ƕ����������Ը�ʱ���ʽ���Ի�����
 ��TDXClient����PairStrategy
 Completed: �ڶ����Զ��µ�������ͬ�г�ͬʱ�µ��ܷ�ɹ������Բ�ͬ�г���������ʱ����С�ӳ�Ҫ��á�
 ��ͨ�˻�һ������0.6-0.8s��
 �����˻�һ������1.1s

 ETF�Գ�����˹�ά����Ŀ��
 1. ����ȯ���ȯ

 ETF�Գ�����Զ�ά����Ŀ��
 1. ���·ֺ죬�ֲ�����
 2. �������������������

 ����2��
 �Գ�H��ETF��510900��������H�ɣ�160717��, ����ETF(159920)����t+0 ETF ��������ͳ�����
 ����,��Ҫ�ѵ㣺1�Գֹɱ��ط����Ա�ﵽ���������

 todo: select max(fin_date) from report_pub_dates where fin_year = 2012 and fin_season = 3
 todo: select max(fin_date) from report_pub_dates where fin_year = 2012 and fin_season = 2
 todo: find out what's wrong with the above

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