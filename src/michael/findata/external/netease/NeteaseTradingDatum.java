package michael.findata.external.netease;

import michael.findata.external.SecurityTradingDatum;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;

public class NeteaseTradingDatum extends SecurityTradingDatum {
	protected JSONObject data = null;
	protected String stockCode;
	protected String neteaseInternalCode;

	public NeteaseTradingDatum(String stockCode) throws IOException {
		this.stockCode = stockCode;
		try {
			if (stockCode.startsWith("6") || stockCode.startsWith("9") || stockCode.startsWith("5")) {
				neteaseInternalCode = "0" + stockCode;
			} else {
				neteaseInternalCode = "1" + stockCode;
			}
			URL url = new URL("http://api.money.163.com/data/feed/" + neteaseInternalCode);
//			InputStreamReader isr = new InputStreamReader(url.openStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			String s = br.readLine();
			s = s.substring(21, s.length()-2);
//			isr.
//			is.skip(22);
//			byte[] in = new byte[is.available() - 2];
//			is.read(in);
//			String s = "{"+new String(in);
			data = (JSONObject) JSONValue.parse(s);
			br.close();
		} catch (Exception e) {
		}
	}

	public String getStockCode() {
		return stockCode;
	}

	@Override
	public Number getValue() {
		return (Number) getProperty("price");
	}

	public String getStockName() {
		return (String) getProperty("name");
	}

	public Number getCurrent() {
		return (Number) getProperty("price");
	}

	private Object getProperty(String name) {
		try {
			return ((Map) data.get(neteaseInternalCode)).get(name);
		} catch (Exception e) {
			return null;
		}
	}
}
