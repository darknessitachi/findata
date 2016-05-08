import com.numericalmethod.algoquant.data.historicaldata.yahoo.YahooDepthCacheFactory;
import com.numericalmethod.algoquant.execution.component.chart.plotter.SimpleStrategyPlotter;
import com.numericalmethod.algoquant.execution.component.simulator.SimpleSimulatorBuilder;
import com.numericalmethod.algoquant.execution.component.simulator.Simulator;
import com.numericalmethod.algoquant.execution.component.tradeblotter.TradeBlotter;
import com.numericalmethod.algoquant.execution.datatype.depth.cache.DepthCaches;
import com.numericalmethod.algoquant.execution.datatype.execution.Execution;
import com.numericalmethod.algoquant.execution.datatype.fxrate.ExchangeRateTable;
import com.numericalmethod.algoquant.execution.datatype.fxrate.SimpleExchangeRateTable;
import com.numericalmethod.algoquant.execution.datatype.product.Product;
import com.numericalmethod.algoquant.execution.datatype.product.fx.Currencies;
import com.numericalmethod.algoquant.execution.datatype.product.stock.Exchange;
import com.numericalmethod.algoquant.execution.datatype.product.stock.SimpleStock;
import com.numericalmethod.algoquant.execution.datatype.product.stock.Stock;
import com.numericalmethod.algoquant.execution.performance.measure.*;
import com.numericalmethod.algoquant.execution.performance.measure.ir.InformationRatioForZeroInvestment;
import com.numericalmethod.algoquant.execution.performance.measure.omega.OmegaForPeriods;
import com.numericalmethod.algoquant.execution.performance.report.PerformanceReport;
import com.numericalmethod.algoquant.execution.performance.report.analyzer.PerformanceAnalyzer;
import com.numericalmethod.algoquant.execution.performance.report.analyzer.SimplePerformanceAnalyzer;
import com.numericalmethod.algoquant.execution.simulation.template.SimTemplateYahooEOD;
import com.numericalmethod.algoquant.model.elliott2005.strategy.Elliott2005Strategy;
import com.numericalmethod.algoquant.model.elliott2005.strategy.MeanReversionAroundLongTermMeanConstantCoefficients;
import com.numericalmethod.algoquant.model.util.returns.ReturnCalculators;
import com.numericalmethod.algoquant.util.annotation.Demo;

import static com.numericalmethod.nmutils.NMUtils.*;

import com.numericalmethod.suanshu.algebra.linear.vector.doubles.dense.DenseVector;
import com.numericalmethod.suanshu.misc.datastructure.time.JodaTimeUtils;

import java.util.*;

import michael.findata.algoquant.strategy.Elliott2005;
import org.joda.time.*;
import org.slf4j.Logger;

@Demo
public class Elliott2005Demo {

	private static final Logger logger = getClassLogger();

	// parameters
	private final Interval interval = new Interval(
			JodaTimeUtils.getDate(2015, 1, 1, Exchange.SHSE.timeZone()),
			JodaTimeUtils.getDate(2015, 11, 23, Exchange.SHSE.timeZone()));
	private final ExchangeRateTable rates = new SimpleExchangeRateTable(Currencies.CNY);
	private final int calibrationWindow = 10;
//	private final Stock stock1 = new SimpleStock ("600809.SS", Currencies.CNY, Exchange.SHSE);
//	private final Stock stock2 = new SimpleStock ("000596.SZ", Currencies.CNY, Exchange.SZSE);
	private final Stock stock1 = new SimpleStock("600886.SS", Currencies.CNY, Exchange.SHSE);
	private final Stock stock2 = new SimpleStock("600674.SS", Currencies.CNY, Exchange.SHSE);

//	private final Stock stock1 = new SimpleStock("601169.SS", Currencies.CNY, Exchange.SHSE);
//	private final Stock stock2 = new SimpleStock("601166.SS", Currencies.CNY, Exchange.SHSE);

	private final List<Product> stocks = Arrays.asList(stock1, stock2);

	public void run() {
		// set up the data sources
		YahooDepthCacheFactory yahoo = new YahooDepthCacheFactory(SimTemplateYahooEOD.DEFAULT_DATA_FOLDER);

		// create a source of market depth
		DepthCaches depthCaches = new DepthCaches(yahoo, Arrays.asList(stock1, stock2), interval);

        /*
		 * uncomment the customerization that you want to simulate
         */
//        Elliott2005Strategy.Customization customization
//            = new MeanReversionAroundPosterioriStateEstimateCointegratedCoefficients(0.1, 2,// beta range
//                                                                                     0.2,// cointegration confidence
//                                                                                     0.02, 0.01); // entry/exit signals
//        Elliott2005Strategy.Customization customization
//            = new MeanReversionAroundPosterioriStateEstimateConstantCoefficients(new DenseVector(1.0, -1.0),// the spread coefficients
//                                                                                 0.03, 0.01);// entry/exit signals
		Elliott2005Strategy.Customization customization
				= new MeanReversionAroundLongTermMeanConstantCoefficients(new DenseVector(1.0, -1.0),	// the spread coefficients
																										// applying this spread on prices you get priceA-priceB
																										// applying this spread on price logs you get log(priceA/priceB),
																										// 		which after exponentials, you can restore back to priceA/priceB
				0.2, 0.1); // entry/exit signals

		// set up the strategy with the specifications and customerization
		Elliott2005 strategy = new Elliott2005(stocks, customization, calibrationWindow);
		logger.info(strategy.toString());

		// set up a simulator to host the strategy
		Simulator simulator = new SimpleSimulatorBuilder()
				.withDepthUpdates(depthCaches)
				.useStrategyPlotter(new SimpleStrategyPlotter("Elliott's 2005 Pairs Trading: "
						+ stocks.get(0).symbol() + " vs "
						+ stocks.get(1).symbol()))
				.build();

		// run the strategy
		TradeBlotter tradeBlotter = simulator.run(strategy);

		// execution summary
		List<Execution> allExecutions = tradeBlotter.allExecutions();
//		logger.info(String.format("%d executions: %s", allExecutions.size(), allExecutions));

		// performance analysis
		Collection<PerformanceMeasure> measures = new ArrayList<>(3);
		measures.add(new ProfitLoss());
		measures.add(new InformationRatioForZeroInvestment(interval, Period.years(1), 0.));
		measures.add(new OmegaForPeriods(0,
				interval,
				Period.years(1),
				ReturnCalculators.ABSOLUTE,
				0.));

		PerformanceAnalyzer analyzer = new SimplePerformanceAnalyzer(measures);
		PerformanceReport report = analyzer.analyze(tradeBlotter, depthCaches, rates);
		logger.info(String.format("performance report:%n%s", report.toString()));
	}

	public static void main(String[] args) {
		Elliott2005Demo demo = new Elliott2005Demo();
		demo.run();
	}
}