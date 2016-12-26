package michael.findata.service;

import michael.findata.external.SecurityTimeSeriesData;
import michael.findata.external.SecurityTimeSeriesDatum;
import michael.findata.external.tdx.TDXClient;
import michael.findata.external.tdx.TDXFileBasedPriceHistory;
import michael.findata.model.ExchangeRateDaily;
import michael.findata.spring.data.repository.ExchangeRateDailyRepository;
import michael.findata.util.Consumer2;
import michael.findata.util.FinDataConstants;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static michael.findata.util.FinDataConstants.yyyyDashMMDashdd;
@Service
public class StockPriceService extends JdbcDaoSupport {

	@Autowired
	private ExchangeRateDailyRepository exchangeRateDailyRepo;

	// Bulk-load stock pricing data from THS, make sure THS pricing data is complete before doing this!!!!!
	public void refreshStockPriceHistories() throws IOException, SQLException, ParseException,
			ClassNotFoundException, IllegalAccessException, InstantiationException {
		SqlRowSet rs = getJdbcTemplate().queryForRowSet(
				"SELECT code, id, name FROM stock WHERE ((stock.number_of_shares <> 0 AND latest_year IS NOT NULL AND NOT is_fund) OR (is_fund AND stock.is_interesting)) AND NOT is_ignored ORDER BY code");
		List<String> codes = new ArrayList<>();
		while (rs.next()) {
			codes.add(rs.getString("code"));
		}
		ThreadLocal<TDXClient> threadLocalClient = new ThreadLocal<>();
		codes.stream().forEach(code -> {
			try {
				refreshStockPriceHistory(code, threadLocalClient);
			} catch (IOException | SQLException | ParseException | InterruptedException e) {
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

		public float getInvestment() {
			return number_shares * paid_price * (1 + 0.0008f);
		}

		public float getSellTotal(float selling_price) {
			return (selling_price * number_shares) * (1 - 0.0018f);
		}
	}

	public float getInvestedCash(Collection<Slot> slots) {
		return (float) slots.stream().mapToDouble(Slot::getInvestment).sum();
	}

	public void walk(LocalDate start,
					 LocalDate end,
					 boolean log,
					 Consumer2<DateTime, Map<String, SecurityTimeSeriesDatum>> doStuff,
					 String... codes) {
		String temp = "";
		for (String s : codes) {
			temp += "'" + s + "', ";
		}

		SqlRowSet rs = getJdbcTemplate().queryForRowSet(
				"SELECT date, code, open, high, low, close " +
				"FROM code_price " +
				"WHERE ? <= date AND date <= ? AND code IN ("+temp+"'') ORDER BY date, code ASC"
				, start.toDate(), end.toDate());
		DateTime date = null;
		DateTime oldDate;

		// exchange rates
		Timestamp tsStart = new Timestamp(start.toDateTimeAtStartOfDay().getMillis());
		Timestamp tsEnd = new Timestamp(end.toDateTimeAtStartOfDay().getMillis());
		Map<Timestamp, ExchangeRateDaily> forexHKD = new HashMap<>();
		Map<Timestamp, ExchangeRateDaily> forexUSD = new HashMap<>();
		exchangeRateDailyRepo.findByDateBetweenAndCurrency(tsStart, tsEnd, "HKD").forEach(forex -> {
			forexHKD.put(forex.date(), forex);
		});
		exchangeRateDailyRepo.findByDateBetweenAndCurrency(tsStart, tsEnd, "USD").forEach(forex -> {
			forexUSD.put(forex.date(), forex);
		});

		HashMap<String, SecurityTimeSeriesDatum> datumMap = new HashMap<>();
		while (rs.next()) {
			oldDate = date;
			date = new DateTime(rs.getDate("date"));
			if (oldDate != null && !oldDate.equals(date)) {
				// date changed ...

				// fill in dummy data for missing stocks
				Set<String> codesIn = datumMap.keySet();
				for (String code : codes) {
					if (!codesIn.contains(code)) {
						datumMap.put(code, new SecurityTimeSeriesDatum(oldDate));
					}
				}
				ExchangeRateDaily hkdRate = forexHKD.get(new Timestamp(oldDate.toDate().getTime()));
				ExchangeRateDaily usdRate = forexUSD.get(new Timestamp(oldDate.toDate().getTime()));
				// exchange rate
				if (hkdRate != null) {
					datumMap.put("HKD", new SecurityTimeSeriesDatum(oldDate, 0, 0, 0, 0, 0, (float)hkdRate.close(), false));
				}
				if (usdRate != null) {
					datumMap.put("USD", new SecurityTimeSeriesDatum(oldDate, 0, 0, 0, 0, 0, (float)usdRate.close(), false));
				}


				doStuff.apply(oldDate, datumMap);
				if (log) {
					Arrays.stream(codes).forEach(code -> System.out.println(code + "\t" + datumMap.get(code)));
				}
				datumMap.clear();
			}
			datumMap.put(rs.getString("code"),
					new SecurityTimeSeriesDatum(
					date,
					rs.getInt("open"),
					rs.getInt("high"),
					rs.getInt("low"),
					rs.getInt("close"),
					999999, 999999));
		}
		// last day
		if (date != null) {
			// fill in dummy data for missing stocks
			Set<String> codesIn = datumMap.keySet();
			for (String code : codes) {
				if (!codesIn.contains(code)) {
					datumMap.put(code, new SecurityTimeSeriesDatum(date));
				}
			}
			ExchangeRateDaily hkdRate = forexHKD.get(new Timestamp(date.toDate().getTime()));
			ExchangeRateDaily usdRate = forexUSD.get(new Timestamp(date.toDate().getTime()));
			// exchange rate
			if (hkdRate != null) {
				datumMap.put("HKD", new SecurityTimeSeriesDatum(date, 0, 0, 0, 0, 0, (float)hkdRate.close(), false));
			}
			if (usdRate != null) {
				datumMap.put("USD", new SecurityTimeSeriesDatum(date, 0, 0, 0, 0, 0, (float)usdRate.close(), false));
			}
			doStuff.apply(date, datumMap);
			if (log) {
				Arrays.stream(codes).forEach(code -> System.out.println(code + "\t" + datumMap.get(code)));
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
			serial++;
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
				investment += getInvestedCash(pList) * ((currentDate.getTime() - lastDate.getTime()) / 1000 / 60 / 60 / 24);
			}
			lastDate = currentDate;

			// First find out what slots can be bought
			int i = 0;
			for (; i < pList.size(); i++) {
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
			for (i = pList.size() - 1; i > -1; i--) {
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
				position.number_shares = (int) (Math.ceil(baseUnitAmount / position.paid_price / 100) * 100);
				position.buying_date = currentDate;
				System.out.println(currentDate + "\tBuying No." + position.serial + ": " + position.paid_price + "\t" + position.number_shares);
			});
			if (positionsToBuy.size() != 0) {
				System.out.println("Total Investment: " + getInvestedCash(pList));
			}
			// Sell
			positionsToSell.stream().forEach(position -> {
				float selling_price = low < position.sell_floor ? position.sell_floor : low;
				float profit = position.getSellTotal(selling_price) - position.getInvestment();
				if (position.paid_price == -1f) {
					System.out.println("error");
				}
				System.out.println(currentDate + "\tSelling No." + position.serial + ": " + selling_price + "\t" + position.paid_price + "\t" + position.number_shares + "\t" + profit);
				cashProfit[0] += profit;
				position.buying_date = null;
				position.paid_price = -1f;
				position.number_shares = 0;
			});
			if (positionsToSell.size() != 0) {
				System.out.println("Total Profit: " + cashProfit[0]);
			}
		}
		long period = (end.getTime() - start.getTime()) / 1000 / 60 / 60 / 24;
		float averageInv = investment / period;
		float profitR = (cashProfit[0] + averageInv) / averageInv;
//		float anualizedProfR = profitR ^ (365f/period);
		System.out.println("Period: " + period + " days (" + (period / 365f) + " years)");
		System.out.println("Time*Investment: " + investment);
		System.out.println("Average investment: " + averageInv);
		System.out.println("Profit rate: " + profitR);
		System.out.println("Annualized: " + Math.pow(profitR, 365f / period));
	}

	private int tdxclientcount = -1;
	private synchronized String getNextTDXClientConfig () {
		tdxclientcount ++;
		System.out.println("Got config: "+TDXClient.TDXClientConfigs[tdxclientcount]);
		return TDXClient.TDXClientConfigs[tdxclientcount];
	}

	@Transactional
	public void refreshStockPriceHistory(String code, ThreadLocal<TDXClient> threadLocalClient) throws IOException, SQLException, ParseException, InterruptedException {
		DateTime latest = null;
		int stockId;
		String name;

		SqlRowSet rs;
		try {
			rs = getJdbcTemplate().queryForRowSet("SELECT max(sp.date) date, s.name name, s.id stock_id FROM stock s LEFT OUTER JOIN stock_price sp ON sp.stock_id = s.id WHERE s.code = ?", code);
		} catch (Exception e) {
			System.out.println("Cannot find max pricing date for stock: " + code);
			e.printStackTrace();
			return;
		}
		Date d;
		if (rs.next()) {
			d = rs.getDate("date");
			if (d == null) {
				latest = null;
			} else {
				latest = new DateTime(rs.getDate("date"));
			}
			stockId = rs.getInt("stock_id");
			name = rs.getString("name");
		} else {
			System.out.println("Stock " + code + " cannot be found.");
			return;
		}

		if (latest == null) {
			latest = new DateTime(FinDataConstants.EARLIEST);
		}
		DateTimeFormatter format = DateTimeFormat.forPattern(yyyyDashMMDashdd);
		System.out.println(code + " Latest price: " + format.print(latest));
//		System.err.println((fc.size()-headerSize)/recordSize);
		if (threadLocalClient.get() == null) {
			TDXClient client = new TDXClient(getNextTDXClientConfig());
			client.connect();
			threadLocalClient.set(client);
		}
		TDXClient client = threadLocalClient.get();

//		SecurityTimeSeriesData ts = new THSPriceHistory(code);
//		SecurityTimeSeriesData ts = new TDXFileBasedPriceHistory(code);
		SecurityTimeSeriesData ts = client.getEODs(code, latest);

		SecurityTimeSeriesDatum temp;
		while (ts.hasNext()) {
			temp = ts.popNext();

			if (temp.getVolume() == 0){
				System.out.println("Skipped: "+code+" "+temp.getDateTime());
				continue;
			}

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
		System.out.println(code + " " + name + " 's daily quote updated.");
		ts.close();
		Thread.sleep(20l);
	}

	public static void main(String[] args) throws ParseException {
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
		System.out.println("Time taken: " + (System.currentTimeMillis() - stamp) / 1000 + " seconds.");
	}
}