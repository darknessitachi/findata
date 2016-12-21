package michael.findata.algoquant.execution.component.broker;

import com.numericalmethod.algoquant.execution.datatype.order.Order;
import com.sun.jna.Library;
import com.sun.jna.Native;
import michael.findata.algoquant.execution.datatype.order.HexinOrder;
import michael.findata.algoquant.execution.listener.OrderListener;
import michael.findata.model.Stock;
import org.slf4j.Logger;

import java.util.Collection;

import static com.numericalmethod.nmutils.NMUtils.getClassLogger;

public class LocalNativeTdxBroker implements Broker{
	private static Logger LOGGER = getClassLogger();

	private interface TdxLibrary extends Library {
		//�����溯��
		//��ͨ����
		void OpenTdx();

		//�ر�ͨ����
		void CloseTdx();

		/// <summary>
		/// �����˻���¼
		/// </summary>
		/// <param name="IP">ȯ�̽��׷�����IP</param>
		/// <param name="Port">ȯ�̽��׷������˿�</param>
		/// <param name="Version">����ͨ���ſͻ��˵İ汾��</param>
		/// <param name="YybID">Ӫҵ�����룬�뵽��ַ http://www.chaoguwaigua.com/downloads/qszl.htm ��ѯ</param>
		/// <param name="AccountNo">�����ĵ�¼�˺ţ�ȯ��һ��ʹ���ʽ��ʻ���ͻ���</param>
		/// <param name="TradeAccount">�����˺ţ�һ�����¼�ʺ���ͬ. ���¼ȯ��ͨ�����������ѯ�ɶ��б��ɶ��б��ڵ��ʽ��ʺž��ǽ����ʺ�, �����ѯ���������վ���ȵ��ʴ���Ŀ</param>
		/// <param name="JyPassword">��������</param>
		/// <param name="TxPassword">ͨѶ����</param>
		/// <param name="ErrInfo">��APIִ�з��غ�������������˴�����Ϣ˵����һ��Ҫ����256�ֽڵĿռ䡣û����ʱΪ���ַ�����</param>
		/// <returns>�ͻ���ID��ʧ��ʱ����-1</returns>
		int Logon(String IP, short Port, String Version, short YybID, String AccountNo, String TradeAccount, String JyPassword, String TxPassword, byte[] ErrInfo);

		//ע��
		void Logoff(int ClientID);

		/// <summary>
		/// ��ѯ���ֽ�������
		/// </summary>
		/// <param name="clientID">�ͻ���ID</param>
		/// <param name="Category">��ʾ��ѯ��Ϣ�����࣬0�ʽ�  1�ɷ�   2����ί��  3���ճɽ�     4�ɳ���   5�ɶ�����  6�������   7��ȯ���  8����֤ȯ</param>
		/// <param name="Result">��APIִ�з��غ�Result�ڱ����˷��صĲ�ѯ����, ��ʽΪ������ݣ�������֮��ͨ��\n�ַ��ָ������֮��ͨ��\t�ָ���һ��Ҫ����1024*1024�ֽڵĿռ䡣����ʱΪ���ַ�����</param>
		/// <param name="ErrInfo">��APIִ�з��غ�������������˴�����Ϣ˵����һ��Ҫ����256�ֽڵĿռ䡣û����ʱΪ���ַ�����</param>
		void QueryData(int ClientID, int Category, byte[] Result, byte[] ErrInfo);

