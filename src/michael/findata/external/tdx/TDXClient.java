package michael.findata.external.tdx;

import com.numericalmethod.algoquant.execution.datatype.product.Product;
import com.sun.jna.Native;
import com.sun.jna.ptr.ShortByReference;
import michael.findata.algoquant.execution.datatype.depth.Depth;
import michael.findata.external.SecurityTimeSeriesData;
import michael.findata.external.SecurityTimeSeriesDatum;
import michael.findata.model.Stock;
import michael.findata.util.CalendarUtil;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.math.BigDecimal;
import java.util.*;

public class TDXClient {
	TDXLibrary[] hqLib;
	String [] ip;
	String [] name;
	boolean [] fault;
	int [] port;
	byte[] result = new byte[65535];
	byte[] errInfo = new byte[256];
	boolean connected;
	private static final short OHLC_QUERY_LIMIT = 800;
//	private static final short EOM_QUERY_LIMIT = 720; // 3 days
	public static final String [] TDXClientConfigs = {
			"221.236.13.218:7709",    // 招商证券成都行情 - 9-33 ms
			"221.236.13.219:7709",    // 招商证券成都行情 - 8-25 ms
			"125.71.28.133:7709",    // cd1010 - 8 ms
			"221.236.15.14:995",    // 国金成都电信2.1
			"124.161.97.84:7709",    // 申银万国成都网通2
			"124.161.97.83:7709",    // 申银万国成都网通1
			"125.64.39.62:7709",    // 申银万国成都电信2
			"125.71.28.133:443",    // cd1010 - 8 ms
			"221.236.15.14:7709",    // 国金成都电信1.13
			"119.6.204.139:7709",    // 国金成都联通5.135
			"125.64.39.61:7709",    // 申银万国成都电信1
			"125.64.41.12:7709",    // 成都电信54
			"119.4.167.141:7709",    // 华西L1
			"119.4.167.142:7709",    // 华西L2
			"119.4.167.181:7709",    // 华西L3
			"119.4.167.182:7709",    // 华西L4
			"119.4.167.164:7709",    // 华西L5
			"119.4.167.165:7709",    // 华西L6
			"119.4.167.163:7709",    // 华西L7
			"119.4.167.175:7709",    // 华西L8
			"218.6.198.151:7709",    // 华西L1
			"218.6.198.152:7709",    // 华西L2
			"218.6.198.174:7709",    // 华西L3
			"218.6.198.175:7709",    // 华西L4
			"218.6.198.155:7709",    // 华西L5
			"218.6.198.156:7709",    // 华西L6
			"218.6.198.157:7709",    // 华西L7
			"218.6.198.158:7709",    // 华西L8
			"182.131.7.141:7709",    // 华西E1
			"182.131.7.142:7709",    // 华西E2
			"182.131.7.143:7709",    // 华西E3
			"182.131.7.144:7709",    // 华西E4
			"182.131.7.145:7709",    // 华西E5
			"182.131.7.146:7709",    // 华西E6
			"182.131.7.147:7709",    // 华西E7
			"182.131.7.148:7709",    // 华西E8
			"182.131.3.245:7709",    // 上证云行情J330 - 9ms)
			"221.237.158.106:7709",    // 西南证券金点子成都电信主站1
			"221.237.158.107:7709",    // 西南证券金点子成都电信主站2
			"221.237.158.108:7709",    // 西南证券金点子成都电信主站3
			"183.230.9.136:7709",    // 西南证券金点子重庆移动主站1
			"183.230.134.6:7709",    // 西南证券金点子重庆移动主站2
			"219.153.1.115:7709",    // 西南证券金点子重庆电信主站1
			"113.207.29.12:7709"    // 西南证券金点子重庆联通主站1
	};

//	String testdata [] = new String [] {
//	"市场\t代码\t活跃度\t现价\t昨收\t开盘\t最高\t最低\t时间\t保留\t总量\t现量\t总金额\t内盘\t外盘\t保留\t保留\t买一价\t卖一价\t买一量\t卖一量\t买二价\t卖二价\t买二量\t卖二量\t买三价\t卖三价\t买三量\t卖三量\t买四价\t卖四价\t买四量\t卖四量\t买五价\t卖五价\t买五量\t卖五量\t保留\t保留\t保留\t保留\t保留\t涨速\t活跃度\n" +
//	"0\t000568\t0\t0.000000\t23.500000\t0.000000\t0.000000\t0.000000\t15023600\t0\t0\t0\t0.000000\t0\t0\t0\t2237\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0\t0\t0\t0\t0\t0.000000\t0\n" +
//	"1\t600026\t1799\t6.170000\t6.130000\t6.130000\t6.200000\t6.070000\t14999983\t-617\t120609\t161\t74034344.000000\t62997\t57612\t-1\t0\t6.170000\t6.180000\t308\t957\t6.160000\t6.190000\t1302\t1634\t6.150000\t6.200000\t2009\t1728\t6.140000\t6.210000\t2277\t700\t6.130000\t6.220000\t1124\t1758\t2681\t1\t-11\t-17\t4\t0.000000\t1799\n",
//
//	"市场\t代码\t活跃度\t现价\t昨收\t开盘\t最高\t最低\t时间\t保留\t总量\t现量\t总金额\t内盘\t外盘\t保留\t保留\t买一价\t卖一价\t买一量\t卖一量\t买二价\t卖二价\t买二量\t卖二量\t买三价\t卖三价\t买三量\t卖三量\t买四价\t卖四价\t买四量\t卖四量\t买五价\t卖五价\t买五量\t卖五量\t保留\t保留\t保留\t保留\t保留\t涨速\t活跃度\n" +
//	"0\t000568\t0\t0.000000\t23.500000\t0.000000\t0.000000\t0.000000\t15023600\t0\t0\t0\t0.000000\t0\t0\t0\t2237\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0\t0\t0\t0\t0\t0.000000\t0\n" +
//	"1\t600026\t1799\t6.170000\t6.130000\t6.130000\t6.200000\t6.070000\t14999983\t-617\t120609\t161\t74034344.000000\t62997\t57612\t-1\t0\t6.170000\t6.180000\t308\t957\t6.160000\t6.190000\t1302\t1634\t6.150000\t6.200000\t2009\t1728\t6.140000\t6.210000\t2277\t700\t6.130000\t6.220000\t1124\t1758\t2681\t1\t-11\t-17\t4\t0.000000\t1799\n",
//
//	"市场\t代码\t活跃度\t现价\t昨收\t开盘\t最高\t最低\t时间\t保留\t总量\t现量\t总金额\t内盘\t外盘\t保留\t保留\t买一价\t卖一价\t买一量\t卖一量\t买二价\t卖二价\t买二量\t卖二量\t买三价\t卖三价\t买三量\t卖三量\t买四价\t卖四价\t买四量\t卖四量\t买五价\t卖五价\t买五量\t卖五量\t保留\t保留\t保留\t保留\t保留\t涨速\t活跃度\n" +
//	"0\t000568\t0\t0.000000\t23.500000\t0.000000\t0.000000\t0.000000\t15023600\t0\t0\t0\t0.000000\t0\t0\t0\t2237\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0\t0\t0\t0\t0\t0.000000\t0\n" +
//	"1\t600026\t1799\t6.170000\t6.130000\t6.130000\t6.200000\t6.070000\t14999983\t-617\t120609\t161\t74034344.000000\t62997\t57612\t-1\t0\t6.170000\t6.180000\t308\t957\t6.160000\t6.190000\t1302\t1634\t6.150000\t6.200000\t2009\t1728\t6.140000\t6.210000\t2277\t700\t6.130000\t6.220000\t1124\t1759\t2681\t1\t-11\t-17\t4\t0.000000\t1799\n",
//
//	"市场\t代码\t活跃度\t现价\t昨收\t开盘\t最高\t最低\t时间\t保留\t总量\t现量\t总金额\t内盘\t外盘\t保留\t保留\t买一价\t卖一价\t买一量\t卖一量\t买二价\t卖二价\t买二量\t卖二量\t买三价\t卖三价\t买三量\t卖三量\t买四价\t卖四价\t买四量\t卖四量\t买五价\t卖五价\t买五量\t卖五量\t保留\t保留\t保留\t保留\t保留\t涨速\t活跃度\n" +
//	"0\t000568\t0\t0.000000\t23.500000\t0.000000\t0.000000\t0.000000\t15023600\t0\t0\t0\t0.000000\t0\t0\t0\t2237\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0\t0\t0\t0\t0\t0.000000\t0\n" +
//	"1\t600026\t1799\t6.170000\t6.130000\t6.130000\t6.200000\t6.070000\t14999984\t-617\t120609\t161\t74034344.000000\t62997\t57612\t-1\t0\t6.170000\t6.180000\t308\t957\t6.160000\t6.190000\t1302\t1634\t6.150000\t6.200000\t2009\t1728\t6.140000\t6.210000\t2277\t700\t6.130000\t6.220000\t1124\t1758\t2681\t1\t-11\t-17\t4\t0.000000\t1799\n"
//	};

