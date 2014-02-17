package michael.findata.service;

import michael.findata.external.SecurityTimeSeriesData;
import michael.findata.external.SecurityTimeSeriesDatum;
import michael.findata.external.tdx.TDXPriceHistory;
import michael.findata.util.FinDataConstants;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.util.Date;

import static michael.findata.util.FinDataConstants.yyyyDashMMDashdd;

public class StockPriceService extends JdbcDaoSupport {
	// Bulk-load stock pricing data from THS, make sure THS pricing data is complete before doing this!!!!!
	public void refreshStockPriceHistories() throws IOException, SQLException, ParseException, ClassNotFoundException, IllegalAccessException, InstantiationException {
		SqlRowSet rs = getJdbcTemplate().queryForRowSet("SELECT code, id, name FROM stock ORDER BY code");
		String code;
		while (rs.next()) {
			code = rs.getString("code");
			refreshStockPriceHistory(code);
		}
	}

	@Transactional
	public void refreshStockPriceHistory(String code) throws IOException, SQLException, ParseException {
//		SecurityTimeSeriesData ts = new THSPriceHistory(code);
		SecurityTimeSeriesData ts = new TDXPriceHistory(code);
		Date latest = null;
		int stockId;
		String name;

		SqlRowSet rs = getJdbcTemplate().queryForRowSet("SELECT max(sp.date) date, s.name name, s.id stock_id FROM stock s LEFT OUTER JOIN stock_price sp ON sp.stock_id = s.id WHERE s.code = ?", code);
		if (rs.next()) {
			latest = rs.getDate("date");
			stockId = rs.getInt("stock_id");
			name = rs.getString("name");
		} else {
			System.out.println("Stock "+ code + " cannot be found.");
			return;
		}

		if (latest == null) {
			latest = FinDataConstants.EARLIEST;
		}

		System.out.println(code+" Latest price: " + yyyyDashMMDashdd.format(latest));
//		System.err.println((fc.size()-headerSize)/recordSize);
		SecurityTimeSeriesDatum temp;
		while (ts.hasNext()) {
			temp = ts.next();

			if (temp.getDate().after(latest)) {
				System.out.println((temp.getDate().getYear()+1900) + "-" + (temp.getDate().getMonth() + 1) + "-" + temp.getDate().getDate());
			} else {
				break;
			}

			try {
				getJdbcTemplate().update("INSERT INTO stock_price (stock_id, date, open, high, low, close, avg, adjustment_factor) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
						stockId, temp.getDate(), temp.getOpen(), temp.getHigh(), temp.getLow(), temp.getClose(), (temp.getOpen() + temp.getHigh() + temp.getLow() + temp.getClose()) / 4, null);
			} catch (DuplicateKeyException ex) {
				System.err.println(ex.getMessage());
				break;
			}
		}
		System.out.println(code + " "+ name + " 's daily quote updated.");
		ts.close();
	}
}