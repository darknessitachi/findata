package michael.findata.external.tdx.test;


import michael.findata.external.tdx.TDXClient;

public class TDXIntegratedClientTest {
	public static void main (String [] args) {
		TDXClient client = new TDXClient(
				"182.131.3.245:7709", // 上证云行情J330 - 9ms
				"125.71.28.133:443", // cd1010 - 8 ms
				"221.236.13.218:7709", // 招商证券成都行情 - 9-33 ms
				"221.236.13.219:7709", // 招商证券成都行情 - 8-25 ms
				"125.71.28.133:7709" // cd1010 - 8 ms
		);
		client.connect();
		client.test("159901", "159902", "159903", "510500", "162711", "510510");
		client.disconnect();
	}
}