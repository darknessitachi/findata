package michael.findata.algoquant.strategy.grid;

import michael.findata.spring.data.repository.GridInstanceRepository;
import michael.findata.spring.data.repository.StockRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class GridStrategyViewer {
	public static void main (String args []) {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		GridInstanceRepository gridRepo = (GridInstanceRepository)context.getBean("gridInstanceRepository");
		StockRepository stockRepo = (StockRepository)context.getBean("stockRepository");
		System.out.printf("Stock\t\t\tBsln\tPk\t\tBtm\t\tTotal\tSellable\tActive\n");
		gridRepo.findAll().forEach(gridStrategy -> {
			System.out.printf("%s\t%.2f\t%.2f\t%.2f\t%s\t%s\t\t%s\n",
					gridStrategy.getStock(),
					gridStrategy.getCurrentBaseline(),
					gridStrategy.getCurrentPeak(),
					gridStrategy.getCurrentBottom(),
					gridStrategy.getPositionTotal(),
					gridStrategy.getPositionSellable(),
					gridStrategy.isActive());
//			System.out.print();
		});
		System.out.println("");
	}
}
