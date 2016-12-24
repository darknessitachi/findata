package michael.findata.algoquant.strategy.pair;

import com.numericalmethod.algoquant.execution.component.simulator.SimpleSimulatorBuilder;
import com.numericalmethod.algoquant.execution.component.simulator.Simulator;
import com.numericalmethod.algoquant.execution.component.simulator.market.limitorder.AlwaysFillModel;
import com.numericalmethod.algoquant.execution.component.tradeblotter.TradeBlotter;
import com.numericalmethod.algoquant.execution.datatype.depth.cache.DepthCaches;
import com.numericalmethod.algoquant.execution.datatype.execution.Execution;
import com.numericalmethod.algoquant.execution.datatype.fxrate.ExchangeRateTable;
import com.numericalmethod.algoquant.execution.datatype.fxrate.SimpleExchangeRateTable;
import com.numericalmethod.algoquant.execution.datatype.product.fx.Currencies;
import com.numericalmethod.algoquant.execution.datatype.product.portfolio.Portfolio;
import com.numericalmethod.algoquant.execution.performance.measure.PerformanceMeasure;
import com.numericalmethod.algoquant.execution.performance.measure.ProfitLoss;
import com.numericalmethod.algoquant.execution.performance.measure.ir.InformationRatioForTrades;
import com.numericalmethod.algoquant.execution.performance.measure.ir.InformationRatioForZeroInvestment;
import com.numericalmethod.algoquant.execution.performance.measure.omega.OmegaForPeriods;
import com.numericalmethod.algoquant.execution.performance.report.PerformanceReport;
import com.numericalmethod.algoquant.execution.performance.report.analyzer.PerformanceAnalyzer;
import com.numericalmethod.algoquant.execution.performance.report.analyzer.SimplePerformanceAnalyzer;
import com.numericalmethod.algoquant.execution.strategy.Strategy;
import com.numericalmethod.algoquant.model.util.returns.ReturnsCalculators;
import michael.findata.data.local.LocalMinuteDepthCacheFactory;
import michael.findata.model.Stock;
import michael.findata.service.DividendService;
import michael.findata.service.StockService;
import michael.findata.spring.data.repository.PairInstanceRepository;
import michael.findata.spring.data.repository.PairStatsRepository;
import michael.findata.spring.data.repository.StockRepository;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static michael.findata.util.LogUtil.getClassLogger;
import static michael.findata.algoquant.execution.datatype.order.HexinOrder.HexinType.CREDIT_BUY;
import static michael.findata.algoquant.execution.datatype.order.HexinOrder.HexinType.CREDIT_SELL;

public class HoppingStrategyBackTest {
	private static final Logger logger = getClassLogger();
	private final Strategy strategy;
	private final Interval interval;
	private final Collection<? extends Stock> stocks;
	private final ExchangeRateTable rates = new SimpleExchangeRateTable(Currencies.CNY);
	private LocalMinuteDepthCacheFactory cacheFactory;

	HoppingStrategyBackTest(Collection<? extends Stock> stocks, Strategy strategy, Interval interval) {
		this.stocks = stocks;
		this.strategy = strategy;
		this.interval = interval;
	}

	public void run() {
		// TODO: 9/17/2016 something is wrong, depth caches were created successfully, but it seems that the depth update methods were not called.
//		LocalMinuteDepthCacheFactory cacheFactory = new LocalMinuteDepthCacheFactory();
		DepthCaches depthCaches = new DepthCaches();
		stocks.forEach(stock -> depthCaches.addCache(stock, cacheFactory.newInstance(stock, interval)));
//		tdx.newInstance(stocks)
//		tdx.newInstances(stocks, interval).entrySet().forEach(entry ->{
//			depthCaches.addCache(entry.getKey(), entry.getValue());
//		});

		// set up a simulator to host the strategy
		Simulator simulator = new SimpleSimulatorBuilder()
				.withDepthUpdates(depthCaches)
				.synchronizeDepthUpdates()
				.useLimitOrderExecutionModel(new AlwaysFillModel())
//				.useStrategyPlotter(new SimpleStrategyPlotter("Test"))
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
		measures.add(new InformationRatioForTrades());
//		measures.add(new InformationRatioForPeriods());
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
		StockRepository stockRepo = (StockRepository) context.getBean("stockRepository");
		PairStatsRepository pairStatsRepo = (PairStatsRepository) context.getBean("pairStatsRepository");
		PairInstanceRepository pairInstanceRepo = (PairInstanceRepository) context.getBean("pairInstanceRepository");
		DividendService ds = (DividendService) context.getBean("dividendService");
		StockService ss = (StockService) context.getBean("stockService");
		LocalMinuteDepthCacheFactory tdx = (LocalMinuteDepthCacheFactory) context.getBean("localMinuteDepthCacheFactory");

		Portfolio<Stock> portfolio = new Portfolio<>();
		portfolio.putIfAbsent(stockRepo.findOneByCode("160706"), 36000d*2);
		portfolio.putIfAbsent(stockRepo.findOneByCode("510300"), 10000d*2);
//		portfolio.putIfAbsent(stockRepo.findOneByCode("510310"), 25000d);
//		portfolio.putIfAbsent(stockRepo.findOneByCode("510330"), 10000d);
//		portfolio.putIfAbsent(stockRepo.findOneByCode("510360"), 33000d);
		portfolio.putIfAbsent(stockRepo.findOneByCode("159919"), 9000d*2);

		Set<String> scope = ss.getStockGroup("michael/findata/algoquant/strategy/pair/hopping_strategy_scope.csv");

		int numRun = 1;
		int daysPerRun = 150;
		// insert strategies
		DateTime start = DateTime.parse("2016-05-28");
		DateTime end;

		for (int i = 0; i < numRun; i++) {
			end = start.plusDays(daysPerRun - 1).plusHours(23);
			Interval interval = new Interval(start, end);
			HoppingStrategy hoppingStrategy = new HoppingStrategy(
					scope, portfolio, start.toLocalDate(),
					CREDIT_SELL,
					CREDIT_BUY,
					pairStatsRepo, pairInstanceRepo, ds);
			HoppingStrategyBackTest demo = new HoppingStrategyBackTest(stockRepo.findByCodeIn(scope), hoppingStrategy, interval);
			demo.cacheFactory = tdx;
			demo.run();

			portfolio = hoppingStrategy.getPortfolio();
			System.out.println(portfolio);
			start = start.plusDays(daysPerRun);
		}
	}
}