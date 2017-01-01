package michael.findata.algoquant.execution.strategy;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.numericalmethod.algoquant.execution.component.broker.Broker;
import com.numericalmethod.algoquant.execution.component.simulator.event.timer.TimerEvent;
import com.numericalmethod.algoquant.execution.component.simulator.message.ChannelMessage;
import com.numericalmethod.algoquant.execution.component.simulator.message.handler.ChannelMessageHandler;
import com.numericalmethod.algoquant.execution.component.tradeblotter.TradeBlotter;
import com.numericalmethod.algoquant.execution.datatype.StockEOD;
import com.numericalmethod.algoquant.execution.datatype.depth.Depth;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import com.numericalmethod.algoquant.execution.datatype.execution.Execution;
import com.numericalmethod.algoquant.execution.datatype.order.Order;
import com.numericalmethod.algoquant.execution.datatype.product.stock.market.MarketSnapshot;
import michael.findata.model.Dividend;
import michael.findata.model.Stock;
import org.apache.logging.log4j.Logger;
import org.apache.xpath.operations.Div;
import org.joda.time.DateTime;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

import static michael.findata.util.LogUtil.getClassLogger;

public class DisruptorStrategyMarshaller implements Strategy, ChannelMessageHandler {
	private static Logger LOGGER = getClassLogger();

	private Strategy baseStrategy;

	public DisruptorStrategyMarshaller (Strategy baseStrategy) {
		this.baseStrategy = baseStrategy;
		startDisruptor();
	}

	@Override
	public void handle(ChannelMessage message) {
		disruptor.getRingBuffer().publishEvent(
				(channelMessage, sequence, msg) -> channelMessage.set(msg.time(), msg.data(), msg.marketCondition(), msg.blotter(), msg.broker()),
				message);
		LOGGER.info("\t{}\tChannel message queued [Message:{}].", baseStrategy, message.data());
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
		LOGGER.info("\t{}\tChannel message queued [Depth].", baseStrategy);
	}

	@Override
	public void onExecution(DateTime now, Execution execution, MarketCondition mc, TradeBlotter blotter, Broker broker) {
		disruptor.getRingBuffer().publishEvent(
				(event, sequence, objects) -> event.set(
						(DateTime)objects[0],
						(Execution)objects[1],
						(MarketCondition)objects[2],
						(TradeBlotter)objects[3],
						(Broker)objects[4]),
				now, execution, mc, blotter, broker);
		LOGGER.info("\t{}\tChannel message queued [Execution].", baseStrategy);
	}

	@Override
	public void onTimerUpdate(DateTime now, TimerEvent evt, MarketCondition mc, TradeBlotter blotter, Broker broker) {
		disruptor.getRingBuffer().publishEvent(
				(event, sequence, objects) -> event.set(
						(DateTime)objects[0],
						(TimerEvent)objects[1],
						(MarketCondition)objects[2],
						(TradeBlotter)objects[3],
						(Broker)objects[4]),
				now, evt, mc, blotter, broker);
		LOGGER.info("\t{}\tChannel message queued [Timer].", baseStrategy);
	}

	@Override
	public void onEODUpdate(DateTime now, StockEOD eod, MarketCondition mc, TradeBlotter blotter, Broker broker) {
		disruptor.getRingBuffer().publishEvent(
				(event, sequence, objects) -> event.set(
						(DateTime)objects[0],
						(StockEOD)objects[1],
						(MarketCondition)objects[2],
						(TradeBlotter)objects[3],
						(Broker)objects[4]),
				now, eod, mc, blotter, broker);
		LOGGER.info("\t{}\tChannel message queued [EOD].", baseStrategy);
	}

	@Override
	public void onMarketSnapshotUpdate(DateTime now, MarketSnapshot snapshot, MarketCondition mc, TradeBlotter blotter, Broker broker) {
		disruptor.getRingBuffer().publishEvent(
				(event, sequence, objects) -> event.set(
						(DateTime)objects[0],
						(MarketSnapshot)objects[1],
						(MarketCondition)objects[2],
						(TradeBlotter)objects[3],
						(Broker)objects[4]),
				now, snapshot, mc, blotter, broker);
		LOGGER.info("\t{}\tChannel message queued [MarketSnapshot].", baseStrategy);
	}

	@Override
	public void onDividend(DateTime now, Dividend dividend, MarketCondition mc, TradeBlotter blotter, Broker broker) {
		disruptor.getRingBuffer().publishEvent(
				(event, sequence, objects) -> event.set(
						(DateTime)objects[0],
						(Dividend)objects[1],
						(MarketCondition)objects[2],
						(TradeBlotter)objects[3],
						(Broker)objects[4]),
				now, dividend, mc, blotter, broker);
		LOGGER.info("\t{}\tChannel message queued [Dividend].", baseStrategy);
	}

	private Disruptor<ChannelMessage> disruptor;

	private void startDisruptor () {
		// Specify the size of the ring buffer, must be power of 2.
		int bufferSize = 128;
		// Construct the Disruptor
		disruptor = new Disruptor<>(ChannelMessage::new, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.MULTI, new BlockingWaitStrategy());
		// Connect the handler
		disruptor.handleEventsWith((chnmsg, sequence, endOfBatch) -> {
			if (chnmsg.data() instanceof Depth) {
				baseStrategy.onDepthUpdate(chnmsg.time(), (Depth) chnmsg.data(), chnmsg.marketCondition(), chnmsg.blotter(), chnmsg.broker());
			} else if (chnmsg.data() instanceof Execution) {
				baseStrategy.onExecution(chnmsg.time(), (Execution) chnmsg.data(), chnmsg.marketCondition(), chnmsg.blotter(), chnmsg.broker());
			} else if (chnmsg.data() instanceof TimerEvent) {
				baseStrategy.onTimerUpdate(chnmsg.time(), (TimerEvent) chnmsg.data(), chnmsg.marketCondition(), chnmsg.blotter(), chnmsg.broker());
			} else if (chnmsg.data() instanceof MarketSnapshot) {
				baseStrategy.onMarketSnapshotUpdate(chnmsg.time(), (MarketSnapshot) chnmsg.data(), chnmsg.marketCondition(), chnmsg.blotter(), chnmsg.broker());
			} else if (chnmsg.data() instanceof Dividend) {
				baseStrategy.onDividend(chnmsg.time(), (Dividend) chnmsg.data(), chnmsg.marketCondition(), chnmsg.blotter(), chnmsg.broker());
			} else if (chnmsg.data() instanceof StockEOD) {
				baseStrategy.onEODUpdate(chnmsg.time(), (StockEOD) chnmsg.data(), chnmsg.marketCondition(), chnmsg.blotter(), chnmsg.broker());
			}
		});
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
		LOGGER.info("\t{}\t disruptor shutting down...", baseStrategy);
		disruptor.shutdown();
		LOGGER.info("\t{}\t disruptor shutdown completed.", baseStrategy);
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
}