	//	boolean updated = false;
	// serverConfig is in the format of "ip:port", eg. "182.131.3.245:7709"
	public TDXClient(String ... serverConfig) {
		hqLib = new TDXLibrary[serverConfig.length];
		ip = new String [serverConfig.length];
		port = new int [serverConfig.length];
		name = new String [serverConfig.length];
		fault = new boolean[serverConfig.length];
		String [] temp;
		for (int i = serverConfig.length - 1; i > -1; i--) {
			hqLib[i] = (TDXLibrary) Native.loadLibrary("TdxHqApi", TDXLibrary.class);
			temp = serverConfig[i].split(":");
			ip[i] = temp[0];
			port[i] = Integer.parseInt(temp[1]);
		}
		connected = false;
	}

	public boolean isConnected() {
		return connected;
	}

	public void noOp () {
		System.out.println("Dummy noop "+name[0]);
	}

	public boolean connect () {
		boolean success = false;
		for (int i = hqLib.length - 1; i > -1; i--) {
			if (hqLib[i].TdxHq_Connect(ip[i], port[i], result, errInfo)) {
				name[i] = Native.toString(result, "GBK").split("\n")[1].split("\t")[0];
				System.out.println(name[i]+" is now connected.");
				success = true;
				fault[i] = false;
			} else {
				System.out.println(ip[i]+":"+port[i]+" is not working.");
				System.out.println(Native.toString(errInfo, "GBK"));
				fault[i] = true;
//				success = false;
			}
		}
		connected = success;
		return success;
	}

