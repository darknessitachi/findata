package michael.findata.external.tdx.test;


import michael.findata.external.SecurityShareNumberChange;
import michael.findata.external.tdx.TDXClient;
import org.joda.time.Hours;
import org.joda.time.LocalTime;

import java.util.concurrent.Semaphore;

public class TDXClientTest {
	boolean updated = false;
	public static void main (String [] args) {
		//��װ����֤ȯ������֤ȯ������֤ȯ���������֤ȯ��˾��ͨ���������
		//�ҵ������connect.cfg�ļ��������ҳ����µķ������б����л���
		//֤ȯ�ķ�������ࡣ
		TDXClient client1 = new TDXClient(
				"221.236.13.218:7709",	// ����֤ȯ�ɶ����� - 9-33 ms
				"221.236.13.219:7709",	// ����֤ȯ�ɶ����� - 8-25 ms
				"125.71.28.133:7709",	// cd1010 - 8 ms
				"221.236.15.14:995",	// ����ɶ�����2.1
				"124.161.97.84:7709",	// ��������ɶ���ͨ2
				"124.161.97.83:7709",	// ��������ɶ���ͨ1
				"125.64.39.62:7709",	// ��������ɶ�����2
				"125.71.28.133:443",	// cd1010 - 8 ms
				"221.236.15.14:7709",	// ����ɶ�����1.13
				"119.6.204.139:7709",	// ����ɶ���ͨ5.135
				"125.64.39.61:7709",	// ��������ɶ�����1
				"125.64.41.12:7709"	// �ɶ�����54
		);
		TDXClient client2 = new TDXClient(
				"119.4.167.141:7709",	// ����L1
				"119.4.167.142:7709",	// ����L2
				"119.4.167.181:7709",	// ����L3
				"119.4.167.182:7709",	// ����L4
				"119.4.167.164:7709",	// ����L5
				"119.4.167.165:7709",	// ����L6
				"119.4.167.163:7709",	// ����L7
				"119.4.167.175:7709",	// ����L8
				"218.6.198.151:7709",	// ����L1
				"218.6.198.152:7709",	// ����L2
				"218.6.198.174:7709",	// ����L3
				"218.6.198.175:7709"	// ����L4
		);
		TDXClient client3 = new TDXClient(
				"218.6.198.155:7709",	// ����L5
				"218.6.198.156:7709",	// ����L6
				"218.6.198.157:7709",	// ����L7
				"218.6.198.158:7709",	// ����L8
				"182.131.7.141:7709",	// ����E1
				"182.131.7.142:7709",	// ����E2
				"182.131.7.143:7709",	// ����E3
				"182.131.7.144:7709",	// ����E4
				"182.131.7.145:7709",	// ����E5
				"182.131.7.146:7709",	// ����E6
				"182.131.7.147:7709",	// ����E7
				"182.131.7.148:7709",	// ����E8
				"182.131.3.245:7709"	// ��֤������J330 - 9ms)
		);
		client1.connect();
		client1.getXDXRInfo("510220", "510880", "510180", "510300", "161831", "164705", "159937", "000568", "200771").forEach(data -> {
			for (String field : data) {
				System.out.print(field);
				System.out.print("\t");
			}
			System.out.println();
		});
		client1.disconnect();
//		TDXMetaClient mt = new TDXMetaClient(1000L, client1, client2, client3);
//		mt.test(1);
	}
}

class TDXMetaClient {
	TDXClient [] clients;
	boolean semaphore = false;
	TDXMetaClient (long gapMillis, TDXClient ... clients) {
		this.clients = clients;
	}

	public void test (int i) {
		Runnable runnable = () -> {
			clients[i].connect();
			boolean updated;
			while (true) {
				System.out.println("Polling... "+i);
				if (LocalTime.now().getHourOfDay() > 20) {
					System.out.println("Passed 03:00 PM, stop polling.");
					clients[i].disconnect();
					break;
				}
				updated = clients[i].pollQuotes(200
						, "000568", "002272", "600035", "510500", "162711", "510510"
						, "159901", "159902", "159903", "510500", "162711", "510510"
						, "159901", "159902", "159903", "510500", "162711", "510510"
						, "159901", "159902", "159903", "510500", "162711", "510510"
						, "159901", "159902", "159903", "510500", "162711", "510510"
						, "159901", "159902", "159903", "510500", "162711", "510510"
						, "159901", "159902", "159903", "510500", "162711", "510510"
						, "159901", "159902", "159903", "510500", "162711", "510510"
						, "159901", "159902", "159903", "510500", "162711", "510510"
				);
				if (updated) {
					System.out.println("Got data from "+i);
				}
				try {
					Thread.sleep(20000L);
				} catch (InterruptedException e) {
					System.out.println("Interrupt: "+i);
				}
			}
		};
		Thread t = new Thread(runnable);
		t.start();
	}
}