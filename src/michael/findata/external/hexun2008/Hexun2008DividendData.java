package michael.findata.external.hexun2008;

import michael.findata.external.SecurityDividendData;
import michael.findata.external.SecurityDividendRecord;
import michael.findata.util.FinDataConstants;
import michael.findata.util.StringParserUtil;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
		CloseableHttpClient httpClient = FinDataConstants.httpClient;
		this.stockCode = stockCode;
		String [] characteristicsStrings = {
				"<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" class=\"web2\" style=\"text-align:center;\">",
				"<td class=\"lastbgcolor\"><strong>",
				"</tr>"};
		Date d;
		double total_amount;
		Number temp;
		String paymentDateString;
		dividendRecords = new TreeMap<>();
		try {
			HttpGet get = new HttpGet("http://stockdata.stock.hexun.com/2009_fhzzgb_" + stockCode + ".shtml");
			get.setHeader("Host", "stockdata.stock.hexun.com");
			get.setHeader("Connection", "keep-alive");
			get.setHeader("Cache-Control", "max-age=0");
			get.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			get.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.130 Safari/537.36");
			get.setHeader("Accept-Encoding", "gzip, deflate, sdch");
			get.setHeader("Accept-Language", "en-US,en;q=0.8");
			CloseableHttpResponse response = httpClient.execute(get);
			BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "GB2312"));
			String line = StringParserUtil.skipByCharacteristicStrings(br, characteristicsStrings);
			if (line == null) {
				br.close();
				throw new Hexun2008DataException("Cannot find dividend data for " + stockCode);
			}
			Matcher m = Pattern.compile("([\\d-]{10})\\D+font10\">(\\d+\\.\\d+)\\D+font10\">(\\d+\\.\\d+)\\D+font10\">(\\d+\\.\\d+)\\D+font10\">([\\d-]{10}|-)\\D+font10\">([\\d,]+\\.\\d+)\\D+font10\">([\\d-]{10}|-)").matcher(line);
			SimpleDateFormat sdf = new SimpleDateFormat(FinDataConstants.yyyyDashMMDashdd);
			DecimalFormat df = new DecimalFormat(Hexun2008Constants.NORMAL_DECIMAL_FORMAT);
			while (m.find()) {
				d = sdf.parse(m.group(1));
				paymentDateString = m.group(7);
				temp = df.parse(m.group(6));
				if (temp instanceof Double) {
					 total_amount = 10000 * (Double)temp;
				} else if (temp instanceof Long) {
					total_amount = 10000 * (Long)temp;
				} else if (temp instanceof Integer) {
					total_amount = 10000 * (Integer)temp;
				} else {
					br.close();
					throw new NumberFormatException("Unexpected Number Type: "+temp);
				}
				dividendRecords.put(d, new SecurityDividendRecord(d, Float.parseFloat(m.group(2)), Float.valueOf(m.group(3)), Float.valueOf(m.group(4)), paymentDateString.length() == 10 ? sdf.parse(paymentDateString) : null, total_amount));
			}
			br.close();
		} catch (IOException | ParseException | Hexun2008DataException e) {
			e.printStackTrace();
		}
	}
}