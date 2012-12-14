package michael.findata.external.hexun2008;

import java.text.DecimalFormat;
import java.text.ParseException;

public class Hexun2008Constants {
	public static DecimalFormat normalDecimalFormat = new DecimalFormat("###,###,###,###,###,###.00");
	public static DecimalFormat accurateDecimalFormat = new DecimalFormat("###,###,###,###,###,###.0000");

	public static void main (String [] args) throws ParseException {
		System.out.println(normalDecimalFormat.parse("830,000.00"));
	}
}
