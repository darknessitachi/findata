package michael.findata.external.tdx.test;

import com.numericalmethod.algoquant.data.cache.SequentialCache;
import michael.findata.algoquant.execution.component.broker.LocalTdxBrokerProxy;
import michael.findata.algoquant.execution.datatype.StockEOM;
import michael.findata.data.local.LocalEOMCacheFactory;
import michael.findata.external.SecurityTimeSeriesData;
import michael.findata.external.SecurityTimeSeriesDatum;
import michael.findata.external.tdx.TDXClient;
import michael.findata.model.Stock;
import michael.findata.model.StockPriceMinute;
import michael.findata.service.DividendService;
import michael.findata.service.StockPriceMinuteService;
import michael.findata.service.StockService;
import org.apache.xerces.impl.xs.SchemaSymbols;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.*;

import static com.numericalmethod.nmutils.NMUtils.getClassLogger;

@Test
@ContextConfiguration(locations = { "classpath:michael/findata/pair_spring.xml" })
public class FindataTests extends AbstractTestNGSpringContextTests {

	private static final Logger LOGGER = getClassLogger();

	@Autowired
	private StockService ss;

	@Autowired
	private DividendService ds;

	@Autowired
	private LocalEOMCacheFactory localEOMCacheFactory;

	@Autowired
	private StockPriceMinuteService stockPriceMinuteService;

	@Autowired
	private TDXClient client;

	private long timestamp;

	// Run once, e.g. Database connection, connection pool
	@BeforeClass
	public void onceBeforeClass() {
		LOGGER.info("@BeforeClass - runOnceBefore tests in "+FindataTests.class);
		//安装华西证券，招商证券，国金证券申银万国等证券公司的通达信软件，
		//找到里面的connect.cfg文件，可以找出最新的服务器列表，其中华西
		//证券的服务器最多。
//		client = new TDXClient(
//				"221.237.158.106:7709",    // 西南证券金点子成都电信主站1
//				"221.237.158.107:7709",    // 西南证券金点子成都电信主站2
//				"221.237.158.108:7709"    // 西南证券金点子成都电信主站3
//		);
		client.connect();
	}

	// Run once, e.g close connection, cleanup
	@AfterClass
	public void onceAfterClass() {
		LOGGER.info("@AfterClass - runOnceAfter tests in "+FindataTests.class);
		client.disconnect();
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
	public void test_TDXClient_getEOMs() {
		LOGGER.info("@Test");
		SecurityTimeSeriesData data = client.getEOMs("002799", (short) 18, (short) 3);
		SecurityTimeSeriesDatum minute;
		while (data.hasNext()) {
			minute = data.popNext();
			System.out.println("Time:\t"+minute.getDateTime());
			System.out.println("Open:\t"+minute.getOpen());
			System.out.println("High:\t"+minute.getHigh());
			System.out.println("Low:\t"+minute.getLow());
			System.out.println("Close:\t"+minute.getClose());
			System.out.println("Amt:\t"+minute.getAmount());
			System.out.println("Vol:\t"+minute.getVolume());
		}
	}

	@Test
	public void test_TDXClient_getXDXRInfo() {
		LOGGER.info("@Test");
		System.out.println("市场\t证券代码\t日期\t保留\t送现金\t配股价\t送股数\t配股比例");
		client.getXDXRInfo("159901", "510300", "502011", "600381").forEach(strings -> {
			for (String string : strings) {
				System.out.print(string);
				System.out.print("\t");
			}
			System.out.println();
		});
	}

	@Test
	public void test_DividendService_refreshDividendDataForFund() {
		ds.refreshDividendDataForFund("200054", client);
	}

	@Test
	public void test_DividendService_calculateAdjFactorForStock() {
		ds.calculateAdjFactorForStock("200054");
		ds.calculateAdjFactorForStock("600381");
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
	public void test_StockPriceMinuteService () {
		LocalDate start = new LocalDate(2016, 8, 4);
		LocalDate end = new LocalDate(2016, 8, 5);
		DividendService.PriceAdjuster pa = ds.newPriceAdjuster(start, end, "601009", "000568", "510300");
		stockPriceMinuteService.walk(start,
				end,
				false,
				(date, data) -> {
					System.out.print(date);
					System.out.print("\tAdjusted:\t" + pa.adjust("601009", start, date.toLocalDate(), data.get("601009").getClose()/1000d));
					System.out.print("\tAdjusted:\t" + pa.adjust("000568", start, date.toLocalDate(), data.get("000568").getClose()/1000d));
					System.out.println("\tAdjusted:\t" + pa.adjust("510300", start, date.toLocalDate(), data.get("510300").getClose()/1000d));},
				"601009", "000568", "510300"
		);
	}
}