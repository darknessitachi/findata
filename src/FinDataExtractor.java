import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import michael.findata.external.FinancialSheet;
import michael.findata.external.hexun2008.Hexun2008FinancialSheet;
import michael.findata.external.hexun2008.Hexun2008ShareNumberDatum;
import michael.findata.external.netease.NeteaseTradingDatum;
import michael.findata.util.FinDataConstants;
import michael.findata.util.ResourceUtil;
import org.cyberneko.html.parsers.DOMParser;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.DOMReader;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

enum CodeListFileType {
	THS, // Tong Hua Shun
	TDX  // Tong Da Xin
}

public class FinDataExtractor {
	public static void main(String args[]) throws IOException, SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException, URISyntaxException, DocumentException, SAXException, ParseException {
//		Connection con = jdbcConnection();
//		Statement st = con.createStatement();
//		ResultSet rs = st.executeQuery("select name from stock where id < 10000");
//		while (rs.next()) {
//			System.out.println (rs.getString(1));
//		}
//		refreshStockCode(false);
//		DecimalFormat normalDecimalFormat = new DecimalFormat("###,###.00");
//		Number n = normalDecimalFormat.parse("19,597,000,000.00");
//		System.out.println ();
//		refreshStockCode(false);
		updateFinData();
	}

	public static void test() throws IOException, DocumentException, SAXException, SQLException {
		DOMParser parser = new DOMParser();
		parser.parse("http://stockdata.stock.hexun.com/2008/lr.aspx?stockid=600016&accountdate=2005.12.31");
		DOMReader domReader = new DOMReader();
		Document doc = domReader.read(parser.getDocument());
		XPath tablePath = doc.createXPath("//DIV[@id=\"zaiyaocontent\"]//TBODY/TR[position()>1 and position()<last()]");
		XPath fieldNamePath = doc.createXPath(".//STRONG[1]");
		XPath fieldValuePath = doc.createXPath("./TD[2]/DIV");
		Element nameElement, valueElement;
		Object o = tablePath.evaluate(doc);

//		Connection con = DriverManager.getConnection("jdbc.mysql://localhost/findata", "root", "108129");
//		con.setAutoCommit(false);
//		Statement stmt = con.createStatement();
//		stmt.executeQuery("SELECT id, name FROM source ORDER BY id");
//		stmt.executeQuery("SELECT code FROM stock ORDER BY id");
//		stmt.executeQuery("SELECT name FROM fin_period ORDER BY id");
//		stmt.executeQuery("SELECT id, name FROM fin_sheet ORDER BY id");

		for (Element e : (List<Element>) o) {
			nameElement = (Element) fieldNamePath.evaluate(e);
			valueElement = (Element) fieldValuePath.evaluate(e);
			System.out.print(nameElement.getText());
			System.out.print(" ");
			System.out.println(valueElement.getText());
		}
	}

	// Refresh Stock Code every 2 months

