package michael.findata.service;

import com.numericalmethod.algoquant.execution.datatype.product.stock.Exchange;
import michael.findata.external.SecurityDividendData;
import michael.findata.external.SecurityDividendRecord;
import michael.findata.external.hexun2008.Hexun2008DividendData;
import michael.findata.external.hexun2008.Hexun2008FundDividendData;
import michael.findata.external.tdx.TDXClient;
import michael.findata.model.AdjFactor;
import michael.findata.model.AdjFunction;
import michael.findata.model.Dividend;
import michael.findata.model.Stock;
import michael.findata.spring.data.repository.DividendRepository;
import michael.findata.spring.data.repository.StockRepository;
import michael.findata.util.FinDataConstants;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.transaction.annotation.Transactional;

import java.security.InvalidParameterException;
import java.sql.*;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;

public class DividendService extends JdbcDaoSupport {

	@Autowired
	private DividendRepository dividendRepo;

	@Autowired
	private StockRepository stockRepo;

	public boolean refreshDividendDataForFund(String code, TDXClient client) {
		boolean updated = false;
		Stock stock = stockRepo.findOneByCode(code);
		Set<Dividend> existing = dividendRepo.findByStock_Code(code);
		SimpleDateFormat sdf = new SimpleDateFormat(FinDataConstants.yyyyMMdd);
		Stack<String[]> info = client.getXDXRInfo(code);
		String [] data;
		while (!info.empty()) {
			data = info.pop();
			Dividend dividend = new Dividend();
			dividend.setStock(stock);
			float amount = Float.parseFloat(data[4])/10;
			float split;
			switch (data[3]) {
				case "1":
					split = Float.parseFloat(data[6]) / 10;
					break;
				case "11":
					split = Float.parseFloat(data[6]) - 1;
					break;
				default:
					continue;
			}
			if (amount == 0 && split == 0) {
//				System.out.println("Skipped: ");
//				for (String field : data) {
//					System.out.print(field);
//					System.out.print("\t");
//				}
//				System.out.println();
				continue;
			}
			try {
				dividend.setPaymentDate(sdf.parse(data[2]));
			} catch (ParseException e) {
				e.printStackTrace();
				continue;
			}
			if (existing.contains(dividend)) {
//				System.out.println("Already existing!");
				break;
			}
			dividend.setAmount(amount);
//			dividend.setSplit(split);
			dividend.setBonus(split);
			dividend.setAnnouncementDate(dividend.getPaymentDate());
			dividendRepo.save(dividend);
			System.out.print("Dividend saved:\t");
			System.out.print(dividend.getStock().getCode());
			System.out.print("\t");
			System.out.print(dividend.getPaymentDate());
			System.out.print("\t");
			System.out.print(dividend.getAmount());
			System.out.print("\t");
			System.out.println(split);
			updated = true;
		}
		return updated;
	}

	/**
	 * Refresh dividend payout from website (Hexun)
	 */
	public void refreshDividendData() throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException {
		SqlRowSet rs;
//		rs = getJdbcTemplate().queryForRowSet("SELECT id, code, name, latest_year, latest_season FROM stock WHERE ((NOT is_fund) OR (is_fund AND stock.is_interesting)) AND NOT is_ignored ORDER BY code");
		rs = getJdbcTemplate().queryForRowSet("SELECT id, code, name, latest_year, latest_season FROM stock WHERE (NOT is_fund) AND NOT is_ignored ORDER BY code");
		List<Stock> stocks = new ArrayList<>();
		while (rs.next()) {
			Stock s = new Stock();
			s.setId(rs.getInt("id"));
			s.setCode(rs.getString("code"));
			s.setName(rs.getString("name"));
			stocks.add(s);
		}
		stocks.parallelStream().forEach(stock -> {
			boolean [] updated = {false};
			try {
				refreshDividendDataForStock(stock.getCode(), stock.getId(), stock.getName(), updated);
			} catch (Exception e) {
				System.err.println("Unexpected exception caught when refreshing dividend records for "+stock.getCode());
				System.err.println(e.getMessage());
			}
			if (updated[0]) {
				System.out.println("Dividend updated: "+stock.getCode());
				calculateAdjFactorForStock(stock.getCode());
			}
		});
	}

	public void refreshDividendDataForInterestingFunds(TDXClient tdxClient) {
		tdxClient.connect();
		List<Stock> interestingFunds = stockRepo.findByFundAndInteresting(true, true);
		for (Stock fund : interestingFunds) {
			System.out.println("Refreshing dividend for fund: "+fund.getCode()+" "+fund.getName());
			refreshDividendDataForFund(fund.getCode(), tdxClient);
		}
		tdxClient.disconnect();
	}

