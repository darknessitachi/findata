package michael.findata.external.sina;

import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import com.numericalmethod.algoquant.execution.datatype.product.Product;
import michael.findata.algoquant.execution.datatype.depth.Depth;
import michael.findata.model.Stock;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HK stocks only
 * Request:
 * http://hq.sinajs.cn/?list=rt_hk01288,rt_hk00914,rt_hk02039
 *
 * Response: (var hq_str_rt_hk01288="eng_name,chi_name,open,yest_close,high,low,current,%change,$change,bid1,ask1,xxx,xxx,xxx,xxx,xxx,xxx,time,...)
 * var hq_str_rt_hk01288="ABC,农业银行,3.360,3.370,3.360,3.260,3.270,-0.100,-2.967,3.260,3.270,461815251.140,140581234,5.112,5.107,3.480,2.500,2016/10/12,16:08:15,100|0,N|Y|Y,3.270|3.170|3.430,0|||0.000|0.000|0.000, |0,Y";
 * var hq_str_rt_hk00914="ANHUI CONCH,安徽海螺水泥股份,21.400,21.650,21.400,20.350,20.600,-1.050,-4.850,20.600,20.650,585173762.500,28284000,15.426,2.087,25.500,13.700,2016/10/12,16:08:15,100|0,N|Y|Y,20.700|19.840|21.350,0|||0.000|0.000|0.000, |0,Y";
 * var hq_str_rt_hk02039="CIMC,中集集团 ,9.290,9.280,9.420,9.150,9.350,0.070,0.754,9.350,9.370,12718458.000,1374500,314.267,2.353,15.220,8.600,2016/10/12,16:08:16,100|0,N|N|Y,9.370|9.170|9.350,0|||0.000|0.000|0.000, |0,Y";
 */
public class SinaHkInstantSnapshot implements MarketCondition {
//	protected JSONArray data = null;
	protected Depth[] depths;
	protected HashMap<String, Depth> depthMap;
	private DateTime tick;
	private static LocalTime nine30 = new LocalTime (9, 30);
	private static LocalTime twelve = new LocalTime (12, 0);
	private static LocalTime thirteen = new LocalTime (13, 0);
	private static LocalTime sixteen = new LocalTime (16, 0);
	private static long hardCodedVol = 50000;

	public SinaHkInstantSnapshot (String ... codes) {
		depths = new Depth[codes.length];
		depthMap = new HashMap<>(); prdDepthMap = new HashMap<>();
		try {
			// 9:30至中午12:00；下午13:00至下午16:00
			// Outside of these two periods? then it is not traded (used later)
			LocalTime localTime = new DateTime(DateTimeZone.forOffsetHours(8)).toLocalTime();
			boolean inTradingPeriod = (localTime.isAfter(nine30) && localTime.isBefore(twelve)) || (localTime.isAfter(thirteen) && localTime.isBefore(sixteen));

			StringBuilder urlStr = new StringBuilder("http://hq.sinajs.cn/?list=");
			for (String code1 : codes) {
				urlStr.append(",rt_hk").append(code1);
			}
			URL url = new URL(urlStr.toString());
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			String s;
			Pattern p = Pattern.compile("var hq_str_rt_hk(\\d\\d\\d\\d\\d)=\".+,(.+),([\\d\\.]+,){4}([\\d\\.\\-]+),([\\d\\.\\-]+,){2}([\\d\\.]+),([\\d\\.]+),([\\d\\.]+,){6}(\\d\\d\\d\\d/\\d\\d/\\d\\d,\\d\\d:\\d\\d:\\d\\d).+");
			DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy/MM/dd,HH:mm:ss");
			int i = 0;
			Stock stock;
			long currentMillis = System.currentTimeMillis();
			long eightHours = 8 * 1000 * 60 * 60;
			while ((s = br.readLine()) != null) {
				Matcher m =p.matcher(s);
				if (m.find()) {
//				System.out.println(m.group());
					// group 1: code
					String code = m.group(1);
					// group 2: chinese name
					String name = m.group(2);
					// group 4: latest price
					String latest = m.group(4);
					// group 6: bid1
					String bid1 = m.group(6);
					// group 7: ask1
					String ask1 = m.group(7);
					// group 9: time
					String time = m.group(9);

					// if last trading was more than 8 hour ago, then this stock is not trading
					boolean trading;
					if (inTradingPeriod) {
						trading = (currentMillis - formatter.parseDateTime(time).getMillis()) < eightHours;
					} else {
						trading = false;
					}
					stock = new Stock(code, name);
//					code = code.substring(1, 5);
//					stock = HKEXStock.newInstance(code+".HK", name);
					depths[i] = new Depth(
							Double.parseDouble(latest),
							stock, trading,
							Double.parseDouble(bid1),
							Double.parseDouble(ask1));
					depths[i].setVols(hardCodedVol, hardCodedVol); //hard-coded volume
					depthMap.put(code, depths[i]);
					prdDepthMap.put(depths[i].product(), depths[i]);
					i++;
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	public SinaHkInstantSnapshot (String JSON) {
//		data = (JSONArray)((JSONObject) JSONValue.parse(JSON)).get("StockHq");
//		buildDepths();
//	}

	public Map<String, Depth> getDepthMap () {
		return depthMap;
	}

	public Depth[] getDepths () {
		return depths;
	}

//	public JSONArray getData () {
//		return data;
//	}

	public Depth getDepth (String code) {
		return depthMap.get(code);
	}

//	public void purgeData () {
//		this.data = null;
//	}

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
	}}
