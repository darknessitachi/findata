package michael.findata.service;

import michael.findata.external.ReportPublication;
import michael.findata.external.ReportPublicationList;
import michael.findata.external.cninfo.CnInfoReportPublicationList;
import michael.findata.external.netease.NeteaseFinancialReportDailyList;
import michael.findata.external.netease.NeteaseFinancialReportList;
import michael.findata.external.shse.SHSEReportPublication;
import michael.findata.external.shse.SHSEReportPublicationList;
import michael.findata.external.szse.SZSEFinancialReportDailyList;
import michael.findata.external.szse.SZSEFinancialReportListOfToday;
import michael.findata.external.szse.SZSEReportPublication;
import michael.findata.util.FinDataConstants;
import org.joda.time.LocalDate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReportPubDateService extends JdbcDaoSupport {

	// Used daily during the earnings report seasons. Going through the daily digests of SH and SZ stock exchanges and
	// grab the financial report publication dates, after which the corresponding new fin data are grabbed.
	@Transactional
	public void updateFindataWithDates(int daysCovered) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException, IOException, ParseException {
		// Figure out the last date when we did this
		GregorianCalendar last = new GregorianCalendar();
		last.setTimeInMillis(getJdbcTemplate().queryForObject("SELECT max(_date) FROM fin_data_updates", java.sql.Date.class).getTime());
		last.add(Calendar.DATE, -daysCovered);

		HashSet<String> stocksToUpdateFindata = new HashSet<>();
		HashSet<ReportPublication> pubs = new HashSet<>();

		SimpleDateFormat FORMAT_yyyyDashMMDashdd = new SimpleDateFormat(FinDataConstants.yyyyDashMMDashdd);
		// get publications from daily digests
		do {
			System.out.println("Getting report published on "+ FORMAT_yyyyDashMMDashdd.format(last.getTime())+"...");
			pubs.addAll(new SHSEReportPublicationList(last.getTime(), last.getTime()).getReportPublications());
			pubs.addAll(new SZSEFinancialReportDailyList(last.getTime()).getReportPublications());
//			pubs.addAll(new NeteaseFinancialReportDailyList(last.getTime(), last.getTime()).getReportPublications());
			last.add(Calendar.DATE, 1);
		} while (last.getTimeInMillis()<FinDataConstants.currentTimeStamp.getTime());

		for (ReportPublication p : pubs) {
			System.out.println(p.getCode() + " " + FORMAT_yyyyDashMMDashdd.format(p.getDate()) + ": " + p.getYear() + " " + p.getSeason());
			try {
				getJdbcTemplate().update("INSERT INTO report_pub_dates (stock_id, fin_year, fin_season, fin_date) VALUES ((SELECT id FROM stock WHERE code = ?), ?, ?, ?)",
						p.getCode(), p.getYear(), p.getSeason(), new java.sql.Date(p.getDate().getTime()));
				stocksToUpdateFindata.add(p.getCode());
			} catch (Exception ex) {
				if (ex.getMessage().contains("Duplicate entry")) {
					// We are making some overlap on purpose to ensure all dates are captured.
				} else {
					// todo What would be the error if not from duplicate entries? I'm not sure yet.
					System.err.println(ex.getMessage());
				}
			}
		}

		// get publications from SZ today's list
		System.out.println("Getting publication dates from SZ Financial Report List of Today.");
		for (ReportPublication p : new SZSEFinancialReportListOfToday().getReportPublications()) {
			try {
				getJdbcTemplate().update("INSERT INTO report_pub_dates (stock_id, fin_year, fin_season, fin_date) VALUES ((SELECT id FROM stock WHERE name = ?), ?, ?, ?)",
						p.getName(), p.getYear(), p.getSeason(), new java.sql.Date(p.getDate().getTime()));
				stocksToUpdateFindata.add(p.getCode());
			} catch (Exception ex) {
				if (ex.getMessage().contains("Duplicate entry")) {
					// We are making some overlap on purpose to ensure all dates are captured.
				} else {
					// todo What would be the error if not from duplicate entries? I'm not sure yet.
					System.err.println(ex.getMessage());
				}
			}
		}

		// Finally a timestamp and some cleaning to delete meaningless data
		getJdbcTemplate().update("UPDATE fin_data_updates SET _date = current_date()");
		getJdbcTemplate().update("DELETE FROM report_pub_dates WHERE stock_id IS NULL");
	}


	// This is used together with FinDataService.refreshFinData(EnumStyleRefreshFinData.FILL_RECENT_ACCORDING_TO_REPORT_PUBLICATION_DATE ..
	// to make sure that report pub dates and fin_data are in sync
	public void fillLatestPublicationDateAccordingToLatestFinData () {
		SqlRowSet rs = getJdbcTemplate().queryForRowSet(
				"select rpd.*, s.latest_year, s.latest_season, s.code, s.name \n" +
						"from \n" +
						" (select max(fin_year*10+fin_season) d, stock_id from report_pub_dates group by stock_id) rpd,\n" +
						" stock s \n" +
						"where \n" +
						"  rpd.stock_id = s.id and \n" +
						"  s.latest_year*10+s.latest_season <> d and not s.is_ignored;");
		while (rs.next()) {
			fillMissingReportPublicationDatesWithNetease(rs.getString("code"));
		}
	}

	// This is used to quickly update publication dates after 2 or more seasons of report publication dates was missed.
    // However, this doesn't guarantee that the latest report publication dates will be updated to date.
    // The reason is that this only fills the gaps/holes as identified from the data within the report_pub_dates table.
    // In other words, for example, if the most recent report publication dates are missing from a stock, since there
    // is no gap visible from report_pub_dates's data, nothing will be done to update these two publication dates.
	public void scanForPublicationDateGaps (int earliestYear, boolean includeIgnored) {
		SimpleDateFormat FORMAT_yyyyDashMMDashdd = new SimpleDateFormat(FinDataConstants.yyyyDashMMDashdd);
		HashSet<String> toBeFilledWithNeteasePublicationList = new HashSet<>();
		SqlRowSet rs = getJdbcTemplate().queryForRowSet(
				"SELECT code, name, rpd.fin_year year, rpd.fin_season season " +
				"FROM report_pub_dates rpd, stock s " +
				"WHERE rpd.stock_id = s.id " + (includeIgnored ? "":"AND s.is_ignored = 0 ") +
						"AND rpd.fin_year >= " + earliestYear + " " +
				"ORDER BY code, rpd.fin_year, rpd.fin_season");
		String codePrev, namePrev;
		int yearPrev, seasonPrev;
		String code = null, name = null;
		int year = 0, season = 0;
		int i, tempY, tempS, startIndex, stopIndex, stopIndexFinal;
		java.util.Date now = new java.util.Date();
		if (now.getMonth() < 4) {
			stopIndexFinal = (now.getYear()+1900)*4;
		} else if (now.getMonth() < 8) {
			stopIndexFinal = (now.getYear()+1900)*4 + 2;
		} else if (now.getMonth() < 10) {
			stopIndexFinal = (now.getYear()+1900)*4 + 3;
		} else {
			stopIndexFinal = (now.getYear()+1900)*4 + 4;
		}
		ReportPublication rp;
		while (rs.next()) {
			codePrev = code;
			namePrev = name;
			yearPrev = year;
			seasonPrev = season;
			code = rs.getString("code");
			name = rs.getString("name");
			year = rs.getInt("year");
			season = rs.getInt("season");
			if (codePrev == null) {
				continue;
			}
			if (!codePrev.equals(code)) {
				// new stock started, need to finish off the old one
				startIndex = yearPrev*4+seasonPrev+1;
				stopIndex = stopIndexFinal;
			} else {
				startIndex = yearPrev*4+seasonPrev+1;
				stopIndex = year*4+season;
			}
			for (i = startIndex; i < stopIndex; i++) {
				tempY = i / 4;
				tempS = i % 4;
				if (tempS == 0) {
					tempY --;
					tempS = 4;
				}
				if (tempY <= 2001 && (tempS == 1 || tempS == 3)) {
					continue;
				}
				// todo start from here
//				if (tempY >= 1994) {
//					continue;
//				}
//				if (tempY < 1997) {
//					continue;
//				}
				System.out.println("Missing: "+codePrev+" "+namePrev+" "+tempY+" "+tempS);
				if (codePrev.startsWith("6") || codePrev.startsWith("9")) {
					// SH
					try {
						if (codePrev.startsWith("9")) {
							rp = new SHSEReportPublication(FinDataConstants.BAShareCodeRef.get(codePrev), tempY, tempS);
						} else {
							rp = new SHSEReportPublication(codePrev, tempY, tempS);
						}
						System.out.println(FORMAT_yyyyDashMMDashdd.format(rp.getDate()));
						getJdbcTemplate().update("INSERT INTO report_pub_dates (stock_id, fin_year, fin_season, fin_date) VALUES ((SELECT id FROM stock WHERE code = ?), ?, ?, ?)",
								codePrev, tempY, tempS, rp.getDate());
					} catch (Exception e) {
						toBeFilledWithNeteasePublicationList.add(codePrev);
					}
				} else {
					// SZ
					try {
						rp = new SZSEReportPublication(codePrev, tempY, tempS);
						System.out.println(FORMAT_yyyyDashMMDashdd.format(rp.getDate()));
						getJdbcTemplate().update("INSERT INTO report_pub_dates (stock_id, fin_year, fin_season, fin_date) VALUES ((SELECT id FROM stock WHERE code = ?), ?, ?, ?)",
								codePrev, tempY, tempS, rp.getDate());
					} catch (Exception e) {
						toBeFilledWithNeteasePublicationList.add(codePrev);
					}
				}
			}
		}
		toBeFilledWithNeteasePublicationList.forEach(this::fillMissingReportPublicationDatesWithCnInfo);
		// Some cleaning
		getJdbcTemplate().update("DELETE FROM report_pub_dates WHERE fin_year < 1800");
	}

	// This is used to fill recent missing publication dates according to current date.
	// It complements scanForPublicationDateGaps
	public void fillMissingReportPublicationDatesAccordingToCurrentDate() {
		LocalDate today = new LocalDate();
		int index;
		if (today.getMonthOfYear() < 4) {
			index = (today.getYear() - 1)*10+4;
		} else if (today.getMonthOfYear() < 7) {
			index = today.getYear()*10+1;
		} else if (today.getMonthOfYear() < 10) {
			index = today.getYear()*10+2;
		} else {
			index = today.getYear()*10+3;
		}
		SqlRowSet rs = getJdbcTemplate().queryForRowSet(
				"select code from stock where is_fund = false and is_ignored = false and latest_year*10+latest_season < "+index);
		while (rs.next()) {
			fillMissingReportPublicationDatesWithCnInfo(rs.getString("code"));
		}
	}

	private void fillMissingReportPublicationDates(String code, ReportPublicationList rpl) {
		SimpleDateFormat FORMAT_yyyyDashMMDashdd = new SimpleDateFormat(FinDataConstants.yyyyDashMMDashdd);
		for (ReportPublication rp : rpl.getReportPublications()) {
			if (rp.getCode() == null) {
				continue;
			}
			try {
				getJdbcTemplate().update("INSERT INTO report_pub_dates (stock_id, fin_year, fin_season, fin_date) VALUES ((SELECT id FROM stock WHERE code = ?), ?, ?, ?)",
						code, rp.getYear(), rp.getSeason(), rp.getDate());
				System.out.println("Found: " + rp.getCode() + " " + rp.getYear() + " " + rp.getSeason() + ": " + FORMAT_yyyyDashMMDashdd.format(rp.getDate()));
			} catch (Exception ex) {
				if (ex instanceof DuplicateKeyException) {
					// Normal
				} else {
					ex.printStackTrace();
				}
			}
		}
	}

	private void fillMissingReportPublicationDatesWithCnInfo(String code) {
		try {
			fillMissingReportPublicationDates(code, new CnInfoReportPublicationList(code));
		} catch (IOException e) {
			System.out.println("Cannot obtain "+code+"'s report publication dates from CnInfo.");
			e.printStackTrace();
		}
	}

	private void fillMissingReportPublicationDatesWithNetease(String code) {
		try {
			fillMissingReportPublicationDates(code, new NeteaseFinancialReportList(code));
		} catch (IOException e) {
			System.out.println("Cannot obtain "+code+"'s report publication dates from Netease.");
			e.printStackTrace();
		}
	}
}