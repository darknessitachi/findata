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
		System.out.println(new SHSEStock("600000").equals(new SHSEStock("600000", "浦发银行")));
		System.out.println(new SHSEStock("600000").symbol());
		System.out.println(new SZSEStock("000001").symbol());
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

 0. How to know whether the stock is even traded in the first place from Netease Snapshot? need to take care of this when doing real trading.
 status:0 交易中
 status:4 停牌
 status:1 退市

 0. 紧急：一对100%相关的ETF应该允许100仓位，所以对此类票对，应该有多少吃多少，只要超过threshold.因此，需要a）注明此类票对，b）修改算法
 0. 紧急，需要搜集etf基金分拆数据方便还权。虽然现在涉及到的基金还没有在近期分拆.
 0. 相关性条件降低后，要把相应的开仓条件升高（收紧），平仓条件升高（放松），也就是根据相关性系数，动态调整开平仓条件。
 0. order execution: dynamic amount setting, adjust target amount according to liquidity (1k-3k)
 0. 紧急：需要对ETF失败交易进行彻底分析，经简单分析：失败交易包含的股票应该比较集中。
 0. 紧急：基金筛选，交易策略需要考虑还权价格。哪怕是只是在training中有拆分和分红，交易价格都需要考虑还权。
 2. 深圳可卖空的股票名单inputstream encoding
 5. 尝试股票交易自动策略下单。
 6. 完善 adjustment function，完全取消 adjustment factor？
 7. 尝试使用Kalman Filter
 8. 有时间再解决：已经从和讯搜集etf基金分红派现数据，以便对基金价格进行还权处理。但是数据质量堪忧。幸好牵涉到的26个普通ETF的分红数据都是准确的。牵涉到的4个黄金ETF中有一个博时黄金159937分红有问题，但是由于分红在2015年上半年（比较久远），所以不会受到影响到统计套利的计算。
 8. 有时间再解决：现在只有网易实时成交，需要增加新浪雪球和讯实时成交，以便DR
 8. 做一个spread计算器，方便计算任意时间，每个etf的spread，用来筛选etfpair

 ETF对冲策略
 手工维护项目：
 1. 分红数据
 2. 分拆数据
 3. 可融券标的券
 4. 行情服务器可用性

 159903+
 159919
 510050-
 510180
 510300
 510330+
 510510-
 510880+

 0.3 2


 策略1：可行,主要难点：1自持股比重分配以便达到最大利用率
 自持H股ETF（510900）、恒生H股（160717）, 恒生ETF(159920)，对t+0 ETF 进行日内统计配对

 策略4：
 利用融券ETF，对广泛ETF进行 隔日统计配对
 **/