	@Transactional
	public void calculateAdjFactorForStock(String code) {
		SqlRowSet rs = getJdbcTemplate().queryForRowSet(
				"SELECT dividend.id id, stock_id, code, name, payment_date, round(bonus + 1, 6) as fct, amount, stock.exchange exchange " +
				"FROM dividend, stock " +
				"WHERE stock_id = stock.id " +
//				"AND payment_date >= '2015-01-01' " +
//				"AND dividend.adj_factor is NULL " +
				"AND dividend.payment_date IS NOT NULL " +
				"AND stock.code = ? " +
				"ORDER BY code, payment_date", code);
		Date payment_date;
		double yest_close, adj;
		long currentTime = System.currentTimeMillis();
		while (rs.next()) {
			payment_date = rs.getDate("payment_date");
			if (payment_date.getTime() > currentTime) {
				// payment_date in future
				continue;
			}
			System.out.print(rs.getString("code")+" Payment date: " + payment_date);
			try {
				yest_close = getJdbcTemplate().queryForObject("SELECT close FROM code_price WHERE code = ? AND date < ? ORDER BY date DESC LIMIT 1", Double.class, code, payment_date) / 1000d;
			} catch (Exception e) {
				System.out.println();
				continue;
			}
			if ("HK".equals(rs.getString("exchange"))) {
				// H股 exchange rate
				try {
					double fx = getJdbcTemplate().queryForObject("SELECT close FROM exchange_rate_daily WHERE exchange_date < ? AND currency = 'HKD' ORDER BY exchange_date DESC LIMIT 1", Double.class, payment_date);
					yest_close = yest_close * fx;
					System.out.print("\tfx: "+fx);
				} catch (Exception e) {
					System.out.println();
					continue;
				}
			}
			System.out.print("\tyest_clse: " + yest_close);
			adj = rs.getDouble("fct")*yest_close/(yest_close-rs.getDouble("amount"));
			System.out.println("\tadj: " + adj);
			getJdbcTemplate().update("UPDATE dividend SET adj_factor = ? WHERE id = ?", adj, rs.getInt("id"));
		}
	}

