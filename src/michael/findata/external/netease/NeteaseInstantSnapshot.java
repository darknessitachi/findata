package michael.findata.external.netease;

import com.numericalmethod.algoquant.execution.datatype.product.stock.Stock;
import michael.findata.algoquant.execution.datatype.depth.Depth;
import michael.findata.algoquant.product.stock.shse.SHSEStock;
import michael.findata.algoquant.product.stock.szse.SZSEStock;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class NeteaseInstantSnapshot {
	protected JSONObject data = null;
	protected Depth[] depths;
	protected HashMap<String, Depth> depthMap;
	public NeteaseInstantSnapshot (String ... codes) {
		String [] neteaseInternalCodes;
		neteaseInternalCodes = new String [codes.length];
		try {
			StringBuffer urlStr = new StringBuffer("http://api.money.163.com/data/feed/");
			for (int i = codes.length-1; i > -1; i--) {
				if (codes[i].startsWith("6") || codes[i].startsWith("9") || codes[i].startsWith("5")) {
					neteaseInternalCodes[i] = "0" + codes [i];
				} else {
					neteaseInternalCodes[i] = "1" + codes [i];
				}
				urlStr.append(",").append(neteaseInternalCodes[i]);
			}
			URL url = new URL(urlStr.toString());
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			String s = br.readLine();
			s = s.substring(21, s.length()-2);
			data = (JSONObject) JSONValue.parse(s);
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		buildDepths();
	}

	public NeteaseInstantSnapshot (String JSON) {
		data = (JSONObject) JSONValue.parse(JSON);
		buildDepths();
	}

	private void buildDepths() {
		depthMap = new HashMap<>();
		Long zero = 0L;
		depths = new Depth[data.size()];
		Stock stock;
		String code;
		int i = 0;
		for (Map stockSnapshot : (Collection<Map>) data.values()) {
			code = (String) stockSnapshot.get("symbol");
			if (code.startsWith("6") || code.startsWith("9") || code.startsWith("5")) {
				stock = new SHSEStock(code);
			} else {
				stock = new SZSEStock(code);
			}
			depths[i] = new Depth(
					(double)stockSnapshot.get("price"),
					stock, zero.equals(stockSnapshot.get("status")),
					(double)stockSnapshot.get("bid5"),
					(double)stockSnapshot.get("bid4"),
					(double)stockSnapshot.get("bid3"),
					(double)stockSnapshot.get("bid2"),
					(double)stockSnapshot.get("bid1"),
					(double)stockSnapshot.get("ask1"),
					(double)stockSnapshot.get("ask2"),
					(double)stockSnapshot.get("ask3"),
					(double)stockSnapshot.get("ask4"),
					(double)stockSnapshot.get("ask5"));
			depths[i].setVols(
					(long)stockSnapshot.get("bidvol5"),
					(long)stockSnapshot.get("bidvol4"),
					(long)stockSnapshot.get("bidvol3"),
					(long)stockSnapshot.get("bidvol2"),
					(long)stockSnapshot.get("bidvol1"),
					(long)stockSnapshot.get("askvol1"),
					(long)stockSnapshot.get("askvol2"),
					(long)stockSnapshot.get("askvol3"),
					(long)stockSnapshot.get("askvol4"),
					(long)stockSnapshot.get("askvol5")
			);
			depthMap.put(code, depths[i]);
			i++;
		}
	}

	public Depth[] getDepths () {
		return depths;
	}

	public JSONObject getData () {
		return data;
	}

	public Depth getDepth(String code) {
		return depthMap.get(code);
	}
}