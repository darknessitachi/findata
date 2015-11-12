package michael.findata.external.netease;

import com.numericalmethod.algoquant.execution.datatype.depth.Depth;
import michael.findata.algoquant.product.stock.shse.SHSEStock;
import michael.findata.algoquant.product.stock.szse.SZSEStock;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.Random;

/**
 * Created by nicky on 2015/8/18.
 */
public class NeteaseInstantSnapshot {
	protected JSONObject data = null;
	protected String [] codes;
	protected String [] neteaseInternalCodes;
	protected Depth [] depths;
	public NeteaseInstantSnapshot (String ... codes) {
		this.codes = codes;
		neteaseInternalCodes = new String [codes.length];
		try {
			StringBuffer urlStr = new StringBuffer("http://api.money.163.com/data/feed/");
			for (int i = codes.length-1; i > -1; i--) {
				if (codes[i].startsWith("6") || codes[i].startsWith("9")) {
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
		}
		depths = new Depth[codes.length];
		for (int i = codes.length-1; i > -1; i--) {
			Map stockSnapshot;
			try {
				stockSnapshot = (Map) data.get(neteaseInternalCodes[i]);
			} catch (NullPointerException npe) {
				break;
			}
			if (codes[i].startsWith("6") || codes[i].startsWith("9")) {
				depths[i] = new Depth(new SHSEStock((String)stockSnapshot.get("symbol")),
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
			} else {
				depths[i] = new Depth(new SZSEStock((String)stockSnapshot.get("symbol")),
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
			}
		}
	}
	public Depth[] getDepths () {
		return depths;
	}
}