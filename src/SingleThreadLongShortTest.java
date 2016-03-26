/**
 * Created by nicky on 2015/12/28.
 */
public class SingleThreadLongShortTest {
	public static void main(String args[]) throws InterruptedException {
		int port = args.length < 1 ? 10001 : Integer.parseInt(args[0]);
		String msg = args.length < 2 ? "Sell|000858|100|100" : args[1];
		//½¨Á¢Socket
		long current = System.currentTimeMillis();
		SeperatedLongShortThreadsTest.sendOrder(msg, port);
		System.out.println ((System.currentTimeMillis() - current) / 1000.0 + " seconds");
	}
}