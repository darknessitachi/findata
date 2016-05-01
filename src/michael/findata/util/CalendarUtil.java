package michael.findata.util;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.time.Duration;
import java.time.LocalDateTime;

public class CalendarUtil {
	// dt2's date - dt1's date
	// daysBetween (2015-06-19, 2015-06-22) = 3
	public static int daysBetween (DateTime dt1, DateTime dt2) {
		return Days.daysBetween(dt1.toLocalDate(), dt2.toLocalDate()).getDays();
	}

	// dt2's date - dt1's date
	// daysBetween (2015-06-19, 2015-06-22) = 3
	public static int daysBetween (LocalDateTime dt1, LocalDateTime dt2) {
		return (int) Duration.between(dt1.toLocalDate(), dt2.toLocalDate()).toDays();
	}
}