package michael.findata.external.szse.test;

import michael.findata.external.szse.SZSEShortableStockList;

import java.io.IOException;
import java.util.Set;

/**
 * Created by nicky on 2015/11/25.
 */
public class SZSEShortableStockListTest {
	public static void main (String [] args) throws IOException {
		Set<String> shseShortables = new SZSEShortableStockList().getShortables();
		shseShortables.stream().forEach(System.out::println);
	}
}