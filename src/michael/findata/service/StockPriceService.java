package michael.findata.service;

import michael.findata.external.SecurityTimeSeriesData;
import michael.findata.external.SecurityTimeSeriesDatum;
import michael.findata.external.tdx.TDXPriceHistory;
import michael.findata.model.AdjFactor;
import michael.findata.util.CalendarUtil;
import michael.findata.util.Consumer5;
import michael.findata.util.FinDataConstants;
import org.joda.time.DateTime;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import static michael.findata.util.FinDataConstants.yyyyDashMMDashdd;

public class StockPriceService extends JdbcDaoSupport {
	// Bulk-load stock pricing data from THS, make sure THS pricing data is complete before doing this!!!!!
	public void refreshStockPriceHistories() throws IOException, SQLException, ParseException, ClassNotFoundException, IllegalAccessException, InstantiationException {
		SqlRowSet rs = getJdbcTemplate().queryForRowSet("SELECT code, id, name FROM stock WHERE NOT is_ignored ORDER BY code");
		List<String> codes = new ArrayList<>();
		while (rs.next()) {
			codes.add(rs.getString("code"));
		}
		codes.parallelStream().forEach(code -> {
			try {
				refreshStockPriceHistory(code);
			} catch (IOException | SQLException | ParseException e) {
				e.printStackTrace();
			}
		});
	}

	private class Slot {
		public float buy_ceiling;
		public float sell_floor;
		public int serial;
		public float paid_price;
		public int number_shares = 0;
		public Date buying_date;

		Slot(float buy_ceiling, float sell_floor, float paid_price, int number_shares, int serial, Date buying_date) {
			this.buy_ceiling = buy_ceiling;
			this.sell_floor = sell_floor;
			this.paid_price = paid_price;
			this.number_shares = number_shares;
			this.serial = serial;
			this.buying_date = buying_date;
		}

		public float getInvestment(){
			return number_shares*paid_price*(1+0.0008f);
		}

		public float getSellTotal(float selling_price) {
			return (selling_price*number_shares)*(1-0.0018f);
		}
	}

	public float getInvestedCash(Collection<Slot> slots) {
		return (float) slots.stream().mapToDouble(Slot::getInvestment).sum();
	}

//	public void cointegrationTestOHLC(String start, String end, String codeA, String codeB) {
//		Stack<AdjFactor> divA = new Stack<>();
//		Stack<AdjFactor> divB = new Stack<>();
//		getAdjFactors(start, end, codeA, codeB, divA, divB);
//		walk(start, end, codeA, codeB, (date, closeA, closeB) -> {
//			while ((!divA.isEmpty()) && divA.peek().paymentDate.compareTo(date) <= 0) {
//				divA.pop();
//			}
//			while ((!divB.isEmpty()) && divB.peek().paymentDate.compareTo(date) <= 0) {
//				divB.pop();
//			}
//			double adjFctA = divA.empty()? 1.0d : divA.peek().factor;
//			double adjFctB = divB.empty()? 1.0d : divB.peek().factor;
//			System.out.println(date+"\t"+closeA/adjFctA+"\t"+closeB/adjFctB);
//		});
//
//		double [] statistics = stats.stream().mapToDouble(Double::doubleValue).toArray();
//		AugmentedDickeyFuller adfTest = new AugmentedDickeyFuller(statistics);
//		System.out.println("ADF p value: "+adfTest.pValue());
//	}

	private static class Tuple {
		DateTime date;
		Double prA;
		Double prB;

		Tuple (DateTime d, Double pA, Double pB) {
			date = d;
			prA = pA;
			prB = pB;
		}

		Tuple (Date d, Double pA, Double pB) {
			date = new DateTime(d);
			prA = pA;
			prB = pB;
		}
	}

