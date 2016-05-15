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
 5相关性条件降低后，要把相应的平仓条件放松
 注：把相关性过滤条件提高到0.95，测试etf套利，奇迹会发生。

 交易算法：
 1. 确认价格进入交易区间

 2. 如下 A - G 中选出最小值 H

 一、融券卖出：
 1. 确认可融券数量 A
 2. 确认可买方数量 B
 3. 确认融券上限足够 C

 二、买入：
 1. 确认资金 D
 2. 确认卖方数量 E

 三、资产杠杆上限 F

 四、单只票对买入上限 G

 3. 以H为基准进行融券卖出
 4. 以H为基准进行融资买入

 紧急: 证券账户窗口的排列是动态的，桌面上谁在最前面谁就在内存里拍第一，需要另一个方法识别窗口！！！！！
 紧急: 开始用TDXClient驱动PairStrategy
 第一步记录开平仓机会
 第二步自动下单： 测试同市场同时下单能否成功，测试不同市场下两单的时候，最小延迟要多久。
 第三步动态仓位管理
 交易时编程：
 检测交易所snapshots时间间隔。
 测试动态仓位控制
 1. Remove all adjFunction/adjFactor calls, replace with Adjuster calls
 一对100%整协的ETF应该允许100仓位, 所以对此类票对, 只要超过threshold, 应该有多少吃多少. 因此需要 a)注明此类票对; b)修改算法
 票对评级，并且将评级运用在开平仓的时候以便控制仓位
 动态分析ETF Spread，计算每天平均，精准筛选小spreadETF
 5. 尝试股票交易自动策略下单。
 0. 相关性条件降低后，要把相应的开仓条件升高（收紧），平仓条件升高（放松），也就是根据相关性系数，动态调整开平仓条件。
 0. 紧急：需要对ETF失败交易进行彻底分析，经简单分析：失败交易包含的股票应该比较集中。
 7. 尝试使用Kalman Filter
 8. Backup my tdx stock data mysql db data
 8. C#: When credit account is ready, need to customize HexinBroker for Zhongxin Credit account.
 9. C#: HexinBroker SellAndBuy: todo: need to re-process result so it contains errors/successes in the correct order
 10. C# Need to monitor a) positions b) order executions
 测试 java.sql.Timestamp, java.sql.Date 和 java.util.Date 在存储后，提取后的值有无变化
 测试结果：由于各处（包括数据库）的缺省时区是东八区，所以各时间格式可以互换。

 ETF对冲策略人工维护项目：
 1. 可融券标的券
 2. 行情服务器可用性

 ETF对冲策略自动维护项目：
 1. 分红，分拆数据

 策略1：
 利用融券ETF，对广泛ETF进行 隔日统计配对

 策略2：可行,主要难点：1自持股比重分配以便达到最大利用率
 自持H股ETF（510900）、恒生H股（160717）, 恒生ETF(159920)，对t+0 ETF 进行日内统计配对

 策略3：追涨停
 T日带量涨停，并且最低价不是涨停价（即在低位有成交），则T+1日最高价超过T日涨停价2%以上的几率有多大？
 若T+1日最高价无法超过T日涨停价2%，则在T+1日尾平仓，平均亏损多少？

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