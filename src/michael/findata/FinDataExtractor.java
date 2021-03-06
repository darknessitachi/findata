package michael.findata;

import michael.findata.external.*;
import michael.findata.external.hexun2008.Hexun2008DataException;
import michael.findata.external.hexun2008.Hexun2008DividendData;
import michael.findata.external.hexun2008.Hexun2008FinancialSheet;
import michael.findata.external.hexun2008.Hexun2008ShareNumberDatum;
import michael.findata.external.netease.NeteaseTradingDatum;
import michael.findata.external.shse.SHSEFinancialReportDailyList;
import michael.findata.external.shse.SHSEReportPublication;
import michael.findata.external.szse.SZSEFinancialReportDailyList;
import michael.findata.external.szse.SZSEReportPublication;
import michael.findata.external.tdx.TDXFileBasedPriceHistory;
import michael.findata.util.FinDataConstants;
import michael.findata.util.ResourceUtil;
import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.URISyntaxException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import static michael.findata.util.FinDataConstants.*;
import static michael.findata.util.FinDataConstants.SheetType.*;

enum StyleRefreshFinData {
	FULL,
	FILL_MISSING_ACCORDING_TO_REPORT_PUBLICATION_DATE,
	FILL_GIVEN
}

public class FinDataExtractor {

	public static void main(String args[]) throws IOException, SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException, URISyntaxException, DocumentException, SAXException, ParseException, ExternalDataException {
		Date dt = new Date();
//		refreshStockCodes(); //
//		refreshLatestPriceNameAndNumberOfShares();
//		refreshStockPriceHistories(); //
//		updateFindataWithDates(); //
//		refreshFinData(StyleRefreshFinData.FILL_MISSING_ACCORDING_TO_REPORT_PUBLICATION_DATE, null, false);
//		refreshDividendData(); //

//		calculateAdjustmentFactor(DAYS_ADJ_FACTOR_CALC);
//		calculateMaxMinEPEB();
//		updateMissingReportPubDatesAccordingToFindata();
		updateMissingReportPubDatesAccordingToFindata2();

//		refreshStockPriceHistoryTEST(1,"600000", jdbcConnection());
//		refreshReportPubDatesForStockForAYear(jdbcConnection(), "000758", 1804, 2008);
		System.out.printf("%d",(new Date().getTime() - dt.getTime()));
	}

