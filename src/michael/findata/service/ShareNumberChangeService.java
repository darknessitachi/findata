package michael.findata.service;

import michael.findata.external.SecurityShareNumberChange;
import michael.findata.external.hexun2008.Hexun2008DataException;
import michael.findata.external.hexun2008.Hexun2008ShareNumberDatum;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShareNumberChangeService extends JdbcDaoSupport {

	public void refreshNumberOfShares() throws SQLException, IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
		SqlRowSet rs;
		rs = getJdbcTemplate().queryForRowSet("SELECT code, id FROM stock WHERE NOT is_ignored ORDER BY code");
		Map<Integer, String> stockMap = new HashMap<>();

		while (rs.next()) {
			stockMap.put(rs.getInt("id"), rs.getString("code"));
		}

		stockMap.entrySet().parallelStream().forEach(entry -> {
			Integer id = entry.getKey();
			String code = entry.getValue();
			System.out.println("Refreshing share # for stock " + code);
			try {
				refreshNumberOfSharesForStock(id, new Hexun2008ShareNumberDatum(code).getShareNumberChanges());
			} catch (Hexun2008DataException ex) {
				System.out.println(ex.getMessage());
			}
		});

		// Make sure only future share number changes are not used
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

	// Please don't mark this as transactional as it purposely uses sql exception in its logic
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
					// force rollback, since we don't understand/expect this exception
					throw e;
				}
			}
		}
		System.out.println("Changes in number of shares updated.");
	}
}