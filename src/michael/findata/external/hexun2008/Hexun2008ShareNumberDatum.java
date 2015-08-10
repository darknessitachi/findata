package michael.findata.external.hexun2008;

import michael.findata.external.SecurityShareNumberChange;
import michael.findata.external.SecurityShareNumberChangesData;
import michael.findata.external.SecurityShareNumberDatum;
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
import java.util.ArrayList;
import java.util.Date;

import static michael.findata.external.hexun2008.Hexun2008Constants.*;

public class Hexun2008ShareNumberDatum extends SecurityShareNumberDatum implements SecurityShareNumberChangesData {
	private String stockCode;
	private Number numberOfShares = null;
	private ArrayList<SecurityShareNumberChange> shareNumberChanges;

	public Hexun2008ShareNumberDatum(String stockCode) throws Hexun2008DataException {
		CloseableHttpClient httpClient = FinDataConstants.httpClient;
		this.stockCode = stockCode;
		shareNumberChanges = new ArrayList<>();

		String [] characteristicsString = {
				"<td class=\"bgcolor\" width=\"69%\"><strong>",
				"<td height=\"1\" colspan=\"5\" bgcolor=\"#cfcfcf\"></td>"
		};
		String line;
		try {
			HttpGet get = new HttpGet("http://stockdata.stock.hexun.com/2009_gbjg_" + stockCode + ".shtml");
			get.setHeader("Host", "stockdata.stock.hexun.com");
			get.setHeader("Connection", "keep-alive");
			get.setHeader("Cache-Control", "max-age=0");
			get.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			get.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.130 Safari/537.36");
			get.setHeader("Accept-Encoding", "gzip, deflate, sdch");
			get.setHeader("Accept-Language", "en-US,en;q=0.8");
            CloseableHttpResponse response = httpClient.execute(get);
            BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "GB2312"));
            line = StringParserUtil.skipByCharacteristicStrings(br, characteristicsString);
			if (line == null) {
				throw new Hexun2008DataException("Cannot find total number of shares for " + stockCode);
			}
			int index, changeCounter = 0;
			Number nos;
			Date changeDate = null;
			while (line != null) {
//				System.out.println(line);
				if (line.contains(">--<")) break;
				index = line.indexOf("<td align=\"center\" class=\"dotborder\">");
				if (index != -1) index += 37;
				if (index == -1) {
					index = line.indexOf("<td align=\"center\" class=\"bgcolor\">");
					if (index != -1) index += 35;
				}
				if (index != -1) {
					changeCounter ++;
					switch (changeCounter) {
						case 1:
							// Date of change
//							changeDate = FinDataConstants.FORMAT_yyyyDashMMDashdd.parse(line.substring(index, line.length() - 5).trim());
							changeDate = new SimpleDateFormat(FinDataConstants.yyyyDashMMDashdd).parse(line.substring(index, line.length() - 5).trim());
							break;
						case 2:
							// Number of shares after the change
//							nos = FORMAT_normalDecimalFormat.parse(line.substring(line.indexOf(">")+1, line.length() - 5).trim());
							nos = new DecimalFormat(NORMAL_DECIMAL_FORMAT).parse(line.substring(line.indexOf(">")+1, line.length() - 5).trim());
							if (nos instanceof Double) {
								nos = 10000 * (Double)nos;
							} else if (nos instanceof Long) {
								nos = 10000 * (Long)nos;
							} else if (nos instanceof Integer) {
								nos = 10000 * (Integer)nos;
							}
							getShareNumberChanges().add(new SecurityShareNumberChange(changeDate, nos));
					}
					line = br.readLine();
					continue;
				}
				index = line.indexOf("<div class=\"tishi\">");
				if (index != -1) {
					changeCounter = 0;
				}
				line = br.readLine();
			}
			if (getShareNumberChanges().isEmpty()) {
				throw new Hexun2008DataException("Cannot find total number of shares for " + stockCode);
			} else {
				numberOfShares = getShareNumberChanges().get(0).getNumberOfShares();
			}
			br.close();
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getStockCode() {
		return stockCode;
	}

	@Override
	public Number getValue() {
		return numberOfShares;
	}

	public ArrayList<SecurityShareNumberChange> getShareNumberChanges() {
		return shareNumberChanges;
	}
}
