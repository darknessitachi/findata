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
		//基本版函数
		//打开通达信
		void OpenTdx();

		//关闭通达信
		void CloseTdx();

		/// <summary>
		/// 交易账户登录
		/// </summary>
		/// <param name="IP">券商交易服务器IP</param>
		/// <param name="Port">券商交易服务器端口</param>
		/// <param name="Version">设置通达信客户端的版本号</param>
		/// <param name="YybID">营业部代码，请到网址 http://www.chaoguwaigua.com/downloads/qszl.htm 查询</param>
		/// <param name="AccountNo">完整的登录账号，券商一般使用资金帐户或客户号</param>
		/// <param name="TradeAccount">交易账号，一般与登录帐号相同. 请登录券商通达信软件，查询股东列表，股东列表内的资金帐号就是交易帐号, 具体查询方法请见网站“热点问答”栏目</param>
		/// <param name="JyPassword">交易密码</param>
		/// <param name="TxPassword">通讯密码</param>
		/// <param name="ErrInfo">此API执行返回后，如果出错，保存了错误信息说明。一般要分配256字节的空间。没出错时为空字符串。</param>
		/// <returns>客户端ID，失败时返回-1</returns>
		int Logon(String IP, short Port, String Version, short YybID, String AccountNo, String TradeAccount, String JyPassword, String TxPassword, byte[] ErrInfo);

		//注销
		void Logoff(int ClientID);

		/// <summary>
		/// 查询各种交易数据
		/// </summary>
		/// <param name="clientID">客户端ID</param>
		/// <param name="Category">表示查询信息的种类，0资金  1股份   2当日委托  3当日成交     4可撤单   5股东代码  6融资余额   7融券余额  8可融证券</param>
		/// <param name="Result">此API执行返回后，Result内保存了返回的查询数据, 形式为表格数据，行数据之间通过\n字符分割，列数据之间通过\t分隔。一般要分配1024*1024字节的空间。出错时为空字符串。</param>
		/// <param name="ErrInfo">此API执行返回后，如果出错，保存了错误信息说明。一般要分配256字节的空间。没出错时为空字符串。</param>
		void QueryData(int ClientID, int Category, byte[] Result, byte[] ErrInfo);

		/// 下委托交易证券
		/// </summary>
		/// <param name="clientID">客户端ID</param>
		/// <param name="Category">表示委托的种类
		///							0买入
		///							1卖出
		///							2融资买入
		///							3融券卖出
		///							4买券还券
		///							5卖券还款
		///							6现券还券
		///	</param>
		/// <param name="PriceType">表示报价方式
		///							0上海限价委托 深圳限价委托
		/// 						1(市价委托)深圳对方最优价格
		///							2(市价委托)深圳本方最优价格
		///							3(市价委托)深圳即时成交剩余撤销
		///							4(市价委托)上海五档即成剩撤 深圳五档即成剩撤
		///							5(市价委托)深圳全额成交或撤销
		///							6(市价委托)上海五档即成转限价
		///	</param>
		/// <param name="Gddm">股东代码, 交易上海股票填上海的股东代码；交易深圳的股票填入深圳的股东代码</param>
		/// <param name="Zqdm">证券代码</param>
		/// <param name="Price">委托价格</param>
		/// <param name="Quantity">委托数量</param>
		/// <param name="Result">此API执行返回后，Result内保存了返回的查询数据, 形式为表格数据，行数据之间通过\n字符分割，列数据之间通过\t分隔。
		/// 一般要分配1024*1024字节的空间。出错时为空字符串。其中含有委托编号数据</param>
		/// <param name="ErrInfo">此API执行返回后，如果出错，保存了错误信息说明。一般要分配256字节的空间。没出错时为空字符串。</param>
		void SendOrder(int ClientID, int Category, int PriceType, String Gddm, String Zqdm, float Price, int Quantity, byte[] Result, byte[] ErrInfo);

		/// <summary>
		/// 撤委托
		/// </summary>
		/// <param name="clientID">客户端ID</param>
		/// <param name="ExchangeID">交易所ID， 上海1，深圳0(招商证券普通账户深圳是2)</param>
		/// <param name="hth">表示要撤的目标委托的编号</param>
		/// <param name="Result">此API执行返回后，Result内保存了返回的查询数据, 形式为表格数据，行数据之间通过\n字符分割，列数据之间通过\t分隔。
		/// 一般要分配1024*1024字节的空间。出错时为空字符串。其中含有委托编号数据</param>
		/// <param name="ErrInfo">此API执行返回后，如果出错，保存了错误信息说明。一般要分配256字节的空间。没出错时为空字符串。</param>
		void CancelOrder(int ClientID, String ExchangeID, String hth, byte[] Result, byte[] ErrInfo);

		/// <summary>
		/// 获取证券的实时五档行情
		/// </summary>
		/// <param name="clientID">客户端ID</param>
		/// <param name="Zqdm">证券代码</param>
		/// <param name="Result">同上</param>
		/// <param name="ErrInfo">此API执行返回后，如果出错，保存了错误信息说明。一般要分配256字节的空间。没出错时为空字符串。</param>
		void GetQuote(int ClientID, String Zqdm, byte[] Result, byte[] ErrInfo);

		/// <summary>
		/// 融资融券账户直接还款
		/// </summary>
		/// <param name="clientID">客户端ID</param>
		/// <param name="Amount">还款金额</param>
		/// <param name="Result">同上</param>
		/// <param name="ErrInfo">此API执行返回后，如果出错，保存了错误信息说明。一般要分配256字节的空间。没出错时为空字符串。</param>
		void Repay(int ClientID, String Amount, byte[] Result, byte[] ErrInfo);

		///交易接口执行后，如果失败，则字符串ErrInfo保存了出错信息中文说明；
		///如果成功，则字符串Result保存了结果数据,形式为表格数据，行数据之间通过\n字符分割，列数据之间通过\t分隔。
		///Result是\n，\t分隔的中文字符串，比如查询股东代码时返回的结果字符串就是

		///"股东代码\t股东名称\t帐号类别\t保留信息\n
		///0000064567\t\t0\t\nA000064567\t\t1\t\n
		///2000064567\t\t2\t\nB000064567\t\t3\t"

		///查得此数据之后，通过分割字符串， 可以恢复为几行几列的表格形式的数据

		//API使用流程为: 应用程序先调用OpenTdx打开通达信实例，一个实例下可以同时登录多个交易账户，每个交易账户称之为ClientID.
		//通过调用Logon获得ClientID，然后可以调用其他API函数向各个ClientID进行查询或下单;
		//应用程序退出时应调用Logoff注销ClientID, 最后调用CloseTdx关闭通达信实例.
		//OpenTdx和CloseTdx在整个应用程序中只能被调用一次.API带有断线自动重连功能，应用程序只需根据API函数返回的出错信息进行适当错误处理即可
	}

	private TdxLibrary trade;
	private int clientID;
	private String gddmSH = "E042255853";
	private String gddmSZ = "0604227902";

	public LocalNativeTdxBroker (String ip, short port, short yingyebuID, String accountNo, String tradeAccount, String pass, String commsPass) {
		//DLL是32位的,因此必须使用jdk32位开发,才能调用DLL;
		//必须把Trade.dll等4个DLL复制到java工程目录下;
		//java工程必须添加引用 jna.jar, 在 https://github.com/twall/jna 下载 jna.jar
		//无论用什么语言编程，都必须仔细阅读VC版内的关于DLL导出函数的功能和参数含义说明，不仔细阅读完就提出问题者因时间精力所限，恕不解答。
		byte[] Result = new byte[1024];
		byte[] ErrInfo = new byte[256];

		trade = (TdxLibrary) Native.loadLibrary("Trade", TdxLibrary.class);
		trade.OpenTdx();
		//登录
//		clientID = trade.Logon("180.153.18.180", (short) 7708, "6.33", (short) 2, "8009145070", "8009145070", "495179", "495179", ErrInfo);
		long time = System.currentTimeMillis();
		clientID = trade.Logon(ip, port, "6.33", yingyebuID, accountNo, tradeAccount, pass, commsPass, ErrInfo);
		System.out.println(System.currentTimeMillis() - time);

		if (clientID == -1) {
			LOGGER.error(Native.toString(ErrInfo, "GBK"));
			return;
		}
		LOGGER.info("Tdx native broker logon successful!");

		//查询
		trade.QueryData(clientID, 5, Result, ErrInfo);
		//TdxLibrary1.QueryData(ClientID2, 0, Result, ErrInfo);//第二个帐号查询资金
		LOGGER.info(Native.toString(Result, "GBK"));
	}

	@Override
	public void stop() {
		//注销
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