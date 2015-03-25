package michael.findata.service;

import michael.findata.external.ReportPublication;
import michael.findata.external.ReportPublicationList;
import michael.findata.external.cninfo.CnInfoReportPublicationList;
import michael.findata.external.netease.NeteaseFinancialReportDailyList;
import michael.findata.external.netease.NeteaseFinancialReportList;
import michael.findata.external.shse.SHSEReportPublication;
import michael.findata.external.szse.SZSEFinancialReportDailyList;
import michael.findata.external.szse.SZSEFinancialReportListOfToday;
import michael.findata.external.szse.SZSEReportPublication;
import michael.findata.util.FinDataConstants;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
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

		// get publications from daily digests
		do {
			System.out.println("Getting report published on "+ FinDataConstants.FORMAT_yyyyDashMMDashdd.format(last.getTime())+"...");
			pubs.addAll(new NeteaseFinancialReportDailyList(last.getTime(), last.getTime()).getReportPublications());
			pubs.addAll(new SZSEFinancialReportDailyList(last.getTime()).getReportPublications());
			last.add(Calendar.DATE, 1);
		} while (last.getTimeInMillis()<FinDataConstants.currentTimeStamp.getTime());

		for (ReportPublication p : pubs) {
			System.out.println(p.getCode() + " " + FinDataConstants.FORMAT_yyyyDashMMDashdd.format(p.getDate()) + ": " + p.getYear() + " " + p.getSeason());
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
    // However, this doesn't guarantee that the latest report publication dates will be update to date.
    // The reason is that this only fills the gaps as identified from the data within the report_pub_dates table.
    // In other words, for example, if the most recent report publication dates are missing from a stock, since there
    // is no gap visible from report_pub_dates's data, nothing will be done to update these two publication dates.
	public void scanForPublicationDateGaps (int earliestYear, boolean includeIgnored) {
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
						System.out.println(FinDataConstants.FORMAT_yyyyDashMMDashdd.format(rp.getDate()));
						getJdbcTemplate().update("INSERT INTO report_pub_dates (stock_id, fin_year, fin_season, fin_date) VALUES ((SELECT id FROM stock WHERE code = ?), ?, ?, ?)",
								codePrev, tempY, tempS, rp.getDate());
					} catch (Exception e) {
						toBeFilledWithNeteasePublicationList.add(codePrev);
					}
				} else {
					// SZ
					try {
						rp = new SZSEReportPublication(codePrev, tempY, tempS);
						System.out.println(FinDataConstants.FORMAT_yyyyDashMMDashdd.format(rp.getDate()));
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

	private void fillMissingReportPublicationDates(String code, ReportPublicationList rpl) {
		for (ReportPublication rp : rpl.getReportPublications()) {
			if (rp.getCode() == null) {
				continue;
			}
			try {
				getJdbcTemplate().update("INSERT INTO report_pub_dates (stock_id, fin_year, fin_season, fin_date) VALUES ((SELECT id FROM stock WHERE code = ?), ?, ?, ?)",
						code, rp.getYear(), rp.getSeason(), rp.getDate());
				System.out.println("Found: "+rp.getCode() + " " + rp.getYear() + " " + rp.getSeason() + ": "+ FinDataConstants.FORMAT_yyyyDashMMDashdd.format(rp.getDate()));
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

	/** todo missing report publication dates that can't be obtained from any source. please try harder!!
	Missing: 000001 平安银行 1988 2
	Missing: 000001 平安银行 1989 2
	Missing: 000001 平安银行 1990 2
	Missing: 000001 平安银行 1991 2
	Missing: 000002 万  科Ａ 1990 2
	Missing: 000002 万  科Ａ 1991 2
	Missing: 000004 国农科技 1991 2
	Missing: 000005 世纪星源 1991 2
	Missing: 000005 世纪星源 1991 4
	Missing: 000005 世纪星源 1992 2
	Missing: 000005 世纪星源 1992 4
	Missing: 000005 世纪星源 1993 2
	Missing: 000006 深振业Ａ 1991 2
	Missing: 000006 深振业Ａ 1991 4
	Missing: 000007 零七股份 1991 2
	Missing: 000007 零七股份 1991 4
	Missing: 000008 宝利来 1991 2
	Missing: 000008 宝利来 1991 4
	Missing: 000036 华联控股 1998 2
	Missing: 000419 通程控股 1996 2
	Missing: 000421 南京中北 1996 2
	Missing: 000422 湖北宜化 1996 2
	Missing: 000425 徐工机械 1996 2
	Missing: 000430 张家界 1996 2
	Missing: 000629 攀钢钒钛 1998 2
	Missing: 200002 万  科Ｂ 1990 2
	Missing: 200002 万  科Ｂ 1991 2
	Missing: 600601 方正科技 1991 2
	Missing: 600601 方正科技 1992 2
	Missing: 600602 仪电电子 1991 2
	Missing: 600602 仪电电子 1992 2
	Missing: 600630 龙头股份 1998 2
	Missing: 600651 飞乐音响 1991 2
	Missing: 600651 飞乐音响 1992 2
	Missing: 600652 爱使股份 1991 2
	Missing: 600652 爱使股份 1992 2
	Missing: 600653 申华控股 1991 2
	Missing: 600653 申华控股 1992 2
	Missing: 600654 飞乐股份 1991 2
	Missing: 600654 飞乐股份 1992 2
	Missing: 600655 豫园商城 1991 2
	Missing: 600655 豫园商城 1992 2
	Missing: 600656 博元投资 1991 2
	Missing: 600656 博元投资 1992 2
	Missing: 600667 太极实业 1998 2
	Missing: 600689 上海三毛 1998 2
	Missing: 600720 祁连山 1996 2
	Missing: 600724 宁波富达 1996 2
	Missing: 600732 上海新梅 1996 2
	Missing: 600736 苏州高新 1996 2
	Missing: 600739 辽宁成大 1996 2
	Missing: 600740 山西焦化 1996 2
	Missing: 600746 江苏索普 1996 2
	Missing: 600747 大连控股 1996 2
	Missing: 600749 西藏旅游 1996 2
	Missing: 600750 江中药业 1996 2
	Missing: 600753 东方银星 1996 2
	Missing: 600755 厦门国贸 1996 2
	Missing: 600756 浪潮软件 1996 2
	Missing: 600757 长江传媒 1996 2
	Missing: 600758 红阳能源 1996 2
	Missing: 601607 上海医药 1998 2
	Missing: 900901 仪电Ｂ股 1991 2
	Missing: 900901 仪电Ｂ股 1992 2
	Missing: 900922 三毛Ｂ股 1998 2
	**/
}