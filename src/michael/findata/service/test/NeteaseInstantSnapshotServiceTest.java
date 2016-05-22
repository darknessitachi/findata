package michael.findata.service.test;

import michael.findata.algoquant.strategy.pair.StockGroups;
import michael.findata.service.NeteaseInstantSnapshotService;
import michael.findata.service.StockService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NeteaseInstantSnapshotServiceTest {
	public static void main (String args []) throws IOException {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/findata_spring.xml");
		NeteaseInstantSnapshotService niss = (NeteaseInstantSnapshotService) context.getBean("neteaseInstantSnapshotService");
		StockService ss = (StockService) context.getBean("stockService");
		Set<String> codes = ss.getStockGroup(
				"michael/findata/algoquant/strategy/pair/group_domestic.csv",
				"michael/findata/algoquant/strategy/pair/group_oil.csv",
				"michael/findata/algoquant/strategy/pair/group_shortable_nongoldorbondETF.csv");
		codes.addAll(ss.getStockGroup("michael/findata/algoquant/strategy/pair/group_gold.csv"));
		codes.addAll(ss.getStockGroup("michael/findata/algoquant/strategy/pair/group_hk.csv"));
		niss.poll(codes.toArray(new String[codes.size()]), 7000);
	}
}