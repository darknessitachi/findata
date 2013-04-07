package michael.findata;

import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import michael.findata.external.hexun2008.Hexun2008Constants;
import michael.findata.util.FinDataConstants;
import michael.findata.util.ResourceUtil;

import java.io.IOException;
import java.sql.*;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Statement;
import java.text.ParseException;
import java.util.*;
import java.util.Date;

public class FinDataAnalyzer {

	public static void main(String[] args) throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException, IOException, ParseException {
		analyze(true); // Financial
//		analyze(false); // Non-financial
//		modify();
//		growthAnalysis();
//		analyzeGiven(false);
//		analyzeThroughTime("600519", "2000-01-01");
//		analyzeAsOfTime(false, "2010-05-01");
//		migrate();
	}

	private static void analyzeAsOfTime(boolean isFinancial, String asOfDate) throws ParseException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
		analyzeAsOfTime(isFinancial, FinDataConstants.yyyyDashMMDashdd.parse(asOfDate));
	}

	private static void analyzeAsOfTime(boolean isFinancial, Date asOfDate) throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException {
		Connection con = jdbcConnection();
		Statement st = con.createStatement();
		String stockCode;
		ResultSet rsCodes = st.executeQuery("SELECT code FROM stock WHERE code >= 399999 AND code < 900000 AND NOT (is_financial OR is_ignored) ORDER BY code");
		while (rsCodes.next()) {
			stockCode = rsCodes.getString("code");
			analyzeStock(isFinancial, con, stockCode, asOfDate);
		}
	}

	private static void analyzeThroughTime(String stockCode, String asOfDate) throws ParseException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
		analyzeThroughTime(stockCode, FinDataConstants.yyyyDashMMDashdd.parse(asOfDate));
	}

	public static void analyze(boolean financial) throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException, IOException {
		Connection con = jdbcConnection();
		Statement stCodes = con.createStatement();
		ResultSet rsCodes;
		if (financial) {
			rsCodes = stCodes.executeQuery("SELECT code FROM stock WHERE latest_year >= 2011 AND is_financial AND (NOT name LIKE '%ST%') ORDER BY code");
		} else {
			rsCodes = stCodes.executeQuery("SELECT code FROM stock WHERE true AND latest_year >= 2011 AND (NOT name LIKE '%ST%') AND NOT is_financial ORDER BY code");
		}
		String param_stock_code;
		Date today = new Date();
//		GregorianCalendar gc = new GregorianCalendar();
//		gc.add(Calendar.DATE, -1);
//		today = gc.getTime();
		while (rsCodes.next()) {
			param_stock_code = rsCodes.getString(1);
			analyzeStock(financial, con, param_stock_code, today);
		}
		con.close();
	}

	private static void analyzeStock(boolean financial, Connection con, String param_stock_code, Date asOfDate) throws SQLException {
		String dateString;
		dateString = FinDataConstants.yyyyDashMMDashdd.format(asOfDate);
		analyzeStock(financial, con, param_stock_code, dateString);
	}

	private static void analyzeStock(boolean financial, Connection con, String param_stock_code, String dateString) throws SQLException {
		CallableStatement cs;
		ResultSet rsResult;
		if (financial) {
			cs = con.prepareCall("CALL analyze_f ('" + param_stock_code + "', '"+dateString+"', 7)");
		} else {
			cs = con.prepareCall("CALL analyze_nf ('" + param_stock_code + "', '"+dateString+"', 7)");
		}
		try {
			rsResult = cs.executeQuery();
		} catch (SQLException ex) {
//			System.out.println("Error: "+param_stock_code);
			return;
		}
		if (rsResult.next()) {
		if (financial) {
			if (rsResult.getString(2) == null) return;
			System.out.print(param_stock_code + "\t" +
			rsResult.getString(2) + "\t" +
			Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(3)) + "\t" +
			Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(4)) + "\t" +
			Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(5)) + "\t" +
			Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(6)) + "\t" +
			Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(7)) + "\t" +
			Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(8)) + "\t" +
			Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(9)) + "\t" +
			Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(10)) + "\t" +
			Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(11)) + "\t" +
			Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(12)) + "\t" +
			Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(13)) + "\t" +
			Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(14)) + "\t" +
			Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(15)) + "\t" +
			Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(16)) + "\t" +
			Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(17)) + "\t" +
			dateString + "\n");
		} else {
			if (rsResult.getString(2) == null) return;
			System.out.print(param_stock_code + "\t" +
					rsResult.getString(2) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(3)) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(4)) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(5)) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(6)) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(7)) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(8)) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(9)) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(10)) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(11)) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(12)) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(13)) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(14)) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(15)) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(16)) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(17)) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(18)) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(19)) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(20)) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(21)) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(22)) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(23)) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(24)) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(25)) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(26)) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(27)) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(28)) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(29)) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(30)) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(31)) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(32)) + "\t" +
					rsResult.getString(33) + "\t" +
					rsResult.getString(34) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(35)) + "\t" +
					Hexun2008Constants.accurateDecimalFormat.format(rsResult.getDouble(36)) + "\t" +
					dateString + "\n");
		}
	}
	}

	public static void analyzeGiven() throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException, IOException {
		String [][] Subjects = {
				{"000400","2003-08-20","2003","2"},
				{"000400","2004-08-04","2004","2"},
				{"000400","2005-04-20","2005","1"},
				{"000400","2006-04-28","2006","1"},
				{"000400","2007-04-20","2007","1"},
				{"000400","2008-04-26","2008","1"},
				{"000400","2009-04-29","2009","1"},
				{"000400","2010-04-30","2010","1"},
				{"000400","2011-04-28","2011","1"},
				{"002028","2007-02-08","2006","4"},
				{"002028","2012-04-18","2011","4"},
				{"200771","2004-04-22","2004","1"},
				{"200771","2004-10-28","2004","3"},
				{"200771","2009-04-27","2008","4"},
				{"200771","2009-08-21","2009","2"},
				{"600160","2009-08-22","2009","2"},
				{"600160","2010-03-27","2009","4"},
				{"600160","2010-10-27","2010","3"},
				{"600160","2011-08-27","2011","2"},
				{"600636","2010-04-23","2009","4"},
				{"600636","2011-04-15","2010","4"},
				{"600636","2011-04-29","2011","1"},
				{"600428","2005-08-23","2005","2"},
				{"600428","2006-10-30","2006","3"},
				{"600428","2008-03-21","2007","4"},
				{"600428","2012-04-27","2012","1"},
				{"600026","2003-08-26","2003","2"},
				{"600026","2004-04-23","2004","1"},
				{"600026","2005-04-20","2005","1"},
				{"600026","2007-04-27","2007","1"},
				{"600026","2008-04-23","2008","1"},
				{"600026","2010-04-22","2010","1"}
		};
		Connection con = jdbcConnection();
		for (String [] Subject : Subjects) {
			analyzeStock(false, con, Subject[0], Subject[1]);
		}
		con.close();
	}

	public static void analyzeThroughTime(String stockCode, Date start) throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException {
		Calendar c = new GregorianCalendar();
		c.setTime(start);
		Date now = new Date();
		Connection conn = jdbcConnection();
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT is_financial FROM stock WHERE code = "+stockCode);
		boolean isFinancial = false;
		if (rs.next()) {
			isFinancial = rs.getBoolean(1);
		}
		while (start.before(now)) {
			analyzeStock(isFinancial, conn, stockCode, start);
			c.add(Calendar.DATE, 5);
			start = c.getTime();
		}
		analyzeStock(isFinancial, conn, stockCode, now);
		conn.close();
	}

	public static void growthAnalysis() throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException {
		Connection con = jdbcConnection();
		Statement sStock = con.createStatement();
		Statement incomeStmt = con.createStatement();
		PreparedStatement ps = con.prepareStatement("SELECT fin_date FROM report_pub_dates WHERE stock_id = ? AND fin_year = ? AND fin_season = ?");
		ResultSet rs = sStock.executeQuery("SELECT id, code, name, is_financial FROM stock WHERE NOT (is_ignored OR is_financial) ORDER BY code ASC");
		ResultSet incomeResultSet, rs1;
		int stockId, season = -1;
		String stockCode, sql, name;
//		boolean isFinancial;
		ValueTuple vt;
		LinkedList<ValueTuple> temp = new LinkedList<>();
		Stack<ValueTuple> results = new Stack<>();
		int size = 0;
		ValueTuple tempVt;
		Double tempC;
		while (rs.next()) {
			temp.clear();
			results.clear();
			size=0;
			stockId = rs.getInt("id");
			name = rs.getString("name");
			stockCode = rs.getString("code");
//			isFinancial = rs.getBoolean("is_financial");
//			System.out.println(stockCode+"-----------:");

//			if (isFinancial) {
//				sql = "select fin_year, fin_season, value415 from fin_data_"+stockCode+" fd where fd.fin_field_id = 415 order by fin_year desc, fin_season desc";
//			} else {
//				sql = "select fin_year, fin_season, value542 from fin_data_"+stockCode+" fd where fd.fin_field_id = 542 order by fin_year desc, fin_season desc";
//			}
			// 414 operating income;  545 operating cost ; 542 operating profit;
			sql = "select fd542.fin_year, fd542.fin_season, fd542.value as value542, fd414.value as value414, fd545.value as value545" +
					" from fin_data_"+stockCode+" fd542, fin_data_"+stockCode+" fd414, fin_data_"+stockCode+" fd545 where" +
					" fd542.fin_field_id = 542 and fd414.fin_field_id = 414 and fd545.fin_field_id = 545 and" +
					" fd542.fin_year = fd414.fin_year and fd545.fin_year = fd414.fin_year and" +
					" fd542.fin_season = fd414.fin_season and fd545.fin_season = fd414.fin_season order by" +
					" fd542.fin_year desc, fd542.fin_season desc";

			incomeResultSet = incomeStmt.executeQuery(sql);
			season = -1;
			while (incomeResultSet.next()) {
				if (season == -1) {
					season = incomeResultSet.getInt("fin_season");
				} else {
					season += 3;
					season %= 4;
					if (season != incomeResultSet.getInt("fin_season") % 4) {
						break;
					}
				}
				vt = new ValueTuple (incomeResultSet.getInt("fin_year"), incomeResultSet.getInt("fin_season"));
				vt.putValue("542", incomeResultSet.getDouble("value542"));
				vt.putValue("545", incomeResultSet.getDouble("value545"));
				vt.putValue("414", incomeResultSet.getDouble("value414"));
//				vt.putValue("ytd_pm", incomeResultSet.getDouble("ytd_pm"));

//				if (isFinancial) {
//					vt.putValue("415", incomeResultSet.getDouble("value415"));
//				} else {
//					vt.putValue("542", incomeResultSet.getDouble("value542"));
//				}

				try {
					results.peek().lastSeason = vt;
					if (vt.finSeason != 4) {
						for (String vKey : vt.value.keySet()) {
							results.peek().seasonValue.put(vKey, results.peek().value.get(vKey) - vt.value.get(vKey));
						}
						results.peek().seasonValue.put("pm", 1 - results.peek().seasonValue.get("545") / results.peek().seasonValue.get("414"));
					}
					// Profit Margin
					results.peek().putValue("pm", 1 - results.peek().value.get("545")/results.peek().value.get("414"));

				} catch (EmptyStackException ex) {
				}
				results.push(vt);
				temp.offer(vt);
				size++;
				if (size > 4) {
					vt = temp.poll();
					vt.y2ySeason = results.peek();
					size --;
//					System.out.println(stockCode+" "+vt.finYear+" "+vt.finSeason+"\n\tlast:\t"+vt.lastSeason.finYear+" "+vt.lastSeason.finSeason+"\n\ty2y:\t"+vt.y2ySeason.finYear+" "+vt.y2ySeason.finSeason);
				}
			}

			while (!results.empty()) {
				vt = results.pop();
				try {
//					vt.y2ySingleSeasonDelta.put("pm", vt.seasonValue.get("pm") - vt.y2ySeason.seasonValue.get("pm"));
					tempVt = vt;
					tempC = 0d;
					int i = 0;
					do {
						tempC += tempVt.lastSeason.seasonValue.get("pm");
						tempVt = tempVt.lastSeason;
						i ++;
					} while (i < 12);
					tempC /= 12;
					vt.y2ySingleSeasonDelta.put("ma12Delta", vt.seasonValue.get("pm") - tempC);
					vt.y2ySingleSeasonDelta.put("ma12", tempC);
//					vt.y2ySingleSeasonGrowth = vt.seasonValue / vt.y2ySeason.seasonValue -1;
				} catch (NullPointerException ex) {
				}
				try {
//					vt.y2yCumulativeDelta.put("pm", vt.value.get("pm") - vt.y2ySeason.value.get("pm"));
//					vt.y2yCumulativeGrowth = vt.value / vt.y2ySeason.value -1;
				}catch (NullPointerException ex) {
				}
				try {
//					vt.season2seasonDelta.put("pm", vt.seasonValue.get("pm") - vt.lastSeason.seasonValue.get("pm"));
//					vt.season2seasonGrowth = vt.seasonValue / vt.lastSeason.seasonValue - 1;
				}catch (NullPointerException ex) {
				}
//				System.out.println(stockCode+" "+vt.finYear+" "+vt.finSeason+" v: "+vt.value+" sv: "+vt.seasonValue);
//				System.out.println("\ts2sg: "+vt.season2seasonGrowth);
//				System.out.println("\ty2ycg: "+vt.y2yCumulativeGrowth);
//				System.out.println("\ty2ysg: "+vt.y2ySingleSeasonGrowth);
//				System.out.println(vt.y2ySingleSeasonDelta.get("ma12Delta")+" "+vt.finYear+" "+vt.finSeason);
				try {
					if ( vt.finYear == 2010 && vt.finSeason == 1 && vt.y2ySingleSeasonDelta.get("ma12Delta") > 0.06 && vt.y2ySingleSeasonDelta.get("ma12") > 0.04 && vt.y2ySingleSeasonDelta.get("ma12") < 0.19) {
						ps.setInt(1, stockId);
						ps.setInt(2, vt.finYear);
						ps.setInt(3, vt.finSeason);
						rs1 = ps.executeQuery();
						if (rs1.next()) {
							analyzeStock(false, con, stockCode, rs1.getDate("fin_date"));
						}
//						System.out.print("\t"+stockCode+"\t"+vt.finYear+"\t"+vt.finSeason);
//						System.out.print("\t" + vt.seasonValue.get("pm"));
//						System.out.println("\t"+vt.season2seasonDelta.get("pm"));
//						System.out.println(vt.y2ySingleSeasonDelta.get("pm"));
//						System.out.println(vt.y2yCumulativeDelta.get("pm"));
					}
//					if (vt.finYear == 2012 && vt.finSeason == 1 &&
//						vt.lastSeason.lastSeason.value > 0 && vt.lastSeason.lastSeason.seasonValue > 0 &&
//						vt.lastSeason.value > 0 && vt.lastSeason.seasonValue > 0 && vt.lastSeason.season2seasonGrowth > 0 && vt.lastSeason.y2yCumulativeGrowth > 0 && vt.lastSeason.y2ySingleSeasonGrowth > 0 &&
//						vt.value > 0 && vt.seasonValue > 0 && vt.season2seasonGrowth > 0 && vt.y2yCumulativeGrowth > 0 && vt.y2ySingleSeasonGrowth > 0) {
//						System.out.println("-----------------------------------------------------------------------------------------------\n\n");
//						System.out.println("Found: "+name+" "+stockCode+"\n\n");
//						System.out.println("-----------------------------------------------------------------------------------------------");
//					}
				} catch (NullPointerException ex) {
				}
			}
		}
	}

	// todo if this method is ever gonna be used again, you need to get rid of the price_year thing
	public static void migrate () throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
		Connection con = jdbcConnection();
		con.setAutoCommit(false);
		Statement st = con.createStatement();
		PreparedStatement pst = null;
		CallableStatement cst = null;
		Statement st1 = con.createStatement();
		int stockId, pid;
		String stockCode;
		ResultSet rsCode = st1.executeQuery("SELECT code, id, is_financial FROM stock WHERE code >= 000562 AND is_financial ORDER BY code");
		ResultSet rsPrice, analysis;
		float usdX, hkdX, price, noShares, profit, ret, ret_max, ret_min;
		Date start = new Date(), temp;
		double time;
		boolean isFinancial;
		while (rsCode.next()) {
			ret_max = -1000;
			ret_min = 1000;
			stockId = rsCode.getInt("id");
			stockCode = rsCode.getString("code");
			isFinancial = rsCode.getBoolean("is_financial");
			System.out.println(stockCode+" ...");
			for (int year = 1994; year <= 2013; year ++)
			{
				rsPrice = st.executeQuery(
						"select s.code, p.date date, p.id pid\n" +
						"from stock_price_"+year+" p inner join stock s on p.stock_id = s.id\n" +
						"where s.code = " + stockCode + "\n" +
						"order by date;"); // todo p.pe_last_4_seasons is null and
				if (pst != null) {
					pst.close();
				}
				pst = con.prepareStatement("UPDATE stock_price_"+year+" SET pe_last_4_seasons = ?, pe_l4s_max = ?, pe_l4s_min = ? WHERE id = ?");
				while (rsPrice.next()) {
					pid = rsPrice.getInt("pid");
					try {
						if (cst != null) cst.close();
						cst = con.prepareCall((isFinancial? "CALL analyze_f ('" : "CALL analyze_nf ('") + stockCode + "', '"+FinDataConstants.yyyyDashMMDashdd.format(rsPrice.getDate("date"))+"', 1)");
						analysis = cst.executeQuery();
					} catch (MySQLSyntaxErrorException ex) {
						System.out.println("Can't calculate return for "+stockCode+" "+FinDataConstants.yyyyDashMMDashdd.format(rsPrice.getDate("date")));
						continue;
					}
					if (analysis.next()) {
						price = analysis.getFloat("cp");
						noShares = analysis.getFloat("number_of_sh");
						profit = analysis.getFloat("recent_4_season_prof");
						usdX = analysis.getFloat("usd_x");
						hkdX = analysis.getFloat("hkd_x");
						if (stockCode.startsWith("900")) {
							price *= usdX;
						} else if (stockCode.startsWith("200")) {
							price *= hkdX;
						}

						ret = profit/noShares/price;
						if (noShares == 0) {
							continue;
						}
						if (ret > ret_max) ret_max = ret;
						if (ret < ret_min && ret > 0) ret_min = ret;
						pst.setFloat(1, ret);
						pst.setFloat(2, ret_max);
						pst.setFloat(3, ret_min);
						pst.setInt(4, pid);
						pst.executeUpdate();
						System.out.println(stockCode+"\t"+FinDataConstants.yyyyDashMMDashdd.format(rsPrice.getDate("date"))+"\t"+ ret + "\t"+ ret_max + "\t"+ ret_min + "\t");
					}
				}
			}
			con.commit();
			temp = new Date();
			time = (temp.getTime() - start.getTime())/1000;
			start = temp;
			System.out.println(stockCode+" "+time+" seconds ... done.");
		}
		con.close();
	}

	private static Connection jdbcConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection con = DriverManager.getConnection(ResourceUtil.getString(FinDataConstants.JDBC_URL), ResourceUtil.getString(FinDataConstants.JDBC_USER), ResourceUtil.getString(FinDataConstants.JDBC_PASS));
		return con;
	}

	static String [][] data ={
//				{"有机氟材料", "'000426','002326','600277','600892','002407','600160','600636'"}, //富龙热电、永太科技、亿利能源、ST宝诚、三爱富、巨化股份、多氟多
//				{"有机硅材料", "'300019','002211','600260','002129','000925','600237','000055','600299','600596','300041'"}, //铜峰电子,方大集团,硅宝科技,ST新材,新安股份,回天胶业,宏达新材,凯乐科技,中环股份,众合机电
//				{"工程、改性塑料", "'002168','002324','600143','000619','000859','000973','600444、000665','600155','600210','002108'"}, //深圳惠程,普利特,金发科技,海螺型材,国风塑业,宝硕股份,紫江企业,佛塑股份,国通管业,武汉塑料,沧州明珠
//				{"高性能纤维-碳纤维", "'002254','000928','002297','600146','600516'"}, //烟台氨纶,中钢吉炭博,云新材,大元股份,方大炭素
//				{"高性能纤维-粘胶", "'002822','000949','000420','600899','000615','002172'"}, //山东海龙,新乡化纤,吉林化纤,南京化纤,澳洋科技,湖北金环
//				{"高性能纤维-氨纶", "'002254','002064','000584','000949','600699'"}, //烟台氨纶,华峰氨纶,友利控股,新乡化纤,ST得亨
//				{"高性能纤维-涤纶", "'海得利','江南高纤','尤夫股份'"},
//				{"高性能纤维-芳纶纤维", "'002254'"},//烟台氨纶
//				{"高性能纤维-竹浆纤维", "'000949','000420'"}, //新乡化纤,吉林化纤
//				{"高性能纤维-聚乙烯醇纤维", "'600725','000755','600063','600061','600871'"},//云维股份,山西三维,皖维高新,中纺投资,S仪化
//				{"聚氨酯材料", "'002061','600230','002165','600309','601678'"},//江山化工,沧州大化,红宝丽,烟台万华,滨化股份
//				{"电子化学材料", "'002326','000990'"},//永太科技,诚志股份
//				{"高性能膜材料", "'300070','002389','600096','000973','000920','002168'"},//碧水源,南洋科技,云天化,佛塑股份,南方汇通,深圳惠程
//				{"煤头尿素", "'000422','000630','600426','000953','002113','600228','600423','002274'"},//湖北宜化,鲁西化工,华鲁恒升,河池化工,天润发展,昌九生化,柳化股份,华昌化工
//				{"气头尿素", "'建峰化工','沧州大化','川化股份','四川美丰','辽通化工','泸天化','赤天化','云天化'"},
//				{"磷肥", "'六国化工','湖北宜化','兴发集团','澄星股份','ST马龙'"},
//				{"钾肥", "'000792','000839','600251'"},//盐湖钾肥,中信国,安冠农股份
//				{"氯碱", "'氯碱化工','锦化氯碱','沧州化工','巨化股份','金路集团','南化股份'"},
//				{"纯碱、烧碱", "'远兴能源','三友化工','山东海化','双环科技','金晶科技','青岛碱业','英力特','新疆天业','中泰化学','锦化氯碱'"},
//				{"电石法PVC", "'新疆天业','三友化工','云南盐化','中泰化学','巨化股份','太化股份','南化股份','金路集团','氯碱化工','祥龙电业','英利特','内蒙君正','英力特','锦化氯碱','湖北宜化'"},
//				{"乙烯法PVC", "'氯碱化工'"},
//				{"DAP", "'湖北宜化','六国化工'"},
//				{"DMF", "'华鲁恒升','江山化工'"},
//				{"PTA", ""},
			{"杀虫剂-草甘膦", "'新安股份','华星化工','江山股份'"},
			{"杀虫剂-农药", "'利尔化学','诺普信','联化科技','红太阳','渝三峡','沙隆达'"},
			{"煤化工-煤焦化", "'山西焦化','安泰集团','煤气化'"},
			{"煤化工-电石法", "'山西三维','云维股份','皖维股份'"},
			{"煤化工-煤合成气化", "'太工天成','兰花科创','远兴能源','天富热电','天茂集团','双环科技','华鲁恒生','鲁西化工','赤天化','四川美丰','黑化股份'"},
			{"煤化工-乙二醇", "'丹化科技'"},
			{"煤化工-甲醇", "'远兴能源','泸天化','华昌化工','黑化股份','鲁西化工','丰原生化'"},
			{"煤化工-二甲醚", "'泸天化','天茂集团','威远生化'"},
			{"无机盐", "'红星发展','兰太实业','南风化工','云南盐化'"},
			{"染料", "'浙江龙盛','德美化工','传化股份'"},
			{"涂料", "'渝三峡A','西北化工'"},
			{"民爆", "'久联发展','南岭民爆','雷鸣科化'"},
			{"硝酸铵", "'柳化股份','联合化工','兴化股份'"},
			{"日化", "'上海家化','索芙特','白猫股份','两面针','广州浪奇','蓝星清洗','青岛金王'"},
			{"电石法PVA", "'山西三维','云维股份'"},
			{"醋酐", "'华鲁恒','丹化科技'"},
			{"热法磷酸/三聚磷酸钠", "'兴发集团','澄星股份'"},
			{"纯聚合MDI", "'烟台万华'"},
			{"TDI", "'沧州大化'"},
			{"电石法/顺酐法BDO", "'山西三维'"},
			{"己二酸", "'天利高新'"},
			{"苯乙烯", "'双良股份'"},
	};

	public static void modify () throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException {
		Connection conn = jdbcConnection();
		conn.setAutoCommit(true);
		Statement s1;
		s1 = conn.createStatement();
		String s;
		for (String [] i: data) {
			s = "UPDATE stock SET industry = '化工', subindustry = concat(subindustry, ' ', '"+i[0]+"') WHERE code IN ("+i[1]+") or name IN ("+i[1]+")";
			System.out.println(s);
			s1.executeUpdate(s);
		}
		conn.close();
	}
}

class ValueTuple {
	int finYear, finSeason;
	HashMap<String, Double> value = new HashMap<>();
	HashMap<String, Double> seasonValue = new HashMap<>();
	HashMap<String, Double> y2ySingleSeasonGrowth = new HashMap<>();
	HashMap<String, Double> y2yCumulativeGrowth = new HashMap<>();
	HashMap<String, Double> season2seasonGrowth = new HashMap<>();
	HashMap<String, Double> y2ySingleSeasonDelta = new HashMap<>();
	HashMap<String, Double> y2yCumulativeDelta = new HashMap<>();
	HashMap<String, Double> season2seasonDelta = new HashMap<>();

	ValueTuple y2ySeason;
	ValueTuple lastSeason;
	ValueTuple lastYear;

	ValueTuple (int finYear, int finSeason) {
		this.finYear = finYear;
		this.finSeason = finSeason;
	}

	public void putValue (String name, double value) {
		this.value.put(name, value);
		if (finSeason == 1) this.seasonValue.put(name, value);
	}
}