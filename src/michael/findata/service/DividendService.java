package michael.findata.service;

import michael.findata.external.SecurityDividendData;
import michael.findata.external.SecurityDividendRecord;
import michael.findata.external.hexun2008.Hexun2008DividendData;
import michael.findata.external.hexun2008.Hexun2008FundDividendData;
import michael.findata.model.AdjFactor;
import michael.findata.model.AdjFunction;
import michael.findata.model.Stock;
import org.joda.time.DateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.function.Function;

public class DividendService extends JdbcDaoSupport {

	/**
	 * Refresh dividend payout from website (Hexun)
	 */
	public void refreshDividendData() throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException {
		SqlRowSet rs;
		rs = getJdbcTemplate().queryForRowSet("SELECT id, code, name, latest_year, latest_season FROM stock WHERE is_fund AND NOT is_ignored ORDER BY code");
		List<Stock> stocks = new ArrayList<>();
		while (rs.next()) {
			Stock s = new Stock();
			s.setId(rs.getInt("id"));
			s.setCode(rs.getString("code"));
			s.setName(rs.getString("name"));
			stocks.add(s);
		}
		stocks.parallelStream().forEach(stock -> {
			try {
				refreshDividendDataForStock(stock.getCode(), stock.getId(), stock.getName());
			} catch (Exception e) {
				System.err.println("Unexpected exception caught when refreshing dividend records for "+stock.getCode());
				System.err.println(e.getMessage());
			}
		});
	}

	@Transactional
	public void calculateAdjFactorForStock(String code) {
		SqlRowSet rs = getJdbcTemplate().queryForRowSet(
				"SELECT dividend.id id, stock_id, code, name, payment_date, round(bonus + split + 1, 4) as fct, amount " +
				"FROM dividend, stock " +
				"WHERE stock_id = stock.id " +
//				"AND payment_date >= '2015-01-01' " +
//				"AND dividend.adj_factor is NULL " +
				"AND stock.code = ? " +
				"ORDER BY code, payment_date", code);
		Date payment_date;
		double yest_close, adj;
		while (rs.next()) {
			payment_date = rs.getDate("payment_date");
			System.out.print(rs.getString("code")+" Payment date: " + payment_date);
			try {
				yest_close = getJdbcTemplate().queryForObject("SELECT close FROM code_price WHERE code = ? AND date < ? ORDER BY date DESC LIMIT 1", Double.class, code, payment_date) / 1000d;
			} catch (Exception e) {
				continue;
			}
			System.out.print("\tyest_clse: " + yest_close);
			adj = rs.getDouble("fct")*yest_close/(yest_close-rs.getDouble("amount"));
			System.out.println("\tadj: " + adj);
			getJdbcTemplate().update("UPDATE dividend SET adj_factor = ? WHERE id = ?", adj, rs.getInt("id"));
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
		SecurityDividendData sdd;
		if (code.startsWith("5") || code.startsWith("1")) {
			sdd = new Hexun2008FundDividendData(code);
		} else {
			sdd = new Hexun2008DividendData(code);
		}
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

	// todo: handle bonus and splits 并且改为后复权
	public static void getAdjFactors (DateTime start, DateTime end, String codeA, String codeB, Stack<AdjFactor> divA, Stack<AdjFactor> divB,  JdbcTemplate jdbcTemplate) {
		SqlRowSet rsAdj = jdbcTemplate.queryForRowSet(
				"SELECT payment_date, adj_factor, code " +
						"FROM dividend, stock " +
						"WHERE stock_id = stock.id " +
						"AND code IN (?, ?) " +
						"AND (payment_date BETWEEN ? AND ?) " +
						"ORDER BY payment_date DESC", codeA, codeB, start.toLocalDate().toDate(), end.toLocalDate().toDate());
		while (rsAdj.next()) {
			if (rsAdj.getString("code").equals(codeA)) {
				if (divA.isEmpty()) {
					divA.push(new AdjFactor(rsAdj.getDate("payment_date"), rsAdj.getDouble("adj_factor")));
				} else {
					divA.push(new AdjFactor(rsAdj.getDate("payment_date"), divA.peek().factor *rsAdj.getDouble("adj_factor")));
				}
			} else {
				if (divB.isEmpty()) {
					divB.push(new AdjFactor(rsAdj.getDate("payment_date"), rsAdj.getDouble("adj_factor")));
				} else {
					divB.push(new AdjFactor(rsAdj.getDate("payment_date"), divB.peek().factor *rsAdj.getDouble("adj_factor")));
				}
			}
//			System.out.print(rsAdj.getString("code")+"\t");
//			System.out.print(rsAdj.getDate("payment_date") + "\t");
//			System.out.println(rsAdj.getDouble("adj_factor"));
		}
	}

	public void getAdjFactors(DateTime start, DateTime end, String codeA, String codeB, Stack<AdjFactor> divA, Stack<AdjFactor> divB) {
		getAdjFactors(start, end, codeA, codeB, divA, divB, getJdbcTemplate());
	}

	// Currently this only handles dividends not bonus and split
	public static void getAdjFunctions (DateTime start, DateTime end, String codeA, String codeB, Stack<AdjFunction<Integer, Integer>> divA, Stack<AdjFunction<Integer, Integer>> divB, JdbcTemplate jdbcTemplate) {
		SqlRowSet rsAdj = jdbcTemplate.queryForRowSet(
				"SELECT payment_date, adj_factor, code, amount, bonus, split " +
						"FROM dividend, stock " +
						"WHERE stock_id = stock.id " +
						"AND code IN (?, ?) " +
						"AND (payment_date BETWEEN ? AND ?) " +
						"ORDER BY payment_date DESC", codeA, codeB, start.toLocalDate().toDate(), end.toLocalDate().toDate());
		while (rsAdj.next()) {

			// Currently this only handles dividends not bonus and split
			// todo: handle bonus and splits 并且改为后复权
			// Two types of adjustments: 1. always re-invest dividend; 2. always take away dividend cash
			// This can only do type 2, not type 1, because type 1 requires spot price at the time of dividend

			// must evaluate sql query result first before putting the result into a function
			// - late evaluation doesn't work after the sql query is closed;
			int i = (int) (rsAdj.getFloat("amount") * 1000);
			Function<Integer, Integer> adjFunc = pri -> pri + i;

			if (rsAdj.getString("code").equals(codeA)) {
				divA.push(new AdjFunction<>(rsAdj.getDate("payment_date"), adjFunc));
			} else {
				divB.push(new AdjFunction<>(rsAdj.getDate("payment_date"), adjFunc));
			}
			System.out.print("Adj: ");
			System.out.print(rsAdj.getString("code")+"\t");
			System.out.print(rsAdj.getDate("payment_date") + "\t");
			System.out.println(i);
		}
	}

	public void getAdjFunctions(DateTime start, DateTime end, String codeA, String codeB, Stack<AdjFunction<Integer, Integer>> divA, Stack<AdjFunction<Integer, Integer>> divB) {
		getAdjFunctions(start, end, codeA, codeB, divA, divB, getJdbcTemplate());
	}
}