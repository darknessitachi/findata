package michael.findata.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class FinDataConstants {

	public static final String BASE_NAME = "/michael/findata/resource/init.properties";

	public static final String JDBC_URL = ResourceUtil.getString("michael.findata.db.url");

	public static final String JDBC_USER = ResourceUtil.getString("michael.findata.db.user");

	public static final String JDBC_PASS = ResourceUtil.getString("michael.findata.db.pass");

	public static final String STOCK_LIST_FILE = ResourceUtil.getString("michael.findata.stocklist");

	public static final String THS_BASE_DIR = ResourceUtil.getString("michael.findata.thsbase");

	public static final String TDX_BASE_DIR = ResourceUtil.getString("michael.findata.tdxbase");

	public static final Date currentTimeStamp = new Date();

	public static final Map<String, String> ABShareCodeRef = new HashMap<>();

	static {
		ABShareCodeRef.put("200002", "000002");
		ABShareCodeRef.put("200011", "000011");
		ABShareCodeRef.put("200012", "000012");
		ABShareCodeRef.put("200016", "000016");
		ABShareCodeRef.put("200017", "000017");
		ABShareCodeRef.put("200018", "000018");
		ABShareCodeRef.put("200019", "000019");
		ABShareCodeRef.put("200020", "000020");
		ABShareCodeRef.put("200022", "000022");
		ABShareCodeRef.put("200024", "000024");
		ABShareCodeRef.put("200025", "000025");
		ABShareCodeRef.put("200026", "000026");
		ABShareCodeRef.put("200028", "000028");
		ABShareCodeRef.put("200029", "000029");
		ABShareCodeRef.put("200030", "000030");
		ABShareCodeRef.put("200037", "000037");
		ABShareCodeRef.put("200039", "000039");
		ABShareCodeRef.put("200045", "000045");
		ABShareCodeRef.put("200053", "200053");
		ABShareCodeRef.put("200054", "200054");
		ABShareCodeRef.put("200055", "000055");
		ABShareCodeRef.put("200056", "000056");
		ABShareCodeRef.put("200058", "000058");
		ABShareCodeRef.put("200152", "200152");
		ABShareCodeRef.put("200160", "200160");
		ABShareCodeRef.put("200168", "200168");
		ABShareCodeRef.put("200413", "000413");
		ABShareCodeRef.put("200418", "000418");
		ABShareCodeRef.put("200429", "000429");
		ABShareCodeRef.put("200468", "200468");
		ABShareCodeRef.put("200488", "000488");
		ABShareCodeRef.put("200505", "000505");
		ABShareCodeRef.put("200512", "200512");
		ABShareCodeRef.put("200513", "000513");
		ABShareCodeRef.put("200521", "000521");
		ABShareCodeRef.put("200530", "000530");
		ABShareCodeRef.put("200539", "000539");
		ABShareCodeRef.put("200541", "000541");
		ABShareCodeRef.put("200550", "000550");
		ABShareCodeRef.put("200553", "000553");
		ABShareCodeRef.put("200570", "000570");
		ABShareCodeRef.put("200581", "000581");
		ABShareCodeRef.put("200596", "000596");
		ABShareCodeRef.put("200613", "000613");
		ABShareCodeRef.put("200625", "000625");
		ABShareCodeRef.put("200706", "200706");
		ABShareCodeRef.put("200725", "000725");
		ABShareCodeRef.put("200726", "000726");
		ABShareCodeRef.put("200761", "000761");
		ABShareCodeRef.put("200771", "200771");
		ABShareCodeRef.put("200869", "000869");
		ABShareCodeRef.put("200986", "200986");
		ABShareCodeRef.put("200992", "200992");

		ABShareCodeRef.put("900901", "600602");
		ABShareCodeRef.put("900902", "600604");
		ABShareCodeRef.put("900903", "600611");
		ABShareCodeRef.put("900904", "600613");
		ABShareCodeRef.put("900905", "600612");
		ABShareCodeRef.put("900906", "600610");
		ABShareCodeRef.put("900907", "600614");
		ABShareCodeRef.put("900908", "600618");
		ABShareCodeRef.put("900909", "600623");
		ABShareCodeRef.put("900910", "600619");
		ABShareCodeRef.put("900911", "600639");
		ABShareCodeRef.put("900912", "600648");
		ABShareCodeRef.put("900913", "600617");
		ABShareCodeRef.put("900914", "600650");
		ABShareCodeRef.put("900915", "600818");
		ABShareCodeRef.put("900916", "600679");
		ABShareCodeRef.put("900917", "600851");
		ABShareCodeRef.put("900918", "600819");
		ABShareCodeRef.put("900919", "600695");
		ABShareCodeRef.put("900920", "600841");
		ABShareCodeRef.put("900921", "600844");
		ABShareCodeRef.put("900922", "600689");
		ABShareCodeRef.put("900923", "600827");
		ABShareCodeRef.put("900924", "600843");
		ABShareCodeRef.put("900925", "600835");
		ABShareCodeRef.put("900926", "600845");
		ABShareCodeRef.put("900927", "600822");
		ABShareCodeRef.put("900928", "600848");
		ABShareCodeRef.put("900929", "900929");
		ABShareCodeRef.put("900930", "600680");
		ABShareCodeRef.put("900932", "600663");
		ABShareCodeRef.put("900933", "600801");
		ABShareCodeRef.put("900934", "600754");
		ABShareCodeRef.put("900935", "900935");
		ABShareCodeRef.put("900936", "600295");
		ABShareCodeRef.put("900937", "600726");
		ABShareCodeRef.put("900938", "600751");
		ABShareCodeRef.put("900939", "900939");
		ABShareCodeRef.put("900940", "600094");
		ABShareCodeRef.put("900941", "600776");
		ABShareCodeRef.put("900942", "600054");
		ABShareCodeRef.put("900943", "600272");
		ABShareCodeRef.put("900945", "600221");
		ABShareCodeRef.put("900947", "600320");
		ABShareCodeRef.put("900948", "900948");
		ABShareCodeRef.put("900949", "900949");
		ABShareCodeRef.put("900950", "900950");
		ABShareCodeRef.put("900951", "900951");
		ABShareCodeRef.put("900952", "600190");
		ABShareCodeRef.put("900953", "900953");
		ABShareCodeRef.put("900955", "600555");
		ABShareCodeRef.put("900956", "900956");
		ABShareCodeRef.put("900957", "900957");
	}

//	public static final String FINANCIAL_SHEET_BALANCE_SHEET = "balance_sheet";
//	public static final String FINANCIAL_SHEET_PROFIT_AND_LOSS = "profit_and_loss";
//	public static final String FINANCIAL_SHEET_CASH_FLOW = "cash_flow";
//	public static final String FINANCIAL_SHEET_PROVISION = "provision";

	public static enum SheetType {
		balance_sheet,
		profit_and_loss,
		cash_flow,
		provision
	}

	public static enum CompanyType {
		financial,
		non_financial
	}

	public static DecimalFormat CommonDecimalFormat = new DecimalFormat("##,###.00");

	public static SimpleDateFormat yyyyDashMMDashdd = new SimpleDateFormat("yyyy-MM-dd");

	public static SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
}