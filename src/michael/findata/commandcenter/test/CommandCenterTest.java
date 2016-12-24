package michael.findata.commandcenter.test;

import com.numericalmethod.algoquant.execution.datatype.product.portfolio.Portfolio;
import michael.findata.algoquant.execution.component.broker.LocalTdxBrokerProxy;
import michael.findata.algoquant.execution.datatype.order.HexinOrder.HexinType;
import michael.findata.algoquant.strategy.pair.HoppingStrategy;
import michael.findata.commandcenter.CommandCenter;
import michael.findata.external.tdx.TDXClient;
import michael.findata.model.Stock;
import michael.findata.service.DividendService;
import michael.findata.service.PairStrategyService;
import michael.findata.service.StockService;
import michael.findata.spring.data.repository.PairInstanceRepository;
import michael.findata.spring.data.repository.PairStatsRepository;
import michael.findata.spring.data.repository.StockRepository;
import michael.findata.util.DBUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.Set;

import static michael.findata.algoquant.execution.datatype.order.HexinOrder.HexinType.*;

// Test the CommandCenter with HoppingStrategy
public class CommandCenterTest {
	public static void main(String[] args) throws IOException {
		Process dbProcess = DBUtil.tryToStartDB();
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		CommandCenter cc = (CommandCenter) context.getBean("commandCenter");
		StockRepository stockRepo = (StockRepository) context.getBean("stockRepository");
		PairStatsRepository pairStatsRepo = (PairStatsRepository) context.getBean("pairStatsRepository");
		PairInstanceRepository pairInstanceRepo = (PairInstanceRepository) context.getBean("pairInstanceRepository");
		PairStrategyService pss = (PairStrategyService) context.getBean("pairStrategyService");
		DividendService ds = (DividendService) context.getBean("dividendService");
		StockService ss = (StockService) context.getBean("stockService");

		pss.updateDividends();

		HexinType sellOrderType = SIMPLE_SELL;
		HexinType buyOrderType = SIMPLE_BUY;

//		cc.setBroker(new LocalTdxBrokerProxy(10001));
		cc.setShSzHqClient(new TDXClient(TDXClient.TDXClientConfigs));

//		// Calculate pairStats
//		LocalDate simulationStart = LocalDate.now();
//		LocalDate trainingEnd = simulationStart.minusDays(1);
//		LocalDate trainingStart = trainingEnd.minusDays(FinDataConstants.STRATEGY_PAIR_TRAINING_WINDOW_DAYS);
//		pss.calculateStats(trainingStart, trainingEnd, -1);
//		pss.updateAdfpMovingAverage(trainingEnd);

		// Set up command center
//		int minute = 37;
//		cc.setFirstHalfEndCN(new LocalTime(11, minute, 10));
//		cc.setSecondHalfStartCN(new LocalTime(11, minute, 30));
//		cc.setSecondHalfEndCN(new LocalTime(11, minute, 40));

		// Construct portfolio
		// TODO: 2016/5/22 automatically pick up from where it was left the day before
		Portfolio<Stock> portfolio = new Portfolio<>();

		// Fri 2016/07/06 starting
		portfolio.putIfAbsent(stockRepo.findOneByCode("159940"), 30900d);

		Set<String> scope = ss.getStockGroup("michael/findata/algoquant/strategy/pair/group_domestic_bluechip.csv");

		// insert strategies
		HoppingStrategy hoppingStrategy = new HoppingStrategy(
				scope, portfolio,
				sellOrderType, buyOrderType,
				pairStatsRepo, pairInstanceRepo, ds);

		cc.addStrategy(hoppingStrategy);
//		cc.addTargetSecurities(hoppingStrategy.getTargetSecurities());
		cc.start();
	}
}