	public void disconnect () {
		for (TDXLibrary lib : hqLib) {
			lib.TdxHq_Disconnect();
		}
		connected = false;
		StringBuilder sb = new StringBuilder();
		sb.append("Summary: \n");
		for (int i = 0; i < name.length; i++) {
			sb.append(ip[i]).append(":").append(port[i]).append(fault[i] ? " fault - " : " worked - ").append(name[i]).append("\n");
		}
		System.out.println(sb.toString());
	}

	String [] bulkTemp = null;
	String raw = null;

	// Repetitively poll quotes up to a preset number of times, until count is up or until any update is detected.
	// this is a much resource-intensive way of polling quotes, given SH and SZ exchanges are only publishing snapshots
	// every 5/3 seconds respectively. In other words, there is no point polling from SH for another 4.5/2.5 seconds if
	// SH/SZ has just published a snapshot.
	public Map<Product, com.numericalmethod.algoquant.execution.datatype.depth.Depth> pollQuotes (
			int pollTimes, long gapMillis, Map<String, Stock> stockMap, String... codes) {
//		System.out.println("Polling ... @\t"+System.currentTimeMillis());
		int count = -1;
//		long start;
		boolean success;
		HashMap<Product, com.numericalmethod.algoquant.execution.datatype.depth.Depth> depthMap = new HashMap<>();

		byte[] market = calcMarkets(codes);

		ShortByReference resultCount = new ShortByReference();
		resultCount.setValue((short) codes.length);

		String [] bulkLastTemp = bulkTemp;
		String rawTemp = raw;
//		String [] stockTemp;

		int index;
		boolean updated = false; // marks whether data has been updated from server
//		long start = System.currentTimeMillis();
		while ((rawTemp == null || !updated) && count < pollTimes) {
			if (gapMillis > 0) {
				try {
//					System.out.println("Heartbeat completed, sleeping ...");
					Thread.sleep(gapMillis);
				} catch (InterruptedException e) {
					System.out.println("Interrupt.");
				}
			}
			count ++;
			index = count%hqLib.length;

			if (fault[index]) continue;

//			System.out.println(name[index]+" Starting @ "+(System.currentTimeMillis()));
			success = hqLib[index].TdxHq_GetSecurityQuotes(market, codes, resultCount, result, errInfo);
//			System.out.println("Time taken(ms) for this round: "+(System.currentTimeMillis() - start));
			if (!success) {
				System.out.println(ip[index]+":"+port[index]+" is not working.");
				System.out.println(Native.toString(errInfo, "GBK"));
				fault[index] = true;
			} else {
//				System.out.println(count+"---------------");
				rawTemp = raw;
				raw = Native.toString(result, "GBK");
//				System.out.println(raw);
//				raw = testdata[count]; // todo test only
				if (raw.equals(rawTemp)) {
//					System.out.println("not updated!");
					continue;
				}
				bulkLastTemp = bulkTemp;
				bulkTemp = raw.split("[\n\t]");
				int end = bulkTemp.length - 43;
				for (int i = 44; i < end; i += 44) {
					// todo: can we use timestamp as a flag for update?
					if (bulkLastTemp != null) {
						// update check with timestamp
						if (bulkLastTemp[i+8].equals(bulkTemp[i+8])) {// if timestamp is not changed, does this mean there shouldn't be any change, since there is no update at all?
//							System.out.println("Timestamp no change for: "+bulkTemp[i+1]);
						} else { // if the timestamps has changed, this line of data must have been updated.
//							System.out.println("Timestamp change for: "+bulkTemp[i+1]+"\t"+bulkLastTemp[i+8]+"->"+bulkTemp[i+8]);
							updated = true;
						}
						// change check 1
						if (bulkLastTemp[i+3].equals(bulkTemp[i+3])) { // if current price for this tick is the same as it was last tick.
							boolean changed = false;
							for (int j = 17; j <= 36; j++) {
								if (!bulkLastTemp[i+j].equals(bulkTemp[i+j])) {
									// if any part of depth for this tick is not the same as it was last tick,
									// this means the depth of this stock has indeed changed, thus needs to be sent out
									// to strategies
//									System.out.println("Depth change for: "+bulkTemp[i+1]+" @\t"+System.currentTimeMillis());
									updated = true;
									changed = true;
									break;
								}
							}
							if (!changed) {
								// if there is no change continue with the next stock in the same batch
//								System.out.println("Depth no change for: "+bulkTemp[i+1]);
								continue;
							}
						} else {
//							System.out.println("Depth change for: "+bulkTemp[i+1]+" @\t"+System.currentTimeMillis());
							updated = true;
						}
					} else {
						// first time
//						System.out.println("First time for "+bulkTemp[i+1]+" @\t"+System.currentTimeMillis());
						updated = true;
					}
					Depth stockDepth = new Depth(
							new BigDecimal(bulkTemp[i+3]).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue(), stockMap.get(bulkTemp[i+1]), true,
							new BigDecimal(bulkTemp[i+33]).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue(),
							new BigDecimal(bulkTemp[i+29]).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue(),
							new BigDecimal(bulkTemp[i+25]).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue(),
							new BigDecimal(bulkTemp[i+21]).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue(),
							new BigDecimal(bulkTemp[i+17]).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue(),
							new BigDecimal(bulkTemp[i+18]).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue(),
							new BigDecimal(bulkTemp[i+22]).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue(),
							new BigDecimal(bulkTemp[i+26]).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue(),
							new BigDecimal(bulkTemp[i+30]).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue(),
							new BigDecimal(bulkTemp[i+34]).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue()
					);
					stockDepth.setVols(
							Integer.parseInt(bulkTemp[i+35])*100,
							Integer.parseInt(bulkTemp[i+31])*100,
							Integer.parseInt(bulkTemp[i+27])*100,
							Integer.parseInt(bulkTemp[i+23])*100,
							Integer.parseInt(bulkTemp[i+19])*100,
							Integer.parseInt(bulkTemp[i+20])*100,
							Integer.parseInt(bulkTemp[i+24])*100,
							Integer.parseInt(bulkTemp[i+28])*100,
							Integer.parseInt(bulkTemp[i+32])*100,
							Integer.parseInt(bulkTemp[i+36])*100
					);
//					System.out.println(name[index]+"/"+bulkTemp[i+1]+" updated@"+(new Date()));
					depthMap.put(stockDepth.product(), stockDepth);
				}
//System.out.println("A @\t"+System.currentTimeMillis());
			}
//System.out.println("B @\t"+System.currentTimeMillis());
		}
//System.out.println("C @\t"+System.currentTimeMillis());
//		System.out.println(codes[0]+": Time taken(ms) for this round: "+(System.currentTimeMillis() - start));
//		System.out.println(depthMap.get(codes[1]));
		return depthMap;
	}