		/// ��ί�н���֤ȯ
		/// </summary>
		/// <param name="clientID">�ͻ���ID</param>
		/// <param name="Category">��ʾί�е�����
		///							0����
		///							1����
		///							2��������
		///							3��ȯ����
		///							4��ȯ��ȯ
		///							5��ȯ����
		///							6��ȯ��ȯ
		///	</param>
		/// <param name="PriceType">��ʾ���۷�ʽ
		///							0�Ϻ��޼�ί�� �����޼�ί��
		/// 						1(�м�ί��)���ڶԷ����ż۸�
		///							2(�м�ί��)���ڱ������ż۸�
		///							3(�м�ί��)���ڼ�ʱ�ɽ�ʣ�೷��
		///							4(�м�ί��)�Ϻ��嵵����ʣ�� �����嵵����ʣ��
		///							5(�м�ί��)����ȫ��ɽ�����
		///							6(�м�ί��)�Ϻ��嵵����ת�޼�
		///	</param>
		/// <param name="Gddm">�ɶ�����, �����Ϻ���Ʊ���Ϻ��Ĺɶ����룻�������ڵĹ�Ʊ�������ڵĹɶ�����</param>
		/// <param name="Zqdm">֤ȯ����</param>
		/// <param name="Price">ί�м۸�</param>
		/// <param name="Quantity">ί������</param>
		/// <param name="Result">��APIִ�з��غ�Result�ڱ����˷��صĲ�ѯ����, ��ʽΪ������ݣ�������֮��ͨ��\n�ַ��ָ������֮��ͨ��\t�ָ���
		/// һ��Ҫ����1024*1024�ֽڵĿռ䡣����ʱΪ���ַ��������к���ί�б������</param>
		/// <param name="ErrInfo">��APIִ�з��غ�������������˴�����Ϣ˵����һ��Ҫ����256�ֽڵĿռ䡣û����ʱΪ���ַ�����</param>
		void SendOrder(int ClientID, int Category, int PriceType, String Gddm, String Zqdm, float Price, int Quantity, byte[] Result, byte[] ErrInfo);

		/// <summary>
		/// ��ί��
		/// </summary>
		/// <param name="clientID">�ͻ���ID</param>
		/// <param name="ExchangeID">������ID�� �Ϻ�1������0(����֤ȯ��ͨ�˻�������2)</param>
		/// <param name="hth">��ʾҪ����Ŀ��ί�еı��</param>
		/// <param name="Result">��APIִ�з��غ�Result�ڱ����˷��صĲ�ѯ����, ��ʽΪ������ݣ�������֮��ͨ��\n�ַ��ָ������֮��ͨ��\t�ָ���
		/// һ��Ҫ����1024*1024�ֽڵĿռ䡣����ʱΪ���ַ��������к���ί�б������</param>
		/// <param name="ErrInfo">��APIִ�з��غ�������������˴�����Ϣ˵����һ��Ҫ����256�ֽڵĿռ䡣û����ʱΪ���ַ�����</param>
		void CancelOrder(int ClientID, String ExchangeID, String hth, byte[] Result, byte[] ErrInfo);

		/// <summary>
		/// ��ȡ֤ȯ��ʵʱ�嵵����
		/// </summary>
		/// <param name="clientID">�ͻ���ID</param>
		/// <param name="Zqdm">֤ȯ����</param>
		/// <param name="Result">ͬ��</param>
		/// <param name="ErrInfo">��APIִ�з��غ�������������˴�����Ϣ˵����һ��Ҫ����256�ֽڵĿռ䡣û����ʱΪ���ַ�����</param>
		void GetQuote(int ClientID, String Zqdm, byte[] Result, byte[] ErrInfo);

		/// <summary>
		/// ������ȯ�˻�ֱ�ӻ���
		/// </summary>
		/// <param name="clientID">�ͻ���ID</param>
		/// <param name="Amount">������</param>
		/// <param name="Result">ͬ��</param>
		/// <param name="ErrInfo">��APIִ�з��غ�������������˴�����Ϣ˵����һ��Ҫ����256�ֽڵĿռ䡣û����ʱΪ���ַ�����</param>
		void Repay(int ClientID, String Amount, byte[] Result, byte[] ErrInfo);

