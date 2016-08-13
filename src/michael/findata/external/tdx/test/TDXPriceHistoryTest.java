package michael.findata.external.tdx.test;

import michael.findata.external.SecurityTimeSeriesDatum;
import michael.findata.external.tdx.TDXFileBasedPriceHistory;

public class TDXPriceHistoryTest {
	public static void main (String [] args) {
		TDXFileBasedPriceHistory ml = new TDXFileBasedPriceHistory("600977");
		SecurityTimeSeriesDatum minute;
		while (ml.hasNext()) {
			minute = ml.popNext();
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