	private byte[] calcMarkets(String[] codes) {
		byte[] market = new byte[codes.length];
		for (int i = codes.length -1; i > -1; i--) {
			market[i]=calcMarket(codes[i]);
		}
		return market;
	}

	private byte calcMarket(String code) {
		switch (code.charAt(0)) {
			case '5':
			case '6':
			case '9':
				return 1;
			default:
				return 0;
		}
	}

	public SecurityTimeSeriesData getEODs (String code, DateTime earliest) {
		short days = (short) (10 + CalendarUtil.daysBetween(earliest, new DateTime()) * 270 / 365);
		return getEODs(code, days);
	}

	public SecurityTimeSeriesData getEODs (String code) {
		return getEODs(code, OHLC_QUERY_LIMIT);
	}

	private SecurityTimeSeriesData getEODs(String code, short daysCount) {
		ShortByReference count = new ShortByReference(daysCount > OHLC_QUERY_LIMIT ? OHLC_QUERY_LIMIT : daysCount);
		if (hqLib[0].TdxHq_GetSecurityBars((byte)9, calcMarket(code), code, (short)(0), count, result, errInfo)) {
			return new TDXClientPriceHistory(Native.toString(result, "GBK"), true);
		} else {
			return null;
		}
	}

	public SecurityTimeSeriesData getEOMs(String code, short startOnDay, short daysCount) {
		short countPerDay = 240;
		short daysPerQuery = (short)(OHLC_QUERY_LIMIT / countPerDay);
		short countPerQuery = (short)(daysPerQuery * countPerDay);
		short queryCount = (short)(daysCount / daysPerQuery + (daysCount % daysPerQuery == 0 ? 0 : 1));
		String resultString = "";

		for (short i = 0; i < queryCount; i++) {
			ShortByReference count;
			if (i + 1 == queryCount) {
				count = new ShortByReference((short)((daysCount + daysPerQuery - (i + 1) * daysPerQuery) * countPerDay));
			} else {
				count = new ShortByReference(countPerQuery);
			}
//			System.out.println("i: "+i);
//			System.out.println("count: "+count.getValue());
			if (hqLib[i % hqLib.length].TdxHq_GetSecurityBars((byte)8, calcMarket(code), code, (short)((i * countPerQuery)+(countPerDay*startOnDay)), count, result, errInfo)) {
				if (i == queryCount - 1) {
					resultString = Native.toString(result, "GBK") + "\n" + resultString;
				} else {
					String temp = Native.toString(result, "GBK");
					temp = temp.substring(temp.indexOf('\n')+1, temp.length());
					resultString = temp + "\n"+ resultString ;
				}
			} else {
				return null;
			}
		}
//		System.out.println("----");
//		System.out.println(resultString);
//		System.out.println("----");
		return new TDXClientPriceHistory(resultString, false);
	}

