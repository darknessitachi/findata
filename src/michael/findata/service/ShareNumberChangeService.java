package michael.findata.service;

import michael.findata.external.ExternalDataException;
import michael.findata.external.SecurityShareNumberChange;
import michael.findata.external.hexun2008.Hexun2008DataException;
import michael.findata.external.hexun2008.Hexun2008ShareNumberDatum;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

public class ShareNumberChangeService extends JdbcDaoSupport {
	@Transactional
	public void refreshNumberOfShares() throws SQLException, IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
		SqlRowSet rs;
//		java.sql.Date today = new java.sql.Date(new java.util.Date().getTime());
		rs = getJdbcTemplate().queryForRowSet("SELECT code, id FROM stock ORDER BY code");
		ArrayList<SecurityShareNumberChange> ssnc;
		while (rs.next()) {
			System.out.print(rs.getString("code"));
			refreshNumberOfSharesForStock(rs.getInt("id"), new Hexun2008ShareNumberDatum(rs.getString("code")).getShareNumberChanges());
		}

		// Make sure only future share number changes are not used todo: shouldn't use max
//		getJdbcTemplate().update("UPDATE stock, (SELECT stock_id, max(snc.number_of_shares) sn FROM share_number_change snc WHERE change_date <= current_date() GROUP BY stock_id) sn SET stock.number_of_shares = sn.sn WHERE stock.id = sn.stock_id");
		getJdbcTemplate().update(
				"UPDATE " +
					"stock s," +
					"share_number_change snc," +
					"(SELECT stock_id, max(change_date) change_date FROM share_number_change WHERE change_date <= current_date() GROUP BY stock_id) cdt " +
				"SET " +
					"s.number_of_shares = snc.number_of_shares " +
				"WHERE " +
					"snc.change_date = cdt.change_date AND " +
					"snc.stock_id = cdt.stock_id AND " +
					"s.id = snc.stock_id");
	}

	@Transactional
	public void refreshNumberOfSharesForStock (String stockCode) {
		int stockId = getJdbcTemplate().queryForObject("SELECT id FROM stock WHERE code = ?", Integer.class, stockCode);
		refreshNumberOfSharesForStock(stockId, new Hexun2008ShareNumberDatum(stockCode).getShareNumberChanges());
		getJdbcTemplate().update("UPDATE stock, (SELECT stock_id, number_of_shares sn FROM share_number_change snc WHERE stock_id = ? AND change_date <= current_date() ORDER BY change_date DESC LIMIT 1) sn SET stock.number_of_shares = sn.sn WHERE stock.id = sn.stock_id", stockId);
	}

	private void refreshNumberOfSharesForStock(int stockId, ArrayList<SecurityShareNumberChange> snc) {
		int times = getJdbcTemplate().queryForObject("SELECT count(*) c FROM share_number_change WHERE stock_id = ?", Integer.class, stockId);
		for (int i = snc.size() - 1 - times; i > -1; i--) {
			try {
				getJdbcTemplate().update("INSERT INTO share_number_change (stock_id, change_date, number_of_shares) VALUES (?, ?, ?)",
						stockId,
						new Date(snc.get(i).getChangeDate().getTime()),
						snc.get(i).getNumberOfShares().longValue());
			} catch (Exception e) {
				if (e.getMessage().contains("Duplicate entry")) {
					// Delete the duplicate entry and re-do the insert again.
					getJdbcTemplate().update("DELETE FROM share_number_change WHERE stock_id = ? and change_date = ?",
							stockId, new Date(snc.get(i).getChangeDate().getTime()));
					i += 2;
				} else {
					// force rollback
					throw e;
				}
			}
		}
		System.out.println(" Changes in number of shares updated.");
	}
}