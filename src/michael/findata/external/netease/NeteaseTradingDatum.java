package michael.findata.external.netease;

import michael.findata.external.SecurityTradingDatum;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: michaelc
 * Date: 2010-11-17
 * Time: 18:16:19
 * To change this template use File | Settings | File Templates.
 */
public class NeteaseTradingDatum extends SecurityTradingDatum {
	protected JSONObject data = null;
	protected String stockCode;
	protected String neteaseInternalCode;

	public NeteaseTradingDatum(String stockCode) throws IOException {
		this.stockCode = stockCode;
		try {
			if (stockCode.startsWith("6") || stockCode.startsWith("9")) {
				neteaseInternalCode = "0" + stockCode;
			} else {
				neteaseInternalCode = "1" + stockCode;
			}
			URL url = new URL("http://api.money.163.com/data/feed/" + neteaseInternalCode);
			InputStream is = url.openStream();
			is.skip(22);
			byte[] in = new byte[is.available() - 2];
			is.read(in);
			String s = "{" + new String(in);
			data = (JSONObject) JSONValue.parse(s);
		} catch (Exception e) {
		}
	}

	public String getStockCode() {
		return stockCode;
	}

	public String getStockName() {
		return (String) getProperty("name");
	}

	public Double getCurrent() {
		return (Double) getProperty("price");
	}

	private Object getProperty(String name) {
		try {
			return ((Map) data.get(neteaseInternalCode)).get(name);
		} catch (Exception e) {
			return null;
		}
	}
}