	public SecurityTimeSeriesData getEOMs(String code, short daysCount) {
		short countPerDay = 240;
		short daysPerQuery = (short)(OHLC_QUERY_LIMIT / countPerDay);
		short countPerQuery = (short)(daysPerQuery * countPerDay);
		short queryCount = (short)(daysCount / daysPerQuery + (daysCount % daysPerQuery == 0 ? 0 : 1));
		String resultString = "";

		for (short i = 0; i < queryCount; i++) {
			ShortByReference count;
			if (i + 1 == queryCount) {
				count = new ShortByReference((short)((daysCount + daysPerQuery - (i + 1) * daysPerQuery) * countPerDay));
			} else {
				count = new ShortByReference(countPerQuery);
			}
			if (hqLib[i % hqLib.length].TdxHq_GetSecurityBars((byte)7, calcMarket(code), code, (short)(i * countPerQuery), count, result, errInfo)) {
				if (i == queryCount - 1) {
					resultString = Native.toString(result, "GBK") + "\n" + resultString;
				} else {
					String temp = Native.toString(result, "GBK");
					temp = temp.substring(temp.indexOf('\n')+1, temp.length());
					resultString = temp + "\n"+ resultString ;
				}
			} else {
				return null;
			}
		}
		System.out.println("----");
		System.out.println(resultString);
		System.out.println("----");
		return new TDXClientPriceHistory(resultString, false);
	}

