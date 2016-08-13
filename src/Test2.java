import michael.findata.service.StockPriceMinuteService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.text.ParseException;

public class Test2 {
	public static void main (String [] args) throws ParseException, IOException {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		StockPriceMinuteService sr = (StockPriceMinuteService) context.getBean("stockPriceMinuteService");
		// Need to update data in 70 trading days
		sr.updateMinuteData();
//		StockPriceMinuteRepository spmr = (StockPriceMinuteRepository) context.getBean("stockPriceMinuteRepository");
//		StockPriceMinute spm = spmr.findOneByStock_CodeAndDate("510070", new Timestamp(DateTime.parse("2016-07-18").getMillis()));
//		System.out.println(spm.getOpen(1));
//		System.out.println(spm.getHigh(1));
//		System.out.println(spm.getLow(1));
//		System.out.println(spm.getClose(1));
//		System.out.println(spm.getVolum(1));
//		System.out.println(spm.getAmount(1));
	}
}