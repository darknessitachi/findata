package michael.findata.external.tdx.test;

import michael.findata.external.SecurityTimeSeriesDatum;
import michael.findata.external.tdx.TDXPriceHistory;

/**
 * Created by nicky on 2015/11/15.
 */
public class TDXPriceHistoryTest {
	public static void main (String [] args) {
		TDXPriceHistory ml = new TDXPriceHistory("000568");
		SecurityTimeSeriesDatum minute;
		while (ml.hasNext()) {
			minute = ml.next();
			System.out.println(minute.getDateTime());
			System.out.println(minute.getOpen());
			System.out.println(minute.getHigh());
			System.out.println(minute.getLow());
			System.out.println(minute.getClose());
			System.out.println(minute.getAmount());
			System.out.println(minute.getVolume());
		}
	}
}
