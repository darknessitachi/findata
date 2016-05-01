/*
 * Copyright (c) Numerical Method Inc.
 * http://www.numericalmethod.com/
 * 
 * THIS SOFTWARE IS LICENSED, NOT SOLD.
 * 
 * YOU MAY USE THIS SOFTWARE ONLY AS DESCRIBED IN THE LICENSE.
 * IF YOU ARE NOT AWARE OF AND/OR DO NOT AGREE TO THE TERMS OF THE LICENSE,
 * DO NOT USE THIS SOFTWARE.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITH NO WARRANTY WHATSOEVER,
 * EITHER EXPRESS OR IMPLIED, INCLUDING, WITHOUT LIMITATION,
 * ANY WARRANTIES OF ACCURACY, ACCESSIBILITY, COMPLETENESS,
 * FITNESS FOR A PARTICULAR PURPOSE, MERCHANTABILITY, NON-INFRINGEMENT, 
 * TITLE AND USEFULNESS.
 * 
 * IN NO EVENT AND UNDER NO LEGAL THEORY,
 * WHETHER IN ACTION, CONTRACT, NEGLIGENCE, TORT, OR OTHERWISE,
 * SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIMS, DAMAGES OR OTHER LIABILITIES,
 * ARISING AS A RESULT OF USING OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.numericalmethod.algoquant.data.calendar;

import com.numericalmethod.algoquant.execution.datatype.product.fx.Currencies;
import com.numericalmethod.algoquant.execution.datatype.product.stock.*;
import static com.numericalmethod.nmutils.collection.CollectionUtils.*;
import java.util.*;
import org.joda.time.*;

/**
 * Provides holiday calendar using Yahoo! Finance EOD data.
 *
 * @author Ken Yiu
 */
public class HolidayCalendarFromYahoo implements HolidayCalendar {

	/**
	 * Stock data are referenced to determine whether or not a weekday is a holiday. If all
	 * referenced stocks indicate a particular weekday is not a trading day (no quote or zero volume
	 * and OHLC are the same), that weekday is treated as a holiday.
	 *
	 * Note: Occasionally, stock data for some dates on Yahoo! Finance may not be available. Adding
	 * more stocks in the reference list for finding the union of trading dates would improve the
	 * error resilience.
	 */
	public static enum ExchangeHolidayCalendarSettings {

		/**
		 * The settings for {@linkplain Exchange#HKEX}.
		 */
		HKEX(
				Arrays.<Exchange>asList(Exchange.HKEX),
				Arrays.<Stock>asList(
						new SimpleStock("^HSI", Currencies.HKD, Exchange.HKEX)),
				new LocalDate(1990, 1, 1)
		),
		/**
		 * The settings for {@linkplain Exchange#NYSE}, {@linkplain Exchange#NEW_YORK}, and
		 * {@linkplain Exchange#NYSE_ARCA}.
		 */
		NYSE(
				Arrays.<Exchange>asList(Exchange.NYSE, Exchange.NEW_YORK, Exchange.NYSE_ARCA),
				Arrays.<Stock>asList(
						new SimpleStock("^NYA", Currencies.USD, Exchange.NYSE),
						new SimpleStock("IBM", Currencies.USD, Exchange.NYSE)),
				new LocalDate(1980, 1, 1)
		),
		/**
		 * The settings for {@linkplain Exchange#NASDAQ}.
		 */
		NASDAQ(
				Arrays.<Exchange>asList(Exchange.NASDAQ),
				Arrays.<Stock>asList(
						new SimpleStock("^IXIC", Currencies.USD, Exchange.NASDAQ),
						new SimpleStock("AAPL", Currencies.USD, Exchange.NASDAQ)),
				new LocalDate(1984, 2, 3)
		),
		/**
		 * The settings for {@linkplain Exchange#LSE}.
		 */
		LSE(
				Arrays.<Exchange>asList(Exchange.LSE),
				Arrays.<Stock>asList(
						new SimpleStock("^FTSE", Currencies.GBP, Exchange.LSE),
						new SimpleStock("BARC.L", Currencies.GBP, Exchange.LSE)),
				new LocalDate(1999, 1, 4)
		),
		/**
		 * The settings for {@linkplain Exchange#SGX}.
		 */
		SGX(
				Arrays.<Exchange>asList(Exchange.SGX),
				Arrays.<Stock>asList(
						new SimpleStock("^STI", Currencies.SGD, Exchange.SGX), // STRAITS TIMES INDEX
						new SimpleStock("S68.SI", Currencies.SGD, Exchange.SGX), // Singapore Exchange Ltd.
						new SimpleStock("FSTAS.SI", Currencies.SGD, Exchange.SGX)), // FTSE ST All-Share Index
				new LocalDate(1997, 1, 1)
		),
		/**
		 * The settings for {@linkplain Exchange#SHSE}.
		 */
		SHSE(
				Arrays.<Exchange>asList(Exchange.SHSE),
				Arrays.<Stock>asList(
						new SimpleStock("600651.SS", Currencies.CNY, Exchange.SHSE),
						new SimpleStock("600689.SS", Currencies.CNY, Exchange.SHSE),
						new SimpleStock("600654.SS", Currencies.CNY, Exchange.SHSE)
//						new SimpleStock("000001.SS", Currencies.CNY, Exchange.SHSE)
				),
				new LocalDate(1991, 1, 1)
		),
		/**
		 * The settings for {@linkplain Exchange#SZSE}.
		 */
		SZSE(
				Arrays.<Exchange>asList(Exchange.SZSE),
				Arrays.<Stock>asList(
						new SimpleStock("000001.SZ", Currencies.CNY, Exchange.SZSE),
						new SimpleStock("000002.SZ", Currencies.CNY, Exchange.SZSE),
						new SimpleStock("000022.SZ", Currencies.CNY, Exchange.SZSE)
//						new SimpleStock("399106.SZ", Currencies.CNY, Exchange.SZSE)
		),
				new LocalDate(1991, 1, 1)
		);