	public static void refreshLatestPriceNameAndNumberOfShares() throws SQLException, IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
		Connection con = jdbcConnection();
		con.setAutoCommit(false);
		ResultSet rs;
		int updateCount;
		PreparedStatement p = con.prepareStatement("SELECT code, id FROM stock WHERE last_updated < ? OR last_updated IS NULL ORDER BY code");
		PreparedStatement pUpdateStock = con.prepareStatement("UPDATE stock SET name=?, current_price=?, number_of_shares=?, last_updated=? WHERE code=?");
		PreparedStatement shareNumberChange = con.prepareStatement("SELECT count(*) FROM share_number_change WHERE stock_id = ?");
		PreparedStatement updateShareNumberChange = con.prepareStatement("INSERT INTO share_number_change (stock_id, change_date, number_of_shares) VALUES (?, ?, ?)");
		PreparedStatement deleteObsolete = con.prepareStatement("DELETE FROM share_number_change WHERE stock_id = ? and change_date = ?");
		Date d = new Date();
		java.sql.Date today = new java.sql.Date(d.getTime());
		d.setHours(15);
		d.setMinutes(0);
		d.setSeconds(0);
		p.setDate(1, today);
		rs = p.executeQuery();
		ResultSet shareNumberChangeRs;
		String code;
		int stock_id, times;
		Statement st = con.createStatement();
		while (rs.next()) {
			code = rs.getString(1);
			stock_id = rs.getInt(2);
			NeteaseTradingDatum td = new NeteaseTradingDatum(code);
			Hexun2008ShareNumberDatum snd;
			try {
				snd = new Hexun2008ShareNumberDatum(code);
			} catch (Hexun2008DataException e) {
				System.out.println(code + "\tUnable to retrieve name, price or share number.");
				continue;
			}
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
					System.out.println("\tUnable to update name and price. " + updateCount + " rows updated.");
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
								// Delete the duplicate entry and re-do the insert again.
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
					System.out.println("\tName, price and total number of shares updated.");
				}
			} else {
				System.out.println(code + "\tUnable to retrieve name, price or share number.");
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
	 * @param stockCodesToUpdateFindata
	 */
	public static void refreshFinData(StyleRefreshFinData style, HashSet<String> stockCodesToUpdateFindata, boolean updateReportPublicationDates) throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException, IOException {
		Connection con = jdbcConnection();
		String code;
		java.util.Date cDate = FinDataConstants.currentTimeStamp;
		int id, currentYear = cDate.getYear() + 1900, currentMonth = cDate.getMonth() + 1;
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

		if (style == StyleRefreshFinData.FILL_GIVEN && stockCodesToUpdateFindata != null && !stockCodesToUpdateFindata.isEmpty()){
			String temp = "";
			for (String s : stockCodesToUpdateFindata) {
				temp += "'" + s + "', ";
			}
			rs = sStock.executeQuery("SELECT id, code, latest_year, latest_season, is_financial FROM stock WHERE code IN ("+temp+"' ') ORDER BY code DESC");
		} else if (style == StyleRefreshFinData.FILL_MISSING_ACCORDING_TO_REPORT_PUBLICATION_DATE) {
			// Fill Missing
			rs = sStock.executeQuery("SELECT id, code, latest_year, latest_season, is_financial FROM stock s, (select max(fin_year*10+fin_season) d, stock_id from report_pub_dates group by stock_id order by stock_id) rpd WHERE rpd.stock_id = s.id AND latest_year * 10 + latest_season < rpd.d ORDER BY code DESC");
		} else {
			// Full
			rs = sStock.executeQuery("SELECT id, code, latest_year, latest_season, is_financial FROM stock WHERE (latest_year != 2012 OR latest_season != 4) ORDER BY code DESC");
		}
		short cSeason = -1;
		boolean someSheetsAreEmpty;
		boolean isFinancial;
		Iterator<String> it;
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
			System.out.println(code);

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
							v = sheet.getValue(name);
							if (v == null) {
								v = 0;
							}
							pInsert.setObject(order, v);
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
		if (updateReportPublicationDates && !stocksToUpdateReportDates.isEmpty()) {
			System.out.println("New report found. Now trying to refresh their publication dates...");
			currentYear = new Date().getYear()+1900;
			for (Map.Entry<String, Integer> entry: stocksToUpdateReportDates.entrySet()) {
				refreshReportPubDatesForStockForAYear(con, entry.getKey(), entry.getValue(), currentYear);
			}
		}
	}

	// Bulk-load stock pricing data from THS, make sure THS pricing data is complete before doing this!!!!!
	public static void refreshStockPriceHistories() throws IOException, SQLException, ParseException, ClassNotFoundException, IllegalAccessException, InstantiationException {
		Connection c = jdbcConnection();
		Statement s = c.createStatement();
		Date dt = new Date();
		ResultSet rs = s.executeQuery("SELECT code, id, name FROM stock ORDER BY code");
//		System.out.println((new Date().getTime() - dt.getTime()));
		int id;
		String code, name;
		while (rs.next()) {
			id = rs.getInt("id");
			code = rs.getString("code");
			name = rs.getString("name");
			if (name == null) continue;
			System.out.println(code+" "+name);
			refreshStockPriceHistory(id, code, c);
		}
		c.close();
	}

	// Used daily during the earnings report seasons. Going through the daily digests of SH and SZ stock exchanges are grab the financial report publication dates,
	// after which corresponding new fin data are grabbed.
	public static void updateFindataWithDates() throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException, IOException, ParseException {
		SimpleDateFormat FORMAT_yyyyDashMMDashdd = new SimpleDateFormat(FinDataConstants.yyyyDashMMDashdd);
		Connection con = jdbcConnection();
		con.setAutoCommit(true);
		Statement st = con.createStatement();
		PreparedStatement ps = con.prepareStatement("INSERT INTO report_pub_dates (stock_id, fin_year, fin_season, fin_date) VALUES ((SELECT id FROM stock WHERE code = ?), ?, ?, ?)");
		// Figure out the last date when we did this
		ResultSet rs = st.executeQuery("SELECT max(_date) FROM fin_data_updates");
		rs.next();
		GregorianCalendar last = new GregorianCalendar();
		last.setTimeInMillis(rs.getDate(1).getTime());
		last.add(Calendar.DATE, -2);

		HashSet<String> stocksToUpdateFindata = new HashSet<>();
		HashSet<ReportPublication> pubs = new HashSet<>();
		do {
			System.out.println("Getting report publisheds on "+FORMAT_yyyyDashMMDashdd.format(last.getTime())+"...");
			pubs.addAll(new SHSEFinancialReportDailyList(last.getTime()).getReportPublications());
			pubs.addAll(new SZSEFinancialReportDailyList(last.getTime()).getReportPublications());
			last.add(Calendar.DATE, 1);
		} while (last.getTimeInMillis()<FinDataConstants.currentTimeStamp.getTime());

		for (ReportPublication p : pubs) {
			ps.setString(1, p.getCode());
			ps.setInt(2, p.getYear());
			ps.setInt(3, p.getSeason());
			ps.setDate(4, new java.sql.Date(p.getDate().getTime()));
			System.out.println(p.getCode() + " " + FORMAT_yyyyDashMMDashdd.format(p.getDate()) + ": " + p.getYear() + " " + p.getSeason());
			try {
				ps.executeUpdate();
				stocksToUpdateFindata.add(p.getCode());
			} catch (SQLException ex) {
				if (!ex.getMessage().contains("Duplicate entry")) {
					// We are making some overlap to ensure all dates are captured.
					System.out.println(ex.getMessage());
				}
			}
		}

		// Finally a timestamp and delete meaningless data
		st.executeUpdate("UPDATE fin_data_updates SET _date = current_date()");
		st.executeUpdate("DELETE FROM report_pub_dates WHERE stock_id IS NULL;");
		con.close();

		if (!stocksToUpdateFindata.isEmpty()) {
			refreshFinData(StyleRefreshFinData.FILL_GIVEN, stocksToUpdateFindata, false);
		}
	}

	// fix report data publication dates according to financial report data. If there are records in financial report data for a particular stock, year, season but
	// there is no corresponding report publication dates for them, populate report_pub_dates table for that combination.
	public static void updateMissingReportPubDatesAccordingToFindata2 () throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, IOException {
		Connection con = jdbcConnection();
		Statement st = con.createStatement();
		int y, season;
		ResultSet rs = st.executeQuery(
						"select s.id, s.code, fr.stock_id, fr.fin_year, fr.fin_season\n" +
						"from stock s join fin_rep_f fr on s.id = fr.stock_id left outer join report_pub_dates rpd\n" +
						"on rpd.stock_id = fr.stock_id and fr.fin_year = rpd.fin_year and fr.fin_season = rpd.fin_season\n" +
						"where rpd.stock_id is null\n" +
						"union\n" +
						"select s.id, s.code, fr.stock_id, fr.fin_year, fr.fin_season\n" +
						"from stock s join fin_rep_nf fr on s.id = fr.stock_id left outer join report_pub_dates rpd\n" +
						"on rpd.stock_id = fr.stock_id and fr.fin_year = rpd.fin_year and fr.fin_season = rpd.fin_season\n" +
						"where rpd.stock_id is null order by fin_year desc, fin_season desc, code");
		String stockCode;
		int stockId;
		while (rs.next()) {
			stockCode = rs.getString("code");
			stockId = rs.getInt("id");
			y = rs.getInt("fin_year");
			season = rs.getInt("fin_season");

			if (stockCode.startsWith("9") || stockCode.startsWith("200")) {
				stockCode = FinDataConstants.BAShareCodeRef.get(stockCode);
			}
			if (stockCode == null) {
				System.out.println("Can't find corresponding A share for stock "+rs.getString("code"));
			} else {
				refreshReportPubDatesForStockForASeason(con, stockCode, stockId, y, season);
			}
		}
	}

	private static void refreshReportPubDatesForStockForASeason(Connection con, String stockCode, int stockId, int year, int season) throws SQLException, IOException {
		con.setAutoCommit(true);
		PreparedStatement ps = con.prepareStatement("INSERT INTO report_pub_dates (stock_id, fin_year, fin_season, fin_date) VALUES (?, ?, ?, ?)");
		ReportPublication repPub;
//		System.out.println(stockCode);
		try {
			if (stockCode.startsWith("6") || stockCode.startsWith("9")) {
				repPub = new SHSEReportPublication(stockCode, year, season);
			} else {
				repPub = new SZSEReportPublication(stockCode, year, season);
			}
		} catch (ParseException e) {
			System.err.println("ParseException " + e.getMessage() + " when trying to get report publication date for " + stockCode + " " + year + " " + season);
			return;
		} catch (FileNotFoundException e) {
			System.err.println("FileNotFoundException " + e.getMessage() + " when trying to get report publication date for " + stockCode + " " + year + " " + season);
			return;
		}
		System.out.println(stockCode + " " + year + " " + season + " published on " + repPub.getDate());
		ps.setInt(1, stockId);
		ps.setInt(2, repPub.getYear());
		ps.setInt(3, repPub.getSeason());
		ps.setDate(4, new java.sql.Date(repPub.getDate().getTime()));
		ps.addBatch();

		try {
			ps.executeBatch();
		} catch (BatchUpdateException ex) {
			if (!ex.getMessage().contains("Duplicate entry")) {
				// We are requesting for info for the past 6 years, it's common to have quite a lot of duplicates that already exist in our database.
				System.out.println(ex.getMessage());
			}
		}
	}

	public static void updateMissingReportPubDatesAccordingToFindata() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
		Connection con = jdbcConnection();
		Statement st = con.createStatement();
		int y;
		ResultSet rs = st.executeQuery(
				"select s.id, s.code, s.latest_year year from report_pub_dates rpd right outer join stock s on s.id = rpd.stock_id and rpd.fin_year = s.latest_year and rpd.fin_season = s.latest_season where rpd.stock_id is null and not s.is_ignored union \n"+
				"select\n" +
				"  s.id id,\n" +
				"  s.code code,\n" +
//				"  s.name,\n" +
				"  c.fin_year year\n" +
//				"  c.count,\n" +
//				"  m.min_y,\n" +
//				"  m.max_y\n" +
				"from\n" +
				"    (select min(fin_year) min_y, max(fin_year) max_y, stock_id from report_pub_dates group by stock_id) m,\n" +
				"    (select count(*) count, stock_id, fin_year from report_pub_dates group by stock_id, fin_year) c,\n" +
				"    stock s\n" +
				"where\n" +
				"  s.id = m.stock_id and\n" +
				"  m.stock_id = c.stock_id and\n" +
				"  c.fin_year < max_y and c.fin_year > min_y and count <> 4 and\n" +
				"  not s.is_ignored\n");
		String stockCode;
		int stockId;
//		int currentYear = new Date().getYear()+1900;
		while (rs.next()) {
			stockCode = rs.getString("code");
			stockId = rs.getInt("id");
			y = rs.getInt("year");

			if (stockCode.startsWith("9") || stockCode.startsWith("200")) {
				stockCode = FinDataConstants.BAShareCodeRef.get(stockCode);
			}
			if (stockCode == null) {
				System.out.println("Can't find corresponding A share for stock "+rs.getString("code"));
			} else {
				refreshReportPubDatesForStockForAYear(con, stockCode, stockId, y);
			}
		}
	}

	private static void refreshReportPubDatesForStockForAYear(Connection con, String stockCode, int stockId, int year) throws SQLException, IOException {
		con.setAutoCommit(true);
		PreparedStatement ps = con.prepareStatement("INSERT INTO report_pub_dates (stock_id, fin_year, fin_season, fin_date) VALUES (?, ?, ?, ?)");
		int season;
		ReportPublication repPub;
//		System.out.println(stockCode);
		for (season = 4; season > 0; season--) {
			try {
				if (stockCode.startsWith("6") || stockCode.startsWith("9")) {
					repPub = new SHSEReportPublication(stockCode, year, season);
				} else {
					repPub = new SZSEReportPublication(stockCode, year, season);
				}
			} catch (ParseException e) {
				System.err.println("ParseException "+e.getMessage()+" when trying to get report publication date for "+stockCode+" "+year+" "+season);
				continue;
			} catch (FileNotFoundException e) {
				System.err.println("FileNotFoundException "+e.getMessage()+" when trying to get report publication date for "+stockCode+" "+year+" "+season);
				continue;
			}
			System.out.println(stockCode + " " + year + " " + season + " published on " + repPub.getDate());
			ps.setInt(1, stockId);
			ps.setInt(2, repPub.getYear());
			ps.setInt(3, repPub.getSeason());
			ps.setDate(4, new java.sql.Date(repPub.getDate().getTime()));
			ps.addBatch();
		}

		try {
			ps.executeBatch();
		} catch (BatchUpdateException ex) {
			if (!ex.getMessage().contains("Duplicate entry")) {
				// We are requesting for info for the past 6 years, it's common to have quite a lot of duplicates that already exist in our database.
				System.out.println(ex.getMessage());
			}
		}
	}

	static final Date earliest = new Date(3, 3, 3);

	private static void refreshStockPriceHistory(int stockId, String code, Connection con) throws IOException, SQLException, ParseException {

//		SecurityTimeSeriesData ts = new THSPriceHistory(code);
		SecurityTimeSeriesData ts = new TDXFileBasedPriceHistory(code);
		Statement st = con.createStatement();
//		HashMap<Integer, PreparedStatement> pm = new HashMap<>();
		PreparedStatement ps = con.prepareStatement("INSERT INTO stock_price (stock_id, date, open, high, low, close, avg, adjustment_factor) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
		Statement s = con.createStatement();
		Date latest = null;
		int currentYear = new Date().getYear()+1900;
//		int priceYear;
		ResultSet rs;

//		for (int y = currentYear; y >= 1991; y --) {
//			rs = s.executeQuery("SELECT max(date) FROM stock_price WHERE stock_id = "+stockId);
//			if (rs.next()) {
//				latest = rs.getDate(1);
//				if (latest != null) {
//					System.out.println("Got: "+yyyyMMdd.format(rs.getDate(1)));
//					break;
//				}
//			}
//		}

		rs = s.executeQuery("SELECT max(date) FROM stock_price WHERE stock_id = "+stockId);
		if (rs.next()) {
			latest = rs.getDate(1);
		}

		if (latest == null) {
			latest = earliest;
		}

		SimpleDateFormat FORMAT_yyyyMMdd = new SimpleDateFormat(FinDataConstants.yyyyMMdd);
		System.out.println("Latest: " + FORMAT_yyyyMMdd.format(latest));
		con.setAutoCommit(false);
//		System.out.println((fc.size()-headerSize)/recordSize);
		SecurityTimeSeriesDatum temp;
		while (ts.hasNext()) {
			temp = ts.next();
//			priceYear = temp.getDate().getYear()+1900;

			if (temp.getDateTime().after(latest)) {
				System.out.println((temp.getDate().getYear()+1900) + " " + (temp.getDate().getMonth() + 1) + " " + temp.getDate().getDate());
			} else {
				break;
			}
//			ps = pm.get(priceYear);
//			if (ps == null) {
//				ps = con.prepareStatement("INSERT INTO stock_price_"+priceYear+" (stock_id, date, open, high, low, close, avg, adjustment_factor) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
//				pm.put(priceYear, ps);
//			}
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
//		for (Map.Entry<Integer, PreparedStatement> e : pm.entrySet()) {
//			PreparedStatement p = e.getValue();
//			try {
//				p.executeBatch();
//				System.out.println(code + " "+ e.getKey()+" updated.");
//			}
//			catch (BatchUpdateException exx)
//			{
//				System.out.println(exx.getMessage());
//			}
//		}
		try {
			ps.executeBatch();
			System.out.println(code + " updated.");
		}
		catch (BatchUpdateException exx)
		{
			System.out.println(exx.getMessage());
		}
		con.commit();
		ts.close();
		st.close();
	}

//	static URL cninfoListedCompanyReportUrl;
//
//	static {
//		try {
//			cninfoListedCompanyReportUrl = new URL("http://112.95.250.13/search/stockfulltext.jsp");
////			cninfoListedCompanyReportUrl = new URL("http://www.cninfo.com.cn/search/stockfulltext.jsp");
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		}
//	}

//	static String [] seasonParam = {"010305","010303", "010307", "010301"};

	public static void calculateAdjustmentFactor (int noOfDaysToCover) throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException {
		Connection con = jdbcConnection();
		con.setAutoCommit(false);
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery("SELECT stock_id, code, name, payment_date, round(bonus + split + 1, 4) as fct FROM dividend, stock WHERE stock_id = stock.id AND payment_date >= '1989-01-01' AND (bonus + split) <> 0 ORDER BY code, payment_date");
		int stockId = -1;
		String stockName = null;
		String stockCode = null;
		java.sql.Date dStart = null;
		java.sql.Date dEnd = null;

		SimpleDateFormat FORMAT_yyyyDashMMDashdd = new SimpleDateFormat(FinDataConstants.yyyyDashMMDashdd);
		Calendar cal = new GregorianCalendar();
		cal.set(Calendar.DATE, -noOfDaysToCover);
		java.sql.Date dLastFactored = new java.sql.Date(cal.getTimeInMillis());
		System.out.println("Calculating adjustment factor since "+ FORMAT_yyyyDashMMDashdd.format(dLastFactored));

		float currentAdjFactor = 1;
		while (rs.next()) {
			dStart = dEnd;
//			dLastFactored = rs.getDate("last_fct_date");
			if (stockId != rs.getInt("stock_id")) {
				// new Stock coming in
				if (stockId != -1) {
					// update the last part of the last stock
					System.out.println("Stock "+stockId+" - "+stockCode+" - "+stockName);
					System.out.println("\tStart: "+ dStart);
					System.out.println("\tEnd: now");
					System.out.println("\tFactor: "+currentAdjFactor);
					setAdjustmentFactorForStock(stockId, dStart.after(dLastFactored) ? dStart : dLastFactored, null, currentAdjFactor, con);
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
				if (dStart.after(dLastFactored)) {
					System.out.println("Stock "+stockId+" - "+stockCode+" - "+stockName);
					System.out.println("\tStart: "+ dStart);
					System.out.println("\tEnd: "+ dEnd);
					System.out.println("\tFactor: "+currentAdjFactor);
					setAdjustmentFactorForStock(stockId, dStart, dEnd, currentAdjFactor, con);
				}
			}
			currentAdjFactor *= rs.getFloat("fct");
		}
		// update the last part of the last stock
		System.out.println("Stock "+stockId+" - "+stockCode+" - "+stockName);
		System.out.println("\tStart: "+ dEnd);
		System.out.println("\tEnd: now");
		System.out.println("\tFactor: "+currentAdjFactor);
		setAdjustmentFactorForStock(stockId, dEnd.after(dLastFactored) ? dEnd : dLastFactored, null, currentAdjFactor, con);
		// fill in 1 as adjustment factors for the nulls
//		st.executeUpdate("UPDATE stock_price SET adjustment_factor = 1 WHERE adjustment_factor is NULL AND date <= ?");
		con.commit();
	}

	private static void setAdjustmentFactorForStock(int stockId, java.sql.Date start, java.sql.Date end, float factor, Connection con) throws SQLException {
		PreparedStatement ps;
		if (end == null) {
			System.out.println("Update "+stockId+" "+factor+" "+start+" - now");
			ps = con.prepareStatement("UPDATE stock_price SET adjustment_factor = ? WHERE stock_id = "+stockId+" AND date >= ?");
		} else {
			System.out.println("Update "+stockId+" "+factor+" "+start+" - "+end);
			ps = con.prepareStatement("UPDATE stock_price SET adjustment_factor = ? WHERE stock_id = "+stockId+" AND date >= ? AND date < ?");
			ps.setDate (3, end);
		}
		ps.setFloat(1, factor);
		ps.setDate(2, start);
		ps.executeUpdate();
	}

	public static void calculateMaxMinEPEB () throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
		SimpleDateFormat FORMAT_yyyyDashMMDashdd = new SimpleDateFormat(FinDataConstants.yyyyDashMMDashdd);
		Connection con = jdbcConnection();
		con.setAutoCommit(false);
		PreparedStatement pstUpdateRow = con.prepareStatement("UPDATE stock_price SET ep_last_4_seasons = ?, ep_l4s_max = ?, ep_l4s_min = ? WHERE id = ?");
		PreparedStatement pstGetPreviousMaxMin = con.prepareStatement("SELECT ep_l4s_max, ep_l4s_min, date FROM stock_price WHERE stock_id = ? AND ep_l4s_max IS NOT NULL AND ep_l4s_min IS NOT NULL ORDER BY DATE DESC LIMIT 1");
		PreparedStatement pstGetRowsToUpdate = con.prepareStatement(
				"select s.code, p.date date, p.id pid " +
				"from stock_price p inner join stock s on p.stock_id = s.id " +
				"where p.ep_last_4_seasons is null and s.code = ? AND date > ? " +
				"order by date");
		CallableStatement cst = null;
		Statement st1 = con.createStatement();
//		int currentYear = FinDataConstants.currentTimeStamp.getYear() + 1900;
		int stockId, pid;
		String stockCode;
		ResultSet rsCode = st1.executeQuery("SELECT code, id, is_financial FROM stock where true ORDER BY code");
		ResultSet rsPrice, analysis, rs;
		float usdX, hkdX, price, noShares, profit, ret, ret_max, ret_min;
		Date start = new Date(), temp;
		java.sql.Date dLastCalculated;
		double time;
		boolean isFinancial;
		while (rsCode.next()) {
			stockId = rsCode.getInt("id");
			stockCode = rsCode.getString("code");
			isFinancial = rsCode.getBoolean("is_financial");
			System.out.println(stockCode+" ...");
			pstGetPreviousMaxMin.setInt(1, stockId);
			rs = pstGetPreviousMaxMin.executeQuery();
			if (rs.next()) {
				ret_max = rs.getFloat("ep_l4s_max");
				ret_min = rs.getFloat("ep_l4s_min");
				dLastCalculated = rs.getDate("date");
			} else {
				ret_max = -1000;
				ret_min = 1000;
				dLastCalculated = new java.sql.Date(earliest.getTime());
			}
//			for (int year = currentYear-1; year <= currentYear; year ++)
//			{
			pstGetRowsToUpdate.setString(1, stockCode);
			pstGetRowsToUpdate.setDate(2, dLastCalculated);
			rsPrice = pstGetRowsToUpdate.executeQuery();
//			if (pstUpdateRow != null) {
//				pstUpdateRow.close();
//			}
//			pstUpdateRow = con.prepareStatement("UPDATE stock_price_"+year+" SET ep_last_4_seasons = ?, ep_l4s_max = ?, ep_l4s_min = ? WHERE id = ?");
			while (rsPrice.next()) {
				pid = rsPrice.getInt("pid");
				try {
					if (cst != null) cst.close();
					cst = con.prepareCall((isFinancial ? "CALL analyze_f ('" : "CALL analyze_nf ('") + stockCode + "', '" + FORMAT_yyyyDashMMDashdd.format(rsPrice.getDate("date")) + "', 1)");
					analysis = cst.executeQuery();
				} catch (SQLSyntaxErrorException ex) {
					System.out.println("Can't calculate return for " + stockCode + " " + FORMAT_yyyyDashMMDashdd.format(rsPrice.getDate("date")));
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

					ret = profit / noShares / price;
					if (noShares == 0) {
						continue;
					}
					if (ret > ret_max) ret_max = ret;
					if (ret < ret_min && ret > 0) ret_min = ret;
					pstUpdateRow.setFloat(1, ret);
					pstUpdateRow.setFloat(2, ret_max);
					pstUpdateRow.setFloat(3, ret_min);
					pstUpdateRow.setInt(4, pid);
					pstUpdateRow.executeUpdate();
					System.out.println(stockCode + "\t" + FORMAT_yyyyDashMMDashdd.format(rsPrice.getDate("date")) + "\t" + ret + "\t" + ret_max + "\t" + ret_min + "\t");
				}
			}
//			}
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
		Connection con = DriverManager.getConnection(ResourceUtil.getString(JDBC_URL), ResourceUtil.getString(JDBC_USER), ResourceUtil.getString(JDBC_PASS));
		return con;
	}
}