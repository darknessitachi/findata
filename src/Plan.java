import michael.findata.algoquant.strategy.TestStrategy;
import michael.findata.algoquant.strategy.grid.GridStrategy;
import michael.findata.algoquant.strategy.pair.stocks.ShortInHKPairStrategy;
import michael.findata.commandcenter.CommandCenter;
import michael.findata.email.AsyncMailer;
import michael.findata.external.tdx.TDXClient;
import michael.findata.spring.data.repository.GridStrategyRepository;
import michael.findata.spring.data.repository.ShortInHkPairStrategyRepository;
import michael.findata.spring.data.repository.StockRepository;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.apache.commons.mail.EmailException;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class Plan {
	public static void main (String args []) throws IOException, EmailException {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		StockRepository stockRepo = (StockRepository) context.getBean("stockRepository");
		CommandCenter cc = (CommandCenter) context.getBean("commandCenter");
		cc.setShSzHqClient(new TDXClient(TDXClient.TDXClientConfigs));

		cc.addStrategy(new TestStrategy(stockRepo));

		// Set up command center
		int hour = 21;
		int minute = 40;
		cc.setFirstHalfEndCN(new LocalTime(hour, minute, 10));
		cc.setSecondHalfStartCN(new LocalTime(hour, minute, 25));
		cc.setSecondHalfEndCN(new LocalTime(hour, minute, 59));
		cc.setFirstHalfEndHK(new LocalTime(hour, minute, 10));
		cc.setSecondHalfStartHK(new LocalTime(hour, minute, 30));
		cc.setSecondHalfEndHK(new LocalTime(hour, minute, 55));

		cc.start();

	}
}

