package michael.findata.util;

import org.joda.time.DateTime;
import org.joda.time.Days;

/**
 * Created by nicky on 2015/12/5.
 */
public class CalendarUtil {
	// dt2's date - dt1's date
	// daysBetween (2015-06-19, 2015-06-22) = 3
	public static int daysBetween (DateTime dt1, DateTime dt2) {
		return Days.daysBetween(dt1.toLocalDate(), dt2.toLocalDate()).getDays();
	}
}
