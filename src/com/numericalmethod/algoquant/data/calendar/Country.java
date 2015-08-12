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

/**
 * Country name and code.
 *
 * @author Haksun Li
 * @see <a href="http://en.wikipedia.org/wiki/ISO_3166-1">Wikipedia: ISO 3166-1</a>
 */
public enum Country {

	/**
	 * Argentina.
	 */
	ARGENTINA("Argentina", "AR"),
	/**
	 * Australia.
	 */
	AUSTRALIA("Australia", "AU"),
	/**
	 * France.
	 */
	FRANCE("France", "FR"),
	/**
	 * Mainland China
	 */
	CHINA("China", "CN"),
	/**
	 * Hong Kong.
	 */
	HONG_KONG("Hong Kong", "HK"),
	/**
	 * Japan.
	 */
	JAPAN("Japan", "JP"),
	/**
	 * New Zealand.
	 */
	NEW_ZEALAND("New Zealand", "NZ"),
	/**
	 * Saudi Arabia.
	 */
	SAUDI_ARABIA("Saudi Arabia", "SA"),
	/**
	 * Singapore.
	 */
	SINGAPORE("Singapore", "SG"),
	/**
	 * South Korea.
	 */
	SOUTH_KOREA("South Korea", "KR"),
	/**
	 * Spain.
	 */
	SPAIN("Spain", "ES"),
	/**
	 * Switzerland.
	 */
	SWITZERLAND("Switzerland", "CH"),
	/**
	 * Taiwan.
	 */
	TAIWAN("Taiwan", "TW"),
	/**
	 * United Arab Emirates.
	 */
	UNITED_ARAB_EMIRATES("United Arab Emirates", "AE"),
	/**
	 * United Kingdom.
	 */
	UNITED_KINGDOM("United Kingdom", "UK"),
	/**
	 * United States.
	 */
	UNITED_STATES("United States", "US");

	private final String shortName;
	private final String alpha2Code;

	private Country(String shortName, String alpha2Code) {
		this.shortName = shortName;
		this.alpha2Code = alpha2Code;
	}

	/**
	 * Returns the short name.
	 *
	 * @return the short name
	 */
	public String shortName() {
		return shortName;
	}

	/**
	 * Returns the ISO 3166-1 alpha-2 code.
	 *
	 * @return the ISO 3166-1 alpha-2 code
	 */
	public String alpha2Code() {
		return alpha2Code;
	}

	@Override
	public String toString() {
		return shortName();
	}
}