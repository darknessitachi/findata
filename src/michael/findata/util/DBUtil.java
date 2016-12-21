package michael.findata.util;

import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;

import java.io.IOException;

import static com.numericalmethod.nmutils.NMUtils.getClassLogger;

public class DBUtil {
	private static Logger LOGGER = getClassLogger();

	public static void dealWithDBAccessError (Exception e) {
		if (e instanceof DataAccessException && e.getMessage().contains("JDBC")) {
			LOGGER.warn("{}: {} seems like a DB crash.", e, e.getMessage());
			tryToStartDB();
		} else {
			LOGGER.warn("Don't know how to handle exception {}: {}\n", e.getClass(), e.getMessage());
			e.printStackTrace();
		}
	}

	public static void tryToStartDB () {
		try {
			LOGGER.info("Trying to start DB.");
//			new ProcessBuilder("D:\\Development\\MySql-5.6\\bin\\mysqld.exe", "--defaults-file=\"F:\\MySQL Server Findata\\my.ini\"").start();
			Runtime.getRuntime().exec("D:\\Development\\MySql-5.6\\bin\\mysqld.exe --defaults-file=\"F:\\MySQL Server Findata\\my.ini\"");
		} catch (IOException ex) {
			LOGGER.warn("Exception caught when trying to start DB {}: {}", ex, ex.getMessage());
			ex.printStackTrace();
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
