package michael.findata.test;

import com.numericalmethod.algoquant.data.cache.SequentialCache;
import junit.framework.Assert;
import michael.findata.algoquant.execution.component.broker.LocalInteractiveBrokers;
import michael.findata.algoquant.execution.component.broker.LocalTdxBrokerProxy;
import michael.findata.algoquant.execution.component.broker.MetaBroker;
import michael.findata.algoquant.execution.datatype.StockEOM;
import michael.findata.algoquant.execution.datatype.order.HexinOrder;
import michael.findata.algoquant.strategy.grid.GridStrategy;
import michael.findata.algoquant.strategy.pair.PairStrategyUtil;
import michael.findata.algoquant.strategy.pair.stocks.ShortInHKPairStrategy;
import michael.findata.data.local.LocalEOMCacheFactory;
import michael.findata.email.AsyncMailer;
import michael.findata.external.ib.HistoricalData;
import michael.findata.external.jrj.JrjHkInstantSnapshot;
import michael.findata.external.sina.SinaHkInstantSnapshot;
import michael.findata.external.tdx.test.FindataTests;
import michael.findata.model.Stock;
import michael.findata.service.DividendService;
import michael.findata.service.PairStrategyService;
import michael.findata.service.StockPriceMinuteService;
import michael.findata.spring.data.repository.GridStrategyRepository;
import michael.findata.spring.data.repository.PairStatsRepository;
import michael.findata.spring.data.repository.ShortInHkPairStrategyRepository;
import michael.findata.spring.data.repository.StockRepository;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.*;

import java.util.ArrayList;
import java.util.List;

import static michael.findata.util.LogUtil.getClassLogger;
import static michael.findata.algoquant.execution.datatype.order.HexinOrder.HexinType.SIMPLE_BUY;
import static michael.findata.algoquant.execution.datatype.order.HexinOrder.HexinType.SIMPLE_SELL;
import static michael.findata.util.FinDataConstants.OPENCL_REGRESSION_SIZE;

@Test
@ContextConfiguration(locations = { "classpath:michael/findata/test/test_no_tdx_client.xml" })
public class FindataTestsWithoutTdxClient extends AbstractTestNGSpringContextTests {

	private static final Logger LOGGER = getClassLogger();

	@Autowired
	private HistoricalData historicalData;

	@Autowired
	private StockRepository stockRepo;

	@Autowired
	private PairStatsRepository pairStatsRepo;

	@Autowired
	private ShortInHkPairStrategyRepository shortInHkPairStrategyRepo;

	@Autowired
	private PairStrategyService pairStrategyService;

	@Autowired
	private LocalEOMCacheFactory localEOMCacheFactory;

	@Autowired
	private StockPriceMinuteService stockPriceMinuteService;

	@Autowired
	private DividendService ds;

	@Autowired
	private GridStrategyRepository gridStrategyRepo;

	private long timestamp;

	// Run once, e.g. Database connection, connection pool
	@BeforeClass
	public void onceBeforeClass() {
		LOGGER.info("@BeforeClass - runOnceBefore tests in "+FindataTests.class);
	}

	// Run once, e.g close connection, cleanup
	@AfterClass
	public void onceAfterClass() {
		LOGGER.info("@AfterClass - runOnceAfter tests in "+FindataTests.class);
	}

	// Run before every test
	// e.g. Creating an similar object and share for all @Test
	@BeforeMethod
	public void beforeTestMethod() {
		LOGGER.debug("@Before - runBeforeTestMethod");
		timestamp = System.currentTimeMillis();
	}

	// Run after every test
	// Should rename to @AfterTestMethod
	@AfterMethod
	public void afterTestMethod() {
		LOGGER.debug("@After - runAfterTestMethod");
		System.out.println("Time taken: "+(System.currentTimeMillis() - timestamp)+" ms.");
	}

	@Test
	public void test_LocalTdxBrokerProxyTest() {
		new LocalTdxBrokerProxy(10001);
	}

