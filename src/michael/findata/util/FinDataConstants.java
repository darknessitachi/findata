package michael.findata.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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

	public static String s1Report = "一季";
	public static String s2Report = "半年";
	public static String s2Report2 = "中期";
	public static String s3Report = "三季";
	public static String s4Report = "年度";

	public static Pattern p = Pattern.compile("target=new>.*(\\d\\d\\d\\d).*年(.*)报告(摘要|正文|全文)?(（已取消）)?<.*(\\d\\d\\d\\d-\\d\\d-\\d\\d)");
}