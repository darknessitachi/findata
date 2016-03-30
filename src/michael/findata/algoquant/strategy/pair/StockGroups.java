package michael.findata.algoquant.strategy.pair;

import com.numericalmethod.algoquant.data.cache.SequentialCacheFactory;
import com.numericalmethod.algoquant.data.cache.TimedEntry;
import com.numericalmethod.algoquant.data.cache.VectorCache;
import com.numericalmethod.algoquant.data.calendar.TimeZoneUtils;
import com.numericalmethod.algoquant.data.historicaldata.yahoo.YahooEODCacheFactory;
import com.numericalmethod.algoquant.execution.datatype.StockEOD;
import com.numericalmethod.algoquant.execution.datatype.product.fx.Currencies;
import com.numericalmethod.algoquant.execution.datatype.product.stock.Exchange;
import com.numericalmethod.algoquant.execution.datatype.product.stock.SimpleStock;
import com.numericalmethod.algoquant.execution.datatype.product.stock.Stock;
import com.numericalmethod.algoquant.execution.simulation.template.SimTemplateYahooEOD;
import com.numericalmethod.suanshu.misc.datastructure.time.JodaTimeUtils;
import com.numericalmethod.suanshu.stats.test.timeseries.adf.AugmentedDickeyFuller;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by nicky on 2015/11/21.
 */
public class StockGroups {
	public static void main (String [] args) throws Exception {
		// set up the list of products
		ArrayList<Stock> stockList = new ArrayList<>();

		// specify the search interval
		DateTime end = DateTime.now(TimeZoneUtils.SINGAPORE).minusDays(1);
		DateTime begin = end.minusDays(365*4);
		Interval interval = new Interval(begin, end);

		// set up the data source; we download data from Yahoo! Finance here.
		YahooEODCacheFactory yahooEOD = new YahooEODCacheFactory(SimTemplateYahooEOD.DEFAULT_DATA_FOLDER);
		stockList.stream().forEach(stockA -> stockList.stream().forEach(stockB -> {
			if (stockA.symbol().compareTo(stockB.symbol()) < 0) {
				performADFTest(stockA, stockB, interval, yahooEOD);
			}
		}));
//		System.out.println();
	}

