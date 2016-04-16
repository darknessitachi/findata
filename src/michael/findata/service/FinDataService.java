package michael.findata.service;

import michael.findata.external.FinancialSheet;
import michael.findata.external.hexun2008.Hexun2008FinancialSheet;
import michael.findata.model.Stock;
import michael.findata.util.FinDataConstants;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import static michael.findata.util.FinDataConstants.SheetType.*;

public class FinDataService extends JdbcDaoSupport {

	private final String INSERT_PL_NF = "INSERT INTO profit_and_loss_nf (" +
			"pl01, pl02, pl03, pl04, pl05, pl06, pl07, pl08, pl09, pl10, " +
			"pl11, pl12, pl13, pl14, pl15, pl16, pl17, pl18, pl19, pl20, " +
			"pl21, pl22, pl23, pl24, pl25, pl26, pl27, " +
			"stock_id, fin_year, fin_season) VALUES (" +
			"?, ?, ?, ?, ?, ?, ?, ?, ?," +
			"?, ?, ?, ?, ?, ?, ?, ?, ?," +
			"?, ?, ?, ?, ?, ?, ?, ?, ?," +
			"?, ?, ?)";
	private final String INSERT_BS_NF = "INSERT INTO balance_sheet_nf (" +
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
			"?, ?, ?)";
	private final String INSERT_CF_NF = "INSERT INTO cash_flow_nf (" +
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
			"?, ?, ?)";
	private final String INSERT_PL_F = "INSERT INTO profit_and_loss_f (" +
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
			"?, ?, ?)";
	private final String INSERT_BS_F = "INSERT INTO balance_sheet_f (" +
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
			"?, ?, ?)";
	private final String INSERT_CF_F = "INSERT INTO cash_flow_f (" +
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
			"?, ?, ?)";
	private final String INSERT_P = "INSERT INTO provision (" +
			"pv01, pv02, pv03, pv04, pv05, pv06, pv07, pv08, pv09, pv10, " +
			"pv11, pv12, pv13, pv14, pv15, pv16, " +
			"stock_id, fin_year, fin_season) VALUES (" +
			"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
			"?, ?, ?, ?, ?, ?, " +
			"?, ?, ?)";
	private EnumMap<FinDataConstants.SheetType, String> financialInsert;
	private EnumMap<FinDataConstants.SheetType, String> nonFinancialInsert;

