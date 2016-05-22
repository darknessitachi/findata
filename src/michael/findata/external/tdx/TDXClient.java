package michael.findata.external.tdx;

import com.numericalmethod.algoquant.execution.datatype.product.Product;
import com.sun.jna.Native;
import com.sun.jna.ptr.ShortByReference;
import michael.findata.algoquant.execution.datatype.depth.Depth;
import michael.findata.model.Stock;

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

	String testdata [] = new String [] {
	"市场\t代码\t活跃度\t现价\t昨收\t开盘\t最高\t最低\t时间\t保留\t总量\t现量\t总金额\t内盘\t外盘\t保留\t保留\t买一价\t卖一价\t买一量\t卖一量\t买二价\t卖二价\t买二量\t卖二量\t买三价\t卖三价\t买三量\t卖三量\t买四价\t卖四价\t买四量\t卖四量\t买五价\t卖五价\t买五量\t卖五量\t保留\t保留\t保留\t保留\t保留\t涨速\t活跃度\n" +
	"0\t000568\t0\t0.000000\t23.500000\t0.000000\t0.000000\t0.000000\t15023600\t0\t0\t0\t0.000000\t0\t0\t0\t2237\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0\t0\t0\t0\t0\t0.000000\t0\n" +
	"1\t600026\t1799\t6.170000\t6.130000\t6.130000\t6.200000\t6.070000\t14999983\t-617\t120609\t161\t74034344.000000\t62997\t57612\t-1\t0\t6.170000\t6.180000\t308\t957\t6.160000\t6.190000\t1302\t1634\t6.150000\t6.200000\t2009\t1728\t6.140000\t6.210000\t2277\t700\t6.130000\t6.220000\t1124\t1758\t2681\t1\t-11\t-17\t4\t0.000000\t1799\n",

	"市场\t代码\t活跃度\t现价\t昨收\t开盘\t最高\t最低\t时间\t保留\t总量\t现量\t总金额\t内盘\t外盘\t保留\t保留\t买一价\t卖一价\t买一量\t卖一量\t买二价\t卖二价\t买二量\t卖二量\t买三价\t卖三价\t买三量\t卖三量\t买四价\t卖四价\t买四量\t卖四量\t买五价\t卖五价\t买五量\t卖五量\t保留\t保留\t保留\t保留\t保留\t涨速\t活跃度\n" +
	"0\t000568\t0\t0.000000\t23.500000\t0.000000\t0.000000\t0.000000\t15023600\t0\t0\t0\t0.000000\t0\t0\t0\t2237\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0\t0\t0\t0\t0\t0.000000\t0\n" +
	"1\t600026\t1799\t6.170000\t6.130000\t6.130000\t6.200000\t6.070000\t14999983\t-617\t120609\t161\t74034344.000000\t62997\t57612\t-1\t0\t6.170000\t6.180000\t308\t957\t6.160000\t6.190000\t1302\t1634\t6.150000\t6.200000\t2009\t1728\t6.140000\t6.210000\t2277\t700\t6.130000\t6.220000\t1124\t1758\t2681\t1\t-11\t-17\t4\t0.000000\t1799\n",

	"市场\t代码\t活跃度\t现价\t昨收\t开盘\t最高\t最低\t时间\t保留\t总量\t现量\t总金额\t内盘\t外盘\t保留\t保留\t买一价\t卖一价\t买一量\t卖一量\t买二价\t卖二价\t买二量\t卖二量\t买三价\t卖三价\t买三量\t卖三量\t买四价\t卖四价\t买四量\t卖四量\t买五价\t卖五价\t买五量\t卖五量\t保留\t保留\t保留\t保留\t保留\t涨速\t活跃度\n" +
	"0\t000568\t0\t0.000000\t23.500000\t0.000000\t0.000000\t0.000000\t15023600\t0\t0\t0\t0.000000\t0\t0\t0\t2237\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0\t0\t0\t0\t0\t0.000000\t0\n" +
	"1\t600026\t1799\t6.170000\t6.130000\t6.130000\t6.200000\t6.070000\t14999983\t-617\t120609\t161\t74034344.000000\t62997\t57612\t-1\t0\t6.170000\t6.180000\t308\t957\t6.160000\t6.190000\t1302\t1634\t6.150000\t6.200000\t2009\t1728\t6.140000\t6.210000\t2277\t700\t6.130000\t6.220000\t1124\t1759\t2681\t1\t-11\t-17\t4\t0.000000\t1799\n",

	"市场\t代码\t活跃度\t现价\t昨收\t开盘\t最高\t最低\t时间\t保留\t总量\t现量\t总金额\t内盘\t外盘\t保留\t保留\t买一价\t卖一价\t买一量\t卖一量\t买二价\t卖二价\t买二量\t卖二量\t买三价\t卖三价\t买三量\t卖三量\t买四价\t卖四价\t买四量\t卖四量\t买五价\t卖五价\t买五量\t卖五量\t保留\t保留\t保留\t保留\t保留\t涨速\t活跃度\n" +
	"0\t000568\t0\t0.000000\t23.500000\t0.000000\t0.000000\t0.000000\t15023600\t0\t0\t0\t0.000000\t0\t0\t0\t2237\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0.000000\t0.000000\t0\t0\t0\t0\t0\t0\t0\t0.000000\t0\n" +
	"1\t600026\t1799\t6.170000\t6.130000\t6.130000\t6.200000\t6.070000\t14999984\t-617\t120609\t161\t74034344.000000\t62997\t57612\t-1\t0\t6.170000\t6.180000\t308\t957\t6.160000\t6.190000\t1302\t1634\t6.150000\t6.200000\t2009\t1728\t6.140000\t6.210000\t2277\t700\t6.130000\t6.220000\t1124\t1758\t2681\t1\t-11\t-17\t4\t0.000000\t1799\n"
	};

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
	public Map<Product, com.numericalmethod.algoquant.execution.datatype.depth.Depth> pollQuotes (int pollTimes, long gapMillis, Map<String, Stock> stockMap, String... codes) {
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
				for (int j = 1; j < lines.length; j++ ) {
					data = lines[j].split("\t");
					if (data[3].equals("1") || data[3].equals("11")) {
						res.push(data);
					}
				}
			}
		}
		return res;
	}
}