import michael.findata.external.FinancialSheet;
import michael.findata.external.hexun2008.Hexun2008FinancialSheet;
import michael.findata.util.FinDataConstants;
import michael.findata.util.ResourceUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static michael.findata.util.FinDataConstants.JDBC_PASS;
import static michael.findata.util.FinDataConstants.JDBC_URL;
import static michael.findata.util.FinDataConstants.JDBC_USER;
import static michael.findata.util.FinDataConstants.SheetType.*;
import static michael.findata.util.FinDataConstants.SheetType.profit_and_loss;

public class Test {
	public static void main (String [] args) throws ClassNotFoundException, SQLException, InstantiationException, IOException, IllegalAccessException {
//		Matcher m = FinDataConstants.p.matcher("    <li><span class=\"title\">·<a href=\"/finalpage/2008-04-29/39222641.PDF\" target=new>吉林敖东：2008年第一季度报告</a>&nbsp;&nbsp;<img align=absMiddle border=0 height=16 src='/imagesnew/Pdf.gif' width=16> (138K) </span> <span class=\"time\">2008-04-29 06:30</span></li>");
//		Matcher m = FinDataConstants.p.matcher("    <li><span class=\"title\">·<a href=\"/finalpage/2010-04-28/57883640.PDF\" target=new>吉林敖东：2010年第一季度报告全文</a>&nbsp;&nbsp;<img align=absMiddle border=0 height=16 src='/imagesnew/Pdf.gif' width=16> (48K) </span> <span class=\"time\">2010-04-28 06:30</span></li>");
//		Pattern p = Pattern.compile("http://static.sse.com.cn/sseportal/cs/zhs/scfw/gg/ssgs/(\\d\\d\\d\\d-\\d\\d-\\d\\d)/([\\d]{6})_([\\d]{4})_([n,z,1,3]).pdf");
//		Matcher m = p.matcher("<td class=\"table3\" bgcolor=\"white\" height=\"20\"><a href=\"http://static.sse.com.cn/sseportal/cs/zhs/scfw/gg/ssgs/2013-01-19/600803_2012_n.pdf\" target=\"_blank\">威远生化年报</a></td>\n");
//		Pattern p = Pattern.compile("第<b>1</b>页/共<b>(\\d+)</b>页");
//		Matcher m = p1.matcher("第<b>1</b>页/共<b>505</b>页");
//		Pattern p = Pattern.compile("\\['(\\d\\d\\d\\d)\\.\\d\\d.\\d\\d','\\d+.{1,5}度'\\]\\];$");
//		Matcher m = p.matcher("dateurl=\"http://stockdata.stock.hexun.com/2008/xjll.aspx?stockid=600000&accountdate=\"; dateArr = [['2012.09.30','12年前3季'],['2012.06.30','12年中期'],['2012.03.15','12年第1季'],['2011.12.31','11年年度'],['2011.09.30','11年前3季'],['2011.06.30','11年中期'],['2011.03.15','11年第1季'],['2010.12.31','10年年度'],['2010.09.30','10年前3季'],['2010.06.30','10年中期'],['2010.03.15','10年第1季'],['2009.12.31','09年年度'],['2009.09.30','09年前3季'],['2009.06.30','09年中期'],['2009.03.15','09年第1季'],['2008.12.31','08年年度'],['2008.09.30','08年前3季'],['2008.06.30','08年中期'],['2008.03.15','08年第1季'],['2007.12.31','07年年度'],['2007.09.30','07年前3季'],['2007.06.30','07年中期'],['2007.03.15','07年第1季'],['2006.12.31','06年年度'],['2006.09.30','06年前3季'],['2006.06.30','06年中期'],['2006.03.15','06年第1季'],['2005.12.31','05年年度'],['2005.09.30','05年前3季'],['2005.06.30','05年中期'],['2005.03.15','05年第1季'],['2004.12.31','04年年度'],['2004.09.30','04年前3季'],['2004.06.30','04年中期'],['2004.03.15','04年第1季'],['2003.12.31','03年年度'],['2003.09.30','03年前3季'],['2003.06.30','03年中期'],['2003.03.15','03年第1季'],['2002.12.31','02年年度'],['2002.06.30','02年中期'],['2001.12.31','01年年度'],['2001.06.30','01年中期'],['2000.12.31','00年年度'],['2000.06.30','00年中期'],['1999.12.31','99年年度'],['1998.12.31','98年年度']];");
//		System.out.println(m.find());
//		System.out.println(m.group(1));
//		System.out.println(m.group(2));
//		System.out.println(m.group(3));
//		System.out.println(m.group(4));
//		Pattern p1 = Pattern.compile("<td class=\"td_text\"><a href=\"/f10/ggmx_(\\d{6})_[\\d_]+.html\" target=\"_blank\" title='.+([\\d?一二三四五六七八九０１２３４５６７８９]{4})年.+'>");
		Pattern p2 = Pattern.compile("、\\(([0|1|2|3][\\d]{5})、.*([0|1|2|3][\\d]{5}).*\\).+：([\\d]{4})(.+度).*主要.+");
//		Pattern p3 = Pattern.compile("<td class=\"td_text\">(一季度报告|中期报告|三季度报告|年度报告)</td>");
		Matcher m = p2.matcher("二十四、(112132、300147) 香雪制药：2012年年度报告主要财务指标及分配预案");
		System.out.println(m.find());
//		System.out.println(m.group(1));
//		System.out.println(m.group(2));
//		m = p2.matcher("                    <td class=\"align_c\">2003-04-11</td>");
//		System.out.println(m.find());
//		System.out.println(m.group(1));
//		m = p3.matcher("                    <td class=\"td_text\">年度报告</td>");
//		System.out.println(m.find());
//		System.out.println(m.group(1));
//		Integer.parseInt("ss");
//	refreshFinData(null);

//		URL url = new URL("http://query.sse.com.cn/infodisplay/queryLatestBulletin.do?jsonCallBack=&isPagination=false&productId=&reportType2=DQGG&reportType=ALL&beginDate=2012-08-31&endDate=2012-08-31&pageHelp.pageSize=2000&pageHelp.beginPage=1&pageHelp.endPage=1&_=1359207649232");
//		URLConnection con = url.openConnection();
//		con.setRequestProperty("Referer", "http://www.sse.com.cn/disclosure/listedinfo/announcement/search_result_index.shtml?x=1&productId=&startDate=2012-02-09&endDate=2012-02-09&reportType2=%E5%AE%9A%E6%9C%9F%E5%85%AC%E5%91%8A&reportType=ALL&moreConditions=true");
//		BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
//		String line;
//		while ((line = br.readLine()) != null) {
//			System.out.println(line);
//		}
//		JSONObject obj = (JSONObject) JSONValue.parse(new InputStreamReader(con.getInputStream(), "UTF-8"));
//		JSONArray array = (JSONArray)obj.get("result");
//		for (Object o : array) {
//			obj = (JSONObject) o;
//			System.out.println(obj.get("URL"));
//		}
//		JSONObject obj = (JSONObject) JSONObject.fromObject(new InputStreamReader(new URL("http://query.sse.com.cn/infodisplay/queryLatestBulletin.do?jsonCallBack=&isPagination=false&productId=&reportType2=DQGG&reportType=ALL&beginDate=2012-07-25&endDate=2012-07-25&_=1359207649232").openStream()));
//		JSONObject obj = JSONObject.fromObject("{\"actionErrors\":[],\"actionMessages\":[],\"beginDate\":\"2013-01-19\",\"endDate\":\"2013-08-19\",\"errorMessages\":[],\"errors\":{},\"fieldErrors\":{},\"isPagination\":\"false\",\"issueHomeFlag\":null,\"jsonCallBack\":\"jsonpCallback38623\",\"locale\":\"zh_CN\",\"pageHelp\":{\"beginPage\":1,\"cacheSize\":5,\"data\":null,\"endDate\":\"2013-01-19\",\"endPage\":null,\"objectResult\":null,\"pageCount\":1,\"pageNo\":1,\"pageSize\":20,\"searchDate\":null,\"sort\":null,\"startDate\":\"2013-01-19\",\"total\":2},\"productId\":\"\",\"reportType\":\"ALL\",\"reportType2\":\"DQGG\",\"result\":[{\"INDEXCLASS\":\"M_Bulletin_L_Comp\",\"PLAN_Date\":null,\"PLAN_Year\":null,\"SSEDate\":\"2013-01-19\",\"SSETime\":\"\",\"SSETimeStr\":null,\"URL\":\"http:\\/\\/static.sse.com.cn\\/disclosure\\/listedinfo\\/announcement\\/c\\/2013-01-19\\/600803_2012_nzy.pdf\",\"author\":null,\"book_Name\":null,\"bulletinHeading\":\"定期报告\",\"bulletinType\":\"年报摘要\",\"bulletin_No\":null,\"bulletin_Type\":\"年报摘要\",\"bulletin_Year\":\"2012\",\"category_A\":null,\"category_B\":null,\"category_C\":null,\"category_D\":null,\"chapter_No\":null,\"companyAbbr\":null,\"dispatch_Organ\":null,\"file_Serial\":null,\"finish_Time\":null,\"initial_Date\":null,\"isChangeFlag\":null,\"journal_Issue\":null,\"journal_Name\":null,\"journal_Section\":null,\"journal_Year\":null,\"keyWord\":\"\",\"key_Word\":\"\",\"language\":null,\"lemma_CN\":null,\"lemma_EN\":null,\"publishing_Comp\":null,\"question\":null,\"question_Class\":null,\"read_Status\":null,\"save_Time\":null,\"section\":null,\"security_Code\":\"600803\",\"source\":null,\"spareVolEnd\":null,\"title\":\"威远生化年报摘要\",\"title_ETC\":null,\"title_PY\":null,\"unit_Code\":null,\"unit_Type\":null},{\"INDEXCLASS\":\"M_Bulletin_L_Comp\",\"PLAN_Date\":null,\"PLAN_Year\":null,\"SSEDate\":\"2013-01-19\",\"SSETime\":\"\",\"SSETimeStr\":null,\"URL\":\"http:\\/\\/static.sse.com.cn\\/disclosure\\/listedinfo\\/announcement\\/c\\/2013-01-19\\/600803_2012_n.pdf\",\"author\":null,\"book_Name\":null,\"bulletinHeading\":\"定期报告\",\"bulletinType\":\"年报\",\"bulletin_No\":null,\"bulletin_Type\":\"年报\",\"bulletin_Year\":\"2012\",\"category_A\":null,\"category_B\":null,\"category_C\":null,\"category_D\":null,\"chapter_No\":null,\"companyAbbr\":null,\"dispatch_Organ\":null,\"file_Serial\":null,\"finish_Time\":null,\"initial_Date\":null,\"isChangeFlag\":null,\"journal_Issue\":null,\"journal_Name\":null,\"journal_Section\":null,\"journal_Year\":null,\"keyWord\":\"\",\"key_Word\":\"\",\"language\":null,\"lemma_CN\":null,\"lemma_EN\":null,\"publishing_Comp\":null,\"question\":null,\"question_Class\":null,\"read_Status\":null,\"save_Time\":null,\"section\":null,\"security_Code\":\"600803\",\"source\":null,\"spareVolEnd\":null,\"title\":\"威远生化年报\",\"title_ETC\":null,\"title_PY\":null,\"unit_Code\":null,\"unit_Type\":null}],\"texts\":null,\"type\":\"\"}");
//		System.out.println();
	}

