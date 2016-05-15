package michael.findata.external.tdx;

import com.sun.jna.Native;
import com.sun.jna.ptr.ShortByReference;
import michael.findata.algoquant.execution.datatype.depth.Depth;

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
	String raw;

	// Repetitively poll quotes up to a preset number of times, until count is up or until any update is detected.
	// this is a much resource-intensive way of polling quotes, given SH and SZ exchanges are only publishing snapshots
	// every 5/3 seconds respectively. In other words, there is no point polling from SH for another 4.5/2.5 seconds if
	// SH/SZ has just published a snapshot.
	public Map<String, Depth> pollQuotes (int pollTimes, String ... codes) {
		int count = 0;
		long start;
		boolean success;
		HashMap<String, Depth> depthMap = new HashMap<>();
//		HashSet<String> modifiedStocks = new HashSet<>();

		byte[] market = calcMarkets(codes);

		ShortByReference resultCount = new ShortByReference();
		resultCount.setValue((short) codes.length);

//		if (bulkTemp == null || bulkTemp.length != codes.length + 1) {
//			bulkTemp = new String[codes.length+1];
//			for (int i = bulkTemp.length - 1; i > -1; i--) {
//				bulkTemp[i] = "";
//			}
//			raw = "";
//		}

		String rawTemp = raw;
		String [] bulkLastTemp = bulkTemp;
//		String [] stockTemp;

		int index;
		boolean updated = false; // marks whether data has been updated from server
		start = System.currentTimeMillis();
		while (!updated && count < pollTimes) {
			index = count%hqLib.length;

			if (fault[index]) continue;

			success = hqLib[index].TdxHq_GetSecurityQuotes(market, codes, resultCount, result, errInfo);
//			System.out.println("Time taken(ms) for this round: "+(System.currentTimeMillis() - start));
			count ++;
			if (!success) {
				System.out.println(ip[index]+":"+port[index]+" is not working.");
				System.out.println(Native.toString(errInfo, "GBK"));
				fault[index] = true;
			} else {
				raw = Native.toString(result, "GBK");
				if (raw.equals(rawTemp)) {
//					System.out.println("not updated!");
					continue;
				}
				bulkTemp = raw.split("[\n\t]");
				for (int i = bulkTemp.length - 1; i > 0; i--) {
					// todo: can we use timestamp as a flag for update?
					if (bulkLastTemp != null && bulkLastTemp[i+8].equals(bulkTemp[i+8])) { // if the timestamps has changed, this line of data must have been updated.
						updated = true;
					} else { // if timestamp is not changed, does this mean there shouldn't be any change, since there is no update at all?

					}
					// change check 1
					if (bulkLastTemp != null && bulkLastTemp[i+3].equals(bulkTemp[i+3])) { // if current price for this tick is the same as it was last tick.
						boolean changed = false;
						for (int j = 17; j <= 36; j++) {
							if (!bulkLastTemp[i+3].equals(bulkTemp[i+3])) { // if any part of depth for this tick is not the same as it was last tick.
								changed = true;
								break;
							}
						}
						if (!changed) {
							continue;
						}
					}
					Depth stockDepth = new Depth(
							Double.parseDouble(bulkTemp[i+3]), null, true,
							Double.parseDouble(bulkTemp[i+33]),
							Double.parseDouble(bulkTemp[i+29]),
							Double.parseDouble(bulkTemp[i+25]),
							Double.parseDouble(bulkTemp[i+21]),
							Double.parseDouble(bulkTemp[i+17]),
							Double.parseDouble(bulkTemp[i+18]),
							Double.parseDouble(bulkTemp[i+22]),
							Double.parseDouble(bulkTemp[i+26]),
							Double.parseDouble(bulkTemp[i+30]),
							Double.parseDouble(bulkTemp[i+34])
					);
					stockDepth.setVols(
							Integer.parseInt(bulkTemp[i+35]),
							Integer.parseInt(bulkTemp[i+31]),
							Integer.parseInt(bulkTemp[i+27]),
							Integer.parseInt(bulkTemp[i+23]),
							Integer.parseInt(bulkTemp[i+19]),
							Integer.parseInt(bulkTemp[i+20]),
							Integer.parseInt(bulkTemp[i+24]),
							Integer.parseInt(bulkTemp[i+28]),
							Integer.parseInt(bulkTemp[i+32]),
							Integer.parseInt(bulkTemp[i+36])
					);
					depthMap.put(bulkTemp[i+1], stockDepth);
					if (updated) {
						System.out.println(raw+"\n"+name[index]+" updated@"+(new Date()));
					}
				}
			}
		}
		System.out.println(codes[0]+": Time taken(ms) for this round: "+(System.currentTimeMillis() - start));
		return depthMap;
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

		String [] bulkTemp = null;
		String [] bulkLastTemp;
//		String [] stockTemp;

//		for (int i = bulkTemp.length - 1; i > -1; i--) {
//			bulkTemp[i] = "";
//		}

		while (count < 700) {
			start = System.currentTimeMillis();
			success = hqLib[count%hqLib.length].TdxHq_GetSecurityQuotes(market, codes, resultCount, result, errInfo);
//			System.out.println("Time taken(ms) for "+(count%hqLib.length)+": "+(System.currentTimeMillis() - start));
			count ++;
//			start = System.currentTimeMillis();
			if (!success) {
				System.out.println(ip[count%hqLib.length]+":"+port[count%hqLib.length]+" is not working.");
				System.out.println(Native.toString(errInfo, "GBK"));
			} else {
				modifiedStocks.clear();
				String a = Native.toString(result, "GBK");
				bulkLastTemp = bulkTemp;
				bulkTemp = a.split("[\n\t]");
				int end = bulkTemp.length - 43;
				for (int i = 44; i < end; i += 44) {
					// Change check 1
					boolean changed = false;
					boolean timeUpdated = (bulkLastTemp == null || !bulkLastTemp[i+8].equals(bulkTemp[i+8]));
					if (bulkLastTemp != null && bulkLastTemp[i+3].equals(bulkTemp[i+3])) { // if current price for this tick is the same as it was last tick.
						for (int j = 17; j <= 36; j++) {
							if (!bulkLastTemp[i+3].equals(bulkTemp[i+3])) { // if any part of depth for this tick is not the same as it was last tick.
								changed = true;
								break;
							}
						}
					} else {
						changed = true;
					}
					System.out.println(bulkTemp[i+1]+"\t"+bulkTemp[i+8]+"\tchanged\t"+changed+"\ttimeupdated\t"+timeUpdated);
					if (!changed) {
						continue;
					}
//					stockTemp = bulkTemp[i].split("\t");
					Depth stockDepth = new Depth(
							Double.parseDouble(bulkTemp[i+3]), null, true,
							Double.parseDouble(bulkTemp[i+33]),
							Double.parseDouble(bulkTemp[i+29]),
							Double.parseDouble(bulkTemp[i+25]),
							Double.parseDouble(bulkTemp[i+21]),
							Double.parseDouble(bulkTemp[i+17]),
							Double.parseDouble(bulkTemp[i+18]),
							Double.parseDouble(bulkTemp[i+22]),
							Double.parseDouble(bulkTemp[i+26]),
							Double.parseDouble(bulkTemp[i+30]),
							Double.parseDouble(bulkTemp[i+34])
					);
					stockDepth.setVols(
							Integer.parseInt(bulkTemp[i+35]),
							Integer.parseInt(bulkTemp[i+31]),
							Integer.parseInt(bulkTemp[i+27]),
							Integer.parseInt(bulkTemp[i+23]),
							Integer.parseInt(bulkTemp[i+19]),
							Integer.parseInt(bulkTemp[i+20]),
							Integer.parseInt(bulkTemp[i+24]),
							Integer.parseInt(bulkTemp[i+28]),
							Integer.parseInt(bulkTemp[i+32]),
							Integer.parseInt(bulkTemp[i+36])
					);
					depthMap.put(bulkTemp[i+1], stockDepth);
					modifiedStocks.add(bulkTemp[i+1]);
				}
				if (!depthMap.isEmpty()) {
					System.out.println(System.currentTimeMillis());
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