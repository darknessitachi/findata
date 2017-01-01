package michael.findata.algoquant.execution.component.broker;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.numericalmethod.algoquant.execution.datatype.order.Order;

import com.numericalmethod.algoquant.execution.strategy.handler.ExecutionHandler;
import org.apache.logging.log4j.Logger;

import java.util.Collection;

import static michael.findata.util.LogUtil.getClassLogger;

public class DisruptorBrokerMarshaller implements Broker{

	private static Logger LOGGER = getClassLogger();
	private Broker baseBroker;
	private Disruptor<OrderEvent> disruptor;

	DisruptorBrokerMarshaller (Broker baseBroker) {
		this.baseBroker = baseBroker;
		startDisruptor();
	}

	@Override
	public void stop() {
		LOGGER.info("\t{}\t disruptor shutting down...", baseBroker);
		disruptor.shutdown();
		LOGGER.info("\t{}\t disruptor shutdown completed.", baseBroker);
		baseBroker.stop();
	}

	@Override
	public void setOrderListener(Order o, ExecutionHandler handler) {
		baseBroker.setOrderListener(o, handler);
	}

	@Override
	public void sendOrder(Collection<? extends Order> orders) {
		disruptor.getRingBuffer().publishEvent((event, sequence, odrs) -> event.set(true, odrs), orders);
		LOGGER.info("\t{}\tSend order queued.", baseBroker);
	}

	@Override
	public void cancelOrder(Collection<? extends Order> orders) {
		disruptor.getRingBuffer().publishEvent((event, sequence, odrs) -> event.set(false, odrs), orders);
		LOGGER.info("\t{}\tCancel order queued.", baseBroker);
	}

	public void startDisruptor () {
		// Specify the size of the ring buffer, must be power of 2.
		int bufferSize = 128;
		// Construct the Disruptor
		disruptor = new Disruptor<>(OrderEvent::new, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.MULTI, new BlockingWaitStrategy());
		disruptor.handleEventsWith((ordermsg, sequence, endOfBatch) -> {
			if (ordermsg.sendOrCancel) {
				baseBroker.sendOrder(ordermsg.orders);
			} else {
				baseBroker.cancelOrder(ordermsg.orders);
			}
		});
		// Start the Disruptor, starts all threads running
		disruptor.start();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				LOGGER.info("\t{}\t disruptor shutting down...", baseBroker);
				disruptor.shutdown();
				LOGGER.info("\t{}\t disruptor shutdown completed.", baseBroker);
			}
		});
	}

	private static class OrderEvent {
		private boolean sendOrCancel; // true = send | false = cancel
		private Collection<? extends Order> orders;
		public void set (boolean sendOrCancel, Collection<? extends Order> orders) {
			this.sendOrCancel = sendOrCancel;
			this.orders = orders;
		}
	}
}