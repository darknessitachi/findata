package michael.findata.external.shse.test;

import michael.findata.external.shse.SHSEShortableStockList;

import java.io.IOException;
import java.util.Set;

/**
 * Created by nicky on 2015/11/25.
 */
public class SHSEShortableStockListTest {
	public static void main (String [] args) throws IOException {
		Set<String> shseShortables = new SHSEShortableStockList().getShortables();
		shseShortables.stream().forEach(System.out::println);
	}
}
