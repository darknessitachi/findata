package com.numericalmethod.algoquant.execution.component.simulator.message;

import com.numericalmethod.algoquant.execution.component.broker.Broker;
import com.numericalmethod.algoquant.execution.component.simulator.event.Event;
import com.numericalmethod.algoquant.execution.component.tradeblotter.TradeBlotter;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import org.joda.time.DateTime;

public class ChannelMessage {

	private DateTime time;
	private Event data;
	private MarketCondition marketCondition;
	private TradeBlotter blotter;
	private Broker broker;

	public ChannelMessage () {
	}

	public void set (DateTime time,
					 Event data,
					 MarketCondition marketCondition,
					 TradeBlotter blotter,
					 Broker broker) {
		this.time = time;
		this.data = data;
		this.marketCondition = marketCondition;
		this.blotter = blotter;
		this.broker = broker;
	}

	public ChannelMessage(DateTime time,
						  Event data,
						  MarketCondition marketCondition,
						  TradeBlotter blotter,
						  Broker broker) {
		set(time, data, marketCondition, blotter, broker);
	}

	public DateTime time() {
		return time;
	}

	public Event data() {
		return data;
	}

	public MarketCondition marketCondition() {
		return marketCondition;
	}

	public TradeBlotter blotter() {
		return blotter;
	}

	public Broker broker() {
		return broker;
	}
}
