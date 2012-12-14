package michael.findata.external.hexun2008;

import michael.findata.external.SecurityShareNumberChange;
import michael.findata.external.SecurityShareNumberChangesData;
import michael.findata.external.SecurityShareNumberDatum;
import michael.findata.util.FinDataConstants;
import michael.findata.util.StringParserUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static michael.findata.external.hexun2008.Hexun2008Constants.*;

public class Hexun2008ShareNumberDatum extends SecurityShareNumberDatum implements SecurityShareNumberChangesData {
	private String stockCode;
	private Number numberOfShares = null;
	private ArrayList<SecurityShareNumberChange> shareNumberChanges;

	public Hexun2008ShareNumberDatum(String stockCode) {
		this.stockCode = stockCode;

		String [] characteristicsString = {
				"<td class=\"bgcolor\" width=\"69%\"><strong>",
				"<td height=\"1\" colspan=\"5\" bgcolor=\"#cfcfcf\"></td>"
		};
		String line;
		try {
			URL url = new URL("http://stockdata.stock.hexun.com/2009_gbjg_" + stockCode + ".shtml");
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			line = StringParserUtil.skipByCharacteristicStrings(br, characteristicsString);
			if (line == null) {
				throw new Hexun2008DataException("Cannot find total number of shares for " + stockCode);
			}
			int index, changeCounter = 0;
			Number nos;
			Date changeDate = null;
			shareNumberChanges = new ArrayList<SecurityShareNumberChange>();
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
							changeDate = FinDataConstants.yyyyDashMMDashdd.parse(line.substring(index, line.length() - 5).trim());
							break;
						case 2:
							// Number of shares after the change
							nos = normalDecimalFormat.parse(line.substring(line.indexOf(">")+1, line.length() - 5).trim());
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
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Hexun2008DataException e) {
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