	@Test
	public void test_LocalEOMCacheFactory () {
		SequentialCache<StockEOM> cache = localEOMCacheFactory.newInstance(new Stock("000338"),
				Interval.parse("2016-08-01T08:00:00/2016-08-08T15:30:00"));
		cache.forEach(entry -> {
			System.out.println(entry.time());
		});
	}

	@Test
	public void test_JrjHkInstantSnapshot () {
		JrjHkInstantSnapshot snapshot = new JrjHkInstantSnapshot("01288", "02039");
		snapshot.depths().values().forEach(System.out::println);
	}

	@Test
	public void test_SinaHkInstantSnapshot () {
		SinaHkInstantSnapshot snapshot = new SinaHkInstantSnapshot("01288", "02039", "03606", "00998", "06818");
		snapshot.depths().values().forEach(System.out::println);
	}

	@Test
	public void test_MataBroker () throws InterruptedException {
		MetaBroker broker = new MetaBroker();
		Thread.currentThread().sleep(5000l);
		ArrayList<HexinOrder> orderList = new ArrayList<>();

		Stock s000338 = stockRepo.findOneByCode("000338");
		Stock s002128 = stockRepo.findOneByCode("002128");
		Stock hk01288 = stockRepo.findOneByCode("01288");
		Stock hk00914 = stockRepo.findOneByCode("00914");

		HexinOrder sell000338 = new HexinOrder(s000338, 100, 555, SIMPLE_SELL);
		HexinOrder buy600026 = new HexinOrder(s002128, 100, 1.11, SIMPLE_SELL);
		HexinOrder sell01288 = new HexinOrder(hk01288, 1000, 241, SIMPLE_SELL);
		HexinOrder buy00914 = new HexinOrder(hk00914, 1000, 1.11, SIMPLE_BUY);

		orderList.add(sell01288);
		orderList.add(buy00914);
		orderList.add(sell000338);
		orderList.add(buy600026);

		broker.sendOrder(orderList);
		broker.cancelOrder(orderList);

		broker.stop();
	}

