import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import michael.findata.external.*;
import michael.findata.external.hexun2008.Hexun2008DividendData;
import michael.findata.external.hexun2008.Hexun2008FinancialSheet;
import michael.findata.external.hexun2008.Hexun2008ShareNumberDatum;
import michael.findata.external.netease.NeteaseTradingDatum;
import michael.findata.external.tdx.TDXPriceHistory;
import michael.findata.util.FinDataConstants;
import michael.findata.util.ResourceUtil;
import org.cyberneko.html.parsers.DOMParser;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.DOMReader;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.Date;
import java.util.regex.Matcher;

import static michael.findata.util.FinDataConstants.*;
import static michael.findata.util.FinDataConstants.SheetType.*;

enum CodeListFileType {
	THS, // Tong Hua Shun
	TDX  // Tong Da Xin
}

public class FinDataExtractor {

	public static void main(String args[]) throws IOException, SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException, URISyntaxException, DocumentException, SAXException, ParseException {
//		refreshStockCodes();
//		refreshStockPriceHistories();
//		refreshLatestPriceNameAndNumberOfShares();
//		refreshFinData();
//		refreshDividendData();
//		calculateAdjustmentFactor(2012);
//		updateReportPubDates();
//		refreshStockPriceHistoryTEST(1,"600000", jdbcConnection());
	}