	public void walk(DateTime start,
					 DateTime end,
					 int maxSteps,
					 String codeA,
					 String codeB,
					 boolean log,
					 Consumer5<DateTime, Double, Double, Float, Float> doStuff) {

		Stack<AdjFactor> adjFctA = new Stack<>();
		Stack<AdjFactor> adjFctB = new Stack<>();
		DividendService.getAdjFactors(start, end, codeA, codeB, adjFctA, adjFctB, getJdbcTemplate());

		SqlRowSet rs = getJdbcTemplate().queryForRowSet(
			"SELECT stockA.date date, stockA.close closeA, stockB.close closeB "+
			"FROM code_price stockA INNER JOIN code_price stockB ON stockA.date = stockB.date " +
			"WHERE stockA.code = ? AND stockB.code = ? AND ? <= stockA.date AND stockA.date <= ? ORDER BY stockA.date DESC LIMIT ?"
			, codeA, codeB, start.toLocalDate().toDate(), end.toLocalDate().toDate(), maxSteps);
		Stack<Tuple> temp = new Stack<>();
		while (rs.next()) {
			temp.push(new Tuple(rs.getDate("date"), rs.getInt("closeA")/1000d, rs.getInt("closeB")/1000d));
		}
		Tuple t;
		DateTime date;
		while (!temp.isEmpty()) {
			t = temp.pop();
			date = t.date;
			while ((!adjFctA.isEmpty()) && CalendarUtil.daysBetween(adjFctA.peek().paymentDate, date) >= 0) {
				adjFctA.pop();
			}
			while ((!adjFctB.isEmpty()) && CalendarUtil.daysBetween(adjFctB.peek().paymentDate, date) >= 0) {
				adjFctB.pop();
			}
			double prA = t.prA / (adjFctA.empty() ? 1.0d : adjFctA.peek().factor);
			double prB = t.prB / (adjFctB.empty() ? 1.0d : adjFctB.peek().factor);

			doStuff.apply(date, prA, prB, 100000f, 100000f);
			if (log) {
				System.out.println(date + "\t" + "\t" + prA + "\t" + "\t" + prB);
			}
		}
	}

	public void stockPriceHistoryWalker(String code, Date start, Date end, float historyMax, float deltaPctg) {
		float p = historyMax;
		ArrayList<Slot> pList = new ArrayList<>();
		final float[] cashProfit = {0f};
		float investment = 0f;
		Date lastDate = null;
		int serial = 1;
		while (p > 1f) {
			pList.add(new Slot(Math.round(p / deltaPctg * 100) / 100f, Math.round(p * 100) / 100f, -1f, 0, serial, null));
			System.out.print("No." + pList.get(pList.size() - 1).serial);
			System.out.print("\tS.Floor: " + pList.get(pList.size() - 1).sell_floor);
			System.out.println("\tB.Ceiling: " + pList.get(pList.size() - 1).buy_ceiling);
			p = p / deltaPctg;
			serial ++;
		}
		float baseUnitAmount = 10000f;

		SqlRowSet rs = getJdbcTemplate().queryForRowSet(
			"SELECT * " +
			"FROM stock s LEFT OUTER JOIN stock_price sp ON sp.stock_id = s.id " +
			"WHERE s.code = ? AND sp.date >= ? AND sp.date <= ?", code, start, end);

		ArrayList<Slot> positionsToBuy = new ArrayList<>();
		ArrayList<Slot> positionsToSell = new ArrayList<>();

		while (rs.next()) {
			positionsToBuy.clear();
			positionsToSell.clear();
			float low = rs.getFloat("low") / 1000f;
			float high = rs.getFloat("high") / 1000f;
//			System.out.print(rs.getString("code"));
//			System.out.print("\t" + rs.getDate("date"));
//			System.out.print("\t" + high);
//			System.out.println("\t" + low);
			Slot pTemp;

			// Calculate money * time cost first;
			Date currentDate = rs.getDate("date");
			if (lastDate == null) {
				investment += 0f;
			} else {
				investment += getInvestedCash(pList)*((currentDate.getTime()-lastDate.getTime())/1000/60/60/24);
			}
			lastDate = currentDate;

			// First find out what slots can be bought
			int i = 0;
			for (; i < pList.size(); i ++) {
				pTemp = pList.get(i);
				if (pTemp.buy_ceiling >= low) {
					if (pTemp.number_shares <= 0) {
						positionsToBuy.add(pTemp);
					}
				} else {
					break;
				}
			}
//			float buying_price = (i == 0) ? low : (high > pList.get(i-1).buy_ceiling ? pList.get(i-1).buy_ceiling : high);
//			float buying_price = (high < pList.get(i-1).buy_ceiling ? pList.get(i-1).buy_ceiling : high);
			// Find out what slots can be sold
			for (i = pList.size() - 1; i > -1; i --) {
				pTemp = pList.get(i);
				if (pTemp.sell_floor <= high) {
					if (pTemp.number_shares > 0) {
						positionsToSell.add(pTemp);
					}
				} else {
					break;
				}
			}

			// Buy
			positionsToBuy.stream().forEach(position -> {
				position.paid_price = high > position.buy_ceiling ? position.buy_ceiling : high;
				position.number_shares = (int) (Math.ceil(baseUnitAmount / position.paid_price / 100)*100);
				position.buying_date = currentDate;
				System.out.println(currentDate + "\tBuying No."+position.serial+": " + position.paid_price + "\t" + position.number_shares);
			});
			if (positionsToBuy.size() != 0) {
				System.out.println("Total Investment: "+ getInvestedCash(pList));
			}
			// Sell
			positionsToSell.stream().forEach(position -> {
				float selling_price = low < position.sell_floor ? position.sell_floor : low;
				float profit = position.getSellTotal(selling_price) - position.getInvestment();
				if (position.paid_price == -1f) {
					System.out.println("error");
				}
				System.out.println(currentDate+"\tSelling No." + position.serial + ": " + selling_price + "\t" + position.paid_price + "\t" + position.number_shares + "\t" + profit);
				cashProfit[0] += profit;
				position.buying_date = null;
				position.paid_price = -1f;
				position.number_shares = 0;
			});
			if (positionsToSell.size() != 0) {
				System.out.println("Total Profit: "+ cashProfit[0]);
			}
		}
		long period = (end.getTime()-start.getTime())/1000/60/60/24;
		float averageInv = investment/period;
		float profitR = (cashProfit[0] + averageInv)/averageInv;
//		float anualizedProfR = profitR ^ (365f/period);
		System.out.println("Period: " + period +" days ("+(period/365f)+" years)");
		System.out.println("Time*Investment: "+investment);
		System.out.println("Average investment: "+averageInv);
		System.out.println("Profit rate: "+profitR);
		System.out.println("Annualized: "+Math.pow(profitR, 365f/period));
	}

