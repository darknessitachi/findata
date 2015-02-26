package michael.findata.service;

import michael.findata.external.SecurityDividendData;
import michael.findata.external.SecurityDividendRecord;
import michael.findata.external.hexun2008.Hexun2008DividendData;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.sql.Date;
import java.util.*;

public class DividendService extends JdbcDaoSupport {

	/**
	 * Refresh dividend payout from website (Hexun)
	 */
	public void refreshDividendData() throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException {
		SqlRowSet rs;
		rs = getJdbcTemplate().queryForRowSet("SELECT id, code, name, latest_year, latest_season FROM stock WHERE NOT is_ignored ORDER BY code");
		while (rs.next()) {
			try {
				refreshDividendDataForStock(rs.getString("code"), rs.getInt("id"), rs.getString("name"));
			} catch (Exception e) {
				System.err.println("Unexpected exception caught when refreshing dividend records for "+rs.getString("code"));
				System.err.println(e.getMessage());
			}
		}
	}

	@Transactional
	private void refreshDividendDataForStock(String code, int id, String name) throws SQLException {
		Date announcementDate;
		Date paymentDate;
		float amount;
		float bonus;
		float split;
		double totalAmount;
		SecurityDividendData sdd = new Hexun2008DividendData(code);
		sdd.getDividendRecords();
		Map.Entry<java.util.Date, SecurityDividendRecord> e = sdd.getDividendRecords().pollLastEntry();
		System.out.println("Refreshing dividend data for "+code+" - "+name);
		while (e != null)
		{
			announcementDate = new Date(e.getKey().getTime());
			paymentDate = e.getValue().getPaymentDate() == null ? null : new Date(e.getValue().getPaymentDate().getTime());
			amount = e.getValue().getAmount();
			bonus = e.getValue().getBonus();
			split = e.getValue().getSplit();
			totalAmount = e.getValue().getTotal_amount();
			System.out.println(announcementDate+"\t"+amount+"\t"+paymentDate);
			try {
				getJdbcTemplate().update("INSERT INTO dividend (stock_id, announcement_date, amount, bonus, split, payment_date, total_amount) VALUES (?, ?, ?, ?, ?, ?, ?)",
						id, announcementDate, amount, bonus, split, paymentDate, totalAmount);
			} catch (Exception ex) {
				if (ex.getMessage().contains("Duplicate")) {
					getJdbcTemplate().update("UPDATE dividend SET amount = ?, bonus = ?, split = ?, payment_date = ?, total_amount = ? WHERE stock_id = ? AND announcement_date = ?",
							amount, bonus, split, paymentDate, totalAmount, id, announcementDate);
				} else {
					//forcing rollback
					throw ex;
				}
				break;
			}
			e = sdd.getDividendRecords().pollLastEntry();
		}
	}
}