		///���׽ӿ�ִ�к����ʧ�ܣ����ַ���ErrInfo�����˳�����Ϣ����˵����
		///����ɹ������ַ���Result�����˽������,��ʽΪ������ݣ�������֮��ͨ��\n�ַ��ָ������֮��ͨ��\t�ָ���
		///Result��\n��\t�ָ��������ַ����������ѯ�ɶ�����ʱ���صĽ���ַ�������

		///"�ɶ�����\t�ɶ�����\t�ʺ����\t������Ϣ\n
		///0000064567\t\t0\t\nA000064567\t\t1\t\n
		///2000064567\t\t2\t\nB000064567\t\t3\t"

		///��ô�����֮��ͨ���ָ��ַ����� ���Իָ�Ϊ���м��еı����ʽ������

		//APIʹ������Ϊ: Ӧ�ó����ȵ���OpenTdx��ͨ����ʵ����һ��ʵ���¿���ͬʱ��¼��������˻���ÿ�������˻���֮ΪClientID.
		//ͨ������Logon���ClientID��Ȼ����Ե�������API���������ClientID���в�ѯ���µ�;
		//Ӧ�ó����˳�ʱӦ����Logoffע��ClientID, ������CloseTdx�ر�ͨ����ʵ��.
		//OpenTdx��CloseTdx������Ӧ�ó�����ֻ�ܱ�����һ��.API���ж����Զ��������ܣ�Ӧ�ó���ֻ�����API�������صĳ�����Ϣ�����ʵ���������
	}

	private TdxLibrary trade;
	private int clientID;
	private String gddmSH = "E042255853";
	private String gddmSZ = "0604227902";

	public LocalNativeTdxBroker (String ip, short port, short yingyebuID, String accountNo, String tradeAccount, String pass, String commsPass) {
		//DLL��32λ��,��˱���ʹ��jdk32λ����,���ܵ���DLL;
		//�����Trade.dll��4��DLL���Ƶ�java����Ŀ¼��;
		//java���̱���������� jna.jar, �� https://github.com/twall/jna ���� jna.jar
		//������ʲô���Ա�̣���������ϸ�Ķ�VC���ڵĹ���DLL���������Ĺ��ܺͲ�������˵��������ϸ�Ķ���������������ʱ�侫�����ޣ�ˡ�����
		byte[] Result = new byte[1024];
		byte[] ErrInfo = new byte[256];

		trade = (TdxLibrary) Native.loadLibrary("Trade", TdxLibrary.class);
		trade.OpenTdx();
		//��¼
//		clientID = trade.Logon("180.153.18.180", (short) 7708, "6.33", (short) 2, "8009145070", "8009145070", "495179", "495179", ErrInfo);
		long time = System.currentTimeMillis();
		clientID = trade.Logon(ip, port, "6.33", yingyebuID, accountNo, tradeAccount, pass, commsPass, ErrInfo);
		System.out.println(System.currentTimeMillis() - time);

		if (clientID == -1) {
			LOGGER.error(Native.toString(ErrInfo, "GBK"));
			return;
		}
		LOGGER.info("Tdx native broker logon successful!");

		//��ѯ
		trade.QueryData(clientID, 5, Result, ErrInfo);
		//TdxLibrary1.QueryData(ClientID2, 0, Result, ErrInfo);//�ڶ����ʺŲ�ѯ�ʽ�
		LOGGER.info(Native.toString(Result, "GBK"));
	}

	@Override
	public void stop() {
		//ע��
		trade.Logoff(clientID);
		trade.CloseTdx();
		LOGGER.info("Local Native Tdx Broker stopped.");
	}

	@Override
	public void setOrderListener(Order o, OrderListener listener) {
	}