	private static void performADFTest (Stock stockA,
										Stock stockB,
										Interval interval,
										SequentialCacheFactory<Stock, StockEOD> cacheFactory) {
		VectorCache<StockEOD> vc;
		try {
			vc = new VectorCache(cacheFactory.newInstance(stockA, interval), cacheFactory.newInstance(stockB, interval));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		DateTime now = DateTime.now();
		DateTime oneYearMark = now.minusDays(365);
		DateTime twoYearMark = now.minusDays(365*2);
		DateTime threeYearMark = now.minusDays(365*3);

		ArrayList<Double> year4 = new ArrayList<>();
		ArrayList<Double> year3 = new ArrayList<>();
		ArrayList<Double> year2 = new ArrayList<>();
		ArrayList<Double> year1 = new ArrayList<>();
		System.out.print(stockA.companyName()+" "+stockA.symbol()+"\t"+stockB.companyName()+" "+stockB.symbol());
		for (TimedEntry<VectorCache.Vector<StockEOD>> te : vc) {
			if (te.data().get(1).volume() > 0 && te.data().get(2).volume() > 0) {
				double ratio = te.data().get(1).adjClose() / te.data().get(2).adjClose();
//				double ratio = Math.log(te.data().get(1).adjClose() / te.data().get(2).adjClose());
				if (te.time().isAfter(oneYearMark)) {
					year1.add(ratio);
				}
				if (te.time().isAfter(twoYearMark)) {
					year2.add(ratio);
				}
				if (te.time().isAfter(threeYearMark)) {
					year3.add(ratio);
				}
				year4.add(ratio);
			}
		}
		System.out.print("\t");
		System.out.print(new AugmentedDickeyFuller(year1.stream().mapToDouble(b->b).toArray()).pValue() + "\t");
		System.out.print(new AugmentedDickeyFuller(year2.stream().mapToDouble(b->b).toArray()).pValue() + "\t");
		System.out.print(new AugmentedDickeyFuller(year3.stream().mapToDouble(b->b).toArray()).pValue() + "\t");
		System.out.print(new AugmentedDickeyFuller(year4.stream().mapToDouble(b->b).toArray()).pValue() + "\t");
		System.out.print(new AugmentedDickeyFuller(year1.stream().mapToDouble(b->Math.log(b)).toArray()).pValue() + "\t");
		System.out.print(new AugmentedDickeyFuller(year2.stream().mapToDouble(b->Math.log(b)).toArray()).pValue() + "\t");
		System.out.print(new AugmentedDickeyFuller(year3.stream().mapToDouble(b->Math.log(b)).toArray()).pValue() + "\t");
		System.out.println(new AugmentedDickeyFuller(year4.stream().mapToDouble(b->Math.log(b)).toArray()).pValue() + "\t");
	}

	public static Stock[] ETF = new Stock[]{
			new SimpleStock("510050.SS", "50ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510060.SS", "央企ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510180.SS", "180ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510230.SS", "金融ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510300.SS", "300ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510310.SS", "HS300ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510330.SS", "华夏300", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510500.SS", "500ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510510.SS", "广发500", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510630.SS", "消费行业", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510650.SS", "金融行业", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510660.SS", "医药行业", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510880.SS", "红利ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510900.SS", "H股ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("512070.SS", "非银ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("512610.SS", "医药卫生", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("159901.SZ", "深100ETF", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("159902.SZ", "中 小 板", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("159903.SZ", "深成ETF", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("159915.SZ", "创业板", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("159919.SZ", "300ETF", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("159920.SZ", "恒生ETF", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("159923.SZ", "100ETF", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("159929.SZ", "医药ETF", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("159939.SZ", "信息技术", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("159943.SZ", "深证ETF", Currencies.CNY, Exchange.SZSE),
	};

	public static Stock[] ETFShortable = new Stock[]{
			new SimpleStock("510050.SS", "50ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510180.SS", "180ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510300.SS", "300ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510330.SS", "华夏300", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510500.SS", "500ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510510.SS", "广发500", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510880.SS", "红利ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510900.SS", "H股ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("159901.SZ", "深100ETF", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("159902.SZ", "中小板", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("159903.SZ", "深成ETF", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("159919.SZ", "300ETF", Currencies.CNY, Exchange.SZSE),
	};

	public static Stock[] GoldETF = new Stock[]{
			new SimpleStock("159934.SZ", "黄金ETF", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("159937.SZ", "博时黄金", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("518800.SS", "黄金基金", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("518880.SS", "黄金ETF", Currencies.CNY, Exchange.SHSE),
	};

	public static Stock[] GoldETFShortable = new Stock[]{
			new SimpleStock("518880", "黄金ETF", Currencies.CNY, Exchange.SHSE),
	};

	public static Stock[] Highway = new Stock[] {
			new SimpleStock("000429.SZ", "粤高速Ａ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000548.SZ", "湖南投资", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000828.SZ", "东莞控股", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000900.SZ", "现代投资", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000916.SZ", "华北高速", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600012.SS", "皖通高速", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600020.SS", "中原高速", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600033.SS", "福建高速", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600035.SS", "楚天高速", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600269.SS", "赣粤高速", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600350.SS", "山东高速", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600377.SS", "宁沪高速", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600548.SS", "深高速", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601188.SS", "龙江交通", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601518.SS", "吉林高速", Currencies.CNY, Exchange.SHSE)
	};

	public static Stock[] Banking = new Stock[]{
			new SimpleStock("000001.SZ", "平安银行", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("002142.SZ", "宁波银行", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600000.SS", "浦发银行", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600015.SS", "华夏银行", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600016.SS", "民生银行", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600036.SS", "招商银行", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601009.SS", "南京银行", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601166.SS", "兴业银行", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601169.SS", "北京银行", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601288.SS", "农业银行", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601328.SS", "交通银行", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601398.SS", "工商银行", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601818.SS", "光大银行", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601939.SS", "建设银行", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601988.SS", "中国银行", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601998.SS", "中信银行", Currencies.CNY, Exchange.SHSE)
	};

	public static Stock[] Insurance = new Stock[]{
			new SimpleStock("601318.SS", "中国平安", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601336.SS", "新华保险", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601601.SS", "中国太保", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601628.SS", "中国人寿", Currencies.CNY, Exchange.SHSE)
	};

	public static Stock[] Electricity = new Stock[]{
			new SimpleStock("000027.SZ", "深圳能源", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000037.SZ", "深南电Ａ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000301.SZ", "东方市场", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000531.SZ", "穗恒运Ａ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000539.SZ", "粤电力Ａ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000543.SZ", "皖能电力", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000600.SZ", "建投能源", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000601.SZ", "韶能股份", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000690.SZ", "宝新能源", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000692.SZ", "惠天热电", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000695.SZ", "滨海能源", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000720.SZ", "新能泰山", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000722.SZ", "湖南发展", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000767.SZ", "漳泽电力", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000791.SZ", "甘肃电投", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000862.SZ", "银星能源", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000875.SZ", "吉电股份", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000883.SZ", "湖北能源", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000899.SZ", "赣能股份", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000939.SZ", "凯迪生态", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000958.SZ", "东方能源", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000966.SZ", "长源电力", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000993.SZ", "闽东电力", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("001896.SZ", "豫能控股", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("002039.SZ", "黔源电力", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600011.SS", "华能国际", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600021.SS", "上海电力", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600023.SS", "浙能电力", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600027.SS", "华电国际", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600098.SS", "广州发展", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600101.SS", "明星电力", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600116.SS", "三峡水利", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600131.SS", "岷江水电", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600167.SS", "联美控股", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600236.SS", "桂冠电力", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600310.SS", "桂东电力", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600396.SS", "金山股份", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600452.SS", "涪陵电力", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600483.SS", "福能股份", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600505.SS", "西昌电力", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600509.SS", "天富能源", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600578.SS", "京能电力", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600642.SS", "申能股份", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600644.SS", "*ST乐电", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600674.SS", "川投能源", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600719.SS", "大连热电", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600726.SS", "华电能源", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600744.SS", "华银电力", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600758.SS", "红阳能源", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600780.SS", "通宝能源", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600795.SS", "国电电力", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600863.SS", "内蒙华电", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600864.SS", "哈投股份", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600868.SS", "梅雁吉祥", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600886.SS", "国投电力", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600900.SS", "长江电力", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600969.SS", "郴电国际", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600979.SS", "广安爱众", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600982.SS", "宁波热电", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600995.SS", "文山电力", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("601016.SS", "节能风电", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("601985.SS", "中国核电", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("601991.SS", "大唐发电", Currencies.CNY, Exchange.SZSE)
	};

	public static Stock[] Coal = new Stock[]{
			new SimpleStock("000571.SZ", "新大洲Ａ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000723.SZ", "美锦能源", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000780.SZ", "平庄能源", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000933.SZ", "神火股份", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000937.SZ", "冀中能源", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000968.SZ", "煤 气 化", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000983.SZ", "西山煤电", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("002128.SZ", "露天煤业", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600121.SS", "郑州煤电", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600123.SS", "兰花科创", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600157.SS", "永泰能源", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600179.SS", "黑化股份", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600188.SS", "兖州煤业", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600348.SS", "阳泉煤业", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600395.SS", "盘江股份", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600397.SS", "安源煤业", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600403.SS", "大有能源", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600408.SS", "*ST安泰", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600508.SS", "上海能源", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600546.SS", "山煤国际", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600714.SS", "金瑞矿业", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600721.SS", "百花村", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600725.SS", "云维股份", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600740.SS", "山西焦化", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600792.SS", "云煤能源", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600971.SS", "恒源煤电", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600997.SS", "开滦股份", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601001.SS", "大同煤业", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601011.SS", "宝泰隆", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601015.SS", "陕西黑猫", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601088.SS", "中国神华", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601101.SS", "昊华能源", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601225.SS", "陕西煤业", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601666.SS", "平煤股份", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601699.SS", "潞安环能", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601898.SS", "中煤能源", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601918.SS", "国投新集", Currencies.CNY, Exchange.SHSE)
	};

	public static Stock[] Securities = new Stock[]{
			new SimpleStock("000166.SZ", "申万宏源", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000686.SZ", "东北证券", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000728.SZ", "国元证券", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000750.SZ", "国海证券", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000776.SZ", "广发证券", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000783.SZ", "长江证券", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("002500.SZ", "山西证券", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("002673.SZ", "西部证券", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("002736.SZ", "国信证券", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600030.SS", "中信证券", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600061.SS", "国投安信", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600109.SS", "国金证券", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600369.SS", "西南证券", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600837.SS", "海通证券", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600958.SS", "东方证券", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600999.SS", "招商证券", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601099.SS", "太平洋", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601198.SS", "东兴证券", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601211.SS", "国泰君安", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601377.SS", "兴业证券", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601555.SS", "东吴证券", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601688.SS", "华泰证券", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601788.SS", "光大证券", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601901.SS", "方正证券", Currencies.CNY, Exchange.SHSE)
	};

	public static Stock[] Alcohol = new Stock[]{
			new SimpleStock("000557.SZ", "*ST广夏", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000568.SZ", "泸州老窖", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000596.SZ", "古井贡酒", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000729.SZ", "燕京啤酒", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000752.SZ", "西藏发展", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000799.SZ", "*ST酒鬼", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000858.SZ", "五 粮 液", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000860.SZ", "顺鑫农业", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000869.SZ", "张  裕Ａ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000929.SZ", "兰州黄河", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000995.SZ", "*ST皇台", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("002304.SZ", "洋河股份", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("002461.SZ", "珠江啤酒", Currencies.CNY, Exchange.SZSE),
//		stockList.add(new SimpleStock("002646.SZ", "青青稞酒", Currencies.CNY, Exchange.SZSE));
			new SimpleStock("600059.SS", "古越龙山", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600084.SS", "中葡股份", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600090.SS", "啤酒花", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600132.SS", "重庆啤酒", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600197.SS", "伊力特", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600199.SS", "金种子酒", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600238.SS", "海南椰岛", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600260.SS", "凯乐科技", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600365.SS", "通葡股份", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600519.SS", "贵州茅台", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600543.SS", "莫高股份", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600559.SS", "老白干酒", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600573.SS", "惠泉啤酒", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600600.SS", "青岛啤酒", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600616.SS", "金枫酒业", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600702.SS", "沱牌舍得", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600779.SS", "*ST水井", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600809.SS", "山西汾酒", Currencies.CNY, Exchange.SHSE),
//		stockList.add(new SimpleStock("601579.SS", "会稽山", Currencies.CNY, Exchange.SHSE));
//		stockList.add(new SimpleStock("603198.SS", "迎驾贡酒", Currencies.CNY, Exchange.SHSE));
//		stockList.add(new SimpleStock("603369.SS", "今世缘", Currencies.CNY, Exchange.SHSE));
//		stockList.add(new SimpleStock("603589.SS", "口子窖", Currencies.CNY, Exchange.SHSE));
	};
}