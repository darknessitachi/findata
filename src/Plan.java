import com.numericalmethod.algoquant.model.elliott2005.strategy.Elliott2005StrategyDemo1;
import michael.findata.algoquant.strategy.pair.HoppingStrategy;

public class Plan {
	public static void main(String[] args) {
		Elliott2005StrategyDemo1 demo = new Elliott2005StrategyDemo1();
		demo.run();
	}
}
/**
 *

 select pi.code_to_short, pi.code_to_long, openable_on, pctg(max_res) max_res, pctg(ps.stdev) stdev,
 format(max_res/stdev,2) ratio, pctg(adf_p_ma) adf_p_ma
 from pair_instance pi, pair_stats ps
 where pi.pair_stats_id = ps.id and max_res > 0.01 and max_res/stdev > 1.5 order by max_res desc;

 select * from pair_stats where adf_p_ma < 0.07 and adf_p < 0.011 and training_end = date(now()) and code_to_short in ('510060', '510070', '159943');

 select count(*), training_end from pair_stats group by training_end order by training_end
 *
 紧急:  第三步动态仓位管理
 测试动态仓位控制
 0. C#买卖单发出后，若是普通单下单成功，则会正确返回单号（getAck）；其他类型下单成功不会返回单号。需要统一。
 1. Remove all adjFunction/adjFactor calls, replace with Adjuster calls
 0. 相关性条件降低后，要把相应的开仓条件升高（收紧），平仓条件升高（放松），也就是根据相关性系数，动态调整开平仓条件。
 0. 紧急：需要对ETF失败交易进行彻底分析，经简单分析：失败交易包含的股票应该比较集中。
 7. 尝试使用Kalman Filter
 8. Backup my tdx stock data mysql db data
 10. C# Need to monitor a) positions b) order executions

 测试 java.sql.Timestamp, java.sql.Date 和 java.util.Date 在存储后，提取后的值有无变化
 测试结果：由于各处（包括数据库）的缺省时区是东八区，所以各时间格式可以互换。
 用TDXClient驱动PairStrategy
 Completed: 第二步自动下单：测试同市场同时下单能否成功，测试不同市场下两单的时候，最小延迟要多久。
 普通账户一个单子0.6-0.8s，
 信用账户一个单子1.1s

 ETF对冲策略人工维护项目：
 1. 可融券标的券

 ETF对冲策略自动维护项目：
 1. 更新分红，分拆数据
 2. 测试行情服务器可用性

 策略1：
 利用融券ETF，对广泛ETF进行 隔日统计配对

 策略2：
 自持H股ETF（510900）、恒生H股（160717）, 恒生ETF(159920)，对t+0 ETF 进行日内统计配对
 可行,主要难点：1自持股比重分配以便达到最大利用率

 todo: select max(fin_date) from report_pub_dates where fin_year = 2012 and fin_season = 3
 todo: select max(fin_date) from report_pub_dates where fin_year = 2012 and fin_season = 2
 todo: find out what's wrong with the above

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