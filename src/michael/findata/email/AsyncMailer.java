package michael.findata.email;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static michael.findata.util.LogUtil.getClassLogger;

public class AsyncMailer {
	private static Logger LOGGER = getClassLogger();

	private int port = 25;
	private String hostName = "smtp.sina.com";
	private String userName = "8122850yjtang";
	private String password = "108129yao";
	private String from = "8122850yjtang@sina.com";
	private boolean sslOnConnect = false;
	private Disruptor<EmailEvent> disruptor;
//	private ExecutorService executor;

	private static class EmailEvent {
		private String title;
		private String message;
		private String [] to;

		public void set(String title, String message) {
			this.title = title;
			this.message = message;
		}

		public void set(String title, String message, String ... to) {
			this.title = title;
			this.message = message;
			this.to = to;
		}
	}

	public AsyncMailer () {
		// Executor that will be used to construct new threads for consumers
		Executor executor = Executors.newCachedThreadPool();
		// Specify the size of the ring buffer, must be power of 2.
		int bufferSize = 1024;
		// Construct the Disruptor
		disruptor = new Disruptor<>(EmailEvent::new, bufferSize, executor);
		// Connect the handler
		disruptor.handleEventsWith((event, sequence, endOfBatch) -> sendEMail(event.title, event.message, event.to));
		// Start the Disruptor, starts all threads running
		disruptor.start();
	}

	public void stop () {
//		executor.shutdown();
		LOGGER.info("Mailer is shutting down...");
		disruptor.shutdown();
	}

	public void email (String subject, String message) {
		email(subject, message, "8122850@qq.com", "michael.tang@anz.com");
	}

	public void email (String subject, String message, String ... to) {
		RingBuffer<EmailEvent> ringBuffer = disruptor.getRingBuffer();
		ringBuffer.publishEvent((event, sequence, sbj, msg, t) -> event.set(sbj, msg, t), subject, message, to);
		LOGGER.info("Email queued.");
	}

	private void sendEMail(String subject, String message, String ... to) {
		userName = "8122850yjtang";
		from = "8122850yjtang@sina.com";
		new Thread() {
			@Override
			public void run() {
				Email email = new SimpleEmail();
				email.setHostName(hostName);
				email.setSmtpPort(port);
				email.setAuthenticator(new DefaultAuthenticator(userName, password));
				email.setSSLOnConnect(sslOnConnect);
				try {
					email.setFrom(from);
					email.setSubject(subject);
					email.setContent(message, "text/html; charset=utf-8");
					for (String t : to) {
						email.addTo(t);
					}
					LOGGER.info("Sending email: hostName={} / port={} / userName={} / password={} / sslOnConnect={} / from={} / subject={}", hostName, port, userName, password, sslOnConnect, from, subject);
					email.send();
					LOGGER.info("Email titled: {} sent.", subject);
				} catch (EmailException e) {
					LOGGER.warn("Exception {} when sending email. Subject: {}\nMessage: {}", e.getMessage(), subject, message);
					e.printStackTrace();
				}
			}
		}.start();
		LOGGER.info("Email job fired.");
		try {
			Thread.sleep(47000); // delay for 47 seconds so that we don't trigger email server jam protection
		} catch (InterruptedException e) {
			LOGGER.warn("Exception {} when sleeping.", e.getMessage());
			e.printStackTrace();
		}
	}

	public static AsyncMailer instance = new AsyncMailer();
}