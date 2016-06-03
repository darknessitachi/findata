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

import java.util.*;

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

	public static final String[] ETFBlueChipsCodes = new String [] {
			"510050", "510060", "510180", "510230", "510300", "510310", "510330",
			"510360", "510420", "510450", "510710", "510650", "510660", "510680",
			"512610", "512990", "159919", "159923", "159929", "162411", "160706",
			"160716", "165309"};

	public static final String [] ETFSmallCapCodes = new String [] {
			"510500","510510","512330","512500","512510","159901","159902",
			"159903","159907","159912","159922","159923","159939","159943",
			"160416","162411","162711"
	};

	public static Stock[] ETFBlueChips = new Stock[]{
			new SimpleStock("510050.SS", "50ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510060.SS", "����ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510180.SS", "180ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510230.SS", "����ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510420.SS", "180EWETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510450.SS", "180��ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510650.SS", "������ҵ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510660.SS", "ҽҩ��ҵ", Currencies.CNY, Exchange.SHSE),
//			new SimpleStock("510680.SS", "���50", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510710.SS", "��50ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("512610.SS", "ҽҩ����", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("512990.SS", "MSCIA��", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("159919.SZ", "300ETF", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("159923.SZ", "100ETF", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("159929.SZ", "ҽҩETF", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("162411.SZ", "��������", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("160716.SZ", "��ʵ50", Currencies.CNY, Exchange.SZSE),

			new SimpleStock("510300.SS", "300ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510310.SS", "HS300ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510330.SS", "����300", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510360.SS", "�㷢300", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("160706.SZ", "��ʵ300", Currencies.CNY, Exchange.SZSE),
	};

	public static Stock[] ETFSmallCaps = new Stock[]{
			new SimpleStock("510500.SS", "500ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510510.SS", "�㷢500", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("512330.SS", "500��Ϣ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("512500.SS", "��֤500", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("512510.SS", "ETF500", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("159901.SZ", "��100ETF", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("159902.SZ", "��С��", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("159903.SZ", "���ETF", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("159907.SZ", "��С300", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("159912.SZ", "��300ETF", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("159922.SZ", "500ETF", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("159923.SZ", "100ETF", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("159939.SZ", "��Ϣ����", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("159943.SZ", "��֤ETF", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("160416.SZ", "ʯ�ͻ���", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("162411.SZ", "��������", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("162711.SZ", "�㷢500L", Currencies.CNY, Exchange.SZSE),
	};

	// Group 3
	public static Stock[] HKETF = new Stock[] {
			new SimpleStock("159920.SZ", "����ETF", Currencies.CNY, Exchange.SZSE), //�����/�۱һ���*����ָ����ָ��������
			new SimpleStock("160125.SZ", "�Ϸ����", Currencies.CNY, Exchange.SZSE), //������һ��ʵ����ĺ���ָ��������*95%�������ͬ�ڻ��ڴ������*5%
			new SimpleStock("160717.SZ", "����H��", Currencies.CNY, Exchange.SZSE), //�����/�۱һ���*�����й���ҵָ��
			new SimpleStock("161831.SZ", "H�ɷּ�", Currencies.CNY, Exchange.SZSE), //�����/�۱һ���*�����й���ҵָ��������*95%+����һ��ڴ��������*5%��˰��
			new SimpleStock("164705.SZ", "������", Currencies.CNY, Exchange.SZSE), //������һ��ʵ����ĺ���ָ��������*95������ҵ���л��ڴ�����ʣ�˰��*5%
			new SimpleStock("510900.SS", "H��ETF", Currencies.CNY, Exchange.SHSE), //�����й���ҵָ��(ʹ�ù�ֵ��������)
			new SimpleStock("513600.SS", "��ָETF", Currencies.CNY, Exchange.SHSE), //����ָ��
			new SimpleStock("513660.SS", "����ͨ", Currencies.CNY, Exchange.SHSE) //����ֵ���ʵ����ĺ���ָ��������
	};

	// Group 4
	public static Stock[] GoldETF = new Stock[]{
			new SimpleStock("159934.SZ", "�ƽ�ETF", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("159937.SZ", "��ʱ�ƽ�", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("518800.SS", "�ƽ����", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("518880.SS", "�ƽ�ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("160719.SZ", "��ʵ�ƽ�", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("161116.SZ", "�׻��ƽ�", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("161226.SZ", "��������", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("164701.SZ", "�����", Currencies.CNY, Exchange.SZSE),
	};

	public static Stock[] ETFShortable = new Stock[]{
			new SimpleStock("510050.SS", "50ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510180.SS", "180ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510230.SS", "����ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510300.SS", "300ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510310.SS", "HS300ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510330.SS", "����300", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510500.SS", "500ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510510.SS", "�㷢500", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("510900.SS", "H��ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("512990.SS", "MSCIA��", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("512070.SS", "����ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("518880.SS", "�ƽ�ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("159901.SZ", "��100ETF", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("159902.SZ", "��С��", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("159903.SZ", "���ETF", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("159919.SZ", "300ETF", Currencies.CNY, Exchange.SZSE),
	};

	public static Stock[] TPlus0Funds = new Stock[]{
			new SimpleStock("159920.SZ", "����ETF", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("160125.SZ", "�Ϸ����", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("160416.SZ", "ʯ�ͻ���", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("160717.SZ", "����H��", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("161210.SZ", "��Ͷ����", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("161714.SZ", "���̽�ש", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("161815.SZ", "����ͨ��", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("162411.SZ", "��������", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("164701.SZ", "�����", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("164815.SZ", "������Դ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("165510.SZ", "�ų��Ĺ�", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("165513.SZ", "�ų���Ʒ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("510900.SS", "H��ETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("513030.SS", "�¹�30", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("513100.SS", "��ָETF", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("513500.SS", "����500", Currencies.CNY, Exchange.SHSE),
	};

	public static Stock[] GoldETFShortable = new Stock[]{
			new SimpleStock("518880.SS", "�ƽ�ETF", Currencies.CNY, Exchange.SHSE),
	};

	public static Stock[] TPlus0Gold = new Stock[] {
			new SimpleStock("160719.SZ", "��ʵ�ƽ�", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("161116.SZ", "�׻��ƽ�", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("164701.SZ", "�����", Currencies.CNY, Exchange.SZSE),
	};

	public static Set<String> TPlus0 = new HashSet<>();

	static {
		Arrays.stream(TPlus0Funds).forEachOrdered(stock -> TPlus0.add(stock.symbol().substring(0, 6)));
		Arrays.stream(TPlus0Gold).forEachOrdered(stock -> TPlus0.add(stock.symbol().substring(0, 6)));
	}

	public static Stock[] Highway = new Stock[] {
			new SimpleStock("000429.SZ", "�����٣�", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000548.SZ", "����Ͷ��", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000828.SZ", "��ݸ�ع�", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000900.SZ", "�ִ�Ͷ��", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000916.SZ", "��������", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600012.SS", "��ͨ����", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600020.SS", "��ԭ����", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600033.SS", "��������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600035.SS", "�������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600269.SS", "��������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600350.SS", "ɽ������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600377.SS", "��������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600548.SS", "�����", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601188.SS", "������ͨ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601518.SS", "���ָ���", Currencies.CNY, Exchange.SHSE)
	};

	public static Stock[] Banking = new Stock[]{
			new SimpleStock("000001.SZ", "ƽ������", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("002142.SZ", "��������", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600000.SS", "�ַ�����", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600015.SS", "��������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600016.SS", "��������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600036.SS", "��������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601009.SS", "�Ͼ�����", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601166.SS", "��ҵ����", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601169.SS", "��������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601288.SS", "ũҵ����", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601328.SS", "��ͨ����", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601398.SS", "��������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601818.SS", "�������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601939.SS", "��������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601988.SS", "�й�����", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601998.SS", "��������", Currencies.CNY, Exchange.SHSE)
	};

	public static Stock[] Insurance = new Stock[]{
			new SimpleStock("601318.SS", "�й�ƽ��", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601336.SS", "�»�����", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601601.SS", "�й�̫��", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601628.SS", "�й�����", Currencies.CNY, Exchange.SHSE)
	};

	public static Stock[] LargeMarketCap = new Stock [] {
			new SimpleStock("601857.SS", "�й�ʯ��", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601398.SS", "��������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601288.SS", "ũҵ����", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601988.SS", "�й�����", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601628.SS", "�й�����", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600028.SS", "�й�ʯ��", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601318.SS", "�й�ƽ��", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600000.SS", "�ַ�����", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600036.SS", "��������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600519.SS", "����ę́", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601166.SS", "��ҵ����", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600016.SS", "��������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601766.SS", "�й��г�", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601088.SS", "�й���", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600104.SS", "��������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601328.SS", "��ͨ����", Currencies.CNY, Exchange.SHSE),
//			new SimpleStock("601998.SS", "��������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601668.SS", "�й�����", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601601.SS", "�й�̫��", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601818.SS", "�������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601800.SS", "�й�����", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601390.SS", "�й�����", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601989.SS", "�й��ع�", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601169.SS", "��������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("000001.SZ", "ƽ������", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600018.SS", "�ϸۼ���", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600900.SS", "��������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601186.SS", "�й�����", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600837.SS", "��֤ͨȯ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("000858.SZ", "����Һ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("601006.SS", "������·", Currencies.CNY, Exchange.SHSE)
	};

	public static Stock[] Electricity = new Stock[]{
			new SimpleStock("000027.SZ", "������Դ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000037.SZ", "���ϵ��", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000301.SZ", "�����г�", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000531.SZ", "����ˣ�", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000539.SZ", "��������", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000543.SZ", "���ܵ���", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000600.SZ", "��Ͷ��Դ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000601.SZ", "���ܹɷ�", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000690.SZ", "������Դ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000692.SZ", "�����ȵ�", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000695.SZ", "������Դ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000720.SZ", "����̩ɽ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000722.SZ", "���Ϸ�չ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000767.SZ", "�������", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000791.SZ", "�����Ͷ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000862.SZ", "������Դ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000875.SZ", "����ɷ�", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000883.SZ", "������Դ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000899.SZ", "���ܹɷ�", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000939.SZ", "������̬", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000958.SZ", "������Դ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000966.SZ", "��Դ����", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000993.SZ", "��������", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("001896.SZ", "ԥ�ܿع�", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("002039.SZ", "ǭԴ����", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600011.SS", "���ܹ���", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600021.SS", "�Ϻ�����", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600023.SS", "���ܵ���", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600027.SS", "�������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600098.SS", "���ݷ�չ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600101.SS", "���ǵ���", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600116.SS", "��Ͽˮ��", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600131.SS", "ẽ�ˮ��", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600167.SS", "�����ع�", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600236.SS", "��ڵ���", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600310.SS", "�𶫵���", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600396.SS", "��ɽ�ɷ�", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600452.SS", "�������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600483.SS", "���ܹɷ�", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600505.SS", "��������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600509.SS", "�츻��Դ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600578.SS", "���ܵ���", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600642.SS", "���ܹɷ�", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600644.SS", "*ST�ֵ�", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600674.SS", "��Ͷ��Դ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600719.SS", "�����ȵ�", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600726.SS", "������Դ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600744.SS", "��������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600758.SS", "������Դ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600780.SS", "ͨ����Դ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600795.SS", "�������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600863.SS", "���ɻ���", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600864.SS", "��Ͷ�ɷ�", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600868.SS", "÷�㼪��", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600886.SS", "��Ͷ����", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600900.SS", "��������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600969.SS", "�������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600979.SS", "�㰲����", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600982.SS", "�����ȵ�", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600995.SS", "��ɽ����", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601016.SS", "���ܷ��", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601985.SS", "�й��˵�", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601991.SS", "���Ʒ���", Currencies.CNY, Exchange.SHSE)
	};

	public static Stock[] Coal = new Stock[]{
			new SimpleStock("000571.SZ", "�´��ޣ�", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000723.SZ", "������Դ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000780.SZ", "ƽׯ��Դ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000933.SZ", "���ɷ�", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000937.SZ", "������Դ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000968.SZ", "ú �� ��", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000983.SZ", "��ɽú��", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("002128.SZ", "¶��úҵ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600121.SS", "֣��ú��", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600123.SS", "�����ƴ�", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600157.SS", "��̩��Դ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600179.SS", "�ڻ��ɷ�", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600188.SS", "����úҵ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600348.SS", "��Ȫúҵ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600395.SS", "�̽��ɷ�", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600397.SS", "��Դúҵ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600403.SS", "������Դ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600408.SS", "*ST��̩", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600508.SS", "�Ϻ���Դ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600546.SS", "ɽú����", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600714.SS", "�����ҵ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600721.SS", "�ٻ���", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600725.SS", "��ά�ɷ�", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600740.SS", "ɽ������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600792.SS", "��ú��Դ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600971.SS", "��Դú��", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600997.SS", "���йɷ�", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601001.SS", "��ͬúҵ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601011.SS", "��̩¡", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601015.SS", "������è", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601088.SS", "�й���", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601101.SS", "껻���Դ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601225.SS", "����úҵ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601666.SS", "ƽú�ɷ�", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601699.SS", "º������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601898.SS", "��ú��Դ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601918.SS", "��Ͷ�¼�", Currencies.CNY, Exchange.SHSE)
	};

	public static Stock[] Securities = new Stock[]{
			new SimpleStock("000166.SZ", "�����Դ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000686.SZ", "����֤ȯ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000728.SZ", "��Ԫ֤ȯ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000750.SZ", "����֤ȯ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000776.SZ", "�㷢֤ȯ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000783.SZ", "����֤ȯ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("002500.SZ", "ɽ��֤ȯ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("002673.SZ", "����֤ȯ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("002736.SZ", "����֤ȯ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("600030.SS", "����֤ȯ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600061.SS", "��Ͷ����", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600109.SS", "����֤ȯ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600369.SS", "����֤ȯ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600837.SS", "��֤ͨȯ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600958.SS", "����֤ȯ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600999.SS", "����֤ȯ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601099.SS", "̫ƽ��", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601198.SS", "����֤ȯ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601211.SS", "��̩����", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601377.SS", "��ҵ֤ȯ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601555.SS", "����֤ȯ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601688.SS", "��̩֤ȯ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601788.SS", "���֤ȯ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("601901.SS", "����֤ȯ", Currencies.CNY, Exchange.SHSE)
	};

	public static Stock[] Alcohol = new Stock[]{
			new SimpleStock("000557.SZ", "*ST����", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000568.SZ", "�����Ͻ�", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000596.SZ", "�ž�����", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000729.SZ", "�ྩơ��", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000752.SZ", "���ط�չ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000799.SZ", "*ST�ƹ�", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000858.SZ", "����Һ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000860.SZ", "˳��ũҵ", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000869.SZ", "��ԣ��", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000929.SZ", "���ݻƺ�", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("000995.SZ", "*ST��̨", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("002304.SZ", "��ӹɷ�", Currencies.CNY, Exchange.SZSE),
			new SimpleStock("002461.SZ", "�齭ơ��", Currencies.CNY, Exchange.SZSE),
//		stockList.add(new SimpleStock("002646.SZ", "��������", Currencies.CNY, Exchange.SZSE));
			new SimpleStock("600059.SS", "��Խ��ɽ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600084.SS", "���Ϲɷ�", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600090.SS", "ơ�ƻ�", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600132.SS", "����ơ��", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600197.SS", "������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600199.SS", "�����Ӿ�", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600238.SS", "����Ҭ��", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600260.SS", "���ֿƼ�", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600365.SS", "ͨ�Ϲɷ�", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600519.SS", "����ę́", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600543.SS", "Ī�߹ɷ�", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600559.SS", "�ϰ׸ɾ�", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600573.SS", "��Ȫơ��", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600600.SS", "�ൺơ��", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600616.SS", "����ҵ", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600702.SS", "�������", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600779.SS", "*STˮ��", Currencies.CNY, Exchange.SHSE),
			new SimpleStock("600809.SS", "ɽ���ھ�", Currencies.CNY, Exchange.SHSE),
//		stockList.add(new SimpleStock("601579.SS", "���ɽ", Currencies.CNY, Exchange.SHSE));
//		stockList.add(new SimpleStock("603198.SS", "ӭ�ݹ���", Currencies.CNY, Exchange.SHSE));
//		stockList.add(new SimpleStock("603369.SS", "����Ե", Currencies.CNY, Exchange.SHSE));
//		stockList.add(new SimpleStock("603589.SS", "���ӽ�", Currencies.CNY, Exchange.SHSE));
	};
}