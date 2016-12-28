package michael.findata.algoquant.execution.strategy;

import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.numericalmethod.algoquant.execution.component.broker.Broker;
import com.numericalmethod.algoquant.execution.component.simulator.message.ChannelMessage;
import com.numericalmethod.algoquant.execution.component.tradeblotter.TradeBlotter;
import com.numericalmethod.algoquant.execution.datatype.depth.Depth;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import com.numericalmethod.algoquant.execution.datatype.order.Order;
import michael.findata.model.Dividend;
import michael.findata.model.Stock;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

import static michael.findata.util.LogUtil.getClassLogger;

public class DisruptorStrategyMarshaller implements Strategy {
	private static Logger LOGGER = getClassLogger();

	private Strategy baseStrategy;

	public DisruptorStrategyMarshaller (Strategy baseStrategy) {
		this.baseStrategy = baseStrategy;
		startDisruptor();
	}

	@Override
	final public void onDepthUpdate(DateTime now, Depth depth, MarketCondition mc, TradeBlotter blotter, Broker broker) {
		disruptor.getRingBuffer().publishEvent(
				(event, sequence, objects) -> event.set(
						(DateTime)objects[0],
						(Depth)objects[1],
						(MarketCondition)objects[2],
						(TradeBlotter)objects[3],
						(Broker)objects[4]),
				now, depth, mc, blotter, broker);
		LOGGER.debug("Channel message queued.");
	}

	private Disruptor<ChannelMessage> disruptor;

	private void startDisruptor () {
		// Specify the size of the ring buffer, must be power of 2.
		int bufferSize = 128;
		// Construct the Disruptor
		disruptor = new Disruptor<>(ChannelMessage::new, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.MULTI, new YieldingWaitStrategy());
		// Connect the handler
//		disruptor.handleEventsWith((event, sequence, endOfBatch) -> onDepthUpdate(event.now, event.depth, event.mc, event.blotter, event.broker));
		// Start the Disruptor, starts all threads running
		disruptor.start();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				LOGGER.info("\t{}\t disruptor shutting down...", baseStrategy);
				disruptor.shutdown();
				LOGGER.info("\t{}\t disruptor shutdown completed.", baseStrategy);
			}
		});
	}

	@Override
	public Logger getLogger() {
		return baseStrategy.getLogger();
	}

	@Override
	public void onStop() {
		baseStrategy.onStop();
	}

	@Override
	public void trySave() {
		baseStrategy.trySave();
	}

	@Override
	public void emailNotification(String titlePrefix) {
		baseStrategy.emailNotification(titlePrefix);
	}

	@Override
	public String notification() {
		return baseStrategy.notification();
	}

	@Override
	public void setRepository(CrudRepository repository) {
		baseStrategy.setRepository(repository);
	}

	@Override
	public CrudRepository getRepository() {
		return baseStrategy.getRepository();
	}

	@Override
	public Collection<Stock> getTargetSecurities() {
		return baseStrategy.getTargetSecurities();
	}

	@Override
	public int hashCode() {
		return baseStrategy.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return baseStrategy.equals(obj);
	}

	@Override
	public String toString() {
		return baseStrategy.toString();
	}

	@Override
	public void onDividend(DateTime now, Dividend dividend, MarketCondition mc, TradeBlotter blotter, Broker broker) {
		baseStrategy.onDividend(now, dividend, mc, blotter, broker);
	}

	private static class DepthUpdateEvent implements UpdateEvent {
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

	private static class OrderUpdatedEvent implements UpdateEvent {
		Order order;
		private void set (Order order) {
			this.order = order;
		}
	}

	private static class MarketConditionUpdateEvent implements UpdateEvent {
	}

	private static class DividendEvent implements UpdateEvent {
	}

	private static interface UpdateEvent {
	}
}