	public void getHistoryTransactionData (String code) {
		ShortByReference count = new ShortByReference();
		if (hqLib[0].TdxHq_GetHistoryTransactionData(calcMarket(code), code, (short)20, count, 20160427, result, errInfo)) {
			System.out.println(Native.toString(result, "GBK"));
		} else {
			System.out.println(Native.toString(errInfo, "GBK"));
		}
	}

	public Stack<String[]> getXDXRInfo (String ... codes) {
		byte [] market = calcMarkets(codes);
		boolean success;
		Stack<String[]> res = new Stack<>();
		for (int i = codes.length - 1; i > -1; i --) {
			success = hqLib[i%hqLib.length].TdxHq_GetXDXRInfo(market[i], codes[i], result, errInfo);
			if (!success) {
				System.out.println(ip[i%hqLib.length]+":"+port[i%hqLib.length]+" is not working.");
				System.out.println(Native.toString(errInfo, "GBK"));
				return null;
			} else {
				String [] lines = Native.toString(result, "GBK").split("\n");
				String [] data;
//				System.out.println(lines[0]);
				for (int j = 1; j < lines.length; j++ ) {
//					System.out.println(lines[j]);
					data = lines[j].split("\t");
					//  1: 轉送股或紅利
					// 11: 類似基金的合並或分拆
					// 復權方法不一樣
					// 若11：復權方法為簡單地將股價除以比例
					if (data[3].equals("1") || data[3].equals("11")) {
						res.push(data);
					}
				}
			}
		}
		return res;
	}

//	private static String testData =
//					"时间\t开盘价\t收盘价\t最高价\t最低价\t成交量\t成交额\n" +
//					"20130313\t30.800000\t30.740000\t31.110000\t30.620000\t53917\t166228672.000000\n" +
//					"20130314\t30.780000\t30.280000\t30.870000\t30.240000\t92563\t282208800.000000\n" +
//					"20130315\t30.250000\t29.350000\t30.250000\t29.300000\t141922\t420919552.000000\n" +
//					"20130318\t29.100000\t27.590000\t29.490000\t27.480000\t197692\t554878784.000000\n" +
//					"20130319\t27.500000\t27.260000\t27.590000\t26.700000\t164236\t445620736.000000\n" +
//					"20130320\t27.230000\t27.920000\t28.200000\t27.100000\t128528\t355753760.000000\n" +
//					"20130321\t27.800000\t27.950000\t28.270000\t27.440000\t111634\t311196320.000000\n";

	private class TDXClientPriceHistory implements SecurityTimeSeriesData {

		private String [] data;

		private int current;

		private SecurityTimeSeriesDatum buffer = null;

		private DateTimeFormatter formatterDay = DateTimeFormat.forPattern("yyyyMMdd");

		private DateTimeFormatter formatterMinute = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

		boolean dayOrMinute;

		// true: day | false: minute
		private TDXClientPriceHistory (String data, boolean dayOrMinute) {
			this.dayOrMinute = dayOrMinute;
			this.data = data.split("\n");
			this.current = this.data.length;
		}

		private SecurityTimeSeriesDatum getNext () {
//			DateTimeFormatter formatter = dayOrMinute ? formatterDay : formatterMinute;
			this.current --;
			if (current > 0) {
				String[] datum = data[current].split("\t");
				return new SecurityTimeSeriesDatum(
						dayOrMinute ? formatterDay.parseDateTime(datum[0]).plusHours(15).plusMinutes(2) : formatterMinute.parseDateTime(datum[0]),
						(int)(Double.parseDouble(datum[1])*1000),
						(int)(Double.parseDouble(datum[3])*1000),
						(int)(Double.parseDouble(datum[4])*1000),
						(int)(Double.parseDouble(datum[2])*1000),
						dayOrMinute ? Integer.parseInt(datum[5])*100 : Integer.parseInt(datum[5]),
						(float)Double.parseDouble(datum[6]));
			} else {
				return null;
			}
		}

		@Override
		public SecurityTimeSeriesDatum popNext() {
			if (buffer != null) {
				SecurityTimeSeriesDatum temp = buffer;
				buffer = null;
				return temp;
			} else {
				return getNext();
			}
		}

		@Override
		public SecurityTimeSeriesDatum peekNext() {
			if (buffer == null) {
				buffer = getNext();
			}
			return buffer;
		}

		@Override
		public void close() {
			current = data.length;
			data = null;
		}

		@Override
		public boolean hasNext() {
			return peekNext() != null;
		}
	}
}