package michael.findata.external.hexun2008;

import java.text.DecimalFormat;
import java.text.ParseException;

public class Hexun2008Constants {
	public static String NORMAL_DECIMAL_FORMAT = "###,###,###,###,###,###.00";
	public static DecimalFormat FORMAT_normalDecimalFormat = new DecimalFormat(NORMAL_DECIMAL_FORMAT);
	public static String ACCURATE_DECIMAL_FORMAT = "###,###,###,###,###,###.0000";
	public static DecimalFormat FORMAT_accurateDecimalFormat = new DecimalFormat(ACCURATE_DECIMAL_FORMAT);

	public static void main (String [] args) throws ParseException {
		System.out.println(FORMAT_normalDecimalFormat.parse("830,000.00"));
	}
}
