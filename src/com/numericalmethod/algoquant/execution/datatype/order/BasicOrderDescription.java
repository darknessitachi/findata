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
package com.numericalmethod.algoquant.execution.datatype.order;

import com.numericalmethod.algoquant.execution.datatype.product.Product;
import com.numericalmethod.suanshu.misc.ArgumentAssertion;
import static com.numericalmethod.suanshu.misc.ArgumentAssertion.*;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class represents basic description of an order.
 *
 * @author Haksun Li
 */
public class BasicOrderDescription {

	public static enum Side {

		BUY(+1),
		SELL(-1),
		UNKNOWN(0);

		private final int sign;

		Side(int sign) {
			this.sign = sign;
		}

		/**
		 * A convenient method for pnl or position computation.
		 * {@linkplain #BUY} has sign of +1. {@linkplain #SELL} has sign of -1.
		 *
		 * @return +1 for {@linkplain #BUY}; -1 for {@linkplain #SELL}
		 */
		public int sign() {
			return sign;
		}

		public Side opposite() {
			return valueOf(-1 * sign);
		}

		public static Side valueOf(double qty) {
			return qty > 0 ? BUY : qty < 0 ? SELL : UNKNOWN;
		}
	}

	private final Product product;
	private final Side side;
	private final double quantity;
	private double price;
	private final Map<String, Object> tags;

	public BasicOrderDescription(Product product, Side side, double quantity, double price) {
		assertPositive(quantity, "quantity");

		this.product = product;
		this.side = side;
		this.quantity = quantity;
		this.price = price;
		this.tags = new TreeMap<String, Object>(); // Use TreeMap to order by Key
	}

	/**
	 * Adds a tag to the order. The tag can be retrieved via the key. Note that once a tag has been
	 * added for a particular key it may not be changed.
	 *
	 * @param key the key of the tag
	 * @param tag the tag
	 */
	public void addTag(String key, Object tag) {
		ArgumentAssertion.assertTrue(!tags.containsKey(key),
				"attempted to add duplicate key: %s", key);
		tags.put(key, tag);
	}

	/**
	 * Retrieves the tag at the given key.
	 *
	 * @param key the key of the tag
	 * @return the tag with the given key
	 */
	public Object getTag(String key) {
		return tags.get(key);
	}

	public Product product() {
		return product;
	}

	public Side side() {
		return side;
	}

	public double quantity() {
		return quantity;
	}

	public double price() {
		return price;
	}

	/**
	 * Update price
	 * @param price updated price
	 */
	public void price(double price) {
		this.price = price;
	}

	@Override
	public String toString() {
		String result = String.format("\t%s \t%s @\t%.3f\tfor\t%.3f\t, tags: %s",
				side().toString(),
				product(),
				price(),
				side.sign()*quantity(),
				tags);
		return result;
	}
}
