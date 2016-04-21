package michael.findata.external.tdx;

import com.sun.jna.Library;
import com.sun.jna.ptr.ShortByReference;

public interface TDXHqLibrary extends Library {

	//连接券商行情服务器
	/// <summary>
	///  连接通达信行情服务器
	/// </summary>
	/// <param name="IP">服务器IP,可在券商通达信软件登录界面“通讯设置”按钮内查得</param>
	/// <param name="Port">服务器端口</param>
	/// <param name="Result">此API执行返回后，Result内保存了返回的查询数据, 形式为表格数据，行数据之间通过\n字符分割，列数据之间通过\t分隔。一般要分配1024*1024字节的空间。出错时为空字符串。</param>
	/// <param name="ErrInfo">此API执行返回后，如果出错，保存了错误信息说明。一般要分配256字节的空间。没出错时为空字符串。</param>
	/// <returns>成功返货true, 失败返回false</returns>
	boolean TdxHq_Connect(String IP, int Port, byte[] Result, byte[] ErrInfo);

	//断开服务器
	void TdxHq_Disconnect();

	//获取指定市场内的证券数目
	/// <summary>
	/// 获取市场内所有证券的数量
	/// </summary>
	/// <param name="Market">市场代码,   0->深圳     1->上海</param>
	/// <param name="Result">此API执行返回后，Result内保存了返回的证券数量</param>
	/// <param name="ErrInfo">此API执行返回后，如果出错，保存了错误信息说明。一般要分配256字节的空间。没出错时为空字符串。</param>
	/// <returns>成功返货true, 失败返回false</returns>
	boolean TdxHq_GetSecurityCount(byte Market, ShortByReference Result, byte[] ErrInfo);

	//获取市场内指定范围内的所有证券代码
	/// <summary>
	/// 获取市场内某个范围内的1000支股票的股票代码
	/// </summary>
	/// <param name="Market">市场代码,   0->深圳     1->上海</param>
	/// <param name="Start">范围开始位置,第一个股票是0, 第二个是1, 依此类推,位置信息依据TdxHq_GetSecurityCount返回的证券总数确定</param>
	/// <param name="Count">范围的大小，API执行后,保存了实际返回的股票数目,</param>
	/// <param name="Result">此API执行返回后，Result内保存了返回的证券代码信息,形式为表格数据，行数据之间通过\n字符分割，列数据之间通过\t分隔。一般要分配1024*1024字节的空间。出错时为空字符串。</param>
	/// <param name="ErrInfo">此API执行返回后，如果出错，保存了错误信息说明。一般要分配256字节的空间。没出错时为空字符串。</param>
	/// <returns>成功返货true, 失败返回false</returns>
	boolean TdxHq_GetSecurityList(byte Market, short Start, ShortByReference Count, byte[] Result, byte[] ErrInfo);

	//获取盘口五档报价
	/// <summary>
	/// 批量获取多个证券的五档报价数据
	/// </summary>
	/// <param name="Market">市场代码,   0->深圳     1->上海, 第i个元素表示第i个证券的市场代码</param>
	/// <param name="Zqdm">证券代码, Count个证券代码组成的数组</param>
	/// <param name="Count">API执行前,表示用户要请求的证券数目,最大50(不同券商可能不一样,具体数目请自行咨询券商或测试), API执行后,保存了实际返回的数目</param>
	/// <param name="Result">此API执行返回后，Result内保存了返回的查询数据, 形式为表格数据，行数据之间通过\n字符分割，列数据之间通过\t分隔。一般要分配1024*1024字节的空间。出错时为空字符串。</param>
	/// <param name="ErrInfo">此API执行返回后，如果出错，保存了错误信息说明。一般要分配256字节的空间。没出错时为空字符串。</param>
	/// <returns>成功返货true, 失败返回false</returns>
	boolean TdxHq_GetSecurityQuotes(byte[] Market, String[] Zqdm, ShortByReference Count, byte[] Result, byte[] ErrInfo);

	//获取股票K线
	boolean TdxHq_GetSecurityBars(byte Category, byte Market, String Zqdm, short Start, ShortByReference Count, byte[] Result, byte[] ErrInfo);

	//获取指数K线
	boolean TdxHq_GetIndexBars(byte Category, byte Market, String Zqdm, short Start, ShortByReference Count, byte[] Result, byte[] ErrInfo);

	//获取分时图数据
	boolean TdxHq_GetMinuteTimeData(byte Market, String Zqdm, byte[] Result, byte[] ErrInfo);

	//获取历史分时图数据
	boolean TdxHq_GetHistoryMinuteTimeData(byte Market, String Zqdm, int date, byte[] Result, byte[] ErrInfo);

	//获取分时成交
	boolean TdxHq_GetTransactionData(byte Market, String Zqdm, short Start, ShortByReference Count, byte[] Result, byte[] ErrInfo);

	//获取历史分时成交
	boolean TdxHq_GetHistoryTransactionData(byte Market, String Zqdm, short Start, ShortByReference Count, int date, byte[] Result, byte[] ErrInfo);

	//获取F10信息类别
	boolean TdxHq_GetCompanyInfoCategory(byte Market, String Zqdm, byte[] Result, byte[] ErrInfo);

	//获取F10信息内容
	boolean TdxHq_GetCompanyInfoContent(byte Market, String Zqdm, String FileName, int Start, int Length, byte[] Result, byte[] ErrInfo);

	//获取权息数据
	boolean TdxHq_GetXDXRInfo(byte Market, String Zqdm, byte[] Result, byte[] ErrInfo);

	//获取财务数据
	/// <summary>
	/// 获取财务信息
	/// </summary>
	/// <param name="Market">市场代码,   0->深圳     1->上海</param>
	/// <param name="Zqdm">证券代码</param>
	/// <param name="Result">此API执行返回后，Result内保存了返回的查询数据,出错时为空字符串。</param>
	/// <param name="ErrInfo">此API执行返回后，如果出错，保存了错误信息说明。一般要分配256字节的空间。没出错时为空字符串。</param>
	/// <returns>成功返货true, 失败返回false</returns>
	boolean TdxHq_GetFinanceInfo(byte Market, String Zqdm, byte[] Result, byte[] ErrInfo);
}
