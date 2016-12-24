package michael.findata.algoquant.strategy.grid;

import michael.findata.algoquant.execution.component.broker.MetaBroker;
import michael.findata.commandcenter.CommandCenter;
import michael.findata.external.tdx.TDXClient;
import michael.findata.spring.data.repository.GridStrategyRepository;
import michael.findata.util.DBUtil;
import org.joda.time.LocalTime;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.List;

public class GridStrategyCommandCenter {
	public static void main(String[] args) throws IOException {
		Process dbProcess = DBUtil.tryToStartDB();
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		CommandCenter cc = (CommandCenter) context.getBean("commandCenter");
		GridStrategyRepository gridRepo = (GridStrategyRepository) context.getBean("gridStrategyRepository");
//		cc.setBroker(new MetaBroker());
		cc.setShSzHqClient(new TDXClient(TDXClient.TDXClientConfigs));

		List<GridStrategy> grids = gridRepo.findByActive(true);
//		Set<Stock> stocks = new HashSet<>();
		for (GridStrategy grid : grids) {
			cc.addStrategy(grid);
			grid.setRepository(gridRepo);
//			stocks.add(grid.getStock());
		}
//		cc.addTargetSecurities(stocks);

		// Set up command center
		int hour = 23;
		int minute = 47;
		cc.setFirstHalfEndCN(new LocalTime(hour, minute, 10));
		cc.setSecondHalfStartCN(new LocalTime(hour, minute, 25));
		cc.setSecondHalfEndCN(new LocalTime(hour, minute, 59));
		cc.setFirstHalfEndHK(new LocalTime(hour, minute, 10));
		cc.setSecondHalfStartHK(new LocalTime(hour, minute, 30));
		cc.setSecondHalfEndHK(new LocalTime(hour, minute, 55));

		cc.start();
	}
}