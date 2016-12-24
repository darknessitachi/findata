import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;


import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MultiThreadTestDisruptor implements EventHandler<MultiThreadTestDisruptor.IntEvent> {

	private static long start;

	private String name;

	@Override
	public void onEvent(IntEvent intEvent, long l, boolean b) throws Exception {
		long time = System.currentTimeMillis()-start;
		System.out.println(name+" got it: "+time);
	}

	public static class IntEvent {
		int i;
//		String message;

		public void set(Integer i) {
			this.i = i;
//			this.message = message;
		}
	}

	MultiThreadTestDisruptor (String name) {
		this.name = name;
	}

	public static void main (String args []) throws InterruptedException {
		int size = 99;
		MultiThreadTestDisruptor handlers [] = new MultiThreadTestDisruptor [size];
		for (int i = 0; i < size; i++) {
			handlers[i] = new MultiThreadTestDisruptor(i+"");
		}

		// Executor that will be used to construct new threads for consumers
		Executor executor = Executors.newCachedThreadPool();
		// Specify the size of the ring buffer, must be power of 2.
		int bufferSize = 1024;
		// Construct the Disruptor
		Disruptor<MultiThreadTestDisruptor.IntEvent> disruptor = new Disruptor<>(IntEvent::new, bufferSize, executor);
		// Connect the handler
		disruptor.handleEventsWith(handlers);
		// Start the Disruptor, starts all threads running
		disruptor.start();

		Thread.sleep(2000L);
		RingBuffer<IntEvent> rb = disruptor.getRingBuffer();
		EventTranslator<IntEvent> et = (event, sequence) -> event.set(1);
		rb.publishEvent(et);
		Thread.sleep(2000L);
		start = System.currentTimeMillis();
		rb.publishEvent(et);
		disruptor.shutdown();
	}
}