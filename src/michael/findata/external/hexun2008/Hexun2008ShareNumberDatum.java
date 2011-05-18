package michael.findata.external.hexun2008;

import michael.findata.external.SecurityShareNumberDatum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

import static michael.findata.external.hexun2008.Hexun2008Constants.*;

public class Hexun2008ShareNumberDatum extends SecurityShareNumberDatum {
	private String stockCode;
	private Number numberOfShares = null;

	public Hexun2008ShareNumberDatum(String stockCode) {
		this.stockCode = stockCode;
		String line;
		try {
			URL url = new URL("http://stockdata.stock.hexun.com/2009_gbjg_" + stockCode + ".shtml");
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			do {
				line = br.readLine();
//				System.out.println(line);
			} while (line != null && !line.contains("变动时间")); // ????
			if (line == null) {
				throw new Hexun2008DataException("Cannot find total number of shares for " + stockCode);
			}
			do {
				line = br.readLine();
//				System.out.println(line);
			} while (line != null && !line.contains("<td height=\"1\" colspan=\"5\" bgcolor=\"#cfcfcf\"></td>"));
			if (line == null) {
				throw new Hexun2008DataException("Cannot find total number of shares for " + stockCode);
			}
			do {
				line = br.readLine();
//				System.out.println(line);
			} while (line != null && !line.contains("<td align=\"center\" class=\"dotborder\">"));
			if (line == null) {
				throw new Hexun2008DataException("Cannot find total number of shares for " + stockCode);
			}
			line = br.readLine();
			numberOfShares = normalDecimalFormat.parse(line.substring(line.indexOf(">")+1, line.length() - 5).trim());
			if (numberOfShares instanceof Double) {
				numberOfShares = 10000 * (Double)numberOfShares;
			} else if (numberOfShares instanceof Long) {
				numberOfShares = 10000 * (Long)numberOfShares;
			} else if (numberOfShares instanceof Integer) {
				numberOfShares = 10000 * (Integer)numberOfShares;
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
}
