import com.numericalmethod.algoquant.execution.datatype.order.Order;
import com.numericalmethod.suanshu.stats.descriptive.rank.Quantile;
import michael.findata.algoquant.execution.datatype.order.HexinOrder;
import michael.findata.algoquant.strategy.TestStrategy;
import michael.findata.commandcenter.CommandCenter;
import michael.findata.external.tdx.TDXClient;
import michael.findata.model.Stock;
import michael.findata.spring.data.repository.StockRepository;
import michael.findata.util.DBUtil;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.joda.time.LocalTime;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static michael.findata.algoquant.execution.datatype.order.HexinOrder.HexinType.SIMPLE_BUY;
import static michael.findata.algoquant.execution.datatype.order.HexinOrder.HexinType.SIMPLE_SELL;

public class Plan {
	public static void main (String args []) throws IOException, InterruptedException {
		System.out.println (9999*9999);
	}
}

/**
 * select pi.code_to_short, pi.code_to_long, openable_on, pctg(max_res) max_res, pctg(ps.stdev) stdev,
 * format(max_res/stdev,2) ratio, pctg(adf_p_ma) adf_p_ma
 * from pair_instance pi, pair_stats ps
 * where pi.pair_stats_id = ps.id and max_res > 0.01 and max_res/stdev > 1.5 order by max_res desc;
 * select * from pair_stats where adf_p_ma < 0.07 and adf_p < 0.011 and training_end = date(now()) and code_to_short in ('510060', '510070', '159943');

 * select * from pair_stats where concat(code_to_short,'->',code_to_long)='601398->600016' order by training_end;

 * select count(*), training_end from pair_stats group by training_end having count(*) <> 5740;
 * select count(*), training_end from pair_stats group by training_end order by training_end;

 * select * from pair_stats where code_to_short = 510300 and code_to_long = 510160 order by training_end desc limit 10;

 * delete from pair_instance where max_res is null;

 * select count(*) from pair_instance where max_res is not null;

 * select max_res, code_to_short, code_to_long from pair_instance where max_res is not null and openable_on = '2016-06-08' order by max_res desc;

 * select * from stock where id in (select stock_id from dividend where payment_date is null and announcement_date < '2016-01-01');

 * PairStrategy需要动态仓位管理
 * HoppingStrategy不需要动态仓位管理

 * 0。
 * 利用融券ETF，对广泛ETF进行 隔日统计配对
 * 策略1：只适用于流动性良好的不多的20-30多只ETF，还是要坚持把这个策略做下去。需要对这些高流动性etf重新进行回测。

 * Step 1: update A share historical Data up to today
 * Step 2: update H share historical Data up to today
 * Step 3: Calculate stats and Create strategy for tomorrow
 *
 *
 * 0. use priceadjuster for inspecbyminute/byday
 * 0. use min residual as another indicator for pair trading: already can generate min/max/percentiles in PairStats
 * 0. maybe we need to use a timer to monitor db?
 * 0. use exchange rate in db to init in memory exchange rate
 * 0. a. Use disruptor to implement all brokers so that they function like IB
 *    b. Use disruptor to dispatch depths to strategies.
 * 0. localbrokerproxy line 115 hxOrder.ack(results[i]); if index out of bound occurs here, whole command center will pause, handle exception gracefully,
 * try to separate those orders which have issue from those don't
 * 0. ShortInHK to handle shorting during the last three minutes of HK daily session, when the most profitable moments usually happens
 * 0. ShortInHK to handle short in A too.
 *
 * 0. Residual calc to take into account split/dividend
 * 1. test correlation calculation with dividend taken into consideration
 * 2. ratio calculation in pair strategy needs to use the pairopendate as the reference date
 * 0. Test A/H pair strategy with dividends, do not rush, since dividend season is far down the road
 * 0. Order's "rejected" status
 *
 * 0. Use inspectpair to find shortinA opportunities and try to create a streamlined semi-auto process
 * 0. Learn from ETFnStockBasketTest
 * 0. HKstock minute / daily prices / dividend split
 * 0. Use Kalman Filter or Regression for H->A arb?
 * 0. automatic 股东代码 matching in NativeTdx
 * 0. ShortInHKPairStrategy has three outstanding issues, but it can already be used.

 7. Optimization issue:
 It takes 0ms to determine whether or not to open a position.
 However it takes 25+ms to calculate the position and another 25+ms to submit an order!
 11:08:19.226 [Thread-4] DEBUG ShortInHKPairStrategy,547- 	[ID: 78, 福耀玻璃(HK)->福耀玻璃, s:0.902, o:0.024, c:0.000, NEW]	: Processing depth: 600660 福耀玻璃
 Traded: true
 ask5 18.46	5000
 ask4 18.45	13400
 ask3 18.44	6300
 ask2 18.43	1400
 ask1 18.42	12900
 bid1 18.41	10700
 bid2 18.4	29600
 bid3 18.39	10500
 bid4 18.38	9500
 bid5 18.37	11000
 11:08:19.226 [Thread-4] DEBUG ShortInHKPairStrategy,517- 	[ID: 78, 福耀玻璃(HK)->福耀玻璃, s:0.902, o:0.024, c:0.000, NEW]	: Ratio: 0.8805783333333335
 11:08:19.226 [Thread-4] INFO  ShortInHKPairStrategy,590- 	[ID: 78, 福耀玻璃(HK)->福耀玻璃, s:0.902, o:0.024, c:0.000, NEW]	: Pair is currently NEW. Residual vs OpenThreshold: 0.02412296750212506 vs 0.024 - above open threshold, trying to open the pair.
 11:08:19.251 [Thread-4] INFO  ShortInHKPairStrategy,609- 	[ID: 78, 福耀玻璃(HK)->福耀玻璃, s:0.902, o:0.024, c:0.000, NEW]	: Calculated short/long volumes: 2400.0/2700.0 @ short/long prices: 23.4/18.42 with fx: 0.8939346533768381
 11:08:19.251 [Thread-4] INFO  ShortInHKPairStrategy,611- 	[ID: 78, 福耀玻璃(HK)->福耀玻璃, s:0.902, o:0.024, c:0.000, NEW]	: Calculated short/long ratio: 50203.37013364323/49734.00000000001
 11:08:19.278 [Thread-4] INFO  ShortInHKPairStrategy,629- 	[ID: 78, 福耀玻璃(HK)->福耀玻璃, s:0.902, o:0.024, c:0.000, NEW]	: Short leg opening order submitted: [11]: 	SELL 	03606 福耀玻璃(HK) @	23.400	for	-2400.000	, tags: {Broker=IB}, state: UNFILLED, filled: 0.000000
 11:08:19.279 [Thread-4] INFO  ShortInHKPairStrategy,803- 	[ID: 78, 福耀玻璃(HK)->福耀玻璃, s:0.902, o:0.024, c:0.000, NEW]	: Saving to DB after status changed from NEW to OPENING.

 * 盈富基金(02800) (恒生指数) VS 恒生ETF(SZ159920)
 * 02828.HK 恆生H股 VS 150175+150176
 * 02828.HK 恆生H股 VS 510900, H股ETF
 * 02828.HK 恆生H股 VS 160717 etc
 * 02828.HK 恆生H股 VS H股
 * 3147 @ SEHK (4.75%) VS 159915 (创业板) **
 * 02823.HK 安碩A50中國 (富時中國A50) VS A股
 * 02822.HK 南方A50 (富時中國A50) VS A股
 * 03188.HK 华夏沪深三百 VS A股

 * [Done] Be able to distinguish the same stocks in A and IB
 * [Done] ShortInHKPairStrategy do a save on any status change
 * [Done] Examine what's saved in place of empty data: zeros - 0
 * [Done] Test dividend update routine to make sure the removal of split column is ok.
 * [Done] Carelessly executed this: DELETE FROM stock_price_minute WHERE stock_id IN (1592), now need to make up for 潍柴动力's minute data
 * [Done] How to avoid depth like this being fired?
 * 2016-Dec-17 21:27:23.658 [Thread-3] INFO LocalInteractiveBrokers,306- Depth updated: 00966 中国太平(HK)
 * Traded: true
 * ask1 -1.0	0
 * bid1 -1.0	0

 * 0. 在年底要再做一次adjustment factor recalc for all stocks/interesting funds
 * 1. market order on hexin broker: this can be achieved with limit order: just relax the price much more
 * 0. Update the list of all AH stocks and able to update their price, so that our spread comparison can automatically include them
 * 0. 每天启动CommandCenter的时候，自动探测可交易仓位
 * 0. OcRegressionDouble & OcRegressionDouble64 be able to calculate stdev same as in apache math
 * 0. C#买卖单发出后，若是普通单下单成功，则会正确返回单号（ack）；其他类型下单成功不会返回单号。需要统一。
 * 1. Remove all adjFunction/adjFactor calls, replace with Adjuster calls
 * 0. 相关性条件降低后，要把相应的开仓条件升高（收紧），平仓条件升高（放松），也就是根据相关性系数，动态调整开平仓条件。
 * 8. Backup my tdx stock data mysql db data
 * 10. C# Need to monitor a) positions b) order executions
 * 0. OLMAR on more diversified chinese stocks: prepare getting stock data from my own local mysql
 *
 * 测试 java.sql.Timestamp, java.sql.Date 和 java.util.Date 在存储后，提取后的值有无变化
 * 测试结果：由于各处（包括数据库）的缺省时区是东八区，所以各时间格式可以互换。
 * 用TDXClient驱动PairStrategy
 * Completed: 第二步自动下单：测试同市场同时下单能否成功，测试不同市场下两单的时候，最小延迟要多久。
 * 普通账户一个单子0.6-0.8s，
 * 信用账户一个单子1.1s
 *
 * ETF对冲策略人工维护项目：
 * 1. 可融券标的券
 *
 * ETF对冲策略自动维护项目：
 * 1. 更新分红，分拆数据
 * 2. 测试行情服务器可用性
 *
 * 策略2：
 * 自持H股ETF（510900）、恒生H股（160717）, 恒生ETF(159920)，对t+0 ETF 进行日内统计配对
 * 可行,主要难点：1自持股比重分配以便达到最大利用率
 *
 * todo: select max(fin_date) from report_pub_dates where fin_year = 2012 and fin_season = 3
 * todo: select max(fin_date) from report_pub_dates where fin_year = 2012 and fin_season = 2
 * todo: find out what's wrong with the above
 *
 *
 * !! todo missing fin_data that can't be obtained from any source. please try harder!!
 * 000939 凯迪电力 2005 1 balance_sheet_nf missing
 * 000939 凯迪电力 2007 1 balance_sheet_nf missing
 * 000939 凯迪电力 2005 1 cash_flow_nf missing
 * todo missing report publication dates that can't be obtained from any source. please try harder!!
 * Missing: 000620 新华联 2008 3
 *
 * Missing: 000001 平安银行 1988 2
 * Missing: 000001 平安银行 1989 2
 * Missing: 000001 平安银行 1990 2
 * Missing: 000001 平安银行 1991 2
 * Missing: 000002 万  科Ａ 1990 2
 * Missing: 000002 万  科Ａ 1991 2
 * Missing: 000004 国农科技 1991 2
 * Missing: 000005 世纪星源 1991 2
 * Missing: 000005 世纪星源 1991 4
 * Missing: 000005 世纪星源 1992 2
 * Missing: 000005 世纪星源 1992 4
 * Missing: 000005 世纪星源 1993 2
 * Missing: 000006 深振业Ａ 1991 2
 * Missing: 000006 深振业Ａ 1991 4
 * Missing: 000007 零七股份 1991 2
 * Missing: 000007 零七股份 1991 4
 * Missing: 000008 宝利来 1991 2
 * Missing: 000008 宝利来 1991 4
 * Missing: 000036 华联控股 1998 2
 * Missing: 000419 通程控股 1996 2
 * Missing: 000421 南京中北 1996 2
 * Missing: 000422 湖北宜化 1996 2
 * Missing: 000425 徐工机械 1996 2
 * Missing: 000430 张家界 1996 2
 * Missing: 000629 攀钢钒钛 1998 2
 * Missing: 200002 万  科Ｂ 1990 2
 * Missing: 200002 万  科Ｂ 1991 2
 * Missing: 600601 方正科技 1991 2
 * Missing: 600601 方正科技 1992 2
 * Missing: 600602 仪电电子 1991 2
 * Missing: 600602 仪电电子 1992 2
 * Missing: 600630 龙头股份 1998 2
 * Missing: 600651 飞乐音响 1991 2
 * Missing: 600651 飞乐音响 1992 2
 * Missing: 600652 爱使股份 1991 2
 * Missing: 600652 爱使股份 1992 2
 * Missing: 600653 申华控股 1991 2
 * Missing: 600653 申华控股 1992 2
 * Missing: 600654 飞乐股份 1991 2
 * Missing: 600654 飞乐股份 1992 2
 * Missing: 600655 豫园商城 1991 2
 * Missing: 600655 豫园商城 1992 2
 * Missing: 600656 博元投资 1991 2
 * Missing: 600656 博元投资 1992 2
 * Missing: 600667 太极实业 1998 2
 * Missing: 600689 上海三毛 1998 2
 * Missing: 600720 祁连山 1996 2
 * Missing: 600724 宁波富达 1996 2
 * Missing: 600732 上海新梅 1996 2
 * Missing: 600736 苏州高新 1996 2
 * Missing: 600739 辽宁成大 1996 2
 * Missing: 600740 山西焦化 1996 2
 * Missing: 600746 江苏索普 1996 2
 * Missing: 600747 大连控股 1996 2
 * Missing: 600749 西藏旅游 1996 2
 * Missing: 600750 江中药业 1996 2
 * Missing: 600753 东方银星 1996 2
 * Missing: 600755 厦门国贸 1996 2
 * Missing: 600756 浪潮软件 1996 2
 * Missing: 600757 长江传媒 1996 2
 * Missing: 600758 红阳能源 1996 2
 * Missing: 601607 上海医药 1998 2
 * Missing: 900901 仪电Ｂ股 1991 2
 * Missing: 900901 仪电Ｂ股 1992 2
 * Missing: 900922 三毛Ｂ股 1998 2
 *
 * !! todo missing fin_data that can't be obtained from any source. please try harder!!
 * 000939 凯迪电力 2005 1 balance_sheet_nf missing
 * 000939 凯迪电力 2007 1 balance_sheet_nf missing
 * 000939 凯迪电力 2005 1 cash_flow_nf missing
 * todo missing report publication dates that can't be obtained from any source. please try harder!!
 * Missing: 000620 新华联 2008 3
 *
 * Missing: 000001 平安银行 1988 2
 * Missing: 000001 平安银行 1989 2
 * Missing: 000001 平安银行 1990 2
 * Missing: 000001 平安银行 1991 2
 * Missing: 000002 万  科Ａ 1990 2
 * Missing: 000002 万  科Ａ 1991 2
 * Missing: 000004 国农科技 1991 2
 * Missing: 000005 世纪星源 1991 2
 * Missing: 000005 世纪星源 1991 4
 * Missing: 000005 世纪星源 1992 2
 * Missing: 000005 世纪星源 1992 4
 * Missing: 000005 世纪星源 1993 2
 * Missing: 000006 深振业Ａ 1991 2
 * Missing: 000006 深振业Ａ 1991 4
 * Missing: 000007 零七股份 1991 2
 * Missing: 000007 零七股份 1991 4
 * Missing: 000008 宝利来 1991 2
 * Missing: 000008 宝利来 1991 4
 * Missing: 000036 华联控股 1998 2
 * Missing: 000419 通程控股 1996 2
 * Missing: 000421 南京中北 1996 2
 * Missing: 000422 湖北宜化 1996 2
 * Missing: 000425 徐工机械 1996 2
 * Missing: 000430 张家界 1996 2
 * Missing: 000629 攀钢钒钛 1998 2
 * Missing: 200002 万  科Ｂ 1990 2
 * Missing: 200002 万  科Ｂ 1991 2
 * Missing: 600601 方正科技 1991 2
 * Missing: 600601 方正科技 1992 2
 * Missing: 600602 仪电电子 1991 2
 * Missing: 600602 仪电电子 1992 2
 * Missing: 600630 龙头股份 1998 2
 * Missing: 600651 飞乐音响 1991 2
 * Missing: 600651 飞乐音响 1992 2
 * Missing: 600652 爱使股份 1991 2
 * Missing: 600652 爱使股份 1992 2
 * Missing: 600653 申华控股 1991 2
 * Missing: 600653 申华控股 1992 2
 * Missing: 600654 飞乐股份 1991 2
 * Missing: 600654 飞乐股份 1992 2
 * Missing: 600655 豫园商城 1991 2
 * Missing: 600655 豫园商城 1992 2
 * Missing: 600656 博元投资 1991 2
 * Missing: 600656 博元投资 1992 2
 * Missing: 600667 太极实业 1998 2
 * Missing: 600689 上海三毛 1998 2
 * Missing: 600720 祁连山 1996 2
 * Missing: 600724 宁波富达 1996 2
 * Missing: 600732 上海新梅 1996 2
 * Missing: 600736 苏州高新 1996 2
 * Missing: 600739 辽宁成大 1996 2
 * Missing: 600740 山西焦化 1996 2
 * Missing: 600746 江苏索普 1996 2
 * Missing: 600747 大连控股 1996 2
 * Missing: 600749 西藏旅游 1996 2
 * Missing: 600750 江中药业 1996 2
 * Missing: 600753 东方银星 1996 2
 * Missing: 600755 厦门国贸 1996 2
 * Missing: 600756 浪潮软件 1996 2
 * Missing: 600757 长江传媒 1996 2
 * Missing: 600758 红阳能源 1996 2
 * Missing: 601607 上海医药 1998 2
 * Missing: 900901 仪电Ｂ股 1991 2
 * Missing: 900901 仪电Ｂ股 1992 2
 * Missing: 900922 三毛Ｂ股 1998 2
 **/

