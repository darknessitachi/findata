package michael.findata.algoquant.strategy.pair;

import michael.findata.algoquant.execution.strategy.DisruptorStrategyMarshaller;
import michael.findata.algoquant.strategy.grid.GridStrategy;
import michael.findata.algoquant.strategy.pair.stocks.ShortInHKPairStrategy;
import michael.findata.commandcenter.CommandCenter;
import michael.findata.external.tdx.TDXClient;
import michael.findata.service.DividendService;
import michael.findata.spring.data.repository.GridStrategyRepository;
import michael.findata.spring.data.repository.ShortInHkPairStrategyRepository;
import michael.findata.util.DBUtil;
import org.joda.time.LocalDate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.HashSet;
import java.util.List;

public class PairStrategyCommandCenter {

	public static void main (String [] args) {
		Process dbProcess = DBUtil.tryToStartDB();
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		CommandCenter cc = (CommandCenter) context.getBean("commandCenter");
		GridStrategyRepository gridRepo = (GridStrategyRepository) context.getBean("gridStrategyRepository");
//		StockRepository stockRepo = (StockRepository) context.getBean("stockRepository");
		DividendService dividendService = (DividendService) context.getBean("dividendService");
		cc.setShSzHqClient(new TDXClient(TDXClient.TDXClientConfigs));
		ShortInHkPairStrategyRepository shortInHkPairStrategyRepo = (ShortInHkPairStrategyRepository) context.getBean("shortInHkPairStrategyRepository");

		HashSet<ShortInHKPairStrategy> pairStras = new HashSet<>();
		pairStras.addAll(shortInHkPairStrategyRepo.findByOpenableDate(LocalDate.now().toDate()));
		pairStras.addAll(shortInHkPairStrategyRepo.findByStatusIn(ShortInHKPairStrategy.Status.OPENED, ShortInHKPairStrategy.Status.OPENING));

		pairStras.forEach(strategy -> {
			strategy.dividendService(dividendService);
			strategy.setRepository(shortInHkPairStrategyRepo);
			cc.addStrategy(new DisruptorStrategyMarshaller(strategy));
		});

		List<GridStrategy> grids = gridRepo.findByActive(true);
		for (GridStrategy grid : grids) {
			cc.addStrategy(new DisruptorStrategyMarshaller(grid));
			grid.setRepository(gridRepo);
		}

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