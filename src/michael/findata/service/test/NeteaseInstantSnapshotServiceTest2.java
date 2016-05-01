package michael.findata.service.test;

import michael.findata.service.NeteaseInstantSnapshotService;
import michael.findata.util.FinDataConstants;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;

public class NeteaseInstantSnapshotServiceTest2 {
	public static void main (String [] args) throws IOException {
		DateTimeFormatter formatter = DateTimeFormat.forPattern(FinDataConstants.yyyyMMdd);
		NeteaseInstantSnapshotService niss = new NeteaseInstantSnapshotService();

		LocalDate current = formatter.parseLocalDate("20160410");
		LocalDate end = formatter.parseLocalDate("20160410");

		while (Days.daysBetween(current, end).getDays() >= 0) {
			current = current.plusDays(1);
			niss.getDailyData(current).forEach(mc -> {
				System.out.println();
			});
		}
	}
}