package michael.findata.service.test;

import michael.findata.external.tdx.TDXClient;
import michael.findata.service.DividendService;
import michael.findata.spring.data.repository.PairRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class DividendServiceTest {
	public static void main (String [] args) throws IOException, SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		DividendService ds = (DividendService) context.getBean("dividendService");
		PairRepository pairRepo = (PairRepository) context.getBean("pairRepository");
		Set<String> codes = new HashSet<>();
		pairRepo.findByEnabled(true).forEach(pair->{
			codes.add(pair.getCodeToLong());
			codes.add(pair.getCodeToShort());
		});
		TDXClient c = new TDXClient("218.6.198.155:7709");
		c.connect();
		for (String code : codes) {
			ds.refreshDividendDataForFund(code, c);
		}
		c.disconnect();
	}
}
