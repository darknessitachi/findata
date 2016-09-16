package michael.findata.algoquant.strategy.grid;

import com.numericalmethod.algoquant.data.cache.SequentialCache;
import com.numericalmethod.algoquant.execution.component.simulator.SimpleSimulatorBuilder;
import com.numericalmethod.algoquant.execution.component.simulator.Simulator;
import com.numericalmethod.algoquant.execution.component.simulator.market.limitorder.AlwaysFillModel;
import com.numericalmethod.algoquant.execution.component.tradeblotter.TradeBlotter;
import com.numericalmethod.algoquant.execution.datatype.depth.cache.DepthCaches;
import com.numericalmethod.algoquant.execution.datatype.execution.Execution;
import com.numericalmethod.algoquant.execution.datatype.fxrate.ExchangeRateTable;
import com.numericalmethod.algoquant.execution.datatype.fxrate.SimpleExchangeRateTable;
import com.numericalmethod.algoquant.execution.datatype.product.fx.Currencies;
import com.numericalmethod.algoquant.execution.performance.measure.PerformanceMeasure;
import com.numericalmethod.algoquant.execution.performance.measure.ProfitLoss;
import com.numericalmethod.algoquant.execution.performance.measure.ir.InformationRatioForZeroInvestment;
import com.numericalmethod.algoquant.execution.performance.measure.omega.OmegaForPeriods;
import com.numericalmethod.algoquant.execution.performance.report.PerformanceReport;
import com.numericalmethod.algoquant.execution.performance.report.analyzer.PerformanceAnalyzer;
import com.numericalmethod.algoquant.execution.performance.report.analyzer.SimplePerformanceAnalyzer;
import com.numericalmethod.algoquant.execution.strategy.Strategy;
import com.numericalmethod.algoquant.model.util.returns.ReturnsCalculators;
import michael.findata.algoquant.strategy.DummyStrategy;
import michael.findata.data.historicaldata.tdx.TdxMinuteDepthCacheFactory;
import michael.findata.data.local.LocalDividendCacheFactory;
import michael.findata.data.local.LocalMinuteDepthCacheFactory;
import michael.findata.model.Dividend;
import michael.findata.model.Stock;
import michael.findata.service.DividendService;
import michael.findata.service.StockService;
import michael.findata.spring.data.repository.StockRepository;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.*;

import static com.numericalmethod.nmutils.NMUtils.getClassLogger;

public class GridStrategyBackTest {
	private static final Logger logger = getClassLogger();
	private final Strategy strategy;
	private final Interval interval;
	private final Stock stock;
	private final ExchangeRateTable rates = new SimpleExchangeRateTable(Currencies.CNY);
	private final ApplicationContext appContext;

	public GridStrategyBackTest(Strategy strategy, Interval interval, ApplicationContext appContext, Stock stock) {
		this.stock = stock;
		this.strategy = strategy;
		this.interval = interval;
		this.appContext = appContext;
	}

	public void run() {
		LocalMinuteDepthCacheFactory localDCF = (LocalMinuteDepthCacheFactory) appContext.getBean("localMinuteDepthCacheFactory");
		LocalDividendCacheFactory dividendCacheFactory = (LocalDividendCacheFactory) appContext.getBean("localDividendCacheFactory");

		DepthCaches depthCaches = new DepthCaches();
		depthCaches.addCache(stock, localDCF.newInstance(stock, interval));
		SequentialCache<Dividend> dividendCache = dividendCacheFactory.newInstance(stock, interval);

		Simulator simulator = new SimpleSimulatorBuilder()
				.withDepthUpdates(depthCaches)
				.withNonDepthUpdates(dividendCache)
//				.withNonDepthUpdates(dividendCacheFactory.newInstance(new Stock("600000"), interval))
//				.synchronizeDepthUpdates()
				.useLimitOrderExecutionModel(new AlwaysFillModel())
				.build();

		// run the strategy
		TradeBlotter tradeBlotter = simulator.run(strategy);

		// execution summary
		List<Execution> allExecutions = tradeBlotter.allExecutions();
		// logger.info(String.format("%d executions: %s", allExecutions.size(), allExecutions));

		// performance analysis
		Collection<PerformanceMeasure> measures = new ArrayList<>(3);
		measures.add(new ProfitLoss());
		measures.add(new InformationRatioForZeroInvestment(Period.years(1), 0.));
		measures.add(new OmegaForPeriods(0,
				Period.years(1),
				ReturnsCalculators.ABSOLUTE,
				0.));
		PerformanceAnalyzer analyzer = new SimplePerformanceAnalyzer(measures);
		PerformanceReport report = analyzer.analyze(tradeBlotter, depthCaches, rates);
		logger.info(String.format("performance report:%n%s", report.toString()));
	}

	public static void main(String[] args) throws IOException {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		Interval interval = Interval.parse("2015-12-29T09:10:00/2016-09-17T14:30:00");

		// TODO: 9/14/2016 calculate storage for each of the following:
		String code = "600266"; // 12000 / 9986 : Data not enough 北京城建

//		String code = "002328"; // 12000 / 9986 :
//		String code = "601088"; // 12000 / 9986 : Data not enough
//		String code = "000719"; // 12000 / 9986 : -
//		String code = "600028"; // 12000 / 9986 : 1888.00
//		String code = "002419"; // 12000 / 9986 : 1888.00

		Stock s = new Stock(code);
		GridStrategyBackTest demo = new GridStrategyBackTest(
				new GridStrategy(new GridStrategy.Param(
						12000,
						9986,
						0.0035
				), s),
				interval,
				context,
				s
		);
		demo.run();
	}
}