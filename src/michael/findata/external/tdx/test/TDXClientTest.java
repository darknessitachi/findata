package michael.findata.external.tdx.test;

import michael.findata.external.SecurityTimeSeriesData;
import michael.findata.external.SecurityTimeSeriesDatum;
import michael.findata.external.tdx.TDXClient;
import michael.findata.service.StockService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class TDXClientTest {
	public static void main (String [] args) throws IOException {
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/pair_spring.xml");
		StockService ss = (StockService) context.getBean("stockService");
		//��װ����֤ȯ������֤ȯ������֤ȯ���������֤ȯ��˾��ͨ���������
		//�ҵ������connect.cfg�ļ��������ҳ����µķ������б����л���
		//֤ȯ�ķ�������ࡣ
		TDXClient client1 = new TDXClient(
//				"182.131.3.245:7709",    // ��֤������J330 - 9ms)
				"221.237.158.106:7709",    // ����֤ȯ����ӳɶ�������վ1
				"221.237.158.107:7709",    // ����֤ȯ����ӳɶ�������վ2
				"221.237.158.108:7709"    // ����֤ȯ����ӳɶ�������վ3

//				"221.236.13.218:7709",	// ����֤ȯ�ɶ����� - 9-33 ms
//				"221.236.13.219:7709",	// ����֤ȯ�ɶ����� - 8-25 ms
//				"125.71.28.133:7709",	// cd1010 - 8 ms
//				"221.236.15.14:995",	// ����ɶ�����2.1
//				"124.161.97.84:7709",	// ��������ɶ���ͨ2
//				"124.161.97.83:7709",	// ��������ɶ���ͨ1
//				"125.64.39.62:7709",	// ��������ɶ�����2
//				"125.71.28.133:443",	// cd1010 - 8 ms
//				"221.236.15.14:7709",	// ����ɶ�����1.13
//				"119.6.204.139:7709",	// ����ɶ���ͨ5.135
//				"125.64.39.61:7709",	// ��������ɶ�����1
//				"125.64.41.12:7709"		// �ɶ�����54
		);
//		TDXClient client2 = new TDXClient(
//				"119.4.167.141:7709",	// ����L1
//				"119.4.167.142:7709",	// ����L2
//				"119.4.167.181:7709",	// ����L3
//				"119.4.167.182:7709",	// ����L4
//				"119.4.167.164:7709",	// ����L5
//				"119.4.167.165:7709",	// ����L6
//				"119.4.167.163:7709",	// ����L7
//				"119.4.167.175:7709",	// ����L8
//				"218.6.198.151:7709",	// ����L1
//				"218.6.198.152:7709",	// ����L2
//				"218.6.198.174:7709",	// ����L3
//				"218.6.198.175:7709"	// ����L4
//		);
//		TDXClient client3 = new TDXClient(
//				"218.6.198.155:7709",	// ����L5
//				"218.6.198.156:7709",	// ����L6
//				"218.6.198.157:7709",	// ����L7
//				"218.6.198.158:7709",	// ����L8
//				"182.131.7.141:7709",	// ����E1
//				"182.131.7.142:7709",	// ����E2
//				"182.131.7.143:7709",	// ����E3
//				"182.131.7.144:7709",	// ����E4
//				"182.131.7.145:7709",	// ����E5
//				"182.131.7.146:7709",	// ����E6
//				"182.131.7.147:7709",	// ����E7
//				"182.131.7.148:7709",	// ����E8
//				"182.131.3.245:7709",	// ��֤������J330 - 9ms)
//				"221.237.158.106:7709",	// ����֤ȯ����ӳɶ�������վ1
//				"221.237.158.107:7709",	// ����֤ȯ����ӳɶ�������վ2
//				"221.237.158.108:7709",	// ����֤ȯ����ӳɶ�������վ3
//				"183.230.9.136:7709",	// ����֤ȯ����������ƶ���վ1
//				"183.230.134.6:7709",	// ����֤ȯ����������ƶ���վ2
//				"219.153.1.115:7709",	// ����֤ȯ��������������վ1
//				"113.207.29.12:7709"	// ����֤ȯ�����������ͨ��վ1
//		);
		client1.connect();
//		client2.connect();
//		client2.disconnect();
//		client3.connect();
//		client3.disconnect();
//		client1.pollQuotes(20L, ss.getStockGroup("michael/findata/algoquant/strategy/pair/group_sz.csv").toArray(new String[0]));
//		client1.pollQuotes(3, "000568", "600026");
//		client1.pollQuotes(3, "000568", "600026");
//		client3.pollQuotes(1000, 20L, "000568", "600026");
		long cur = System.currentTimeMillis();
//		SecurityTimeSeriesData data = client1.getEOMs("000001", (short) 8);
//		SecurityTimeSeriesData data = client1.getEOMs("601009", (short) 6, (short) 2);
//		client1.getEOMs("601009", (short) 3, (short) 3);
		client1.getEOMs("601009", (short) 0, (short) 3);
		SecurityTimeSeriesDatum minute;
//		while (data.hasNext()) {
//			minute = data.popNext();
//			System.out.println("Time:\t"+minute.getDateTime());
//			System.out.println("Open:\t"+minute.getOpen());
//			System.out.println("High:\t"+minute.getHigh());
//			System.out.println("Low:\t"+minute.getLow());
//			System.out.println("Close:\t"+minute.getClose());
//			System.out.println("Amt:\t"+minute.getAmount());
//			System.out.println("Vol:\t"+minute.getVolume());
//		}
		System.out.println(System.currentTimeMillis() - cur);
//		TDXMetaClient mt = new TDXMetaClient(1000L, client1, client2, client3);
//		mt.test(1);
		client1.disconnect();
	}
}