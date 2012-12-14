package michael.findata.external;

import java.io.IOException;

public interface SecurityTimeSeriesData {
	public SecurityTimeSeriesDatum next();
	public void close ();
	public boolean hasNext ();
}