/**
 * !! todo missing fin_data that can't be obtained from any source. please try harder!!
 * 000939 凯迪电力 2005 1 balance_sheet_nf missing
 * 000939 凯迪电力 2007 1 balance_sheet_nf missing
 * 000939 凯迪电力 2005 1 cash_flow_nf missing
 */

/** todo missing report publication dates that can't be obtained from any source. please try harder!!
 * Missing: 000620 新华联 2008 3

 Missing: 000001 平安银行 1988 2
 Missing: 000001 平安银行 1989 2
 Missing: 000001 平安银行 1990 2
 Missing: 000001 平安银行 1991 2
 Missing: 000002 万  科Ａ 1990 2
 Missing: 000002 万  科Ａ 1991 2
 Missing: 000004 国农科技 1991 2
 Missing: 000005 世纪星源 1991 2
 Missing: 000005 世纪星源 1991 4
 Missing: 000005 世纪星源 1992 2
 Missing: 000005 世纪星源 1992 4
 Missing: 000005 世纪星源 1993 2
 Missing: 000006 深振业Ａ 1991 2
 Missing: 000006 深振业Ａ 1991 4
 Missing: 000007 零七股份 1991 2
 Missing: 000007 零七股份 1991 4
 Missing: 000008 宝利来 1991 2
 Missing: 000008 宝利来 1991 4
 Missing: 000036 华联控股 1998 2
 Missing: 000419 通程控股 1996 2
 Missing: 000421 南京中北 1996 2
 Missing: 000422 湖北宜化 1996 2
 Missing: 000425 徐工机械 1996 2
 Missing: 000430 张家界 1996 2
 Missing: 000629 攀钢钒钛 1998 2
 Missing: 200002 万  科Ｂ 1990 2
 Missing: 200002 万  科Ｂ 1991 2
 Missing: 600601 方正科技 1991 2
 Missing: 600601 方正科技 1992 2
 Missing: 600602 仪电电子 1991 2
 Missing: 600602 仪电电子 1992 2
 Missing: 600630 龙头股份 1998 2
 Missing: 600651 飞乐音响 1991 2
 Missing: 600651 飞乐音响 1992 2
 Missing: 600652 爱使股份 1991 2
 Missing: 600652 爱使股份 1992 2
 Missing: 600653 申华控股 1991 2
 Missing: 600653 申华控股 1992 2
 Missing: 600654 飞乐股份 1991 2
 Missing: 600654 飞乐股份 1992 2
 Missing: 600655 豫园商城 1991 2
 Missing: 600655 豫园商城 1992 2
 Missing: 600656 博元投资 1991 2
 Missing: 600656 博元投资 1992 2
 Missing: 600667 太极实业 1998 2
 Missing: 600689 上海三毛 1998 2
 Missing: 600720 祁连山 1996 2
 Missing: 600724 宁波富达 1996 2
 Missing: 600732 上海新梅 1996 2
 Missing: 600736 苏州高新 1996 2
 Missing: 600739 辽宁成大 1996 2
 Missing: 600740 山西焦化 1996 2
 Missing: 600746 江苏索普 1996 2
 Missing: 600747 大连控股 1996 2
 Missing: 600749 西藏旅游 1996 2
 Missing: 600750 江中药业 1996 2
 Missing: 600753 东方银星 1996 2
 Missing: 600755 厦门国贸 1996 2
 Missing: 600756 浪潮软件 1996 2
 Missing: 600757 长江传媒 1996 2
 Missing: 600758 红阳能源 1996 2
 Missing: 601607 上海医药 1998 2
 Missing: 900901 仪电Ｂ股 1991 2
 Missing: 900901 仪电Ｂ股 1992 2
 Missing: 900922 三毛Ｂ股 1998 2
 **/