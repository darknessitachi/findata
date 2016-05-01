package michael.findata.external.tdx;

import com.sun.jna.Native;
import com.sun.jna.ptr.ShortByReference;
import michael.findata.algoquant.execution.datatype.depth.Depth;

import java.util.HashMap;
import java.util.HashSet;

public class TDXClient {
	TDXLibrary[] hqLib;
	String [] ip;
	int [] port;
	byte[] result = new byte[65535];
	byte[] errInfo = new byte[256];
	// serverConfig is in the format of "ip:port", eg. "182.131.3.245:7709"
	public TDXClient(String ... serverConfig) {
		hqLib = new TDXLibrary[serverConfig.length];
		ip = new String [serverConfig.length];
		port = new int [serverConfig.length];
		String [] temp;
		for (int i = serverConfig.length - 1; i > -1; i--) {
			hqLib[i] = (TDXLibrary) Native.loadLibrary("TdxHqApi", TDXLibrary.class);
			temp = serverConfig[i].split(":");
			ip[i] = temp[0];
			port[i] = Integer.parseInt(temp[1]);
		}
	}

	public boolean connect () {
		boolean success = true;
		for (int i = hqLib.length - 1; i > -1; i--) {
			if (hqLib[i].TdxHq_Connect(ip[i], port[i], result, errInfo)) {
				System.out.println(Native.toString(result, "GBK"));
			} else {
				System.out.println(ip[i]+":"+port[i]+" is not working.");
				System.out.println(Native.toString(errInfo, "GBK"));
				success = false;
			}
		}
		return success;
	}

	public void disconnect () {
		for (TDXLibrary lib : hqLib) {
			lib.TdxHq_Disconnect();
		}
	}

	public void pollQuotes (long gapMillis, String ... codes) {
		int count = 0;
		long start;
		boolean success;
		HashMap<String, Depth> depthMap = new HashMap<>();
		HashSet<String> modifiedStocks = new HashSet<>();

		byte[] market = calcMarkets(codes);

		ShortByReference resultCount = new ShortByReference();
		resultCount.setValue((short) codes.length);

		String [] bulkTemp = new String[codes.length+1];
		String [] bulkLastTemp;
		String [] stockTemp;

		for (int i = bulkTemp.length - 1; i > -1; i--) {
			bulkTemp[i] = "";
		}

		while (count < 40) {
			start = System.currentTimeMillis();
			success = hqLib[count%hqLib.length].TdxHq_GetSecurityQuotes(market, codes, resultCount, result, errInfo);
			System.out.println("Time taken(ms) for "+(count%hqLib.length)+": "+(System.currentTimeMillis() - start));
			count ++;
			start = System.currentTimeMillis();
			if (!success) {
				System.out.println(ip[count%hqLib.length]+":"+port[count%hqLib.length]+" is not working.");
				System.out.println(Native.toString(errInfo, "GBK"));
			} else {
				modifiedStocks.clear();
				String a = Native.toString(result, "GBK");
				bulkLastTemp = bulkTemp;
				bulkTemp = a.split("\n");
				for (int i = bulkTemp.length - 1; i > 0; i--) {
					// Change check 1
					if (bulkLastTemp[i].equals(bulkTemp[i])) {
						// if data for this tick is the same as its last tick, skip it.
						continue;
					}
					stockTemp = bulkTemp[i].split("\t");
					Depth stockDepth = new Depth(
							Double.parseDouble(stockTemp[3]), null, true,
							Double.parseDouble(stockTemp[33]),
							Double.parseDouble(stockTemp[29]),
							Double.parseDouble(stockTemp[25]),
							Double.parseDouble(stockTemp[21]),
							Double.parseDouble(stockTemp[17]),
							Double.parseDouble(stockTemp[18]),
							Double.parseDouble(stockTemp[22]),
							Double.parseDouble(stockTemp[26]),
							Double.parseDouble(stockTemp[30]),
							Double.parseDouble(stockTemp[34])
					);
					stockDepth.setVols(
							Integer.parseInt(stockTemp[35]),
							Integer.parseInt(stockTemp[31]),
							Integer.parseInt(stockTemp[27]),
							Integer.parseInt(stockTemp[23]),
							Integer.parseInt(stockTemp[19]),
							Integer.parseInt(stockTemp[20]),
							Integer.parseInt(stockTemp[24]),
							Integer.parseInt(stockTemp[28]),
							Integer.parseInt(stockTemp[32]),
							Integer.parseInt(stockTemp[36])
					);
					depthMap.put(stockTemp[1], stockDepth);
					modifiedStocks.add(stockTemp[1]);
				}
				// do stuff
				System.out.println("Time taken(ms) for this round: "+(System.currentTimeMillis() - start));
//				System.out.println(a);
				try {
					Thread.sleep(gapMillis);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
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

	public void getXDXRInfo (String ... codes) {
		byte [] market = calcMarkets(codes);
		boolean success;
		for (int i = codes.length - 1; i > -1; i --) {
			success = hqLib[i%hqLib.length].TdxHq_GetXDXRInfo(market[i], codes[i], result, errInfo);
			if (!success) {
				System.out.println(ip[i%hqLib.length]+":"+port[i%hqLib.length]+" is not working.");
				System.out.println(Native.toString(errInfo, "GBK"));
			} else {
				System.out.println(Native.toString(result, "GBK"));
			}
		}
	}
}