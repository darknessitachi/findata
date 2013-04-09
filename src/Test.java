import michael.findata.external.SecurityTimeSeriesData;
import michael.findata.external.SecurityTimeSeriesDatum;
import michael.findata.external.tdx.TDXPriceHistory;
import michael.findata.model.FinDataUpdates;
import michael.findata.model.Stock;
import michael.findata.util.ResourceUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sound.midi.SysexMessage;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static michael.findata.util.FinDataConstants.*;

public class Test {
	static final Date earliest = new Date(3, 3, 3);
	private static EntityManagerFactory entityManagerFactory;

	public static void main (String [] args) throws ClassNotFoundException, SQLException, InstantiationException, IOException, IllegalAccessException, ParseException {
//		Pattern p2 = Pattern.compile("、\\(([0|1|2|3][\\d]{5})、.*([0|1|2|3][\\d]{5}).*\\).+：([\\d]{4})(.+度).*主要.+");
//		Matcher m = p2.matcher("二十四、(112132、300147) 香雪制药：2012年年度报告主要财务指标及分配预案");
//		System.out.println(m.find());

		Date dt = new Date();
		refreshStockPriceHistories();
		System.out.printf("%d",(new Date().getTime() - dt.getTime()));
	}

	// Bulk-load stock pricing data from THS, make sure THS pricing data is complete before doing this!!!!!
	public static void refreshStockPriceHistories() throws IOException, SQLException, ParseException, ClassNotFoundException, IllegalAccessException, InstantiationException {
		EntityManager em = createEntityManager();
		Connection c = jdbcConnection();
		Date dt = new Date();
		List<Stock> stocks = em.createQuery("SELECT s FROM Stock s ORDER BY s.code").getResultList();
		System.out.println((new Date().getTime() - dt.getTime()));
		int id;
		String code, name;
		for (Stock stock : stocks) {
			id = stock.getId();
			code = stock.getCode();
			name = stock.getName();
			if (name == null) continue;
			System.out.println(code+" "+name);
//			refreshStockPriceHistory(id, code, c, em);
		}
		c.close();
		em.close();
	}

	private static void refreshStockPriceHistory(int stockId, String code, Connection con, EntityManager em) throws IOException, SQLException, ParseException {

//		SecurityTimeSeriesData ts = new THSPriceHistory(code);
		SecurityTimeSeriesData ts = new TDXPriceHistory(code);
		Statement st = con.createStatement();
		PreparedStatement ps = con.prepareStatement("INSERT INTO stock_price (stock_id, date, open, high, low, close, avg, adjustment_factor) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
		Statement s = con.createStatement();
		Date latest = null;
		int currentYear = new Date().getYear()+1900;
		ResultSet rs;

//		em.cr // todo
		rs = s.executeQuery("SELECT max(date) FROM stock_price WHERE stock_id = "+stockId);
		if (rs.next()) {
			latest = rs.getDate(1);
		}

		if (latest == null) {
			latest = earliest;
		}

		System.out.println("Latest: " + yyyyMMdd.format(latest));
		con.setAutoCommit(false);
		em.getTransaction().begin();
		SecurityTimeSeriesDatum temp;
		while (ts.hasNext()) {
			temp = ts.next();

			if (temp.getDate().after(latest)) {
				System.out.println((temp.getDate().getYear()+1900) + " " + (temp.getDate().getMonth() + 1) + " " + temp.getDate().getDate());
			} else {
				break;
			}
			ps.setInt(1, stockId);
			ps.setDate(2, temp.getDate());
			ps.setInt(3, temp.getOpen());
			ps.setInt(4, temp.getHigh());
			ps.setInt(5, temp.getLow());
			ps.setInt(6, temp.getClose());
			ps.setInt(7, (temp.getOpen()+temp.getHigh()+temp.getLow()+temp.getClose())/4);
			ps.setObject(8, null);
//			ps.executeUpdate();
			ps.addBatch();
		}
		try {
			ps.executeBatch();
			System.out.println(code + " updated.");
		}
		catch (BatchUpdateException exx)
		{
			System.out.println(exx.getMessage());
		}
		con.commit();
		em.getTransaction().commit();
		ts.close();
		st.close();
	}

	private static EntityManager createEntityManager() {
		if (entityManagerFactory == null) {
			entityManagerFactory = Persistence.createEntityManagerFactory("sample");
		}
		return entityManagerFactory.createEntityManager();
	}

	private static Connection jdbcConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection con = DriverManager.getConnection(ResourceUtil.getString(JDBC_URL), ResourceUtil.getString(JDBC_USER), ResourceUtil.getString(JDBC_PASS));
		return con;
	}
}