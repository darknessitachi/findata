import michael.findata.external.tdx.TDXClient;
import michael.findata.model.Stock;
import michael.findata.spring.data.repository.StockRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class Plan {

	public static void main (String args []) throws IOException {

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

		TDXClient client = new TDXClient(
//				"182.131.3.245:7709",    // ��֤������J330 - 9ms)
				"221.237.158.106:7709",    // ����֤ȯ����ӳɶ�������վ1
				"221.237.158.107:7709",    // ����֤ȯ����ӳɶ�������վ2
				"221.237.158.108:7709"    // ����֤ȯ����ӳɶ�������վ3

//				"221.236.13.218:7709",	// ����֤ȯ�ɶ����� - 9-33 ms
//				"221.236.13.219:7709",	// ����֤ȯ�ɶ����� - 8-25 ms
//				"125.71.28.133:7709",	// cd1010 - 8 ms
//				"221.236.15.14:995",	// ����ɶ�����2.1
//				"124.161.97.84:7709",	// ��������ɶ���ͨ2
//				"124.161.97.83:7709",	// ��������ɶ���ͨ1
//				"125.64.39.62:7709",	// ��������ɶ�����2
//				"125.71.28.133:443",	// cd1010 - 8 ms
//				"221.236.15.14:7709",	// ����ɶ�����1.13
//				"119.6.204.139:7709",	// ����ɶ���ͨ5.135
//				"125.64.39.61:7709",	// ��������ɶ�����1
//				"125.64.41.12:7709"		// �ɶ�����54
		);
//		TDXClient client2 = new TDXClient(
//				"119.4.167.141:7709",	// ����L1
//				"119.4.167.142:7709",	// ����L2
//				"119.4.167.181:7709",	// ����L3
//				"119.4.167.182:7709",	// ����L4
//				"119.4.167.164:7709",	// ����L5
//				"119.4.167.165:7709",	// ����L6
//				"119.4.167.163:7709",	// ����L7
//				"119.4.167.175:7709",	// ����L8
//				"218.6.198.151:7709",	// ����L1
//				"218.6.198.152:7709",	// ����L2
//				"218.6.198.174:7709",	// ����L3
//				"218.6.198.175:7709"	// ����L4
//		);
//		TDXClient client3 = new TDXClient(
//				"218.6.198.155:7709",	// ����L5
//				"218.6.198.156:7709",	// ����L6
//				"218.6.198.157:7709",	// ����L7
//				"218.6.198.158:7709",	// ����L8
//				"182.131.7.141:7709",	// ����E1
//				"182.131.7.142:7709",	// ����E2
//				"182.131.7.143:7709",	// ����E3
//				"182.131.7.144:7709",	// ����E4
//				"182.131.7.145:7709",	// ����E5
//				"182.131.7.146:7709",	// ����E6
//				"182.131.7.147:7709",	// ����E7
//				"182.131.7.148:7709",	// ����E8
//				"182.131.3.245:7709",	// ��֤������J330 - 9ms)
//				"221.237.158.106:7709",	// ����֤ȯ����ӳɶ�������վ1
//				"221.237.158.107:7709",	// ����֤ȯ����ӳɶ�������վ2
//				"221.237.158.108:7709",	// ����֤ȯ����ӳɶ�������վ3
//				"183.230.9.136:7709",	// ����֤ȯ����������ƶ���վ1
//				"183.230.134.6:7709",	// ����֤ȯ����������ƶ���վ2
//				"219.153.1.115:7709",	// ����֤ȯ��������������վ1
//				"113.207.29.12:7709"	// ����֤ȯ�����������ͨ��վ1
//		);
		client.connect();
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		StockRepository sr = (StockRepository) context.getBean("stockRepository");
		for (Stock s : sr.findByFund(true)) {
			for (String [] strs : client.getXDXRInfo(s.getCode())) {
				if (strs[3].equals("11")) {
					System.out.println(s.getCode());
					break;
				}
			}
		}
		client.disconnect();
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
 ������ȯETF���Թ㷺ETF���� ����ͳ�����
 ����1��ֻ���������������õĲ����20-30��ֻETF������Ҫ��ְ������������ȥ����Ҫ����Щ��������etf���½��лز⡣


 0. backtest 510050 �� 510300 ֮�� 10 w �����������
 0. test/revise hopping strategy on stocks (instead of ETFs)
 0. test dividend update routine to make sure the removal of split column is ok.
 0. regularly test the running of xiadan2. if crashed, restart it.
 0. �����Ҫ����һ��adjustment factor recalc for all stocks/interesting funds
 1. market order on hexin broker: this can be achieved with limit order: just relax the price much more
 0. Update the list of all AH stocks and able to update their price, so that our spread comparison can automatically include them
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