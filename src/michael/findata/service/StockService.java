package michael.findata.service;

import michael.findata.external.netease.NeteaseTradingDatum;
import michael.findata.util.ResourceUtil;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;

import static michael.findata.util.FinDataConstants.STOCK_LIST_FILE;

public class StockService extends JdbcDaoSupport {
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
		EnumTypeCodeListFile fileType;
		ArrayList<String> codeList = new ArrayList<>();

		HashSet<String> codeSetOriginal = new HashSet<>();
		SqlRowSet rs = getJdbcTemplate().queryForRowSet("SELECT code FROM stock");
		while (rs.next()) {
			codeSetOriginal.add(rs.getString(1));
		}

		// Determining the type of stock list file
		br.mark(100);
		br.readLine();
		if (br.readLine().contains("[Market]")) {
			fileType = EnumTypeCodeListFile.THS;
		} else {
			fileType = EnumTypeCodeListFile.TDX;
		}
		br.reset();

		switch (fileType) {
			case THS:
				// Getting codes from THS
				StringTokenizer tk;
				String temp[];
				int start, end;
				boolean enterMarket = false;
				DecimalFormat dm = new DecimalFormat("000000");
				while ((line = br.readLine()) != null) {
					if (line.contains("[Market_16_17]") || line.contains("[Market_16_18]") || line.contains("[Market_32_33]") || line.contains("[Market_32_34]")) {
						enterMarket = true;
					} else if (line.length() < 3) {
						enterMarket = false;
					} else if (enterMarket && line.startsWith("CodeList")) {
						tk = new StringTokenizer(line, "=,", false);
						tk.nextToken();
						while (tk.hasMoreTokens()) {
							line = tk.nextToken();
							if (line.contains("-")) {
								temp = line.split("-");
								start = Integer.parseInt(temp[0]);
								end = Integer.parseInt(temp[1]);
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
				updateCount = getJdbcTemplate().update("INSERT INTO stock (code, is_ignored) VALUES (?, FALSE)", code);
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
		rs = getJdbcTemplate().queryForRowSet("SELECT code, id FROM stock ORDER BY code");
		while (rs.next()) {
			refreshLatestPriceAndNameForStock(rs.getString("code"));
		}
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
}