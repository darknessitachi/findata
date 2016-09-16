package michael.findata.algoquant.strategy;

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
import michael.findata.data.historicaldata.tdx.TdxMinuteDepthCacheFactory;
import michael.findata.model.Stock;
import michael.findata.service.DividendService;
import michael.findata.service.StockService;
import michael.findata.spring.data.repository.PairInstanceRepository;
import michael.findata.spring.data.repository.PairStatsRepository;
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

public class DummyStrategyBackTest {
	private static final Logger logger = getClassLogger();
	private final Strategy strategy;
	private final Interval interval;
	private final Collection<? extends Stock> stocks;
	private final ExchangeRateTable rates = new SimpleExchangeRateTable(Currencies.CNY);

	public DummyStrategyBackTest (Collection<? extends Stock> stocks, Strategy strategy, Interval interval) {
		this.stocks = stocks;
		this.strategy = strategy;
		this.interval = interval;
	}

	public void run() {
		TdxMinuteDepthCacheFactory tdx = new TdxMinuteDepthCacheFactory();
		DepthCaches depthCaches = new DepthCaches();
		tdx.newInstances(stocks, interval).entrySet().forEach(entry ->{
			depthCaches.addCache(entry.getKey(), entry.getValue());
		});

		// set up a simulator to host the strategy
		Simulator simulator = new SimpleSimulatorBuilder()
				.withDepthUpdates(depthCaches)
				.synchronizeDepthUpdates()
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

	public static void main (String [] args) throws IOException {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		StockRepository stockRepo = (StockRepository) context.getBean("stockRepository");
		PairStatsRepository pairStatsRepo = (PairStatsRepository) context.getBean("pairStatsRepository");
		PairInstanceRepository pairInstanceRepo = (PairInstanceRepository) context.getBean("pairInstanceRepository");
		DividendService ds = (DividendService) context.getBean("dividendService");
		StockService ss = (StockService) context.getBean("stockService");
		Interval interval = new Interval(
				DateTime.parse("2016-03-07"),
				DateTime.parse("2016-03-14").plusHours(23));

		Set<String> scope = ss.getStockGroup("michael/findata/algoquant/strategy/pair/group_manu_3.csv");

		DummyStrategyBackTest demo = new DummyStrategyBackTest(stockRepo.findByCodeIn(scope), new DummyStrategy(), interval);
		demo.run();
	}
}