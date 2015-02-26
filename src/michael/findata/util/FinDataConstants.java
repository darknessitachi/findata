package michael.findata.util;

import michael.findata.service.EnumTypeCodeListFile;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class FinDataConstants {

	public static final String BASE_NAME = "/michael/findata/init.properties";

	public static final String JDBC_URL = ResourceUtil.getString("michael.findata.db.url");

	public static final String JDBC_USER = ResourceUtil.getString("michael.findata.db.user");

	public static final String JDBC_PASS = ResourceUtil.getString("michael.findata.db.pass");

	public static final String STOCK_LIST_FILE = ResourceUtil.getString("michael.findata.stocklist");

	public static final EnumTypeCodeListFile STOCK_LIST_TYPE = EnumTypeCodeListFile.valueOf(ResourceUtil.getString("michael.findata.stocklist.type"));

	public static final String THS_BASE_DIR = ResourceUtil.getString("michael.findata.thsbase");

	public static final String TDX_BASE_DIR = ResourceUtil.getString("michael.findata.tdxbase");

	// The most recent # days covered when calculating stock price adjustment factor;
	public static final int DAYS_ADJ_FACTOR_CALC = ResourceUtil.getInt("michael.findata.days_adj_factor_calc");

	// The most recent # days covered when getting report publication dates from websites;
	public static final int DAYS_REPORT_PUB_DATES = ResourceUtil.getInt("michael.findata.days_report_pub_dates");

	public static final Date currentTimeStamp = new Date();

	// Some early dates to assume for date range lower bound in database
	// See more explanation from actual usages
	public static final Date EARLIEST = new Date(3, 3, 3);


	public static final Map<String, String> ABShareCodeRef = new HashMap<>();

	static {
		ABShareCodeRef.put("000002", "200002");
		ABShareCodeRef.put("000011", "200011");
		ABShareCodeRef.put("000012", "200012");
		ABShareCodeRef.put("000016", "200016");
		ABShareCodeRef.put("000017", "200017");
		ABShareCodeRef.put("000018", "200018");
		ABShareCodeRef.put("000019", "200019");
		ABShareCodeRef.put("000020", "200020");
		ABShareCodeRef.put("000022", "200022");
		ABShareCodeRef.put("000024", "200024");
		ABShareCodeRef.put("000025", "200025");
		ABShareCodeRef.put("000026", "200026");
		ABShareCodeRef.put("000028", "200028");
		ABShareCodeRef.put("000029", "200029");
		ABShareCodeRef.put("000030", "200030");
		ABShareCodeRef.put("000037", "200037");
		ABShareCodeRef.put("000039", "200039");
		ABShareCodeRef.put("000045", "200045");
		ABShareCodeRef.put("000055", "200055");
		ABShareCodeRef.put("000056", "200056");
		ABShareCodeRef.put("000058", "200058");
		ABShareCodeRef.put("000413", "200413");
		ABShareCodeRef.put("000418", "200418");
		ABShareCodeRef.put("000429", "200429");
		ABShareCodeRef.put("000488", "200488");
		ABShareCodeRef.put("000505", "200505");
		ABShareCodeRef.put("000513", "200513");
		ABShareCodeRef.put("000521", "200521");
		ABShareCodeRef.put("000530", "200530");
		ABShareCodeRef.put("000539", "200539");
		ABShareCodeRef.put("000541", "200541");
		ABShareCodeRef.put("000550", "200550");
		ABShareCodeRef.put("000553", "200553");
		ABShareCodeRef.put("000570", "200570");
		ABShareCodeRef.put("000581", "200581");
		ABShareCodeRef.put("000596", "200596");
		ABShareCodeRef.put("000613", "200613");
		ABShareCodeRef.put("000625", "200625");
		ABShareCodeRef.put("000725", "200725");
		ABShareCodeRef.put("000726", "200726");
		ABShareCodeRef.put("000761", "200761");
		ABShareCodeRef.put("000869", "200869");
		ABShareCodeRef.put("600602", "900901");
		ABShareCodeRef.put("600604", "900902");
		ABShareCodeRef.put("600611", "900903");
		ABShareCodeRef.put("600613", "900904");
		ABShareCodeRef.put("600612", "900905");
		ABShareCodeRef.put("600610", "900906");
		ABShareCodeRef.put("600614", "900907");
		ABShareCodeRef.put("600618", "900908");
		ABShareCodeRef.put("600623", "900909");
		ABShareCodeRef.put("600619", "900910");
		ABShareCodeRef.put("600639", "900911");
		ABShareCodeRef.put("600648", "900912");
		ABShareCodeRef.put("600617", "900913");
		ABShareCodeRef.put("600650", "900914");
		ABShareCodeRef.put("600818", "900915");
		ABShareCodeRef.put("600679", "900916");
		ABShareCodeRef.put("600851", "900917");
		ABShareCodeRef.put("600819", "900918");
		ABShareCodeRef.put("600695", "900919");
		ABShareCodeRef.put("600841", "900920");
		ABShareCodeRef.put("600844", "900921");
		ABShareCodeRef.put("600689", "900922");
		ABShareCodeRef.put("600827", "900923");
		ABShareCodeRef.put("600843", "900924");
		ABShareCodeRef.put("600835", "900925");
		ABShareCodeRef.put("600845", "900926");
		ABShareCodeRef.put("600822", "900927");
		ABShareCodeRef.put("600848", "900928");
		ABShareCodeRef.put("600680", "900930");
		ABShareCodeRef.put("600663", "900932");
		ABShareCodeRef.put("600801", "900933");
		ABShareCodeRef.put("600754", "900934");
		ABShareCodeRef.put("600295", "900936");
		ABShareCodeRef.put("600726", "900937");
		ABShareCodeRef.put("600751", "900938");
		ABShareCodeRef.put("600094", "900940");
		ABShareCodeRef.put("600776", "900941");
		ABShareCodeRef.put("600054", "900942");
		ABShareCodeRef.put("600272", "900943");
		ABShareCodeRef.put("600221", "900945");
		ABShareCodeRef.put("600698", "900946");
		ABShareCodeRef.put("600320", "900947");
		ABShareCodeRef.put("600190", "900952");
		ABShareCodeRef.put("600555", "900955");
	}

	public static final Map<String, String> BAShareCodeRef = new HashMap<>();

	static {
		BAShareCodeRef.put("200002", "000002");
		BAShareCodeRef.put("200011", "000011");
		BAShareCodeRef.put("200012", "000012");
		BAShareCodeRef.put("200016", "000016");
		BAShareCodeRef.put("200017", "000017");
		BAShareCodeRef.put("200018", "000018");
		BAShareCodeRef.put("200019", "000019");
		BAShareCodeRef.put("200020", "000020");
		BAShareCodeRef.put("200022", "000022");
		BAShareCodeRef.put("200024", "000024");
		BAShareCodeRef.put("200025", "000025");
		BAShareCodeRef.put("200026", "000026");
		BAShareCodeRef.put("200028", "000028");
		BAShareCodeRef.put("200029", "000029");
		BAShareCodeRef.put("200030", "000030");
		BAShareCodeRef.put("200037", "000037");
		BAShareCodeRef.put("200039", "000039");
		BAShareCodeRef.put("200045", "000045");
		BAShareCodeRef.put("200053", "200053");
		BAShareCodeRef.put("200054", "200054");
		BAShareCodeRef.put("200055", "000055");
		BAShareCodeRef.put("200056", "000056");
		BAShareCodeRef.put("200058", "000058");
		BAShareCodeRef.put("200152", "200152");
		BAShareCodeRef.put("200160", "200160");
		BAShareCodeRef.put("200168", "200168");
		BAShareCodeRef.put("200413", "000413");
		BAShareCodeRef.put("200418", "000418");
		BAShareCodeRef.put("200429", "000429");
		BAShareCodeRef.put("200468", "200468");
		BAShareCodeRef.put("200488", "000488");
		BAShareCodeRef.put("200505", "000505");
		BAShareCodeRef.put("200512", "200512");
		BAShareCodeRef.put("200513", "000513");
		BAShareCodeRef.put("200521", "000521");
		BAShareCodeRef.put("200530", "000530");
		BAShareCodeRef.put("200539", "000539");
		BAShareCodeRef.put("200541", "000541");
		BAShareCodeRef.put("200550", "000550");
		BAShareCodeRef.put("200553", "000553");
		BAShareCodeRef.put("200570", "000570");
		BAShareCodeRef.put("200581", "000581");
		BAShareCodeRef.put("200596", "000596");
		BAShareCodeRef.put("200613", "000613");
		BAShareCodeRef.put("200625", "000625");
		BAShareCodeRef.put("200706", "200706");
		BAShareCodeRef.put("200725", "000725");
		BAShareCodeRef.put("200726", "000726");
		BAShareCodeRef.put("200761", "000761");
		BAShareCodeRef.put("200770", "200770");
		BAShareCodeRef.put("200771", "200771");
		BAShareCodeRef.put("200869", "000869");
		BAShareCodeRef.put("200986", "200986");
		BAShareCodeRef.put("200992", "200992");

		BAShareCodeRef.put("900901", "600602");
		BAShareCodeRef.put("900902", "600604");
		BAShareCodeRef.put("900903", "600611");
		BAShareCodeRef.put("900904", "600613");
		BAShareCodeRef.put("900905", "600612");
		BAShareCodeRef.put("900906", "600610");
		BAShareCodeRef.put("900907", "600614");
		BAShareCodeRef.put("900908", "600618");
		BAShareCodeRef.put("900909", "600623");
		BAShareCodeRef.put("900910", "600619");
		BAShareCodeRef.put("900911", "600639");
		BAShareCodeRef.put("900912", "600648");
		BAShareCodeRef.put("900913", "600617");
		BAShareCodeRef.put("900914", "600650");
		BAShareCodeRef.put("900915", "600818");
		BAShareCodeRef.put("900916", "600679");
		BAShareCodeRef.put("900917", "600851");
		BAShareCodeRef.put("900918", "600819");
		BAShareCodeRef.put("900919", "600695");
		BAShareCodeRef.put("900920", "600841");
		BAShareCodeRef.put("900921", "600844");
		BAShareCodeRef.put("900922", "600689");
		BAShareCodeRef.put("900923", "600827");
		BAShareCodeRef.put("900924", "600843");
		BAShareCodeRef.put("900925", "600835");
		BAShareCodeRef.put("900926", "600845");
		BAShareCodeRef.put("900927", "600822");
		BAShareCodeRef.put("900928", "600848");
		BAShareCodeRef.put("900929", "900929");
		BAShareCodeRef.put("900930", "600680");
		BAShareCodeRef.put("900932", "600663");
		BAShareCodeRef.put("900933", "600801");
		BAShareCodeRef.put("900934", "600754");
		BAShareCodeRef.put("900935", "900935");
		BAShareCodeRef.put("900936", "600295");
		BAShareCodeRef.put("900937", "600726");
		BAShareCodeRef.put("900938", "600751");
		BAShareCodeRef.put("900939", "900939");
		BAShareCodeRef.put("900940", "600094");
		BAShareCodeRef.put("900941", "600776");
		BAShareCodeRef.put("900942", "600054");
		BAShareCodeRef.put("900943", "600272");
		BAShareCodeRef.put("900945", "600221");
		BAShareCodeRef.put("900946", "600698");
		BAShareCodeRef.put("900947", "600320");
		BAShareCodeRef.put("900948", "900948");
		BAShareCodeRef.put("900949", "900949");
		BAShareCodeRef.put("900950", "900950");
		BAShareCodeRef.put("900951", "900951");
		BAShareCodeRef.put("900952", "600190");
		BAShareCodeRef.put("900953", "900953");
		BAShareCodeRef.put("900955", "600555");
		BAShareCodeRef.put("900956", "900956");
		BAShareCodeRef.put("900957", "900957");
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

	public final static String yyyyDashMMDashdd = "yyyy-MM-dd";
	public final static SimpleDateFormat FORMAT_yyyyDashMMDashdd = new SimpleDateFormat(yyyyDashMMDashdd);
	public final static DateTimeFormatter NEW_FORMATTER_yyyyDashMMDashdd = DateTimeFormatter.ofPattern (yyyyDashMMDashdd);

	public final static String yyyyMMdd = "yyyyMMdd";
	public final static SimpleDateFormat FORMAT_yyyyMMdd = new SimpleDateFormat(yyyyMMdd);

	public final static String yyMMdd = "yyMMdd";
	public final static SimpleDateFormat FORMAT_yyMMdd = new SimpleDateFormat(yyMMdd);

	public static String s1Report = "一季";
	public static String s2Report = "半年";
	public static String s2Report2 = "中期";
	public static String s3Report = "三季";
	public static String s4Report = "年度";
	public static String s4Report2 = "年";
}