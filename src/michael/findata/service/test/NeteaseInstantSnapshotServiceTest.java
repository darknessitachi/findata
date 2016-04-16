package michael.findata.service.test;

import michael.findata.algoquant.strategy.pair.StockGroups;
import michael.findata.service.NeteaseInstantSnapshotService;
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
		Set<String> collect = Stream.concat(Stream.concat(Stream.concat(Stream.concat(Stream.concat(Stream.concat(Stream.concat(Stream.concat(Stream.concat(
				Arrays.stream(StockGroups.Securities),
				Arrays.stream(StockGroups.Banking)),
				Arrays.stream(StockGroups.Insurance)),
				Arrays.stream(StockGroups.ETFBlueChips)),
				Arrays.stream(StockGroups.ETFSmallCaps)),
				Arrays.stream(StockGroups.LargeMarketCap)),
				Arrays.stream(StockGroups.Highway)),
				Arrays.stream(StockGroups.Alcohol)),
				Arrays.stream(StockGroups.TPlus0Funds)),
				Arrays.stream(StockGroups.TPlus0Gold))
				.map(stock -> stock.symbol().substring(0, 6)).collect(Collectors.toSet());
		String [] codes = collect.toArray(new String[collect.size()]);

		niss.poll(codes, 7000);
	}
}