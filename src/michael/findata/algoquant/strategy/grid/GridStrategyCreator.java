package michael.findata.algoquant.strategy.grid;

import michael.findata.spring.data.repository.GridStrategyRepository;
import michael.findata.spring.data.repository.StockRepository;
import org.joda.time.LocalDate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.Timestamp;

public class GridStrategyCreator {
//		String code = "600048"; // 12000 / 9986 : 7402.00 保利地产 : 9.59
//		String code = "000338"; // 12000 / 9986 : Data not enough : 8.37
//		String code = "600104"; // 12000 / 9986 : 1352.00 上汽集团 :
//		String code = "601668"; // 12000 / 9986 : 3309.00 中国建筑 :
//		String code = "600585"; // 12000 / 9986 : 5915.00 海螺水泥 : 16.67 400-1000 shares
//		String code = "600362"; // 12000 / 9986 : 8498.00 江西铜业 : 15.18 800 shares
//		String code = "002128"; // 12000 / 9986 : 9940.00 露天煤业 : 8.37 1300 shares
	public static void main (String args []) {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		GridStrategyRepository gridRepo = (GridStrategyRepository)context.getBean("gridStrategyRepository");
		StockRepository stockRepo = (StockRepository)context.getBean("stockRepository");

		String code = "06818";
		double baseline = 3.66;
		int pos = 37250;
		GridStrategy grid = new GridStrategy(
				new GridStrategy.Param(
						14000,
						9972,
						0.0035),
				stockRepo.findOneByCode(code)
		);
		Timestamp today = new Timestamp(LocalDate.now().toDate().getTime());
		grid.setActive(false);
		grid.setCurrentBaseline(baseline);
		grid.setCurrentBottom(baseline);
		grid.setCurrentPeak(baseline);
		grid.setPositionDate(today);
		grid.setReferenceDate(today);
		grid.setPositionSellable(pos);
		grid.setPositionTotal(pos);
		gridRepo.save(grid);
	}
}