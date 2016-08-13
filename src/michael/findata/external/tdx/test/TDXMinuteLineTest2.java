package michael.findata.external.tdx.test;

import michael.findata.external.tdx.TDXMinuteLine;
import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import static michael.findata.util.FinDataConstants.yyyyMMdd;

public class TDXMinuteLineTest2 {
	public static void main (String [] args) throws ParseException {
		TDXMinuteLine line = new TDXMinuteLine("900956");
		long start = new DateTime(new SimpleDateFormat(yyyyMMdd).parse("20160421")).getMillis();
		while (line.hasNext() && line.peekNext().getDateTime().getMillis() > start) {
			System.out.println("Time:\t"+line.peekNext().getDateTime());
			System.out.println("Open:\t"+line.peekNext().getOpen());
			System.out.println("High:\t"+line.peekNext().getHigh());
			System.out.println("Low:\t"+line.peekNext().getLow());
			System.out.println("Close:\t"+line.peekNext().getClose());
			System.out.println("Amt:\t"+line.peekNext().getAmount());
			System.out.println("Vol:\t"+line.peekNext().getVolume());
			line.popNext();
		}
	}
}