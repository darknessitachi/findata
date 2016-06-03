package michael.findata.commandcenter.test;

import com.numericalmethod.algoquant.execution.datatype.product.portfolio.Portfolio;
import michael.findata.algoquant.execution.component.broker.LocalBrokerProxy;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.Set;

import static michael.findata.algoquant.execution.datatype.order.HexinOrder.HexinType.CREDIT_BUY;
import static michael.findata.algoquant.execution.datatype.order.HexinOrder.HexinType.CREDIT_SELL;

public class CommandCenterTest {
	public static void main(String[] args) throws IOException {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		CommandCenter cc = (CommandCenter) context.getBean("commandCenter");
		StockRepository stockRepo = (StockRepository) context.getBean("stockRepository");
		PairStatsRepository pairStatsRepo = (PairStatsRepository) context.getBean("pairStatsRepository");
		PairInstanceRepository pairInstanceRepo = (PairInstanceRepository) context.getBean("pairInstanceRepository");
		PairStrategyService pss = (PairStrategyService) context.getBean("pairStrategyService");
		DividendService ds = (DividendService) context.getBean("dividendService");
		StockService ss = (StockService) context.getBean("stockService");

		cc.setBroker(new LocalBrokerProxy());
		cc.setShSzClient(new TDXClient(
				"221.236.13.218:7709",    // 招商证券成都行情 - 9-33 ms
				"221.236.13.219:7709",    // 招商证券成都行情 - 8-25 ms
				"125.71.28.133:7709",    // cd1010 - 8 ms
				"221.236.15.14:995",    // 国金成都电信2.1
				"124.161.97.84:7709",    // 申银万国成都网通2
				"124.161.97.83:7709",    // 申银万国成都网通1
				"125.64.39.62:7709",    // 申银万国成都电信2
				"125.71.28.133:443",    // cd1010 - 8 ms
				"221.236.15.14:7709",    // 国金成都电信1.13
				"119.6.204.139:7709",    // 国金成都联通5.135
				"125.64.39.61:7709",    // 申银万国成都电信1
				"125.64.41.12:7709",    // 成都电信54
				"119.4.167.141:7709",    // 华西L1
				"119.4.167.142:7709",    // 华西L2
				"119.4.167.181:7709",    // 华西L3
				"119.4.167.182:7709",    // 华西L4
				"119.4.167.164:7709",    // 华西L5
				"119.4.167.165:7709",    // 华西L6
				"119.4.167.163:7709",    // 华西L7
				"119.4.167.175:7709",    // 华西L8
				"218.6.198.151:7709",    // 华西L1
				"218.6.198.152:7709",    // 华西L2
				"218.6.198.174:7709",    // 华西L3
				"218.6.198.175:7709",    // 华西L4
				"218.6.198.155:7709",    // 华西L5
				"218.6.198.156:7709",    // 华西L6
				"218.6.198.157:7709",    // 华西L7
				"218.6.198.158:7709",    // 华西L8
				"182.131.7.141:7709",    // 华西E1
				"182.131.7.142:7709",    // 华西E2
				"182.131.7.143:7709",    // 华西E3
				"182.131.7.144:7709",    // 华西E4
				"182.131.7.145:7709",    // 华西E5
				"182.131.7.146:7709",    // 华西E6
				"182.131.7.147:7709",    // 华西E7
				"182.131.7.148:7709",    // 华西E8
				"182.131.3.245:7709",    // 上证云行情J330 - 9ms)
				"221.237.158.106:7709",    // 西南证券金点子成都电信主站1
				"221.237.158.107:7709",    // 西南证券金点子成都电信主站2
				"221.237.158.108:7709",    // 西南证券金点子成都电信主站3
				"183.230.9.136:7709",    // 西南证券金点子重庆移动主站1
				"183.230.134.6:7709",    // 西南证券金点子重庆移动主站2
				"219.153.1.115:7709",    // 西南证券金点子重庆电信主站1
				"113.207.29.12:7709"    // 西南证券金点子重庆联通主站1
		));

//		// Calculate pairStats
//		LocalDate simulationStart = LocalDate.now();
//		LocalDate trainingEnd = simulationStart.minusDays(1);
//		LocalDate trainingStart = trainingEnd.minusDays(FinDataConstants.STRATEGY_PAIR_TRAINING_WINDOW_DAYS);
//		pss.calculateStats(trainingStart, trainingEnd, -1);
//		pss.updateAdfpMovingAverage(trainingEnd);

		// Set up command center
//		int minute = 59;
//		cc.setFirstHalfEnd(new LocalTime(23, minute, 10));
//		cc.setSecondHalfStart(new LocalTime(23, minute, 30));
//		cc.setSecondHalfEnd(new LocalTime(23, minute, 40));

		// Construct portfolio
		// TODO: 2016/5/22 automatically pick up from where it was left the day before
		Portfolio<Stock> portfolio = new Portfolio<>();

		// Fri 2016/06/06 starting
		portfolio.putIfAbsent(stockRepo.findOneByCode("159919"), 3000d);
		portfolio.putIfAbsent(stockRepo.findOneByCode("510300"), 3300d);
		portfolio.putIfAbsent(stockRepo.findOneByCode("510310"), 8000d);

		Set<String> scope = ss.getStockGroup("michael/findata/algoquant/strategy/pair/group_domestic_bluechip3.csv");

		// insert strategies
		HoppingStrategy hoppingStrategy = new HoppingStrategy(
				scope, portfolio,
				CREDIT_SELL, CREDIT_BUY,
				pairStatsRepo, pairInstanceRepo, ds);
		cc.addStrategy(hoppingStrategy);
		cc.setTargetSecurities(hoppingStrategy.getStocks());
		cc.start();
	}
}