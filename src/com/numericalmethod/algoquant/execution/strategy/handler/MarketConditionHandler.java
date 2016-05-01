package com.numericalmethod.algoquant.execution.strategy.handler;

import com.numericalmethod.algoquant.execution.component.broker.Broker;
import com.numericalmethod.algoquant.execution.component.tradeblotter.TradeBlotter;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import org.joda.time.DateTime;

public interface MarketConditionHandler extends StrategyHandler{
	/**
	 * Called upon execution of a strategy order.
	 *
	 * @param now       the current time
	 * @param mc        the current market condition
	 * @param blotter   the current trade blotter
	 * @param broker    a broker service
	 */
	public void onMarketConditionUpdate(DateTime now,
							MarketCondition mc,
							TradeBlotter blotter,
							Broker broker);
}