	public FinDataService () {
		financialInsert = new EnumMap<>(FinDataConstants.SheetType.class);
		financialInsert.put(balance_sheet, INSERT_BS_F);
		financialInsert.put(profit_and_loss, INSERT_PL_F);
		financialInsert.put(cash_flow, INSERT_CF_F);
		financialInsert.put(provision, INSERT_P);
		nonFinancialInsert = new EnumMap<>(FinDataConstants.SheetType.class);
		nonFinancialInsert.put(balance_sheet, INSERT_BS_NF);
		nonFinancialInsert.put(profit_and_loss, INSERT_PL_NF);
		nonFinancialInsert.put(cash_flow, INSERT_CF_NF);
		nonFinancialInsert.put(provision, INSERT_P);
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
	public void refreshFinData(EnumStyleRefreshFinData style, HashSet<String> stockCodesToUpdateFindata, boolean includeIgnored, boolean ascendingOrder)
			throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException, IOException {
		java.util.Date cDate = FinDataConstants.currentTimeStamp;
		int currentYear = cDate.getYear() + 1900, currentMonth = cDate.getMonth() + 1;
		getJdbcTemplate().update("UPDATE stock SET latest_year = ?, latest_season = 0 WHERE latest_year IS NULL", currentYear - 3);
		getJdbcTemplate().update("UPDATE stock SET latest_season = 0 WHERE latest_season IS NULL");
		TreeMap<String, Integer> stocksToUpdateReportDates = new TreeMap<>();
		SqlRowSet rs;
		int id;
		String code, name;
		int latestYear, latestSeason;
		boolean isFinancial;
		System.out.println("Current report season: "+currentYear+" "+currentMonth/3);

		if (style == EnumStyleRefreshFinData.FILL_GIVEN && stockCodesToUpdateFindata != null && !stockCodesToUpdateFindata.isEmpty()){
			// Fill fin_data for the given set of stocks
			String temp = "";
			for (String s : stockCodesToUpdateFindata) {
				temp += "'" + s + "', ";
			}
			rs = getJdbcTemplate().queryForRowSet("SELECT id, code, latest_year, latest_season, is_financial, name FROM stock WHERE code IN (" + temp + "' ')"+(includeIgnored? "": " AND (NOT is_fund) AND (NOT is_ignored)")+" ORDER BY code "+(ascendingOrder ? "ASC" : "DESC"));
		} else if (style == EnumStyleRefreshFinData.FILL_RECENT_ACCORDING_TO_REPORT_PUBLICATION_DATE) {
			// Fill recent according to report_pub_dates
			rs = getJdbcTemplate().queryForRowSet("SELECT id, code, latest_year, latest_season, is_financial, name FROM stock s, (SELECT max(fin_year*10+fin_season) d, " +
					"stock_id FROM report_pub_dates GROUP BY stock_id ORDER BY stock_id) rpd WHERE rpd.stock_id = s.id AND latest_year * 10 + latest_season < rpd.d"+(includeIgnored? "": " AND (NOT is_fund) AND (NOT is_ignored)")+" ORDER BY code "+(ascendingOrder ? "ASC" : "DESC"));
		} else if (style == EnumStyleRefreshFinData.FiLL_ALL_RECENT) {
			// Fill all recent
			rs = getJdbcTemplate().queryForRowSet("SELECT id, code, latest_year, latest_season, is_financial, name FROM stock"+(includeIgnored? "": " WHERE (NOT is_fund) AND (NOT is_ignored)")+" ORDER BY code "+(ascendingOrder ? "ASC" : "DESC"));
		} else {
			// todo Fill Missing
			System.err.println("Fill_Missing hasn't been implemented yet.");
			return;
		}
		List<Stock> stocks = new ArrayList<>();
		while (rs.next()) {
			Stock stock = new Stock();
			stock.setId(rs.getInt("id"));
			stock.setCode(rs.getString("code"));
			stock.setLatestYear(rs.getInt("latest_year"));
			stock.setLatestSeason(rs.getInt("latest_season"));
			stock.setFinancial(rs.getBoolean("is_financial"));
			stock.setName(rs.getString("name"));
			stocks.add(stock);
//			if (refreshFinDataForStock(stock.getCode(), stock.getId(), currentYear, currentMonth, stock.getLatestYear(), stock.getLatestSeason(), stock.isFinancial(), stock.getName())) {
//				stocksToUpdateReportDates.put(stock.getCode(), stock.getId());
//			}
		}

		stocks.parallelStream().forEach(stock -> {
			if (refreshFinDataForStock(stock.getCode(), stock.getId(), currentYear, currentMonth, stock.getLatestYear(), stock.getLatestSeason(), stock.isFinancial(), stock.getName())) {
				stocksToUpdateReportDates.put(stock.getCode(), stock.getId());
			}
		});
	}

	@Transactional
	private boolean refreshFinDataForStock(String code, int id, int currentYear, int currentMonth, int latestYear, int latestSeason, boolean financial, String stockName) {
		EnumMap<FinDataConstants.SheetType, String> insert;
		int cYear;
		short cSeason;
		int aYear;
		boolean someSheetsAreEmpty;
		String pInsert;
		FinancialSheet sheet;
		Iterator<String> it;
		int order;
		String name;
		Number v;
		insert = financial ? financialInsert : nonFinancialInsert;
		System.out.println(code+" "+stockName);
		ArrayList<Object> argList = new ArrayList<>();

		// delete invalid rows from the table
		System.out.println("Delete all data later than " + latestYear + "-" + latestSeason);
		JdbcTemplate template = getJdbcTemplate();

		template.update("DELETE FROM profit_and_loss_nf WHERE fin_year*10 + fin_season > ? AND stock_id = ?", (latestYear * 10 + latestSeason), id);
		template.update("DELETE FROM balance_sheet_nf WHERE fin_year*10 + fin_season > ? AND stock_id = ?", (latestYear * 10 + latestSeason), id);
		template.update("DELETE FROM cash_flow_nf WHERE fin_year*10 + fin_season > ? AND stock_id = ?", (latestYear * 10 + latestSeason), id);
		template.update("DELETE FROM profit_and_loss_f WHERE fin_year*10 + fin_season > ? AND stock_id = ?", (latestYear * 10 + latestSeason), id);
		template.update("DELETE FROM balance_sheet_f WHERE fin_year*10 + fin_season > ? AND stock_id = ?", (latestYear * 10 + latestSeason), id);
		template.update("DELETE FROM cash_flow_f WHERE fin_year*10 + fin_season > ? AND stock_id = ?", (latestYear * 10 + latestSeason), id);
		template.update("DELETE FROM provision WHERE fin_year*10 + fin_season > ? AND stock_id = ?", (latestYear * 10 + latestSeason), id);

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
				for (FinDataConstants.SheetType sheetName : FinDataConstants.SheetType.values()) {
					System.out.println(code + "\t" + aYear + "\t" + aSeason + "\t" + sheetName);
					pInsert = insert.get(sheetName);
					sheet = new Hexun2008FinancialSheet(code, sheetName, aYear, aSeason);
					it = sheet.getDatumNames();
					order = 1;
					argList.clear();
					while (it.hasNext()) {
						name = it.next();
						v = sheet.getValue(name);
						if (v == null) {
							v = 0;
						}
//						pInsert.setObject(order, v);
						argList.add(v);
						order++;
					}
					someSheetsAreEmpty = someSheetsAreEmpty || (order == 1 && (sheetName == balance_sheet || sheetName == cash_flow || sheetName == profit_and_loss));
					if (order < 5) continue;
//					pInsert.setInt(order, id); // stock_id
//					pInsert.setInt(order+1, aYear); // accounting year
//					pInsert.setShort(order+2, aSeason); // accounting season
					argList.add(id); // stock_id
					argList.add(aYear); // accounting year
					argList.add(aSeason); // accounting season
					try {
//						pInsert.executeUpdate();
						getJdbcTemplate().update(pInsert, argList.toArray());
					} catch (Exception ex) {
						System.out.println("Problem: when updating "+code+"-"+aYear+"-"+aSeason+"-"+sheetName+" | field no: "+order);
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
			getJdbcTemplate().update("UPDATE stock SET latest_year=?, latest_season=? WHERE id=?", cYear, cSeason, id);
			return true;
		} else {
			return false;
		}
	}

	// This is used together with ReportPubDateService.fillLatestPublicationDateAccordingToLatestFinData
	// to make sure that report pub dates and fin_data are in sync
	public void refreshMissingFinDataAccordingToReportPubDates () {
		boolean isFinancial;
		String code;
		int stockId, year;
		short season;
		SqlRowSet rs = getJdbcTemplate().queryForRowSet("SELECT s.code, s.name, rpd.fin_year, rpd.fin_season, s.is_financial, s.id " +
				"FROM stock s INNER JOIN report_pub_dates rpd ON rpd.stock_id = s.id LEFT OUTER JOIN fin_rep_nf fr ON fr.stock_id = s.id AND fr.fin_year = rpd.fin_year AND fr.fin_season = rpd.fin_season " +
				"WHERE fr.fin_year IS null AND (NOT s.is_financial) ORDER BY rpd.fin_year DESC, rpd.fin_season DESC, code");
		while (rs.next()) {
			code = rs.getString("code");
			stockId = rs.getInt("id");
			year = rs.getInt("fin_year");
			season = rs.getShort("fin_season");
			isFinancial = rs.getBoolean("is_financial");
			System.out.println(refreshFinDataForStockYearSeason(code, stockId, year, season, isFinancial));
		}
		rs = getJdbcTemplate().queryForRowSet("SELECT s.code, s.name, rpd.fin_year, rpd.fin_season, s.is_financial, s.id " +
				"FROM stock s INNER JOIN report_pub_dates rpd ON rpd.stock_id = s.id LEFT OUTER JOIN fin_rep_f fr ON fr.stock_id = s.id AND fr.fin_year = rpd.fin_year AND fr.fin_season = rpd.fin_season " +
				"WHERE fr.fin_year IS null AND s.is_financial ORDER BY rpd.fin_year DESC, rpd.fin_season DESC, code");
		while (rs.next()) {
			code = rs.getString("code");
			stockId = rs.getInt("id");
			year = rs.getInt("fin_year");
			season = rs.getShort("fin_season");
			isFinancial = rs.getBoolean("is_financial");
			System.out.println(refreshFinDataForStockYearSeason(code, stockId, year, season, isFinancial));
		}

	}

	@Transactional
	private boolean refreshFinDataForStockYearSeason(String code, int id, int year, short season, boolean financial) {
		EnumMap<FinDataConstants.SheetType, String> insert;
		insert = financial ? financialInsert : nonFinancialInsert;
		String pInsert;
		FinancialSheet sheet;
		Iterator<String> it;
		int order;
		ArrayList<Object> argList = new ArrayList<>();
		boolean someSheetsAreEmpty = false;
		Number v;
		String name;

		for (FinDataConstants.SheetType sheetName : FinDataConstants.SheetType.values()) {
			System.out.println(code + "\t" + year + "\t" + season + "\t" + sheetName);
			pInsert = insert.get(sheetName);
			sheet = new Hexun2008FinancialSheet(code, sheetName, year, season);
			it = sheet.getDatumNames();
			order = 1;
			argList.clear();
			while (it.hasNext()) {
				name = it.next();
				v = sheet.getValue(name);
				if (v == null) {
					v = 0;
				}
				argList.add(v);
				order++;
			}
			someSheetsAreEmpty = someSheetsAreEmpty || (order == 1 && (sheetName == balance_sheet || sheetName == cash_flow || sheetName == profit_and_loss));
			if (order < 5) continue;
			argList.add(id); // stock_id
			argList.add(year); // accounting year
			argList.add(season); // accounting season
			try {
				getJdbcTemplate().update(pInsert, argList.toArray());
			} catch (Exception ex) {
				System.out.println("Problem: when updating "+code+"-"+year+"-"+season+"-"+sheetName+" | field no: "+order);
				System.out.println(ex.getMessage());
			}
		}
		return !someSheetsAreEmpty;
	}
	/**
	 * !! todo missing fin_data that can't be obtained from any source. please try harder!!
	 * 000939 凯迪电力 2005 1 balance_sheet_nf missing
	 * 000939 凯迪电力 2007 1 balance_sheet_nf missing
	 * 000939 凯迪电力 2005 1 cash_flow_nf missing
	 */
}