package michael.findata.external.hexun2008;

import michael.findata.external.SecurityDividendData;
import michael.findata.external.SecurityDividendRecord;
import michael.findata.util.FinDataConstants;
import michael.findata.util.StringParserUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Hexun2008DividendData implements SecurityDividendData {

	private TreeMap<Date, SecurityDividendRecord> dividendRecords;
	private String stockCode;

	public TreeMap<Date, SecurityDividendRecord> getDividendRecords() {
		return dividendRecords;
	}

	public Hexun2008DividendData (String stockCode) {
		this.stockCode = stockCode;
		String [] characteristicsStrings = {
				"<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" class=\"web2\" style=\"text-align:center;\">",
				"<td class=\"lastbgcolor\"><strong>",
				"</tr>"};
		Date d;
		double total_amount;
		Number temp;
		String paymentDateString;
		dividendRecords = new TreeMap<Date, SecurityDividendRecord>();
		try {
			URL url = new URL("http://stockdata.stock.hexun.com/2009_fhzzgb_" + stockCode + ".shtml");
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			String line = StringParserUtil.skipByCharacteristicStrings(br, characteristicsStrings);
			if (line == null) {
				throw new Hexun2008DataException("Cannot find dividend data for " + stockCode);
			}
			Matcher m = Pattern.compile("([\\d-]{10})\\D+font10\">(\\d+\\.\\d+)\\D+font10\">(\\d+\\.\\d+)\\D+font10\">(\\d+\\.\\d+)\\D+font10\">([\\d-]{10}|-)\\D+font10\">([\\d,]+\\.\\d+)\\D+font10\">([\\d-]{10}|-)").matcher(line);
			while (m.find()) {
//				d = FinDataConstants.FORMAT_yyyyDashMMDashdd.parse(m.group(1));
				d = new SimpleDateFormat(FinDataConstants.yyyyDashMMDashdd).parse(m.group(1));
				paymentDateString = m.group(7);
//				temp = Hexun2008Constants.FORMAT_normalDecimalFormat.parse(m.group(6));
				temp = new DecimalFormat(Hexun2008Constants.NORMAL_DECIMAL_FORMAT).parse(m.group(6));
				if (temp instanceof Double) {
					 total_amount = 10000 * (Double)temp;
				} else if (temp instanceof Long) {
					total_amount = 10000 * (Long)temp;
				} else if (temp instanceof Integer) {
					total_amount = 10000 * (Integer)temp;
				} else {
					throw new NumberFormatException("Unexpected Number Type: "+temp);
				}
				dividendRecords.put(d, new SecurityDividendRecord(d, Float.parseFloat(m.group(2)), Float.valueOf(m.group(3)), Float.valueOf(m.group(4)), paymentDateString.length() == 10 ? FinDataConstants.FORMAT_yyyyDashMMDashdd.parse(paymentDateString) : null, total_amount));
			}
			br.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Hexun2008DataException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}