	@Transactional
	public void refreshDividendDataForStock(String code, int id, String name, boolean [] updated) throws SQLException {
		updated[0] = false;
		Date announcementDate;
		Date paymentDate;
		float amount;
		float bonus;
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
		while (e != null) {
			announcementDate = new Date(e.getKey().getTime());
			paymentDate = e.getValue().getPaymentDate() == null ? null : new Date(e.getValue().getPaymentDate().getTime());
			amount = e.getValue().getAmount();
			bonus = e.getValue().getBonus() + e.getValue().getBonus2();
			if ((amount != 0 || bonus != 0) && paymentDate != null) {
				totalAmount = e.getValue().getTotal_amount();
				System.out.println(announcementDate+"\t"+amount+"\t"+paymentDate);
				try {
					getJdbcTemplate().update("INSERT INTO dividend (stock_id, announcement_date, amount, bonus, payment_date, total_amount) VALUES (?, ?, ?, ?, ?, ?)",
							id, announcementDate, amount, bonus, paymentDate, totalAmount);
					updated[0] = true;
				} catch (Exception ex) {
					if (ex.getMessage().contains("Duplicate")) {
//						getJdbcTemplate().update("UPDATE dividend SET amount = ?, bonus = ?, split = ?, payment_date = ?, total_amount = ? WHERE stock_id = ? AND announcement_date = ?",
//								amount, bonus, split, paymentDate, totalAmount, id, announcementDate);
					} else {
						//forcing rollback
						throw ex;
					}
					break;
				}
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
	public static void getAdjFunctions (DateTime start, DateTime end, String codeA, String codeB,
										Stack<AdjFunction<Integer, Integer>> divA,
										Stack<AdjFunction<Integer, Integer>> divB, JdbcTemplate jdbcTemplate) {
		SqlRowSet rsAdj = jdbcTemplate.queryForRowSet(
				"SELECT payment_date, adj_factor, code, amount, bonus, split " +
						"FROM dividend, stock " +
						"WHERE stock_id = stock.id " +
						"AND code IN (?, ?) " +
						"AND (payment_date BETWEEN ? AND ?) " +
						"ORDER BY payment_date DESC", codeA, codeB, start.toLocalDate().toDate(), end.toLocalDate().toDate());
		while (rsAdj.next()) {

			// Currently this only handles dividends not bonus and split
			// todo: handle bonus and splits
			// 已经改为后复权
			// Two types of adjustments: 1. always re-invest dividend; 2. always take away dividend cash
			// This can only do type 2, not type 1, because type 1 requires spot price at the time of dividend

			// must evaluate sql query result first before putting the result into a function
			// - late evaluation doesn't work after the sql query is closed;
			int i = (int) (rsAdj.getFloat("amount") * 1000);
			float bonus = rsAdj.getFloat("bonus");
			float split = rsAdj.getFloat("split");
			Function<Integer, Integer> adjFunc;
//			adjFunc = pri -> pri + i;
			if (bonus == 0 && split == 0) {
				adjFunc = pri -> pri + i;
			} else {
				adjFunc = pri -> (int)(pri*(1+bonus+split)) + i;
			}

			if (rsAdj.getString("code").equals(codeA)) {
				divA.push(new AdjFunction<>(rsAdj.getDate("payment_date"), adjFunc));
			} else {
				divB.push(new AdjFunction<>(rsAdj.getDate("payment_date"), adjFunc));
			}
//			System.out.print("Adj: ");
//			System.out.print(rsAdj.getString("code")+"\t");
//			System.out.print(rsAdj.getDate("payment_date") + "\t");
//			System.out.println(i);
		}
	}

	public void getAdjFunctions(DateTime start, DateTime end, String codeA, String codeB,
								Stack<AdjFunction<Integer, Integer>> divA,
								Stack<AdjFunction<Integer, Integer>> divB) {
		getAdjFunctions(start, end, codeA, codeB, divA, divB, getJdbcTemplate());
	}

	// Currently this only handles dividends not bonus and split
	public static Map<String, Stack<AdjFunction<Integer, Integer>>> getAdjFunctions (LocalDate start, LocalDate end,
																					 String [] codes,
																					 JdbcTemplate jdbcTemplate) {
		Map<String, Stack<AdjFunction<Integer, Integer>>> result = new HashMap<>();
		if (codes.length == 0) {
			return  result;
		}
		String codesPara = "";
		for (int i = codes.length - 1; i > 0; i-- ) {
			codesPara = ", '"+codes[i]+"'"+codesPara;
			result.put(codes[i], new Stack<>());
		}
		codesPara = "'"+codes[0]+"'" + codesPara;
		result.put(codes[0], new Stack<>());
		SqlRowSet rsAdj = jdbcTemplate.queryForRowSet(
				"SELECT payment_date, adj_factor, code, amount, bonus, split " +
						"FROM dividend, stock " +
						"WHERE stock_id = stock.id " +
						"AND code IN ("+codesPara+") " +
						"AND (payment_date BETWEEN ? AND ?) " +
						"ORDER BY payment_date DESC", start.toDate(), end.toDate());
		while (rsAdj.next()) {

			// Currently this only handles dividends not bonus and split
			// 已经改为后复权,已经能够处理送转股
			// Two types of adjustments: 1. always re-invest dividend; 2. always take away dividend cash
			// This can only do type 2, not type 1, because type 1 requires spot price at the time of dividend

			// must evaluate sql query result first before putting the result into a function
			// - late evaluation doesn't work after the sql query is closed;
			int i = (int) (rsAdj.getFloat("amount") * 1000);
			float bonus = rsAdj.getFloat("bonus");
			float split = rsAdj.getFloat("split");
			Function<Integer, Integer> adjFunc;
			if (bonus == 0 && split == 0) {
				adjFunc = pri -> pri + i;
			} else {
				adjFunc = pri -> (int)(pri*(1+bonus+split)) + i;
			}
			result.get(rsAdj.getString("code")).push(new AdjFunction<>(rsAdj.getDate("payment_date"), adjFunc));
//			System.out.print("Adj: ");
//			System.out.print(rsAdj.getString("code")+"\t");
//			System.out.print(rsAdj.getDate("payment_date") + "\t");
//			System.out.println(i);
		}
		return result;
	}

	public Map<String, Stack<AdjFunction<Integer, Integer>>> getAdjFunctions(LocalDate start, LocalDate end, String [] codes) {
		return getAdjFunctions(start, end, codes, getJdbcTemplate());
	}

	public PriceAdjuster newPriceAdjuster (LocalDate start, LocalDate end, String... codes) {
		return new PriceAdjuster(start, end, Arrays.asList(codes));
	}

	// This can answer queries for prices between start date and end date.
	public class PriceAdjuster {
		// in a query, both reference date and query date must be within this range, inclusive.
		private LocalDate scopeStart;
		private LocalDate scopeEnd;
		long startMillis, endMillis;
		private HashMap<String, Dividend[]> divArrays = new HashMap<>();

		public PriceAdjuster (LocalDate scopeStart, LocalDate scopeEnd, Collection<String> codes) {
			startMillis = scopeStart.toDate().getTime();
			endMillis = scopeEnd.toDate().getTime();
			if (startMillis > endMillis) {
				throw new InvalidParameterException("scopeEnd must be the same as or before scopeStart");
			}
			this.scopeStart = scopeStart;
			this.scopeEnd = scopeEnd;
			HashMap<String, List<Dividend>> divLists = new HashMap<>();
			dividendRepo.findByPaymentDateBetweenAndStock_CodeInOrderByPaymentDateDesc
					(scopeStart.toDate(), scopeEnd.toDate(), codes).forEach(dividend -> {
				String code = dividend.getStock().getCode();
				if (!divLists.containsKey(code)) {
					divLists.put(code, new ArrayList<>());
				}
				divLists.get(dividend.getStock().getCode()).add(dividend);
			});
			divLists.entrySet().forEach(entry -> {
				List<Dividend> divList;
				divList = entry.getValue();
				divArrays.put(entry.getKey(), divList.toArray(new Dividend[divList.size()]));
			});
		}

		public double adjust (String stockCode, LocalDate referenceDate, LocalDate queryDate, double price) {
			long indexInstant;
			long referenceInstant = referenceDate.toDate().getTime();
			long queryInstant = queryDate.toDate().getTime();
			// Make sure scopeStart <= referenceDate <= queryDate <= scopeEnd;
			if (startMillis > referenceInstant || referenceInstant > queryInstant || queryInstant > endMillis) {
				throw new InvalidParameterException("Please make sure scopeStart <= referenceDate <= queryDate <= scopeEnd.");
			}

			Dividend[] divs = divArrays.get(stockCode);
			if (divs == null) {
				return price;
			} else {
				for (Dividend div : divs) {
					indexInstant = div.getPaymentDate().getTime();
					if (indexInstant > queryInstant) {
					} else if (indexInstant > referenceInstant) {
						if (div.getAdjustmentFactor() == null) {
							price = price*(1+div.getBonus())+div.getAmount();
						} else {
							price = price*div.getAdjustmentFactor();
						}
					} else {
						break;
					}
				}
				return price;
			}
		}

		public int adjust (String stockCode, LocalDate referenceDate, LocalDate queryDate, int price) {
			long indexInstant;
			long referenceInstant = referenceDate.toDate().getTime();
			long queryInstant = queryDate.toDate().getTime();
			// Make sure scopeStart <= referenceDate <= queryDate <= scopeEnd;
			if (startMillis > referenceInstant || referenceInstant > queryInstant || queryInstant > endMillis) {
				throw new InvalidParameterException("Please make sure scopeStart <= referenceDate <= queryDate <= scopeEnd.");
			}

			Dividend[] divs = divArrays.get(stockCode);
			double p = price;
			if (divs == null) {
				return price;
			} else {
				for (Dividend div : divs) {
					indexInstant = div.getPaymentDate().getTime();
					if (indexInstant > queryInstant) {
					} else if (indexInstant > referenceInstant) {
						if (div.getAdjustmentFactor() == null) {
							p = p*(1+div.getBonus())+div.getAmount()*1000;
						} else {
							p = p * div.getAdjustmentFactor();
						}
					} else {
						break;
					}
				}
				return (int)p;
			}
		}
	}


	/**
	 * Only use this on HK stocks
	 * @param code
	 * @param amount
	 * @param bonus
	 * @param paymentDate "yyyy-MM-dd"
	 * @param announcementDate "yyyy-MM-dd"
	 */
	public void addDividend (String code,  String announcementDate, float amount, float bonus, String paymentDate) {
		Stock stock = stockRepo.findOneByCode(code);
		Dividend d = new Dividend();
		d.setStock(stock);
		d.setAmount(amount);
		d.setBonus(bonus);
		d.setAnnouncementDate(LocalDate.parse(announcementDate).toDate());
		d.setPaymentDate(LocalDate.parse(paymentDate).toDate());
		dividendRepo.save(d);
	}
}