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
package com.numericalmethod.algoquant.execution.datatype.depth;

import com.numericalmethod.algoquant.execution.component.simulator.event.Event;
import com.numericalmethod.algoquant.execution.datatype.product.Product;

/**
 * This class contains multiple depths on both bid and ask sides for a product.
 *
 * @author Haksun Li
 */
public class Depth implements Event {

	private final Product product;
	/** the number of available levels in the order book */
	private final int nLevels;
	private final double[] bid;
	private final double[] ask;
	private Object otherInfo;

	/**
	 *
	 * @param product
	 * @param price   bid is the same as ask, mainly used in simple simulation
	 */
	public Depth(Product product, final double price) {
		this(product, price, price);
	}

	/**
	 *
	 * @param product
	 * @param prices  bids then asks in ascending order, e.g., ... bid3, bid2, bid1, ask1, ask2, ask3 ...
	 */
	public Depth(Product product, double... prices) {
		this(product, prices, null);
	}

	public Depth(Product product, double[] prices, Object otherInfo) {
		this.product = product;
		this.nLevels = prices.length / 2;

		this.bid = new double[nLevels];
		this.ask = new double[nLevels];

		for (int i = 0; i < nLevels; ++i) {
			this.bid[nLevels - i - 1] = prices[i];
		}

		for (int i = 0; i < nLevels; ++i) {
			this.ask[i] = prices[i + nLevels];
		}

		if (otherInfo == null) {
			this.otherInfo = this;
		} else {
			this.otherInfo = otherInfo;
		}
	}

	public Product product() {
		return product;
	}

	/**
	 * Gets the bid price at a specified level.
	 *
	 * @param level the bid level, counting from 1
	 * @return the bid price at {@code level}
	 */
	public double bid(int level) {
		return bid[level - 1];
	}

	/**
	 * Gets the ask price at a specified level.
	 *
	 * @param level the ask level, counting from 1
	 * @return the ask price at {@code level}
	 */
	public double ask(int level) {
		return ask[level - 1];
	}

	/**
	 * Gets the mid-price between bid and ask. i.e.,
	 * <blockquote><pre>
	 * (bid[0] + ask[0]) / 2.
	 * </pre></blockquote>
	 *
	 * @return the mid price of this depth
	 */
	public double mid() {
		return (bid[0] + ask[0]) / 2;
	}

	/**
	 * Gets the number of depth levels.
	 *
	 * @return the number of levels in this depth
	 */
	public int nLevels() {
		return nLevels;
	}

	public Object otherInfo() {
		return otherInfo;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(product.toString());
		str.append(": [");

		for (int i = -nLevels; i <= nLevels; ++i) {
			if (i < 0) {
				str.append(bid(-i));

				if (i == -1) {
					str.append(" ; ");
				} else {
					str.append(", ");
				}
			} else if (i > 0) {
				str.append(ask(i));

				if (i < nLevels) {
					str.append(", ");
				}
			}
		}

		str.append("]");

		if (otherInfo != null) {
			str.append("(other: ").append(otherInfo).append(")");
		}

		return str.toString();
	}
}