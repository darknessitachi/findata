package michael.findata.service.test;

import michael.findata.service.StockService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class StockServiceTest {
	public static void main (String [] args) throws IOException {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		StockService ss = (StockService) context.getBean("stockService");
		ss.updateSpreadForStocks(0.003d, 5000);
	}
}
