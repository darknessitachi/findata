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
import static java.lang.Math.*;
import java.util.concurrent.atomic.AtomicLong;
import org.joda.time.DateTime;

/**
 * This class represents an order.
 *
 * @author Haksun Li
 */
public abstract class Order extends BasicOrderDescription {

	public static enum OrderState {
		PENDING_SUBMIT,	// IB state: indicates that you have transmitted the order, but have not yet received confirmation that it has been accepted by the order destination.
						// NOTE: This order status is not sent by TWS and should be explicitly set by the API developer when an order is submitted.
		PENDING_CANCEL,	// IB state: indicates that you have sent a request to cancel the order but have not yet received cancel confirmation from the order destination.
						// At this point, your order is not confirmed canceled. You may still receive an execution while your cancellation request is pending.
						// NOTE: This order status is not sent by TWS and should be explicitly set by the API developer when an order is canceled.
		PRE_SUBMITTED,	// IB state: indicates that a simulated order type has been accepted by the IB system and that this order has yet to be elected.
						// The order is held in the IB system until the election criteria are met.
						// At that time the order is transmitted to the order destination as specified.
		API_CANCELLED,	// IB state: after an order has been submitted and before it has been acknowledged, an API client client can request its cancellation, producing this state.
		SUBMITTED,		// IB/TDX state: indicates that your order has been accepted at the order destination and is working.
		CANCELLED,		// IB/TDX state: indicates that the balance of your order has been confirmed canceled by the IB system.
						// This could occur unexpectedly when IB or the destination has rejected your order.
		INACTIVE,		// IB state: indicates that the order has been accepted by the system (simulated orders) or an exchange (native orders)
						// but that currently the order is inactive due to system, exchange or other issues.
		UNFILLED,		// Submitted but unfilled
		FILLED,			// Universal state: indicates that the order has been completely filled.
		PARTIALLY_FILLED
	}

	public static enum OrderExecutionType {

		NO_OP,
		LIMIT_ORDER,
		MARKET_ORDER
	}

	private static final AtomicLong counter = new AtomicLong(0);
	private final long id;
	private DateTime expiry;
	private OrderState state;
	private double filledQuantity = 0.0;

	/**
	 * Gets instructions on how if at all, the order is executed. Note that it is up to the
	 * simulator to ensure that the order is not canceled and that in case of a limit order we do
	 * not buy over/sell under the limit.
	 *
	 * @return the execution type
	 */
	public abstract OrderExecutionType type();

	/**
	 * Returns the opposite of this order.
	 *
	 * @return a new opposite order
	 */
	public abstract Order opposite();

	public Order(Product product, Side side, double quantity, double price) {
		super(product, side, quantity, price);
		this.id = counter.addAndGet(1);
		this.state = OrderState.UNFILLED;
	}

	public Order(Product product, Side side, double quantity) {
		this(product,
				side,
				quantity,
				side == Side.BUY
						? Double.POSITIVE_INFINITY
						: Double.NEGATIVE_INFINITY);
	}

	public long id() {
		return id;
	}

	/**
	 * Sets the time when the order is expired. If no such time is set the order is
	 * good-till-canceled.
	 *
	 * @param expiry the time to cancel this order
	 */
	public void setExpiry(DateTime expiry) {
		this.expiry = expiry;
	}

	/**
	 * Determines whether this order is expired for the current time.
	 *
	 * @param time the current time
	 * @return {@code true} if the order is expired
	 */
	public boolean isExpired(DateTime time) {
		return !(expiry == null || time.isBefore(expiry));
	}

	public void fill(double qty) {
		assertUnfilled();

		filledQuantity = min(quantity(), filledQuantity + qty);
		if (filledQuantity >= quantity()) {
			state = OrderState.FILLED;
		} else {
			state = OrderState.PARTIALLY_FILLED;
		}
	}

	private void assertUnfilled() {
		ArgumentAssertion.assertTrue(
				this.state != OrderState.FILLED,
				"invalid state transition; the order was already filled: " + toString()
		);
	}

	/**
	 * @return the current state of the order
	 */
	public OrderState state() {
		return this.state;
	}

	public void state (OrderState s) {
		this.state = s;
	}

	/**
	 * @return the quantity that has already been filled from this order
	 */
	public double filledQuantity() {
		return filledQuantity;
	}

	@Override
	public String toString() {
		String result = String.format("[%d]: %s, state: %s, filled: %f",
				id,
				super.toString(),
				state(),
				filledQuantity());
		return result;
	}
}