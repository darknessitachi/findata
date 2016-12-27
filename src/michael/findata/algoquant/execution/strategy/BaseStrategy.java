package michael.findata.algoquant.execution.strategy;

import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.EventTranslatorVararg;
import com.lmax.disruptor.dsl.Disruptor;
import com.numericalmethod.algoquant.execution.component.broker.Broker;
import com.numericalmethod.algoquant.execution.component.tradeblotter.TradeBlotter;
import com.numericalmethod.algoquant.execution.datatype.depth.Depth;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import com.numericalmethod.algoquant.execution.datatype.order.Order;
import com.numericalmethod.algoquant.execution.strategy.handler.DepthHandler;
import michael.findata.algoquant.execution.datatype.order.HexinOrder;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static michael.findata.util.LogUtil.getClassLogger;

public abstract class BaseStrategy implements Strategy, DepthHandler {

	private static Logger LOGGER = getClassLogger();

	// true: in simulation/backtesting
	// false: in real trading
	private boolean backtestMode;

	final public boolean isBacktestMode() {
		return backtestMode;
	}

	@Override
	final public void setBacktestMode(boolean backtestMode) {
		this.backtestMode = backtestMode;
	}

	@Override
	final public void onDepthUpdate(DateTime now, Depth depth, MarketCondition mc, TradeBlotter blotter, Broker broker) {
		if (backtestMode) {
			LOGGER.debug("\t{}\t: In backtest, doing depth update assuming single threaded execution.", this);
			doDepthUpdate(now, depth, mc, blotter, broker);
		} else {
			LOGGER.debug("\t{}\t: In real-life trading, doing depth update assuming multi-threaded execution with disruptor", this);
//			disruptor.getRingBuffer().publishEvent(BaseStrategy::translate, now, depth, mc, blotter);
			LOGGER.debug("Depth event queued.");
		}
	}
//	private static final EventTranslatorVararg<DepthEvent> TRANSLATOR =
//			new EventTranslatorVararg<DepthEvent>() {
//				@Override
//				public void translateTo(DepthEvent depthEvent, long sequence, DateTime now, Depth depth, MarketCondition mc, TradeBlotter blotter, Broker broker) {
//
//				}
//			};

	public static void translate(DepthEvent event, long sequence, DateTime now, Depth depth, MarketCondition mc, TradeBlotter blotter) {
		event.set(now, depth, mc, blotter, null);
	}

	abstract public void doDepthUpdate(DateTime now, Depth depth, MarketCondition mc, TradeBlotter blotter, Broker broker);

	private Disruptor<DepthEvent> disruptor;

	final public void startDisruptor () {
		// Executor that will be used to construct new threads for consumers
		Executor executor = Executors.newCachedThreadPool();

		// Specify the size of the ring buffer, must be power of 2.
		int bufferSize = 1024;
		// Construct the Disruptor
		disruptor = new Disruptor<>(DepthEvent::new, bufferSize, executor);
		// Connect the handler
		disruptor.handleEventsWith((event, sequence, endOfBatch) -> onDepthUpdate(event.now, event.depth, event.mc, event.blotter, event.broker));
		// Start the Disruptor, starts all threads running
		disruptor.start();

	}

	private static class DepthEvent {
		DateTime now;
		Depth depth;
		MarketCondition mc;
		TradeBlotter blotter;
		Broker broker;
		private void set (DateTime now, Depth depth, MarketCondition mc, TradeBlotter blotter, Broker broker) {
			this.now = now;
			this.depth = depth;
			this.mc = mc;
			this.blotter = blotter;
			this.broker = broker;
		}
	}
}