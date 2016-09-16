package michael.findata.algoquant.strategy.grid;

import michael.findata.algoquant.execution.component.broker.LocalTdxBrokerProxy;
import michael.findata.commandcenter.CommandCenter;
import michael.findata.external.tdx.TDXClient;
import michael.findata.model.Stock;
import michael.findata.spring.data.repository.GridInstanceRepository;
import org.joda.time.LocalTime;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GridStrategyCommandCenter {
	public static void main(String[] args) throws IOException {
		// TODO: 9/16/2016 simu-test
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		CommandCenter cc = (CommandCenter) context.getBean("commandCenter");
		GridInstanceRepository gridRepo = (GridInstanceRepository) context.getBean("gridInstanceRepository");
		cc.setBroker(new LocalTdxBrokerProxy(10001));
		cc.setShSzHqClient(new TDXClient(TDXClient.TDXClientConfigs));

		List<GridStrategy> grids = gridRepo.findByActive(true);
		Set<Stock> stocks = new HashSet<>();
		for (GridStrategy grid : grids) {
			cc.addStrategy(grid);
			grid.setGridInstanceRepository(gridRepo);
			stocks.add(grid.getStock());
		}
		cc.setTargetSecurities(stocks);

		// Set up command center
		int hour = 16;
		int minute = 53;
		cc.setFirstHalfEnd(new LocalTime(hour, minute, 10));
		cc.setSecondHalfStart(new LocalTime(hour, minute, 30));
		cc.setSecondHalfEnd(new LocalTime(hour, minute, 40));

		cc.start();
	}
}