	/**
	 * Refresh stock code table from stock data software (Tong Da Xin or Tong Hua Shun)
	 * @throws IOException
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static void refreshStockCodes() throws IOException, SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(ResourceUtil.getString(STOCK_LIST_FILE))));
		Connection con = jdbcConnection();
		Statement st = con.createStatement();
		String line;
		int updateCount;
		CodeListFileType fileType;
		ArrayList<String> codeList = new ArrayList<String>();

		ResultSet rs;
		HashSet<String> codeSetOriginal = new HashSet<String>();
		rs = st.executeQuery("SELECT code FROM stock");
		while (rs.next()) {
			codeSetOriginal.add(rs.getString(1));
		}

		// Determining the type of stock list file
		br.mark(100);
		br.readLine();
		if (br.readLine().contains("[Market]")) {
			fileType = CodeListFileType.THS;
		} else {
			fileType = CodeListFileType.TDX;
		}
		br.reset();

		switch (fileType) {
			case THS:
				// Getting codes from THS
				StringTokenizer tk;
				String temp[];
				int start, end;
				boolean enterMarket = false;
				DecimalFormat dm = new DecimalFormat("000000");
				while ((line = br.readLine()) != null) {
					if (line.contains("[Market_16_17]") || line.contains("[Market_16_18]") || line.contains("[Market_32_33]") || line.contains("[Market_32_34]")) {
						enterMarket = true;
					} else if (line.length() < 3) {
						enterMarket = false;
					} else if (enterMarket && line.startsWith("CodeList")) {
						tk = new StringTokenizer(line, "=,", false);
						tk.nextToken();
						while (tk.hasMoreTokens()) {
							line = tk.nextToken();
							if (line.contains("-")) {
								temp = line.split("-");
								start = Integer.parseInt(temp[0]);
								end = Integer.parseInt(temp[1]);
								for (int i = start; i <= end; i++) {
									line = dm.format(i);
									if (!codeSetOriginal.contains(line)) {
										codeList.add(line);
										System.out.println("Added " + line + " to list");
									}
								}
							} else {
								if (!codeSetOriginal.contains(line)) {
									codeList.add(line);
									System.out.println("Added " + line + " to list");
								}
							}
						}
					}
				}
				break;
			case TDX:
				// Getting codes from TDX
				while ((line = br.readLine()) != null) {
					if (line.length() < 6 || line.startsWith("1") || line.startsWith("5")) continue;
					line = line.substring(0, 6);
					if (!codeSetOriginal.contains(line)) {
						codeList.add(line);
						System.out.println("Added " + line + " to list");
					}
				}
		}

		PreparedStatement pInsertStock = con.prepareStatement("INSERT INTO stock (code, is_ignored) VALUES (?, false)");
		con.setAutoCommit(false);

		Collections.sort(codeList);
		for (String code : codeList) {
			updateCount = 0;
			System.out.print(code + "...");
			pInsertStock.setString(1, code);
			try {
				updateCount = pInsertStock.executeUpdate();
			} catch (MySQLIntegrityConstraintViolationException e) {
				if (!e.getMessage().contains("Duplicate")) {
					throw e;
				} else {
					System.out.println(" already exists.");
				}
			}
			if (updateCount < 2) {
				if (updateCount == 1) {
					System.out.println(" inserted.");
				} else {
					// Some notification, telling user that record already exists
				}
			} else {
				System.out.println(" unexpected update count: " + updateCount + " has been produced.");
			}
			con.commit();
		}
		con.close();
	}

	public static void refreshLatestPriceNameAndNumberOfShares() throws SQLException, IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
		Connection con = jdbcConnection();
		con.setAutoCommit(false);
		ResultSet rs;
		int updateCount;
		PreparedStatement p = con.prepareStatement("SELECT code, id FROM stock WHERE (last_updated < ? or last_updated is NULL) AND NOT is_ignored ORDER BY code");
		PreparedStatement pUpdateStock = con.prepareStatement("UPDATE stock SET name=?, current_price=?, number_of_shares=?, last_updated=? WHERE code=?");
		PreparedStatement shareNumberChange = con.prepareStatement("SELECT count(*) FROM share_number_change WHERE stock_id = ?");
		PreparedStatement updateShareNumberChange = con.prepareStatement("INSERT INTO share_number_change (stock_id, change_date, number_of_shares) VALUES (?, ?, ?)");
		PreparedStatement deleteObsolete = con.prepareStatement("DELETE FROM share_number_change WHERE stock_id = ? and change_date = ?");
		Date d = new Date();
		java.sql.Date today = new java.sql.Date(d.getTime());
		d.setHours(15);
		d.setMinutes(0);
		d.setSeconds(0);
		p.setDate(1, new java.sql.Date(d.getTime()));
		rs = p.executeQuery();
		ResultSet shareNumberChangeRs;
		String code;
		int stock_id, times;
		Statement st = con.createStatement();
		while (rs.next()) {
			code = rs.getString(1);
			stock_id = rs.getInt(2);
			NeteaseTradingDatum td = new NeteaseTradingDatum(code);
			Hexun2008ShareNumberDatum snd = new Hexun2008ShareNumberDatum(code);
			if (td.getStockName() != null && td.getCurrent() != null && snd.getValue() != null) {
				pUpdateStock.setString(1, td.getStockName());
				pUpdateStock.setObject(2, td.getCurrent());
				pUpdateStock.setObject(3, snd.getValue());
				pUpdateStock.setDate(4, today);
				pUpdateStock.setString(5, code);
				System.out.println(td.getStockCode()+" "+td.getStockName());
				updateCount = pUpdateStock.executeUpdate();
				if (updateCount != 1) {
					con.rollback();
					System.out.println("\tunable to update name and price. " + updateCount + " rows updated.");
				} else {
					shareNumberChange.setInt(1, stock_id);
					shareNumberChangeRs = shareNumberChange.executeQuery();
					shareNumberChangeRs.next();
					times = shareNumberChangeRs.getInt(1);
					for (int i = snd.getShareNumberChanges().size() - 1 - times; i > -1; i--) {
						updateShareNumberChange.setInt(1, stock_id);
						updateShareNumberChange.setDate(2, new java.sql.Date(snd.getShareNumberChanges().get(i).getChangeDate().getTime()));
						updateShareNumberChange.setLong(3, snd.getShareNumberChanges().get(i).getNumberOfShares().longValue());
						try {
							updateShareNumberChange.executeUpdate();
						} catch (SQLException e) {
							if (e.getMessage().contains("Duplicate entry")) {
								deleteObsolete.setInt(1, stock_id);
								deleteObsolete.setDate(2, new java.sql.Date(snd.getShareNumberChanges().get(i).getChangeDate().getTime()));
								deleteObsolete.executeUpdate();
								i += 2;
							} else {
								throw e;
							}
						}
					}
					con.commit();
					System.out.println("\tname, price and total number of shares updated.");
				}
			} else {
				System.out.println(code + "\tunable to retrieve name, price or share number.");
			}
		}
		// Make sure only past share number changes are used
		st.execute("UPDATE stock, (SELECT stock_id, max(snc.number_of_shares) sn FROM share_number_change snc WHERE change_date <= current_date() GROUP BY stock_id) sn SET stock.number_of_shares = sn.sn WHERE stock.id = sn.stock_id");
		con.commit();
		con.close();
	}

	/**
	 * Refresh financial statements from websites (Tong Da Xin or Tong Hua Shun)
	 * @throws java.io.IOException
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static void refreshFinData() throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException, IOException {
		Connection con = jdbcConnection();
		String code;
		java.util.Date cDate = new java.util.Date();
		int id, currentYear = cDate.getYear() + 1900, currentMonth = cDate.getMonth() + 1;
		PreparedStatement pInsertPLNF, pInsertBSNF, pInsertCFNF, pInsertPLF, pInsertBSF, pInsertCFF, pInsertProvision, pInsert, pUpdateStock = con.prepareStatement("UPDATE stock SET latest_year=?, latest_season=? WHERE id=?");
		con.setAutoCommit(false);
		Statement sStock = con.createStatement();
		Statement sCreateFinData = con.createStatement();
		sStock.execute("UPDATE stock SET latest_year = 2008, latest_season = 0 WHERE latest_year IS NULL");
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
//		rs = sStock.executeQuery("SELECT id, code, latest_year, latest_season FROM stock WHERE code = 000568 ORDER BY code DESC");
		System.out.println("Current report season: "+currentYear+" "+currentMonth/3);
		rs = sStock.executeQuery("SELECT id, code, latest_year, latest_season, is_financial FROM stock WHERE (latest_year*12+latest_season*3) < "+(currentYear*12+currentMonth/3*3)+" AND NOT is_ignored ORDER BY code ASC");
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
		EnumMap<SheetType, PreparedStatement> financialInsert = new EnumMap<>(SheetType.class);
		financialInsert.put(balance_sheet, pInsertBSF);
		financialInsert.put(profit_and_loss, pInsertPLF);
		financialInsert.put(cash_flow, pInsertCFF);
		financialInsert.put(provision, pInsertProvision);
		EnumMap<SheetType, PreparedStatement> nonFinancialInsert = new EnumMap<>(SheetType.class);
		nonFinancialInsert.put(balance_sheet, pInsertBSNF);
		nonFinancialInsert.put(profit_and_loss, pInsertPLNF);
		nonFinancialInsert.put(cash_flow, pInsertCFNF);
		nonFinancialInsert.put(provision, pInsertProvision);
		EnumMap<SheetType, PreparedStatement> insert;
		String name;
		Number v;
		while (rs.next()) {
			id = rs.getInt("id");
			code = rs.getString("code");
			latestYear = rs.getInt("latest_year");
			latestSeason = rs.getInt("latest_season");
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
			System.out.println("Delete all data later than " + latestYear + "-" + latestSeason);
			sCreateFinData.addBatch("DELETE FROM profit_and_loss_nf WHERE fin_year*10 + fin_season > " + (latestYear * 10 + latestSeason) + " AND stock_id = " + id);
			sCreateFinData.addBatch("DELETE FROM balance_sheet_nf WHERE fin_year*10 + fin_season > " + (latestYear * 10 + latestSeason) + " AND stock_id = " + id);
			sCreateFinData.addBatch("DELETE FROM cash_flow_nf WHERE fin_year*10 + fin_season > " + (latestYear * 10 + latestSeason) + " AND stock_id = " + id);
			sCreateFinData.addBatch("DELETE FROM profit_and_loss_f WHERE fin_year*10 + fin_season > " + (latestYear * 10 + latestSeason) + " AND stock_id = " + id);
			sCreateFinData.addBatch("DELETE FROM balance_sheet_f WHERE fin_year*10 + fin_season > " + (latestYear * 10 + latestSeason) + " AND stock_id = " + id);
			sCreateFinData.addBatch("DELETE FROM cash_flow_f WHERE fin_year*10 + fin_season > " + (latestYear * 10 + latestSeason) + " AND stock_id = " + id);
			sCreateFinData.addBatch("DELETE FROM provision WHERE fin_year*10 + fin_season > " + (latestYear * 10 + latestSeason) + " AND stock_id = " + id);
			sCreateFinData.executeBatch();

			// update data
			cYear = -1;
			cSeason = -1;
			for (aYear = latestYear; aYear <= currentYear; aYear++) {
				for (short aSeason = (short)1; aSeason <= 4; aSeason++) {
					if (aYear == latestYear && (aSeason <= latestSeason)) {
						continue; // We already have this
					}

					if (aYear == currentYear && (currentMonth <= aSeason * 3)) {
						continue; // Season hasn't finished yet, no need to try to get reports not existing
					}

					someSheetsAreEmpty = false;
					for (SheetType sheetName : SheetType.values()) {
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
		if (!stocksToUpdateReportDates.isEmpty()) {
			System.out.println("Refreshing report publication dates.");
			currentYear = new Date().getYear()+1900;
			for (Map.Entry<String, Integer> entry: stocksToUpdateReportDates.entrySet()) {
				refreshReportPubDatesForStock(con, entry.getKey(), entry.getValue(), currentYear);
			}
		}
	}

	/**
	 * Refresh dividend payout from website (Hexun)
	 */
	public static void refreshDividendData() throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException {
		Connection con = jdbcConnection();
		Statement sStock = con.createStatement();
		PreparedStatement ps = con.prepareStatement("INSERT INTO dividend (stock_id, announcement_date, amount, bonus, split, payment_date, total_amount) VALUES (?, ?, ?, ?, ?, ?, ?)");
		PreparedStatement ps1 = con.prepareStatement("UPDATE dividend SET amount = ?, bonus = ?, split = ?, payment_date = ?, total_amount = ? WHERE stock_id = ? AND announcement_date = ?");
		ResultSet rs;
		rs = sStock.executeQuery("SELECT id, code, name, latest_year, latest_season FROM stock ORDER BY code");
		String code, name;
		int id;
		float amount, bonus, split;
		double total_amount;
		java.sql.Date paymentDate;
		con.setAutoCommit(false);
		java.sql.Date announcementDate;
		while (rs.next()) {
			id = rs.getInt("id");
			name = rs.getString("name");
			code = rs.getString("code");
			System.out.println(code+" - "+name);
			SecurityDividendData sdd = new Hexun2008DividendData(code);
			sdd.getDividendRecords();
			Map.Entry<Date, SecurityDividendRecord> e = sdd.getDividendRecords().pollLastEntry();
			while (e != null)
			{
				announcementDate = new java.sql.Date(e.getKey().getTime());
				paymentDate = e.getValue().getPaymentDate() == null ? null : new java.sql.Date(e.getValue().getPaymentDate().getTime());
				amount = e.getValue().getAmount();
				bonus = e.getValue().getBonus();
				split = e.getValue().getSplit();
				total_amount = e.getValue().getTotal_amount();
				System.out.println(announcementDate+"\t"+amount+"\t"+paymentDate);
				ps.setInt(1, id);
				ps.setDate(2, announcementDate);
				ps.setFloat(3, amount);
				ps.setFloat(4, bonus);
				ps.setFloat(5, split);
				ps.setDate(6, paymentDate);
				ps.setDouble(7, total_amount);
				try {
					ps.executeUpdate();
				} catch (SQLException ex) {
					if (ex.getMessage().contains("Duplicate")) {
						ps1.setFloat(1, amount);
						ps1.setFloat(2, bonus);
						ps1.setFloat(3, split);
						ps1.setDate(4, paymentDate);
						ps1.setDouble(5, total_amount);
						ps1.setInt(6, id);
						ps1.setDate(7, announcementDate);
						ps1.executeUpdate();
					} else {
						con.rollback();
						ex.printStackTrace();
						System.out.println("Unexpected exception caught when refreshing dividend records for "+code);
					}
					break;
				}
				e = sdd.getDividendRecords().pollLastEntry();
			}
			con.commit();
		}
	}

