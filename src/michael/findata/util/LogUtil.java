package michael.findata.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogUtil {
	public static Logger getClassLogger() {
		StackTraceElement a = Thread.currentThread().getStackTrace()[2];
		String a1 = a.getClassName();
		return LogManager.getLogger(a1);
	}
}