	@Override
	public void sendOrder(Collection<? extends Order> orders) {
		byte[] result = new byte[1024];
		byte[] errInfo = new byte[256];

		for (Order o : orders) {
			int cat;
			String gddm, code;
			if (o instanceof HexinOrder) {
				HexinOrder hexinOrder = (HexinOrder) o;
				switch (hexinOrder.hexinType()) {
					case SIMPLE_SELL:
					case CREDIT_SELL:
						cat = 1;
						break;
					case SIMPLE_BUY:
					case CREDIT_BUY:
						cat = 0;
						break;
					default:
						LOGGER.error("Unable to handle order {}", o);
						continue;
//						normalOrders.add(o);
				}
			} else { // Normal Buy/Sell
				switch (o.side()) {
					case BUY:
						cat = 0;
						break;
					case SELL:
						cat = 1;
						break;
					default:
						LOGGER.error("Unable to handle order {}", o);
						continue;
				}
//				normalOrders.add(o);
			}
			try {
				Stock s = (Stock)o.product();
				code = s.getCode();
				switch (s.exchange()) {
					case SZSE:
						gddm = gddmSZ;
						break;
					case SHSE:
						gddm = gddmSH;
						break;
					default:
						LOGGER.error("Unable to handle product {}", s);
						continue;
				}
			} catch (ClassCastException e) {
				LOGGER.warn("{} is carrying a generic product {}", o, o.product());
				code = o.product().symbol();
				if (code.startsWith("6") || code.startsWith("51")) {
					gddm = gddmSH;
				} else if (code.startsWith("0") || code.startsWith("3") || code.startsWith("1")) {
					gddm = gddmSZ;
				} else {
					LOGGER.error("Unable to handle product {}", o.product());
					continue;
				}
			}
			trade.SendOrder(clientID, cat, 0, gddm, code, (float)o.price(), (int)o.quantity(), result, errInfo);
			String resultString = Native.toString(result, "GBK");
			String errorString = Native.toString(errInfo, "GBK");
			if (resultString.length() != 0) {
				o.id(Integer.parseInt(resultString.substring(resultString.indexOf('\n')+1).trim()));
				if (o instanceof HexinOrder) {
					((HexinOrder) o).ack(resultString);
				}
				LOGGER.info("{} submitted. Result: {}", o, resultString);
			}
			if (errorString.length() != 0) {
				LOGGER.error("{} submission error: ", o, errorString);
			}
		}
	}

	@Override
	public void cancelOrder(Collection<? extends Order> orders) {
		byte[] result = new byte[1024];
		byte[] errInfo = new byte[256];
		String exchangeId;
		for (Order o : orders) {
			try {
				Stock s = (Stock)o.product();
//				code = s.getCode();
				switch (s.exchange()) {
					case SZSE:
						exchangeId = "0";
						break;
					case SHSE:
						exchangeId = "1";
						break;
					default:
						LOGGER.error("Unable to handle product {}", s);
						continue;
				}
			} catch (ClassCastException e) {
				LOGGER.warn("{} is carrying a generic product {}", o, o.product());
				String code = o.product().symbol();
				if (code.startsWith("6") || code.startsWith("51")) {
					exchangeId = "1";
				} else if (code.startsWith("0") || code.startsWith("3") || code.startsWith("1")) {
					exchangeId = "0";
				} else {
					LOGGER.error("Unable to handle product {}", o.product());
					continue;
				}
			}
			trade.CancelOrder(clientID, exchangeId, String.valueOf((int) o.id()), result, errInfo);
			String resultString = Native.toString(result, "GBK");
			String errorString = Native.toString(errInfo, "GBK");
			if (resultString.length() != 0) {
				o.id(Integer.parseInt(resultString.substring(resultString.indexOf('\n')+1).trim()));
				if (o instanceof HexinOrder) {
					((HexinOrder) o).ack(resultString);
				}
				LOGGER.info("{} cancelled. Result: {}", o, resultString);
			}
			if (errorString.length() != 0) {
				LOGGER.error("{} cancellation error: ", o, errorString);
			}
		}
	}

	public static void main (String [] args) {
		new LocalNativeTdxBroker("180.153.18.180", (short)7708, (short)2, "8009145070", "8009145070", "495179", "495179");
	}
}