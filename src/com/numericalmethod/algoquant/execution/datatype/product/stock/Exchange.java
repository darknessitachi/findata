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
package com.numericalmethod.algoquant.execution.datatype.product.stock;

import com.numericalmethod.algoquant.data.calendar.Country;
import com.numericalmethod.algoquant.data.calendar.TimeZoneUtils;
import com.numericalmethod.algoquant.execution.datatype.product.fx.Currencies;
import java.util.Currency;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * The market places for trading equities could be exchanges, mutual funds (MF), etc.
 *
 * @author Haksun Li
 * @see <a href="http://en.wikipedia.org/wiki/List_of_stock_exchanges">Wikipedia: List of stock
 * exchanges</a>
 */
public enum Exchange {

	/**
	 * Hong Kong Stock Exchange.
	 *
	 * @see <a href="http://www.hkex.com.hk/eng/market/sec_tradinfo/tradcal/tradcal_1.htm">HKEX</a>
	 */
	HKEX(TimeZoneUtils.HONG_KONG, 16, 0, Currencies.HKD, Country.HONG_KONG),
	/**
	 * New York.
	 */
	NEW_YORK(TimeZoneUtils.NEW_YORK, 16, 0, Currencies.USD, Country.UNITED_STATES),//this is a fake exchange for things that are not listed, e.g., indices aggregated over a few exchanges
	/**
	 * NYSE.
	 *
	 * @see <a href="http://www.nyse.com/about/newsevents/1176373643795.html">NYSE</a>
	 */
	NYSE(TimeZoneUtils.NEW_YORK, 16, 0, Currencies.USD, Country.UNITED_STATES),
	/**
	 * NYSE Arca.
	 *
	 * @see <a href="http://www.nyse.com/about/newsevents/1176373643795.html">NYSE</a>
	 */
	NYSE_ARCA(TimeZoneUtils.NEW_YORK, 16, 0, Currencies.USD, Country.UNITED_STATES),
	/**
	 * NYSE MKT, formerly American Stock Exchange (AMEX).
	 *
	 * @see <a href="https://www.nyse.com/markets/nyse-mkt">NYSE MKT</a>
	 */
	AMEX(TimeZoneUtils.NEW_YORK, 16, 0, Currencies.USD, Country.UNITED_STATES),
	/**
	 * NASDAQ.
	 *
	 * @see <a href="http://www.nasdaq.com/about/schedule.stm">NASDAQ</a>
	 */
	NASDAQ(TimeZoneUtils.NEW_YORK, 16, 0, Currencies.USD, Country.UNITED_STATES),
	/**
	 * London Stock Exchange.
	 *
	 * @see <a
	 * href="http://asia.advfn.com/StockExchanges/profile/LSE/LondonStockExchange.html">LSE</a>
	 */
	LSE(TimeZoneUtils.GMT, 16, 30, Currencies.GBP, Country.UNITED_KINGDOM),
	/**
	 * Singapore Exchange.
	 *
	 * @see
	 * <a href="http://www.sgx.com/wps/wcm/connect/mp_en/site/trading_on_sgx/securities_market/securities_trading_and_settlement/Trading+Hours?presentationtemplate=design_lib/PT_Printer_Friendly">SGX</a>
	 */
	SGX(TimeZoneUtils.SINGAPORE, 17, 0, Currencies.SGD, Country.SINGAPORE),

	/**
	 * Shanghai Exchange.
	 */
	SHSE(TimeZoneUtils.SINGAPORE, 15, 0, Currencies.CNY, Country.CHINA),

	/**
	 * Shenzhen Exchange.
	 */
	SZSE(TimeZoneUtils.SINGAPORE, 15, 0, Currencies.CNY, Country.CHINA);

	private final DateTimeZone timeZone;
	private final int closeHour;
	private final int closeMinute;
	private final Currency currency;
	private final Country country;

	private Exchange(DateTimeZone tz,
					 int closeHour,
					 int closeMinute,
					 Currency currency,
					 Country country) {
		this.timeZone = tz;
		this.closeHour = closeHour;
		this.closeMinute = closeMinute;
		this.currency = currency;
		this.country = country;
	}

	public DateTime closingTimeOnDate(DateTime date) {
		return date.withTime(closeHour, closeMinute, 0, 0).withZoneRetainFields(timeZone);
	}

	/**
	 * The time zone the exchange lives in.
	 *
	 * @return the time zone
	 */
	public DateTimeZone timeZone() {
		return timeZone;
	}

	/**
	 * The closing hour of the exchange in the local time zone.
	 *
	 * @return the exchange closing hour in 24-hour format (0-23)
	 */
	public int closeHour() {
		return closeHour;
	}

	/**
	 * The closing minute of the exchange, e.g., 30 for half past an hour, in the local time zone.
	 *
	 * @return the exchange closing minute (0-59)
	 */
	public int closeMinute() {
		return closeMinute;
	}

	/**
	 * The default currency used for the product in this exchange.
	 *
	 * @return the currency for trading
	 */
	public Currency currency() {
		return currency;
	}

	/**
	 * The country that the exchange is located.
	 *
	 * @return the country
	 */
	public Country country() {
		return country;
	}

}
