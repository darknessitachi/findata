package michael.findata.external;

import org.joda.time.DateTime;

public class SecurityTimeSeriesDatum {

	private DateTime dateTime;
	private int open; // opening price * 1000
	private int high; // highest of the day * 1000
	private int low; // lowest of the day * 1000
	private int close; // closing price * 1000
	private int volume; // volume (#shares)
	private float amount; // amount (#RMB)
	private boolean traded; // is this security traded at this moment?

	public SecurityTimeSeriesDatum (DateTime dateTime, int open, int high, int low, int close, int volume, float amount) {
		this.dateTime = dateTime;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.volume = volume;
		this.amount = amount;
		this.traded = true;
	}

	// dummy data, meaning the security is not traded at this tick.
	public SecurityTimeSeriesDatum (DateTime dateTime) {
		this.dateTime = dateTime;
		this.traded = false;
	}

	public int getOpen() {
		return open;
	}

	public int getHigh() {
		return high;
	}

	public int getLow() {
		return low;
	}

	public int getClose() {
		return close;
	}

	public int getVolume() {
		return volume;
	}

	public float getAmount() {
		return amount;
	}

	public DateTime getDateTime() {
		return dateTime;
	}

	public boolean isTraded() {
		return traded;
	}

	@Override
	public String toString() {
		return dateTime + "\t" + (traded ? "traded\t" : "not traded\t")+ close;
	}
}