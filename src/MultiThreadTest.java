import org.apache.logging.log4j.Logger;
import org.slf4j.impl.StaticLoggerBinder;

import static michael.findata.util.LogUtil.getClassLogger;

public class MultiThreadTest {

	private static Logger LOGGER = getClassLogger();
	static final StaticLoggerBinder binder = StaticLoggerBinder.getSingleton();

	static {
		System.out.println(binder.getLoggerFactory());
		System.out.println(binder.getLoggerFactoryClassStr());
	}

	static long start;
	public static void main (String args []) throws InterruptedException {
		int size = 99;
		ConsumerThread [] t = new ConsumerThread[size];
		for (int i = 0; i < size; i++) {
			t[i] = new ConsumerThread(i+"");
			t[i].start();
		}
		start = System.currentTimeMillis();
		for (int i = 0; i < size; i++) {
			t[i].interrupt();
		}

		for (int j = 0; j < 4; j++) {
			Thread.sleep(4000L);
			System.out.printf("\n\n");
			start = System.currentTimeMillis();
			for (int i = 0; i < size; i++) {
				t[i].interrupt();
			}
		}
	}
	static class ConsumerThread extends Thread {
		String name;

		ConsumerThread(String name) {
			this.name = name;
		}

		@Override
		public void run() {
			int a = 1;
//			while (buffer == a) {
////				System.out.println(name+" a: "+a);
////				System.out.println(name+" buffer: "+buffer);
//			}

			while (true) {
				try {
					Thread.sleep(10000000L);
				} catch (InterruptedException e) {
					long time = System.currentTimeMillis()-start;
					LOGGER.info(name+" got it: "+time);
				}
			}
		}
	}
}