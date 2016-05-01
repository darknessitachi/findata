import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class SeperatedLongShortThreadsTest {
	public static void main(String args[]) throws InterruptedException {
		String msg = args.length < 2 ? "Buy|000858|0.01|100" : args[1];
		int port = args.length < 1 ? 10001 : Integer.parseInt(args[0]);
		//½¨Á¢Socket
		long current = System.currentTimeMillis();

//		new Thread(() -> {
//			sendOrder("Buy|000858|0.01|100", 10001);
//			System.out.println ((System.currentTimeMillis() - current) / 1000.0 + " seconds");
//		}).start();
//		sendOrder("Sell|600550|100|100", 10002);
//		System.out.println ((System.currentTimeMillis() - current) / 1000.0 + " seconds");

		final long [] finalCurrent = new long [1];
		String [] shortCommand = new String [1];

		// short thread
		Thread shortThread = new Thread() {
			@Override
			public void run() {
				while (true) {
					if(Thread.interrupted()){
						dealWithInterrupt();
					}else{
						try {
							Thread.sleep(9999);
							System.out.println("Child awaken.");
						} catch (InterruptedException e) {
							dealWithInterrupt();
						}
					}
				}
			}

			private void dealWithInterrupt () {
				System.out.println("Shorting: "+shortCommand[0]);
				sendOrder(shortCommand[0], 10001);
				System.out.println ("Short thread: "+ (System.currentTimeMillis() - finalCurrent[0]) / 1000.0 + " seconds");
				shortCommand[0] = null;
			}
		};

		// short thread
		shortThread.start();

		// long thread
		while (true) {
			try {
				Thread.sleep(4732);
				System.out.println("Parent interrupting child.");
				finalCurrent[0] = System.currentTimeMillis();
				shortCommand[0] =  "Sell|600550|100|100";
				shortThread.interrupt();
				sendOrder("Buy|000858|0.01|100", 10002);
				System.out.println ("Long thread: "+ (System.currentTimeMillis() - finalCurrent[0]) / 1000.0 + " seconds");
			} catch (InterruptedException e) {
				System.out.println("Parent interrupted.");
			}
		}
	}

	public static void sendOrder(String msg, int port) {
		Socket s = null;
		try {
			s = new Socket(InetAddress.getByName("127.0.0.1"), port);
			OutputStream ops = s.getOutputStream();
			ops.write(msg.getBytes());
			System.out.println(new BufferedReader(new InputStreamReader(s.getInputStream())).readLine());
			ops.close();
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}