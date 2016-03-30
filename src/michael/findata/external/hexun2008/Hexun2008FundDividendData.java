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

public class Hexun2008FundDividendData implements SecurityDividendData {
	private TreeMap<Date, SecurityDividendRecord> dividendRecords;
	private String fundCode;
	@Override
	public TreeMap<Date, SecurityDividendRecord> getDividendRecords() {
		return dividendRecords;
	}
	public Hexun2008FundDividendData (String fundCode) {
		CloseableHttpClient httpClient = FinDataConstants.httpClient;
		this.fundCode = fundCode;
		String [] characteristicsStrings = {
				"<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" id=\"fundData\">",
				"</tr>"};
		dividendRecords = new TreeMap<>();
		try {
			HttpGet get = new HttpGet("http://jingzhi.funds.hexun.com/database/jjfh.aspx?fundcode="+fundCode);
			get.setHeader("Host", "jingzhi.funds.hexun.com");
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
				throw new Hexun2008DataException("Cannot find dividend data for " + fundCode);
			}
			int index = 0;
			Date announcement_date = null, registration_date, payment_date = null;
			float amount = 0f;
			SimpleDateFormat sdf = new SimpleDateFormat(FinDataConstants.yyyyDashMMDashdd);
			while ((line = br.readLine()) != null && !line.startsWith("</table>")) {
//				System.out.print((index % 11)+"\t");
//				System.out.println(line);
				switch (index % 11) {
					case 0:
						announcement_date = sdf.parse(line.substring(19, 29));
						break;
					case 2:
						amount = Float.parseFloat(line.substring(19, line.length()-5));
						break;
					case 3:
						registration_date = sdf.parse(line.substring(19, 29));
						break;
					case 4:
						payment_date = sdf.parse(line.substring(19, 29));
						break;
					case 5:
						dividendRecords.put(announcement_date,
								new SecurityDividendRecord(
										announcement_date,
										amount,
										0f,
										0f,
										payment_date,
										0d));
						break;
				}
				if (line.length() > 50) {
					index +=3;
				} else {
					index ++;
				}
			}
			br.close();
		} catch (IOException | Hexun2008DataException | ParseException e) {
			System.out.println("Exception caught when getting dividend data for "+fundCode);
			e.printStackTrace();
		}
	}
}