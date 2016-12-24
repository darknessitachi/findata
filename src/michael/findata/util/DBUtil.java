package michael.findata.util;

import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;

import java.io.IOException;

import static michael.findata.util.LogUtil.getClassLogger;

public class DBUtil {
	private static Logger LOGGER = getClassLogger();

	private static Process dbProcess;

	public static Process dealWithDBAccessError (Exception e) {
		if (e instanceof DataAccessException && e.getMessage().contains("JDBC")) {
			LOGGER.warn("{}: {} seems like a DB crash.", e, e.getMessage());
			return tryToStartDB();
		} else {
			LOGGER.warn("Don't know how to handle exception {}: {}\n", e.getClass(), e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public static Process tryToStartDB() {
		try {
			LOGGER.info("Trying to start DB.");
			dbProcess = Runtime.getRuntime().exec("D:\\Development\\MySql-5.6\\bin\\mysqld.exe --defaults-file=\"F:\\MySQL Server Findata\\my.ini\"");
			return dbProcess;
		} catch (IOException ex) {
			LOGGER.warn("Exception caught when trying to start DB {}: {}", ex, ex.getMessage());
			ex.printStackTrace();
			return null;
		}
	}

	public static void tryToStopDB () {
		try {
			LOGGER.info("Trying to stop DB.");
			Runtime.getRuntime().exec("D:\\Development\\MySql-5.6\\bin\\mysqladmin.exe shutdown -uroot -proot");
		} catch (IOException ex) {
			LOGGER.warn("Exception caught when trying to stop DB {}: {}", ex, ex.getMessage());
			ex.printStackTrace();
		}
	}
}