package michael.findata.util;

/**
 * Created by IntelliJ IDEA.
 * User: michaelt
 * Date: 2010-11-5
 * Time: 18:19:10
 * To change this template use File | Settings | File Templates.
 */
public final class FinDataConstants {

	public static final String BASE_NAME = "/michael/findata/resource/init.properties";

	public static final String JDBC_URL = ResourceUtil.getString("michael.findata.db.url");

	public static final String JDBC_USER = ResourceUtil.getString("michael.findata.db.user");

	public static final String JDBC_PASS = ResourceUtil.getString("michael.findata.db.pass");

	public static final String STOCK_LIST_FILE = ResourceUtil.getString("michael.findata.stocklist");

	public static final String FINANCIAL_SHEET_BALANCE_SHEET = "balance_sheet";
	public static final String FINANCIAL_SHEET_PROFIT_AND_LOSS = "profit_and_loss";
	public static final String FINANCIAL_SHEET_CASH_FLOW = "cash_flow";
	public static final String FINANCIAL_SHEET_PROVISION = "provision";

	/**
	 * ����
	 */
	public static final String PASSWORD = ResourceUtil.getString("com.maesinfo.user.password");

	/**
	 * ��֤��URL
	 */
	public static final String CODE_URL = ResourceUtil.getString("com.maesinfo.code.url");

	/**
	 * ������֤����·��
	 */
	public static final String PATH = ResourceUtil.getString("com.maesinfo.code.path");

	/**
	 * ������
	 */
	public static final String SERVER = ResourceUtil.getString("com.maesinfo.site.server");

	/**
	 * ����
	 */
	public static final String LANGUAGE = ResourceUtil.getString("com.maesinfo.site.language");

	/**
	 * �û���¼���齨Key
	 */

	/**
	 * ����key
	 */
	public static final String LANGUAGE_KEY = ResourceUtil.getString("com.maesinfo.form.language.key");

	/**
	 * ����Աkey
	 */
	public static final String ADMIN_KEY = ResourceUtil.getString("com.maesinfo.form.admin.key");

	/**
	 * ����key
	 */
	public static final String PASSWORD_KEY = ResourceUtil.getString("com.maesinfo.form.password.key");

	/**
	 * ��֤��key
	 */
	public static final String CHECKCODE_KEY = ResourceUtil.getString("com.maesinfo.form.checkcode.key");

	/**
	 * �������key
	 */
	public static final String HIDKEY_KEY = ResourceUtil.getString("com.maesinfo.form.hidKey.key");

	/**
	 * ����Сд����key
	 */
	public static final String HIDELOWERCASEPW_KEY = ResourceUtil.getString("com.maesinfo.form.hidLowerCasePW.key");

	/**
	 * ������key
	 */
	public static final String HIDSERVERKEY_KEY = ResourceUtil.getString("com.maesinfo.form.hidServerKey.key");

	/**
	 * �ָ���
	 */
	public static final String SPLIT = "'";

	/**
	 * ˽�й�����
	 */
	private FinDataConstants() {
	}
}