/**
 * select pi.code_to_short, pi.code_to_long, openable_on, pctg(max_res) max_res, pctg(ps.stdev) stdev,
 * format(max_res/stdev,2) ratio, pctg(adf_p_ma) adf_p_ma
 * from pair_instance pi, pair_stats ps
 * where pi.pair_stats_id = ps.id and max_res > 0.01 and max_res/stdev > 1.5 order by max_res desc;
 * <p>
 * select * from pair_stats where adf_p_ma < 0.07 and adf_p < 0.011 and training_end = date(now()) and code_to_short in ('510060', '510070', '159943');
 * <p>
 * select * from pair_stats where concat(code_to_short,'->',code_to_long)='601398->600016' order by training_end;
 * <p>
 * select count(*), training_end from pair_stats group by training_end having count(*) <> 5740;
 * select count(*), training_end from pair_stats group by training_end order by training_end;
 * <p>
 * select * from pair_stats where code_to_short = 510300 and code_to_long = 510160 order by training_end desc limit 10;
 * <p>
 * delete from pair_instance where max_res is null;
 * <p>
 * select count(*) from pair_instance where max_res is not null;
 * <p>
 * select max_res, code_to_short, code_to_long from pair_instance where max_res is not null and openable_on = '2016-06-08' order by max_res desc;
 * <p>
 * select * from stock where id in (select stock_id from dividend where payment_date is null and announcement_date < '2016-01-01');
 * <p>
 * <p>
 * PairStrategy��Ҫ��̬��λ����
 * HoppingStrategy����Ҫ��̬��λ����
 * <p>
 * 0��
 * ������ȯETF���Թ㷺ETF���� ����ͳ�����
 * ����1��ֻ���������������õĲ����20-30��ֻETF������Ҫ��ְ������������ȥ����Ҫ����Щ��������etf���½��лز⡣
 * <p>
 * Step 1: update A share historical Data up to today
 * Step 2: update H share historical Data up to today
 * Step 3: Calculate stats and Create strategy for tomorrow
 *
 * 0. Order's "rejected" status
 * 0. How to stop depth like this to be fired?
 * 2016-Dec-17 21:27:23.658 [Thread-3] INFO LocalInteractiveBrokers,306- Depth updated: 00966 �й�̫ƽ(HK)
 * Traded: true
 * ask1 -1.0	0
 * bid1 -1.0	0

 * 0. Email notification for GridStrategy
 * 0. Use inspectpair to find shortinA opportunities and try to create a stremlined semi auto process
 * 0. Learn from ETFnStockBasketTest
 * 0. ShortInHKPairStrategy has three outstanding issues, but it can already be used.
 * 0. automatic �ɶ����� matching in NativeTdx
 * 0. Test A/H pair stra with dividends, do not rush, since dividend season is far down the road
 * 0. HKstock minute / daily prices / dividend split
 * 0. Use Kalman Filter or Regression for H->A arb?
 * <p>
 * ӯ������(02800) (����ָ��) VS ����ETF(SZ159920)
 * 02828.HK �a��H�� VS 150175+150176
 * 02828.HK �a��H�� VS 510900, H��ETF
 * 02828.HK �a��H�� VS 160717 etc
 * 02828.HK �a��H�� VS H��
 * 3147 @ SEHK (4.75%) VS 159915 (��ҵ��) **
 * 02823.HK ���TA50�Ї� (���r�Ї�A50) VS A��
 * 02822.HK �Ϸ�A50 (���r�Ї�A50) VS A��
 * 03188.HK ���Ļ������� VS A��

 * [Done] Be able to distinguish the same stocks in A and IB
 * [Done] ShortInHKPairStrategy do a save on any status change
 * [Done] Examine what's saved in place of empty data: zeros - 0
 * [Done] Test dividend update routine to make sure the removal of split column is ok.
 * [Done] Carelessly executed this: DELETE FROM stock_price_minute WHERE stock_id IN (1592), now need to make up for Ϋ����'s minute data

 * 0. �����Ҫ����һ��adjustment factor recalc for all stocks/interesting funds
 * 1. market order on hexin broker: this can be achieved with limit order: just relax the price much more
 * 0. Update the list of all AH stocks and able to update their price, so that our spread comparison can automatically include them
 * 0. ÿ������CommandCenter��ʱ���Զ�̽��ɽ��ײ�λ
 * 0. OcRegressionDouble & OcRegressionDouble64 be able to calculate stdev same as in apache math
 * 0. C#������������������ͨ���µ��ɹ��������ȷ���ص��ţ�ack�������������µ��ɹ����᷵�ص��š���Ҫͳһ��
 * 1. Remove all adjFunction/adjFactor calls, replace with Adjuster calls
 * 0. ������������ͺ�Ҫ����Ӧ�Ŀ����������ߣ��ս�����ƽ���������ߣ����ɣ���Ҳ���Ǹ��������ϵ������̬������ƽ��������
 * 8. Backup my tdx stock data mysql db data
 * 10. C# Need to monitor a) positions b) order executions
 * 0. OLMAR on more diversified chinese stocks: prepare getting stock data from my own local mysql
 * <p>
 * ���� java.sql.Timestamp, java.sql.Date �� java.util.Date �ڴ洢����ȡ���ֵ���ޱ仯
 * ���Խ�������ڸ������������ݿ⣩��ȱʡʱ���Ƕ����������Ը�ʱ���ʽ���Ի�����
 * ��TDXClient����PairStrategy
 * Completed: �ڶ����Զ��µ�������ͬ�г�ͬʱ�µ��ܷ�ɹ������Բ�ͬ�г���������ʱ����С�ӳ�Ҫ��á�
 * ��ͨ�˻�һ������0.6-0.8s��
 * �����˻�һ������1.1s
 * <p>
 * ETF�Գ�����˹�ά����Ŀ��
 * 1. ����ȯ���ȯ
 * <p>
 * ETF�Գ�����Զ�ά����Ŀ��
 * 1. ���·ֺ죬�ֲ�����
 * 2. �������������������
 * <p>
 * ����2��
 * �Գ�H��ETF��510900��������H�ɣ�160717��, ����ETF(159920)����t+0 ETF ��������ͳ�����
 * ����,��Ҫ�ѵ㣺1�Գֹɱ��ط����Ա�ﵽ���������
 * <p>
 * todo: select max(fin_date) from report_pub_dates where fin_year = 2012 and fin_season = 3
 * todo: select max(fin_date) from report_pub_dates where fin_year = 2012 and fin_season = 2
 * todo: find out what's wrong with the above
 * <p>
 * <p>
 * !! todo missing fin_data that can't be obtained from any source. please try harder!!
 * 000939 ���ϵ��� 2005 1 balance_sheet_nf missing
 * 000939 ���ϵ��� 2007 1 balance_sheet_nf missing
 * 000939 ���ϵ��� 2005 1 cash_flow_nf missing
 * todo missing report publication dates that can't be obtained from any source. please try harder!!
 * Missing: 000620 �»��� 2008 3
 * <p>
 * Missing: 000001 ƽ������ 1988 2
 * Missing: 000001 ƽ������ 1989 2
 * Missing: 000001 ƽ������ 1990 2
 * Missing: 000001 ƽ������ 1991 2
 * Missing: 000002 ��  �ƣ� 1990 2
 * Missing: 000002 ��  �ƣ� 1991 2
 * Missing: 000004 ��ũ�Ƽ� 1991 2
 * Missing: 000005 ������Դ 1991 2
 * Missing: 000005 ������Դ 1991 4
 * Missing: 000005 ������Դ 1992 2
 * Missing: 000005 ������Դ 1992 4
 * Missing: 000005 ������Դ 1993 2
 * Missing: 000006 ����ҵ�� 1991 2
 * Missing: 000006 ����ҵ�� 1991 4
 * Missing: 000007 ���߹ɷ� 1991 2
 * Missing: 000007 ���߹ɷ� 1991 4
 * Missing: 000008 ������ 1991 2
 * Missing: 000008 ������ 1991 4
 * Missing: 000036 �����ع� 1998 2
 * Missing: 000419 ͨ�̿ع� 1996 2
 * Missing: 000421 �Ͼ��б� 1996 2
 * Missing: 000422 �����˻� 1996 2
 * Missing: 000425 �칤��е 1996 2
 * Missing: 000430 �żҽ� 1996 2
 * Missing: 000629 �ʸַ��� 1998 2
 * Missing: 200002 ��  �ƣ� 1990 2
 * Missing: 200002 ��  �ƣ� 1991 2
 * Missing: 600601 �����Ƽ� 1991 2
 * Missing: 600601 �����Ƽ� 1992 2
 * Missing: 600602 �ǵ���� 1991 2
 * Missing: 600602 �ǵ���� 1992 2
 * Missing: 600630 ��ͷ�ɷ� 1998 2
 * Missing: 600651 �������� 1991 2
 * Missing: 600651 �������� 1992 2
 * Missing: 600652 ��ʹ�ɷ� 1991 2
 * Missing: 600652 ��ʹ�ɷ� 1992 2
 * Missing: 600653 �껪�ع� 1991 2
 * Missing: 600653 �껪�ع� 1992 2
 * Missing: 600654 ���ֹɷ� 1991 2
 * Missing: 600654 ���ֹɷ� 1992 2
 * Missing: 600655 ԥ԰�̳� 1991 2
 * Missing: 600655 ԥ԰�̳� 1992 2
 * Missing: 600656 ��ԪͶ�� 1991 2
 * Missing: 600656 ��ԪͶ�� 1992 2
 * Missing: 600667 ̫��ʵҵ 1998 2
 * Missing: 600689 �Ϻ���ë 1998 2
 * Missing: 600720 ����ɽ 1996 2
 * Missing: 600724 �������� 1996 2
 * Missing: 600732 �Ϻ���÷ 1996 2
 * Missing: 600736 ���ݸ��� 1996 2
 * Missing: 600739 �����ɴ� 1996 2
 * Missing: 600740 ɽ������ 1996 2
 * Missing: 600746 �������� 1996 2
 * Missing: 600747 �����ع� 1996 2
 * Missing: 600749 �������� 1996 2
 * Missing: 600750 ����ҩҵ 1996 2
 * Missing: 600753 �������� 1996 2
 * Missing: 600755 ���Ź�ó 1996 2
 * Missing: 600756 �˳���� 1996 2
 * Missing: 600757 ������ý 1996 2
 * Missing: 600758 ������Դ 1996 2
 * Missing: 601607 �Ϻ�ҽҩ 1998 2
 * Missing: 900901 �ǵ�¹� 1991 2
 * Missing: 900901 �ǵ�¹� 1992 2
 * Missing: 900922 ��ë�¹� 1998 2
 * <p>
 * !! todo missing fin_data that can't be obtained from any source. please try harder!!
 * 000939 ���ϵ��� 2005 1 balance_sheet_nf missing
 * 000939 ���ϵ��� 2007 1 balance_sheet_nf missing
 * 000939 ���ϵ��� 2005 1 cash_flow_nf missing
 * todo missing report publication dates that can't be obtained from any source. please try harder!!
 * Missing: 000620 �»��� 2008 3
 * <p>
 * Missing: 000001 ƽ������ 1988 2
 * Missing: 000001 ƽ������ 1989 2
 * Missing: 000001 ƽ������ 1990 2
 * Missing: 000001 ƽ������ 1991 2
 * Missing: 000002 ��  �ƣ� 1990 2
 * Missing: 000002 ��  �ƣ� 1991 2
 * Missing: 000004 ��ũ�Ƽ� 1991 2
 * Missing: 000005 ������Դ 1991 2
 * Missing: 000005 ������Դ 1991 4
 * Missing: 000005 ������Դ 1992 2
 * Missing: 000005 ������Դ 1992 4
 * Missing: 000005 ������Դ 1993 2
 * Missing: 000006 ����ҵ�� 1991 2
 * Missing: 000006 ����ҵ�� 1991 4
 * Missing: 000007 ���߹ɷ� 1991 2
 * Missing: 000007 ���߹ɷ� 1991 4
 * Missing: 000008 ������ 1991 2
 * Missing: 000008 ������ 1991 4
 * Missing: 000036 �����ع� 1998 2
 * Missing: 000419 ͨ�̿ع� 1996 2
 * Missing: 000421 �Ͼ��б� 1996 2
 * Missing: 000422 �����˻� 1996 2
 * Missing: 000425 �칤��е 1996 2
 * Missing: 000430 �żҽ� 1996 2
 * Missing: 000629 �ʸַ��� 1998 2
 * Missing: 200002 ��  �ƣ� 1990 2
 * Missing: 200002 ��  �ƣ� 1991 2
 * Missing: 600601 �����Ƽ� 1991 2
 * Missing: 600601 �����Ƽ� 1992 2
 * Missing: 600602 �ǵ���� 1991 2
 * Missing: 600602 �ǵ���� 1992 2
 * Missing: 600630 ��ͷ�ɷ� 1998 2
 * Missing: 600651 �������� 1991 2
 * Missing: 600651 �������� 1992 2
 * Missing: 600652 ��ʹ�ɷ� 1991 2
 * Missing: 600652 ��ʹ�ɷ� 1992 2
 * Missing: 600653 �껪�ع� 1991 2
 * Missing: 600653 �껪�ع� 1992 2
 * Missing: 600654 ���ֹɷ� 1991 2
 * Missing: 600654 ���ֹɷ� 1992 2
 * Missing: 600655 ԥ԰�̳� 1991 2
 * Missing: 600655 ԥ԰�̳� 1992 2
 * Missing: 600656 ��ԪͶ�� 1991 2
 * Missing: 600656 ��ԪͶ�� 1992 2
 * Missing: 600667 ̫��ʵҵ 1998 2
 * Missing: 600689 �Ϻ���ë 1998 2
 * Missing: 600720 ����ɽ 1996 2
 * Missing: 600724 �������� 1996 2
 * Missing: 600732 �Ϻ���÷ 1996 2
 * Missing: 600736 ���ݸ��� 1996 2
 * Missing: 600739 �����ɴ� 1996 2
 * Missing: 600740 ɽ������ 1996 2
 * Missing: 600746 �������� 1996 2
 * Missing: 600747 �����ع� 1996 2
 * Missing: 600749 �������� 1996 2
 * Missing: 600750 ����ҩҵ 1996 2
 * Missing: 600753 �������� 1996 2
 * Missing: 600755 ���Ź�ó 1996 2
 * Missing: 600756 �˳���� 1996 2
 * Missing: 600757 ������ý 1996 2
 * Missing: 600758 ������Դ 1996 2
 * Missing: 601607 �Ϻ�ҽҩ 1998 2
 * Missing: 900901 �ǵ�¹� 1991 2
 * Missing: 900901 �ǵ�¹� 1992 2
 * Missing: 900922 ��ë�¹� 1998 2
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