	// Bulk-load stock pricing data from THS, make sure THS pricing data is complete before doing this!!!!!
	public static void refreshStockPriceHistories() throws IOException, SQLException, ParseException, ClassNotFoundException, IllegalAccessException, InstantiationException {
		Connection c = jdbcConnection();
		Statement s = c.createStatement();
		ResultSet rs = s.executeQuery("SELECT code, id, name FROM stock ORDER BY code");
		int id;
		String code, name;
		while (rs.next()) {
			id = rs.getInt("id");
			code = rs.getString("code");
			name = rs.getString("name");
			if (name == null) continue;
//			if (code.startsWith("9") || code.startsWith("6")) {
//				f = new File(THS_BASE_DIR+"history/shase/day/"+code+".day");
//			} else {
//				f = new File(THS_BASE_DIR+"history/sznse/day/"+code+".day");
//			}
			System.out.println(code+" "+name);
//			if (f.exists()) {
//			} else {
//				System.out.println("... No pricing data for this stock...");
//			}
			refreshStockPriceHistory(id, code, c);
		}
		c.close();
	}

	public static void updateReportPubDates() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
		Connection con = jdbcConnection();
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery("SELECT id, code FROM stock WHERE true and code IN (select\n" +
				"s.code\n" +
				"from\n" +
				"(select min(fin_year) min_y, max(fin_year) max_y, stock_id from report_pub_dates group by stock_id) m,\n" +
				"(select count(*) count, stock_id, fin_year from report_pub_dates group by stock_id, fin_year) c,\n" +
				"stock s\n" +
				"where\n" +
				"s.id = m.stock_id and\n" +
				"m.stock_id = c.stock_id and\n" +
				"c.fin_year < max_y and c.fin_year > min_y and count <> 4 and not s.is_ignored\n" +
				"order by s.code, c.fin_year) ORDER BY code");
		String stockCode;
		int stockId, currentYear = new Date().getYear()+1900;
		while (rs.next()) {
			stockCode = rs.getString("code");
			stockId = rs.getInt("id");
//			refreshReportPubDatesForStock(con, stockCode, stockId, currentYear);
			refreshReportPubDatesForStock(con, stockCode, stockId, 2006);
			refreshReportPubDatesForStock(con, stockCode, stockId, 2012);
		}
	}

	private static void refreshReportPubDatesForStock(Connection con, String stockCode, int stockId, int aroundYear) throws IOException, SQLException {
		con.setAutoCommit(true);
		PreparedStatement ps = con.prepareStatement("INSERT INTO report_pub_dates (stock_id, fin_year, fin_season, fin_date) VALUES (?, ?, ?, ?)");
		int season;
		SortedMap<String, String> rDates;
		int s;
		int year;
		System.out.println(stockCode);
		for (season = 4; season >0; season --) {
			rDates = extractReportDates2(stockCode, season, aroundYear, aroundYear-6);
			for (Map.Entry<String, String> entry : rDates.entrySet()) {
				try {
					yyyyDashMMDashdd.parse(entry.getValue());
				} catch (ParseException ex) {
					break;
				}
				year = Integer.parseInt(entry.getKey().substring(0, 4));
				s = Integer.parseInt(entry.getKey().substring(5, 6));
				if (s != season) {
					System.out.println("Caution!!!!");
				}
				System.out.println(entry.getKey()+" "+entry.getValue()+": "+year+" "+s);
				ps.setInt(1, stockId);
				ps.setInt(2, year);
				ps.setInt(3, s);
				ps.setString(4, entry.getValue());
				ps.addBatch();
			}
			try {
				ps.executeBatch();
			} catch (BatchUpdateException ex) {
				System.out.println(ex.getMessage());
			}
		}
	}

	static final Date earliest = new Date(3, 3, 3);

	private static void refreshStockPriceHistory(int stockId, String code, Connection con) throws IOException, SQLException, ParseException {

//		SecurityTimeSeriesData ts = new THSPriceHistory(code);
		SecurityTimeSeriesData ts = new TDXPriceHistory(code);
		Statement st = con.createStatement();
		HashMap<Integer, PreparedStatement> pm = new HashMap<>();
		PreparedStatement ps;
		Statement s = con.createStatement();
		Date latest = null;
		int currentYear = new Date().getYear()+1900;
		int priceYear;
		ResultSet rs;
		for (int y = currentYear; y >= 1991; y --) {
			rs = s.executeQuery("SELECT max(date) FROM stock_price_"+y+" WHERE stock_id = "+stockId);
			if (rs.next()) {
				latest = rs.getDate(1);
				if (latest != null) {
					System.out.println("Got: "+yyyyMMdd.format(rs.getDate(1)));
					break;
				}
			}
		}
		if (latest == null) {
			latest = earliest;
		}
		System.out.println("Latest: " + yyyyMMdd.format(latest));
		con.setAutoCommit(false);
//		System.out.println((fc.size()-headerSize)/recordSize);
		SecurityTimeSeriesDatum temp;
		while (ts.hasNext()) {
			temp = ts.next();
			priceYear = temp.getDate().getYear()+1900;
			if (temp.getDate().after(latest)) {
				System.out.println(""+priceYear + (temp.getDate().getMonth() + 1) + temp.getDate().getDate());
			} else {
				break;
			}
			ps = pm.get(priceYear);
			if (ps == null) {
				ps = con.prepareStatement("INSERT INTO stock_price_"+priceYear+" (stock_id, date, open, high, low, close, avg, adjustment_factor) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
				pm.put(priceYear, ps);
			}
			ps.setInt(1, stockId);
			ps.setDate(2, temp.getDate());
			ps.setInt(3, temp.getOpen());
			ps.setInt(4, temp.getHigh());
			ps.setInt(5, temp.getLow());
			ps.setInt(6, temp.getClose());
			ps.setInt(7, (temp.getOpen()+temp.getHigh()+temp.getLow()+temp.getClose())/4);
			ps.setObject(8, null);
//			ps.executeUpdate();
			ps.addBatch();
		}
		for (PreparedStatement p : pm.values()) {
			p.executeBatch();
		}
		con.commit();
		ts.close();
		st.close();
	}

	static URL url;

	static {
		try {
			url = new URL("http://112.95.250.13/search/stockfulltext.jsp");
//			url = new URL("http://www.cninfo.com.cn/search/stockfulltext.jsp");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	static String [] seasonParam = {"010305","010303", "010307", "010301"};

	private static SortedMap<String, Integer> extractReportDates (String stockCode, int season, int endYear, int startYear) throws IOException {

		URLConnection connection = url.openConnection();
		connection.setDoOutput(true);
		OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "gb2312");
		out.write("noticeType="+seasonParam[season-1]+"&stockCode="+stockCode+"&endTime="+endYear+"-12-31&startTime="+startYear+"-01-01&pageNo=1");

		out.flush();
		out.close();

		String sCurrentLine;
		sCurrentLine = "";
		InputStream l_urlStream;
		l_urlStream = connection.getInputStream();

		BufferedReader l_reader = new BufferedReader(new InputStreamReader(l_urlStream));
		SortedMap<String, Integer> reportDates = new TreeMap<>();
		int index;
		String year;
		Set<String> yearSet = new HashSet<>();
		while ((sCurrentLine = l_reader.readLine()) != null) {
			if (sCurrentLine.contains("<span class=\"time\">")) {
				index = sCurrentLine.indexOf("<span class=\"time\">");
				sCurrentLine = sCurrentLine.substring(index+19,index+29);
				year = sCurrentLine.substring(0,4);
				if (!yearSet.contains(year)) {
					yearSet.add(year);
					reportDates.put(sCurrentLine, season);
				}
			}
		}
		return reportDates;
	}
	public static SortedMap<String, String> extractReportDates2 (String stockCode, int season, int endYear, int startYear) throws IOException {

		String s;
		URLConnection connection = url.openConnection();
		connection.setDoOutput(true);
		OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "gb2312");
		out.write("noticeType="+seasonParam[season-1]+"&stockCode="+stockCode+"&endTime="+endYear+"-12-31&startTime="+startYear+"-01-01&pageNo=1");

		out.flush();
		out.close();

		String sCurrentLine;
		InputStream l_urlStream;
		l_urlStream = connection.getInputStream();

		Matcher m;
		BufferedReader l_reader = new BufferedReader(new InputStreamReader(l_urlStream));
		SortedMap<String, String> reportDates = new TreeMap<>();
		String year;
		while ((sCurrentLine = l_reader.readLine()) != null) {
			m = p.matcher(sCurrentLine);
			if (m.find()) {
				year = m.group(1);
				s = m.group(2);
				if (s.contains(s1Report)) {
						s = " 1";
				} else if (s.contains(s2Report) || s.contains(s2Report2)) {
						s = " 2";
				} else if (s.contains(s3Report)) {
						s = " 3";
				} else if (s.contains(s4Report)) {
						s = " 4";
				} else {
					s = " "+season;
				}

				sCurrentLine = m.group(5);
				if (reportDates.get(year) == null || reportDates.get(year).compareTo(sCurrentLine) > 0) {
					reportDates.put(year+s, sCurrentLine);
				}
			}
		}
		l_reader.close();
		l_urlStream.close();
		return reportDates;
	}

	public static void calculateAdjustmentFactor (int startPricingYear) throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException {
		Connection con = jdbcConnection();
		con.setAutoCommit(false);
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery("SELECT stock_id, code, name, payment_date, round(bonus + split + 1, 3) as fct FROM dividend, stock WHERE stock_id = stock.id AND payment_date >= '1999-01-01' AND (bonus + split) <> 0 ORDER BY code, payment_date");
		int stockId = -1;
		String stockName = null;
		String stockCode = null;
		java.sql.Date dStart = null;
		java.sql.Date dEnd = null;
		float currentAdjFactor = 1;
		while (rs.next()) {
			dStart = dEnd;

			if (stockId != rs.getInt("stock_id")) {
				// new Stock coming in
				if (stockId != -1) {
					// update the last part of the last stock
					System.out.println("Stock "+stockId+" - "+stockCode+" - "+stockName);
					System.out.println("\tStart: "+ dStart);
					System.out.println("\tEnd: now");
					System.out.println("\tFactor: "+currentAdjFactor);
					adjustStockPriceHistory(stockId, dStart, null, currentAdjFactor, con, startPricingYear);
					con.commit();
				}

				// now we can clear the temp values
				stockId = rs.getInt("stock_id");
				stockCode = rs.getString("code");
				stockName = rs.getString("name");
				dStart = null;
				dEnd = null;
				currentAdjFactor = 1;
			}

			dEnd = rs.getDate("payment_date");
			if (dStart != null) {
				System.out.println("Stock "+stockId+" - "+stockCode+" - "+stockName);
				System.out.println("\tStart: "+ dStart);
				System.out.println("\tEnd: "+ dEnd);
				System.out.println("\tFactor: "+currentAdjFactor);
				adjustStockPriceHistory(stockId, dStart, dEnd, currentAdjFactor, con, startPricingYear);
			}
			currentAdjFactor *= rs.getFloat("fct");
		}
		// update the last part of the last stock
		System.out.println("Stock "+stockId+" - "+stockCode+" - "+stockName);
		System.out.println("\tStart: "+ dEnd);
		System.out.println("\tEnd: now");
		System.out.println("\tFactor: "+currentAdjFactor);
		adjustStockPriceHistory(stockId, dEnd, null, currentAdjFactor, con, startPricingYear);
		// fill in 1 as adjustment factors for the nulls
		st.executeUpdate("UPDATE stock_price_"+startPricingYear+" SET adjustment_factor = 1 WHERE adjustment_factor is NULL");
		con.commit();
	}

	private static void adjustStockPriceHistory(int stockId, java.sql.Date start, java.sql.Date end, float factor, Connection con, int startPricingYear) throws SQLException {
		int startYear = start.getYear() + 1900;
		if (startYear < startPricingYear) {
			startYear = startPricingYear;
		}
		int endYear = (end == null? FinDataConstants.currentTimeStamp.getYear() : end.getYear()) + 1900;
		PreparedStatement ps;
		for (int y = startYear; y <= endYear; y++) {
			if (end == null) {
				ps = con.prepareStatement("UPDATE stock_price_"+y+" SET adjustment_factor = ? WHERE stock_id = "+stockId+" AND date >= ?");
			} else {
				ps = con.prepareStatement("UPDATE stock_price_"+y+" SET adjustment_factor = ? WHERE stock_id = "+stockId+" AND date >= ? AND date < ?");
				ps.setDate (3, end);
			}
			ps.setFloat(1, factor);
			ps.setDate(2, start);
			ps.executeUpdate();
		}
	}

	private static Connection jdbcConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection con = DriverManager.getConnection(ResourceUtil.getString(JDBC_URL), ResourceUtil.getString(JDBC_USER), ResourceUtil.getString(JDBC_PASS));
		return con;
	}

	public static void test() throws IOException, DocumentException, SAXException, SQLException {
		DOMParser parser = new DOMParser();
		parser.parse("http://stockdata.stock.hexun.com/2008/lr.aspx?stockid=600016&accountdate=2005.12.31");
		DOMReader domReader = new DOMReader();
		Document doc = domReader.read(parser.getDocument());
		XPath tablePath = doc.createXPath("//DIV[@id=\"zaiyaocontent\"]//TBODY/TR[position()>1 and position()<last()]");
		XPath fieldNamePath = doc.createXPath(".//STRONG[1]");
		XPath fieldValuePath = doc.createXPath("./TD[2]/DIV");
		Element nameElement, valueElement;
		Object o = tablePath.evaluate(doc);

//		Connection con = DriverManager.getConnection("jdbc.mysql://localhost/findata", "root", "108129");
//		con.setAutoCommit(false);
//		Statement stmt = con.createStatement();
//		stmt.executeQuery("SELECT id, name FROM source ORDER BY id");
//		stmt.executeQuery("SELECT code FROM stock ORDER BY id");
//		stmt.executeQuery("SELECT name FROM fin_period ORDER BY id");
//		stmt.executeQuery("SELECT id, name FROM fin_sheet ORDER BY id");

		for (Element e : (List<Element>) o) {
			nameElement = (Element) fieldNamePath.evaluate(e);
			valueElement = (Element) fieldValuePath.evaluate(e);
			System.out.print(nameElement.getText());
			System.out.print(" ");
			System.out.println(valueElement.getText());
		}
	}
}