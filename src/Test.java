import michael.findata.service.*;
import michael.findata.external.SecurityTimeSeriesData;
import michael.findata.external.SecurityTimeSeriesDatum;
import michael.findata.external.tdx.TDXPriceHistory;
import michael.findata.model.Stock;
import michael.findata.util.FinDataConstants;
import michael.findata.util.ResourceUtil;
import org.cyberneko.html.parsers.DOMParser;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.DOMReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.xml.sax.SAXException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
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
//		Pattern p2 = Pattern.compile("target=\"new\">.*([\\d,O]{4})\\s*年?(.*)报告(摘要|正文|全文)?(（更新后）|（已取消）|\\(修订版\\))?<.*");
//		Matcher m = p2.matcher("target=\"new\">路翔股份：2012年年半年度报告</a>&nbsp;&nbsp;&nbsp;<img width=\"19\" height=\"21\" border=\"0\"  src=\"images2008/pdf.jpg\"  onerror=\"this.src='images2008/otherfile.jpg'\"/>");
//		System.out.println(m.find());
//		System.out.println(m.group());
//		System.out.println(m.find());
//		System.out.println(m.group());
//		System.out.println(m.find());
//		System.out.println(m.group());

//		Date dt = new Date();
//		refreshStockPriceHistories();
//		System.out.printf("%d",(new Date().getTime() - dt.getTime()));

		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/findata_spring.xml");
		ReportPubDateService spds = (ReportPubDateService) context.getBean("reportPubDateService");
		StockPriceService sps = (StockPriceService) context.getBean("stockPriceService");
		StockService ss = (StockService) context.getBean("stockService");
		FinDataService fds = (FinDataService) context.getBean("finDataService");
		DividendService ds = (DividendService) context.getBean("dividendService");
		ShareNumberChangeService sncs = (ShareNumberChangeService) context.getBean("shareNumberChangeService");

		long stamp = System.currentTimeMillis();
		// The following are used regularly throughout the year
//		ss.refreshStockCodes();
//		sps.refreshStockPriceHistories();
		ss.refreshLatestPriceAndName();
//		sncs.refreshNumberOfShares();
//		ds.refreshDividendData();

		// This is used to quickly update publication dates after 2 or more seasons of report publication was missed.
//		spds.scanForMissingPublicationDates(2000, false);

		// The following are used mainly during and immediately after earnings report seasons
		spds.updateFindataWithDates(FinDataConstants.DAYS_REPORT_PUB_DATES);
//		fds.refreshFinData(EnumStyleRefreshFinData.FILL_RECENT_ACCORDING_TO_REPORT_PUBLICATION_DATE, null, false);
//		fds.refreshFinData(EnumStyleRefreshFinData.FiLL_ALL_RECENT, null, false, false);
//		fds.refreshMissingFinDataAccordingToReportPubDates();
		System.out.println("Time taken: "+(System.currentTimeMillis() - stamp)/1000);
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

		System.out.println("Latest: " + FORMAT_yyyyMMdd.format(latest));
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
}