	@Test
	public void test_LocalInteractiveBrokers () {
		LocalInteractiveBrokers broker = new LocalInteractiveBrokers(4001, stockRepo.findByCodeIn("00914", "03606").toArray(new Stock [2]));
//		ArrayList<HexinOrder> orderList = new ArrayList<>();

//		Stock hk01288 = new Stock("01288");
//		Stock hk00914 = new Stock("00914");
//
//		HexinOrder sell01288 = new HexinOrder(hk01288, 10000, 4.71, SIMPLE_SELL);
//		HexinOrder buy00914 = new HexinOrder(hk00914, 1000, 14.23, SIMPLE_BUY);
//
//		orderList.add(sell01288);
//		orderList.add(buy00914);
//
//		broker.sendOrder(orderList);
//		broker.cancelOrder(orderList);
		try {
			Thread.currentThread().sleep(600000);
			broker.stop();
//		} catch (IOException e) {
//			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test_PairStrategyUtil () {
		double p1 = 19.31*1.1385;
		double p2 = 12.46;
		double [] result = PairStrategyUtil.calVolumes(p1, 100000, 100, p2, 10000, 1000, 40000, 30000, PairStrategyUtil.BalanceOption.CLOSEST_MATCH, 0.005, 0.01, 0.02, 0.05);
		System.out.printf("Results: %f / %f\n", result[0], result[1]);
		System.out.printf("Results: %f / %f\n", result[0]*p1, result[1]*p2);
		System.out.printf("Results: %f \n", result[0]*p1/(result[1]*p2));
		p1 = 18.27;
		p2 = 20.20963059808983;
		result = PairStrategyUtil.calVolumes(p2, 10000, 500, p1, 21300, 100, 40000, 20000, PairStrategyUtil.BalanceOption.CLOSEST_MATCH, 0.005, 0.01, 0.02, 0.05);
		System.out.printf("Results: %f / %f\n", result[0], result[1]);
		System.out.printf("Results: %f / %f\n", result[0]*p2, result[1]*p1);
		System.out.printf("Results: %f \n", result[0]*p2/(result[1]*p1));
		result = PairStrategyUtil.calVolumes(p1, 100000, 100, p2, 10000, 1000, 40000, 30000, PairStrategyUtil.BalanceOption.CLOSEST_MATCH, 0.005, 0.01, 0.02, 0.05);
		Assert.assertNull(result);
	}

	@Test
	public void test_ShortInHKPairStrategy () {
		ShortInHKPairStrategy pairStrategy = new ShortInHKPairStrategy(pairStatsRepo.findOne(1176732));
		shortInHkPairStrategyRepo.save(pairStrategy);
	}

	@Test
	public void test_PairStrategyService_CreatePair () {
//		pairStrategyService.createPair("06030", "600030");
//		pairStrategyService.createPair("01398", "601398");
//		pairStrategyService.createPair("00939", "601939");
//		pairStrategyService.createPair("03328", "601328");
//		pairStrategyService.createPair("00386", "600028");
//		pairStrategyService.createPair("02318", "601318");
//		pairStrategyService.createPair("02338", "000338");
	}

	@Test
	public void test_HistoricalData () throws InterruptedException {
//		historicalData.update(false);
		historicalData.update(false,
				168, 2196, 1088, 6818, 6837, 358, 2318, 2338, 3606, 914, 177, 386,
				1398, 939, 6030, 3328, 3968, 2800, 2828, 3147, 2823, 2822, 3188, 2333);
//		historicalData.update(true, 1288, 3988);
	}

	@Test
	public void test_StockPriceMinuteService_AShare() {
		LocalDate start = new LocalDate(2016, 5, 16);
		LocalDate end = new LocalDate(2016, 5, 17);
		DividendService.PriceAdjuster pa = ds.newPriceAdjuster(start, end, "601009", "000568", "510300");
		stockPriceMinuteService.walk(start,
				end,
				false,
				(date, data) -> {
					System.out.print(date);
					System.out.print("\tAdjusted:\t" + pa.adjust("601009", start, date.toLocalDate(), data.get("601009").getClose()/1000d));
					System.out.print("\tAdjusted:\t" + pa.adjust("000568", start, date.toLocalDate(), data.get("000568").getClose()/1000d) + "\t" + data.get("000568").isTraded());
					System.out.println("\tAdjusted:\t" + pa.adjust("510300", start, date.toLocalDate(), data.get("510300").getClose()/1000d));},
				"601009", "000568", "510300"
		);
	}

	@Test
	public void test_DividendService_calculateAdjustmentFactorForStock () {
//		ds.calculateAdjFactorForStock("000338");
		ds.calculateAdjFactorForStock("02338");
		ds.calculateAdjFactorForStock("200054");
		ds.calculateAdjFactorForStock("600381");
	}

	@Test
	public void test_DividendService_priceAdjuster() {
		LocalDate start = new LocalDate(2016, 1, 1);
		LocalDate end = new LocalDate(2016, 12, 31);
		DividendService.PriceAdjuster pa = ds.newPriceAdjuster(start, end, "000338", "02338");

		System.out.println(pa.adjust("000338", LocalDate.parse("2016-07-27"), LocalDate.parse("2016-07-28"), 10d));
		System.out.println(pa.adjust("000338", LocalDate.parse("2016-07-28"), LocalDate.parse("2016-07-28"), 10d));
		System.out.println(pa.adjust("000338", LocalDate.parse("2016-07-28"), LocalDate.parse("2016-07-29"), 10d));
		System.out.println(pa.adjust("000338", LocalDate.parse("2016-07-28"), LocalDate.parse("2016-07-30"), 10d));
		System.out.println(pa.adjust("000338", LocalDate.parse("2016-07-28"), LocalDate.parse("2016-10-19"), 10d));
		System.out.println(pa.adjust("000338", LocalDate.parse("2016-07-28"), LocalDate.parse("2016-10-20"), 10d));

		System.out.println(pa.adjust("000338", LocalDate.parse("2016-07-29"), LocalDate.parse("2016-07-29"), 10d));
		System.out.println(pa.adjust("000338", LocalDate.parse("2016-07-29"), LocalDate.parse("2016-07-30"), 10d));
		System.out.println(pa.adjust("000338", LocalDate.parse("2016-07-29"), LocalDate.parse("2016-10-19"), 10d));
		System.out.println(pa.adjust("000338", LocalDate.parse("2016-07-29"), LocalDate.parse("2016-10-20"), 10d));

		System.out.println(pa.adjust("02338", LocalDate.parse("2016-07-02"), LocalDate.parse("2016-07-03"), 10d));
		System.out.println(pa.adjust("02338", LocalDate.parse("2016-07-03"), LocalDate.parse("2016-07-03"), 10d));
		System.out.println(pa.adjust("02338", LocalDate.parse("2016-07-03"), LocalDate.parse("2016-07-04"), 10d));
		System.out.println(pa.adjust("02338", LocalDate.parse("2016-07-03"), LocalDate.parse("2016-07-05"), 10d));
		System.out.println(pa.adjust("02338", LocalDate.parse("2016-07-03"), LocalDate.parse("2016-09-20"), 10d));
		System.out.println(pa.adjust("02338", LocalDate.parse("2016-07-03"), LocalDate.parse("2016-09-21"), 10d));

		System.out.println(pa.adjust("02338", LocalDate.parse("2016-07-04"), LocalDate.parse("2016-07-04"), 10d));
		System.out.println(pa.adjust("02338", LocalDate.parse("2016-07-04"), LocalDate.parse("2016-07-05"), 10d));
		System.out.println(pa.adjust("02338", LocalDate.parse("2016-07-04"), LocalDate.parse("2016-09-20"), 10d));
		System.out.println(pa.adjust("02338", LocalDate.parse("2016-07-04"), LocalDate.parse("2016-09-21"), 10d));
	}

	@Test
	public void test_StockPriceMinuteService_HKShare() {
		LocalDate start = new LocalDate(2016, 9, 1);
		LocalDate end = new LocalDate(2016, 9, 2);
		String key = "00914";
		stockPriceMinuteService.walk(start,
				end,
				false,
				(date, data) -> {
					System.out.print(date);
					System.out.println("\tClose:\t" + data.get(key).isTraded() + "\t"+data.get(key).close());},
				key
		);
	}

	@Test
	public void test_StockPriceMinuteService_walkMixed() {
		LocalDate start = new LocalDate(2016, 9, 1);
		LocalDate end = new LocalDate(2016, 9, 2);
		String key = "00914";
		String key1 = "000568";
		stockPriceMinuteService.walk(start,
				end,
				false,
				(date, data) -> {
					System.out.print(date);
					System.out.print("\tex:\t" + data.get("HKD").getAmount());
					System.out.print("\tClose:\t" + data.get(key1).isTraded() + "\t"+data.get(key1).close());
					System.out.println("\tClose:\t" + data.get(key).isTraded() + "\t"+data.get(key).close());},
				key1, key
		);
	}

	/**
	 * http://stock.finance.sina.com.cn/hkstock/dividends/00914.html
	 */
	@Test
	public void test_DividendService_addDividend () {
		// 海螺水泥
//		ds.addDividend("00914", "2005-12-31", 0.07f, 0f, "2006-05-15");
//		ds.addDividend("00914", "2008-12-31", 0.3f, 0f, "2009-05-04");
//		ds.addDividend("00914", "2009-12-31", 0.35f, 1f, "2010-04-30");
//		ds.addDividend("00914", "2010-12-31", 0.3f, 0.5f, "2011-04-28");
//		ds.addDividend("00914", "2011-12-31", 0.35f, 0f, "2012-06-07");
//		ds.addDividend("00914", "2012-12-31", 0.25f, 0f, "2013-05-30");
//		ds.addDividend("00914", "2013-12-31", 0.35f, 0f, "2014-05-30");
//		ds.addDividend("00914", "2014-12-31", 0.65f, 0f, "2015-06-04");
//		ds.addDividend("00914", "2015-12-31", 0.43f, 0f, "2016-06-06");

		// 福耀玻璃
//		ds.addDividend("03606", "2015-12-31", 0.75f, 0f, "2016-06-07");

		// 潍柴动力
//		ds.addDividend("02338", "2011-12-31", 0.1f, 0.2f, "2012-08-02");
//		ds.addDividend("02338", "2012-06-30", 0.1f, 0f, "2012-09-17");
//		ds.addDividend("02338", "2012-12-31", 0.23f, 0f, "2013-07-25");
//		ds.addDividend("02338", "2013-06-30", 0.1f, 0f, "2013-09-18");
//		ds.addDividend("02338", "2013-12-31", 0.15f, 0f, "2014-07-30");
//		ds.addDividend("02338", "2014-06-30", 0.1f, 0f, "2014-09-29");
//		ds.addDividend("02338", "2014-12-31", 0.15f, 1f, "2015-07-16");
//		ds.addDividend("02338", "2015-06-30", 0.1f, 0f, "2015-09-21");
//		ds.addDividend("02338", "2015-12-31", 0.1f, 0f, "2016-07-04");
//		ds.addDividend("02338", "2016-06-30", 0.1f, 0f, "2016-09-21");

		// 中国平安
//		ds.addDividend("02318", "2016-06-30", 0.2f, 0f, "2016-09-05");
//		ds.addDividend("02318", "2015-12-31", 0.35f, 0f, "2016-07-05");
//		ds.addDividend("02318", "2015-06-30", 0.18f, 0f, "2015-09-07");
//		ds.addDividend("02318", "2014-12-31", 0.5f, 0f, "2015-07-27");
//		ds.addDividend("02318", "2014-06-30", 0.25f, 0f, "2014-09-10");
//		ds.addDividend("02318", "2013-12-31", 0.45f, 0f, "2014-06-25");
//		ds.addDividend("02318", "2013-06-30", 0.2f, 0f, "2013-09-11");
//		ds.addDividend("02318", "2012-12-31", 0.3f, 0f, "2013-05-14");
//		ds.addDividend("02318", "2012-06-30", 0.15f, 0f, "2012-09-21");
//		ds.addDividend("02318", "2011-12-31", 0.25f, 0f, "2012-07-04");
//		ds.addDividend("02318", "2011-06-30", 0.15f, 0f, "2011-08-31");
//		ds.addDividend("02318", "2010-12-31", 0.4f, 0f, "2011-05-13");
//		ds.addDividend("02318", "2010-06-30", 0.15f, 0f, "2010-09-07");
//		ds.addDividend("02318", "2009-12-31", 0.3f, 0f, "2010-05-26");
//		ds.addDividend("02318", "2009-06-30", 0.15f, 0f, "2009-08-28");
//		ds.addDividend("02318", "2008-06-30", 0.2f, 0f, "2008-09-18");
//		ds.addDividend("02318", "2007-12-31", 0.5f, 0f, "2008-04-10");
//		ds.addDividend("02318", "2007-06-30", 0.2f, 0f, "2007-08-28");

		// 宁沪高速
//		ds.addDividend("00177", "2015-12-31", 0.4f, 0f, "2016-06-06");
//		ds.addDividend("00177", "2014-12-31", 0.38f, 0f, "2015-06-25");
//		ds.addDividend("00177", "2013-12-31", 0.38f, 0f, "2014-06-10");
//		ds.addDividend("00177", "2012-12-31", 0.36f, 0f, "2013-06-11");
//		ds.addDividend("00177", "2011-12-31", 0.36f, 0f, "2012-06-21");
//		ds.addDividend("00177", "2010-12-31", 0.36f, 0f, "2011-04-07");
//		ds.addDividend("00177", "2009-12-31", 0.31f, 0f, "2010-04-15");
//		ds.addDividend("00177", "2008-12-31", 0.27f, 0f, "2009-05-14");
//		ds.addDividend("00177", "2007-12-31", 0.27f, 0f, "2008-05-05");

		// 中国石化
//		ds.addDividend("00386", "2016-06-30",0.079f,0f,"2016-09-09");
//		ds.addDividend("00386", "2015-12-31",0.06f,0f,"2016-06-15");
//		ds.addDividend("00386", "2015-06-30",0.09f,0f,"2015-09-14");
//		ds.addDividend("00386", "2014-12-31",0.11f,0f,"2015-06-10");
//		ds.addDividend("00386", "2014-06-30",0.09f,0f,"2014-09-15");
//		ds.addDividend("00386", "2013-12-31",0.15f,0f,"2014-05-22");
//		ds.addDividend("00386", "2013-06-30",0.09f,0f,"2013-09-05");
//		ds.addDividend("00386", "2012-12-31",0.2f,0.3f,"2013-06-10");
//		ds.addDividend("00386", "2012-06-30",0.1f,0f,"2012-09-06");
//		ds.addDividend("00386", "2011-12-31",0.2f,0f,"2012-05-17");
//		ds.addDividend("00386", "2011-06-30",0.1f,0f,"2011-09-08");
//		ds.addDividend("00386", "2010-12-31",0.13f,0f,"2011-06-09");
//		ds.addDividend("00386", "2010-06-30",0.08f,0f,"2010-09-02");
//		ds.addDividend("00386", "2009-12-31",0.11f,0f,"2010-06-03");
//		ds.addDividend("00386", "2009-06-30",0.07f,0f,"2009-09-11");
//		ds.addDividend("00386", "2008-12-31",0.09f,0f,"2009-06-04");
//		ds.addDividend("00386", "2008-06-30",0.03f,0f,"2008-09-11");
//		ds.addDividend("00386", "2007-12-31",0.115f,0f,"2008-06-05");
//		ds.addDividend("00386", "2006-12-31",0.04f,0f,"2006-09-11");
//		ds.addDividend("00386", "2005-12-31",0.04f,0f,"2005-09-12");

		// 长城汽车
		ds.addDividend("02333", "2015-12-31", 0.19f, 0f, "2016-05-23");
		ds.addDividend("02333", "2015-06-30", 0.25f, 2f, "2015-09-30");
		ds.addDividend("02333", "2014-12-31", 0.8f, 0f, "2015-05-14");
		ds.addDividend("02333", "2013-12-31", 0.82f, 0f, "2014-05-13");
		ds.addDividend("02333", "2012-12-31", 0.57f, 0f, "2013-05-14");
		ds.addDividend("02333", "2011-12-31", 0.3f, 0f, "2012-05-09");
		ds.addDividend("02333", "2010-12-31", 0.2f, 0f, "2011-03-28");
		ds.addDividend("02333", "2009-12-31", 0.25f, 0f, "2010-04-15");
		ds.addDividend("02333", "2008-12-31", 0.15f, 0f, "2009-05-04");

//		ds.addDividend("", "", f, f, "");
//		ds.addDividend("", "", f, f, "");
//		ds.addDividend("", "", f, f, "");
	}

	@Test
	public void test_PairStrategyService_cointcorrel () {
//		double [][] result = pss.cointcorrel(
//				DateTime.parse("2016-04-03").withTimeAtStartOfDay(),
//				DateTime.parse("2016-06-03").withTimeAtStartOfDay().plusHours(23),
//				new String [][] {{"160706","510300"}}, OPENCL_REGRESSION_SIZE, null, spms, null, true);

		double [][] result = pairStrategyService.cointcorrel(
				DateTime.parse("2015-09-29").withTimeAtStartOfDay(),
				DateTime.parse("2015-10-13").withTimeAtStartOfDay().plusHours(23),
				new String [][] {{"02333","601633"}}, OPENCL_REGRESSION_SIZE, null, stockPriceMinuteService, null, true);

//		double [][] result = pairStrategyService.cointcorrel(
//				DateTime.parse("2016-10-16").withTimeAtStartOfDay(),
//				DateTime.parse("2016-11-01").withTimeAtStartOfDay().plusHours(23),
//				new String [][] {{"03968","600036"}}, OPENCL_REGRESSION_SIZE, null, stockPriceMinuteService, null, true);

//		double [][] result = pairStrategyService.cointcorrel(
//				DateTime.parse("2016-10-16").withTimeAtStartOfDay(),
//				DateTime.parse("2016-11-01").withTimeAtStartOfDay().plusHours(23),
//				new String [][] {{"00939","601939"}}, OPENCL_REGRESSION_SIZE, null, stockPriceMinuteService, null, true);

//		double [][] result = pairStrategyService.cointcorrel(
//				DateTime.parse("2016-10-16").withTimeAtStartOfDay(),
//				DateTime.parse("2016-11-01").withTimeAtStartOfDay().plusHours(23),
//				new String [][] {{"02318","601318"}}, OPENCL_REGRESSION_SIZE, null, stockPriceMinuteService, null, true);

//		double [][] result = pairStrategyService.cointcorrel(
//				DateTime.parse("2016-10-16").withTimeAtStartOfDay(),
//				DateTime.parse("2016-11-01").withTimeAtStartOfDay().plusHours(23),
//				new String [][] {{"00177","600377"}}, OPENCL_REGRESSION_SIZE, null, stockPriceMinuteService, null, true);

//		double [][] result = pairStrategyService.cointcorrel(
//				DateTime.parse("2016-10-16").withTimeAtStartOfDay(),
//				DateTime.parse("2016-11-01").withTimeAtStartOfDay().plusHours(23),
//				new String [][] {{"02338","000338"}}, OPENCL_REGRESSION_SIZE, null, stockPriceMinuteService, null, true);

//		double [][] result = pairStrategyService.cointcorrel(
//				DateTime.parse("2016-09-01").withTimeAtStartOfDay(),
//				DateTime.parse("2016-11-01").withTimeAtStartOfDay().plusHours(23),
//				new String [][] {{"03606","600660"}}, OPENCL_REGRESSION_SIZE, null, stockPriceMinuteService, null, true);

//		double [][] result = pairStrategyService.cointcorrel(
//				DateTime.parse("2016-09-01").withTimeAtStartOfDay(),
//				DateTime.parse("2016-11-01").withTimeAtStartOfDay().plusHours(23),
//				new String [][] {{"00914","600585"}}, OPENCL_REGRESSION_SIZE, null, stockPriceMinuteService, null, true);

		// slope, std, correl, adf_p
		System.out.printf("slope: %f, std: %f, correl: %f, adf_p: %.2f\n", result[0][0], result[0][1], result[0][2], result[0][3]);
	}

	@Test
	public void test_PairStrategyService_inspectPair() {
//		pairStrategyService.inspectPairByMinute("00914", "600585", "2016-11-02", "2016-12-02", 0.900608);
//		pairStrategyService.inspectPairByMinute("03606", "600660", "2016-11-02", "2016-12-02", 0.900514);
//		pairStrategyService.inspectPairByMinute("02338", "000338", "2016-11-02", "2016-12-02", 0.956049);
//		pairStrategyService.inspectPairByMinute("00177", "600377", "2016-11-02", "2016-12-02", 0.973196);
//		pairStrategyService.inspectPairByMinute("02318", "601318", "2016-11-02", "2016-12-02", 0.965983);
//		pairStrategyService.inspectPairByMinute("00939", "601939", "2016-10-16", "2016-12-02", 1.047381);
//		pairStrategyService.inspectPairByMinute("03968", "600036", "2016-10-16", "2016-12-02", 1.047381);
		pairStrategyService.inspectPairByMinute("01398", "601398", "2016-07-11", "2016-12-02", 1.148928);
	}

	@Test
	public void test_PairStrategyService_inspect() {

//		// 海螺水泥
//		String codeHK = "00914";
//		String codeA = "600585";

//		// 福耀玻璃
//		String codeHK = "03606";
//		String codeA = "600660";

		// 宁沪高速
//		String codeHK = "00177";
//		String codeA = "600377";

//		// 海通证券
//		String codeHK = "06837";
//		String codeA = "600837";

		// 光大银行
//		String codeHK = "06818";
//		String codeA = "601818";

		// 中国石化
//		String codeHK = "00386";
//		String codeA = "600028";

		// 潍柴动力
//		String codeHK = "02338";
//		String codeA = "000338";

		// 中国平安
//		String codeHK = "02318";
//		String codeA = "601318";

		// 工商银行
//		String codeHK = "01398";
//		String codeA = "601398";

		// 建设银行
//		String codeHK = "00939";
//		String codeA = "601939";

		// 中国神华
		// 中国神华
//		String codeHK = "01088";
//		String codeA = "601088";

		// 招商银行
//		String codeHK = "03968";
//		String codeA = "600036";

//		// 青岛啤酒
//		String codeHK = "00168";
//		String codeA = "600600";

		// 复星医药
//		String codeHK = "02196";
//		String codeA = "600196";

		// 长城汽车
//		String codeHK = "02333";
//		String codeA = "601633";

		// 长城汽车
		String codeHK = "00358";
		String codeA = "600362";

		double [][] result = pairStrategyService.cointcorrel(
				DateTime.parse("2016-09-18").withTimeAtStartOfDay(),
				DateTime.parse("2016-12-08").withTimeAtStartOfDay().plusHours(23),
				new String [][] {{codeHK,codeA}}, OPENCL_REGRESSION_SIZE, null, stockPriceMinuteService, null, true);
//		pairStrategyService.inspectPairByMinute(codeHK, codeA, "2016-09-17", "2016-12-25", result[0][0]);
		pairStrategyService.inspectPairByDay(codeHK, codeA, "2014-11-25", "2016-12-25", result[0][0]);

//		String codeHK = "00914";
//		String codeA = "600585";
//		double [][] result = pairStrategyService.cointcorrel(
//				DateTime.parse("2016-11-17").withTimeAtStartOfDay(),
//				DateTime.parse("2016-12-02").withTimeAtStartOfDay().plusHours(23),
//				new String [][] {{codeHK,codeA}}, OPENCL_REGRESSION_SIZE, null, stockPriceMinuteService, null, true);
//		pairStrategyService.inspectPairByMinute(codeHK, codeA, "2016-11-17", "2016-12-02", result[0][0]);

//		String codeHK = "02822";
//		String codeA = "510050";
//		double [][] result = pairStrategyService.cointcorrel(
//				DateTime.parse("2016-07-11").withTimeAtStartOfDay(),
//				DateTime.parse("2016-07-26").withTimeAtStartOfDay().plusHours(23),
//				new String [][] {{codeHK,codeA}}, OPENCL_REGRESSION_SIZE, null, stockPriceMinuteService, null, true);
//		pairStrategyService.inspectPairByMinute(codeHK, codeA, "2016-07-11", "2016-12-02", result[0][0]);
	}

	@Test
	public void test_AsyncMailer_mail() {
		AsyncMailer asyncMailer = new AsyncMailer();
		asyncMailer.email("Try mail", "This is another test!!!!", "8122850@qq.com", "michael.tang@anz.com");
	}

	@Test
	public void test_ShortInHKPairStrategy_emailNotification () {
		shortInHkPairStrategyRepo.findByOpenableDate(LocalDate.now().minusDays(0).toDate()).forEach(strategy -> {
			strategy.emailNotification("Test");
		});
//		AsyncMailer.instance().stop();
	}

	@Test
	public void test_GridStrategy_emailNotification () {
		List<GridStrategy> strategies = gridStrategyRepo.findByActive(true);
		strategies.get(0).emailNotification("Test 1");
		strategies.get(1).emailNotification("Test 2");
		try {
			Thread.sleep(300000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		AsyncMailer.instance().stop();
	}

	@Test
	public void test_Repo () {
		Stock s = new Stock("000123");
		System.out.println(s.getId());
		stockRepo.save(s);
		System.out.println(s.getId());
	}
}