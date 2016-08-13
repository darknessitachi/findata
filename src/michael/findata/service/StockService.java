package michael.findata.service;

import michael.findata.algoquant.execution.datatype.depth.Depth;
import michael.findata.external.netease.NeteaseInstantSnapshot;
import michael.findata.external.netease.NeteaseTradingDatum;
import michael.findata.spring.data.repository.StockRepository;
import michael.findata.util.FinDataConstants;
import michael.findata.util.ResourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static michael.findata.util.FinDataConstants.STOCK_LIST_FILE;

public class StockService extends JdbcDaoSupport {

	@Autowired
	StockRepository stockRepo;

	/**
	 * Refresh stock code table from stock data software (Tong Da Xin or Tong Hua Shun)
	 * @throws java.io.IOException
	 * @throws java.sql.SQLException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@Transactional
	public void refreshStockCodes() throws IOException, SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(ResourceUtil.getString(STOCK_LIST_FILE))));
		String line;
		int updateCount;
//		EnumTypeCodeListFile fileType;
		ArrayList<String> codeList = new ArrayList<>();

		HashSet<String> codeSetOriginal = new HashSet<>();
		SqlRowSet rs = getJdbcTemplate().queryForRowSet("SELECT code FROM stock");
		while (rs.next()) {
			codeSetOriginal.add(rs.getString(1));
		}

//		// Determining the type of stock list file
//		br.mark(100);
//		br.readLine();
//		if (br.readLine().contains("[Market]")) {
//			fileType = EnumTypeCodeListFile.THS;
//		} else {
//			fileType = EnumTypeCodeListFile.TDX;
//		}
//		br.reset();

		switch (FinDataConstants.STOCK_LIST_TYPE) {
			case THS:
				// Getting codes from THS
				StringTokenizer tk;
				String temp[];
				int start, end;
				boolean enterMarket = false;
				DecimalFormat dm = new DecimalFormat("000000");
				while ((line = br.readLine()) != null) {
					if (line.contains("[Market_16_17]") || line.contains("[Market_16_18]") || line.contains("[Market_16_20]") || line.contains("[Market_32_33]") || line.contains("[Market_32_34]") || line.contains("[Market_32_36]")) {
						enterMarket = true;
					} else if (line.startsWith("[Market_")) {
						enterMarket = false;
					} else if (enterMarket && line.startsWith("CodeList")) {
						tk = new StringTokenizer(line, "=,", false);
						tk.nextToken();
						while (tk.hasMoreTokens()) {
							line = tk.nextToken();
							if (line.contains("-")) {
								temp = line.split("-");
								try {
									start = Integer.parseInt(temp[0]);
									end = Integer.parseInt(temp[1]);
								} catch (NumberFormatException nfe) {
									continue;
								}
								for (int i = start; i <= end; i++) {
									line = dm.format(i);
									if (!codeSetOriginal.contains(line)) {
										codeList.add(line);
										System.out.println("Added " + line + " to list");
									}
								}
							} else {
								if (!codeSetOriginal.contains(line)) {
									codeList.add(line);
									System.out.println("Added " + line + " to list");
								}
							}
						}
					}
				}
				break;
			case TDX:
				// Getting codes from TDX
				while ((line = br.readLine()) != null) {
					if (line.length() < 6 || line.startsWith("1") || line.startsWith("5")) continue;
					line = line.substring(0, 6);
					if (!codeSetOriginal.contains(line)) {
						codeList.add(line);
						System.out.println("Added " + line + " to list");
					}
				}
		}

		Collections.sort(codeList);
		for (String code : codeList) {
			if (code.startsWith("93")) {
				continue;
			}
			updateCount = 0;
			System.out.print(code + "...");
			try {
				updateCount = getJdbcTemplate().update("INSERT INTO stock (code, is_fund, is_ignored) VALUES (?, ?, FALSE)", code, code.startsWith("1")||code.startsWith("5"));
			} catch (Exception e) {
				if (e.getMessage().contains("Duplicate")) {
					System.err.println(" already exists.");
				} else {
					throw e;
				}
			}
			if (updateCount < 2) {
				if (updateCount == 1) {
					System.out.println(" inserted.");
				} else {
					System.err.println(" already exists.");
				}
			} else {
				System.err.println(" unexpected update count: " + updateCount + " has been produced.");
			}
		}
	}

	public void refreshLatestPriceAndName() throws SQLException, IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
		SqlRowSet rs;
		rs = getJdbcTemplate().queryForRowSet("SELECT code, id FROM stock WHERE ((NOT is_fund) OR (is_fund AND stock.is_interesting)) AND NOT is_ignored ORDER BY code");
		List<String> codes = new ArrayList<>();
		while (rs.next()) {
			codes.add(rs.getString("code"));
		}
		codes.parallelStream().forEach(code -> {
			try {
				refreshLatestPriceAndNameForStock(code);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
//		while (rs.next()) {
//			refreshLatestPriceAndNameForStock(rs.getString("code"));
//		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void refreshLatestPriceAndNameForStock(String code) throws IOException {
		int updateCount;
		NeteaseTradingDatum td = new NeteaseTradingDatum(code);
		if (td.getStockName() != null && td.getCurrent() != null) {
			System.out.println(td.getStockCode()+" "+td.getStockName());
			updateCount = getJdbcTemplate().update("UPDATE stock SET name=?, current_price=?, last_updated=current_date() WHERE code=?",
					td.getStockName(), td.getCurrent(), code);
			if (updateCount != 1) {
				System.err.println("\tUnable to update name and price. " + updateCount + " rows updated.");
			}
		} else {
			System.err.println(code + "\tUnable to retrieve name and price.");
		}
	}

	public void calculateAdjustmentFactor (int noOfDaysToCover) throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException {
//		SqlRowSet rs = getJdbcTemplate().queryForRowSet("SELECT stock_id, code, name, payment_date, round(bonus + split + 1, 4) as fct FROM dividend, stock WHERE stock_id = stock.id AND payment_date >= '1989-01-01' AND (bonus + split) <> 0 ORDER BY code, payment_date");
		SqlRowSet rs = getJdbcTemplate().queryForRowSet("SELECT stock_id, code, name, payment_date, round(bonus + split + 1, 4) as fct FROM dividend, stock WHERE stock_id = stock.id AND payment_date >= '1989-01-01' AND (bonus + split) <> 0 AND code = '000002' ORDER BY code, payment_date");
		int stockId = -1;
		String stockName = null;
		String stockCode = null;
		java.sql.Date dStart = null;
		java.sql.Date dEnd = null;

		Calendar cal = new GregorianCalendar();
		cal.add(Calendar.DATE, -noOfDaysToCover);
		java.sql.Date dLastFactored = new java.sql.Date(cal.getTimeInMillis());
		SimpleDateFormat FORMAT_yyyyDashMMDashdd = new SimpleDateFormat(FinDataConstants.yyyyDashMMDashdd);
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
					setAdjustmentFactorForStock(stockId, dStart.after(dLastFactored) ? dStart : dLastFactored, null, currentAdjFactor);
//					con.commit();
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
				if (dEnd.after(dLastFactored)) {
					System.out.println("Stock "+stockId+" - "+stockCode+" - "+stockName);
					System.out.println("\tStart: "+ dStart);
					System.out.println("\tEnd: "+ dEnd);
					System.out.println("\tFactor: "+currentAdjFactor);
					setAdjustmentFactorForStock(stockId, dStart.after(dLastFactored) ? dStart : dLastFactored, dEnd, currentAdjFactor);
				}
			}
			currentAdjFactor *= rs.getFloat("fct");
		}
		// update the last part of the last stock
		System.out.println("Stock "+stockId+" - "+stockCode+" - "+stockName);
		System.out.println("\tStart: "+ dEnd);
		System.out.println("\tEnd: now");
		System.out.println("\tFactor: "+currentAdjFactor);
		setAdjustmentFactorForStock(stockId, dEnd.after(dLastFactored) ? dEnd : dLastFactored, null, currentAdjFactor);
		// fill in 1 as adjustment factors for the nulls
//		st.executeUpdate("UPDATE stock_price SET adjustment_factor = 1 WHERE adjustment_factor is NULL AND date <= ?");
//		con.commit();
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	private void setAdjustmentFactorForStock(int stockId, java.sql.Date start, java.sql.Date end, float factor) throws SQLException {
		if (end == null) {
			System.out.println("Updating "+stockId+" "+factor+" "+start+" - now");
//			getJdbcTemplate().update("UPDATE stock_price SET adjustment_factor = ? WHERE stock_id = " + stockId + " AND date >= ?", factor, start);
			getJdbcTemplate().update(
					"UPDATE stock_price SET adjustment_factor = ? WHERE stock_id = ? AND date >= ?",
					factor, stockId, start);
		} else {
			System.out.println("Updating "+stockId+" "+factor+" "+start+" - "+end);
			getJdbcTemplate().update(
					"UPDATE stock_price SET adjustment_factor = ? WHERE stock_id = ? AND date >= ? AND date < ?",
					factor, stockId, start, end);
		}
	}

	public void updateSpreadForStocks (double spreadTreshold, double amountThreshold) throws IOException {
		Set<String> codes = getStockGroup("michael/findata/algoquant/strategy/pair/scope.csv");
		NeteaseInstantSnapshot snapshot = new NeteaseInstantSnapshot(codes.toArray(new String [codes.size()]));
		stockRepo.findByCodeIn(codes.toArray(new String [codes.size()])).forEach(stock -> {
			Depth d = snapshot.getDepth(stock.getCode());
			double bestAsk, bestBid, spread;
			bestAsk = d.bestAsk(amountThreshold);
			bestBid = d.bestBid(amountThreshold);
			if (bestAsk < 0 || bestBid < 0) {
				spread = 100;
			} else {
				spread = bestAsk / bestBid - 1;
			}
//			stock.setSpread(spread);
//			stockRepo.save(stock);
			System.out.println(stock.getCode()+"\t"+stock.getName()+"\t"+stock.getSpread()+"\t->\t"+spread);
		});
	}

	public Set<String> getStockGroup(String ... resourceNames) throws IOException {
		HashSet<String> codes = new HashSet<>();
		for (String rName : resourceNames) {
			codes.addAll(getStockGroup(rName));
		}
		return codes;
	}

	public Set<String> getStockGroup(String resourceName) throws IOException {
		TreeSet<String> codes = new TreeSet<>();
		BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(resourceName)));
		String line;
		while (null != (line = br.readLine())) {
			if (line.length() < 6) {
				continue;
			}
			codes.add(line.substring(0,6));
		}
		return codes;
	}
}