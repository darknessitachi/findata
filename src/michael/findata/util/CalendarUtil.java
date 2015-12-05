package michael.findata.util;

import org.joda.time.DateTime;
import org.joda.time.Days;

/**
 * Created by nicky on 2015/12/5.
 */
public class CalendarUtil {
	public static int daysBetween (DateTime dt1, DateTime dt2) {
		return Days.daysBetween(dt1.toLocalDate(), dt2.toLocalDate()).getDays();
	}
}
