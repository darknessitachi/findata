package michael.findata.external;

import michael.findata.model.AdjFactor;

import java.util.Date;
import java.util.Stack;

public interface SecurityTimeSeriesData {
	SecurityTimeSeriesDatum next();
	void close();
	boolean hasNext ();
}