		private static final Map<Exchange, ExchangeHolidayCalendarSettings> exchange2settings
				= newHashMap();

		static {
			for (ExchangeHolidayCalendarSettings settings : values()) {
				for (Exchange exchange : settings.exchanges()) {
					exchange2settings.put(exchange, settings);
				}
			}
		}

		/**
		 * Looks up the calendar settings of a given exchange.
		 *
		 * @param exchange the exchange
		 * @return the corresponding calendar settings
		 */
		public static ExchangeHolidayCalendarSettings forExchange(Exchange exchange) {
			ExchangeHolidayCalendarSettings settings = exchange2settings.get(exchange);

			if (settings == null) {
				throw new UnsupportedOperationException(String.format(
						"calendar settings for exchange '%s' is not yet supported, "
								+ "please construct the instance via constructor",
						exchange
				));
			}

			return settings;
		}

		private final List<Exchange> exchanges;
		private final List<Stock> stocks;
		private final LocalDate earliestDataDate;

		private ExchangeHolidayCalendarSettings(
				List<Exchange> exchanges,
				List<Stock> stocks,
				LocalDate earliestDataDate
		) {
			this.exchanges = exchanges;
			this.stocks = stocks;
			this.earliestDataDate = earliestDataDate;
		}

		/**
		 * Returns the exchanges that use this settings.
		 *
		 * @return the exchanges that use this settings
		 */
		public List<Exchange> exchanges() {
			return Collections.unmodifiableList(exchanges);
		}

		/**
		 * Returns the stocks used as reference for computing the holidays.
		 *
		 * @return the reference stocks
		 */
		public List<Stock> stocks() {
			return Collections.unmodifiableList(stocks);
		}

		/**
		 * Returns the earliest date in the calendar.
		 *
		 * @return the earliest date in the calendar
		 */
		public LocalDate earliestDataDate() {
			return earliestDataDate;
		}
	}

	/**
	 * Looks up the {@linkplain HolidayCalendarFromYahoo} for a given exchange.
	 *
	 * @param exchange the exchange
	 * @return the corresponding holiday calendar
	 */
	public static HolidayCalendarFromYahoo forExchange(Exchange exchange) {
		ExchangeHolidayCalendarSettings settings
				= ExchangeHolidayCalendarSettings.forExchange(exchange);

		return new HolidayCalendarFromYahoo(
				exchange,
				settings.stocks(),
				settings.earliestDataDate()
		);
	}

	private final HolidayCalendarFromDB calendar;

	/**
	 * Creates an instance for the given exchange, within the given time interval.
	 *
	 * @param exchange        the exchange for which this holiday calendar represents
	 * @param referenceStocks the stocks from whose EOD data the calendar is computed
	 * @param fromDate        the first date of this calendar
	 */
	public HolidayCalendarFromYahoo(
			Exchange exchange,
			List<Stock> referenceStocks,
			LocalDate fromDate
	) {
		this(
				exchange,
				referenceStocks,
				new Interval(
						fromDate.toDateTimeAtStartOfDay(exchange.timeZone()),
						new DateTime(exchange.timeZone()).withTimeAtStartOfDay()
				)
		);
	}

	/**
	 * Creates an instance for the given exchange, within the given time interval.
	 *
	 * @param exchange         the exchange for which this holiday calendar represents
	 * @param referenceStocks  the stocks from whose EOD data the calendar is computed
	 * @param calendarInterval the time interval for this calendar
	 */
	public HolidayCalendarFromYahoo(
			Exchange exchange,
			List<Stock> referenceStocks,
			Interval calendarInterval
	) {
		this.calendar = new HolidayCalendarFromDB(
				exchange,
				new YahooHolidaySource(exchange, referenceStocks),
				calendarInterval
		);
	}

	/**
	 * Creates an instance for the given exchange, within the given time interval.
	 *
	 * @param exchange         the exchange for which this holiday calendar represents
	 * @param referenceStocks  the stocks from whose EOD data the calendar is computed
	 * @param calendarInterval the time interval for this calendar
	 * @param folderName       the name of folder containing the cached holiday data
	 */
	public HolidayCalendarFromYahoo(
			Exchange exchange,
			List<Stock> referenceStocks,
			Interval calendarInterval,
			String folderName
	) {
		this.calendar = new HolidayCalendarFromDB(
				exchange,
				new YahooHolidaySource(exchange, referenceStocks),
				calendarInterval,
				folderName);
	}

	@Override
	public boolean isHoliday(DateTime date) {
		return calendar.isHoliday(date);
	}
}