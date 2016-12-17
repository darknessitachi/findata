package michael.findata.algoquant.strategy.pair;

import michael.findata.algoquant.execution.component.broker.MetaBroker;
import michael.findata.algoquant.strategy.grid.GridStrategy;
import michael.findata.algoquant.strategy.pair.stocks.ShortInHKPairStrategy;
import michael.findata.commandcenter.CommandCenter;
import michael.findata.external.tdx.TDXClient;
import michael.findata.model.Stock;
import michael.findata.spring.data.repository.GridStrategyRepository;
import michael.findata.spring.data.repository.ShortInHkPairStrategyRepository;
import michael.findata.spring.data.repository.StockRepository;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import scala.util.regexp.Base;

import java.util.List;

import static com.numericalmethod.nmutils.NMUtils.getClassLogger;

public class PairStrategyCommandCenter {
	private static Logger LOGGER = getClassLogger();
	public static void main (String [] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		CommandCenter cc = (CommandCenter) context.getBean("commandCenter");
//		GridStrategyRepository gridRepo = (GridStrategyRepository) context.getBean("gridInstanceRepository");
//		StockRepository stockRepo = (StockRepository) context.getBean("stockRepository");
		cc.setShSzHqClient(new TDXClient(TDXClient.TDXClientConfigs));
		ShortInHkPairStrategyRepository shortInHkPairStrategyRepo = (ShortInHkPairStrategyRepository) context.getBean("shortInHkPairStrategyRepository");
		shortInHkPairStrategyRepo.findByOpenableDate(LocalDate.now().toDate()).forEach(strategy -> {
			strategy.setRepository(shortInHkPairStrategyRepo);
			cc.addStrategy(strategy);
//			LOGGER.info("Added strategy: [{}]", strategy);
		});

//		List<GridStrategy> grids = gridRepo.findByActive(true);
////		Set<Stock> stocks = new HashSet<>();
//		for (GridStrategy grid : grids) {
//			grid.setRepository(gridRepo);
//			cc.addStrategy(grid);
////			stocks.add(grid.getStock());
//		}

		// Set up command center
//		int hour = 20;
//		int minute = 30;
//		cc.setFirstHalfEndCN(new LocalTime(hour, minute, 10));
//		cc.setSecondHalfStartCN(new LocalTime(hour, minute, 25));
//		cc.setSecondHalfEndCN(new LocalTime(hour, minute, 59));
//		cc.setFirstHalfEndHK(new LocalTime(hour, minute, 10));
//		cc.setSecondHalfStartHK(new LocalTime(hour, minute, 30));
//		cc.setSecondHalfEndHK(new LocalTime(hour, minute, 55));

		cc.start();
	}
}