	/**
	 * Refresh stock code table from stock data software (Tong Da Xin or Tong Hua Shun)
	 * @param codeOnly true: only refresh stock codes
	 * 				   false: refresh stock code and get latest price and current stock name
	 * @throws IOException
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static void refreshStockCode(boolean codeOnly) throws IOException, SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(ResourceUtil.getString(FinDataConstants.STOCK_LIST_FILE))));
		Connection con = jdbcConnection();
		String line;
		int updateCount;
		CodeListFileType fileType;
		ArrayList<String> codeList = new ArrayList<String>();

		// Determining the type of stock list file
		br.mark(100);
		br.readLine();
		if (br.readLine().contains("[Market]")) {
			fileType = CodeListFileType.THS;
		} else {
			fileType = CodeListFileType.TDX;
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
									codeList.add(dm.format(i));
									System.out.println("Added " + dm.format(i) + " to list");
								}
							} else {
								codeList.add(line);
								System.out.println("Added " + line + " to list");
							}
						}
					}
				}
				break;
			case TDX:
				// Getting codes from TDX
				while ((line = br.readLine()) != null) {
					if (line.length() < 6 || line.startsWith("1") || line.startsWith("5")) continue;
					codeList.add(line.substring(0, 6));
				}
		}

		PreparedStatement pInsertStock = con.prepareStatement("INSERT INTO stock (code) VALUES (?)");
		PreparedStatement pUpdateStock = con.prepareStatement("UPDATE stock SET name=?, current_price=? , number_of_shares=? WHERE code=?");
//		PreparedStatement pInsertPrice = con.prepareStatement ("INSERT INTO stock_price (stock_id, price_date, price) VALUES (?, ?, ?) ");
		con.setAutoCommit(false);
		for (String code : codeList) {
			updateCount = 0;
			System.out.print(code + "...");
			pInsertStock.setString(1, code);
			try {
				updateCount = pInsertStock.executeUpdate();
			} catch (MySQLIntegrityConstraintViolationException e) {
				if (!e.getMessage().contains("Duplicate")) {
					throw e;
				} else {
					System.out.println(" already exists.");
				}
			}
			if (updateCount == 0 || updateCount == 1) {
				if (updateCount == 1) {
					System.out.print(" inserted.");
				} else {
					// Some notification, telling user that record already exists
				}
				if (!codeOnly) {
					NeteaseTradingDatum td = new NeteaseTradingDatum(code);
					Hexun2008ShareNumberDatum snd = new Hexun2008ShareNumberDatum(code);
					pUpdateStock.setString(1, td.getStockName());
					pUpdateStock.setObject(2, td.getCurrent());
					pUpdateStock.setObject(3, snd.getValue());
					pUpdateStock.setString(4, code);
					updateCount = pUpdateStock.executeUpdate();
					if (updateCount != 1) {
						System.out.print("\tunable to update name and price. " + updateCount + " rows updated.");
					} else {
						System.out.print("\tname, price and total number of shares updated.");
					}
				}
				System.out.println();
			} else {
				System.out.println(" unexpected update count: " + updateCount + " has been produced.");
			}
			con.commit();
		}
		con.close();
	}

	public static void updateFinData() throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException {
		Connection con = jdbcConnection();
		String code;
		java.util.Date cDate = new java.util.Date();
		int id, currentYear = cDate.getYear() + 1900, currentMonth = cDate.getMonth() + 1;
		PreparedStatement pInsertFinData, pUpdateStock = con.prepareStatement("UPDATE stock SET latest_year=?, latest_season=? WHERE id=?");
		con.setAutoCommit(false);
		Statement sStock = con.createStatement();
		Statement sCreateFinData = con.createStatement();
		sStock.execute("UPDATE stock SET latest_year = 1999, latest_season = 0 WHERE latest_year IS NULL");
		sStock.execute("UPDATE stock SET latest_season = 0 WHERE latest_season IS NULL");
		con.commit();
		ResultSet rs = sStock.executeQuery("SELECT id, code, latest_year, latest_season FROM stock ORDER BY id DESC");
		FinancialSheet sheet;
		int aYear, order, latestYear, latestSeason, cYear = -1;
		short seasons[] = new short[]{4, 1, 2, 3}, cSeason = -1;
		boolean someSheetsAreEmpty;
		Iterator<String> it;
		String tableName;
		while (rs.next()) {
			id = rs.getInt(1);
			code = rs.getString(2);
			latestYear = rs.getInt(3);
			latestSeason = rs.getInt(4);
			tableName = "fin_data_" + code;
			System.out.println(code);
			// Create table just in case
			sCreateFinData.execute(
					"CREATE TABLE IF NOT EXISTS " + tableName + " (\n" +
							"\tid INT AUTO_INCREMENT,\n" +
							"\tstock_id INT,\n" +
							"\tfin_year INT,\n" +
							"\tfin_season INT(1),\n" +
							"\tfin_sheet VARCHAR(255),\n" +
							"\tsource VARCHAR(255),\n" +
							"\tname VARCHAR(255),\n" +
							"\tvalue DOUBLE,\n" +
							"\torder_ INT,\n" +
							"\tPRIMARY KEY (id),\n" +
							"\tFOREIGN KEY (stock_id) REFERENCES stock(id),\n" +
							"\tUNIQUE (stock_id, fin_year, fin_season, source, fin_sheet, name)\n" +
							")"
			);

			// delete invalid rows from the table
			System.out.println("delete all data later than " + latestYear + "-" + latestSeason);
			sCreateFinData.execute("DELETE FROM " + tableName + " WHERE fin_year*10 + fin_season > " + (latestYear * 10 + latestSeason));

//			switch (latestSeason) {
//				case first :
//					System.out.println ("delete "+latestYear+": 2, 3, 4");
//					sCreateFinData.execute("DELETE FROM "+tableName+" WHERE fin_year = "+latestYear+" AND fin_year IN ('second', 'third', 'fourth')");
//					break;
//				case second :
//					System.out.println ("delete "+latestYear+": 3, 4");
//					sCreateFinData.execute("DELETE FROM "+tableName+" WHERE fin_year = "+latestYear+" AND fin_year IN ('third', 'fourth')");
//					break;
//				case third :
//					System.out.println ("delete "+latestYear+": 4");
//					sCreateFinData.execute("DELETE FROM "+tableName+" WHERE fin_year = "+latestYear+" AND fin_year IN ('fourth')");
//			}

			// update data
			pInsertFinData = con.prepareStatement("INSERT INTO " + tableName + " (stock_id, fin_year, fin_season, fin_sheet, source, name, value, order_) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			cYear = -1;
			cSeason = -1;
			for (aYear = latestYear; aYear <= currentYear; aYear++) {
				for (short aSeason : seasons) {
					if (aYear == latestYear && (aSeason <= latestSeason)) {
						continue; // We already have this
					}
					if (aYear == currentYear && (currentMonth < aSeason * 3)) {
						continue; // Season hasn't finished yet, no need to try to get reports not existing
					}
					someSheetsAreEmpty = false;
					for (String sheetName : Hexun2008FinancialSheet.FINANCIAL_SHEETNAMES) {
						System.out.println(code + "\t" + aYear + "\t" + aSeason + "\t" + sheetName);
						sheet = new Hexun2008FinancialSheet(code, sheetName, aYear, aSeason);
						it = sheet.getDatumNames();
						String name;
						order = 0;
						while (it.hasNext()) {
							name = it.next();
							pInsertFinData.setInt(1, id); // stock_id
							pInsertFinData.setInt(2, aYear); // accounting year
							pInsertFinData.setShort(3, aSeason); // accounting year
							pInsertFinData.setString(4, sheetName); // financial report name
							pInsertFinData.setString(5, sheet.getClass().getCanonicalName());
							pInsertFinData.setString(6, name);
							pInsertFinData.setObject(7, sheet.getValue(name));
							pInsertFinData.setInt(8, order);
							pInsertFinData.executeUpdate();
							order++;
						}
						someSheetsAreEmpty = someSheetsAreEmpty || (order == 0 && (sheetName == FinDataConstants.FINANCIAL_SHEET_BALANCE_SHEET || sheetName == FinDataConstants.FINANCIAL_SHEET_CASH_FLOW || sheetName == FinDataConstants.FINANCIAL_SHEET_PROFIT_AND_LOSS));
					}
					if (!someSheetsAreEmpty) {
						cYear = aYear;
						cSeason = aSeason;
					}
					if (aSeason == 4 && !someSheetsAreEmpty) {
						// Annual report fully downloaded, we don't need the other seasonal reports.
						break;
					}
				}
			}
			if (cYear != -1 && cSeason != -1) {
				pUpdateStock.setInt(1, cYear);
				pUpdateStock.setShort(2, cSeason);
				pUpdateStock.setInt(3, id);
				pUpdateStock.executeUpdate();
			}
			// set latest_year and latest_season
			con.commit();
		}
	}

	private static Connection jdbcConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection con = DriverManager.getConnection(ResourceUtil.getString(FinDataConstants.JDBC_URL), ResourceUtil.getString(FinDataConstants.JDBC_USER), ResourceUtil.getString(FinDataConstants.JDBC_PASS));
		return con;
	}
}
