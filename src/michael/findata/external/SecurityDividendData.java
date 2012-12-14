package michael.findata.external;

import java.util.Date;
import java.util.TreeMap;

public interface SecurityDividendData {
	public TreeMap<Date, SecurityDividendRecord> getDividendRecords ();
}
