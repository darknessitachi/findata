package michael.findata.external;

public interface SecurityTimeSeriesData {
	SecurityTimeSeriesDatum popNext();
	SecurityTimeSeriesDatum peekNext();
	void close();
	boolean hasNext ();
}
