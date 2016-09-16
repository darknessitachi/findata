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
package com.numericalmethod.algoquant.execution.component.simulator.message.handler;

import com.numericalmethod.algoquant.execution.component.simulator.event.Event;
import com.numericalmethod.algoquant.execution.component.simulator.event.timer.TimerEvent;
import com.numericalmethod.algoquant.execution.component.simulator.message.ChannelMessage;
import com.numericalmethod.algoquant.execution.datatype.*;
import com.numericalmethod.algoquant.execution.datatype.depth.Depth;
import com.numericalmethod.algoquant.execution.datatype.execution.Execution;
import com.numericalmethod.algoquant.execution.datatype.product.stock.market.MarketSnapshot;
import com.numericalmethod.algoquant.execution.strategy.handler.*;
import michael.findata.algoquant.execution.strategy.handler.DividendHandler;
import michael.findata.model.Dividend;

/**
 * Factories to construct a {@linkplain ChannelMessageHandler} from a {@linkplain StrategyHandler}.
 *
 * @author Chung Lee
 */
public enum ChannelMessageHandlerFactories implements ChannelMessageHandlerFactory {

	DEPTH(Depth.class) {
		@Override
		public ChannelMessageHandler newInstance(final StrategyHandler handler) {
			if (handler instanceof DepthHandler) {
				return new ChannelMessageHandler() {
					@Override
					public void handle(ChannelMessage msg) {
						((DepthHandler) handler).onDepthUpdate(msg.time(),
								(Depth) msg.data(),
								msg.marketCondition(),
								msg.blotter(),
								msg.broker());
					}
				};
			}
			return new NoOpMessageHandler();
		}
	},
	EXECUTION(Execution.class) {
		@Override
		public ChannelMessageHandler newInstance(final StrategyHandler handler) {

			if (handler instanceof ExecutionHandler) {
				return new ChannelMessageHandler() {

					@Override
					public void handle(ChannelMessage msg) {
						((ExecutionHandler) handler).onExecution(msg.time(),
								(Execution) msg.data(),
								msg.marketCondition(),
								msg.blotter(),
								msg.broker());
					}
				};
			}

			return new NoOpMessageHandler();
		}
	},
	STOCK_EOD(StockEOD.class) {
		@Override
		public ChannelMessageHandler newInstance(final StrategyHandler handler) {

			if (handler instanceof StockEODHandler) {
				return new ChannelMessageHandler() {

					@Override
					public void handle(ChannelMessage msg) {
						((StockEODHandler) handler).onEODUpdate(msg.time(),
								(StockEOD) msg.data(),
								msg.marketCondition(),
								msg.blotter(),
								msg.broker());
					}
				};
			}

			return new NoOpMessageHandler();
		}
	},
	TIMER(TimerEvent.class) {
		@Override
		public ChannelMessageHandler newInstance(final StrategyHandler handler) {

			if (handler instanceof TimerHandler) {
				return new ChannelMessageHandler() {

					@Override
					public void handle(ChannelMessage msg) {
						((TimerHandler) handler).onTimerUpdate(msg.time(),
								(TimerEvent) msg.data(),
								msg.marketCondition(),
								msg.blotter(),
								msg.broker());
					}
				};
			}

			return new NoOpMessageHandler();
		}
	},
	MARKET_SNAPSHOT(MarketSnapshot.class) {
		@Override
		public ChannelMessageHandler newInstance(final StrategyHandler handler) {
			if (handler instanceof MarketSnapshotHandler) {
				return new ChannelMessageHandler() {
					@Override
					public void handle(ChannelMessage msg) {
						((MarketSnapshotHandler) handler).onMarketSnapshotUpdate(msg.time(),
								(MarketSnapshot) msg.data(),
								msg.marketCondition(),
								msg.blotter(),
								msg.broker());
					}
				};
			}
			return new NoOpMessageHandler();
		}
	},
	DIVIDEND(Dividend.class) {
		@Override
		public ChannelMessageHandler newInstance(StrategyHandler handler) {
			if (handler instanceof DividendHandler) {
				return new ChannelMessageHandler() {
					@Override
					public void handle(ChannelMessage msg) {
						((DividendHandler) handler).onDividend(msg.time(),
								(Dividend) msg.data(),
								msg.marketCondition(),
								msg.blotter(),
								msg.broker());
					}
				};
			}
			return new NoOpMessageHandler();
		}
	};

	private final Class<? extends Event> eventClass;

	private ChannelMessageHandlerFactories(Class<? extends Event> eventClass) {
		this.eventClass = eventClass;
	}

	@Override
	public Class<? extends Event> eventClass() {
		return eventClass;
	}

	@Override
	public abstract ChannelMessageHandler newInstance(StrategyHandler handler);
}