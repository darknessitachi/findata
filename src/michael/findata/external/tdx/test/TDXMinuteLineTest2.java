package michael.findata.external.tdx.test;

import michael.findata.external.tdx.TDXMinuteLine;
import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import static michael.findata.util.FinDataConstants.yyyyMMdd;

public class TDXMinuteLineTest2 {
	public static void main (String [] args) throws ParseException {
		TDXMinuteLine line = new TDXMinuteLine("510300");
		long start = new DateTime(new SimpleDateFormat(yyyyMMdd).parse("20160408")).getMillis();
		while (line.hasNext() && line.peekNext().getDateTime().getMillis() > start) {
			System.out.println(line.peekNext().getDateTime() + "\t" + "\t" + line.peekNext().getClose());
			line.popNext();
		}
	}
}