	public static void refreshFinData(HashSet<String> stockCodesToUpdateFindata) throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException, IOException {
		Connection con = jdbcConnection();
		String code;
		java.util.Date cDate = FinDataConstants.currentTimeStamp;
		int id, currentYear = 2004, currentMonth = 1;
		PreparedStatement pInsertPLNF, pInsertBSNF, pInsertCFNF, pInsertPLF, pInsertBSF, pInsertCFF, pInsertProvision, pInsert, pUpdateStock = con.prepareStatement("UPDATE stock SET latest_year=?, latest_season=? WHERE id=?");
		con.setAutoCommit(false);
		Statement sStock = con.createStatement();
		Statement sCreateFinData = con.createStatement();
		sStock.execute("UPDATE stock SET latest_year = "+ (currentYear - 3) +", latest_season = 0 WHERE latest_year IS NULL");
		sStock.execute("UPDATE stock SET latest_season = 0 WHERE latest_season IS NULL");
		con.commit();
		TreeMap<String, Integer> stocksToUpdateReportDates = new TreeMap<>();

		// load fin_field into hash map
//		HashMap<String, Integer> finFields = new HashMap<>();
		ResultSet rs;
//		rs = sStock.executeQuery("SELECT id, fin_sheet, name FROM fin_field");
//		while (rs.next()) {
//			finFields.put(rs.getString(2) + "__" + rs.getString(3), rs.getInt(1));
//		}

		FinancialSheet sheet;
		int aYear, order, latestYear, latestSeason, cYear = -1;
		System.out.println("Current report season: "+currentYear+" "+currentMonth/3);
//		rs = sStock.executeQuery("SELECT id, code, latest_year, latest_season, is_financial FROM stock WHERE (latest_year*12+latest_season*3) < "+(currentYear*12+currentMonth/3*3)+" AND NOT is_ignored ORDER BY code ASC");
		if (stockCodesToUpdateFindata == null || stockCodesToUpdateFindata.isEmpty()) {
			rs = sStock.executeQuery("SELECT id, code, latest_year, latest_season, is_financial FROM stock WHERE id IN (select distinct stock_id from stock_price_1991 union\n" +
					"select distinct stock_id from stock_price_1992 union\n" +
					"select distinct stock_id from stock_price_1993 union\n" +
					"select distinct stock_id from stock_price_1994 union\n" +
					"select distinct stock_id from stock_price_1995 union\n" +
					"select distinct stock_id from stock_price_1996 union\n" +
					"select distinct stock_id from stock_price_1997 union\n" +
					"select distinct stock_id from stock_price_1998 union\n" +
					"select distinct stock_id from stock_price_1999 union\n" +
					"select distinct stock_id from stock_price_2000 union\n" +
					"select distinct stock_id from stock_price_2001 union\n" +
					"select distinct stock_id from stock_price_2002 union\n" +
					"select distinct stock_id from stock_price_2003 ) and code >= 600601 order by code");
		} else {
			String temp = "";
			for (String s : stockCodesToUpdateFindata) {
				temp += "'" + s + "', ";
			}
			rs = sStock.executeQuery("SELECT id, code, latest_year, latest_season, is_financial FROM stock WHERE code IN ("+temp+"' ')");
		}
		short cSeason = -1;
		boolean someSheetsAreEmpty;
		boolean isFinancial;
		Iterator<String> it;
//		String tableName;
		pInsertPLNF = con.prepareStatement("INSERT INTO profit_and_loss_nf (" +
				"pl01, pl02, pl03, pl04, pl05, pl06, pl07, pl08, pl09, pl10, " +
				"pl11, pl12, pl13, pl14, pl15, pl16, pl17, pl18, pl19, pl20, " +
				"pl21, pl22, pl23, pl24, pl25, pl26, pl27, " +
				"stock_id, fin_year, fin_season) VALUES (" +
				"?, ?, ?, ?, ?, ?, ?, ?, ?," +
				"?, ?, ?, ?, ?, ?, ?, ?, ?," +
				"?, ?, ?, ?, ?, ?, ?, ?, ?," +
				"?, ?, ?)");
		pInsertBSNF = con.prepareStatement("INSERT INTO balance_sheet_nf (" +
				"bs01, bs02, bs03, bs04, bs05, bs06, bs07, bs08, bs09, bs10, " +
				"bs11, bs12, bs13, bs14, bs15, bs16, bs17, bs18, bs19, bs20, " +
				"bs21, bs22, bs23, bs24, bs25, bs26, bs27, bs28, bs29, bs30, " +
				"bs31, bs32, bs33, bs34, bs35, bs36, bs37, bs38, bs39, bs40, " +
				"bs41, bs42, bs43, bs44, bs45, bs46, bs47, bs48, bs49, bs50, " +
				"bs51, bs52, bs53, bs54, bs55, bs56, bs57, bs58, bs59, bs60, " +
				"bs61, bs62, bs63, bs64, bs65, bs66, bs67, " +
				"stock_id, fin_year, fin_season) VALUES (" +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?)");
		pInsertCFNF = con.prepareStatement("INSERT INTO cash_flow_nf (" +
				"cf01, cf02, cf03, cf04, cf05, cf06, cf07, cf08, cf09, cf10, " +
				"cf11, cf12, cf13, cf14, cf15, cf16, cf17, cf18, cf19, cf20, " +
				"cf21, cf22, cf23, cf24, cf25, cf26, cf27, cf28, cf29, cf30, " +
				"cf31, cf32, cf33, cf34, cf35, cf36, cf37, cf38, cf39, cf40, " +
				"cf41, cf42, cf43, cf44, cf45, cf46, cf47, cf48, cf49, cf50, " +
				"cf51, cf52, cf53, cf54, cf55, cf56, cf57, cf58, cf59, " +
				"stock_id, fin_year, fin_season) VALUES (" +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?)");
		pInsertPLF = con.prepareStatement("INSERT INTO profit_and_loss_f (" +
				"pl01, pl02, pl03, pl04, pl05, pl06, pl07, pl08, pl09, pl10, " +
				"pl11, pl12, pl13, pl14, pl15, pl16, pl17, pl18, pl19, pl20, " +
				"pl21, pl22, pl23, pl24, pl25, pl26, pl27, pl28, pl29, pl30, " +
				"pl31, pl32, pl33, pl34, pl35, pl36, pl37, pl38, pl39, pl40, " +
				"pl41, pl42, pl43, pl44, pl45, pl46, pl47, " +
				"stock_id, fin_year, fin_season) VALUES (" +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?)");
		pInsertBSF = con.prepareStatement("INSERT INTO balance_sheet_f (" +
				"bs01, bs02, bs03, bs04, bs05, bs06, bs07, bs08, bs09, bs10, " +
				"bs11, bs12, bs13, bs14, bs15, bs16, bs17, bs18, bs19, bs20, " +
				"bs21, bs22, bs23, bs24, bs25, bs26, bs27, bs28, bs29, bs30, " +
				"bs31, bs32, bs33, bs34, bs35, bs36, bs37, bs38, bs39, bs40, " +
				"bs41, bs42, bs43, bs44, bs45, bs46, bs47, bs48, bs49, bs50, " +
				"bs51, bs52, bs53, bs54, bs55, bs56, bs57, bs58, bs59, bs60, " +
				"bs61, bs62, bs63, bs64, bs65, bs66, bs67, bs68, bs69, bs70, " +
				"bs71, bs72, bs73, bs74, bs75, bs76, bs77, bs78, bs79, bs80, " +
				"bs81, bs82, bs83, bs84, bs85, bs86, bs87, bs88, bs89, " +
				"stock_id, fin_year, fin_season) VALUES (" +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?)");
		pInsertCFF = con.prepareStatement("INSERT INTO cash_flow_f (" +
				"cf01, cf02, cf03, cf04, cf05, cf06, cf07, cf08, cf09, cf10, " +
				"cf11, cf12, cf13, cf14, cf15, cf16, cf17, cf18, cf19, cf20, " +
				"cf21, cf22, cf23, cf24, cf25, cf26, cf27, cf28, cf29, cf30, " +
				"cf31, cf32, cf33, cf34, cf35, cf36, cf37, cf38, cf39, cf40, " +
				"cf41, cf42, cf43, cf44, cf45, cf46, cf47, " +
				"stock_id, fin_year, fin_season) VALUES (" +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?)");
		pInsertProvision = con.prepareStatement("INSERT INTO provision (" +
				"pv01, pv02, pv03, pv04, pv05, pv06, pv07, pv08, pv09, pv10, " +
				"pv11, pv12, pv13, pv14, pv15, pv16, " +
				"stock_id, fin_year, fin_season) VALUES (" +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, " +
				"?, ?, ?)");
		EnumMap<FinDataConstants.SheetType, PreparedStatement> financialInsert = new EnumMap<>(FinDataConstants.SheetType.class);
		financialInsert.put(balance_sheet, pInsertBSF);
		financialInsert.put(profit_and_loss, pInsertPLF);
		financialInsert.put(cash_flow, pInsertCFF);
		financialInsert.put(provision, pInsertProvision);
		EnumMap<FinDataConstants.SheetType, PreparedStatement> nonFinancialInsert = new EnumMap<>(FinDataConstants.SheetType.class);
		nonFinancialInsert.put(balance_sheet, pInsertBSNF);
		nonFinancialInsert.put(profit_and_loss, pInsertPLNF);
		nonFinancialInsert.put(cash_flow, pInsertCFNF);
		nonFinancialInsert.put(provision, pInsertProvision);
		EnumMap<FinDataConstants.SheetType, PreparedStatement> insert;
		String name;
		Number v;
		while (rs.next()) {
			id = rs.getInt("id");
			code = rs.getString("code");
//			latestYear = rs.getInt("latest_year");
//			latestSeason = rs.getInt("latest_season");
			latestYear = 1989;
			latestSeason = 1;
			isFinancial = rs.getBoolean("is_financial");
			insert = isFinancial? financialInsert : nonFinancialInsert;
//			tableName = "fin_data_" + code;
			System.out.println(code);
			// Create table just in case

//			sCreateFinData.execute(
//					"CREATE TABLE IF NOT EXISTS " + tableName + " (\n" +
//							"\tid INT AUTO_INCREMENT,\n" +
//							"\tstock_id INT,\n" +
//							"\tsource_id INT,\n" +
//							"\tfin_field_id INT,\n" +
//							"\tfin_year INT,\n" +
//							"\tfin_season INT(1),\n" +
//							"\tvalue DOUBLE,\n" +
//							"\torder_ INT,\n" +
//							"\tPRIMARY KEY (id),\n" +
//							"\tFOREIGN KEY (stock_id) REFERENCES stock(id),\n" +
//							"\tFOREIGN KEY (source_id) REFERENCES source(id),\n" +
//							"\tFOREIGN KEY (fin_field_id) REFERENCES fin_field(id),\n" +
//							"\tUNIQUE (stock_id, fin_year, fin_season, source_id, fin_field_id)\n" +
//							")"
//			);

			// delete invalid rows from the table
//			System.out.println("Delete all data later than " + latestYear + "-" + latestSeason);
//			sCreateFinData.addBatch("DELETE FROM profit_and_loss_nf WHERE fin_year*10 + fin_season > " + (latestYear * 10 + latestSeason) + " AND stock_id = " + id);
//			sCreateFinData.addBatch("DELETE FROM balance_sheet_nf WHERE fin_year*10 + fin_season > " + (latestYear * 10 + latestSeason) + " AND stock_id = " + id);
//			sCreateFinData.addBatch("DELETE FROM cash_flow_nf WHERE fin_year*10 + fin_season > " + (latestYear * 10 + latestSeason) + " AND stock_id = " + id);
//			sCreateFinData.addBatch("DELETE FROM profit_and_loss_f WHERE fin_year*10 + fin_season > " + (latestYear * 10 + latestSeason) + " AND stock_id = " + id);
//			sCreateFinData.addBatch("DELETE FROM balance_sheet_f WHERE fin_year*10 + fin_season > " + (latestYear * 10 + latestSeason) + " AND stock_id = " + id);
//			sCreateFinData.addBatch("DELETE FROM cash_flow_f WHERE fin_year*10 + fin_season > " + (latestYear * 10 + latestSeason) + " AND stock_id = " + id);
//			sCreateFinData.addBatch("DELETE FROM provision WHERE fin_year*10 + fin_season > " + (latestYear * 10 + latestSeason) + " AND stock_id = " + id);
//			sCreateFinData.executeBatch();

			// update data
			cYear = -1;
			cSeason = -1;
			for (aYear = latestYear; aYear <= currentYear; aYear++) {
				for (short aSeason = (short)1; aSeason <= 4; aSeason++) {
//					if (aYear == latestYear && (aSeason <= latestSeason)) {
//						continue; // We already have this
//					}
//
//					if (aYear == currentYear && (currentMonth <= aSeason * 3)) {
//						continue; // Season hasn't finished yet, no need to try to get reports not existing
//					}

					someSheetsAreEmpty = false;
					for (FinDataConstants.SheetType sheetName : FinDataConstants.SheetType.values()) {
						System.out.println(code + "\t" + aYear + "\t" + aSeason + "\t" + sheetName);
						pInsert = insert.get(sheetName);
						sheet = new Hexun2008FinancialSheet(code, sheetName, aYear, aSeason);
						it = sheet.getDatumNames();
						order = 1;
						while (it.hasNext()) {
							name = it.next();
//							pInsertFinData.setInt(4, finFields.get(sheetName+"__"+name));
//							pInsertFinData.setInt(5, 1);
							v = sheet.getValue(name);
							if (v == null) {
								v = 0;
							}
							pInsert.setObject(order, v);
//							pInsertFinData.setInt(7, order);
							order++;
						}
						someSheetsAreEmpty = someSheetsAreEmpty || (order == 1 && (sheetName == balance_sheet || sheetName == cash_flow || sheetName == profit_and_loss));
						if (order < 5) continue;
						pInsert.setInt(order, id); // stock_id
						pInsert.setInt(order+1, aYear); // accounting year
						pInsert.setShort(order+2, aSeason); // accounting year
						try {
							pInsert.executeUpdate();
						} catch (SQLException ex) {
							System.out.println(aYear+"-"+aSeason+"-"+sheetName+"-"+order);
							System.out.println(ex.getMessage());
						}
					}
					if (!someSheetsAreEmpty) {
						cYear = aYear;
						cSeason = aSeason;
					}
				}
			}

			// set latest_year and latest_season
			if (cYear != -1 && cSeason != -1) {
				pUpdateStock.setInt(1, cYear);
				pUpdateStock.setShort(2, cSeason);
				pUpdateStock.setInt(3, id);
				pUpdateStock.executeUpdate();
				stocksToUpdateReportDates.put(code, id);
			}

			con.commit();
		}
//		if (!stocksToUpdateReportDates.isEmpty()) {
//			System.out.println("Refreshing report publication dates.");
//			currentYear = new Date().getYear()+1900;
//			for (Map.Entry<String, Integer> entry: stocksToUpdateReportDates.entrySet()) {
//				refreshReportPubDatesForStock(con, entry.getKey(), entry.getValue(), currentYear);
//			}
//		}
	}

	private static Connection jdbcConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection con = DriverManager.getConnection(ResourceUtil.getString(JDBC_URL), ResourceUtil.getString(JDBC_USER), ResourceUtil.getString(JDBC_PASS));
		return con;
	}
}