import michael.findata.service.*;
import michael.findata.util.FinDataConstants;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

public class Test {
	public static void main (String [] args)
			throws ClassNotFoundException, SQLException, InstantiationException,
			IOException, IllegalAccessException, ParseException {

		/**
		 * Data integrity check:
		 */

		/**
		 * 1. report_pub_dates latest year/season not in sync with stock latest year/season

		 select rpd.*, s.latest_year, s.latest_season, s.code, s.name
		 from
		   (select max(fin_year*10+fin_season) d, stock_id from report_pub_dates group by stock_id) rpd,
		   stock s
		 where
		   rpd.stock_id = s.id and
		   s.latest_year*10+s.latest_season <> d and not s.is_ignored;

		**/

		/**
		 * 2. Adjustment factor must be calculated once there are split/bonus for a stock, the following will find
		 * gaps in the adjustment factor column

		 select nn.min_not_null, min(p.date) min_null, count(p.date) gap_count, max(p.date) max_null, nn.max_not_null
		 from
		 stock_price p,
		 stock s,
		 (select min(p.date) min_not_null,max(p.date) max_not_null from stock_price p, stock s where p.stock_id = s.id and s.code = '000001' and adjustment_factor is not null) nn
		 where
		 p.stock_id = s.id and s.code = '000001' and adjustment_factor is null and date > nn.min_not_null and date < nn.max_not_null;

		 */

		ApplicationContext context = new ClassPathXmlApplicationContext("/michael/findata/findata_spring.xml");
		ReportPubDateService spds = (ReportPubDateService) context.getBean("reportPubDateService");
		StockPriceService sps = (StockPriceService) context.getBean("stockPriceService");
		StockService ss = (StockService) context.getBean("stockService");
		FinDataService fds = (FinDataService) context.getBean("finDataService");
		DividendService ds = (DividendService) context.getBean("dividendService");
		ShareNumberChangeService sncs = (ShareNumberChangeService) context.getBean("shareNumberChangeService");
		SecurityTimeSeriesDataService stsds = (SecurityTimeSeriesDataService) context.getBean("securityTimeSeriesDataService");

		long stamp = System.currentTimeMillis();
		// The following are used regularly throughout the year
//		ss.refreshStockCodes();
//		sps.refreshStockPriceHistories();
//		ss.refreshLatestPriceAndName();
//		ds.refreshDividendData();
		sncs.refreshNumberOfShares();
//		ss.calculateAdjustmentFactor(10);

		// The following are used mainly during and immediately after earnings report seasons
//		spds.updateFindataWithDates(FinDataConstants.DAYS_REPORT_PUB_DATES);
//		fds.refreshFinData(EnumStyleRefreshFinData.FILL_RECENT_ACCORDING_TO_REPORT_PUBLICATION_DATE, null, false, true);

		// The following are used immediately after earning report seasons, especially when there are missing report pub dates from above
//		spds.fillMissingReportPublicationDatesAccordingToCurrentDate();
//		fds.refreshFinData(EnumStyleRefreshFinData.FILL_RECENT_ACCORDING_TO_REPORT_PUBLICATION_DATE, null, false, true);

		// The following is used to update findata forcefully when report dates of some stocks cannot be obtained from web
//		fds.refreshFinData(EnumStyleRefreshFinData.FiLL_ALL_RECENT, null, false, true);
		// and then update report dates according to findata
//		spds.fillLatestPublicationDateAccordingToLatestFinData();


		// This is used to quickly update publication dates after 2 or more seasons of report publication was missed.
//		spds.scanForPublicationDateGaps(2000, false);

//		spds.updateFindataWithDates(91);
//		spds.updateFindataWithDates(FinDataConstants.DAYS_REPORT_PUB_DATES);
//		fds.refreshFinData(EnumStyleRefreshFinData.FILL_RECENT_ACCORDING_TO_REPORT_PUBLICATION_DATE, null, false, true);
//		fds.refreshFinData(EnumStyleRefreshFinData.FiLL_ALL_RECENT, null, false, true);
//		fds.refreshMissingFinDataAccordingToReportPubDates();



		// Statistics
//		ds.calculateAdjFactorForStock("600875");
//		Arrays.stream(StockGroups.Coal).forEach(code -> ds.calculateAdjFactorForStock(code.symbol().substring(0, 6)));

		System.out.println("Time taken: " + (System.currentTimeMillis() - stamp) / 1000d + " seconds.");
	}
}