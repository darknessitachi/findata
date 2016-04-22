import com.sun.jna.Native;
import com.sun.jna.ptr.ShortByReference;
import michael.findata.external.tdx.TDXLibrary;

public class TdxHqApiTest {

	public static void main(String[] args) {
		try {
			//DLL��32λ��,��˱���ʹ��jdk32λ����,���ܵ���DLL;
			//�����TdxHqApi.dll���Ƶ�java����Ŀ¼��;
			//java���̱���������� jna.jar, �� https://github.com/twall/jna ���� jna.jar
			//������ʲô���Ա�̣���������ϸ�Ķ�VC���ڵĹ���DLL���������Ĺ��ܺͲ�������˵��������ϸ�Ķ���������������ʱ�侫�����ޣ�ˡ�����
			TDXLibrary[] TdxHqLibrary = new TDXLibrary[5];
			TdxHqLibrary[0] = (TDXLibrary) Native.loadLibrary("TdxHqApi", TDXLibrary.class);
			TdxHqLibrary[1] = (TDXLibrary) Native.loadLibrary("TdxHqApi", TDXLibrary.class);
			TdxHqLibrary[2] = (TDXLibrary) Native.loadLibrary("TdxHqApi", TDXLibrary.class);
			TdxHqLibrary[3] = (TDXLibrary) Native.loadLibrary("TdxHqApi", TDXLibrary.class);
			TdxHqLibrary[4] = (TDXLibrary) Native.loadLibrary("TdxHqApi", TDXLibrary.class);

			byte[] Result = new byte[65535];
			byte[] ErrInfo = new byte[256];

			long start = System.currentTimeMillis();
//			boolean boolean1 = TdxHqLibrary1.TdxHq_Connect("119.97.185.6", 7721, Result, ErrInfo); // cd1010 - 8 ms!!!
//			boolean boolean1 = TdxHqLibrary1.TdxHq_Connect("222.247.45.173", 7709, Result, ErrInfo); // ������վ:��ɳ����3 - 40-60 ms
//			boolean boolean1 = TdxHqLibrary1.TdxHq_Connect("218.108.50.178", 7709, Result, ErrInfo); // ����֤ȯ�������� - 80+ ms
//			boolean boolean1 = TdxHqLibrary1.TdxHq_Connect("218.18.103.38", 7709, Result, ErrInfo); // ����֤ȯ������վ - 60+ ms
			boolean boolean0 = TdxHqLibrary[0].TdxHq_Connect("182.131.3.245", 7709, Result, ErrInfo); // ��֤������J330 - 9ms
			boolean boolean3 = TdxHqLibrary[3].TdxHq_Connect("125.71.28.133", 443, Result, ErrInfo); // cd1010 - 8 ms!!!
			boolean boolean1 = TdxHqLibrary[1].TdxHq_Connect("221.236.13.218", 7709, Result, ErrInfo); // ����֤ȯ�ɶ����� - 9-33 ms
			boolean boolean2 = TdxHqLibrary[2].TdxHq_Connect("221.236.13.219", 7709, Result, ErrInfo); // ����֤ȯ�ɶ����� - 8-25 ms
			boolean boolean4 = TdxHqLibrary[4].TdxHq_Connect("125.71.28.133", 7709, Result, ErrInfo); // cd1010 - 8 ms
			System.out.println("Time taken(ms): "+(System.currentTimeMillis() - start));
			if (!boolean1) {
				System.out.println(Native.toString(ErrInfo, "GBK"));
				return;
			}
			System.out.println(Native.toString(Result, "GBK"));

			byte[] Market;
			String[] Zqdm;
			int count = 0;

			while (count < 30) {
				Market = new byte[]{0, 0, 0, 1, 0, 1};
				Zqdm = new String[]{"159901", "159902", "159903", "510500", "162711", "510510"};
				ShortByReference Count = new ShortByReference();
				Count.setValue((short) 6);
				start = System.currentTimeMillis();
				boolean1 = TdxHqLibrary[count%1+1].TdxHq_GetSecurityQuotes(Market, Zqdm, Count, Result, ErrInfo);
				System.out.println("Time taken(ms) for "+(count%1+1)+": "+(System.currentTimeMillis() - start));
				if (!boolean1) {
					System.out.println(Native.toString(ErrInfo, "GBK"));
					return;
				}
//				System.out.println(Native.toString(Result, "GBK"));
				Thread.sleep(100);
				count ++;
			}


			ShortByReference Count2 = new ShortByReference();
			Count2.setValue((short) 20);
			start = System.currentTimeMillis();
//			boolean1 = TdxHqLibrary1.TdxHq_GetIndexBars((byte) 0, (byte) 1, "000001", (short) 0, Count2, Result, ErrInfo);
			System.out.println("Time taken(ms): "+(System.currentTimeMillis() - start));
			if (!boolean1) {
				System.out.println(Native.toString(ErrInfo, "GBK"));
				return;
			}
//			System.out.println(Native.toString(Result, "GBK"));

			ShortByReference Count3 = new ShortByReference();
			Count3.setValue((short) 80);
			start = System.currentTimeMillis();
//			boolean1 = TdxHqLibrary1.TdxHq_GetTransactionData((byte) 0, "000001", (short) 0, Count3, Result, ErrInfo);
			System.out.println("Time taken(ms): "+(System.currentTimeMillis() - start));
			if (!boolean1) {
				System.out.println(Native.toString(ErrInfo, "GBK"));
				return;
			}
//			System.out.println(Native.toString(Result, "GBK"));

			start = System.currentTimeMillis();
//			boolean1 = TdxHqLibrary1.TdxHq_GetCompanyInfoContent((byte) 0, "000001", "000001.txt", 0, 10240, Result, ErrInfo);
			System.out.println("Time taken(ms): "+(System.currentTimeMillis() - start));
			if (!boolean1) {
				System.out.println(Native.toString(ErrInfo, "GBK"));
				return;
			}
//			System.out.println(Native.toString(Result, "GBK"));

			start = System.currentTimeMillis();
			TdxHqLibrary[0].TdxHq_Disconnect();
			TdxHqLibrary[1].TdxHq_Disconnect();
			TdxHqLibrary[2].TdxHq_Disconnect();
			TdxHqLibrary[3].TdxHq_Disconnect();
			TdxHqLibrary[4].TdxHq_Disconnect();
			System.out.println("Time taken(ms): "+(System.currentTimeMillis() - start));

			System.out.println("�ѶϿ�����");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}