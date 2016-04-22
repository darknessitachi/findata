package michael.findata.external.tdx;

import com.sun.jna.Native;
import com.sun.jna.ptr.ShortByReference;

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

	public void test (String ... codes) {
		byte[] market;
		int count = 0;
		long start;
		boolean success;

		market = new byte[codes.length];
		for (int i = codes.length -1; i > -1; i--) {
			switch (codes[i].charAt(0)) {
				case '5':
				case '6':
				case '9':
					market[i] = 1;
					break;
				default:
					market[i] = 0;
			}
		}
		ShortByReference resultCount = new ShortByReference();
		resultCount.setValue((short) 6);

		while (count < 30) {
			start = System.currentTimeMillis();
			success = hqLib[count%hqLib.length].TdxHq_GetSecurityQuotes(market, codes, resultCount, result, errInfo);
			System.out.println("Time taken(ms) for "+(count%hqLib.length)+": "+(System.currentTimeMillis() - start));
			if (!success) {
				System.out.println(Native.toString(errInfo, "GBK"));
				return;
			}
//			System.out.println(Native.toString(result, "GBK"));
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			count ++;
		}
	}
}