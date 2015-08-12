import com.numericalmethod.algoquant.data.cache.SequentialCache;
import com.numericalmethod.algoquant.data.cache.processor.sampler.EquiTimeSampler;
import com.numericalmethod.algoquant.data.historicaldata.yahoo.YahooDepthCacheFactory;
import com.numericalmethod.algoquant.execution.component.chart.plotter.SimpleStrategyPlotter;
import com.numericalmethod.algoquant.execution.component.simulator.SimpleSimulatorBuilder;
import com.numericalmethod.algoquant.execution.component.simulator.Simulator;
import com.numericalmethod.algoquant.execution.component.tradeblotter.TradeBlotter;
import com.numericalmethod.algoquant.execution.datatype.depth.Depth;
import com.numericalmethod.algoquant.execution.datatype.depth.cache.DepthCaches;
import com.numericalmethod.algoquant.execution.datatype.execution.Execution;
import com.numericalmethod.algoquant.execution.datatype.fxrate.ExchangeRateTable;
import com.numericalmethod.algoquant.execution.datatype.fxrate.SimpleExchangeRateTable;
import com.numericalmethod.algoquant.execution.datatype.product.fx.Currencies;
import com.numericalmethod.algoquant.execution.datatype.product.stock.Stock;
import com.numericalmethod.algoquant.execution.datatype.product.stock.hkex.HSI;
import com.numericalmethod.algoquant.execution.performance.measure.ProfitLoss;
import com.numericalmethod.algoquant.execution.performance.measure.ir.InformationRatioForPeriods;
import com.numericalmethod.algoquant.execution.performance.measure.omega.OmegaBySummation;
import com.numericalmethod.algoquant.execution.performance.measure.omega.OmegaForPeriods;
import com.numericalmethod.algoquant.execution.simulation.template.SimTemplateYahooEOD;
import com.numericalmethod.algoquant.execution.strategy.Strategy;
import com.numericalmethod.algoquant.execution.strategy.demo.TutorialStrategy;
import com.numericalmethod.algoquant.model.util.returns.ReturnCalculators;
import com.numericalmethod.suanshu.misc.datastructure.time.JodaTimeUtils;
import michael.findata.algoquant.product.stock.shse.SHSEStock;
import michael.findata.algoquant.product.stock.szse.SZSEStock;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.slf4j.Logger;

import java.util.List;

import static com.numericalmethod.nmutils.NMUtils.getClassLogger;

/**
 * Author: Michael, Tang Ying Jian
 * Date: 2015/8/9
 */
public class AlgoQuantTest {

	private static final Logger LOGGER = getClassLogger();

	public static void main(String[] args) throws Exception {
		AlgoQuantTest demo = new AlgoQuantTest();
		demo.run();
	}

	public void run() throws Exception {
		// set up the product
//		final Stock stock = HSI.getInstance();
		final Stock stock = new SZSEStock("000568.SZ");

		// specify the simulation period
		DateTime begin = JodaTimeUtils.getDate(2001, 1, 1, stock.exchange().timeZone());
		DateTime end = JodaTimeUtils.getDate(2015, 8, 11, stock.exchange().timeZone());
		Interval interval = new Interval(begin, end);

		// set up the data source; we download data from Yahoo! Finance here.
		YahooDepthCacheFactory yahoo = new YahooDepthCacheFactory(SimTemplateYahooEOD.DEFAULT_DATA_FOLDER);
		SequentialCache<Depth> dailyData = yahoo.newInstance(stock, interval);

		// clean the data using filters; we simulate using only monthly data.
		EquiTimeSampler<Depth> monthlySampler = new EquiTimeSampler<>(Period.months(1));
		SequentialCache<Depth> monthlyData = monthlySampler.process(dailyData);

		// set up the data source to feed into the simulator
		DepthCaches depthCaches = new DepthCaches(stock, monthlyData);

		// construct an instance of the strategy to simulate
		Strategy strategy = new TutorialStrategy(stock);

		// set up a simulator to host the strategy
		Simulator simulator = new SimpleSimulatorBuilder()
				.withDepthUpdates(depthCaches)
				.useStrategyPlotter(new SimpleStrategyPlotter("tutorial on " + stock.symbol()))
				.build();
		// here is where the actual simulation happens
		TradeBlotter tradeBlotter = simulator.run(strategy);

		// collect all trades that happen during the simulation for post-trading analysis
		List<Execution> executions = tradeBlotter.allExecutions();

		// compute the P&L
		ExchangeRateTable rates = new SimpleExchangeRateTable(Currencies.CNY);
		double pnl = new ProfitLoss().valueOf(executions, depthCaches, rates);

		// compute the information ratio/Sharpe ratio
		double initialCapital = dailyData.iterator().next().data().mid();
		double benchmarkPeriodReturn = 0.0;
		double ir = new InformationRatioForPeriods(initialCapital,
				interval,
				Period.years(1),
				ReturnCalculators.SIMPLE,
				benchmarkPeriodReturn
		).valueOf(executions, depthCaches, rates);

		// compute the Omega
		double lossThreshold = 0.0;
		double omega0 = new OmegaForPeriods(initialCapital,
				interval,
				Period.years(1),
				ReturnCalculators.SIMPLE,
				lossThreshold,
				new OmegaBySummation()
		).valueOf(executions, depthCaches, rates);

		// print out some stats
//		LOGGER.info("pnl = {}; ir = {}; omega(0) = {}", pnl, ir, omega0);
	}
}