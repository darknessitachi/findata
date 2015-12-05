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
import java.util.Map;

/**
 * Created by nicky on 2015/8/18.
 */
public class NeteaseInstantSnapshot {
	protected JSONObject data = null;
	protected String [] codes;
	protected String [] neteaseInternalCodes;
	protected Depth[] depths;
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
		Stock stock;
		for (int i = codes.length-1; i > -1; i--) {
			Map stockSnapshot;
			try {
				stockSnapshot = (Map) data.get(neteaseInternalCodes[i]);
			} catch (NullPointerException npe) {
				break;
			}
			if (codes[i].startsWith("6") || codes[i].startsWith("9")) {
				stock = new SHSEStock((String)stockSnapshot.get("symbol"));
			} else {
				stock = new SZSEStock((String)stockSnapshot.get("symbol"));
			}
			depths[i] = new Depth(stock,
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
		}
	}
	public Depth[] getDepths () {
		return depths;
	}
}