	@Transactional
	public void refreshStockPriceHistory(String code) throws IOException, SQLException, ParseException {
//		SecurityTimeSeriesData ts = new THSPriceHistory(code);
		SecurityTimeSeriesData ts = new TDXPriceHistory(code);
		DateTime latest = null;
		int stockId;
		String name;

		SqlRowSet rs;
		try {
			rs = getJdbcTemplate().queryForRowSet("SELECT max(sp.date) date, s.name name, s.id stock_id FROM stock s LEFT OUTER JOIN stock_price sp ON sp.stock_id = s.id WHERE s.code = ?", code);
		} catch (Exception e) {
			System.out.println("Cannot find max pricing date for stock: "+code);
			e.printStackTrace();
			return;
		}
		if (rs.next()) {
			latest = new DateTime(rs.getDate("date"));
			stockId = rs.getInt("stock_id");
			name = rs.getString("name");
		} else {
			System.out.println("Stock "+ code + " cannot be found.");
			return;
		}

		if (latest == null) {
			latest = new DateTime(FinDataConstants.EARLIEST);
		}

		System.out.println(code+" Latest price: " + new SimpleDateFormat(yyyyDashMMDashdd).format(latest));
//		System.err.println((fc.size()-headerSize)/recordSize);
		SecurityTimeSeriesDatum temp;
		while (ts.hasNext()) {
			temp = ts.next();

			if (temp.getDateTime().isAfter(latest)) {
				System.out.println((temp.getDateTime().getYear()) + "-" + (temp.getDateTime().getMonthOfYear()) + "-" + temp.getDateTime().getDayOfMonth());
			} else {
				break;
			}

			try {
				getJdbcTemplate().update("INSERT INTO stock_price (stock_id, date, open, high, low, close, avg, adjustment_factor) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
						stockId, temp.getDateTime().toLocalDate().toDate(), temp.getOpen(), temp.getHigh(), temp.getLow(), temp.getClose(),
						(temp.getOpen() + temp.getHigh() + temp.getLow() + temp.getClose()) / 4, null);
			} catch (DuplicateKeyException ex) {
				System.err.println(ex.getMessage());
				break;
			}
		}
		System.out.println(code + " "+ name + " 's daily quote updated.");
		ts.close();
	}

	public static void main (String [] args) throws ParseException {
		SimpleDateFormat FORMAT_yyyyDashMMDashdd = new SimpleDateFormat(FinDataConstants.yyyyDashMMDashdd);
		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/findata_spring.xml");
		StockPriceService sps = (StockPriceService) context.getBean("stockPriceService");
		long stamp = System.currentTimeMillis();
		sps.stockPriceHistoryWalker(
				args[0],
				FORMAT_yyyyDashMMDashdd.parse(args[1]),
				FORMAT_yyyyDashMMDashdd.parse(args[2]),
				Float.parseFloat(args[3]),
				Float.parseFloat(args[4]));
		System.out.println("Time taken: " + (System.currentTimeMillis() - stamp) / 1000+" seconds.");
	}
}