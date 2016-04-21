package michael.findata.external.tdx;

import com.sun.jna.Library;
import com.sun.jna.ptr.ShortByReference;

public interface TDXHqLibrary extends Library {

	//����ȯ�����������
	/// <summary>
	///  ����ͨ�������������
	/// </summary>
	/// <param name="IP">������IP,����ȯ��ͨ���������¼���桰ͨѶ���á���ť�ڲ��</param>
	/// <param name="Port">�������˿�</param>
	/// <param name="Result">��APIִ�з��غ�Result�ڱ����˷��صĲ�ѯ����, ��ʽΪ������ݣ�������֮��ͨ��\n�ַ��ָ������֮��ͨ��\t�ָ���һ��Ҫ����1024*1024�ֽڵĿռ䡣����ʱΪ���ַ�����</param>
	/// <param name="ErrInfo">��APIִ�з��غ�������������˴�����Ϣ˵����һ��Ҫ����256�ֽڵĿռ䡣û����ʱΪ���ַ�����</param>
	/// <returns>�ɹ�����true, ʧ�ܷ���false</returns>
	boolean TdxHq_Connect(String IP, int Port, byte[] Result, byte[] ErrInfo);

	//�Ͽ�������
	void TdxHq_Disconnect();

	//��ȡָ���г��ڵ�֤ȯ��Ŀ
	/// <summary>
	/// ��ȡ�г�������֤ȯ������
	/// </summary>
	/// <param name="Market">�г�����,   0->����     1->�Ϻ�</param>
	/// <param name="Result">��APIִ�з��غ�Result�ڱ����˷��ص�֤ȯ����</param>
	/// <param name="ErrInfo">��APIִ�з��غ�������������˴�����Ϣ˵����һ��Ҫ����256�ֽڵĿռ䡣û����ʱΪ���ַ�����</param>
	/// <returns>�ɹ�����true, ʧ�ܷ���false</returns>
	boolean TdxHq_GetSecurityCount(byte Market, ShortByReference Result, byte[] ErrInfo);

	//��ȡ�г���ָ����Χ�ڵ�����֤ȯ����
	/// <summary>
	/// ��ȡ�г���ĳ����Χ�ڵ�1000֧��Ʊ�Ĺ�Ʊ����
	/// </summary>
	/// <param name="Market">�г�����,   0->����     1->�Ϻ�</param>
	/// <param name="Start">��Χ��ʼλ��,��һ����Ʊ��0, �ڶ�����1, ��������,λ����Ϣ����TdxHq_GetSecurityCount���ص�֤ȯ����ȷ��</param>
	/// <param name="Count">��Χ�Ĵ�С��APIִ�к�,������ʵ�ʷ��صĹ�Ʊ��Ŀ,</param>
	/// <param name="Result">��APIִ�з��غ�Result�ڱ����˷��ص�֤ȯ������Ϣ,��ʽΪ������ݣ�������֮��ͨ��\n�ַ��ָ������֮��ͨ��\t�ָ���һ��Ҫ����1024*1024�ֽڵĿռ䡣����ʱΪ���ַ�����</param>
	/// <param name="ErrInfo">��APIִ�з��غ�������������˴�����Ϣ˵����һ��Ҫ����256�ֽڵĿռ䡣û����ʱΪ���ַ�����</param>
	/// <returns>�ɹ�����true, ʧ�ܷ���false</returns>
	boolean TdxHq_GetSecurityList(byte Market, short Start, ShortByReference Count, byte[] Result, byte[] ErrInfo);

	//��ȡ�̿��嵵����
	/// <summary>
	/// ������ȡ���֤ȯ���嵵��������
	/// </summary>
	/// <param name="Market">�г�����,   0->����     1->�Ϻ�, ��i��Ԫ�ر�ʾ��i��֤ȯ���г�����</param>
	/// <param name="Zqdm">֤ȯ����, Count��֤ȯ������ɵ�����</param>
	/// <param name="Count">APIִ��ǰ,��ʾ�û�Ҫ�����֤ȯ��Ŀ,���50(��ͬȯ�̿��ܲ�һ��,������Ŀ��������ѯȯ�̻����), APIִ�к�,������ʵ�ʷ��ص���Ŀ</param>
	/// <param name="Result">��APIִ�з��غ�Result�ڱ����˷��صĲ�ѯ����, ��ʽΪ������ݣ�������֮��ͨ��\n�ַ��ָ������֮��ͨ��\t�ָ���һ��Ҫ����1024*1024�ֽڵĿռ䡣����ʱΪ���ַ�����</param>
	/// <param name="ErrInfo">��APIִ�з��غ�������������˴�����Ϣ˵����һ��Ҫ����256�ֽڵĿռ䡣û����ʱΪ���ַ�����</param>
	/// <returns>�ɹ�����true, ʧ�ܷ���false</returns>
	boolean TdxHq_GetSecurityQuotes(byte[] Market, String[] Zqdm, ShortByReference Count, byte[] Result, byte[] ErrInfo);

	//��ȡ��ƱK��
	boolean TdxHq_GetSecurityBars(byte Category, byte Market, String Zqdm, short Start, ShortByReference Count, byte[] Result, byte[] ErrInfo);

	//��ȡָ��K��
	boolean TdxHq_GetIndexBars(byte Category, byte Market, String Zqdm, short Start, ShortByReference Count, byte[] Result, byte[] ErrInfo);

	//��ȡ��ʱͼ����
	boolean TdxHq_GetMinuteTimeData(byte Market, String Zqdm, byte[] Result, byte[] ErrInfo);

	//��ȡ��ʷ��ʱͼ����
	boolean TdxHq_GetHistoryMinuteTimeData(byte Market, String Zqdm, int date, byte[] Result, byte[] ErrInfo);

	//��ȡ��ʱ�ɽ�
	boolean TdxHq_GetTransactionData(byte Market, String Zqdm, short Start, ShortByReference Count, byte[] Result, byte[] ErrInfo);

	//��ȡ��ʷ��ʱ�ɽ�
	boolean TdxHq_GetHistoryTransactionData(byte Market, String Zqdm, short Start, ShortByReference Count, int date, byte[] Result, byte[] ErrInfo);

	//��ȡF10��Ϣ���
	boolean TdxHq_GetCompanyInfoCategory(byte Market, String Zqdm, byte[] Result, byte[] ErrInfo);

	//��ȡF10��Ϣ����
	boolean TdxHq_GetCompanyInfoContent(byte Market, String Zqdm, String FileName, int Start, int Length, byte[] Result, byte[] ErrInfo);

	//��ȡȨϢ����
	boolean TdxHq_GetXDXRInfo(byte Market, String Zqdm, byte[] Result, byte[] ErrInfo);

	//��ȡ��������
	/// <summary>
	/// ��ȡ������Ϣ
	/// </summary>
	/// <param name="Market">�г�����,   0->����     1->�Ϻ�</param>
	/// <param name="Zqdm">֤ȯ����</param>
	/// <param name="Result">��APIִ�з��غ�Result�ڱ����˷��صĲ�ѯ����,����ʱΪ���ַ�����</param>
	/// <param name="ErrInfo">��APIִ�з��غ�������������˴�����Ϣ˵����һ��Ҫ����256�ֽڵĿռ䡣û����ʱΪ���ַ�����</param>
	/// <returns>�ɹ�����true, ʧ�ܷ���false</returns>
	boolean TdxHq_GetFinanceInfo(byte Market, String Zqdm, byte[] Result, byte[] ErrInfo);
}
