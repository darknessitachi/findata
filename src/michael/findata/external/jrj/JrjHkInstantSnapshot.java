package michael.findata.external.jrj;

import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import com.numericalmethod.algoquant.execution.datatype.product.Product;
import com.numericalmethod.algoquant.execution.datatype.product.stock.Stock;
import com.numericalmethod.algoquant.execution.datatype.product.stock.hkex.HKEXStock;
import michael.findata.algoquant.execution.datatype.depth.Depth;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// HK stocks only
public class JrjHkInstantSnapshot implements MarketCondition {
	protected JSONArray data = null;
	protected Depth[] depths;
	protected HashMap<String, Depth> depthMap;
	private DateTime tick;
	private static LocalTime nine30 = new LocalTime (9, 30);
	private static LocalTime twelve = new LocalTime (12);
	private static LocalTime thirteen = new LocalTime (13);
	private static LocalTime sixteen = new LocalTime (16);

	public JrjHkInstantSnapshot (String ... codes) {
//		String [] neteaseInternalCodes;
//		neteaseInternalCodes = new String [codes.length];
		try {
			// 9:30至中午12:00；下午13:00至下午16:00
			// Outside of these two periods? then it is not traded (used later)
			LocalTime localTime = new DateTime(DateTimeZone.forOffsetHours(8)).toLocalTime();
			boolean inTradingPeriod = (localTime.isAfter(nine30) && localTime.isBefore(twelve)) || (localTime.isAfter(thirteen) && localTime.isBefore(sixteen));

			StringBuilder urlStr = new StringBuilder("http://hkus.hqquery.jrj.com.cn/hkhq.do?tpl=bj&vname=bjData&ids=");
			for (int i = codes.length-1; i > -1; i--) {
//				if (codes[i].startsWith("6") || codes[i].startsWith("9") || codes[i].startsWith("5")) {
//					neteaseInternalCodes[i] = "0" + codes [i];
//				} else {
//					neteaseInternalCodes[i] = "1" + codes [i];
//				}
				urlStr.append(",").append(codes[i]);
			}
			URL url = new URL(urlStr.toString());
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			String s = br.readLine();
			Pattern p = Pattern.compile("\"\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d\"");
			Matcher m =p.matcher(s);
			DateTimeFormatter formatter = DateTimeFormat.forPattern("\"yyyy-MM-dd HH:mm:ss\"");
			long currentMillis = System.currentTimeMillis();
			long eightHours = 8 * 1000 * 60 * 60;
			while (m.find()) {
//				System.out.println(m.group());
				// if last trading was more than 8 hour ago, then this stock is not trading
				String trading;
				if (inTradingPeriod) {
					trading = (currentMillis - formatter.parseDateTime(m.group()).getMillis()) < eightHours ? "1" : "0";
				} else {
					trading = "0";
				}
				s = s.replaceFirst("\"\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d\"", trading);
			}
			s = s.substring(11, s.length())
//					.replaceAll("\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d", "")
					.replace("{", "{\"").replace(",", ",\"").replace(":", "\":")
					.replace(",\"{", ",{");
			data = (JSONArray)((JSONObject) JSONValue.parse(s)).get("StockHq");
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		buildDepths();
	}

	public JrjHkInstantSnapshot (String JSON) {
		data = (JSONArray)((JSONObject) JSONValue.parse(JSON)).get("StockHq");
		buildDepths();
	}

	public Map<String, Depth> getDepthMap () {
		return depthMap;
	}

	private void buildDepths() {
		depthMap = new HashMap<>(); prdDepthMap = new HashMap<>();
//		Long zero = 0L;
		depths = new Depth[data.size()];
		Stock stock;
		String code;
		int i = 0;
		for (Map stockSnapshot : (Collection<Map>) data) {
			code = ((String) stockSnapshot.get("code")).substring(1, 5);
			stock = HKEXStock.newInstance(code+".HK", (String)stockSnapshot.get("name"));
//			if (code.startsWith("6") || code.startsWith("9") || code.startsWith("5")) {
//				stock = new michael.findata.model.Stock(code);
//			} else {
//				stock = new michael.findata.model.Stock(code);
//			}
//			System.out.println(stockSnapshot.get("time"));
			depths[i] = new Depth(
					(double)stockSnapshot.get("np"),
					stock, (Long)stockSnapshot.get("time") == 1,
					(double)stockSnapshot.get("bp5"),
					(double)stockSnapshot.get("bp4"),
					(double)stockSnapshot.get("bp3"),
					(double)stockSnapshot.get("bp2"),
					(double)stockSnapshot.get("bp1"),
					(double)stockSnapshot.get("sp1"),
					(double)stockSnapshot.get("sp2"),
					(double)stockSnapshot.get("sp3"),
					(double)stockSnapshot.get("sp4"),
					(double)stockSnapshot.get("sp5"));
			depths[i].setVols(
					(long)stockSnapshot.get("ba5"),
					(long)stockSnapshot.get("ba4"),
					(long)stockSnapshot.get("ba3"),
					(long)stockSnapshot.get("ba2"),
					(long)stockSnapshot.get("ba1"),
					(long)stockSnapshot.get("sa1"),
					(long)stockSnapshot.get("sa2"),
					(long)stockSnapshot.get("sa3"),
					(long)stockSnapshot.get("sa4"),
					(long)stockSnapshot.get("sa5")
			);
			depthMap.put(code, depths[i]);prdDepthMap.put(depths[i].product(), depths[i]);
			i++;
		}
	}

	public Depth[] getDepths () {
		return depths;
	}

	public JSONArray getData () {
		return data;
	}

	public Depth getDepth (String code) {
		return depthMap.get(code);
	}

	public void purgeData () {
		this.data = null;
	}

	protected HashMap<Product, com.numericalmethod.algoquant.execution.datatype.depth.Depth> prdDepthMap;

	@Override
	public Map<Product, com.numericalmethod.algoquant.execution.datatype.depth.Depth> depths() {
		return Collections.unmodifiableMap(prdDepthMap);
	}

	@Override
	public com.numericalmethod.algoquant.execution.datatype.depth.Depth depth(Product product) {
		return prdDepthMap.get(product);
	}

	/**
	 * Gets another immutable {@linkplain MarketCondition}.
	 *
	 * @param depth an order book update
	 * @return an updated market condition
	 */
	@Override
	public MarketCondition updateDepth(com.numericalmethod.algoquant.execution.datatype.depth.Depth depth) {
		prdDepthMap.put(depth.product(), depth);
		return this;
	}

	public DateTime getTick() {
		return tick;
	}

	public void setTick(DateTime tick) {
		this.tick = tick;
	}
}